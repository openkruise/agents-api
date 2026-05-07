package sandbox

import (
	"context"
	"fmt"
	"github.com/openkruise/agents-api/sdk/runtime"
)

// SandboxOption configures sandbox creation or connection behavior.
type SandboxOption func(*sandboxOptions)

// sandboxOptions holds all configurable parameters for creating or connecting to a sandbox.
type sandboxOptions struct {
	// Connection config options
	configOpts []ConnectionConfigOption

	// Create-specific options
	timeout   int32
	autoPause *bool
	metadata  map[string]string
	envVars   map[string]string
	secure    bool
}

// WithTimeout sets the sandbox timeout in seconds.
func WithTimeout(timeout int32) SandboxOption {
	return func(o *sandboxOptions) {
		o.timeout = timeout
	}
}

// WithMetadata sets metadata key-value pairs for the sandbox.
func WithMetadata(metadata map[string]string) SandboxOption {
	return func(o *sandboxOptions) {
		o.metadata = metadata
	}
}

// WithEnvVars sets environment variables for the sandbox.
func WithEnvVars(envVars map[string]string) SandboxOption {
	return func(o *sandboxOptions) {
		o.envVars = envVars
	}
}

// WithAutoPause sets the auto-pause behavior.
func WithAutoPause(autoPause bool) SandboxOption {
	return func(o *sandboxOptions) {
		o.autoPause = &autoPause
	}
}

// WithSecure enables secure mode for the sandbox.
func WithSecure(secure bool) SandboxOption {
	return func(o *sandboxOptions) {
		o.secure = secure
	}
}

// WithConfig applies one or more ConnectionConfigOption to the sandbox.
func WithConfig(configOpts ...ConnectionConfigOption) SandboxOption {
	return func(o *sandboxOptions) {
		o.configOpts = append(o.configOpts, configOpts...)
	}
}

// Sandbox combines the management API (lifecycle) with the in-sandbox envd
// client (data plane: Files, Commands) via the embedded runtime.Client.
type Sandbox struct {
	*runtime.Client

	templateID  string
	envdVersion string
	config      *ConnectionConfig
	api         *SandboxApi
}

// Create creates a new sandbox from a template (defaults to "code-interpreter").
func Create(ctx context.Context, template string, opts ...SandboxOption) (*Sandbox, error) {
	options := applySandboxOptions(opts)
	config := NewConnectionConfig(options.configOpts...)

	if template == "" {
		template = "code-interpreter"
	}

	createOpts := CreateSandboxOpts{
		Template:  template,
		Timeout:   options.timeout,
		AutoPause: options.autoPause,
		Metadata:  options.metadata,
		EnvVars:   options.envVars,
		Secure:    options.secure,
	}
	if createOpts.Timeout <= 0 {
		createOpts.Timeout = int32(defaultSandboxTimeout)
	}

	api := NewSandboxApi(config)

	resp, err := api.CreateSandbox(ctx, createOpts)
	if err != nil {
		return nil, fmt.Errorf("failed to create sandbox: %w", err)
	}

	sb := newSandbox(config, api, resp.SandboxID, resp.TemplateID, resp.EnvdVersion)
	return sb, nil
}

// Connect connects to an existing sandbox by its ID, resuming it if paused.
func Connect(ctx context.Context, sandboxID string, opts ...SandboxOption) (*Sandbox, error) {
	options := applySandboxOptions(opts)
	config := NewConnectionConfig(options.configOpts...)

	timeout := options.timeout
	if timeout <= 0 {
		timeout = int32(defaultSandboxTimeout)
	}

	api := NewSandboxApi(config)

	resp, err := api.ConnectSandbox(ctx, sandboxID, timeout)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to sandbox: %w", err)
	}

	sb := newSandbox(config, api, resp.GetSandboxID(), resp.GetTemplateID(), resp.GetEnvdVersion())
	return sb, nil
}

// applySandboxOptions merges all SandboxOption into a sandboxOptions struct.
func applySandboxOptions(opts []SandboxOption) *sandboxOptions {
	options := &sandboxOptions{}
	for _, opt := range opts {
		opt(options)
	}
	return options
}

// newSandbox initializes a Sandbox with an envd client and management API.
func newSandbox(config *ConnectionConfig, api *SandboxApi, sandboxID, templateID, envdVersion string) *Sandbox {
	client := runtime.NewWithConfig(sandboxID, config.toEnvdConfig(sandboxID))

	return &Sandbox{
		Client:      client,
		templateID:  templateID,
		envdVersion: envdVersion,
		config:      config,
		api:         api,
	}
}

// TemplateID returns the template identifier.
func (s *Sandbox) TemplateID() string {
	return s.templateID
}

// EnvdVersion returns the envd version reported by the management API.
func (s *Sandbox) EnvdVersion() string {
	return s.envdVersion
}

// GetInfo returns detailed information about this sandbox.
func (s *Sandbox) GetInfo(ctx context.Context) (*SandboxInfo, error) {
	return s.api.GetInfo(ctx, s.SandboxID())
}

// SetTimeout sets a new timeout for this sandbox.
func (s *Sandbox) SetTimeout(ctx context.Context, timeout int32) error {
	return s.api.SetTimeout(ctx, s.SandboxID(), timeout)
}

// Pause pauses this sandbox.
func (s *Sandbox) Pause(ctx context.Context) (string, error) {
	return s.api.Pause(ctx, s.SandboxID())
}

// Kill kills this sandbox.
func (s *Sandbox) Kill(ctx context.Context) (bool, error) {
	return s.api.Kill(ctx, s.SandboxID())
}

// Close is an alias for Kill, intended for use with defer.
func (s *Sandbox) Close(ctx context.Context) error {
	_, err := s.Kill(ctx)
	return err
}
