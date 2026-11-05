package runtime

import (
	"context"
	"encoding/base64"
	"encoding/binary"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/openkruise/agents-api/runtime/envd/process"
	"github.com/openkruise/agents-api/runtime/envd/process/processconnect"
	"google.golang.org/protobuf/proto"
)

// newTestCommands spins up a process service test server and returns a
// Commands client pointing at it.
func newTestCommands(t *testing.T, handler http.HandlerFunc) (*Commands, *httptest.Server) {
	t.Helper()
	server := httptest.NewServer(handler)
	t.Cleanup(server.Close)

	rpc := processconnect.NewProcessClient(server.Client(), server.URL)
	headers := map[string]string{
		"Authorization":    "Basic cm9vdDo=",
		"e2b-sandbox-id":   "sbx-1",
		"e2b-sandbox-port": "49983",
	}
	return NewCommands(rpc, headers), server
}

// connectFrames envelopes protobuf messages into Connect Protocol
// server-streaming frames: 1 byte flags + 4 bytes big-endian length + payload.
// It returns an error instead of calling t.Fatalf because it runs on the
// httptest handler goroutine, where FailNow must not be called.
func connectFrames(msgs ...proto.Message) ([]byte, error) {
	var buf []byte
	for _, m := range msgs {
		payload, err := proto.Marshal(m)
		if err != nil {
			return nil, fmt.Errorf("marshal %T: %w", m, err)
		}
		frame := make([]byte, 5+len(payload))
		frame[0] = 0x00
		binary.BigEndian.PutUint32(frame[1:5], uint32(len(payload)))
		copy(frame[5:], payload)
		buf = append(buf, frame...)
	}
	return buf, nil
}

// connectEndOfStreamFrame builds the Connect Protocol end-of-stream frame:
// flags 0b10 plus a JSON connectEndStreamMessage (empty, i.e. no error).
// It must be the last frame on the stream so the client sees a clean EOF.
func connectEndOfStreamFrame() []byte {
	payload := []byte("{}")
	frame := make([]byte, 5+len(payload))
	frame[0] = 0b00000010 // connectFlagEnvelopeEndStream
	binary.BigEndian.PutUint32(frame[1:5], uint32(len(payload)))
	copy(frame[5:], payload)
	return frame
}

// startStreamResult carries what the httptest handler observed back to the
// test goroutine. Receiving it over a channel also creates the
// happens-before edge that makes reading auth race-free: response bytes
// arriving over TCP do not, by themselves, synchronize the two goroutines.
type startStreamResult struct {
	auth string
	err  error
}

// recordStartStream adapts writeStartStream into an httptest handler. The
// handler goroutine never touches *testing.T (FailNow must run on the test
// goroutine); it only sends one buffered result that the test goroutine
// receives after the RPC returns.
func recordStartStream(results chan<- startStreamResult) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		auth, err := writeStartStream(w, r)
		results <- startStreamResult{auth: auth, err: err}
	}
}

// receiveStartStream waits for the handler's result on the test goroutine.
// Only call it after the RPC succeeded: a client-side validation failure
// never reaches the handler, so nothing would ever be sent and receiving
// would block forever. Check the RPC error first, then receive.
func receiveStartStream(t *testing.T, results <-chan startStreamResult) startStreamResult {
	t.Helper()
	res := <-results
	if res.err != nil {
		t.Fatalf("test server: %v", res.err)
	}
	return res
}

// writeStartStream answers a Start RPC with a start event followed by
// stdout and end events, and reports the incoming Authorization header. It
// runs on the httptest handler goroutine and therefore must not touch
// *testing.T; failures travel back through the returned error instead.
// Marshal failures are answered with a 500 so the client fails fast rather
// than hanging on a truncated response; a wrong path is still answered with
// the regular stream and reported via the error.
func writeStartStream(w http.ResponseWriter, r *http.Request) (string, error) {
	frames, err := connectFrames(
		startEvent(42),
		stdoutEvent("out\n"),
		endEvent(0),
	)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return "", err
	}
	w.Header().Set("Content-Type", "application/connect+proto")
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write(frames)
	_, _ = w.Write(connectEndOfStreamFrame())

	if r.URL.Path != "/process.Process/Start" {
		return "", fmt.Errorf("path = %q, want /process.Process/Start", r.URL.Path)
	}
	return r.Header.Get("Authorization"), nil
}

func startEvent(pid uint32) *process.StartResponse {
	return &process.StartResponse{
		Event: &process.ProcessEvent{
			Event: &process.ProcessEvent_Start{
				Start: &process.ProcessEvent_StartEvent{Pid: pid},
			},
		},
	}
}

func stdoutEvent(text string) *process.StartResponse {
	return &process.StartResponse{
		Event: &process.ProcessEvent{
			Event: &process.ProcessEvent_Data{
				Data: &process.ProcessEvent_DataEvent{
					Output: &process.ProcessEvent_DataEvent_Stdout{
						Stdout: []byte(text),
					},
				},
			},
		},
	}
}

func endEvent(exitCode int32) *process.StartResponse {
	return &process.StartResponse{
		Event: &process.ProcessEvent{
			Event: &process.ProcessEvent_End{
				End: &process.ProcessEvent_EndEvent{
					ExitCode: exitCode,
					Exited:   true,
				},
			},
		},
	}
}

func TestUserAuthHeader(t *testing.T) {
	// Matches the envd Basic-auth convention: "user:" with empty password.
	want := "Basic " + base64.StdEncoding.EncodeToString([]byte("nobody:"))
	if got := userAuthHeader("nobody"); got != want {
		t.Errorf("userAuthHeader() = %q, want %q", got, want)
	}
	if got := userAuthHeader("user"); got != "Basic dXNlcjo=" {
		t.Errorf("userAuthHeader(user) = %q, want Basic dXNlcjo=", got)
	}
}

func TestStartSendsUserAuthorization(t *testing.T) {
	results := make(chan startStreamResult, 1)
	commands, _ := newTestCommands(t, recordStartStream(results))

	handle, err := commands.Start(context.Background(), "whoami", RunOpts{User: "nobody"})
	if err != nil {
		t.Fatalf("Start() error = %v", err)
	}
	srv := receiveStartStream(t, results)
	defer handle.Disconnect()

	if handle.Pid() != 42 {
		t.Errorf("Pid() = %d, want 42", handle.Pid())
	}
	if want := "Basic " + base64.StdEncoding.EncodeToString([]byte("nobody:")); srv.auth != want {
		t.Errorf("Authorization = %q, want %q", srv.auth, want)
	}
}

func TestStartKeepsDefaultAuthorization(t *testing.T) {
	results := make(chan startStreamResult, 1)
	commands, _ := newTestCommands(t, recordStartStream(results))

	handle, err := commands.Start(context.Background(), "whoami")
	if err != nil {
		t.Fatalf("Start() error = %v", err)
	}
	srv := receiveStartStream(t, results)
	defer handle.Disconnect()

	if srv.auth != "Basic cm9vdDo=" {
		t.Errorf("Authorization = %q, want the default Basic cm9vdDo=", srv.auth)
	}
}

func TestRunWithUserWaitsForCompletion(t *testing.T) {
	results := make(chan startStreamResult, 1)
	commands, _ := newTestCommands(t, recordStartStream(results))

	res, err := commands.Run(context.Background(), "whoami", RunOpts{User: "nobody"})
	if err != nil {
		t.Fatalf("Run() error = %v", err)
	}
	srv := receiveStartStream(t, results)
	if res.ExitCode != 0 {
		t.Errorf("ExitCode = %d, want 0", res.ExitCode)
	}
	if res.Stdout != "out\n" {
		t.Errorf("Stdout = %q, want out\\n", res.Stdout)
	}
	if want := "Basic " + base64.StdEncoding.EncodeToString([]byte("nobody:")); srv.auth != want {
		t.Errorf("Authorization = %q, want %q", srv.auth, want)
	}
}

// startRequestResult carries the StartRequest process config observed by the
// httptest handler back to the test goroutine.
type startRequestResult struct {
	cmd  string
	args []string
	err  error
}

// recordStartRequest parses the StartRequest protobuf from the request body,
// answers with the regular stream and reports the requested shell. The
// handler goroutine never touches *testing.T; failures travel back through
// the error field.
func recordStartRequest(results chan<- startRequestResult) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		body, err := io.ReadAll(r.Body)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			results <- startRequestResult{err: err}
			return
		}
		var req process.StartRequest
		// The connect server-streaming request body is an envelope frame
		// (1 byte flags + 4 bytes big-endian length + payload); bare
		// protobuf bodies are for unary endpoints only.
		if len(body) < 5 || binary.BigEndian.Uint32(body[1:5]) != uint32(len(body)-5) {
			err := fmt.Errorf("invalid connect envelope: %d body bytes", len(body))
			http.Error(w, err.Error(), http.StatusInternalServerError)
			results <- startRequestResult{err: err}
			return
		}
		if err := proto.Unmarshal(body[5:], &req); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			results <- startRequestResult{err: err}
			return
		}
		if _, err := writeStartStream(w, r); err != nil {
			results <- startRequestResult{err: err}
			return
		}
		p := req.GetProcess()
		results <- startRequestResult{cmd: p.GetCmd(), args: p.GetArgs()}
	}
}

// TestStartShellOverride verifies RunOpts.Shell: the override must reach the
// StartRequest process config, and an unset Shell keeps the /bin/bash
// default.
func TestStartShellOverride(t *testing.T) {
	results := make(chan startRequestResult, 2)
	commands, _ := newTestCommands(t, recordStartRequest(results))

	handle, err := commands.Start(context.Background(), "echo hi", RunOpts{Shell: "/bin/sh"})
	if err != nil {
		t.Fatalf("Start() error = %v", err)
	}
	// Start succeeded, so the handler already wrote the response — and its
	// send happens after that write — meaning a result is guaranteed to be
	// waiting on the channel.
	srv := <-results
	defer handle.Disconnect()
	if srv.err != nil {
		t.Fatalf("test server: %v", srv.err)
	}
	if srv.cmd != "/bin/sh" {
		t.Errorf("shell override: Cmd = %q, want /bin/sh", srv.cmd)
	}
	if fmt.Sprint(srv.args) != fmt.Sprint([]string{"-l", "-c", "echo hi"}) {
		t.Errorf("shell override: Args = %q, want [\"-l\", \"-c\", \"echo hi\"]", srv.args)
	}

	handle2, err := commands.Start(context.Background(), "echo hi")
	if err != nil {
		t.Fatalf("Start() error = %v", err)
	}
	srv2 := <-results
	defer handle2.Disconnect()
	if srv2.err != nil {
		t.Fatalf("test server: %v", srv2.err)
	}
	if srv2.cmd != "/bin/bash" {
		t.Errorf("default shell: Cmd = %q, want /bin/bash", srv2.cmd)
	}
	if fmt.Sprint(srv2.args) != fmt.Sprint([]string{"-l", "-c", "echo hi"}) {
		t.Errorf("default shell: Args = %q, want [\"-l\", \"-c\", \"echo hi\"]", srv2.args)
	}
}

func TestStartRejectsUserWithColon(t *testing.T) {
	// If the client-side validation ever disappears, the handler answers
	// 500 so the test fails with an HTTP error instead of silently
	// authenticating as username "a" with password "b".
	commands, _ := newTestCommands(t, func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "start request should not have been sent", http.StatusInternalServerError)
	})

	_, err := commands.Start(context.Background(), "whoami", RunOpts{User: "a:b"})
	if err == nil {
		t.Fatal("Start() with User containing ':' should fail")
	}
	if !strings.Contains(err.Error(), "must not contain ':'") {
		t.Errorf("Start() error = %v, want a User validation error", err)
	}
}

// blockingStartHandler never answers the Start RPC; it blocks until the test
// closes done. The client's request-timeout watchdog must abort the wait.
func blockingStartHandler(done <-chan struct{}) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		<-done
	}
}

// TestStartRequestTimeoutPerCall verifies that RunOpts.RequestTimeout bounds
// the wait for the start event: a server that never answers must fail fast
// with a readable timeout error mentioning the option name.
func TestStartRequestTimeoutPerCall(t *testing.T) {
	done := make(chan struct{})
	commands, _ := newTestCommands(t, blockingStartHandler(done))
	// close(done) must run before server.Close (registered inside
	// newTestCommands): t.Cleanup is LIFO, so register it afterwards.
	t.Cleanup(func() { close(done) })

	start := time.Now()
	_, err := commands.Start(context.Background(), "sleep 100", RunOpts{RequestTimeout: 100 * time.Millisecond})
	if err == nil {
		t.Fatal("Start() should fail when the start event never arrives")
	}
	if !strings.Contains(err.Error(), "waiting for the start event") ||
		!strings.Contains(err.Error(), "RunOpts.RequestTimeout") {
		t.Errorf("Start() error = %v, want a request-timeout error", err)
	}
	if elapsed := time.Since(start); elapsed > 5*time.Second {
		t.Errorf("Start() took %s to fail, want it aborted by the 100ms watchdog", elapsed)
	}
}

// TestStartRequestTimeoutDisarmedAfterStartEvent verifies the official
// request_timeout semantics: the watchdog only bounds opening the stream. A
// stream that opens immediately but stays quiet for longer than
// RequestTimeout must still deliver its end event.
func TestStartRequestTimeoutDisarmedAfterStartEvent(t *testing.T) {
	commands, _ := newTestCommands(t, func(w http.ResponseWriter, r *http.Request) {
		frames, err := connectFrames(startEvent(42))
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/connect+proto")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write(frames)
		w.(http.Flusher).Flush()

		// Stay silent far longer than the request timeout (generous
		// margin for a loaded CI): the watchdog must
		// have been disarmed by the start event above.
		time.Sleep(2 * time.Second)
		rest, err := connectFrames(endEvent(0))
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		_, _ = w.Write(rest)
		_, _ = w.Write(connectEndOfStreamFrame())
	})

	res, err := commands.Run(context.Background(), "quiet-cmd", RunOpts{RequestTimeout: 150 * time.Millisecond})
	if err != nil {
		t.Fatalf("Run() error = %v, want the stream to survive past the disarmed request timeout", err)
	}
	if res.ExitCode != 0 {
		t.Errorf("ExitCode = %d, want 0", res.ExitCode)
	}
}

// TestStartOverallTimeout verifies RunOpts.Timeout bounds the whole stream:
// unlike RequestTimeout (disarmed by the start event), a server that opens
// the stream but never sends the end event must still be cut off at the
// overall deadline with a readable error mentioning the option name.
func TestStartOverallTimeout(t *testing.T) {
	done := make(chan struct{})
	commands, _ := newTestCommands(t, func(w http.ResponseWriter, r *http.Request) {
		frames, err := connectFrames(startEvent(42))
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/connect+proto")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write(frames)
		w.(http.Flusher).Flush()
		// Never send the end event: the overall timeout must cut the stream.
		<-done
	})
	// close(done) must run before server.Close (registered inside
	// newTestCommands): t.Cleanup is LIFO, so register it afterwards.
	t.Cleanup(func() { close(done) })

	handle, err := commands.Start(context.Background(), "quiet-cmd", RunOpts{Timeout: 150 * time.Millisecond})
	if err != nil {
		t.Fatalf("Start() error = %v, want the start event to arrive first", err)
	}

	start := time.Now()
	_, err = handle.Wait(nil, nil)
	if err == nil {
		t.Fatal("Wait() should fail when the end event never arrives")
	}
	if !strings.Contains(err.Error(), "RunOpts.Timeout") {
		t.Errorf("Wait() error = %v, want an overall-timeout error", err)
	}
	if elapsed := time.Since(start); elapsed > 5*time.Second {
		t.Errorf("Wait() took %s to fail, want it aborted by the 150ms deadline", elapsed)
	}
}

// TestStartRequestTimeoutClientLevelFallback verifies the official fallback
// chain: an unset per-call RequestTimeout falls back to the client-level
// request timeout configured via NewCommandsWithStreaming.
func TestStartRequestTimeoutClientLevelFallback(t *testing.T) {
	done := make(chan struct{})
	server := httptest.NewServer(blockingStartHandler(done))
	// LIFO order: close(done) (registered second) runs before server.Close,
	// so the blocked handler exits before the server waits for it.
	t.Cleanup(server.Close)
	t.Cleanup(func() { close(done) })

	rpc := processconnect.NewProcessClient(server.Client(), server.URL)
	commands := NewCommandsWithStreaming(rpc, rpc, map[string]string{
		"Authorization":    "Basic cm9vdDo=",
		"e2b-sandbox-id":   "sbx-1",
		"e2b-sandbox-port": "49983",
	}, 120*time.Millisecond)

	start := time.Now()
	_, err := commands.Start(context.Background(), "sleep 100")
	if err == nil {
		t.Fatal("Start() should fail when the start event never arrives")
	}
	if !strings.Contains(err.Error(), "waiting for the start event") ||
		!strings.Contains(err.Error(), "client-level request timeout") {
		t.Errorf("Start() error = %v, want a request-timeout error naming the client-level fallback", err)
	}
	if strings.Contains(err.Error(), "RunOpts.RequestTimeout") {
		t.Errorf("Start() error = %v, must not name RunOpts.RequestTimeout when the per-call option was not set", err)
	}
	if elapsed := time.Since(start); elapsed > 5*time.Second {
		t.Errorf("Start() took %s to fail, want it aborted by the 120ms client-level watchdog", elapsed)
	}
}

// connectStartEvent builds a ConnectResponse carrying a start event, the
// first frame a Connect subscription answers with.
func connectStartEvent(pid uint32) *process.ConnectResponse {
	return &process.ConnectResponse{
		Event: &process.ProcessEvent{
			Event: &process.ProcessEvent_Start{
				Start: &process.ProcessEvent_StartEvent{Pid: pid},
			},
		},
	}
}

// TestConnectToProcessUsesStreamingClient guards the regression where
// ConnectToProcess ran on the unary client: that client's overall HTTP
// timeout would cut a subscription to a process running longer than the
// client-level request timeout. The unary server fails the test if hit;
// only the streaming server may answer.
func TestConnectToProcessUsesStreamingClient(t *testing.T) {
	const wantPid = uint32(7)
	hit := make(chan string, 2)

	streamServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		frames, err := connectFrames(connectStartEvent(wantPid))
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/connect+proto")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write(frames)
		_, _ = w.Write(connectEndOfStreamFrame())
		hit <- "streaming"
	}))
	t.Cleanup(streamServer.Close)
	unaryServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.Error(w, "streaming RPCs must not hit the unary client", http.StatusInternalServerError)
		hit <- "unary"
	}))
	t.Cleanup(unaryServer.Close)

	unaryRPC := processconnect.NewProcessClient(unaryServer.Client(), unaryServer.URL)
	streamRPC := processconnect.NewProcessClient(streamServer.Client(), streamServer.URL)
	commands := NewCommandsWithStreaming(unaryRPC, streamRPC, map[string]string{
		"Authorization":    "Basic cm9vdDo=",
		"e2b-sandbox-id":   "sbx-1",
		"e2b-sandbox-port": "49983",
	}, time.Second)

	handle, err := commands.ConnectToProcess(context.Background(), wantPid)
	if err != nil {
		t.Fatalf("ConnectToProcess() error = %v", err)
	}
	defer handle.Disconnect()
	if got := handle.Pid(); got != wantPid {
		t.Errorf("Pid() = %d, want %d", got, wantPid)
	}

	select {
	case got := <-hit:
		if got != "streaming" {
			t.Fatalf("ConnectToProcess hit the %q server, want the streaming server", got)
		}
	case <-time.After(5 * time.Second):
		t.Fatal("ConnectToProcess did not reach any test server")
	}
}

// TestConnectToProcessRequestTimeoutPerCall verifies ConnectOpts.RequestTimeout:
// a server that never answers the connect request must fail fast with a
// readable timeout error naming the per-call option (previously only the
// client-level request timeout applied to ConnectToProcess).
func TestConnectToProcessRequestTimeoutPerCall(t *testing.T) {
	done := make(chan struct{})
	commands, _ := newTestCommands(t, blockingStartHandler(done))
	t.Cleanup(func() { close(done) })

	start := time.Now()
	_, err := commands.ConnectToProcess(context.Background(), 42, ConnectOpts{RequestTimeout: 100 * time.Millisecond})
	if err == nil {
		t.Fatal("ConnectToProcess() should fail when the connect event never arrives")
	}
	if !strings.Contains(err.Error(), "waiting for the connect event") ||
		!strings.Contains(err.Error(), "ConnectOpts.RequestTimeout") {
		t.Errorf("ConnectToProcess() error = %v, want a request-timeout error naming the per-call option", err)
	}
	if strings.Contains(err.Error(), "client-level") {
		t.Errorf("ConnectToProcess() error = %v, must not name the client-level fallback when the per-call option was set", err)
	}
	if elapsed := time.Since(start); elapsed > 5*time.Second {
		t.Errorf("ConnectToProcess() took %s to fail, want it aborted by the 100ms watchdog", elapsed)
	}
}

// TestConnectToProcessOverallTimeout verifies ConnectOpts.Timeout bounds the
// whole connect stream: a subscription that opens but never receives the end
// event must be cut at the deadline with a readable error naming the option
// (previously this path had no overall limit at all).
func TestConnectToProcessOverallTimeout(t *testing.T) {
	done := make(chan struct{})
	commands, _ := newTestCommands(t, func(w http.ResponseWriter, r *http.Request) {
		frames, err := connectFrames(connectStartEvent(42))
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/connect+proto")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write(frames)
		w.(http.Flusher).Flush()
		// Never send the end event: the overall timeout must cut the stream.
		<-done
	})
	t.Cleanup(func() { close(done) })

	handle, err := commands.ConnectToProcess(context.Background(), 42, ConnectOpts{Timeout: 150 * time.Millisecond})
	if err != nil {
		t.Fatalf("ConnectToProcess() error = %v, want the connect event to arrive first", err)
	}

	start := time.Now()
	_, err = handle.Wait(nil, nil)
	if err == nil {
		t.Fatal("Wait() should fail when the end event never arrives")
	}
	if !strings.Contains(err.Error(), "ConnectOpts.Timeout") {
		t.Errorf("Wait() error = %v, want an overall-timeout error naming ConnectOpts.Timeout", err)
	}
	if elapsed := time.Since(start); elapsed > 5*time.Second {
		t.Errorf("Wait() took %s to fail, want it aborted by the 150ms deadline", elapsed)
	}
}

// TestConnectToProcessOverallTimeoutBeforeStartEvent verifies the attribution
// of ConnectOpts.Timeout when it fires before the connect start event: the
// opening RPC wait must name ConnectOpts.Timeout like the Start path names
// RunOpts.Timeout, instead of surfacing the raw deadline error.
func TestConnectToProcessOverallTimeoutBeforeStartEvent(t *testing.T) {
	done := make(chan struct{})
	commands, _ := newTestCommands(t, blockingStartHandler(done))
	t.Cleanup(func() { close(done) })

	start := time.Now()
	_, err := commands.ConnectToProcess(context.Background(), 42, ConnectOpts{
		Timeout:        100 * time.Millisecond,
		RequestTimeout: 5 * time.Second, // keep the watchdog out of the way
	})
	if err == nil {
		t.Fatal("ConnectToProcess() should fail when the connect event never arrives")
	}
	if !strings.Contains(err.Error(), "ConnectOpts.Timeout") ||
		!strings.Contains(err.Error(), "timed out after 100ms") {
		t.Errorf("ConnectToProcess() error = %v, want an overall-timeout error naming ConnectOpts.Timeout", err)
	}
	if strings.Contains(err.Error(), "RequestTimeout") {
		t.Errorf("ConnectToProcess() error = %v, must not name the request timeout when the overall deadline fired first", err)
	}
	if elapsed := time.Since(start); elapsed > 5*time.Second {
		t.Errorf("ConnectToProcess() took %s to fail, want it aborted by the 100ms deadline", elapsed)
	}
}

// TestStartWithStructLiteralCommands guards the zero requestTimeout left
// by struct-literal construction (&Commands{...} bypasses the constructors):
// time.AfterFunc(0, ...) fires immediately, so before the guard every Start
// failed with "timed out after 0s". The fallback must restore the package
// default.
func TestStartWithStructLiteralCommands(t *testing.T) {
	results := make(chan startStreamResult, 1)
	server := httptest.NewServer(recordStartStream(results))
	t.Cleanup(server.Close)

	rpc := processconnect.NewProcessClient(server.Client(), server.URL)
	// Struct literal, no constructor: requestTimeout stays zero.
	commands := &Commands{
		Rpc:       rpc,
		StreamRPC: rpc,
		headers: map[string]string{
			"Authorization":    "Basic cm9vdDo=",
			"e2b-sandbox-id":   "sbx-1",
			"e2b-sandbox-port": "49983",
		},
	}

	res, err := commands.Run(context.Background(), "echo hi")
	if err != nil {
		t.Fatalf("Run() error = %v, want struct-literal construction to fall back to the default request timeout", err)
	}
	receiveStartStream(t, results)
	if res.ExitCode != 0 {
		t.Errorf("ExitCode = %d, want 0", res.ExitCode)
	}
}

// TestStartWithStructLiteralMissingStreamRPC guards the nil StreamRPC left
// by struct-literal construction (&Commands{Rpc: rpc} bypasses the
// constructors). Before the streaming-client split this construction worked
// because Start/ConnectToProcess ran on Rpc; the fallback must keep it
// working instead of panicking on a nil interface.
func TestStartWithStructLiteralMissingStreamRPC(t *testing.T) {
	results := make(chan startStreamResult, 1)
	server := httptest.NewServer(recordStartStream(results))
	t.Cleanup(server.Close)

	rpc := processconnect.NewProcessClient(server.Client(), server.URL)
	// Struct literal, no constructor: StreamRPC stays nil.
	commands := &Commands{
		Rpc: rpc,
		headers: map[string]string{
			"Authorization":    "Basic cm9vdDo=",
			"e2b-sandbox-id":   "sbx-1",
			"e2b-sandbox-port": "49983",
		},
	}

	res, err := commands.Run(context.Background(), "echo hi")
	if err != nil {
		t.Fatalf("Run() error = %v, want struct-literal construction without StreamRPC to fall back to Rpc", err)
	}
	receiveStartStream(t, results)
	if res.ExitCode != 0 {
		t.Errorf("ExitCode = %d, want 0", res.ExitCode)
	}
}

// TestStartCallerDeadlineNotAttributedToOverallTimeout pins the
// ctx.Err() == nil guard: a caller's own context deadline expiring must
// surface as the caller's error, not as a RunOpts.Timeout message, even
// when a per-call overall timeout is set.
func TestStartCallerDeadlineNotAttributedToOverallTimeout(t *testing.T) {
	done := make(chan struct{})
	commands, _ := newTestCommands(t, blockingStartHandler(done))
	// close(done) must run before server.Close (registered inside
	// newTestCommands): t.Cleanup is LIFO, so register it afterwards.
	t.Cleanup(func() { close(done) })

	ctx, cancel := context.WithTimeout(context.Background(), 100*time.Millisecond)
	defer cancel()

	_, err := commands.Start(ctx, "sleep 100", RunOpts{Timeout: 30 * time.Second})
	if err == nil {
		t.Fatal("Start() should fail when the caller's deadline expires")
	}
	if strings.Contains(err.Error(), "RunOpts.Timeout") {
		t.Errorf("Start() error = %v, want the caller's deadline error, not a RunOpts.Timeout message", err)
	}
	if !strings.Contains(err.Error(), "context deadline exceeded") {
		t.Errorf("Start() error = %v, want the caller's context deadline error", err)
	}
}
