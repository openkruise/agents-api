# E2B Java SDK (Management Client)

> **Dependency**: This package is not published to a public Maven repository. Download the project and build the JAR
> manually.

---

## Quick Start

```java
import io.openkruise.agents.client.e2b.*;

// Reads E2B_API_KEY and E2B_DOMAIN from environment variables
ConnectionConfig config = new ConnectionConfig.Builder().build();

SandboxApi api = new SandboxApi(config);

// Create → Use → Close connection + Explicitly kill sandbox
Sandbox sandbox = api.create("code-interpreter");
try (sandbox) {
    sandbox.commands.run("echo hello");
    sandbox.files.writeText("/tmp/demo.txt", "Hello!");
} finally {
    api.kill(sandbox.getSandboxID());  // Explicitly kill sandbox
}
```

**Environment Variables Setup**:

```bash
export E2B_API_KEY="your-api-key"
export E2B_DOMAIN="your.domain.com"
```

Full
examples: [Lifecycle Management](../examples/e2b/SandboxApiManagerExample.java) | [Commands](../examples/e2b/SandboxCommandsExample.java) | [Files](../examples/e2b/SandboxFilesExample.java)

---

## 1. Sandbox Lifecycle Management (SandboxApi)

`SandboxApi` provides full lifecycle management for Sandboxes: create, connect, query, pause, resume, and kill.

### Initialization

```java
// Reads E2B_API_KEY and E2B_DOMAIN from environment variables
ConnectionConfig config = new ConnectionConfig.Builder().build();

SandboxApi api = new SandboxApi(config);
```

### API Methods

| Method                                                                             | Description                                           |
|------------------------------------------------------------------------------------|-------------------------------------------------------|
| `create(String template)`                                                          | Create sandbox from template                          |
| `create(NewSandbox body)`                                                          | Create sandbox (full parameters)                      |
| `connect(String sandboxID)`                                                        | Connect to an existing sandbox                        |
| `connect(String sandboxID, int timeout)`                                           | Connect to an existing sandbox (with timeout)         |
| `list()`                                                                           | List all running sandboxes                            |
| `list(String metadata)`                                                            | List sandboxes filtered by metadata                   |
| `list(String metadata, List<SandboxState> state, String nextToken, Integer limit)` | Paginated list with state filter                      |
| `getInfo(String sandboxID)`                                                        | Get details; 404 → `SandboxNotFoundException`         |
| `kill(String sandboxID)`                                                           | Kill sandbox; `true` on success, `false` if not found |
| `pause(String sandboxID)`                                                          | Pause sandbox; 409 treated as already paused          |
| `setTimeout(String sandboxID, int timeout)`                                        | Update timeout (seconds)                              |

### Lifecycle Example

```java
// ---- Create ----
Sandbox sandbox = api.create("code-interpreter");
String id = sandbox.getSandboxID();

// Full-parameter creation
Sandbox sandbox2 = api.create(new NewSandbox()
    .templateID("code-interpreter")
    .timeout(600)
    .envVars(Map.of("FOO", "1")));

// ---- Query ----
SandboxInfo info = api.getInfo(id);
List<SandboxInfo> all = api.list();

// ---- Connect to existing sandbox ----
Sandbox reconnected = api.connect(id);

// ---- Pause / Set timeout ----
api.pause(id);
api.setTimeout(id, 600);

// ---- Kill ----
api.kill(id);
```

### Sandbox Instance

`SandboxApi.create()` / `connect()` returns a `Sandbox` instance, which is the entry point for data-plane operations:

| Method / Field       | Description                                    |
|----------------------|------------------------------------------------|
| `sandboxID`          | Sandbox ID                                     |
| `commands`           | Command execution module (`Commands`)          |
| `files`              | Filesystem module (`Filesystem`)               |
| `codeInterpreter`    | Code interpreter module (`CodeInterpreter`)    |
| `getSandboxURL()`    | Sandbox envd URL                               |
| `getConfig()`        | Connection config                              |
| `getRuntimeClient()` | Underlying `RuntimeClient`                     |
| `close()`            | Close connection (supports try-with-resources) |

---

## 2. Command Execution (Commands)

Operate on processes inside the container via `sandbox.commands`. Built on OkHttp + Connect Protocol; commands are
executed through `/bin/bash -l -c <cmd>`.

### Methods

| Method                                          | Description                                                          |
|-------------------------------------------------|----------------------------------------------------------------------|
| `run(String cmd)`                               | **Foreground**: start command and wait, returns `CommandResult`      |
| `run(String cmd, RunOptions options)`           | Foreground with options                                              |
| `runBackground(String cmd, RunOptions options)` | **Background**: returns `CommandHandle`, caller decides when to wait |
| `list()`                                        | List all running processes, returns `List<ProcessInfo>`              |
| `kill(int pid)`                                 | Send SIGKILL to the given PID                                        |
| `sendInput(int pid, String data)`               | Write data to stdin of the given process                             |
| `sendSignal(int pid, Signal signal)`            | Send a signal to the given process                                   |
| `closeStdin(int pid)`                           | Close stdin of the given process                                     |
| `connect(int pid)`                              | Reconnect to a running process, subscribe to its subsequent output   |

### RunOptions

```java
RunOptions opts = new RunOptions()
    .envs(Map.of("LANG", "C"))               // Environment variables
    .cwd("/tmp")                              // Working directory
    .onStdout(line -> System.out.print(line)) // Streaming stdout callback
    .onStderr(line -> System.err.print(line));// Streaming stderr callback
```

### CommandResult

| Field      | Type     | Description     |
|------------|----------|-----------------|
| `stdout`   | `String` | Standard output |
| `stderr`   | `String` | Standard error  |
| `exitCode` | `int`    | Exit code       |

### CommandHandle

Returned by `runBackground()` / `connect()`, implements `AutoCloseable`:

| Method                | Description                                     |
|-----------------------|-------------------------------------------------|
| `getPid()`            | Returns process PID                             |
| `isCompleted()`       | Whether the process has completed               |
| `waitForCompletion()` | Block until completion, returns `CommandResult` |
| `kill()`              | Kill the process                                |
| `close()`             | Close stream and underlying resources           |

### Examples

```java
// Foreground execution
CommandResult res = sandbox.commands.run("pwd");
System.out.println(res.getStdout());

// With options + streaming output
CommandResult res2 = sandbox.commands.run("ls -la /tmp",
    new RunOptions().cwd("/tmp").onStdout(System.out::print));

// Background + manual kill
CommandHandle handle = sandbox.commands.runBackground("sleep 60", new RunOptions());
System.out.println("pid = " + handle.getPid());
handle.kill();
handle.close();

// List processes
List<ProcessInfo> procs = sandbox.commands.list();
for (ProcessInfo p : procs) {
    System.out.printf("PID: %d, Cmd: %s%n", p.getPid(), p.getCmd());
}
```

---

## 3. Filesystem (Filesystem)

Operate on files inside the container via `sandbox.files`. Metadata operations use the envd Filesystem service; file
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
sandbox.files.makeDir("/tmp/work");
List<EntryInfo> entries = sandbox.files.listDir("/tmp");
sandbox.files.move("/tmp/old.txt", "/tmp/new.txt");
sandbox.files.remove("/tmp/work");

// File read/write
sandbox.files.writeText("/tmp/hello.txt", "Hello, World!");
String content = sandbox.files.readText("/tmp/hello.txt");

// Binary read/write
WriteInfo info = sandbox.files.write("/tmp/data.bin", new byte[]{0x01, 0x02});
byte[] data = sandbox.files.read("/tmp/data.bin");

// Directory watch
WatchHandle wh = sandbox.files.watchDir("/tmp", true, event ->
    System.out.printf("Event: %s %s%n", event.getType(), event.getName()));
wh.stop();
```

---

## 4. Code Interpreter (CodeInterpreter)

Execute code in the sandbox via `sandbox.codeInterpreter`. Supports Python, JavaScript, TypeScript, R, Java, Bash, and
more.

### Methods

| Method                                                  | Description                                      |
|---------------------------------------------------------|--------------------------------------------------|
| `runCode(String code)`                                  | Execute Python code (default language)           |
| `runCode(String code, String language)`                 | Execute code in specified language               |
| `runCode(String code, String language, RunCodeOptions)` | Execute code with options (cwd, env vars, etc.)  |
| `runCode(RunCodeRequest request)`                       | Execute code (full request object)               |
| `createCodeContext(String cwd, String language)`        | Create code execution context (set cwd/language) |
| `removeCodeContext(String contextId)`                   | Remove specified code execution context          |
| `listCodeContexts()`                                    | List all code execution contexts                 |

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
Execution result = sandbox.codeInterpreter.runCode("print('Hello from Python!')");
for (String line : result.getLogs().getStdout()) {
    System.out.println(line);
}

// Execute JavaScript code
Execution jsResult = sandbox.codeInterpreter.runCode(
    "console.log('Hello from JS!');",
    RunCodeLanguage.JAVASCRIPT.getValue()
);

// Execute with options (environment variables)
RunCodeOptions opts = new RunCodeOptions()
    .setEnvVars(Map.of("API_KEY", "secret"));
Execution result2 = sandbox.codeInterpreter.runCode(
    "import os; print(os.environ.get('API_KEY'))",
    RunCodeLanguage.PYTHON.getValue(),
    opts
);

// Use Context to set working directory
Context ctx = sandbox.codeInterpreter.createCodeContext("/tmp", "python");
System.out.println("Context created: " + ctx);

RunCodeOptions ctxOpts = new RunCodeOptions()
    .setContextId(ctx.getId());
Execution ctxResult = sandbox.codeInterpreter.runCode(
    "import os; print('CWD:', os.getcwd())",
    RunCodeLanguage.PYTHON.getValue(),
    ctxOpts
);

// Clean up Context
sandbox.codeInterpreter.removeCodeContext(ctx.getId());

// List all Contexts
List<Context> contexts = sandbox.codeInterpreter.listCodeContexts();
for (Context c : contexts) {
    System.out.println(c);
}

// Get main result text
String mainText = result2.getText();
System.out.println("Main result: " + mainText);

// Streaming execution (event-by-event processing)
RunCodeRequest request = new RunCodeRequest("for i in range(5): print(i)", "python");
sandbox.codeInterpreter.runCodeStreaming(request, event -> {
    if (event instanceof StdoutEvent) {
        System.out.print(((StdoutEvent) event).getText());
    } else if (event instanceof ErrorEvent) {
        System.err.println("Error: " + ((ErrorEvent) event).getError());
    }
});
```

Full example: [SandboxCodeInterpreterExample.java](../examples/e2b/SandboxCodeInterpreterExample.java)

---

## 5. SandboxInfo (Immutable)

`SandboxInfo` is the return type of `list()` / `getInfo()`, an **immutable object** (all fields `final`, no setters,
`metadata` and `volumeMounts` are unmodifiable collections).

| Field                 | Type                       | Description                           |
|-----------------------|----------------------------|---------------------------------------|
| `sandboxID`           | `String`                   | Sandbox ID                            |
| `templateID`          | `String`                   | Template ID                           |
| `alias`               | `String`                   | Alias                                 |
| `clientID`            | `String`                   | Client ID                             |
| `startedAt`           | `OffsetDateTime`           | Start time                            |
| `endAt`               | `OffsetDateTime`           | End time                              |
| `cpuCount`            | `Integer`                  | CPU count                             |
| `memoryMB`            | `Integer`                  | Memory (MB)                           |
| `diskSizeMB`          | `Integer`                  | Disk (MB)                             |
| `envdVersion`         | `String`                   | envd version                          |
| `metadata`            | `Map<String, String>`      | Metadata (unmodifiable)               |
| `state`               | `String`                   | State                                 |
| `volumeMounts`        | `List<SandboxVolumeMount>` | Volume mounts (unmodifiable)          |
| `envdAccessToken`     | `String`                   | envd access token (detail only)       |
| `domain`              | `String`                   | Domain (detail only)                  |
| `allowInternetAccess` | `Boolean`                  | Internet access allowed (detail only) |
| `network`             | `SandboxNetworkConfig`     | Network config (detail only)          |
| `lifecycle`           | `SandboxLifecycle`         | Lifecycle (detail only)               |

> `list()` returns `SandboxInfo` with only basic fields; detail-exclusive fields are `null`.

---

## 6. Connection Configuration (ConnectionConfig)

### Scheme & Protocol

Connection behavior is determined by two orthogonal dimensions: **Scheme** and **Protocol**, which shape the URL format.

#### Protocol (Routing Protocol)

| Value                | API URL                          | Sandbox URL                                     |
|----------------------|----------------------------------|-------------------------------------------------|
| **Native (default)** | `https://api.<domain>`           | `https://<port>-<sandboxID>.<domain>`           |
| **Private**          | `<scheme>://<domain>/kruise/api` | `<scheme>://<domain>/kruise/<sandboxID>/<port>` |

- **Native**: Subdomain-based routing, for native public deployments
- **Private**: Path-prefix-based routing through a unified gateway, for private deployments or port-forwarding scenarios

### Builder Methods

| Method                          | Description                                                    |
|---------------------------------|----------------------------------------------------------------|
| `.apiKey(String)`               | API Key, written to `X-API-Key` header                         |
| `.accessToken(String)`          | Access Token, written to `X-Access-Token` header               |
| `.domain(String)`               | Domain, default `your.domain.com`                              |
| `.scheme(String)`               | URL scheme, default `https`                                    |
| `.protocol(Protocol)`           | Routing protocol, default `Protocol.NATIVE`                    |
| `.apiURL(String)`               | **Highest priority**: directly overrides API base URL          |
| `.sandboxBaseURL(String)`       | **Highest priority**: directly overrides sandbox envd base URL |
| `.requestTimeoutMs(long)`       | HTTP request timeout (ms), default 60000                       |
| `.port(int)`                    | envd port, default 49983                                       |
| `.codeInterpreterPort(int)`     | Code interpreter port, default 49999                           |
| `.debug(boolean)`               | Debug mode; kill/setTimeout skip actual calls                  |
| `.headers(Map<String, String>)` | Custom request headers                                         |
| `.addHeader(String, String)`    | Add a single custom header                                     |

### Priority

`apiURL` / `sandboxBaseURL` (explicit override) > `protocol` + `domain` assembly > Environment variables (`E2B_API_KEY`,
`E2B_DOMAIN`) > Defaults

### Environment Variables

The Builder constructor automatically reads these environment variables as defaults, which can then be overridden by
Builder methods:

| Variable      | Description     |
|---------------|-----------------|
| `E2B_API_KEY` | Default API Key |
| `E2B_DOMAIN`  | Default domain  |

### Proxy Configuration (ProxyConfig)

Supports accessing E2B services through an HTTP proxy, suitable for internal network environments or scenarios requiring
traffic forwarding. Also supports custom SSL/TLS configuration.

#### Builder Methods

| Method                                | Description                              |
|---------------------------------------|------------------------------------------|
| `.proxy(Proxy)`                       | Set Java `Proxy` object                  |
| `.proxy(String host, int port)`       | Shortcut: create HTTP proxy by host/port |
| `.sslContext(SSLContext)`             | Custom SSL context                       |
| `.trustManager(X509TrustManager)`     | Custom certificate trust manager         |
| `.hostnameVerifier(HostnameVerifier)` | Custom hostname verifier                 |

#### Basic Usage

```java
// 1. Build ProxyConfig
ProxyConfig proxyConfig = new ProxyConfig.Builder()
    .proxy("proxy.example.com", 8080)
    .build();

// 2. Set proxy in ConnectionConfig
ConnectionConfig config = new ConnectionConfig.Builder()
    .apiKey("your-api-key")
    .domain("your.domain.com")
    .proxyConfig(proxyConfig)
    .build();

SandboxApi api = new SandboxApi(config);
Sandbox sandbox = api.create("code-interpreter");
```

#### Proxy Configuration with SSL/TLS

For development/testing environments that need to trust self-signed certificates or skip certificate verification:

```java
import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

// 1. Create a TrustManager that trusts all certificates (for dev/test only)
TrustManager[] trustAll = new TrustManager[]{
    new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] c, String a) {}
        @Override
        public void checkServerTrusted(X509Certificate[] c, String a) {}
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
};

// 2. Initialize SSLContext
SSLContext sslCtx = SSLContext.getInstance("TLS");
sslCtx.init(null, trustAll, new SecureRandom());

// 3. Build ProxyConfig
ProxyConfig proxyConfig = new ProxyConfig.Builder()
    .proxy(new Proxy(Proxy.Type.HTTP,
        new InetSocketAddress("proxy.example.com", 8080)))
    .sslContext(sslCtx)
    .trustManager((X509TrustManager) trustAll[0])
    .hostnameVerifier((hostname, session) -> true)
    .build();

// 4. Set in ConnectionConfig
ConnectionConfig config = new ConnectionConfig.Builder()
    .apiKey("your-api-key")
    .domain("your.domain.com")
    .proxyConfig(proxyConfig)
    .build();
```

> **Note**: Setting `proxyConfig(...)` automatically enables proxy. Proxy configuration applies to both control plane (
> API calls) and data plane (Runtime connections).

---

## 7. K8s Direct Connect Mode

Connect directly to a sandbox in a K8s cluster, bypassing the E2B control plane:

```java
import io.openkruise.agents.client.runtime.*;

RuntimeConfig config = new RuntimeConfig.Builder()
    .runtimeUrl("http://localhost:49983")
    .runtimeToken("your-token")
    .build();

RuntimeClient client = RuntimeClient.create("sandbox-id", config);

// Commands and file operations
CommandResult res = client.commands.run("echo hello");
client.files.writeText("/tmp/test.txt", "Hello!");

client.close(); // Close connection + release thread pool
```

See Runtime layer docs: [README](../runtime/README.md) | [Chinese Docs](../runtime/README_zh-CH.md)
