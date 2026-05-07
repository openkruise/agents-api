package sandbox

import (
	"context"
	"fmt"
	"net/http"
	"time"

	"github.com/openkruise/agents-api/sdk/proto/api"
)

// SandboxInfo represents information about a sandbox.
type SandboxInfo struct {
	SandboxID   string
	TemplateID  string
	Alias       string
	ClientID    string
	StartedAt   time.Time
	EndAt       time.Time
	CpuCount    int32
	MemoryMB    int32
	DiskSizeMB  int32
	EnvdVersion string
	Metadata    map[string]string
	State       string
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

// List lists all running sandboxes.
func (s *SandboxApi) List(ctx context.Context) ([]SandboxInfo, error) {
	resp, httpResp, err := s.apiClient.SandboxesApi.V2SandboxesGet(ctx).Execute()
	if err != nil {
		return nil, fmt.Errorf("failed to list sandboxes: %w", err)
	}
	if httpResp.StatusCode >= 300 {
		return nil, fmt.Errorf("list sandboxes failed with status %d", httpResp.StatusCode)
	}

	sandboxes := make([]SandboxInfo, len(resp))
	for i, sb := range resp {
		sandboxes[i] = SandboxInfo{
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
			sandboxes[i].Metadata = *meta
		}
	}

	return sandboxes, nil
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
	Template  string
	Timeout   int32
	AutoPause *bool
	Metadata  map[string]string
	EnvVars   map[string]string
	Secure    bool
}
