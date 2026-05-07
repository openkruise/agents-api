// Package runtime provides a client that talks directly to the envd service
// inside a sandbox for in-sandbox operations (filesystem and process).
package runtime

import (
	"fmt"
	"os"
	"time"
)

const (
	defaultDomain         = "e2b.app"
	defaultScheme         = "http"
	defaultRequestTimeout = 60 * time.Second
	defaultEnvdPort       = 49983
	defaultEnvdAuthHeader = "Basic cm9vdDo="
)

// Config stores everything needed to address and authenticate against envd.
type Config struct {
	// Domain is the base domain envd is served from. Default: "e2b.app".
	Domain string
	// Scheme is the URL scheme: "https" (default) or "http".
	Scheme string
	// EnvdPort is the port envd listens on inside the sandbox.
	EnvdPort int
	// RuntimeToken is the token used to authenticate with the runtime.
	RuntimeToken string

	// SandboxBaseURL, when non-empty, fully overrides Protocol/Domain based
	// URL composition. The final envd URL is "<SandboxBaseURL>/<sandboxID>".
	SandboxBaseURL string

	// AuthHeader is the value sent in the "Authorization" header to envd.
	// Defaults to "Basic cm9vdDo=" (root with empty password) which matches
	// the stock envd deployment.
	AuthHeader string
	// APIKey, when non-empty, is sent as "X-API-Key" header. Only useful when
	// the ingress in front of envd also validates this header.
	APIKey string
	// Headers contains additional headers to send with every envd request.
	Headers map[string]string

	// RequestTimeout is the timeout applied to the underlying HTTP client.
	RequestTimeout time.Duration
}

// Option configures a Config via the functional-options pattern.
type Option func(*Config)

// NewConfig builds a Config with defaults, environment-variable fallback and
// then user-supplied options applied in that order.
//
// Environment variables consumed:
//
//	E2B_DOMAIN     -> Domain
//	SCHEME     -> Scheme
//	E2B_API_KEY    -> APIKey  (optional pass-through)
func NewConfig(opts ...Option) *Config {
	cfg := &Config{
		Domain:         defaultDomain,
		Scheme:         defaultScheme,
		EnvdPort:       defaultEnvdPort,
		AuthHeader:     defaultEnvdAuthHeader,
		Headers:        make(map[string]string),
		RequestTimeout: defaultRequestTimeout,
	}

	if v := os.Getenv("E2B_DOMAIN"); v != "" {
		cfg.Domain = v
	}
	if v := os.Getenv("SCHEME"); v != "" {
		cfg.Scheme = v
	}
	if v := os.Getenv("E2B_API_KEY"); v != "" {
		cfg.APIKey = v
	}

	for _, opt := range opts {
		opt(cfg)
	}
	return cfg
}

// WithDomain sets the envd domain.
func WithDomain(domain string) Option {
	return func(c *Config) { c.Domain = domain }
}

// WithScheme sets the URL scheme ("https" or "http").
func WithScheme(scheme string) Option {
	return func(c *Config) { c.Scheme = scheme }
}

// WithEnvdPort sets a custom envd port.
func WithEnvdPort(port int) Option {
	return func(c *Config) { c.EnvdPort = port }
}

// WithRuntimeToken sets a runtimeToken.
func WithRuntimeToken(runtimeToken string) Option {
	return func(c *Config) { c.RuntimeToken = runtimeToken }
}

// WithSandboxBaseURL fully overrides URL composition.
func WithSandboxBaseURL(url string) Option {
	return func(c *Config) { c.SandboxBaseURL = url }
}

// WithAuthHeader overrides the default envd Authorization header.
func WithAuthHeader(header string) Option {
	return func(c *Config) { c.AuthHeader = header }
}

// WithAPIKey sets the optional X-API-Key header value.
func WithAPIKey(apiKey string) Option {
	return func(c *Config) { c.APIKey = apiKey }
}

// WithHeader adds (or overrides) a single custom header.
func WithHeader(key, value string) Option {
	return func(c *Config) {
		if c.Headers == nil {
			c.Headers = make(map[string]string)
		}
		c.Headers[key] = value
	}
}

// WithHeaders merges a map of custom headers.
func WithHeaders(headers map[string]string) Option {
	return func(c *Config) {
		if c.Headers == nil {
			c.Headers = make(map[string]string)
		}
		for k, v := range headers {
			c.Headers[k] = v
		}
	}
}

// WithRequestTimeout sets the HTTP client timeout.
func WithRequestTimeout(d time.Duration) Option {
	return func(c *Config) { c.RequestTimeout = d }
}

// WithConfig replaces the working Config with a pre-built one.
func WithConfig(cfg *Config) Option {
	return func(c *Config) {
		if cfg == nil {
			return
		}
		*c = *cfg
		// Defensive copy of the headers map so callers cannot mutate ours.
		if cfg.Headers != nil {
			c.Headers = make(map[string]string, len(cfg.Headers))
			for k, v := range cfg.Headers {
				c.Headers[k] = v
			}
		}
	}
}

// SandboxURL returns the envd base URL for a given sandbox ID.
// If SandboxBaseURL is set, it is returned as-is; otherwise composed from Scheme and Domain.
func (c *Config) SandboxURL(sandboxID string) string {
	if c.SandboxBaseURL != "" {
		return c.SandboxBaseURL
	}
	return fmt.Sprintf("%s://%s", c.scheme(), c.Domain)
}

// SandboxHeaders builds the headers map sent with every envd request for sandboxID.
func (c *Config) SandboxHeaders(sandboxID string) map[string]string {
	headers := make(map[string]string, 4+len(c.Headers))

	auth := c.AuthHeader
	if auth == "" {
		auth = defaultEnvdAuthHeader
	}
	headers["Authorization"] = auth

	if c.APIKey != "" {
		headers["X-API-Key"] = c.APIKey
	}

	if c.RuntimeToken != "" {
		headers["X-Access-Token"] = c.RuntimeToken
	}
	headers["e2b-sandbox-id"] = sandboxID
	headers["e2b-sandbox-port"] = fmt.Sprintf("%d", c.EnvdPort)

	for k, v := range c.Headers {
		headers[k] = v
	}
	return headers
}

func (c *Config) scheme() string {
	if c.Scheme != "" {
		return c.Scheme
	}
	return defaultScheme
}
