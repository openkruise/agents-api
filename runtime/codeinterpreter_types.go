package runtime

import "time"

// Supported code execution languages for the code interpreter.
const (
	// LanguagePython executes code as Python (default).
	LanguagePython = "python"
	// LanguageJavaScript executes code as JavaScript.
	LanguageJavaScript = "javascript"
	// LanguageTypeScript executes code as TypeScript.
	LanguageTypeScript = "typescript"
	// LanguageR executes code as R.
	LanguageR = "r"
	// LanguageJava executes code as Java.
	LanguageJava = "java"
	// LanguageBash executes code as Bash.
	LanguageBash = "bash"
)

// ExecutionError describes an error raised by the executed code itself
// (e.g. a Python exception), reported via an "error" stream event.
type ExecutionError struct {
	// Name is the error class name, e.g. "ValueError".
	Name string
	// Value is the error message.
	Value string
	// Traceback is the full stack trace of the error.
	Traceback string
}

// Error implements the error interface.
func (e *ExecutionError) Error() string {
	return e.Name + ": " + e.Value + "\n" + e.Traceback
}

// Logs collects the text printed to stdout and stderr during an execution.
type Logs struct {
	// Stdout holds the stdout chunks in arrival order.
	Stdout []string
	// Stderr holds the stderr chunks in arrival order.
	Stderr []string
}

// Result represents the data displayed as a result of executing code,
// similar to a Jupyter notebook cell output. Only the formats produced by
// the executed code are populated; the rest stay zero-valued.
type Result struct {
	// Text is the plain-text representation.
	Text string `json:"text"`
	// HTML is the HTML representation.
	HTML string `json:"html"`
	// Markdown is the Markdown representation.
	Markdown string `json:"markdown"`
	// SVG is the SVG image representation.
	SVG string `json:"svg"`
	// PNG is the PNG image representation.
	PNG string `json:"png"`
	// JPEG is the JPEG image representation.
	JPEG string `json:"jpeg"`
	// PDF is the PDF representation.
	PDF string `json:"pdf"`
	// LaTeX is the LaTeX representation.
	LaTeX string `json:"latex"`
	// JSON is a structured JSON object representation.
	JSON map[string]interface{} `json:"json"`
	// Javascript is the JavaScript representation.
	Javascript string `json:"javascript"`
	// Data holds additional structured data attached to the result.
	Data map[string]interface{} `json:"data"`
	// Extra holds extra metadata attached to the result.
	Extra map[string]interface{} `json:"extra"`
	// MainResult reports whether this is the main (final) result.
	MainResult bool `json:"is_main_result"`
}

// Formats returns the names of the formats present in this Result, in a
// fixed order, e.g. ["text", "png"]. A string format is reported when its
// field is non-empty; json and data when their maps are non-nil.
func (r *Result) Formats() []string {
	var formats []string
	if r.Text != "" {
		formats = append(formats, "text")
	}
	if r.HTML != "" {
		formats = append(formats, "html")
	}
	if r.Markdown != "" {
		formats = append(formats, "markdown")
	}
	if r.SVG != "" {
		formats = append(formats, "svg")
	}
	if r.PNG != "" {
		formats = append(formats, "png")
	}
	if r.JPEG != "" {
		formats = append(formats, "jpeg")
	}
	if r.PDF != "" {
		formats = append(formats, "pdf")
	}
	if r.LaTeX != "" {
		formats = append(formats, "latex")
	}
	if r.JSON != nil {
		formats = append(formats, "json")
	}
	if r.Javascript != "" {
		formats = append(formats, "javascript")
	}
	if r.Data != nil {
		formats = append(formats, "data")
	}
	return formats
}

// Execution is the aggregated result of a completed code execution: all
// results, logs and the error (if the executed code raised one) collected
// from the execution stream.
type Execution struct {
	// Results holds all displayable results in arrival order.
	Results []*Result
	// Logs holds the stdout/stderr chunks in arrival order.
	Logs Logs
	// Error is the error raised by the executed code, nil when it ran
	// successfully. It is distinct from transport-level errors, which are
	// returned as regular Go errors.
	Error *ExecutionError
	// ExecutionCount is the number of executions reported by the
	// interpreter. It stays 0 when no number_of_executions event is
	// received.
	ExecutionCount int
}

// Text returns the text of the main result, or "" when there is none.
func (e *Execution) Text() string {
	for _, r := range e.Results {
		if r.MainResult && r.Text != "" {
			return r.Text
		}
	}
	return ""
}

// Context is an isolated code execution context with its own working
// directory and language environment. Code executed with the same ContextID
// shares state (variables, imports) across runs.
type Context struct {
	// ID is the server-assigned context identifier.
	ID string `json:"id"`
	// Language is the context language (default "python").
	Language string `json:"language"`
	// Cwd is the context working directory (default "/home/user").
	Cwd string `json:"cwd"`
}

// ExecutionEvent is a single event from the code execution NDJSON stream.
// Each line of the /execute response is one event; the concrete type is
// determined by the event's "type" field.
type ExecutionEvent interface {
	executionEvent()
}

// NumberOfExecutionsEvent reports the number of prior executions in the
// current context.
type NumberOfExecutionsEvent struct {
	Count int
}

// StdoutEvent carries a chunk of stdout output.
type StdoutEvent struct {
	Text      string
	Timestamp string
}

// StderrEvent carries a chunk of stderr output.
type StderrEvent struct {
	Text      string
	Timestamp string
}

// ResultEvent carries a displayable result.
type ResultEvent struct {
	Result *Result
}

// ErrorEvent carries an error raised by the executed code.
type ErrorEvent struct {
	Err *ExecutionError
}

// EndOfExecutionEvent marks the end of the execution stream.
type EndOfExecutionEvent struct{}

func (*NumberOfExecutionsEvent) executionEvent() {}
func (*StdoutEvent) executionEvent()             {}
func (*StderrEvent) executionEvent()             {}
func (*ResultEvent) executionEvent()             {}
func (*ErrorEvent) executionEvent()              {}
func (*EndOfExecutionEvent) executionEvent()     {}

// RunCodeOpts carries the options for a code execution: the execution
// parameters (language, cwd, env vars, timeout, context) and the streaming
// callbacks invoked in real time as events arrive.
type RunCodeOpts struct {
	// Language is the code language: LanguagePython (default),
	// LanguageJavaScript, LanguageTypeScript, LanguageR, LanguageJava or
	// LanguageBash. Ignored when ContextID is set.
	Language string
	// Cwd is the working directory for the execution. Empty means the
	// service default.
	Cwd string
	// Envs are environment variables for the execution.
	Envs map[string]string
	// Timeout is the server-side execution timeout, sent to the server in
	// seconds. Zero means the service default.
	Timeout time.Duration
	// ContextID executes the code inside an existing context (created via
	// CreateContext). When set, Language is ignored because the context
	// already pins the language.
	ContextID string
	// OnStdout, when non-nil, is invoked for every stdout event as it
	// arrives.
	OnStdout func(StdoutEvent)
	// OnStderr, when non-nil, is invoked for every stderr event as it
	// arrives.
	OnStderr func(StderrEvent)
	// OnResult, when non-nil, is invoked for every result as it arrives.
	OnResult func(*Result)
	// OnError, when non-nil, is invoked when the executed code raises an
	// error.
	OnError func(*ExecutionError)
	// OnEvent, when non-nil, is invoked for every stream event, including
	// NumberOfExecutionsEvent and EndOfExecutionEvent.
	OnEvent func(ExecutionEvent)
}
