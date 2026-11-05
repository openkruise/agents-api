# Runtime Java SDK (Runtime Client)

> **Dependency**: This package is not published to a public Maven repository. Download the project and build the JAR
> manually.

---

## Package Structure

```
runtime/
├── RuntimeClient.java            # Unified entry: create / newFromK8s
├── RuntimeConfig.java            # Builder-pattern config: domain / scheme / runtimeToken / ...
├── K8sHelper.java                # Extract runtimeToken from Sandbox CR annotation
├── EnvdMethods.java              # envd service and method name constants
├── commands/                     # Command execution
│   ├── Commands.java             # run / runBackground / kill / sendInput / list / connect
│   ├── CommandHandle.java        # Background process handle: waitForCompletion / kill / disconnect
│   ├── CommandResult.java        # Command result: stdout / stderr / exitCode
│   └── CommandExitException.java # Thrown on non-zero exit codes (carries the result)
├── filesystem/                   # Filesystem
│   ├── Filesystem.java           # listDir / read / write / makeDir / remove / watchDir / move
│   └── WatchHandle.java          # Directory watch handle: stop
├── codeinterpreter/              # Code interpreter
├── utils/                        # Utilities
│   ├── ConnectStreamReader.java  # Connect Protocol streaming response parser
│   └── MessageStream.java        # Streaming message interface (hasNext / next / close)
├── exceptions/                   # Exceptions
│   ├── SandboxException.java     # Sandbox runtime exception
│   └── K8sOperationException.java # K8s operation exception
└── envd/                         # Protobuf generated code
    ├── process/                  # envd Process service
    └── filesystem/               # envd Filesystem service
```

---

## Quick Start

### Option 1: Direct Connection

Use when you know the sandbox URL and token:

```java
import io.openkruise.agents.client.runtime.*;
import io.openkruise.agents.client.runtime.commands.CommandResult;

RuntimeConfig config = new RuntimeConfig.Builder()
    .runtimeUrl("http://localhost:49983")
    .runtimeToken("your-token")
    .build();

try (RuntimeClient client = RuntimeClient.create("sandbox-id", config)) {
    CommandResult res = client.commands.run("uname -a");
    System.out.println(res.getStdout());
}
```

### Option 2: K8s Auto-Discovery

When running in-cluster or with kubeconfig access, use `newFromK8s` to automatically resolve `sandboxID` and
`runtimeToken` from the Sandbox CR:

```java
RuntimeConfig config = new RuntimeConfig.Builder()
    .domain("sandbox-gateway.sandbox-system.svc:7788")
    .scheme("http")
    .build();

try (RuntimeClient client = RuntimeClient.newFromK8s("default", "your-sandbox-name", config)) {
    System.out.println("Runtime URL: " + client.getRuntimeURL());
    CommandResult res = client.commands.run("uname -a");
    System.out.println(res.getStdout());
}
```

**K8s Mode Notes:**

- `newFromK8s` queries the Sandbox CR and extracts `runtimeToken` from annotation
  `agents.kruise.io/runtime-access-token`
- `sandboxID` format is `namespace--name` (double-hyphen joined)
- Kubeconfig resolution order: `KUBECONFIG` env → `~/.kube/config` → in-cluster config

Full example: [K8sDirectConnectExample.java](../examples/K8sDirectConnectExample.java)

---

## 1. RuntimeClient

`RuntimeClient` is the unified entry point for the Runtime layer, providing command execution and filesystem operations.

### Creation

| Method                                                                                 | Description               |
|----------------------------------------------------------------------------------------|---------------------------|
| `RuntimeClient.create(String sandboxID, RuntimeConfig config)`                         | Direct creation           |
| `RuntimeClient.newFromK8s(String namespace, String sandboxName, RuntimeConfig config)` | Auto-discover from K8s CR |

### Fields

| Field             | Type              | Description              |
|-------------------|-------------------|--------------------------|
| `commands`        | `Commands`        | Command execution module |
| `files`           | `Filesystem`      | Filesystem module        |
| `codeInterpreter` | `CodeInterpreter` | Code interpreter module  |

### Methods

| Method            | Description                                                                    |
|-------------------|--------------------------------------------------------------------------------|
| `getSandboxID()`  | Returns sandbox ID                                                             |
| `getRuntimeURL()` | Returns runtime URL                                                            |
| `getConfig()`     | Returns RuntimeConfig                                                          |
| `close()`         | Close HTTP connection pool + release thread pool (supports try-with-resources) |

> `close()` only releases local HTTP resources; it does **not** terminate the remote sandbox.

---

## 2. Connection Configuration (RuntimeConfig)

The runtime client only needs `scheme` + `domain` to determine the envd address (`<scheme>://<domain>`); no Protocol
routing is involved.

### Builder Methods

| Method                          | Description                                                                        |
|---------------------------------|------------------------------------------------------------------------------------|
| `.domain(String)`               | envd domain, default `domain.app`                                                  |
| `.scheme(String)`               | URL scheme, default `http`                                                         |
| `.runtimeToken(String)`         | Runtime Token, written to `X-Access-Token` header                                  |
| `.runtimeUrl(String)`           | **Highest priority**: directly overrides URL; `getSandboxURL()` returns this value |
| `.apiKey(String)`               | API Key, written to `X-API-Key` header                                             |
| `.authHeader(String)`           | Override default Authorization header                                              |
| `.headers(Map<String, String>)` | Merge multiple custom headers                                                      |
| `.addHeader(String, String)`    | Add a single custom header                                                         |
| `.requestTimeoutMs(long)`       | Timeout for single requests (list/kill/sendInput); also limits how long Start/ConnectToProcess wait for a command to start. 0 disables; default 60000 |
| `.sandboxPort(int)`             | envd port, default 49983 (for `e2b-sandbox-port` header)                           |
| `.codeInterpreterPort(int)`     | Code interpreter port, default 49999                                               |
| `.urlBuilder(URLBuilder)`       | URL builder, supports E2B and Runtime modes                                        |
| `.httpClient(OkHttpClient)`     | Custom OkHttpClient for all HTTP requests (proxy, SSL, timeouts, etc.)             |

### Priority

`runtimeUrl` (explicit override) > `scheme` + `domain` assembly > Defaults

### Custom HTTP Client

You can provide a custom `OkHttpClient` to configure proxy, SSL certificates, timeouts, interceptors, etc.:

```java
import okhttp3.OkHttpClient;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

// Build custom client with proxy, SSL, timeouts, etc.
OkHttpClient customClient = new OkHttpClient.Builder()
    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.host", 8080)))
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(120, TimeUnit.SECONDS)
    .build();

RuntimeConfig config = new RuntimeConfig.Builder()
    .domain("sandbox-gateway.sandbox-system.svc:7788")
    .scheme("http")
    .httpClient(customClient)  // Use custom client
    .build();

try (RuntimeClient client = RuntimeClient.create("sandbox-id", config)) {
    CommandResult res = client.commands.run("uname -a");
    System.out.println(res.getStdout());
}
```

**Note**: When `httpClient` is provided, it is used directly for all HTTP requests. The streaming client is derived from it with `readTimeout=0` for long-running operations.

---

## 3. Command Execution (Commands)

Operate on processes inside the container via `client.commands`. Built on OkHttp + Connect Protocol; commands are
executed through `<shell> -l -c <cmd>` (default shell `/bin/bash`, configurable via `RunOptions.shell()`).

### Methods

| Method                                          | Description                                                          |
|-------------------------------------------------|----------------------------------------------------------------------|
| `run(String cmd)`                               | **Foreground**: start command and wait, returns `CommandResult`; throws `CommandExitException` on a non-zero exit, `SandboxException` on a truncated stream      |
| `run(String cmd, RunOptions options)`           | Foreground with options (same return value and exceptions)                                              |
| `runBackground(String cmd, RunOptions options)` | **Background**: returns `CommandHandle`, caller decides when to wait |
| `list()`                                        | List all running processes, returns `List<ProcessInfo>`              |
| `kill(int pid)`                                 | Send SIGKILL to the given PID                                        |
| `sendInput(int pid, String data)`               | Write data to stdin of the given process                             |
| `sendSignal(int pid, Signal signal)`            | Send a signal to the given process                                   |
| `closeStdin(int pid)`                           | Close stdin of the given process                                     |
| `connect(int pid)`                              | Reconnect to a running process, subscribe to its subsequent output   |
| `connect(int pid, ConnectOptions options)`      | Reconnect with per-call options (the same two limits as Start)        |

### RunOptions

```java
RunOptions opts = new RunOptions()
    .envs(Map.of("LANG", "C"))               // Environment variables
    .cwd("/tmp")                              // Working directory
    .user("nobody")                           // Run as this user (must exist in the image, must not contain ':'); applies to run/runBackground only; unset = default (root)
    .requestTimeoutMs(3000)                   // Time limit (ms) for waiting the command to start; disarmed once the command starts. 0 disables the wait (Go: no disable value). Unset = client default (60s)
    .timeoutMs(60000)                         // Whole-stream time limit (ms): connect handshake to end event. Zero or unset = no limit (the official E2B SDK defaults to 60s; set it explicitly if you want that limit). On timeout the stream fails; the process keeps running
    .onStdout(line -> System.out.print(line)) // Streaming stdout callback
    .onStderr(line -> System.err.print(line));// Streaming stderr callback
```

### ConnectOptions

`connect` supports the same two per-call limits as `start`, configured via
`ConnectOptions` in `connect(int pid, ConnectOptions)`:

```java
ConnectOptions opts = new ConnectOptions()
    .requestTimeoutMs(3000)                   // Time limit (ms) for waiting for the connect start event; disarmed once it arrives. 0 disables the wait (Go: no disable value). Unset = client default (60s)
    .timeoutMs(60000)                         // Whole connect-stream time limit (ms). Zero or unset = no limit. On timeout the stream fails; the process keeps running
```

### CommandResult

| Field      | Type     | Description                                                             |
|------------|----------|-------------------------------------------------------------------------|
| `stdout`   | `String` | Standard output                                                         |
| `stderr`   | `String` | Standard error                                                          |
| `exitCode` | `int`    | Exit code                                                               |
| `error`    | `String` | Server-side error message from the end event (e.g. a spawn failure); empty when the process exited normally |

### CommandHandle

Returned by `runBackground()` / `connect()`, implements `AutoCloseable`:

| Method                | Description                                     |
|-----------------------|-------------------------------------------------|
| `getPid()`            | Returns process PID                             |
| `isCompleted()`       | Whether the process has completed               |
| `waitForCompletion()` | Block until completion, returns `CommandResult`; throws `CommandExitException` (carrying the result) when the exit code is non-zero, `SandboxException` on a truncated stream |
| `kill()`              | Kill the process; throws `IllegalStateException` when envd reports it is already gone (no signal sent, the completion claim is rolled back) |
| `disconnect()`        | Stop reading output without killing the process; a blocked `waitForCompletion()` fails with an exception |
| `close()`             | Close stream and underlying HTTP resources      |

### Examples

```java
// Foreground execution
CommandResult res = client.commands.run("pwd");
System.out.println(res.getStdout());

// With options + streaming output
CommandResult res2 = client.commands.run("ls -la /tmp",
    new RunOptions().cwd("/tmp").onStdout(System.out::print));

// Background + manual kill — try-with-resources releases the stream even
// when kill() throws because the process is already gone
try (CommandHandle handle = client.commands.runBackground("sleep 60", new RunOptions())) {
    System.out.println("pid = " + handle.getPid());
    handle.kill();
}

// List processes
List<ProcessInfo> procs = client.commands.list();
for (ProcessInfo p : procs) {
    System.out.printf("PID: %d, Cmd: %s%n", p.getPid(), p.getCmd());
}
```

---

## 4. Filesystem (Filesystem)

Operate on files inside the container via `client.files`. Metadata operations use the envd Filesystem service; file
read/write uses the HTTP `/files` endpoint.

### Methods

**Directory Operations**

| Method                                 | Description                                     |
|----------------------------------------|-------------------------------------------------|
| `listDir(String path)`                 | List directory entries (depth=1)                |
| `listDir(String path, int depth)`      | List directory entries (specified depth)        |
| `makeDir(String path)`                 | Create directory recursively; `false` if exists |
| `move(String oldPath, String newPath)` | Rename/move a file or directory                 |

**File Information**

| Method                 | Description                                  |
|------------------------|----------------------------------------------|
| `exists(String path)`  | Check if path exists                         |
| `getInfo(String path)` | Get file/directory info, returns `EntryInfo` |

**File Read / Write**

| Method                                                | Description                                         |
|-------------------------------------------------------|-----------------------------------------------------|
| `read(String path)`                                   | Read file content (`byte[]`)                        |
| `read(String path, String user)`                      | Read file content (as specified user)               |
| `readText(String path)`                               | Read file content (`String`, UTF-8)                 |
| `readText(String path, String user)`                  | Read file content (as specified user, UTF-8)        |
| `readStream(String path)`                             | Stream file content (`InputStream`, must close)     |
| `readStream(String path, String user)`                | Stream file content (as specified user, must close) |
| `readTextStream(String path)`                         | Stream file text (`BufferedReader`, UTF-8, must close) |
| `readTextStream(String path, String user)`            | Stream file text (as specified user, must close)    |
| `write(String path, byte[] data)`                     | Write file (binary), returns `WriteInfo`            |
| `write(String path, byte[] data, String user)`        | Write file (as specified user), returns `WriteInfo` |
| `writeText(String path, String content)`              | Write file (text, UTF-8), returns `WriteInfo`       |
| `writeText(String path, String content, String user)` | Write file (as specified user), returns `WriteInfo` |

**Delete**

| Method                | Description                |
|-----------------------|----------------------------|
| `remove(String path)` | Delete a file or directory |

**Directory Watch**

| Method                                                                        | Description                         |
|-------------------------------------------------------------------------------|-------------------------------------|
| `watchDir(String path, Consumer<FilesystemEvent> onEvent)`                    | Watch directory change events       |
| `watchDir(String path, boolean recursive, Consumer<FilesystemEvent> onEvent)` | Watch directory changes (recursive) |

### WatchHandle

| Method        | Description                                       |
|---------------|---------------------------------------------------|
| `stop()`      | Stop watching and close underlying HTTP resources |
| `isStopped()` | Whether the watch has been stopped                |

### Examples

```java
// Directory operations
client.files.makeDir("/tmp/work");
List<EntryInfo> entries = client.files.listDir("/tmp");
client.files.move("/tmp/old.txt", "/tmp/new.txt");
client.files.remove("/tmp/work");

// File read/write
client.files.writeText("/tmp/hello.txt", "Hello, World!");
String content = client.files.readText("/tmp/hello.txt");

// Binary read/write
WriteInfo info = client.files.write("/tmp/data.bin", new byte[]{0x01, 0x02});
byte[] data = client.files.read("/tmp/data.bin");

// Stream large files (remember to close)
try (InputStream in = client.files.readStream("/tmp/large.log")) {
    byte[] buffer = new byte[4096];
    while (in.read(buffer) != -1) { /* process */ }
}
try (BufferedReader reader = client.files.readTextStream("/tmp/large.log")) {
    String line;
    while ((line = reader.readLine()) != null) { /* process */ }
}

// Directory watch
WatchHandle wh = client.files.watchDir("/tmp", true, event ->
    System.out.printf("Event: %s %s%n", event.getType(), event.getName()));
wh.stop();
```

---

## 5. Code Interpreter (CodeInterpreter)

Execute code in the sandbox via `client.codeInterpreter`. Supports Python, JavaScript, TypeScript, R, Java, Bash, and
more.

### Methods

| Method                                                       | Description                                               |
|--------------------------------------------------------------|-----------------------------------------------------------|
| `runCode(String code)`                                       | Execute Python code (default language)                    |
| `runCode(String code, String language)`                      | Execute code in specified language                        |
| `runCode(String code, String language, RunCodeOptions)`      | Execute code with options (cwd, env vars, etc.)           |
| `runCode(RunCodeRequest request)`                            | Execute code (full request object)                        |
| `runCodeStreaming(RunCodeRequest, Consumer<ExecutionEvent>)` | Streaming execution, event-by-event callback (low memory) |
| `createCodeContext(String cwd, String language)`             | Create code execution context (set cwd/language)          |
| `removeCodeContext(String contextId)`                        | Remove specified code execution context                   |
| `listCodeContexts()`                                         | List all code execution contexts                          |

### RunCodeOptions

```java
RunCodeOptions opts = new RunCodeOptions()
    .setCwd("/tmp")                              // Working directory
    .setEnvVars(Map.of("DEBUG", "true"))         // Environment variables
    .setTimeoutMs(30000L)                        // Timeout (milliseconds)
    .setContextId("context-id");                 // Use specified context (mutually exclusive with language)
```

### Context (Code Execution Context)

Context maintains an independent code execution environment. Each Context has its own working directory and language
environment.

| Field      | Type     | Description |
|------------|----------|-------------|
| `id`       | `String` | Context ID  |
| `language` | `String` | Language    |
| `cwd`      | `String` | Working dir |

**Note**: When using Context, the `language` parameter in `runCode()` is ignored (server requires `context_id` and
`language` to be mutually exclusive).

### Execution (Execution Result)

| Field            | Type             | Description                                 |
|------------------|------------------|---------------------------------------------|
| `results`        | `List<Result>`   | Execution results (text/html/image formats) |
| `logs`           | `Logs`           | Log output (stdout/stderr)                  |
| `error`          | `ExecutionError` | Execution error (if any)                    |
| `executionCount` | `Integer`        | Execution count                             |

### Result (Result Format)

Supports multiple output formats, similar to Jupyter notebook:

| Field        | Type                  | Description    |
|--------------|-----------------------|----------------|
| `text`       | `String`              | Plain text     |
| `html`       | `String`              | HTML output    |
| `markdown`   | `String`              | Markdown       |
| `png`        | `String` (base64)     | PNG image      |
| `jpeg`       | `String` (base64)     | JPEG image     |
| `svg`        | `String`              | SVG graphic    |
| `json`       | `Map<String, Object>` | JSON data      |
| `mainResult` | `boolean`             | Is main result |

### Examples

```java
// Execute Python code
Execution result = client.codeInterpreter.runCode("print('Hello from Python!')");
for (String line : result.getLogs().getStdout()) {
    System.out.println(line);
}

// Execute JavaScript code
Execution jsResult = client.codeInterpreter.runCode(
    "console.log('Hello from JS!');",
    RunCodeLanguage.JAVASCRIPT.getValue()
);

// Execute with options (environment variables)
RunCodeOptions opts = new RunCodeOptions()
    .setEnvVars(Map.of("API_KEY", "secret"));
Execution result2 = client.codeInterpreter.runCode(
    "import os; print(os.environ.get('API_KEY'))",
    RunCodeLanguage.PYTHON.getValue(),
    opts
);

// Use Context to set working directory
Context ctx = client.codeInterpreter.createCodeContext("/tmp", "python");
System.out.println("Context created: " + ctx);

RunCodeOptions ctxOpts = new RunCodeOptions()
    .setContextId(ctx.getId());
Execution ctxResult = client.codeInterpreter.runCode(
    "import os; print('CWD:', os.getcwd())",
    RunCodeLanguage.PYTHON.getValue(),
    ctxOpts
);

// Clean up Context
client.codeInterpreter.removeCodeContext(ctx.getId());

// List all Contexts
List<Context> contexts = client.codeInterpreter.listCodeContexts();
for (Context c : contexts) {
    System.out.println(c);
}

// Get main result text
String mainText = result2.getText();
System.out.println("Main result: " + mainText);

// Streaming execution (event-by-event processing)
RunCodeRequest request = new RunCodeRequest("for i in range(5): print(i)", "python");
client.codeInterpreter.runCodeStreaming(request, event -> {
    if (event instanceof StdoutEvent) {
        System.out.print(((StdoutEvent) event).getText());
    } else if (event instanceof ErrorEvent) {
        System.err.println("Error: " + ((ErrorEvent) event).getError());
    }
});
```

---

## 6. Exception Hierarchy

| Exception Class         | Description                                                                    |
|-------------------------|--------------------------------------------------------------------------------|
| `SandboxException`      | Sandbox runtime exception (command execution failure, stream read error, etc.) |
| `CommandExitException`  | Command finished with a non-zero exit code; carries stdout/stderr/exit code and the end-event `error` |
| `K8sOperationException` | K8s operation exception (CR query failure, token extraction failure, etc.)     |

---

## 7. Resource Management

`RuntimeClient` implements `AutoCloseable`; `close()` releases:

- OkHttpClient **Dispatcher** thread pool
- OkHttpClient **ConnectionPool** connection pool

Recommended: use try-with-resources:

```java
try (RuntimeClient client = RuntimeClient.create(sandboxID, config)) {
    // operations...
} // HTTP resources released automatically
```

> `close()` only releases local HTTP resources; it does **not** terminate the remote sandbox.
