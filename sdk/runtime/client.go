package runtime

import (
	"net/http"

	"github.com/openkruise/agents-api/sdk/proto/envd/filesystem/filesystemconnect"
	"github.com/openkruise/agents-api/sdk/proto/envd/process/processconnect"
)

// Client talks directly to the runtime service of a single sandbox,
// exposing in-sandbox capabilities (Files, Commands).
type Client struct {
	// Commands provides command execution in the sandbox.
	Commands *Commands
	// Files provides filesystem operations in the sandbox.
	Files *Filesystem

	sandboxID  string
	config     *Config
	runtimeURL string
	httpClient *http.Client
}

// New constructs a runtime Client for a known sandbox ID.
func New(sandboxID string, opts ...Option) *Client {
	cfg := NewConfig(opts...)
	return NewWithConfig(sandboxID, cfg)
}

// NewWithConfig is like New but takes a pre-built Config.
func NewWithConfig(sandboxID string, cfg *Config) *Client {
	if cfg == nil {
		cfg = NewConfig()
	}
	httpClient := cfg.HTTPClient()
	runtimeURL := cfg.SandboxURL(sandboxID)
	headers := cfg.SandboxHeaders(sandboxID)

	fsRPC := filesystemconnect.NewFilesystemClient(httpClient, runtimeURL)
	procRPC := processconnect.NewProcessClient(httpClient, runtimeURL)

	return &Client{
		Commands:   NewCommands(procRPC, headers),
		Files:      NewFilesystem(fsRPC, httpClient, runtimeURL, headers),
		sandboxID:  sandboxID,
		config:     cfg,
		runtimeURL: runtimeURL,
		httpClient: httpClient,
	}
}

// SandboxID returns the sandbox identifier this client is bound to.
func (c *Client) SandboxID() string {
	return c.sandboxID
}

// RuntimeURL returns the resolved runtime base URL for the bound sandbox.
func (c *Client) RuntimeURL() string {
	return c.runtimeURL
}

// Config returns the underlying configuration (read-only).
func (c *Client) Config() *Config {
	return c.config
}
