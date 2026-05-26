package runtime

import (
	"fmt"
	"strings"

	"connectrpc.com/connect"
	"github.com/openkruise/agents-api/sdk/proto/envd/process"
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

	startStream   *connect.ServerStreamForClient[process.StartResponse]
	connectStream *connect.ServerStreamForClient[process.ConnectResponse]
}

// NewCommandHandle creates a CommandHandle from a Start stream.
func NewCommandHandle(
	pid uint32,
	stream *connect.ServerStreamForClient[process.StartResponse],
	handleKill func() bool,
) *CommandHandle {
	return &CommandHandle{
		pid:         pid,
		startStream: stream,
		handleKill:  handleKill,
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

// Disconnect disconnects from the command without killing it.
// You can reconnect using Commands.ConnectToProcess.
func (h *CommandHandle) Disconnect() {
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
