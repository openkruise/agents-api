package runtime

import (
	"context"
	"encoding/base64"
	"encoding/binary"
	"fmt"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

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
// Call it after the RPC returns so the recorded header is visible without
// a data race.
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
	srv := receiveStartStream(t, results)
	if err != nil {
		t.Fatalf("Start() error = %v", err)
	}
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
	srv := receiveStartStream(t, results)
	if err != nil {
		t.Fatalf("Start() error = %v", err)
	}
	defer handle.Disconnect()

	if srv.auth != "Basic cm9vdDo=" {
		t.Errorf("Authorization = %q, want the default Basic cm9vdDo=", srv.auth)
	}
}

func TestRunWithUserWaitsForCompletion(t *testing.T) {
	results := make(chan startStreamResult, 1)
	commands, _ := newTestCommands(t, recordStartStream(results))

	res, err := commands.Run(context.Background(), "whoami", RunOpts{User: "nobody"})
	srv := receiveStartStream(t, results)
	if err != nil {
		t.Fatalf("Run() error = %v", err)
	}
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
