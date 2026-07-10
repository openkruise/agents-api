# Runtime Java Client

Java runtime client for OpenKruise Agents, providing data plane operations (command execution, filesystem, code
interpreter).

## Installation

```xml

<dependency>
    <groupId>io.github.openkruise</groupId>
    <artifactId>agents-client-runtime</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Overview

| Class             | Description                                                                         |
|-------------------|-------------------------------------------------------------------------------------|
| `RuntimeConfig`   | Runtime connection configuration with Builder pattern, supports custom OkHttpClient |
| `RuntimeClient`   | Unified entry point: commands, files, codeInterpreter                               |
| `Commands`        | Command execution within the sandbox                                                |
| `Filesystem`      | File operations within the sandbox                                                  |
| `CodeInterpreter` | Code execution within the sandbox                                                   |

## Quick Start

### K8s Direct Connect

Connect directly to a sandbox in a K8s cluster, bypassing the E2B control plane:

```java
import io.openkruise.agents.client.runtime.*;

RuntimeConfig config = new RuntimeConfig.Builder()
    .domain("sandbox-gateway.sandbox-system.svc:7788")
    .scheme("http")
    .build();

try (RuntimeClient client = RuntimeClient.newFromK8s("default", "your-sandbox-name", config)) {
    // Execute command
    CommandResult result = client.commands.run("uname -a");
    System.out.println(result.getStdout());

    // File operations
    client.files.writeText("/tmp/test.txt", "Hello!");
    String content = client.files.readText("/tmp/test.txt");

    // Code interpreter
    Execution exec = client.codeInterpreter.runCode("print('Hello from Python!')");
}
```

## Documentation

- [English Documentation](src/main/java/io/openkruise/agents/client/runtime/README.md) — Full API reference with
  examples
- [中文文档](src/main/java/io/openkruise/agents/client/runtime/README_zh-CH.md) — 完整 API 参考及示例

## Examples

- [K8s Direct Connect](src/main/java/io/openkruise/agents/client/examples/K8sDirectConnectExample.java)