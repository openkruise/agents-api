package runtime

import (
	"context"
	"encoding/base64"
	"errors"
	"fmt"
	"net/http"
	"strings"
	"time"

	"connectrpc.com/connect"
	"github.com/openkruise/agents-api/runtime/envd/process"
	"github.com/openkruise/agents-api/runtime/envd/process/processconnect"
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
// envd process service: a unary client for request/response RPCs (List, Kill,
// SendStdin) and a streaming client for command output subscriptions
// (Start, ConnectToProcess).
type Commands struct {
	// Rpc is the unary client used for request/response process RPCs
	// (List, Kill, SendStdin). Its HTTP client carries the client-level
	// request timeout.
	Rpc processconnect.ProcessClient
	// StreamRPC is the client used for server-streaming process RPCs
	// (Start, ConnectToProcess). It is built on the streaming HTTP client
	// with no overall timeout, so a long-running command's output stream is
	// not cut off; the wait for the stream to open is bounded separately by
	// the request-timeout watchdog (see RunOpts.RequestTimeout).
	StreamRPC processconnect.ProcessClient

	headers map[string]string
	// requestTimeout is the client-level time limit for opening a command
	// stream (the wait for the start event). RunOpts.RequestTimeout overrides
	// it per call; it falls back to defaultRequestTimeout when unset.
	requestTimeout time.Duration
}

// NewCommands creates a new Commands instance.
func NewCommands(rpc processconnect.ProcessClient, headers map[string]string) *Commands {
	return &Commands{
		Rpc:            rpc,
		StreamRPC:      rpc,
		headers:        headers,
		requestTimeout: defaultRequestTimeout,
	}
}

// NewCommandsWithStreaming creates a Commands instance that uses separate
// clients for unary and streaming process RPCs. requestTimeout is the
// client-level time limit for opening a command stream (the wait for the
// start event); values <= 0 fall back to defaultRequestTimeout.
func NewCommandsWithStreaming(rpc, streamRPC processconnect.ProcessClient, headers map[string]string, requestTimeout time.Duration) *Commands {
	if requestTimeout <= 0 {
		requestTimeout = defaultRequestTimeout
	}
	return &Commands{
		Rpc:            rpc,
		StreamRPC:      streamRPC,
		headers:        headers,
		requestTimeout: requestTimeout,
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
	// User, when non-empty, runs the command as that user. The username is
	// sent via the Authorization header (Basic auth, matching the envd
	// convention) and must exist inside the sandbox image; usernames
	// containing ':' are rejected because Basic auth would split them into
	// a user:password pair. The override applies to the process start
	// request only (Run/Start); other process RPCs (List, Kill, SendStdin,
	// ConnectToProcess) keep the default header. When empty, the configured
	// Authorization header is used (root by default).
	User string
	// Shell is the shell program used to execute the command. Empty =
	// "/bin/bash", preserving the login environment via `-l -c <cmd>`.
	// Set "/bin/sh" or another shell when bash is not available in the
	// sandbox image. Matches Java RunOptions.shell().
	Shell string
	// Stdin enables stdin for the command.
	Stdin bool
	// RequestTimeout is the time limit for waiting the command to start —
	// the wait until envd confirms with a start event. Once the start event
	// arrives the limit is disarmed and does not restrict how long the
	// command may keep running or streaming output afterwards. Zero falls
	// back to the client-level request timeout (60s by default); Go has no
	// explicit-disable value for it — to relax the wait, set
	// RequestTimeout itself to a larger value (e.g. time.Hour). Note that a
	// deadline on the context passed to Start does NOT relax this wait: the
	// watchdog is armed independently of the caller's context. On timeout
	// the stream fails with an error while the process keeps running in the
	// sandbox (kill it explicitly via Commands.Kill or
	// CommandHandle.Kill). This matches the official E2B SDK's
	// request_timeout semantics.
	RequestTimeout time.Duration
	// Timeout is the time limit for the whole command stream — from the
	// moment the start request is issued until the end event arrives. This
	// matches the official E2B SDK's timeout semantics, except that zero
	// means no limit: the official SDK defaults to 60s, while unset keeps
	// the stream unbounded here — set it explicitly if you want the
	// official default. On timeout the stream fails with an error while the
	// process keeps running in the sandbox (kill it explicitly via
	// Commands.Kill or CommandHandle.Kill).
	Timeout time.Duration
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
// The handle keeps the stream's child context (and the opt.Timeout timer)
// alive until Wait/Disconnect completes or the returned handle is otherwise
// released; dropping the handle without either leaves them to the parent
// context — call Wait or Disconnect to release them deterministically.
func (c *Commands) Start(ctx context.Context, cmd string, opts ...RunOpts) (*CommandHandle, error) {
	opt := RunOpts{}
	if len(opts) > 0 {
		opt = opts[0]
	}

	shell := opt.Shell
	if shell == "" {
		shell = "/bin/bash"
	}
	processConfig := &process.ProcessConfig{
		Cmd:  shell,
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
	if opt.User != "" {
		if strings.ContainsRune(opt.User, ':') {
			return nil, fmt.Errorf("invalid RunOpts.User %q: must not contain ':' (Basic auth would split it into a user:password pair)", opt.User)
		}
		req.Header().Set("Authorization", userAuthHeader(opt.User))
	}

	// The output stream runs on the streaming HTTP client, which sets no
	// overall timeout (Config.StreamingHTTPClient uses Timeout: 0); the child
	// context still inherits any deadline on ctx. The request-timeout
	// watchdog below cancels it while waiting for the start event and is
	// disarmed as soon as the start event arrives, so it never limits how
	// long the command may keep running afterwards.
	streamCtx, cancelStream := context.WithCancel(ctx)
	if opt.Timeout > 0 {
		// RunOpts.Timeout bounds the whole stream: the handshake, the wait
		// for the start event and the output streaming until the end event.
		// It runs on a child context with a deadline; cancelStream is
		// chained so the handle's releaseStream releases both contexts.
		var cancelDeadline context.CancelFunc
		streamCtx, cancelDeadline = context.WithTimeout(streamCtx, opt.Timeout)
		baseCancel := cancelStream
		cancelStream = func() { cancelDeadline(); baseCancel() }
	}
	requestTimeout := opt.RequestTimeout
	if requestTimeout <= 0 {
		requestTimeout = c.resolvedRequestTimeout()
	}
	watchdogTimedOut := func(t *time.Timer) string {
		// Timer.Stop returns false when the timer already fired, so the
		// first Stop call distinguishes a request timeout from other
		// stream errors. The message names the source that actually armed
		// the watchdog: the per-call option, or the client-level fallback.
		if !t.Stop() {
			source := "client-level request timeout"
			if opt.RequestTimeout > 0 {
				source = "RunOpts.RequestTimeout"
			}
			return fmt.Sprintf("timed out after %s waiting for the start event (%s; the process may still be running in the sandbox)", requestTimeout, source)
		}
		return ""
	}
	overallTimedOut := func(err error) string {
		// The deadline child context surfaces as context.DeadlineExceeded
		// from either the opening RPC or the stream reads. The ctx.Err() ==
		// nil guard prevents misattribution: when the caller's own context
		// deadline fires, cancellation propagates DeadlineExceeded to this
		// child too, but then ctx.Err() is non-nil and the error belongs to
		// the caller, not to RunOpts.Timeout.
		if opt.Timeout > 0 && ctx.Err() == nil && errors.Is(err, context.DeadlineExceeded) {
			return fmt.Sprintf("timed out after %s (RunOpts.Timeout; the process may still be running in the sandbox)", opt.Timeout)
		}
		return ""
	}
	// Arm the watchdog before the blocking RPC call: StreamRPC.Start waits
	// for the response headers inside client.Do, so the watchdog must
	// already be running to bound that wait. Inherent narrow race: when
	// the watchdog fires in the same instant the start event arrives, the
	// stream is canceled and Start may succeed while Wait reports a plain
	// context-canceled stream error instead of a timeout message. The
	// window is microseconds, nothing leaks; accepted.
	watchdog := time.AfterFunc(requestTimeout, cancelStream)

	stream, err := c.resolvedStreamRPC().Start(streamCtx, req)
	if err != nil {
		cancelStream()
		if msg := watchdogTimedOut(watchdog); msg != "" {
			return nil, errors.New(msg)
		}
		if msg := overallTimedOut(err); msg != "" {
			return nil, errors.New(msg)
		}
		return nil, fmt.Errorf("failed to start command: %w", err)
	}

	if !stream.Receive() {
		cancelStream()
		stream.Close()
		if msg := watchdogTimedOut(watchdog); msg != "" {
			return nil, errors.New(msg)
		}
		if err := stream.Err(); err != nil {
			if msg := overallTimedOut(err); msg != "" {
				return nil, errors.New(msg)
			}
			return nil, fmt.Errorf("failed to receive start event: %w", err)
		}
		return nil, fmt.Errorf("stream closed without start event")
	}
	// Start event arrived — disarm the request watchdog; the rest of the
	// stream is not bounded by it.
	watchdog.Stop()

	startEvent := stream.Msg()
	if startEvent.GetEvent() == nil || startEvent.GetEvent().GetStart() == nil {
		cancelStream()
		stream.Close()
		return nil, fmt.Errorf("expected start event, got: %v", startEvent)
	}

	pid := startEvent.GetEvent().GetStart().GetPid()

	// Kill decouples from the stream context: after a timeout or cancel
	// cuts the stream (or the caller's context dies), killing the process
	// must still work — kill stays an explicit, independent operation in
	// the official E2B contract. WithoutCancel drops the deadline and
	// cancellation but keeps the parent's values (its Value delegates to
	// the parent), so trace spans and context-carried credentials still
	// reach the kill RPC; only the cancellation/deadline is shed, which is
	// what lets the kill outlive a stream that was already cut.
	return NewCommandHandle(pid, stream, func() bool {
		killed, _ := c.Kill(context.WithoutCancel(ctx), pid)
		return killed
	}, cancelStream).withTimeout(opt.Timeout), nil
}

// resolvedRequestTimeout returns the effective client-level request timeout.
// The field is unexported and only set by the constructors, but Commands
// has exported fields (Rpc, StreamRPC), so a struct literal like
// &Commands{Rpc: rpc, StreamRPC: rpc} is a legal construction that leaves
// requestTimeout zero — and time.AfterFunc(0, ...) fires immediately,
// failing every Start/ConnectToProcess with "timed out after 0s". Guard
// that path by falling back to the package default, matching NewCommands.
func (c *Commands) resolvedRequestTimeout() time.Duration {
	if c.requestTimeout > 0 {
		return c.requestTimeout
	}
	return defaultRequestTimeout
}

// resolvedStreamRPC returns the client for server-streaming process RPCs,
// falling back to Rpc when StreamRPC is unset. A struct literal like
// &Commands{Rpc: rpc} is a legal construction (both fields are exported)
// that leaves StreamRPC nil — before the streaming-client split this value
// worked because Start/ConnectToProcess ran on Rpc, so the fallback keeps
// that construction working instead of panicking on a nil interface.
func (c *Commands) resolvedStreamRPC() processconnect.ProcessClient {
	if c.StreamRPC != nil {
		return c.StreamRPC
	}
	return c.Rpc
}

// ConnectOpts contains options for connecting to a running command. It
// mirrors the official E2B connect capability, which offers the same two
// per-call limits as start; previously only the client-level request
// timeout applied to ConnectToProcess.
type ConnectOpts struct {
	// RequestTimeout is the time limit for waiting for the connect start
	// event. Zero falls back to the client-level request timeout (60s by
	// default); Go has no explicit-disable value for it — to relax the
	// wait, set RequestTimeout itself to a larger value (e.g. time.Hour).
	// Like RunOpts.RequestTimeout, a deadline on the context passed to
	// ConnectToProcess does NOT relax this wait. This matches the official
	// E2B connect request_timeout semantics.
	RequestTimeout time.Duration
	// Timeout is the time limit for the whole connect stream — from the
	// moment the connect request is issued until the end event arrives.
	// Zero means no limit. On timeout the stream fails with an error while
	// the process keeps running in the sandbox (kill it explicitly via
	// Commands.Kill or CommandHandle.Kill).
	Timeout time.Duration
}

// ConnectToProcess connects to a running command by PID.
// The handle keeps the stream's child context (and the opt.Timeout timer)
// alive until Wait/Disconnect completes or the returned handle is otherwise
// released; dropping the handle without either leaves them to the parent
// context — call Wait or Disconnect to release them deterministically.
func (c *Commands) ConnectToProcess(ctx context.Context, pid uint32, opts ...ConnectOpts) (*CommandHandle, error) {
	opt := ConnectOpts{}
	if len(opts) > 0 {
		opt = opts[0]
	}
	req := connect.NewRequest(&process.ConnectRequest{
		Process: &process.ProcessSelector{
			Selector: &process.ProcessSelector_Pid{Pid: pid},
		},
	})
	c.setHeaders(req)

	// The subscription runs on the streaming client (no overall HTTP
	// timeout) like Start: the process being watched may outlive any fixed
	// deadline. Opening the stream is still bounded by the request timeout
	// via the watchdog below — per call via ConnectOpts.RequestTimeout,
	// falling back to the client-level value (the official E2B connect
	// request_timeout role).
	streamCtx, cancelStream := context.WithCancel(ctx)
	if opt.Timeout > 0 {
		// ConnectOpts.Timeout bounds the whole connect stream, chained like
		// RunOpts.Timeout on the Start path so releaseStream releases both.
		var cancelDeadline context.CancelFunc
		streamCtx, cancelDeadline = context.WithTimeout(streamCtx, opt.Timeout)
		baseCancel := cancelStream
		cancelStream = func() { cancelDeadline(); baseCancel() }
	}
	requestTimeout := opt.RequestTimeout
	if requestTimeout <= 0 {
		requestTimeout = c.resolvedRequestTimeout()
	}
	watchdog := time.AfterFunc(requestTimeout, cancelStream)
	// Same Timer.Stop fired-detection as the Start path; the timeout
	// message names the source that actually armed the watchdog: the
	// per-call option, or the client-level fallback.
	watchdogTimedOut := func() error {
		if !watchdog.Stop() {
			source := "client-level request timeout"
			if opt.RequestTimeout > 0 {
				source = "ConnectOpts.RequestTimeout"
			}
			return fmt.Errorf("timed out after %s waiting for the connect event (%s; the process may still be running in the sandbox)", requestTimeout, source)
		}
		return nil
	}
	overallTimedOut := func(err error) string {
		// Same structural attribution as the Start path's overallTimedOut:
		// the deadline child context surfaces as context.DeadlineExceeded
		// from either the opening RPC or the first stream read, and the
		// ctx.Err() == nil guard keeps a caller's own deadline from being
		// misattributed to ConnectOpts.Timeout.
		if opt.Timeout > 0 && ctx.Err() == nil && errors.Is(err, context.DeadlineExceeded) {
			return fmt.Sprintf("timed out after %s (ConnectOpts.Timeout; the process may still be running in the sandbox)", opt.Timeout)
		}
		return ""
	}

	stream, err := c.resolvedStreamRPC().Connect(streamCtx, req)
	if err != nil {
		cancelStream()
		if msg := watchdogTimedOut(); msg != nil {
			return nil, msg
		}
		if msg := overallTimedOut(err); msg != "" {
			return nil, errors.New(msg)
		}
		return nil, fmt.Errorf("failed to connect to process: %w", err)
	}

	if !stream.Receive() {
		cancelStream()
		stream.Close()
		if msg := watchdogTimedOut(); msg != nil {
			return nil, msg
		}
		if err := stream.Err(); err != nil {
			if msg := overallTimedOut(err); msg != "" {
				return nil, errors.New(msg)
			}
			return nil, fmt.Errorf("failed to receive connect event: %w", err)
		}
		return nil, fmt.Errorf("stream closed without connect event")
	}
	watchdog.Stop()

	startEvent := stream.Msg()
	if startEvent.GetEvent() == nil || startEvent.GetEvent().GetStart() == nil {
		cancelStream()
		stream.Close()
		return nil, fmt.Errorf("expected start event, got: %v", startEvent)
	}

	actualPid := startEvent.GetEvent().GetStart().GetPid()

	// Kill decouples from the stream context: after a timeout or cancel
	// cuts the stream (or the caller's context dies), killing the process
	// must still work — kill stays an explicit, independent operation in
	// the official E2B contract. WithoutCancel drops the deadline and
	// cancellation but keeps the parent's values (its Value delegates to
	// the parent), so trace spans and context-carried credentials still
	// reach the kill RPC; only the cancellation/deadline is shed, which is
	// what lets the kill outlive a stream that was already cut.
	return NewCommandHandleFromConnect(actualPid, stream, func() bool {
		killed, _ := c.Kill(context.WithoutCancel(ctx), actualPid)
		return killed
	}).withCancelStream(cancelStream).withTimeout(opt.Timeout), nil
}

func (c *Commands) setHeaders(req interface{ Header() http.Header }) {
	for k, v := range c.headers {
		req.Header().Set(k, v)
	}
}

// userAuthHeader returns the Authorization header value that authenticates
// as the given user, matching the envd Basic-auth convention ("user:" with an
// empty password).
func userAuthHeader(user string) string {
	return "Basic " + base64.StdEncoding.EncodeToString([]byte(user+":"))
}
