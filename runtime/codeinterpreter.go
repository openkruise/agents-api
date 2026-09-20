package runtime

import (
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
)

const (
	// codeInterpreterExecutePath is the endpoint that executes code and
	// streams back NDJSON events.
	codeInterpreterExecutePath = "/execute"
	// codeInterpreterContextsPath is the endpoint managing execution contexts.
	codeInterpreterContextsPath = "/contexts"

	// codeInterpreterErrorLimit caps how much of an error response body is
	// read into memory for error messages.
	codeInterpreterErrorLimit = 4096

	// codeInterpreterMaxLineSize caps the size of a single NDJSON line. It
	// bounds memory usage when a misbehaving server streams a run-away line
	// without newlines; legitimate lines (e.g. results with embedded base64
	// images) stay far below it.
	codeInterpreterMaxLineSize = 16 << 20 // 16 MiB
)

// CodeInterpreter executes code inside a sandbox via the code-interpreter
// service (its own port, 49999 by default, routed through the
// "e2b-sandbox-port" header). The /execute endpoint answers with an NDJSON
// stream: each line is one JSON event (stdout, stderr, result, error, ...),
// mirroring the Java SDK implementation.
type CodeInterpreter struct {
	httpClient      *http.Client // short requests (context CRUD)
	streamingClient *http.Client // NDJSON streaming, no overall timeout
	baseURL         string       // code-interpreter base URL (port routed via header)
	headers         map[string]string
	// maxLineLen caps a single NDJSON line read from the stream.
	maxLineLen int
}

// NewCodeInterpreter creates a CodeInterpreter backed by the given HTTP
// clients. httpClient is used for short requests (context CRUD);
// streamingClient must have no overall timeout because /execute streams
// until the code finishes (cancellation is controlled via ctx). When
// streamingClient is nil, httpClient is used for streaming too.
func NewCodeInterpreter(httpClient, streamingClient *http.Client, baseURL string, headers map[string]string) *CodeInterpreter {
	if streamingClient == nil {
		streamingClient = httpClient
	}
	return &CodeInterpreter{
		httpClient:      httpClient,
		streamingClient: streamingClient,
		baseURL:         baseURL,
		headers:         headers,
		maxLineLen:      codeInterpreterMaxLineSize,
	}
}

// RunCode executes code in the sandbox and waits for the execution stream to
// finish, collecting all events into the returned Execution. Callbacks set
// on opts are invoked in real time as events arrive.
//
// An error raised by the executed code is not returned as a Go error: it is
// reported via Execution.Error. Only transport-level failures (HTTP status,
// network, ctx cancellation) produce a Go error.
func (ci *CodeInterpreter) RunCode(ctx context.Context, code string, opts ...RunCodeOpts) (*Execution, error) {
	opt := RunCodeOpts{}
	if len(opts) > 0 {
		opt = opts[0]
	}

	exec := &Execution{}
	err := ci.runCode(ctx, code, opt, func(event ExecutionEvent) {
		applyEventToExecution(exec, event)
		dispatchRunCodeCallbacks(&opt, event)
	})
	if err != nil {
		return nil, err
	}
	return exec, nil
}

// RunCodeStreaming executes code and processes events as they arrive without
// buffering the whole output, keeping memory usage low for large executions.
// Events are delivered through the callbacks set on opts; no aggregated
// Execution is built.
func (ci *CodeInterpreter) RunCodeStreaming(ctx context.Context, code string, opts ...RunCodeOpts) error {
	opt := RunCodeOpts{}
	if len(opts) > 0 {
		opt = opts[0]
	}
	return ci.runCode(ctx, code, opt, func(event ExecutionEvent) {
		dispatchRunCodeCallbacks(&opt, event)
	})
}

// CreateContext creates a new code execution context with the given working
// directory and language. Empty values fall back to the service defaults
// ("/home/user" and "python").
func (ci *CodeInterpreter) CreateContext(ctx context.Context, cwd string, language string) (*Context, error) {
	payload := struct {
		Cwd      string `json:"cwd,omitempty"`
		Language string `json:"language,omitempty"`
	}{Cwd: cwd, Language: language}
	body, err := json.Marshal(payload)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal create context request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost,
		ci.baseURL+codeInterpreterContextsPath, bytes.NewReader(body))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	ci.setHeaders(req)

	resp, err := ci.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to create context: %w", err)
	}
	defer resp.Body.Close()

	if !isSuccessStatus(resp.StatusCode) {
		return nil, fmt.Errorf("create context failed (status %d): %s", resp.StatusCode, readErrorBody(resp.Body))
	}

	var created Context
	if err := json.NewDecoder(resp.Body).Decode(&created); err != nil {
		return nil, fmt.Errorf("failed to decode create context response: %w", err)
	}
	// Apply the same defaults as the service when fields are omitted.
	if created.Language == "" {
		created.Language = LanguagePython
	}
	if created.Cwd == "" {
		created.Cwd = "/home/user"
	}
	return &created, nil
}

// RemoveContext removes the code execution context with the given ID.
func (ci *CodeInterpreter) RemoveContext(ctx context.Context, contextID string) error {
	if contextID == "" {
		return fmt.Errorf("context ID cannot be empty")
	}

	target := ci.baseURL + codeInterpreterContextsPath + "/" + url.PathEscape(contextID)
	req, err := http.NewRequestWithContext(ctx, http.MethodDelete, target, nil)
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}
	ci.setHeaders(req)

	resp, err := ci.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to remove context: %w", err)
	}
	defer resp.Body.Close()

	if !isSuccessStatus(resp.StatusCode) {
		return fmt.Errorf("remove context failed (status %d): %s", resp.StatusCode, readErrorBody(resp.Body))
	}
	return nil
}

// ListContexts lists all code execution contexts in the sandbox.
func (ci *CodeInterpreter) ListContexts(ctx context.Context) ([]*Context, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet,
		ci.baseURL+codeInterpreterContextsPath, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	ci.setHeaders(req)

	resp, err := ci.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to list contexts: %w", err)
	}
	defer resp.Body.Close()

	if !isSuccessStatus(resp.StatusCode) {
		return nil, fmt.Errorf("list contexts failed (status %d): %s", resp.StatusCode, readErrorBody(resp.Body))
	}

	var contexts []*Context
	if err := json.NewDecoder(resp.Body).Decode(&contexts); err != nil {
		return nil, fmt.Errorf("failed to decode list contexts response: %w", err)
	}
	return contexts, nil
}

// runCode posts the code to /execute and feeds every parsed NDJSON event to
// handler until the stream ends.
func (ci *CodeInterpreter) runCode(ctx context.Context, code string, opt RunCodeOpts, handler func(ExecutionEvent)) error {
	if strings.TrimSpace(code) == "" {
		return fmt.Errorf("code cannot be empty")
	}
	for key := range opt.Envs {
		if strings.TrimSpace(key) == "" {
			return fmt.Errorf("environment variable key cannot be empty")
		}
	}

	body, err := json.Marshal(buildRunCodePayload(code, opt))
	if err != nil {
		return fmt.Errorf("failed to marshal run code request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost,
		ci.baseURL+codeInterpreterExecutePath, bytes.NewReader(body))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	ci.setHeaders(req)

	resp, err := ci.streamingClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to execute code: %w", err)
	}
	defer resp.Body.Close()

	if !isSuccessStatus(resp.StatusCode) {
		return fmt.Errorf("code execution failed (status %d): %s", resp.StatusCode, readErrorBody(resp.Body))
	}

	// NDJSON stream: each line is one JSON event. Malformed lines are
	// skipped so a single bad chunk cannot kill the whole stream, and lines
	// larger than maxLineLen abort it so a run-away line cannot grow
	// memory without bound.
	reader := &limitedLineReader{r: bufio.NewReader(resp.Body), maxLine: ci.maxLineLen}
	for {
		line, err := reader.ReadLine()
		if len(line) > 0 {
			if event, parseErr := parseExecutionEvent(bytes.TrimSpace(line)); parseErr == nil && event != nil {
				handler(event)
			}
		}
		if err == io.EOF {
			return nil
		}
		if err != nil {
			return fmt.Errorf("failed to read execution stream: %w", err)
		}
	}
}

// runCodePayload is the JSON body sent to the /execute endpoint.
type runCodePayload struct {
	Code      string            `json:"code"`
	Language  string            `json:"language,omitempty"`
	ContextID string            `json:"context_id,omitempty"`
	Cwd       string            `json:"cwd,omitempty"`
	Envs      map[string]string `json:"env_vars,omitempty"`
	// Timeout is the server-side execution timeout in seconds.
	Timeout *float64 `json:"timeout,omitempty"`
}

// buildRunCodePayload merges code and RunCodeOpts into the request body.
func buildRunCodePayload(code string, opt RunCodeOpts) runCodePayload {
	payload := runCodePayload{
		Code:      code,
		Cwd:       opt.Cwd,
		Envs:      opt.Envs,
		ContextID: opt.ContextID,
	}
	// Only one of context_id or language is accepted: the context already
	// pins the language.
	if opt.ContextID == "" {
		payload.Language = opt.Language
		if payload.Language == "" {
			payload.Language = LanguagePython
		}
	}
	if opt.Timeout > 0 {
		seconds := opt.Timeout.Seconds()
		payload.Timeout = &seconds
	}
	return payload
}

// parseExecutionEvent decodes a single NDJSON line into an ExecutionEvent.
// Unknown event types yield an error (the caller skips the line).
func parseExecutionEvent(line []byte) (ExecutionEvent, error) {
	var probe struct {
		Type string `json:"type"`
	}
	if err := json.Unmarshal(line, &probe); err != nil {
		return nil, err
	}

	switch probe.Type {
	case "number_of_executions":
		var evt struct {
			ExecutionCount int `json:"execution_count"`
		}
		if err := json.Unmarshal(line, &evt); err != nil {
			return nil, err
		}
		return &NumberOfExecutionsEvent{Count: evt.ExecutionCount}, nil

	case "stdout", "stderr":
		var evt struct {
			Text      string `json:"text"`
			Timestamp string `json:"timestamp"`
		}
		if err := json.Unmarshal(line, &evt); err != nil {
			return nil, err
		}
		if probe.Type == "stdout" {
			return &StdoutEvent{Text: evt.Text, Timestamp: evt.Timestamp}, nil
		}
		return &StderrEvent{Text: evt.Text, Timestamp: evt.Timestamp}, nil

	case "result":
		// Result fields are flattened on the event's top level.
		result := &Result{}
		if err := json.Unmarshal(line, result); err != nil {
			return nil, err
		}
		return &ResultEvent{Result: result}, nil

	case "error":
		var evt struct {
			Name      string `json:"name"`
			Value     string `json:"value"`
			Traceback string `json:"traceback"`
		}
		if err := json.Unmarshal(line, &evt); err != nil {
			return nil, err
		}
		return &ErrorEvent{Err: &ExecutionError{
			Name:      evt.Name,
			Value:     evt.Value,
			Traceback: evt.Traceback,
		}}, nil

	case "end_of_execution":
		return &EndOfExecutionEvent{}, nil

	default:
		return nil, fmt.Errorf("unknown event type: %q", probe.Type)
	}
}

// applyEventToExecution folds a stream event into the aggregated Execution.
func applyEventToExecution(exec *Execution, event ExecutionEvent) {
	switch e := event.(type) {
	case *NumberOfExecutionsEvent:
		exec.ExecutionCount = e.Count
	case *StdoutEvent:
		exec.Logs.Stdout = append(exec.Logs.Stdout, e.Text)
	case *StderrEvent:
		exec.Logs.Stderr = append(exec.Logs.Stderr, e.Text)
	case *ResultEvent:
		exec.Results = append(exec.Results, e.Result)
	case *ErrorEvent:
		exec.Error = e.Err
	}
}

// dispatchRunCodeCallbacks invokes the user callbacks registered on opts for
// the given event.
func dispatchRunCodeCallbacks(opts *RunCodeOpts, event ExecutionEvent) {
	switch e := event.(type) {
	case *StdoutEvent:
		if opts.OnStdout != nil {
			opts.OnStdout(*e)
		}
	case *StderrEvent:
		if opts.OnStderr != nil {
			opts.OnStderr(*e)
		}
	case *ResultEvent:
		if opts.OnResult != nil {
			opts.OnResult(e.Result)
		}
	case *ErrorEvent:
		if opts.OnError != nil {
			opts.OnError(e.Err)
		}
	}
	if opts.OnEvent != nil {
		opts.OnEvent(event)
	}
}

func (ci *CodeInterpreter) setHeaders(req *http.Request) {
	for k, v := range ci.headers {
		req.Header.Set(k, v)
	}
}

// limitedLineReader reads newline-delimited lines from r while enforcing a
// per-line size cap, so a line without newlines cannot grow memory without
// bound. Lines include the trailing newline, mirroring
// bufio.Reader.ReadBytes('\n').
type limitedLineReader struct {
	r       *bufio.Reader
	maxLine int
	buf     []byte
}

// ReadLine returns the next line. The returned slice is only valid until
// the next call to ReadLine. At EOF the final unterminated line is returned
// together with io.EOF.
func (l *limitedLineReader) ReadLine() ([]byte, error) {
	l.buf = l.buf[:0]
	for {
		chunk, err := l.r.ReadSlice('\n')
		if len(chunk) > 0 {
			l.buf = append(l.buf, chunk...)
			if len(l.buf) > l.maxLine {
				return nil, fmt.Errorf("line exceeds maximum length %d bytes", l.maxLine)
			}
		}
		if err == nil {
			return l.buf, nil
		}
		if err == bufio.ErrBufferFull {
			continue
		}
		return l.buf, err
	}
}

// readErrorBody reads a bounded snippet of an error response body for
// inclusion in error messages.
func readErrorBody(body io.Reader) string {
	b, _ := io.ReadAll(io.LimitReader(body, codeInterpreterErrorLimit))
	return string(b)
}

// isSuccessStatus reports whether statusCode is a 2xx code.
func isSuccessStatus(statusCode int) bool {
	return statusCode >= http.StatusOK && statusCode < http.StatusMultipleChoices
}
