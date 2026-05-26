package sandbox

import (
	"fmt"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/openkruise/agents-api/sdk/proto/api"
	"github.com/openkruise/agents-api/sdk/runtime"
)

// Protocol defines the URL routing protocol.
// ProtocolNative uses subdomain-based routing; ProtocolPrivate uses path-based gateway routing.
type Protocol string

const (
	// ProtocolNative uses subdomain-based routing (original protocol).
	ProtocolNative Protocol = "native"
	// ProtocolPrivate uses path-based routing through a gateway.
	ProtocolPrivate Protocol = "private"
)

const (
	defaultDomain         = "your.domain.com"
	defaultScheme         = "https"
	defaultRequestTimeout = 60 * time.Second
	defaultSandboxTimeout = 300 // seconds
	defaultRuntimePort    = 49983
)

// ConnectionConfig stores the configuration for connecting to E2B services.
type ConnectionConfig struct {
	// APIKey is the E2B API key for authentication.
	APIKey string
	// AccessToken is the OAuth2 access token (alternative to APIKey).
	AccessToken string
	// Domain is the base domain for E2B services (default: "your.domain.com").
	Domain string
	// Scheme is the URL scheme, "https" (default) or "http".
	Scheme string
	// Protocol determines the URL routing mode: ProtocolNative or ProtocolPrivate.
	Protocol Protocol
	// APIURL overrides the full API base URL. Highest priority, bypasses Protocol/Domain.
	APIURL string
	// SandboxBaseURL overrides the sandbox envd base URL pattern. Highest priority, bypasses Protocol/Domain.
	SandboxBaseURL string
	// Debug enables debug mode, skipping certain API calls.
	Debug bool
	// RequestTimeout is the timeout for HTTP requests.
	RequestTimeout time.Duration
	// RuntimePort is the port for the runtime service inside the sandbox.
	RuntimePort int
	// Headers contains additional headers to send with sandbox requests.
	Headers map[string]string

	// apiClient is a lazily-initialized shared API client.
	apiClient     *api.APIClient
	apiClientOnce sync.Once
}

// NewConnectionConfig creates a new ConnectionConfig with defaults and environment variable fallback.
func NewConnectionConfig(opts ...ConnectionConfigOption) *ConnectionConfig {
	config := &ConnectionConfig{
		Domain:         defaultDomain,
		Scheme:         defaultScheme,
		Protocol:       ProtocolNative,
		RequestTimeout: defaultRequestTimeout,
		RuntimePort:    defaultRuntimePort,
		Headers:        make(map[string]string),
	}

	// Apply environment variable defaults
	if apiKey := os.Getenv("X_API_KEY"); apiKey != "" {
		config.APIKey = apiKey
	}
	if accessToken := os.Getenv("X_ACCESS_TOKEN"); accessToken != "" {
		config.AccessToken = accessToken
	}
	if scheme := os.Getenv("SCHEME"); scheme != "" {
		config.Scheme = scheme
	}
	if protocol := os.Getenv("PROTOCOL"); protocol != "" {
		config.Protocol = Protocol(protocol)
	}

	// Apply options
	for _, opt := range opts {
		opt(config)
	}

	return config
}

// ConnectionConfigOption is a functional option for ConnectionConfig.
type ConnectionConfigOption func(*ConnectionConfig)

// WithAPIKey sets the API key.
func WithAPIKey(apiKey string) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.APIKey = apiKey
	}
}

// WithAccessToken sets the access token.
func WithAccessToken(accessToken string) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.AccessToken = accessToken
	}
}

// WithDomain sets the domain.
func WithDomain(domain string) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.Domain = domain
	}
}

// WithDebug enables debug mode.
func WithDebug(debug bool) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.Debug = debug
	}
}

// WithRequestTimeout sets the request timeout.
func WithRequestTimeout(timeout time.Duration) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.RequestTimeout = timeout
	}
}

// WithScheme sets the URL scheme ("https" or "http").
func WithScheme(scheme string) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.Scheme = scheme
	}
}

// WithProtocol sets the URL routing protocol (ProtocolNative or ProtocolPrivate).
func WithProtocol(protocol Protocol) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.Protocol = protocol
	}
}

// WithAPIURL overrides the full API base URL. Highest priority, bypasses Protocol/Domain.
func WithAPIURL(apiURL string) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.APIURL = apiURL
	}
}

// WithSandboxBaseURL overrides the sandbox envd base URL. Highest priority, bypasses Protocol/Domain.
func WithSandboxBaseURL(sandboxBaseURL string) ConnectionConfigOption {
	return func(c *ConnectionConfig) {
		c.SandboxBaseURL = sandboxBaseURL
	}
}

// GetAPIURL returns the base API URL.
func (c *ConnectionConfig) GetAPIURL() string {
	if c.APIURL != "" {
		return c.APIURL
	}
	scheme := c.getScheme()
	if c.Protocol == ProtocolPrivate {
		return fmt.Sprintf("%s://%s/kruise/api", scheme, c.Domain)
	}
	return fmt.Sprintf("%s://api.%s", scheme, c.Domain)
}

// GetSandboxURL returns the envd API URL for a given sandbox.
func (c *ConnectionConfig) GetSandboxURL(sandboxID string) string {
	if c.SandboxBaseURL != "" {
		return fmt.Sprintf("%s/%s", c.SandboxBaseURL, sandboxID)
	}
	scheme := c.getScheme()
	if c.Protocol == ProtocolPrivate {
		return fmt.Sprintf("%s://%s/kruise/%s/%d", scheme, c.Domain, sandboxID, c.RuntimePort)
	}
	return fmt.Sprintf("%s://%d-%s.%s", scheme, c.RuntimePort, sandboxID, c.Domain)
}

// getScheme returns the URL scheme, defaulting to "https".
func (c *ConnectionConfig) getScheme() string {
	if c.Scheme != "" {
		return c.Scheme
	}
	return defaultScheme
}

// toEnvdConfig converts this ConnectionConfig into a runtime.Config for the envd client.
func (c *ConnectionConfig) toEnvdConfig(sandboxID string) *runtime.Config {
	cfg := &runtime.Config{
		Domain:         c.Domain,
		Scheme:         c.Scheme,
		RuntimePort:    c.RuntimePort,
		APIKey:         c.APIKey,
		RequestTimeout: c.RequestTimeout,
	}

	// Pre-compute the full sandbox URL using the Protocol-aware logic so
	// the envd client can use it directly without knowing about Protocol.
	cfg.SandboxBaseURL = c.GetSandboxURL(sandboxID)

	if len(c.Headers) > 0 {
		cfg.Headers = make(map[string]string, len(c.Headers))
		for k, v := range c.Headers {
			cfg.Headers[k] = v
		}
	}
	return cfg
}

// NewAPIClient returns a shared OpenAPI client configured for this connection.
// The client is lazily initialized on first call and reused across subsequent calls,
// sharing a single HTTP Transport and connection pool.
func (c *ConnectionConfig) NewAPIClient() *api.APIClient {
	c.apiClientOnce.Do(func() {
		cfg := api.NewConfiguration()
		cfg.Servers = api.ServerConfigurations{
			{URL: c.GetAPIURL()},
		}
		cfg.HTTPClient = &http.Client{
			Timeout: c.RequestTimeout,
		}
		if c.APIKey != "" {
			cfg.DefaultHeader["X-API-Key"] = c.APIKey
		}
		c.apiClient = api.NewAPIClient(cfg)
	})
	return c.apiClient
}
