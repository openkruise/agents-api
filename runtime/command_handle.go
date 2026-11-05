package runtime

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"time"

	"connectrpc.com/connect"
	"github.com/openkruise/agents-api/runtime/envd/process"
)

// CommandResult represents the result of a command execution.
type CommandResult struct {
	Stdout   string
	Stderr   string
	ExitCode int32
	Error    string
}

// CommandExitError is returned when a command exits with a non-zero exit code.
type CommandExitError struct {
	Stdout       string
	Stderr       string
	ExitCode     int32
	ErrorMessage string
}

func (e *CommandExitError) Error() string {
	msg := fmt.Sprintf("command exited with code %d", e.ExitCode)
	if e.ErrorMessage != "" {
		msg += fmt.Sprintf(": %s", e.ErrorMessage)
	}
	if e.Stderr != "" {
		msg += fmt.Sprintf("\nstderr: %s", e.Stderr)
	}
	return msg
}

// CommandHandle provides a handle to interact with a running command.
// It abstracts over both Start and Connect server-streaming responses.
type CommandHandle struct {
	pid        uint32
	handleKill func() bool
	stdout     strings.Builder
	stderr     strings.Builder
	result     *CommandResult
	// timeout records the per-call overall stream timeout (RunOpts.Timeout
	// or ConnectOpts.Timeout) set by the constructor paths; zero for handles
	// without one. Wait uses it to attribute a context.DeadlineExceeded
	// stream error to the user-facing option name.
	timeout time.Duration

	startStream   *connect.ServerStreamForClient[process.StartResponse]
	connectStream *connect.ServerStreamForClient[process.ConnectResponse]
	// cancelStream releases the child context the output stream (Start or
	// Connect) runs on. It is invoked when the handle is finished with
	// (Wait completion or Disconnect). Nil for handles constructed directly
	// without one.
	cancelStream func()
}

// withTimeout records the per-call overall stream timeout (RunOpts.Timeout
// on the Start path, ConnectOpts.Timeout on the Connect path) for error
// attribution in Wait; it distinguishes the per-call deadline from other
// stream errors.
func (h *CommandHandle) withTimeout(d time.Duration) *CommandHandle {
	h.timeout = d
	return h
}

// withCancelStream records the function releasing the child context
// behind a Connect stream; used by ConnectToProcess so that Wait
// completion or Disconnect releases that context like the Start path.
func (h *CommandHandle) withCancelStream(cancel func()) *CommandHandle {
	h.cancelStream = cancel
	return h
}

// NewCommandHandle creates a CommandHandle from a Start stream. cancelStream
// releases the child context behind the stream; it is invoked when the handle
// is finished with (Wait completion or Disconnect).
func NewCommandHandle(
	pid uint32,
	stream *connect.ServerStreamForClient[process.StartResponse],
	handleKill func() bool,
	cancelStream func(),
) *CommandHandle {
	return &CommandHandle{
		pid:          pid,
		startStream:  stream,
		handleKill:   handleKill,
		cancelStream: cancelStream,
	}
}

// NewCommandHandleFromConnect creates a CommandHandle from a Connect stream.
func NewCommandHandleFromConnect(
	pid uint32,
	stream *connect.ServerStreamForClient[process.ConnectResponse],
	handleKill func() bool,
) *CommandHandle {
	return &CommandHandle{
		pid:           pid,
		connectStream: stream,
		handleKill:    handleKill,
	}
}

// Pid returns the process ID of the command.
func (h *CommandHandle) Pid() uint32 {
	return h.pid
}

// Wait waits for the command to finish and returns the result.
// If the command exits with a non-zero exit code, a CommandExitError is returned.
func (h *CommandHandle) Wait(onStdout func(string), onStderr func(string)) (*CommandResult, error) {
	if h.startStream != nil {
		return h.waitStartStream(onStdout, onStderr)
	}
	if h.connectStream != nil {
		return h.waitConnectStream(onStdout, onStderr)
	}
	return nil, fmt.Errorf("no stream available")
}

func (h *CommandHandle) waitStartStream(onStdout, onStderr func(string)) (*CommandResult, error) {
	defer h.startStream.Close()
	defer h.releaseStream()

	for h.startStream.Receive() {
		msg := h.startStream.Msg()
		event := msg.GetEvent()
		if event == nil {
			continue
		}

		if data := event.GetData(); data != nil {
			if len(data.GetStdout()) > 0 {
				out := string(data.GetStdout())
				h.stdout.WriteString(out)
				if onStdout != nil {
					onStdout(out)
				}
			}
			if len(data.GetStderr()) > 0 {
				out := string(data.GetStderr())
				h.stderr.WriteString(out)
				if onStderr != nil {
					onStderr(out)
				}
			}
		}

		if end := event.GetEnd(); end != nil {
			h.result = &CommandResult{
				Stdout:   h.stdout.String(),
				Stderr:   h.stderr.String(),
				ExitCode: end.GetExitCode(),
				Error:    end.GetError(),
			}
		}
	}

	if err := h.startStream.Err(); err != nil {
		// Known limitation: if the caller's own context deadline expires,
		// the propagated error is also DeadlineExceeded and gets attributed
		// to RunOpts.Timeout here (the handle does not keep the caller's
		// context to distinguish the two, unlike the Start path). Only
		// observable when both a per-call timeout and a caller deadline are
		// in play and the latter expires.
		if h.timeout > 0 && errors.Is(err, context.DeadlineExceeded) {
			return nil, fmt.Errorf("timed out after %s (RunOpts.Timeout; the process may still be running in the sandbox)", h.timeout)
		}
		return nil, fmt.Errorf("stream error: %w", err)
	}

	if h.result == nil {
		return nil, fmt.Errorf("command ended without an end event")
	}

	if h.result.ExitCode != 0 {
		return h.result, &CommandExitError{
			Stdout:       h.result.Stdout,
			Stderr:       h.result.Stderr,
			ExitCode:     h.result.ExitCode,
			ErrorMessage: h.result.Error,
		}
	}

	return h.result, nil
}

func (h *CommandHandle) waitConnectStream(onStdout, onStderr func(string)) (*CommandResult, error) {
	defer h.connectStream.Close()
	defer h.releaseStream()

	for h.connectStream.Receive() {
		msg := h.connectStream.Msg()
		event := msg.GetEvent()
		if event == nil {
			continue
		}

		if data := event.GetData(); data != nil {
			if len(data.GetStdout()) > 0 {
				out := string(data.GetStdout())
				h.stdout.WriteString(out)
				if onStdout != nil {
					onStdout(out)
				}
			}
			if len(data.GetStderr()) > 0 {
				out := string(data.GetStderr())
				h.stderr.WriteString(out)
				if onStderr != nil {
					onStderr(out)
				}
			}
		}

		if end := event.GetEnd(); end != nil {
			h.result = &CommandResult{
				Stdout:   h.stdout.String(),
				Stderr:   h.stderr.String(),
				ExitCode: end.GetExitCode(),
				Error:    end.GetError(),
			}
		}
	}

	if err := h.connectStream.Err(); err != nil {
		// Known limitation (mirror of the Start path): if the caller's own
		// context deadline expires, the propagated DeadlineExceeded is
		// attributed to the per-call timeout here too. This path only runs for
		// a connect handle, so the message always names ConnectOpts.Timeout.
		if h.timeout > 0 && errors.Is(err, context.DeadlineExceeded) {
			return nil, fmt.Errorf("timed out after %s (ConnectOpts.Timeout; the process may still be running in the sandbox)", h.timeout)
		}
		return nil, fmt.Errorf("stream error: %w", err)
	}

	if h.result == nil {
		return nil, fmt.Errorf("command ended without an end event")
	}

	if h.result.ExitCode != 0 {
		return h.result, &CommandExitError{
			Stdout:       h.result.Stdout,
			Stderr:       h.result.Stderr,
			ExitCode:     h.result.ExitCode,
			ErrorMessage: h.result.Error,
		}
	}

	return h.result, nil
}

// releaseStream cancels the child context the output stream runs on, so the
// resources behind the stream are released once the handle is finished with.
// It is idempotent and safe to call on handles without a child context.
func (h *CommandHandle) releaseStream() {
	if h.cancelStream != nil {
		h.cancelStream()
	}
}

// Disconnect disconnects from the command without killing it.
// You can reconnect using Commands.ConnectToProcess.
func (h *CommandHandle) Disconnect() {
	h.releaseStream()
	if h.startStream != nil {
		h.startStream.Close()
	}
	if h.connectStream != nil {
		h.connectStream.Close()
	}
}

// Kill kills the command using SIGKILL.
func (h *CommandHandle) Kill() bool {
	return h.handleKill()
}
