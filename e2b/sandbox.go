package e2b

import (
	"context"
	"fmt"
	"math/rand/v2"
	"sync/atomic"

	"github.com/openkruise/agents-api/e2b/api"
	"github.com/openkruise/agents-api/runtime"
)

// SandboxOption configures sandbox creation or connection behavior.
type SandboxOption func(*sandboxOptions)

// sandboxOptions holds all configurable parameters for creating or connecting to a sandbox.
type sandboxOptions struct {
	// Connection config options
	configOpts []ConnectionConfigOption

	// Create-specific options
	timeout             int32
	autoPause           *bool
	autoResume          *api.SandboxAutoResumeConfig
	secure              bool
	allowInternetAccess *bool
	network             *api.SandboxNetworkConfig
	metadata            map[string]string
	envVars             map[string]string
	mcp                 map[string]interface{}
	volumeMounts        []api.SandboxVolumeMount

	// trafficAccessToken is an explicit seed for the traffic token manager
	// (WithTrafficAccessToken), used by Create and Connect alike; empty
	// means "derive the token from the API response".
	trafficAccessToken string
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

// WithTrafficAccessToken seeds the traffic token manager with an externally
// issued Traffic JWT, overriding whatever token the management API returns
// (create/connect responses may carry a legacy opaque token instead). It is
// the Go counterpart of the Python SDK's traffic_access_token constructor
// parameter: when an external identity provider — not the sandbox-manager —
// mints the JWTs, the client is handed the initial token this way and
// refreshes continue through the management API. Non-JWT values keep the
// legacy behavior (no traffic header, no refresh).
func WithTrafficAccessToken(token string) SandboxOption {
	return func(o *sandboxOptions) {
		o.trafficAccessToken = token
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

// WithAutoResume sets the auto-resume configuration for the sandbox.
func WithAutoResume(autoResume *api.SandboxAutoResumeConfig) SandboxOption {
	return func(o *sandboxOptions) {
		o.autoResume = autoResume
	}
}

// WithAllowInternetAccess controls whether the sandbox can access the internet.
func WithAllowInternetAccess(allow bool) SandboxOption {
	return func(o *sandboxOptions) {
		o.allowInternetAccess = &allow
	}
}

// WithNetwork sets the network configuration for the sandbox.
func WithNetwork(network *api.SandboxNetworkConfig) SandboxOption {
	return func(o *sandboxOptions) {
		o.network = network
	}
}

// WithMcp sets the MCP configuration for the sandbox.
func WithMcp(mcp map[string]interface{}) SandboxOption {
	return func(o *sandboxOptions) {
		o.mcp = mcp
	}
}

// WithVolumeMounts sets the volume mounts for the sandbox.
func WithVolumeMounts(volumeMounts []api.SandboxVolumeMount) SandboxOption {
	return func(o *sandboxOptions) {
		o.volumeMounts = volumeMounts
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

	// trafficToken holds the traffic token manager once installed; nil until
	// the sandbox is known to be JWT-protected (token returned by the
	// management API or enable-jwt-auth metadata). Read atomically since the
	// data-plane transport reads it on every request.
	trafficToken atomic.Pointer[trafficTokenManager]
}

// Create creates a new sandbox from a template (defaults to "code-interpreter").
func Create(ctx context.Context, template string, opts ...SandboxOption) (*Sandbox, error) {
	options := applySandboxOptions(opts)
	config := NewConnectionConfig(options.configOpts...)

	if template == "" {
		template = "code-interpreter"
	}

	createOpts := CreateSandboxOpts{
		Template:            template,
		Timeout:             options.timeout,
		AutoPause:           options.autoPause,
		AutoResume:          options.autoResume,
		Secure:              options.secure,
		AllowInternetAccess: options.allowInternetAccess,
		Network:             options.network,
		Metadata:            options.metadata,
		EnvVars:             options.envVars,
		Mcp:                 options.mcp,
		VolumeMounts:        options.volumeMounts,
	}
	if createOpts.Timeout <= 0 {
		createOpts.Timeout = int32(defaultSandboxTimeout)
	}

	api := NewSandboxApi(config)

	resp, err := api.CreateSandbox(ctx, createOpts)
	if err != nil {
		return nil, fmt.Errorf("failed to create sandbox: %w", err)
	}

	sb := newSandbox(config, api, resp.SandboxID, resp.EnvdAccessToken, resp.TemplateID, resp.EnvdVersion)
	if options.trafficAccessToken != "" {
		// An explicit seed (externally issued JWT) overrides the response
		// token, mirroring the Python SDK's traffic_access_token parameter.
		sb.installTrafficTokenManager(options.trafficAccessToken)
	} else {
		sb.installTrafficTokenManager(resp.TrafficAccessToken)
	}
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

	trafficAccessToken := ""
	if token, ok := resp.GetTrafficAccessTokenOk(); ok && token != nil && *token != "" {
		trafficAccessToken = *token
	}

	sb := newSandbox(config, api, resp.GetSandboxID(), resp.GetEnvdAccessToken(), resp.GetTemplateID(), resp.GetEnvdVersion())
	switch {
	case options.trafficAccessToken != "":
		// An explicit seed (externally issued JWT) overrides the response
		// token, mirroring the Python SDK's traffic_access_token parameter.
		sb.installTrafficTokenManager(options.trafficAccessToken)
	case trafficAccessToken != "":
		sb.installTrafficTokenManager(trafficAccessToken)
	default:
		// No token on the connect response: resume an existing sandbox whose
		// JWT-protected state may have been issued before this client saw it.
		// The lookup is best-effort so non-JWT deployments (no traffic token
		// at all) keep working when get_info cannot be served.
		if info, err := sb.GetInfo(ctx); err == nil && requiresTrafficToken(info.Metadata) {
			sb.installTrafficTokenManager("")
		}
	}
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
func newSandbox(config *ConnectionConfig, api *SandboxApi, sandboxID, EnvdAccessToken, templateID, envdVersion string) *Sandbox {
	config.AccessToken = EnvdAccessToken

	sb := &Sandbox{
		templateID:  templateID,
		envdVersion: envdVersion,
		config:      config,
		api:         api,
	}
	// The traffic token provider is installed up front even though the
	// manager may only be attached later: it reads the atomic pointer on
	// every data-plane request and skips the header while no manager exists.
	envdConfig := config.toEnvdConfig(sandboxID)
	envdConfig.TrafficTokenProvider = sb.trafficTokenProvider
	sb.Client = runtime.NewWithConfig(sandboxID, envdConfig)

	return sb
}

// installTrafficTokenManager attaches a traffic token manager seeded with
// token ("" to bootstrap via the first refresh). Non-JWT tokens (legacy
// opaque UUIDs) and debug mode keep the legacy behavior: no traffic header,
// no refresh. Installing twice is a no-op, mirroring the Python patch's
// lazy-install guard.
func (s *Sandbox) installTrafficTokenManager(token string) {
	if s.config.Debug || s.trafficToken.Load() != nil {
		return
	}
	manager, err := newTrafficTokenManager(
		token,
		s.refreshTrafficToken,
		trafficNow,
		rand.Float64,
	)
	if err != nil {
		// Seed token is not a JWT: legacy deployment, no automatic refresh.
		return
	}
	s.trafficToken.Store(manager)
}

// trafficTokenProvider adapts the sandbox's traffic token manager to
// runtime.TrafficTokenProvider: every data-plane request obtains a valid
// token (refreshing when needed) right before it is sent. Without a manager
// it returns an empty token, which the transport skips (legacy sandboxes
// send no traffic header).
func (s *Sandbox) trafficTokenProvider(ctx context.Context) (string, error) {
	if m := s.trafficToken.Load(); m != nil {
		return m.EnsureValidToken(ctx, false)
	}
	return "", nil
}

// refreshTrafficToken fetches a fresh traffic access token for this sandbox
// from the management API.
func (s *Sandbox) refreshTrafficToken(ctx context.Context) (TrafficAccessToken, error) {
	token, err := s.api.RefreshTrafficAccessToken(ctx, s.SandboxID())
	if token != nil {
		return *token, err
	}
	return TrafficAccessToken{}, err
}

// TrafficAccessToken returns the currently cached traffic access token
// without triggering a refresh. It returns "" when the sandbox is not
// JWT-protected.
func (s *Sandbox) TrafficAccessToken() string {
	if m := s.trafficToken.Load(); m != nil {
		return m.currentToken()
	}
	return ""
}

// RefreshTrafficAccessToken forces a refresh of the traffic access token and
// returns the fresh token. Concurrent forced refreshes coalesce into a
// single refresh; a failed refresh still returns the current token when it
// has not expired (and an error otherwise). It returns an error for
// sandboxes that are not JWT-protected.
func (s *Sandbox) RefreshTrafficAccessToken(ctx context.Context) (string, error) {
	m := s.trafficToken.Load()
	if m == nil {
		return "", fmt.Errorf("sandbox %s has no traffic access token", s.SandboxID())
	}
	return m.EnsureValidToken(ctx, true)
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
