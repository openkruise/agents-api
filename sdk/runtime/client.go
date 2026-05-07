package runtime

import (
	"net/http"

	"github.com/openkruise/agents-api/sdk/proto/envd/filesystem/filesystemconnect"
	"github.com/openkruise/agents-api/sdk/proto/envd/process/processconnect"
)

// Client talks directly to the envd service of a single sandbox,
// exposing in-sandbox capabilities (Files, Commands).
type Client struct {
	// Commands provides command execution in the sandbox.
	Commands *Commands
	// Files provides filesystem operations in the sandbox.
	Files *Filesystem

	sandboxID  string
	config     *Config
	envdURL    string
	httpClient *http.Client
}

// New constructs an envd Client for a known sandbox ID.
func New(sandboxID string, opts ...Option) *Client {
	cfg := NewConfig(opts...)
	return NewWithConfig(sandboxID, cfg)
}

// NewWithConfig is like New but takes a pre-built Config.
func NewWithConfig(sandboxID string, cfg *Config) *Client {
	if cfg == nil {
		cfg = NewConfig()
	}
	httpClient := &http.Client{Timeout: cfg.RequestTimeout}
	envdURL := cfg.SandboxURL(sandboxID)
	headers := cfg.SandboxHeaders(sandboxID)

	fsRPC := filesystemconnect.NewFilesystemClient(httpClient, envdURL)
	procRPC := processconnect.NewProcessClient(httpClient, envdURL)

	return &Client{
		Commands:   NewCommands(procRPC, headers),
		Files:      NewFilesystem(fsRPC, httpClient, envdURL, headers),
		sandboxID:  sandboxID,
		config:     cfg,
		envdURL:    envdURL,
		httpClient: httpClient,
	}
}

// SandboxID returns the sandbox identifier this client is bound to.
func (c *Client) SandboxID() string {
	return c.sandboxID
}

// EnvdURL returns the resolved envd base URL for the bound sandbox.
func (c *Client) EnvdURL() string {
	return c.envdURL
}

// Config returns the underlying configuration (read-only).
func (c *Client) Config() *Config {
	return c.config
}
