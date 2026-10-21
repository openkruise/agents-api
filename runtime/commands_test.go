package runtime

import (
	"context"
	"encoding/base64"
	"encoding/binary"
	"net/http"
	"net/http/httptest"
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
func connectFrames(t *testing.T, msgs ...proto.Message) []byte {
	t.Helper()
	var buf []byte
	for _, m := range msgs {
		payload, err := proto.Marshal(m)
		if err != nil {
			t.Fatalf("marshal %T: %v", m, err)
		}
		frame := make([]byte, 5+len(payload))
		frame[0] = 0x00
		binary.BigEndian.PutUint32(frame[1:5], uint32(len(payload)))
		copy(frame[5:], payload)
		buf = append(buf, frame...)
	}
	return buf
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

// writeStartStream answers a Start RPC with a start event followed by the
// given events and records the incoming Authorization header.
func writeStartStream(t *testing.T, w http.ResponseWriter, r *http.Request) (auth string) {
	t.Helper()
	if r.URL.Path != "/process.Process/Start" {
		t.Errorf("path = %q, want /process.Process/Start", r.URL.Path)
	}
	w.Header().Set("Content-Type", "application/connect+proto")
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write(connectFrames(t,
		startEvent(42),
		stdoutEvent("out\n"),
		endEvent(0),
	))
	_, _ = w.Write(connectEndOfStreamFrame())
	return r.Header.Get("Authorization")
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
	var gotAuth string
	commands, _ := newTestCommands(t, func(w http.ResponseWriter, r *http.Request) {
		gotAuth = writeStartStream(t, w, r)
	})

	handle, err := commands.Start(context.Background(), "whoami", RunOpts{User: "nobody"})
	if err != nil {
		t.Fatalf("Start() error = %v", err)
	}
	defer handle.Disconnect()

	if handle.Pid() != 42 {
		t.Errorf("Pid() = %d, want 42", handle.Pid())
	}
	if want := "Basic " + base64.StdEncoding.EncodeToString([]byte("nobody:")); gotAuth != want {
		t.Errorf("Authorization = %q, want %q", gotAuth, want)
	}
}

func TestStartKeepsDefaultAuthorization(t *testing.T) {
	var gotAuth string
	commands, _ := newTestCommands(t, func(w http.ResponseWriter, r *http.Request) {
		gotAuth = writeStartStream(t, w, r)
	})

	handle, err := commands.Start(context.Background(), "whoami")
	if err != nil {
		t.Fatalf("Start() error = %v", err)
	}
	defer handle.Disconnect()

	if gotAuth != "Basic cm9vdDo=" {
		t.Errorf("Authorization = %q, want the default Basic cm9vdDo=", gotAuth)
	}
}

func TestRunWithUserWaitsForCompletion(t *testing.T) {
	var gotAuth string
	commands, _ := newTestCommands(t, func(w http.ResponseWriter, r *http.Request) {
		gotAuth = writeStartStream(t, w, r)
	})

	res, err := commands.Run(context.Background(), "whoami", RunOpts{User: "nobody"})
	if err != nil {
		t.Fatalf("Run() error = %v", err)
	}
	if res.ExitCode != 0 {
		t.Errorf("ExitCode = %d, want 0", res.ExitCode)
	}
	if res.Stdout != "out\n" {
		t.Errorf("Stdout = %q, want out\\n", res.Stdout)
	}
	if want := "Basic " + base64.StdEncoding.EncodeToString([]byte("nobody:")); gotAuth != want {
		t.Errorf("Authorization = %q, want %q", gotAuth, want)
	}
}
