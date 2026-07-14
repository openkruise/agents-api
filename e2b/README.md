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
| `WithRequestTimeout(d time.Duration)` | HTTP request timeout, defaults to 60s                          |
| `WithHTTPClient(client *http.Client)` | Custom HTTP client for API requests                            |

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
```
