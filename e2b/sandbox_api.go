package e2b

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
	"strings"
	"time"

	"github.com/openkruise/agents-api/e2b/api"
)

// SandboxInfo represents information about a sandbox.
type SandboxInfo struct {
	SandboxID    string
	TemplateID   string
	Alias        string
	ClientID     string
	StartedAt    time.Time
	EndAt        time.Time
	CpuCount     int32
	MemoryMB     int32
	DiskSizeMB   int32
	EnvdVersion  string
	Metadata     map[string]string
	State        string
	VolumeMounts []api.SandboxVolumeMount
}

// ListResult represents the result of a list operation with pagination support.
type ListResult struct {
	Sandboxes []SandboxInfo
	NextToken string
}

// SandboxMetrics represents sandbox resource metrics.
type SandboxMetrics struct {
	CpuCount   int32
	CpuUsedPct float64
	MemTotal   int64
	MemUsed    int64
	DiskTotal  int64
	DiskUsed   int64
	Timestamp  int64
}

// SnapshotInfo represents snapshot information.
type SnapshotInfo struct {
	SnapshotID string
}

// SandboxCreateResponse represents the response from creating a sandbox.
type SandboxCreateResponse struct {
	SandboxID          string
	TemplateID         string
	EnvdVersion        string
	EnvdAccessToken    string
	TrafficAccessToken string
	Domain             string
}

// SandboxApi provides sandbox lifecycle management operations.
type SandboxApi struct {
	config    *ConnectionConfig
	apiClient *api.APIClient
}

// NewSandboxApi creates a new SandboxApi instance.
func NewSandboxApi(config *ConnectionConfig) *SandboxApi {
	return &SandboxApi{
		config:    config,
		apiClient: config.NewAPIClient(),
	}
}

// ListSandboxOpts contains options for listing sandboxes.
type ListSandboxOpts struct {
	// Metadata filters sandboxes by metadata key-value pairs.
	// Keys and values will be URL encoded automatically.
	Metadata map[string]string
	// State filters sandboxes by one or more states (e.g., "running", "paused").
	State []api.SandboxState
	// NextToken is the cursor to start the list from (for pagination).
	NextToken string
	// Limit is the maximum number of items to return per page.
	Limit int32
}

// List lists all sandboxes with pagination support.
// Returns a ListResult containing the sandboxes and a NextToken for fetching the next page.
func (s *SandboxApi) List(ctx context.Context, opts ...ListSandboxOpts) (*ListResult, error) {
	req := s.apiClient.SandboxesApi.V2SandboxesGet(ctx)

	if len(opts) > 0 {
		o := opts[0]
		if len(o.Metadata) > 0 {
			// Build metadata query string: key1=value1&key2=value2
			var pairs []string
			for k, v := range o.Metadata {
				pairs = append(pairs, url.QueryEscape(k)+"="+url.QueryEscape(v))
			}
			req = req.Metadata(strings.Join(pairs, "&"))
		}
		if len(o.State) > 0 {
			req = req.State(o.State)
		}
		if o.NextToken != "" {
			req = req.NextToken(o.NextToken)
		}
		if o.Limit > 0 {
			req = req.Limit(o.Limit)
		}
	}

	resp, httpResp, err := req.Execute()
	if err != nil {
		return nil, fmt.Errorf("failed to list sandboxes: %w", err)
	}
	if httpResp.StatusCode >= 300 {
		return nil, fmt.Errorf("list sandboxes failed with status %d", httpResp.StatusCode)
	}

	sandboxes := make([]SandboxInfo, len(resp))
	for i, sb := range resp {
		sandboxes[i] = convertToSandboxInfo(sb)
	}

	// Extract nextToken from response header for pagination
	nextToken := httpResp.Header.Get("x-next-token")

	return &ListResult{
		Sandboxes: sandboxes,
		NextToken: nextToken,
	}, nil
}

// convertToSandboxInfo converts an API response item to SandboxInfo.
func convertToSandboxInfo(sb api.SandboxesGet200ResponseInner) SandboxInfo {
	info := SandboxInfo{
		SandboxID:   sb.GetSandboxID(),
		TemplateID:  sb.GetTemplateID(),
		ClientID:    sb.GetClientID(),
		StartedAt:   sb.GetStartedAt(),
		EndAt:       sb.GetEndAt(),
		CpuCount:    sb.GetCpuCount(),
		MemoryMB:    sb.GetMemoryMB(),
		DiskSizeMB:  sb.GetDiskSizeMB(),
		EnvdVersion: sb.GetEnvdVersion(),
		State:       string(sb.GetState()),
	}
	if meta, ok := sb.GetMetadataOk(); ok && meta != nil {
		info.Metadata = *meta
	}
	return info
}

// GetInfo retrieves information about a specific sandbox.
func (s *SandboxApi) GetInfo(ctx context.Context, sandboxID string) (*SandboxInfo, error) {
	resp, httpResp, err := s.apiClient.SandboxesApi.SandboxesSandboxIDGet(ctx, sandboxID).Execute()
	if err != nil {
		if httpResp != nil && httpResp.StatusCode == http.StatusNotFound {
			return nil, fmt.Errorf("sandbox %s not found", sandboxID)
		}
		return nil, fmt.Errorf("failed to get sandbox info: %w", err)
	}

	info := &SandboxInfo{
		SandboxID:   resp.GetSandboxID(),
		TemplateID:  resp.GetTemplateID(),
		ClientID:    resp.GetClientID(),
		StartedAt:   resp.GetStartedAt(),
		EndAt:       resp.GetEndAt(),
		CpuCount:    resp.GetCpuCount(),
		MemoryMB:    resp.GetMemoryMB(),
		DiskSizeMB:  resp.GetDiskSizeMB(),
		EnvdVersion: resp.GetEnvdVersion(),
		State:       string(resp.GetState()),
	}
	if meta, ok := resp.GetMetadataOk(); ok && meta != nil {
		info.Metadata = *meta
	}

	return info, nil
}

// Kill kills a sandbox by ID.
func (s *SandboxApi) Kill(ctx context.Context, sandboxID string) (bool, error) {
	if s.config.Debug {
		return true, nil
	}

	httpResp, err := s.apiClient.SandboxesApi.SandboxesSandboxIDDelete(ctx, sandboxID).Execute()
	if err != nil {
		if httpResp != nil && httpResp.StatusCode == http.StatusNotFound {
			return false, nil
		}
		return false, fmt.Errorf("failed to kill sandbox: %w", err)
	}

	return true, nil
}

// SetTimeout sets a new timeout for the sandbox.
func (s *SandboxApi) SetTimeout(ctx context.Context, sandboxID string, timeout int32) error {
	if s.config.Debug {
		return nil
	}

	body := *api.NewSandboxesSandboxIDTimeoutPostRequest(timeout)
	httpResp, err := s.apiClient.SandboxesApi.SandboxesSandboxIDTimeoutPost(ctx, sandboxID).
		SandboxesSandboxIDTimeoutPostRequest(body).Execute()
	if err != nil {
		if httpResp != nil && httpResp.StatusCode == http.StatusNotFound {
			return fmt.Errorf("sandbox %s not found", sandboxID)
		}
		return fmt.Errorf("failed to set timeout: %w", err)
	}

	return nil
}

// CreateSandbox creates a new sandbox from a template.
func (s *SandboxApi) CreateSandbox(ctx context.Context, opts CreateSandboxOpts) (*SandboxCreateResponse, error) {
	body := api.NewCreateSandboxRequest(opts.Template)

	if opts.Timeout > 0 {
		body.SetTimeout(opts.Timeout)
	}
	if opts.Metadata != nil {
		body.SetMetadata(opts.Metadata)
	}
	if opts.EnvVars != nil {
		body.SetEnvVars(opts.EnvVars)
	}
	if opts.AutoPause != nil {
		body.SetAutoPause(*opts.AutoPause)
	}
	if opts.Secure {
		body.SetSecure(opts.Secure)
	}
	if opts.AutoResume != nil {
		body.SetAutoResume(*opts.AutoResume)
	}
	if opts.AllowInternetAccess != nil {
		body.SetAllowInternetAccess(*opts.AllowInternetAccess)
	}
	if opts.Network != nil {
		body.SetNetwork(*opts.Network)
	}
	if opts.Mcp != nil {
		body.SetMcp(opts.Mcp)
	}
	if opts.VolumeMounts != nil {
		body.SetVolumeMounts(opts.VolumeMounts)
	}

	resp, httpResp, err := s.apiClient.SandboxesApi.SandboxesPost(ctx).
		CreateSandboxRequest(*body).Execute()
	if err != nil {
		return nil, fmt.Errorf("failed to create sandbox: %w", err)
	}
	if httpResp.StatusCode >= 300 {
		return nil, fmt.Errorf("create sandbox failed with status %d", httpResp.StatusCode)
	}

	domain := ""
	if d, ok := resp.GetDomainOk(); ok && d != nil {
		domain = *d
	}
	envdAccessToken := ""
	if t, ok := resp.GetEnvdAccessTokenOk(); ok && t != nil {
		envdAccessToken = *t
	}
	trafficAccessToken := ""
	if t, ok := resp.GetTrafficAccessTokenOk(); ok && t != nil {
		trafficAccessToken = *t
	}

	return &SandboxCreateResponse{
		SandboxID:          resp.GetSandboxID(),
		TemplateID:         resp.GetTemplateID(),
		EnvdVersion:        resp.GetEnvdVersion(),
		EnvdAccessToken:    envdAccessToken,
		TrafficAccessToken: trafficAccessToken,
		Domain:             domain,
	}, nil
}

// ConnectSandbox connects to an existing (possibly paused) sandbox.
func (s *SandboxApi) ConnectSandbox(ctx context.Context, sandboxID string, timeout int32) (*api.Sandbox, error) {
	if timeout <= 0 {
		timeout = int32(defaultSandboxTimeout)
	}

	body := api.NewConnectSandbox(timeout)
	resp, httpResp, err := s.apiClient.SandboxesApi.SandboxesSandboxIDConnectPost(ctx, sandboxID).
		ConnectSandbox(*body).Execute()
	if err != nil {
		if httpResp != nil && httpResp.StatusCode == http.StatusNotFound {
			return nil, fmt.Errorf("sandbox %s not found", sandboxID)
		}
		return nil, fmt.Errorf("failed to connect to sandbox: %w", err)
	}

	return resp, nil
}

// Pause pauses a sandbox.
func (s *SandboxApi) Pause(ctx context.Context, sandboxID string) (string, error) {
	httpResp, err := s.apiClient.SandboxesApi.SandboxesSandboxIDPausePost(ctx, sandboxID).Execute()
	if err != nil {
		if httpResp != nil {
			if httpResp.StatusCode == http.StatusNotFound {
				return "", fmt.Errorf("sandbox %s not found", sandboxID)
			}
			if httpResp.StatusCode == http.StatusConflict {
				return sandboxID, nil
			}
		}
		return "", fmt.Errorf("failed to pause sandbox: %w", err)
	}

	return sandboxID, nil
}

// CreateSandboxOpts contains options for creating a sandbox.
type CreateSandboxOpts struct {
	Template            string
	Timeout             int32
	AutoPause           *bool
	AutoResume          *api.SandboxAutoResumeConfig
	Secure              bool
	AllowInternetAccess *bool
	Network             *api.SandboxNetworkConfig
	Metadata            map[string]string
	EnvVars             map[string]string
	Mcp                 map[string]interface{}
	VolumeMounts        []api.SandboxVolumeMount
}
