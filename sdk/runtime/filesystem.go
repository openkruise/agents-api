package runtime

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"net/url"
	"time"

	"connectrpc.com/connect"
	"github.com/openkruise/agents-api/sdk/proto/envd/filesystem"
	"github.com/openkruise/agents-api/sdk/proto/envd/filesystem/filesystemconnect"
)

const (
	runtimeFilesRoute = "/files"
	defaultUsername   = "node"
)

// EntryType represents the type of a filesystem entry.
type EntryType string

const (
	EntryTypeFile    EntryType = "file"
	EntryTypeDir     EntryType = "directory"
	EntryTypeSymlink EntryType = "symlink"
)

// EntryInfo represents information about a filesystem entry.
type EntryInfo struct {
	Name          string
	Type          EntryType
	Path          string
	Size          int64
	Mode          uint32
	Permissions   string
	Owner         string
	Group         string
	ModifiedTime  time.Time
	SymlinkTarget *string
}

// WriteInfo contains information about a written file.
type WriteInfo struct {
	Path string `json:"path"`
	Type string `json:"type"`
}

// Filesystem provides filesystem operations in the sandbox over the official
type Filesystem struct {
	Rpc         filesystemconnect.FilesystemClient
	httpClient  *http.Client
	runtimeURL  string
	baseFileURL *url.URL // pre-parsed base URL for file operations
	headers     map[string]string
}

// NewFilesystem creates a new Filesystem instance backed by the gRPC client
// and HTTP client for file content read/write.
func NewFilesystem(rpc filesystemconnect.FilesystemClient, httpClient *http.Client, runtimeURL string, headers map[string]string) *Filesystem {
	// Pre-parse the base file URL to avoid repeated parsing on every request.
	baseURL, _ := url.Parse(runtimeURL + runtimeFilesRoute)
	return &Filesystem{
		Rpc:         rpc,
		httpClient:  httpClient,
		runtimeURL:  runtimeURL,
		baseFileURL: baseURL,
		headers:     headers,
	}
}

// Read reads the content of a file and returns it as a byte slice.
// An optional user can be provided to run the operation as that user.
func (f *Filesystem) Read(ctx context.Context, path string, user ...string) ([]byte, error) {
	u := *f.baseFileURL // shallow copy of pre-parsed URL
	q := u.Query()
	q.Set("path", path)
	username := defaultUsername
	if len(user) > 0 && user[0] != "" {
		username = user[0]
	}
	q.Set("username", username)
	u.RawQuery = q.Encode()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, u.String(), nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	f.setHTTPHeaders(req)

	resp, err := f.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to read file: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNotFound {
		return nil, fmt.Errorf("file not found: %s", path)
	}
	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("failed to read file (status %d): %s", resp.StatusCode, string(body))
	}

	return io.ReadAll(resp.Body)
}

// ReadText reads the content of a file and returns it as a string.
// An optional user can be provided to run the operation as that user.
func (f *Filesystem) ReadText(ctx context.Context, path string, user ...string) (string, error) {
	data, err := f.Read(ctx, path, user...)
	if err != nil {
		return "", err
	}
	return string(data), nil
}

// Write writes content to a file. If the file doesn't exist, it will be created.
// If the file already exists, it will be overwritten.
// Writing to a path whose parent directories don't exist will create them automatically.
// An optional user can be provided to run the operation as that user.
func (f *Filesystem) Write(ctx context.Context, path string, data []byte, user ...string) (*WriteInfo, error) {
	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)

	part, err := writer.CreateFormFile("file", path)
	if err != nil {
		return nil, fmt.Errorf("failed to create form file: %w", err)
	}
	if _, err := part.Write(data); err != nil {
		return nil, fmt.Errorf("failed to write data to form: %w", err)
	}
	if err := writer.Close(); err != nil {
		return nil, fmt.Errorf("failed to close multipart writer: %w", err)
	}

	u := *f.baseFileURL // shallow copy of pre-parsed URL
	q := u.Query()
	q.Set("path", path)
	username := defaultUsername
	if len(user) > 0 && user[0] != "" {
		username = user[0]
	}
	q.Set("username", username)
	u.RawQuery = q.Encode()

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, u.String(), body)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", writer.FormDataContentType())
	f.setHTTPHeaders(req)

	resp, err := f.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to write file: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		respBody, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("failed to write file (status %d): %s", resp.StatusCode, string(respBody))
	}

	var results []WriteInfo
	if err := json.NewDecoder(resp.Body).Decode(&results); err != nil {
		// If we can't decode, return a basic WriteInfo with the path
		return &WriteInfo{Path: path}, nil
	}
	if len(results) == 0 {
		return &WriteInfo{Path: path}, nil
	}
	return &results[0], nil
}

// WriteText writes a string to a file (convenience wrapper around Write).
// An optional user can be provided to run the operation as that user.
func (f *Filesystem) WriteText(ctx context.Context, path string, content string, user ...string) (*WriteInfo, error) {
	return f.Write(ctx, path, []byte(content), user...)
}

// List lists entries in a directory.
func (f *Filesystem) List(ctx context.Context, path string, depth ...int32) ([]EntryInfo, error) {
	d := uint32(1)
	if len(depth) > 0 && depth[0] >= 1 {
		d = uint32(depth[0])
	}

	req := connect.NewRequest(&filesystem.ListDirRequest{
		Path:  path,
		Depth: d,
	})
	f.setRPCHeaders(req)

	resp, err := f.Rpc.ListDir(ctx, req)
	if err != nil {
		return nil, fmt.Errorf("failed to list directory: %w", err)
	}

	entries := make([]EntryInfo, 0, len(resp.Msg.Entries))
	for _, entry := range resp.Msg.Entries {
		entryType := mapFileType(entry.Type)
		if entryType == "" {
			continue
		}
		entries = append(entries, convertEntryInfo(entry))
	}

	return entries, nil
}

// Exists checks if a file or directory exists.
func (f *Filesystem) Exists(ctx context.Context, path string) (bool, error) {
	req := connect.NewRequest(&filesystem.StatRequest{Path: path})
	f.setRPCHeaders(req)

	_, err := f.Rpc.Stat(ctx, req)
	if err != nil {
		if connectErr, ok := err.(*connect.Error); ok && connectErr.Code() == connect.CodeNotFound {
			return false, nil
		}
		return false, fmt.Errorf("failed to check existence: %w", err)
	}

	return true, nil
}

// GetInfo returns information about a file or directory.
func (f *Filesystem) GetInfo(ctx context.Context, path string) (*EntryInfo, error) {
	req := connect.NewRequest(&filesystem.StatRequest{Path: path})
	f.setRPCHeaders(req)

	resp, err := f.Rpc.Stat(ctx, req)
	if err != nil {
		return nil, fmt.Errorf("failed to get file info: %w", err)
	}

	info := convertEntryInfo(resp.Msg.Entry)
	return &info, nil
}

// Remove removes a file or directory.
func (f *Filesystem) Remove(ctx context.Context, path string) error {
	req := connect.NewRequest(&filesystem.RemoveRequest{Path: path})
	f.setRPCHeaders(req)

	_, err := f.Rpc.Remove(ctx, req)
	if err != nil {
		return fmt.Errorf("failed to remove: %w", err)
	}

	return nil
}

// Rename renames/moves a file or directory.
func (f *Filesystem) Rename(ctx context.Context, oldPath, newPath string) (*EntryInfo, error) {
	req := connect.NewRequest(&filesystem.MoveRequest{
		Source:      oldPath,
		Destination: newPath,
	})
	f.setRPCHeaders(req)

	resp, err := f.Rpc.Move(ctx, req)
	if err != nil {
		return nil, fmt.Errorf("failed to rename: %w", err)
	}

	info := convertEntryInfo(resp.Msg.Entry)
	return &info, nil
}

// MakeDir creates a new directory (and all parent directories if needed).
func (f *Filesystem) MakeDir(ctx context.Context, path string) (bool, error) {
	req := connect.NewRequest(&filesystem.MakeDirRequest{Path: path})
	f.setRPCHeaders(req)

	_, err := f.Rpc.MakeDir(ctx, req)
	if err != nil {
		if connectErr, ok := err.(*connect.Error); ok && connectErr.Code() == connect.CodeAlreadyExists {
			return false, nil
		}
		return false, fmt.Errorf("failed to make directory: %w", err)
	}

	return true, nil
}

func mapFileType(ft filesystem.FileType) EntryType {
	switch ft {
	case filesystem.FileType_FILE_TYPE_FILE:
		return EntryTypeFile
	case filesystem.FileType_FILE_TYPE_DIRECTORY:
		return EntryTypeDir
	case filesystem.FileType_FILE_TYPE_SYMLINK:
		return EntryTypeSymlink
	default:
		return ""
	}
}

func convertEntryInfo(entry *filesystem.EntryInfo) EntryInfo {
	info := EntryInfo{
		Name:        entry.Name,
		Type:        mapFileType(entry.Type),
		Path:        entry.Path,
		Size:        entry.Size,
		Mode:        entry.Mode,
		Permissions: entry.Permissions,
		Owner:       entry.Owner,
		Group:       entry.Group,
	}

	if entry.ModifiedTime != nil {
		info.ModifiedTime = entry.ModifiedTime.AsTime()
	}

	if entry.SymlinkTarget != nil {
		target := *entry.SymlinkTarget
		info.SymlinkTarget = &target
	}

	return info
}

func (f *Filesystem) setRPCHeaders(req interface{ Header() http.Header }) {
	for k, v := range f.headers {
		req.Header().Set(k, v)
	}
}

func (f *Filesystem) setHTTPHeaders(req *http.Request) {
	for k, v := range f.headers {
		req.Header.Set(k, v)
	}
}
