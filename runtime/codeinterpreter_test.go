package runtime

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"reflect"
	"strings"
	"testing"
	"time"
)

// newTestCodeInterpreter spins up a code-interpreter test server and returns
// a CodeInterpreter client pointing at it, plus the server for inspection.
func newTestCodeInterpreter(t *testing.T, handler http.HandlerFunc) (*CodeInterpreter, *httptest.Server) {
	t.Helper()
	server := httptest.NewServer(handler)
	t.Cleanup(server.Close)

	headers := map[string]string{
		"Authorization":    "Basic cm9vdDo=",
		"e2b-sandbox-id":   "sbx-1",
		"e2b-sandbox-port": "49999",
	}
	return NewCodeInterpreter(server.Client(), server.Client(), server.URL, headers), server
}

// eventName returns a readable name for an ExecutionEvent, for assertions.
func eventName(ev ExecutionEvent) string {
	switch ev.(type) {
	case *NumberOfExecutionsEvent:
		return "number_of_executions"
	case *StdoutEvent:
		return "stdout"
	case *StderrEvent:
		return "stderr"
	case *ResultEvent:
		return "result"
	case *ErrorEvent:
		return "error"
	case *EndOfExecutionEvent:
		return "end_of_execution"
	default:
		return "unknown"
	}
}

func writeNDJSON(t *testing.T, w http.ResponseWriter, lines ...string) {
	t.Helper()
	w.Header().Set("Content-Type", "application/x-ndjson")
	w.WriteHeader(http.StatusOK)
	for _, line := range lines {
		if _, err := io.WriteString(w, line+"\n"); err != nil {
			t.Errorf("failed to write ndjson line: %v", err)
		}
	}
}

func TestRunCodeAggregatesExecution(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != "/execute" {
			t.Errorf("path = %q, want /execute", r.URL.Path)
		}
		var payload runCodePayload
		if err := json.NewDecoder(r.Body).Decode(&payload); err != nil {
			t.Errorf("failed to decode request body: %v", err)
		}
		if payload.Code != "print('hi')" {
			t.Errorf("code = %q", payload.Code)
		}
		if payload.Language != "python" {
			t.Errorf("language = %q, want python", payload.Language)
		}

		writeNDJSON(t, w,
			`{"type":"number_of_executions","execution_count":3}`,
			`{"type":"stdout","text":"hi\n","timestamp":"2026-01-01T00:00:00Z"}`,
			`{"type":"stderr","text":"warn","timestamp":"2026-01-01T00:00:01Z"}`,
			`{"type":"result","text":"42","is_main_result":true}`,
			`{"type":"result","png":"<png>","is_main_result":false}`,
			`{"type":"end_of_execution"}`,
		)
	})

	exec, err := interpreter.RunCode(context.Background(), "print('hi')")
	if err != nil {
		t.Fatalf("RunCode() error = %v", err)
	}

	if exec.ExecutionCount != 3 {
		t.Errorf("ExecutionCount = %d, want 3", exec.ExecutionCount)
	}
	if len(exec.Logs.Stdout) != 1 || exec.Logs.Stdout[0] != "hi\n" {
		t.Errorf("stdout = %v, want [hi\\n]", exec.Logs.Stdout)
	}
	if len(exec.Logs.Stderr) != 1 || exec.Logs.Stderr[0] != "warn" {
		t.Errorf("stderr = %v, want [warn]", exec.Logs.Stderr)
	}
	if len(exec.Results) != 2 {
		t.Fatalf("results = %d, want 2", len(exec.Results))
	}
	if exec.Results[0].Text != "42" || !exec.Results[0].MainResult {
		t.Errorf("main result = %+v", exec.Results[0])
	}
	if exec.Results[1].PNG != "<png>" || exec.Results[1].MainResult {
		t.Errorf("png result = %+v", exec.Results[1])
	}
	if exec.Error != nil {
		t.Errorf("Error = %v, want nil", exec.Error)
	}
	if got := exec.Text(); got != "42" {
		t.Errorf("Text() = %q, want 42", got)
	}
}

func TestRunCodeCollectsError(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		writeNDJSON(t, w,
			`{"type":"stdout","text":"before"}`,
			`{"type":"error","name":"ValueError","value":"bad value","traceback":"Traceback..."}`,
			`{"type":"end_of_execution"}`,
		)
	})

	exec, err := interpreter.RunCode(context.Background(), "raise ValueError")
	if err != nil {
		t.Fatalf("RunCode() error = %v, want nil (code error is not a transport error)", err)
	}
	if exec.Error == nil {
		t.Fatal("Execution.Error = nil, want populated")
	}
	if exec.Error.Name != "ValueError" || exec.Error.Value != "bad value" || exec.Error.Traceback != "Traceback..." {
		t.Errorf("Execution.Error = %+v", exec.Error)
	}
	if got := exec.Error.Error(); !strings.Contains(got, "ValueError: bad value") {
		t.Errorf("Error() message = %q", got)
	}
}

func TestRunCodeInvokesCallbacks(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		writeNDJSON(t, w,
			`{"type":"stdout","text":"a"}`,
			`{"type":"stderr","text":"b"}`,
			`{"type":"result","text":"r"}`,
			`{"type":"error","name":"E","value":"v","traceback":"t"}`,
			`{"type":"end_of_execution"}`,
		)
	})

	var events []string
	exec, err := interpreter.RunCode(context.Background(), "x", RunCodeOpts{
		OnStdout: func(e StdoutEvent) { events = append(events, "stdout:"+e.Text) },
		OnStderr: func(e StderrEvent) { events = append(events, "stderr:"+e.Text) },
		OnResult: func(res *Result) { events = append(events, "result:"+res.Text) },
		OnError:  func(e *ExecutionError) { events = append(events, "error:"+e.Name) },
		OnEvent:  func(ev ExecutionEvent) { events = append(events, "event:"+eventName(ev)) },
	})
	if err != nil {
		t.Fatalf("RunCode() error = %v", err)
	}
	// Aggregation still happens alongside callbacks.
	if len(exec.Logs.Stdout) != 1 || len(exec.Results) != 1 || exec.Error == nil {
		t.Errorf("aggregation incomplete: %+v", exec)
	}

	joined := strings.Join(events, ",")
	for _, want := range []string{"stdout:a", "stderr:b", "result:r", "error:E", "event:end_of_execution"} {
		if !strings.Contains(joined, want) {
			t.Errorf("callbacks missing %q, got: %s", want, joined)
		}
	}
}

func TestRunCodeStreamingWithoutAggregation(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		writeNDJSON(t, w,
			`{"type":"stdout","text":"s1"}`,
			`{"type":"end_of_execution"}`,
		)
	})

	var got []string
	err := interpreter.RunCodeStreaming(context.Background(), "code", RunCodeOpts{
		OnEvent: func(ev ExecutionEvent) {
			if _, ok := ev.(*StdoutEvent); ok {
				got = append(got, "stdout")
			}
			if _, ok := ev.(*EndOfExecutionEvent); ok {
				got = append(got, "end")
			}
		},
	})
	if err != nil {
		t.Fatalf("RunCodeStreaming() error = %v", err)
	}
	if strings.Join(got, ",") != "stdout,end" {
		t.Errorf("events = %v, want [stdout end]", got)
	}
}

func TestRunCodeSkipsMalformedLines(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		writeNDJSON(t, w,
			"not-json",
			`{"type":"stdout","text":"ok"}`,
			`{"type":"totally_unknown","x":1}`,
			`{"type":"end_of_execution"}`,
		)
	})

	exec, err := interpreter.RunCode(context.Background(), "code")
	if err != nil {
		t.Fatalf("RunCode() error = %v", err)
	}
	if len(exec.Logs.Stdout) != 1 || exec.Logs.Stdout[0] != "ok" {
		t.Errorf("stdout = %v, want [ok]", exec.Logs.Stdout)
	}
}

func TestRunCodeHTTPError(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "boom", http.StatusInternalServerError)
	})

	exec, err := interpreter.RunCode(context.Background(), "code")
	if err == nil {
		t.Fatal("RunCode() error = nil, want error")
	}
	if exec != nil {
		t.Errorf("execution = %+v, want nil", exec)
	}
	if !strings.Contains(err.Error(), "500") || !strings.Contains(err.Error(), "boom") {
		t.Errorf("error = %v, want status and body included", err)
	}
}

func TestRunCodeEmptyCode(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		t.Error("server should not be called with empty code")
	})
	if _, err := interpreter.RunCode(context.Background(), "  "); err == nil {
		t.Fatal("RunCode(empty) error = nil, want error")
	}
}

func TestRunCodeRejectsEmptyEnvKey(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		t.Error("server should not be called with an invalid env key")
	})
	if _, err := interpreter.RunCode(context.Background(), "code", RunCodeOpts{
		Envs: map[string]string{" ": "value"},
	}); err == nil {
		t.Fatal("RunCode(blank env key) error = nil, want error")
	}
}

func TestRunCodeContextCancellation(t *testing.T) {
	// release lets the handler return deterministically during cleanup even
	// if the server does not detect the client disconnect in time.
	release := make(chan struct{})
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		// Consume the request body: the HTTP server only watches for client
		// disconnects (background read) once the body has been drained.
		_, _ = io.Copy(io.Discard, r.Body)
		writeNDJSON(t, w, `{"type":"stdout","text":"first"}`)
		// Hold the stream open until the client cancels or the test ends.
		select {
		case <-r.Context().Done():
		case <-release:
		}
	})
	t.Cleanup(func() { close(release) })

	ctx, cancel := context.WithCancel(context.Background())
	done := make(chan error, 1)
	go func() {
		_, err := interpreter.RunCode(ctx, "code")
		done <- err
	}()

	time.Sleep(100 * time.Millisecond)
	cancel()

	select {
	case err := <-done:
		if err == nil {
			t.Fatal("RunCode() error = nil, want cancellation error")
		}
		if !errors.Is(err, context.Canceled) {
			t.Errorf("error = %v, want context.Canceled", err)
		}
	case <-time.After(5 * time.Second):
		t.Fatal("RunCode() did not return after cancellation")
	}
}

func TestRunCodeSendsOptions(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		if got := r.Header.Get("e2b-sandbox-port"); got != "49999" {
			t.Errorf("e2b-sandbox-port = %q, want 49999", got)
		}
		var payload runCodePayload
		if err := json.NewDecoder(r.Body).Decode(&payload); err != nil {
			t.Errorf("decode body: %v", err)
		}
		if payload.Language != "javascript" {
			t.Errorf("language = %q, want javascript", payload.Language)
		}
		if payload.Cwd != "/tmp/work" {
			t.Errorf("cwd = %q", payload.Cwd)
		}
		if payload.Envs["KEY"] != "value" {
			t.Errorf("envs = %v", payload.Envs)
		}
		if payload.Timeout == nil || *payload.Timeout != 30 {
			t.Errorf("timeout = %v, want 30", payload.Timeout)
		}
		writeNDJSON(t, w, `{"type":"end_of_execution"}`)
	})

	_, err := interpreter.RunCode(context.Background(), "code", RunCodeOpts{
		Language: LanguageJavaScript,
		Cwd:      "/tmp/work",
		Envs:     map[string]string{"KEY": "value"},
		Timeout:  30 * time.Second,
	})
	if err != nil {
		t.Fatalf("RunCode() error = %v", err)
	}
}

func TestRunCodeSendsContextID(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		var payload runCodePayload
		if err := json.NewDecoder(r.Body).Decode(&payload); err != nil {
			t.Fatalf("decode body: %v", err)
		}
		if payload.ContextID != "ctx-42" {
			t.Errorf("context_id = %q, want ctx-42", payload.ContextID)
		}
		if payload.Language != "" {
			t.Errorf("language = %q, want empty alongside context_id", payload.Language)
		}
		writeNDJSON(t, w, `{"type":"end_of_execution"}`)
	})

	_, err := interpreter.RunCode(context.Background(), "code", RunCodeOpts{ContextID: "ctx-42"})
	if err != nil {
		t.Fatalf("RunCode() error = %v", err)
	}
}

func TestContextManagement(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		switch {
		case r.Method == http.MethodPost && r.URL.Path == "/contexts":
			if got := r.Header.Get("e2b-sandbox-port"); got != "49999" {
				t.Errorf("e2b-sandbox-port = %q, want 49999", got)
			}
			var body struct {
				Cwd      string `json:"cwd"`
				Language string `json:"language"`
			}
			if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
				t.Errorf("decode body: %v", err)
			}
			if body.Cwd != "/home/user/proj" || body.Language != "python" {
				t.Errorf("create body = %+v", body)
			}
			w.WriteHeader(http.StatusOK)
			_, _ = io.WriteString(w, `{"id":"ctx-1","language":"python","cwd":"/home/user/proj"}`)

		case r.Method == http.MethodGet && r.URL.Path == "/contexts":
			w.WriteHeader(http.StatusOK)
			_, _ = io.WriteString(w, `[{"id":"ctx-1","language":"python","cwd":"/home/user/proj"}]`)

		case r.Method == http.MethodDelete && r.URL.Path == "/contexts/ctx-1":
			w.WriteHeader(http.StatusOK)

		default:
			t.Errorf("unexpected request: %s %s", r.Method, r.URL.Path)
			http.NotFound(w, r)
		}
	})

	// Create
	created, err := interpreter.CreateContext(context.Background(), "/home/user/proj", LanguagePython)
	if err != nil {
		t.Fatalf("CreateContext() error = %v", err)
	}
	if created.ID != "ctx-1" || created.Language != "python" || created.Cwd != "/home/user/proj" {
		t.Errorf("created = %+v", created)
	}

	// List
	contexts, err := interpreter.ListContexts(context.Background())
	if err != nil {
		t.Fatalf("ListContexts() error = %v", err)
	}
	if len(contexts) != 1 || contexts[0].ID != "ctx-1" {
		t.Errorf("contexts = %+v", contexts)
	}

	// Remove
	if err := interpreter.RemoveContext(context.Background(), "ctx-1"); err != nil {
		t.Fatalf("RemoveContext() error = %v", err)
	}
}

func TestCreateContextAppliesDefaults(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = io.WriteString(w, `{"id":"ctx-9"}`)
	})

	created, err := interpreter.CreateContext(context.Background(), "", "")
	if err != nil {
		t.Fatalf("CreateContext() error = %v", err)
	}
	if created.Language != "python" || created.Cwd != "/home/user" {
		t.Errorf("defaults missing: %+v", created)
	}
}

func TestRemoveContextRequiresID(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		t.Error("server should not be called with empty context ID")
	})
	if err := interpreter.RemoveContext(context.Background(), ""); err == nil {
		t.Fatal("RemoveContext(empty) error = nil, want error")
	}
}

func TestContextManagementHTTPError(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "denied", http.StatusForbidden)
	})

	if _, err := interpreter.CreateContext(context.Background(), "", ""); err == nil {
		t.Error("CreateContext() error = nil, want error")
	}
	if _, err := interpreter.ListContexts(context.Background()); err == nil {
		t.Error("ListContexts() error = nil, want error")
	}
	if err := interpreter.RemoveContext(context.Background(), "ctx-1"); err == nil {
		t.Error("RemoveContext() error = nil, want error")
	}
}

func TestParseExecutionEventUnknownType(t *testing.T) {
	if _, err := parseExecutionEvent([]byte(`{"type":"whatever"}`)); err == nil {
		t.Fatal("unknown type error = nil, want error")
	}
	if _, err := parseExecutionEvent([]byte(`{"type":123}`)); err == nil {
		t.Fatal("invalid type field error = nil, want error")
	}
}

func TestParseResultEventRichFormats(t *testing.T) {
	line := `{"type":"result","text":"t","html":"<h1>","markdown":"# m","svg":"<svg>","png":"p",` +
		`"jpeg":"j","pdf":"f","latex":"x^2","javascript":"js","is_main_result":true,` +
		`"json":{"a":1},"data":{"b":[1,2]},"extra":{"c":true}}`
	event, err := parseExecutionEvent([]byte(line))
	if err != nil {
		t.Fatalf("parseExecutionEvent() error = %v", err)
	}
	resultEvent, ok := event.(*ResultEvent)
	if !ok {
		t.Fatalf("event type = %T, want *ResultEvent", event)
	}
	res := resultEvent.Result
	if res.Text != "t" || res.HTML != "<h1>" || res.Markdown != "# m" || res.SVG != "<svg>" ||
		res.PNG != "p" || res.JPEG != "j" || res.PDF != "f" || res.LaTeX != "x^2" || res.Javascript != "js" {
		t.Errorf("string formats = %+v", res)
	}
	if !res.MainResult {
		t.Error("is_main_result = false, want true")
	}
	if res.JSON["a"] != float64(1) {
		t.Errorf("json = %v", res.JSON)
	}
	data, _ := res.Data["b"].([]interface{})
	if len(data) != 2 {
		t.Errorf("data.b = %v", res.Data["b"])
	}
	if res.Extra["c"] != true {
		t.Errorf("extra = %v", res.Extra)
	}
}

func TestResultFormats(t *testing.T) {
	// All formats present: returned in the fixed order used by the
	// Java getFormats() / Python formats() implementations.
	full := &Result{
		Text:       "t",
		HTML:       "<h1>",
		Markdown:   "# m",
		SVG:        "<svg>",
		PNG:        "p",
		JPEG:       "j",
		PDF:        "f",
		LaTeX:      "x^2",
		JSON:       map[string]interface{}{"a": 1},
		Javascript: "js",
		Data:       map[string]interface{}{"b": 1},
	}
	want := []string{"text", "html", "markdown", "svg", "png", "jpeg", "pdf", "latex", "json", "javascript", "data"}
	if got := full.Formats(); !reflect.DeepEqual(got, want) {
		t.Errorf("Formats() = %v, want %v", got, want)
	}

	// Empty result carries no formats.
	if got := (&Result{}).Formats(); len(got) != 0 {
		t.Errorf("empty Formats() = %v, want empty", got)
	}

	// Only the populated fields are reported.
	partial := &Result{PNG: "p", MainResult: true}
	if got := partial.Formats(); !reflect.DeepEqual(got, []string{"png"}) {
		t.Errorf("partial Formats() = %v, want [png]", got)
	}
}

func TestRunCodeStreamingDeliversAllEvents(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		writeNDJSON(t, w,
			`{"type":"number_of_executions","execution_count":7}`,
			`{"type":"stdout","text":"out","timestamp":"2026-01-01T00:00:00Z"}`,
			`{"type":"stderr","text":"err","timestamp":"2026-01-01T00:00:01Z"}`,
			`{"type":"result","text":"42","is_main_result":true}`,
			`{"type":"error","name":"ValueError","value":"boom","traceback":"tb"}`,
			`{"type":"end_of_execution"}`,
		)
	})

	var names []string
	var sawResult *Result
	var sawErr *ExecutionError
	err := interpreter.RunCodeStreaming(context.Background(), "print('hi')", RunCodeOpts{
		OnResult: func(r *Result) { sawResult = r },
		OnError:  func(e *ExecutionError) { sawErr = e },
		OnEvent:  func(ev ExecutionEvent) { names = append(names, eventName(ev)) },
	})
	if err != nil {
		t.Fatalf("RunCodeStreaming() error = %v", err)
	}

	// The streaming API must deliver every event type in arrival order,
	// including number_of_executions and end_of_execution which the
	// aggregated Execution does not expose.
	want := []string{
		"number_of_executions",
		"stdout",
		"stderr",
		"result",
		"error",
		"end_of_execution",
	}
	if !reflect.DeepEqual(names, want) {
		t.Errorf("events = %v, want %v", names, want)
	}
	if sawResult == nil || sawResult.Text != "42" {
		t.Errorf("OnResult result = %+v, want text 42", sawResult)
	}
	if sawErr == nil || sawErr.Name != "ValueError" {
		t.Errorf("OnError error = %+v, want ValueError", sawErr)
	}
}

func TestRunCodeRejectsOversizedLine(t *testing.T) {
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		// A single line without a newline, larger than the configured cap.
		if _, err := w.Write(bytes.Repeat([]byte("a"), 200)); err != nil {
			t.Errorf("failed to write oversized line: %v", err)
		}
	})
	interpreter.maxLineLen = 64

	_, err := interpreter.RunCode(context.Background(), "print('hi')")
	if err == nil {
		t.Fatal("RunCode() error = nil, want line-length error")
	}
	if !strings.Contains(err.Error(), "line exceeds maximum length") {
		t.Errorf("error = %q, want line-length error", err)
	}
}

func TestRunCodeAcceptsLineWithinLimit(t *testing.T) {
	// A line close to the cap but still within it must be parsed normally.
	text := strings.Repeat("x", 220)
	line := `{"type":"stdout","text":"` + text + `"}`
	if len(line) >= 256 {
		t.Fatalf("test line length %d must stay below the cap 256", len(line))
	}

	var got string
	interpreter, _ := newTestCodeInterpreter(t, func(w http.ResponseWriter, r *http.Request) {
		writeNDJSON(t, w, line)
	})
	interpreter.maxLineLen = 256

	err := interpreter.RunCodeStreaming(context.Background(), "print('hi')", RunCodeOpts{
		OnStdout: func(e StdoutEvent) { got = e.Text },
	})
	if err != nil {
		t.Fatalf("RunCodeStreaming() error = %v", err)
	}
	if got != text {
		t.Errorf("stdout text length = %d, want %d", len(got), len(text))
	}
}
