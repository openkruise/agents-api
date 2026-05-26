# Sandbox Go SDK

## Installation

First, add the `agents-api` dependency to
your `go.mod`: [View Releases](https://github.com/openkruise/agents-api/releases)

```
require github.com/openkruise/agents-api <tag>
```

This package provides two ways to operate within an Sandbox environment:

| Package     | Import Path                                    | Purpose                                                                                                                            |
|-------------|------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| **sandbox** | `github.com/openkruise/agents-api/sdk/sandbox` | **Management Client**: Sandbox lifecycle management (Create / Connect / Pause / Kill) + in-container operations (Commands / Files) |
| **runtime** | `github.com/openkruise/agents-api/sdk/runtime` | **Runtime Client**: Directly operates on a running sandbox's envd service for command execution and file operations                |

---

## Package Structure

```
sdk/
├── sandbox/                          #   Management Client (package sandbox)
│   ├── sandbox.go                    #   Sandbox struct: Create / Connect / Pause / Kill
│   ├── sandbox_api.go                #   Low-level REST client (SandboxApi): List / GetInfo / Kill / ...
│   └── config.go                     #   ConnectionConfig: Protocol / Scheme / Domain / API URL
│
├── runtime/                          #   Runtime Client (package runtime)
│   ├── client.go                     #   Client struct: New / NewWithConfig
│   ├── k8s.go                        #   NewFromK8s: auto-resolve sandboxID & runtimeToken from K8s
│   ├── config.go                     #   Config & Options: Domain / Scheme / RuntimeToken / ...
│   ├── commands.go                   #   Commands: Run / Start / Kill / SendStdin / List / ConnectToProcess
│   ├── command_handle.go             #   CommandHandle: Wait / Disconnect / Kill
│   └── filesystem.go                 #   Filesystem: List / Exists / GetInfo / MakeDir / Rename / Remove / Read / Write
│
├── proto/
│   ├── api/                          #   OpenAPI generated REST client (sandbox management)
│   └── envd/                         #   protobuf generated code
│       ├── process/                  #   envd Process gRPC
│       │   ├── process.pb.go
│       │   └── processconnect/
│       └── filesystem/               #   envd Filesystem gRPC
│           ├── filesystem.pb.go
│           └── filesystemconnect/
│
├── README.md
└── README_zh-CH.md
```

---

## Quick Start: sandbox (Management Client)

```go
package main

import (
	"context"
	"fmt"
	"github.com/openkruise/agents-api/sdk/sandbox"
	"log"
)

func main() {
	ctx := context.Background()

	sb, err := sandbox.Create(ctx, "code-interpreter",
		sandbox.WithConfig(
			sandbox.WithAPIKey("your-api-key"),
			sandbox.WithDomain("your.domain.com"),
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

[Full example](https://github.com/openkruise/agents-api/blob/master/sdk/example/sandbox/main.go)

---

## Quick Start: runtime (Runtime Client)

When running inside a cluster or with kubeconfig access, use `NewFromK8s` to automatically resolve `sandboxID` and `runtimeToken` from the Sandbox CR:

```go
package main

import (
	"context"
	"fmt"

	"github.com/openkruise/agents-api/sdk/runtime"
)

func main() {
	ctx := context.Background()

	// domain is the address of the sandbox gateway.
	// In-cluster: use the K8s Service DNS, e.g. "sandbox-gateway.sandbox-system.svc:7788"
	// Local dev:  use port-forward address, e.g. "127.0.0.1:7788"
	domain := "sandbox-gateway.sandbox-system.svc:7788"
	namespace := "default"
	sandboxName := "your-sandbox-name"
	c, err := runtime.NewFromK8s(ctx, namespace, sandboxName,
		runtime.WithDomain(domain),
	)
	if err != nil {
		fmt.Printf("Error: %v\n", err)
		return
	}

	fmt.Printf("Runtime URL: %s\n", c.RuntimeURL())

	res, _ := c.Commands.Run(ctx, "uname -a")
	fmt.Println(res.Stdout)
}
```

**Key Notes:**
- `NewFromK8s` queries the Sandbox CR and extracts `runtimeToken` from annotation `agents.kruise.io/runtime-access-token`
- `sandboxID` is composed as `namespace--name` (double-dash separator)
- Kubeconfig resolution order: `KUBECONFIG` env → `~/.kube/config` → in-cluster config
- The underlying K8s client is created once and shared across calls (via `sync.Once`)

[Full example](https://github.com/openkruise/agents-api/blob/master/sdk/example/runtime_client/main.go)

---

## Connection Configuration

### sandbox Package: Scheme & Protocol

Connection behavior in the sandbox package is controlled via `ConnectionConfig`, determined by two orthogonal
dimensions: **Scheme** and **Protocol**.

#### Protocol (Routing Mode)

| Value                | Constant                  | API URL                          | Sandbox URL                                     |
|----------------------|---------------------------|----------------------------------|-------------------------------------------------|
| **Native (default)** | `sandbox.ProtocolNative`  | `https://api.<domain>`           | `https://<port>-<sandboxID>.<domain>`           |
| **Private**          | `sandbox.ProtocolPrivate` | `<scheme>://<domain>/kruise/api` | `<scheme>://<domain>/kruise/<sandboxID>/<port>` |

- **Native**: Subdomain-based routing, for standard public cloud deployments
- **Private**: Path-prefix-based routing (`/kruise/...`) through a unified gateway, for private deployments or local
  port forwarding

#### Scheme

| Value                   | Use Case                                           |
|-------------------------|----------------------------------------------------|
| **`"https"` (default)** | Production / public network                        |
| **`"http"`**            | Local port forwarding, TLS-free intranet debugging |

#### ConnectionConfigOption List

Applied via `sandbox.NewConnectionConfig(opts...)` or embedded in `Create/Connect` calls via `WithConfig(...)`:

| Option                                | Description                                                                     |
|---------------------------------------|---------------------------------------------------------------------------------|
| `WithAPIKey(key string)`              | API Key, sent as `X-API-Key` header                                             |
| `WithDomain(domain string)`           | Domain, default `your.domain.com`                                               |
| `WithScheme(scheme string)`           | URL scheme, default `https`                                                     |
| `WithProtocol(p Protocol)`            | Routing protocol, default `ProtocolNative`                                      |
| `WithAPIURL(url string)`              | **Highest priority**: overrides API base URL directly, bypasses Protocol/Domain |
| `WithSandboxBaseURL(url string)`      | **Highest priority**: overrides sandbox envd base URL directly                  |
| `WithRequestTimeout(d time.Duration)` | HTTP request timeout, default 60s                                               |

#### Priority

`WithAPIURL` / `WithSandboxBaseURL` (explicit override) > `WithProtocol` + `WithDomain` composition > environment
variables > defaults

---

### runtime Package: Runtime Client Config

The Runtime Client **does not involve Protocol** — only `Scheme` + `Domain` are needed to determine the envd
address (`<scheme>://<domain>`).

#### Option List

| Option                                | Description                                    |
|---------------------------------------|------------------------------------------------|
| `WithDomain(domain string)`           | envd domain, default `your.domain.com`         |
| `WithScheme(scheme string)`           | URL scheme, default `http`                     |
| `WithRuntimeToken(token string)`      | Runtime token, sent as `X-Access-Token` header |
| `WithRuntimePort(port int)`           | Runtime port, default `49983`                  |
| `WithAPIKey(apiKey string)`           | Optional API Key                               |
| `WithAuthHeader(header string)`       | Override default Authorization header          |
| `WithSandboxBaseURL(url string)`      | Fully override URL composition                 |
| `WithHeader(key, value string)`       | Add a single custom header                     |
| `WithHeaders(headers map)`            | Merge multiple custom headers                  |
| `WithRequestTimeout(d time.Duration)` | HTTP timeout, default 60s                      |
| `WithConfig(cfg *Config)`             | Replace with a pre-built Config                |

---

## Create / Connect Sandbox

### `Create(ctx, template, opts...) (*Sandbox, error)`

Creates a new sandbox from a template. Defaults to `"code-interpreter"` when template is empty.

```go
package main

import (
	"github.com/openkruise/agents-api/sdk/sandbox"
)

func main() {
	sb, err := sandbox.Create(ctx, "code-interpreter",
		sandbox.WithConfig(
			sandbox.WithAPIKey("xxx"),
			sandbox.WithDomain("example.com"),
			sandbox.WithProtocol(sandbox.ProtocolPrivate),
		),
		sandbox.WithTimeout(600),
		sandbox.WithMetadata(map[string]string{"k": "v"}),
		sandbox.WithEnvVars(map[string]string{"FOO": "1"}),
		sandbox.WithAutoPause(true),
		sandbox.WithSecure(true),
	)
}
```

### `Connect(ctx, sandboxID, opts...) (*Sandbox, error)`

Connects to an existing sandbox. Automatically resumes if paused.

```go
sb, err := sandbox.Connect(ctx, "default--xxx-xxx",
sandbox.WithConfig(sandbox.WithAPIKey("xxx"), sandbox.WithDomain("example.com")),
)
```

### SandboxOption List

| Option                       | Description                                     |
|------------------------------|-------------------------------------------------|
| `WithConfig(opts...)`        | Embed a set of `ConnectionConfigOption`         |
| `WithTimeout(seconds int32)` | Sandbox lifetime timeout, default 300 seconds   |
| `WithMetadata(map)`          | Sandbox metadata                                |
| `WithEnvVars(map)`           | Environment variables injected into the sandbox |
| `WithAutoPause(bool)`        | Whether to enable auto-pause                    |
| `WithSecure(bool)`           | Enable secure mode                              |

### Sandbox Instance Methods

| Method                                 | Description                              |
|----------------------------------------|------------------------------------------|
| `SandboxID() string`                   | Returns the sandbox ID                   |
| `TemplateID() string`                  | Returns the template ID                  |
| `GetInfo(ctx) (*SandboxInfo, error)`   | Gets sandbox details                     |
| `SetTimeout(ctx, timeout int32) error` | Updates the timeout                      |
| `Pause(ctx) (string, error)`           | Pauses the sandbox                       |
| `Kill(ctx) (bool, error)`              | Destroys the sandbox                     |
| `Close(ctx) error`                     | Alias for `Kill`, convenient for `defer` |

`Sandbox` exposes two sub-modules:

- `sb.Commands` — command execution (`*Commands`)
- `sb.Files` — filesystem operations (`*Filesystem`)

---

## Sandbox Management API (SandboxApi)

`SandboxApi` is the low-level REST client that can be used independently without creating a `Sandbox` instance (e.g., to
list all sandboxes).

```go
api := sandbox.NewSandboxApi(sandbox.NewConnectionConfig(
sandbox.WithAPIKey("xxx"),
sandbox.WithDomain("example.com"),
))
```

| Method                                                                       | Description                                                 |
|------------------------------------------------------------------------------|-------------------------------------------------------------|
| `List(ctx) ([]SandboxInfo, error)`                                           | Lists all running sandboxes                                 |
| `GetInfo(ctx, sandboxID) (*SandboxInfo, error)`                              | Gets sandbox details; returns `not found` error on 404      |
| `Kill(ctx, sandboxID) (bool, error)`                                         | Destroys a sandbox; returns `true` directly in `Debug` mode |
| `SetTimeout(ctx, sandboxID, timeout int32) error`                            | Updates the timeout                                         |
| `CreateSandbox(ctx, opts CreateSandboxOpts) (*SandboxCreateResponse, error)` | Low-level create API                                        |
| `ConnectSandbox(ctx, sandboxID, timeout int32) (*client.Sandbox, error)`     | Low-level connect API                                       |
| `Pause(ctx, sandboxID) (string, error)`                                      | Pauses a sandbox                                            |

---

## Command Execution (Commands)

Operate in-container processes via `sb.Commands` (sandbox mode) or `c.Commands` (direct mode). Backed by
envd's `Process` gRPC service.

### Methods

| Method                                                      | Description                                                                                         |
|-------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `Run(ctx, cmd, opts...) (*CommandResult, error)`            | **Foreground execution**: starts a command and waits for completion, returns stdout/stderr/exitCode |
| `Start(ctx, cmd, opts...) (*CommandHandle, error)`          | **Background start**: returns a handle, caller decides when to `Wait`                               |
| `List(ctx) ([]ProcessInfo, error)`                          | Lists all running processes                                                                         |
| `Kill(ctx, pid uint32) (bool, error)`                       | Sends SIGKILL to the given PID; returns `false, nil` if process doesn't exist                       |
| `SendStdin(ctx, pid uint32, data string) error`             | Writes data to a process's stdin                                                                    |
| `ConnectToProcess(ctx, pid uint32) (*CommandHandle, error)` | Reconnects to a running process, subscribing to its subsequent output                               |

### `RunOpts` Fields

```go
type RunOpts struct {
Envs       map[string]string // Process environment variables
Cwd        string            // Working directory
Stdin      bool // Whether to allow SendStdin
Background bool // Background execution (reserved)
OnStdout   func (string) // Streaming stdout callback (foreground only)
OnStderr   func (string) // Streaming stderr callback (foreground only)
}
```

> Commands are executed via `/bin/bash -l -c <cmd>`, preserving the login environment.

### `CommandHandle`

Returned by `Start` / `ConnectToProcess` for interaction or waiting:

| Method                                             | Description                                                              |
|----------------------------------------------------|--------------------------------------------------------------------------|
| `Pid() uint32`                                     | Returns the process PID                                                  |
| `Wait(onStdout, onStderr) (*CommandResult, error)` | Blocks until completion; non-zero exit code includes `*CommandExitError` |
| `Disconnect()`                                     | Disconnects from the stream **without killing** the process              |
| `Kill() bool`                                      | Kills the process                                                        |

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

### Example

```go
// Foreground execution with streaming output
res, err := sb.Commands.Run(ctx, "ls -la /tmp", runtime.RunOpts{
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

Operate in-container files via `sb.Files` (sandbox mode) or `c.Files` (direct mode). Metadata operations use envd
Filesystem gRPC, file content read/write uses the HTTP `/files` endpoint.

### Methods

| Method                                                              | Description                                                             |
|---------------------------------------------------------------------|-------------------------------------------------------------------------|
| `List(ctx, path, depth...) ([]EntryInfo, error)`                    | Lists directory entries; `depth` defaults to 1                          |
| `Exists(ctx, path) (bool, error)`                                   | Checks if a path exists (via `Stat`, returns false on 404)              |
| `GetInfo(ctx, path) (*EntryInfo, error)`                            | Gets file/directory info                                                |
| `MakeDir(ctx, path) (bool, error)`                                  | Creates a directory recursively; returns `false, nil` if already exists |
| `Rename(ctx, oldPath, newPath) (*EntryInfo, error)`                 | Renames/moves a file or directory                                       |
| `Remove(ctx, path) error`                                           | Removes a file or directory                                             |
| `Read(ctx, path, user...) ([]byte, error)`                          | Reads file content (binary); `user` defaults to `"node"`                |
| `ReadText(ctx, path, user...) (string, error)`                      | Reads file content (text)                                               |
| `Write(ctx, path, data []byte, user...) (*WriteInfo, error)`        | Writes file content (binary); auto-creates parent directories           |
| `WriteText(ctx, path, content string, user...) (*WriteInfo, error)` | Writes file content (text)                                              |

### Example

```go
package main

import (
	"fmt"
)

func main() {
	// Directory operations
	c.Files.MakeDir(ctx, "/tmp/work")

	entries, _ := c.Files.List(ctx, "/tmp")
	for _, e := range entries {
		fmt.Printf("%s %s (%d bytes)\n", e.Type, e.Name, e.Size)
	}

	c.Files.Rename(ctx, "/tmp/work", "/tmp/done")
	c.Files.Remove(ctx, "/tmp/done")

	// File content read/write
	c.Files.WriteText(ctx, "/tmp/hello.txt", "Hello, World!")
	content, _ := c.Files.ReadText(ctx, "/tmp/hello.txt")
	fmt.Println(content) // Hello, World!

	// Binary read/write
	c.Files.Write(ctx, "/tmp/data.bin", []byte{0x00, 0x01, 0x02})
	data, _ := c.Files.Read(ctx, "/tmp/data.bin")
}
```

---

## Full Examples

### sandbox Mode (Management Client)

```go
package main

import (
	"context"
	"fmt"
	"github.com/openkruise/agents-api/sdk/sandbox"
	"log"
)

func main() {
	ctx := context.Background()

	sb, err := sandbox.Create(ctx, "code-interpreter",
		sandbox.WithConfig(
			sandbox.WithAPIKey("your-api-key"),
			sandbox.WithDomain("your.domain.com"),
			sandbox.WithProtocol(sandbox.ProtocolPrivate),
		),
		sandbox.WithTimeout(600),
	)
	if err != nil {
		log.Fatal(err)
	}
	defer sb.Close(ctx)

	fmt.Println("sandbox:", sb.SandboxID())

	if res, err := sb.Commands.Run(ctx, "uname -a"); err == nil {
		fmt.Println(res.Stdout)
	}

	sb.Files.MakeDir(ctx, "/tmp/demo")
}

```

### runtime Mode (Runtime Client)

```go
package main

import (
	"context"
	"fmt"
	"github.com/openkruise/agents-api/sdk/runtime"
)

func main() {
	ctx := context.Background()

	c := runtime.New("your-sandbox-id",
		runtime.WithDomain("your.domain.com"),
		runtime.WithScheme("http"),
		runtime.WithRuntimeToken("your-runtime-token"),
	)

	if res, err := c.Commands.Run(ctx, "uname -a"); err == nil {
		fmt.Println(res.Stdout)
	}

	c.Files.MakeDir(ctx, "/tmp/demo")

	// File content read/write
	c.Files.WriteText(ctx, "/tmp/hello.txt", "Hello from Go SDK!")
	content, _ := c.Files.ReadText(ctx, "/tmp/hello.txt")
	fmt.Println(content)
}

```

For more complete demos, see:

- [Management Client example](https://github.com/openkruise/agents-api/blob/master/sdk/example/sandbox/main.go)
- [Runtime Client example](https://github.com/openkruise/agents-api/blob/master/sdk/example/envd_client/main.go)
