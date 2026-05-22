package runtime

import (
	"context"
	"fmt"
	"net/http"

	"connectrpc.com/connect"
	"github.com/openkruise/agents-api/sdk/proto/envd/process"
	"github.com/openkruise/agents-api/sdk/proto/envd/process/processconnect"
)

// ProcessInfo represents information about a running process.
type ProcessInfo struct {
	Pid  uint32
	Tag  string
	Cmd  string
	Args []string
	Envs map[string]string
	Cwd  string
}

// Commands provides command execution functionality in the sandbox via the
type Commands struct {
	Rpc     processconnect.ProcessClient
	headers map[string]string
}

// NewCommands creates a new Commands instance.
func NewCommands(rpc processconnect.ProcessClient, headers map[string]string) *Commands {
	return &Commands{
		Rpc:     rpc,
		headers: headers,
	}
}

// List lists all running commands and PTY sessions.
func (c *Commands) List(ctx context.Context) ([]ProcessInfo, error) {
	req := connect.NewRequest(&process.ListRequest{})
	c.setHeaders(req)

	resp, err := c.Rpc.List(ctx, req)
	if err != nil {
		return nil, fmt.Errorf("failed to list processes: %w", err)
	}

	processes := make([]ProcessInfo, len(resp.Msg.Processes))
	for i, p := range resp.Msg.Processes {
		info := ProcessInfo{
			Pid: p.Pid,
		}
		if p.Tag != nil {
			info.Tag = *p.Tag
		}
		if p.Config != nil {
			info.Cmd = p.Config.Cmd
			info.Args = p.Config.Args
			info.Envs = p.Config.Envs
			if p.Config.Cwd != nil {
				info.Cwd = *p.Config.Cwd
			}
		}
		processes[i] = info
	}

	return processes, nil
}

// Kill kills a running command by PID using SIGKILL.
func (c *Commands) Kill(ctx context.Context, pid uint32) (bool, error) {
	req := connect.NewRequest(&process.SendSignalRequest{
		Process: &process.ProcessSelector{
			Selector: &process.ProcessSelector_Pid{Pid: pid},
		},
		Signal: process.Signal_SIGNAL_SIGKILL,
	})
	c.setHeaders(req)

	_, err := c.Rpc.SendSignal(ctx, req)
	if err != nil {
		if connectErr, ok := err.(*connect.Error); ok && connectErr.Code() == connect.CodeNotFound {
			return false, nil
		}
		return false, fmt.Errorf("failed to kill process: %w", err)
	}

	return true, nil
}

// SendStdin sends data to a command's stdin.
func (c *Commands) SendStdin(ctx context.Context, pid uint32, data string) error {
	req := connect.NewRequest(&process.SendInputRequest{
		Process: &process.ProcessSelector{
			Selector: &process.ProcessSelector_Pid{Pid: pid},
		},
		Input: &process.ProcessInput{
			Input: &process.ProcessInput_Stdin{
				Stdin: []byte(data),
			},
		},
	})
	c.setHeaders(req)

	_, err := c.Rpc.SendInput(ctx, req)
	if err != nil {
		return fmt.Errorf("failed to send stdin: %w", err)
	}

	return nil
}

// RunOpts contains options for running a command.
type RunOpts struct {
	// Envs are environment variables for the command.
	Envs map[string]string
	// Cwd is the working directory for the command.
	Cwd string
	// Stdin enables stdin for the command.
	Stdin bool
	// Background runs the command in the background, returning a CommandHandle.
	Background bool
	// OnStdout is called for each stdout chunk (foreground only).
	OnStdout func(string)
	// OnStderr is called for each stderr chunk (foreground only).
	OnStderr func(string)
}

// Run executes a command and waits for it to finish.
func (c *Commands) Run(ctx context.Context, cmd string, opts ...RunOpts) (*CommandResult, error) {
	opt := RunOpts{}
	if len(opts) > 0 {
		opt = opts[0]
	}

	handle, err := c.Start(ctx, cmd, opt)
	if err != nil {
		return nil, err
	}

	return handle.Wait(opt.OnStdout, opt.OnStderr)
}

// Start starts a command and returns a CommandHandle for interacting with it.
func (c *Commands) Start(ctx context.Context, cmd string, opts ...RunOpts) (*CommandHandle, error) {
	opt := RunOpts{}
	if len(opts) > 0 {
		opt = opts[0]
	}

	processConfig := &process.ProcessConfig{
		Cmd:  "/bin/bash",
		Args: []string{"-l", "-c", cmd},
	}
	if opt.Cwd != "" {
		cwd := opt.Cwd
		processConfig.Cwd = &cwd
	}
	if opt.Envs != nil {
		processConfig.Envs = opt.Envs
	}

	req := connect.NewRequest(&process.StartRequest{
		Process: processConfig,
		Stdin:   &opt.Stdin,
	})
	c.setHeaders(req)

	stream, err := c.Rpc.Start(ctx, req)
	if err != nil {
		return nil, fmt.Errorf("failed to start command: %w", err)
	}

	if !stream.Receive() {
		if err := stream.Err(); err != nil {
			return nil, fmt.Errorf("failed to receive start event: %w", err)
		}
		return nil, fmt.Errorf("stream closed without start event")
	}

	startEvent := stream.Msg()
	if startEvent.GetEvent() == nil || startEvent.GetEvent().GetStart() == nil {
		return nil, fmt.Errorf("expected start event, got: %v", startEvent)
	}

	pid := startEvent.GetEvent().GetStart().GetPid()

	return NewCommandHandle(pid, stream, func() bool {
		killed, _ := c.Kill(ctx, pid)
		return killed
	}), nil
}

// ConnectToProcess connects to a running command by PID.
func (c *Commands) ConnectToProcess(ctx context.Context, pid uint32) (*CommandHandle, error) {
	req := connect.NewRequest(&process.ConnectRequest{
		Process: &process.ProcessSelector{
			Selector: &process.ProcessSelector_Pid{Pid: pid},
		},
	})
	c.setHeaders(req)

	stream, err := c.Rpc.Connect(ctx, req)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to process: %w", err)
	}

	if !stream.Receive() {
		if err := stream.Err(); err != nil {
			return nil, fmt.Errorf("failed to receive connect event: %w", err)
		}
		return nil, fmt.Errorf("stream closed without connect event")
	}

	startEvent := stream.Msg()
	if startEvent.GetEvent() == nil || startEvent.GetEvent().GetStart() == nil {
		return nil, fmt.Errorf("expected start event, got: %v", startEvent)
	}

	actualPid := startEvent.GetEvent().GetStart().GetPid()

	return NewCommandHandleFromConnect(actualPid, stream, func() bool {
		killed, _ := c.Kill(ctx, actualPid)
		return killed
	}), nil
}

func (c *Commands) setHeaders(req interface{ Header() http.Header }) {
	for k, v := range c.headers {
		req.Header().Set(k, v)
	}
}
