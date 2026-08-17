# Runtime Go SDK (Runtime Client)

## Installation

Add the `agents-api` dependency to your `go.mod`: [View Releases](https://github.com/openkruise/agents-api/releases)

```
require github.com/openkruise/agents-api <tag>
```

| Package     | Import Path                                | Description                                                                              |
|-------------|--------------------------------------------|------------------------------------------------------------------------------------------|
| **runtime** | `github.com/openkruise/agents-api/runtime` | **Runtime Client**: Directly operate the envd service inside a running sandbox container |

---

## Package Structure

```
runtime/
├── client.go                     #   Client struct: New / NewWithConfig
├── k8s.go                        #   NewFromK8s: auto-resolve sandboxID and runtimeToken from K8s
├── config.go                     #   Config & Options: Domain / Scheme / RuntimeToken / ...
├── commands.go                   #   Commands: Run / Start / Kill / SendStdin / List / ConnectToProcess
├── command_handle.go             #   CommandHandle: Wait / Disconnect / Kill
├── filesystem.go                 #   Filesystem: List / Exists / GetInfo / MakeDir / Rename / Remove / Read / Write
└── envd/                         #   protobuf generated code
    ├── process/                  #   envd Process gRPC
    │   ├── process.pb.go
    │   └── processconnect/
    └── filesystem/               #   envd Filesystem gRPC
        ├── filesystem.pb.go
        └── filesystemconnect/
```

---

## Quick Start

When running in-cluster or with kubeconfig access, use `NewFromK8s` to automatically resolve `sandboxID` and
`runtimeToken` from the Sandbox CR:

```go
package main

import (
	"context"
	"fmt"

	"github.com/openkruise/agents-api/runtime"
)

func main() {
	ctx := context.Background()

	// domain is the sandbox gateway address.
	// In-cluster: use K8s Service DNS, e.g. "sandbox-gateway.sandbox-system.svc:7788"
	// Local dev: use port-forward address, e.g. "127.0.0.1:7788"
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

- `NewFromK8s` queries the Sandbox CR and extracts `runtimeToken` from annotation
  `agents.kruise.io/runtime-access-token`
- `sandboxID` format is `namespace--name` (double-dash separator)
- kubeconfig resolution order: `KUBECONFIG` env var → `~/.kube/config` → in-cluster config

Full
example: [Runtime Client Example](https://github.com/openkruise/agents-api/blob/master/runtime/examples/runtime-example/main.go)

---

## Connection Configuration

The runtime client **does not involve Protocol** — only `Scheme` + `Domain` are needed to determine the envd address (
`<scheme>://<domain>`).

### Option List

| Option                                | Description                                    |
|---------------------------------------|------------------------------------------------|
| `WithDomain(domain string)`           | envd domain, defaults to `your.domain.com`     |
| `WithScheme(scheme string)`           | URL scheme, defaults to `http`                 |
| `WithRuntimeToken(token string)`      | Runtime token, sent as `X-Access-Token` header |
| `WithRuntimePort(port int)`           | Runtime port, defaults to `49983`              |
| `WithAPIKey(apiKey string)`           | Optional API Key                               |
| `WithAuthHeader(header string)`       | Override default Authorization header          |
| `WithSandboxBaseURL(url string)`      | Completely override URL assembly               |
| `WithHeader(key, value string)`       | Add a single custom header                     |
| `WithHeaders(headers map)`            | Merge multiple custom headers                  |
| `WithRequestTimeout(d time.Duration)` | HTTP timeout, defaults to 60s                  |
| `WithConfig(cfg *Config)`             | Pass a pre-built Config to replace defaults    |

---

## Command Execution (Commands)

Operate in-container processes via `c.Commands`. Uses the envd `Process` gRPC service under the hood.

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
res, err := c.Commands.Run(ctx, "ls -la /tmp", runtime.RunOpts{
Cwd:      "/tmp",
Envs:     map[string]string{"LANG": "C"},
OnStdout: func (line string) { fmt.Print(line) },
})

// Background start + manual Kill
h, _ := c.Commands.Start(ctx, "sleep 60")
fmt.Println("pid =", h.Pid())
h.Kill()
```

---

## Filesystem

Operate in-container files via `c.Files`. Metadata operations use envd Filesystem gRPC; file content read/write uses
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

// Stream large files (caller must Close)
rc, _ := c.Files.ReadStream(ctx, "/tmp/large.log")
defer rc.Close()
io.Copy(os.Stdout, rc)
```
