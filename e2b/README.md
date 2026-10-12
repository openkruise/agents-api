# E2B Go SDK (Management Client)

## Installation

Add the `agents-api` dependency to your `go.mod`: [View Releases](https://github.com/openkruise/agents-api/releases)

```
require github.com/openkruise/agents-api <tag>
```

| Package | Import Path                            | Description                                                                                                                 |
|---------|----------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| **e2b** | `github.com/openkruise/agents-api/e2b` | **Management Client**: Sandbox lifecycle management (Create / Connect / Pause / Kill) + in-container ops (Commands / Files) |

---

## Package Structure

```
e2b/
├── api/                              #   OpenAPI-generated REST client (sandbox management)
├── sandbox.go                        #   Sandbox struct: Create / Connect / Pause / Kill
├── sandbox_api.go                    #   Low-level REST client (SandboxApi): List / GetInfo / Kill / ...
├── traffic_token.go                  #   Traffic JWT manager: transparent refresh, backoff, fail-closed
├── keys.go                           #   EncodeForE2BSDK: key-compat encoding for the E2B ecosystem
└── config.go                         #   ConnectionConfig: Protocol / Scheme / Domain / API URL
```

---

## Quick Start

```go
package main

import (
	"context"
	"fmt"
	"github.com/openkruise/agents-api/e2b"
	"log"
)

func main() {
	ctx := context.Background()

	sb, err := e2b.Create(ctx, "code-interpreter",
		e2b.WithConfig(
			e2b.WithAPIKey("your-api-key"),
			e2b.WithDomain("your.domain.com"),
		),
	)
	if err != nil {
		log.Fatal(err)
	}
	defer sb.Close(ctx)

	res, _ := sb.Commands.Run(ctx, "echo hello")
	fmt.Println(res.Stdout)

	sb.Files.MakeDir(ctx, "/tmp/demo")
}
```

Full
example: [Management Client Example](https://github.com/openkruise/agents-api/blob/master/examples/e2b-example/main.go)

---

## Connection Configuration

### Scheme & Protocol

Connection behavior is controlled by `ConnectionConfig`, determined by two orthogonal dimensions: **Scheme** and *
*Protocol**.

#### Protocol (Routing)

| Value                | Constant              | API URL                          | Sandbox URL                                     |
|----------------------|-----------------------|----------------------------------|-------------------------------------------------|
| **Native (default)** | `e2b.ProtocolNative`  | `https://api.<domain>`           | `https://<port>-<sandboxID>.<domain>`           |
| **Private**          | `e2b.ProtocolPrivate` | `<scheme>://<domain>/kruise/api` | `<scheme>://<domain>/kruise/<sandboxID>/<port>` |

- **Native**: Subdomain-based routing for public cloud deployments
- **Private**: Path-prefix routing (`/kruise/...`) via a unified gateway, suitable for private deployments or local
  port-forwarding

> The code interpreter (`sb.CodeInterpreter`) uses its own port (default `49999`), so its URL differs from the sandbox
> URL above: `https://49999-<sandboxID>.<domain>` (Native) or `<scheme>://<domain>/kruise/<sandboxID>/49999` (Private).
> This is handled automatically by `sb.CodeInterpreter` — see
> the [Runtime SDK Code Interpreter](https://github.com/openkruise/agents-api/blob/master/runtime/README.md#code-interpreter)
> section for the API surface.

#### Scheme

| Value                   | Use Case                                 |
|-------------------------|------------------------------------------|
| **`"https"` (default)** | Production / public network              |
| **`"http"`**            | Local port-forward, intranet without TLS |

#### ConnectionConfigOption List

Applied via `e2b.NewConnectionConfig(opts...)` or embedded in `Create/Connect` with `WithConfig(...)`:

| Option                                | Description                                                    |
|---------------------------------------|----------------------------------------------------------------|
| `WithAPIKey(key string)`              | API Key, sent as `X-API-Key` header                            |
| `WithDomain(domain string)`           | Domain name, defaults to `your.domain.com`                     |
| `WithScheme(scheme string)`           | URL scheme, defaults to `https`                                |
| `WithProtocol(p Protocol)`            | Routing protocol, defaults to `ProtocolNative`                 |
| `WithAPIURL(url string)`              | **Highest priority**: directly overrides API base URL          |
| `WithSandboxBaseURL(url string)`      | **Highest priority**: directly overrides sandbox envd base URL |
| `WithCodeInterpreterPort(port int)`   | Code interpreter port, defaults to `49999`                     |
| `WithRequestTimeout(d time.Duration)` | HTTP request timeout, defaults to 60s                          |
| `WithHTTPClient(client *http.Client)` | Custom HTTP client for API requests                            |
| `WithHeader(key, value string)`         | Add a custom header sent with every request                   |

#### Priority

`WithAPIURL` / `WithSandboxBaseURL` (explicit override) > `WithProtocol` + `WithDomain` assembly > environment
variables > defaults

---

## Create / Connect Sandbox

### `Create(ctx, template, opts...) (*Sandbox, error)`

Creates a new sandbox from a template. Defaults to `"code-interpreter"` when `template` is empty.

```go
package main

import (
	"github.com/openkruise/agents-api/e2b"
)

func main() {
	sb, err := e2b.Create(ctx, "code-interpreter",
		e2b.WithConfig(
			e2b.WithAPIKey("xxx"),
			e2b.WithDomain("example.com"),
			e2b.WithProtocol(e2b.ProtocolPrivate),
		),
		e2b.WithTimeout(600),
		e2b.WithMetadata(map[string]string{"k": "v"}),
		e2b.WithEnvVars(map[string]string{"FOO": "1"}),
		e2b.WithAutoPause(true),
		e2b.WithSecure(true),
	)
}
```

### `Connect(ctx, sandboxID, opts...) (*Sandbox, error)`

Connects to an existing sandbox.

```go
sb, err := e2b.Connect(ctx, "default--xxx-xxx",
e2b.WithConfig(e2b.WithAPIKey("xxx"), e2b.WithDomain("example.com")),
)
```

### SandboxOption List

| Option                       | Description                                 |
|------------------------------|---------------------------------------------|
| `WithConfig(opts...)`        | Embed a set of `ConnectionConfigOption`     |
| `WithTimeout(seconds int32)` | Sandbox TTL, defaults to 300s               |
| `WithMetadata(map)`          | Sandbox metadata                            |
| `WithEnvVars(map)`           | Environment variables injected into sandbox |
| `WithAutoPause(bool)`        | Enable auto-pause                           |
| `WithSecure(bool)`           | Enable secure mode                          |

### Sandbox Instance Methods

| Method                                 | Description                              |
|----------------------------------------|------------------------------------------|
| `SandboxID() string`                   | Returns the sandbox ID                   |
| `TemplateID() string`                  | Returns the template ID                  |
| `GetInfo(ctx) (*SandboxInfo, error)`   | Get sandbox details                      |
| `SetTimeout(ctx, timeout int32) error` | Update timeout                           |
| `Pause(ctx) (string, error)`           | Pause the sandbox                        |
| `Kill(ctx) (bool, error)`              | Destroy the sandbox                      |
| `Close(ctx) error`                     | Alias for `Kill`, convenient for `defer` |
| `TrafficAccessToken() string`          | Currently cached traffic token (no refresh) |
| `RefreshTrafficAccessToken(ctx) (string, error)` | Force a traffic token refresh    |

`Sandbox` exposes two sub-modules:

- `sb.Commands` — Command execution (`*Commands`)
- `sb.Files` — Filesystem operations (`*Filesystem`)

---

## Sandbox Management API (SandboxApi)

`SandboxApi` is a low-level REST client that can be used independently without creating a Sandbox instance (e.g.,
listing all sandboxes).

```go
api := e2b.NewSandboxApi(e2b.NewConnectionConfig(
e2b.WithAPIKey("xxx"),
e2b.WithDomain("example.com"),
))
```

| Method                                                                       | Description                                   |
|------------------------------------------------------------------------------|-----------------------------------------------|
| `List(ctx, opts ...ListSandboxOpts) (*ListResult, error)`                    | List sandboxes with pagination support        |
| `GetInfo(ctx, sandboxID) (*SandboxInfo, error)`                              | Get sandbox details; 404 returns `not found`  |
| `Kill(ctx, sandboxID) (bool, error)`                                         | Destroy sandbox; returns `true` in Debug mode |
| `SetTimeout(ctx, sandboxID, timeout int32) error`                            | Update timeout                                |
| `CreateSandbox(ctx, opts CreateSandboxOpts) (*SandboxCreateResponse, error)` | Low-level create API                          |
| `ConnectSandbox(ctx, sandboxID, timeout int32) (*client.Sandbox, error)`     | Low-level connect API                         |
| `Pause(ctx, sandboxID) (string, error)`                                      | Pause sandbox                                 |
| `RefreshTrafficAccessToken(ctx, sandboxID) (*TrafficAccessToken, error)`      | Fetch a fresh traffic access token            |

### ListSandboxOpts

Options for paginated listing:

```go
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
```

### ListResult

Result of a list operation with pagination support:

```go
type ListResult struct {
    Sandboxes []SandboxInfo  // List of sandboxes in this page
    NextToken string         // Token for fetching the next page (empty if no more pages)
}
```

### Pagination Example

```go
// Fetch all sandboxes with pagination
var allSandboxes []e2b.SandboxInfo
nextToken := ""
pageSize := int32(10)

for {
    result, err := api.List(ctx, e2b.ListSandboxOpts{
        Limit:     pageSize,
        NextToken: nextToken,
    })
    if err != nil {
        log.Fatal(err)
    }
    
    allSandboxes = append(allSandboxes, result.Sandboxes...)
    
    // Check if there are more pages
    if result.NextToken == "" {
        break
    }
    nextToken = result.NextToken
}

fmt.Printf("Total: %d sandboxes\n", len(allSandboxes))
```

---

## Traffic JWT Refresh

When a sandbox is created with metadata `"security.agents.kruise.io/enable-jwt-auth": "true"`, the sandbox-manager
issues a short-lived **Traffic JWT**. Every data-plane request (Commands / Files / CodeInterpreter) must then carry it
as the `e2b-traffic-access-token` header, or the gateway rejects the request with 403.

The SDK handles this transparently — **no code change is required on the caller side**:

1. `Create` / `Connect` receive the initial token from the management API and install a token manager
   (empty-token bootstrap via a first refresh when connecting to an already-running JWT sandbox without a token).
2. Every data-plane request obtains a valid token right before it is sent. Tokens are refreshed ahead of expiry
   (`min(300s, max(60s, validity/5))`, minus a small random jitter to spread refreshes).
3. Concurrent requests needing a refresh at the same time coalesce into a **single** refresh call (this also holds
   when the refresh fails: concurrent forced refreshes keep the current token and share one retry).
4. Failed refreshes retry with exponential backoff (server `Retry-After` is honored). While the current token is
   still valid, the stale token is used; once it expires the manager **fails closed** and returns
   `*TrafficAccessTokenExpired` instead of sending a request that would be rejected.
5. A refresh is detached from the caller's context: cancelling the caller does not abort the in-flight refresh.

```go
sb, err := e2b.Create(ctx, "code-interpreter",
    e2b.WithConfig(e2b.WithAPIKey("xxx"), e2b.WithDomain("example.com")),
    e2b.WithMetadata(map[string]string{"security.agents.kruise.io/enable-jwt-auth": "true"}),
)
// ...data-plane calls (Commands / Files / CodeInterpreter) just work —
// the token is attached and refreshed automatically.

// Optional manual control:
token := sb.TrafficAccessToken()          // cached token, no refresh, "" when not JWT-protected
token, err = sb.RefreshTrafficAccessToken(ctx)  // forced refresh (concurrent forces coalesce)

// Externally issued initial token (external identity provider deployments):
sb2, err := e2b.Connect(ctx, id,
    e2b.WithConfig(e2b.WithAPIKey("xxx"), e2b.WithDomain("example.com")),
    e2b.WithTrafficAccessToken(jwt), // seed the manager; refreshes continue via the management API
)
```

Non-JWT sandboxes (no traffic token, legacy opaque tokens) and `WithDebug(true)` keep the legacy behavior: no
traffic header, no refresh, and `RefreshTrafficAccessToken` returns an error.

> The low-level `SandboxApi.RefreshTrafficAccessToken(ctx, sandboxID)` POSTs
> `/sandboxes/{sandboxID}/traffic-access-token` and returns the raw
> `*TrafficAccessToken{Token, ExpiresAt}`; the `Sandbox`-level machinery above is the recommended entry point.

### Verifying against a JWT-enabled gateway

`examples/jwt-gateway-example` is the Go counterpart of the Python SDK's `demo_jwt_gateway.py`: it runs the
complete flow against a sandbox-gateway with `enableJwtAuth=true` and an external OIDC provider — a data-plane
call rejected without a valid JWT, calls carrying an externally issued token (`WithTrafficAccessToken`), and
automatic rotation of short-lived tokens. A local translating proxy routes the refresh endpoint to the external
issuer (the OSS sandbox-manager issues opaque UUID tokens), so the full SDK refresh path — headers, Retry-After
handling, response validation — is exercised end to end. See the example's header comment for deployment
prerequisites and environment variables.

### Key Compatibility Encoding

`keys.go` provides `EncodeForE2BSDK`, which wraps a raw (OpenKruise) API key into the format understood by
E2B-ecosystem tooling that expects `e2b_`-prefixed keys:

```go
encoded := e2b.EncodeForE2BSDK("my-raw-key") // e2b_6f6b6167...
```

The encoding is `e2b_` + fixed magic + version `01` + 8-hex-digit length + hex body + 8-byte SHA-256 checksum —
byte-for-byte compatible with the Python SDK's `encode_for_e2b_sdk`.

### Rollout

Sandbox-manager preserves the legacy, approximately 100-year Traffic JWT validity by default. Deploy this SDK (or
another client with equivalent refresh support) before configuring a shorter `--traffic-access-token-validity` for
existing JWT-authenticated workloads; clients that do not refresh lose data-plane access when a short-lived token
expires.

Deploy the lazy-Connect behavior (recovering a missing token by checking the sandbox metadata on reconnect) before
upgrading sandbox-manager to a version that no longer issues Traffic JWTs from Connect. Older clients cannot recover
a missing token when reconnecting by sandbox ID.

---

## Command Execution (Commands)

Operate in-container processes via `sb.Commands`. Uses the envd `Process` gRPC service under the hood.

### Methods

| Method                                                      | Description                                                                 |
|-------------------------------------------------------------|-----------------------------------------------------------------------------|
| `Run(ctx, cmd, opts...) (*CommandResult, error)`            | **Foreground**: run and wait for completion, returns stdout/stderr/exitCode |
| `Start(ctx, cmd, opts...) (*CommandHandle, error)`          | **Background**: returns a handle, caller decides when to `Wait`             |
| `List(ctx) ([]ProcessInfo, error)`                          | List all running processes                                                  |
| `Kill(ctx, pid uint32) (bool, error)`                       | Send SIGKILL to PID; returns `false, nil` if not found                      |
| `SendStdin(ctx, pid uint32, data string) error`             | Write data to a process's stdin                                             |
| `ConnectToProcess(ctx, pid uint32) (*CommandHandle, error)` | Reconnect to a running process, subscribe to output                         |

### `RunOpts` Fields

```go
type RunOpts struct {
Envs       map[string]string // Process environment variables
Cwd        string            // Working directory
Stdin      bool // Allow writing via SendStdin
Background bool // Background execution (reserved)
OnStdout   func (string) // Streaming stdout callback (foreground)
OnStderr   func (string) // Streaming stderr callback (foreground)
}
```

> Commands are executed via `/bin/bash -l -c <cmd>`, preserving the login environment.

### `CommandHandle`

Returned by `Start` / `ConnectToProcess` for interaction or waiting:

| Method                                             | Description                                                  |
|----------------------------------------------------|--------------------------------------------------------------|
| `Pid() uint32`                                     | Returns the process PID                                      |
| `Wait(onStdout, onStderr) (*CommandResult, error)` | Block until exit; non-zero exit includes `*CommandExitError` |
| `Disconnect()`                                     | Disconnect subscription but **do not kill** process          |
| `Kill() bool`                                      | Kill the process                                             |

### `CommandResult` / `CommandExitError`

```go
type CommandResult struct {
Stdout   string
Stderr   string
ExitCode int32
Error    string
}

// Returned when exit code is non-zero (alongside *CommandResult)
type CommandExitError struct {
Stdout, Stderr string
ExitCode       int32
ErrorMessage   string
}
```

### Examples

```go
// Foreground execution + streaming output
res, err := sb.Commands.Run(ctx, "ls -la /tmp", e2b.RunOpts{
Cwd:      "/tmp",
Envs:     map[string]string{"LANG": "C"},
OnStdout: func (line string) { fmt.Print(line) },
})

// Background start + manual Kill
h, _ := sb.Commands.Start(ctx, "sleep 60")
fmt.Println("pid =", h.Pid())
h.Kill()
```

---

## Filesystem

Operate in-container files via `sb.Files`. Metadata operations use envd Filesystem gRPC; file content read/write uses
the HTTP `/files` endpoint.

### Methods

| Method                                                              | Description                                                  |
|---------------------------------------------------------------------|--------------------------------------------------------------|
| `List(ctx, path, depth...) ([]EntryInfo, error)`                    | List directory entries; `depth` defaults to 1                |
| `Exists(ctx, path) (bool, error)`                                   | Check if path exists (via `Stat`, 404 → false)               |
| `GetInfo(ctx, path) (*EntryInfo, error)`                            | Get file/directory info                                      |
| `MakeDir(ctx, path) (bool, error)`                                  | Recursively create directory; returns `false, nil` if exists |
| `Rename(ctx, oldPath, newPath) (*EntryInfo, error)`                 | Rename / move                                                |
| `Remove(ctx, path) error`                                           | Delete file or directory                                     |
| `Read(ctx, path, user...) ([]byte, error)`                          | Read file content (binary); `user` defaults to `"node"`      |
| `ReadText(ctx, path, user...) (string, error)`                      | Read file content (text)                                     |
| `ReadStream(ctx, path, user...) (io.ReadCloser, error)`             | Stream file content; caller must Close                      |
| `Write(ctx, path, data []byte, user...) (*WriteInfo, error)`        | Write file content (binary); auto-creates parent dirs        |
| `WriteText(ctx, path, content string, user...) (*WriteInfo, error)` | Write file content (text)                                    |

### Examples

```go
// Directory operations
sb.Files.MakeDir(ctx, "/tmp/work")

entries, _ := sb.Files.List(ctx, "/tmp")
for _, e := range entries {
fmt.Printf("%s %s (%d bytes)\n", e.Type, e.Name, e.Size)
}

sb.Files.Rename(ctx, "/tmp/work", "/tmp/done")
sb.Files.Remove(ctx, "/tmp/done")

// File content read/write
sb.Files.WriteText(ctx, "/tmp/hello.txt", "Hello, World!")
content, _ := sb.Files.ReadText(ctx, "/tmp/hello.txt")
fmt.Println(content) // Hello, World!

// Stream large files (caller must Close)
rc, _ := sb.Files.ReadStream(ctx, "/tmp/large.log")
defer rc.Close()
io.Copy(os.Stdout, rc)
```
