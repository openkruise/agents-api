package runtime

import (
	"context"
	"fmt"
	"net/http"
)

// TrafficTokenHeader is the header carrying the sandbox traffic access token
// on data-plane requests. It is required when the sandbox-gateway enforces
// JWT auth (sandbox metadata "security.agents.kruise.io/enable-jwt-auth=true").
const TrafficTokenHeader = "e2b-traffic-access-token"

// TrafficTokenProvider returns a valid traffic access token for a sandbox's
// data-plane requests. It is invoked immediately before each request is
// sent so short-lived tokens (Traffic JWTs) can be refreshed transparently;
// returning an error fails the request before it leaves the process
// (fail-closed).
type TrafficTokenProvider func(ctx context.Context) (string, error)

// trafficTokenTransport wraps a RoundTripper and injects the traffic access
// token header into every request passing through it. Every data-plane
// request of a sandbox (connect RPC, raw HTTP, streaming) goes through this
// wrapper, mirroring the Python SDK's request hooks and interceptors.
type trafficTokenTransport struct {
	base     http.RoundTripper
	provider TrafficTokenProvider
}

// RoundTrip implements http.RoundTripper.
func (t *trafficTokenTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	token, err := t.provider(req.Context())
	if err != nil {
		return nil, fmt.Errorf("traffic access token: %w", err)
	}
	// The RoundTripper contract forbids mutating the incoming request;
	// clone it before adding the header.
	cloned := req.Clone(req.Context())
	if token != "" {
		cloned.Header.Set(TrafficTokenHeader, token)
	}

	base := t.base
	if base == nil {
		base = http.DefaultTransport
	}
	return base.RoundTrip(cloned)
}

// withTrafficToken returns a shallow copy of client whose Transport injects
// the traffic access token header into every request. The copy shares the
// original's connection pool while the original client is left untouched,
// so each sandbox can install its own token source even when a custom
// client is shared across sandboxes.
func withTrafficToken(client *http.Client, provider TrafficTokenProvider) *http.Client {
	copied := *client
	copied.Transport = &trafficTokenTransport{base: client.Transport, provider: provider}
	return &copied
}
