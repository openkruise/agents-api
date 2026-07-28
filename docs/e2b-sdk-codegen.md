# E2B SDK Code Generation Guide

This document describes how to generate Java and Go client code from E2B prototype specifications, covering both the
Sandbox API (OpenAPI) and envd gRPC services (Protobuf).

## Prerequisites

- [Install `openapi-generator`](https://openapi-generator.tech/docs/installation)
- [Install `buf`](https://docs.buf.build/installation)
- Install Git CLI to clone the E2B specification repository locally; all generation operations are executed in the
  corresponding local directories

> 💡 **Tip**: All code generation steps below are executed locally. You need to enter the corresponding directory before
> running generation commands.

## Prototype Specification Sources

| Type        | Specification Address                                                                                     | Description                      |
|-------------|-----------------------------------------------------------------------------------------------------------|----------------------------------|
| Sandbox API | [`e2b-dev/E2B/spec/openapi.yml`](https://github.com/e2b-dev/E2B/blob/main/spec/openapi.yml)               | OpenAPI 3.0 specification        |
| envd gRPC   | [`e2b-dev/E2B/spec/envd`](https://github.com/e2b-dev/E2B/tree/main/spec/envd)                             | Protobuf specification directory |

## Java Code Generation

### Sandbox API Code Generation

#### 1. Clone the E2B Repository

```bash
git clone https://github.com/e2b-dev/E2B.git
cd E2B
```

#### 2. Generate Java Client

```bash
openapi-generator generate \
  -i ./spec/openapi.yml \
  -g java \
  --library okhttp-gson \
  -o ./api-client \
  --additional-properties=\
artifactId=e2b-api-client,\
groupId=io.openkruise.agents.client.e2b,\
apiPackage=io.openkruise.agents.client.e2b.api,\
modelPackage=io.openkruise.agents.client.e2b.api.models,\
invokerPackage=io.openkruise.agents.client.e2b.api.invoker,\
dateLibrary=java8,\
disallowAdditionalPropertiesIfNotPresent=false
```

#### 3. Update to Project

Copy the generated code to the corresponding directory in the agents-api repository:

```bash
# Source path: ./api-client/src/main/java/io/openkruise/agents/client/e2b/api
# Target path: agents-api/e2b/java/src/main/java/io/openkruise/agents/client/e2b/api
```

📦 **Target repository path**: [`e2b/java/src/main/java/io/openkruise/agents/client/e2b/api`](../e2b/java/src/main/java/io/openkruise/agents/client/e2b/api)

### envd gRPC Code Generation

#### 1. Clone the E2B Repository

```bash
# If not already cloned
git clone https://github.com/e2b-dev/E2B.git
cd spec/envd
```

#### 2. Configure buf Generation Rules

Create a `buf.gen.java.yaml` file in the `spec/envd` directory:

```yaml
version: v1
plugins:
  - plugin: java
    out: ./java/src/main/java

  - plugin: grpc-java
    out: ./java/src/main/java

managed:
  enabled: true
  optimize_for: SPEED
  java_package_prefix:
    default: io.openkruise.agents.client.runtime.envd
```

#### 3. Generate Java gRPC Code

```bash
buf generate --template buf.gen.java.yaml
```

#### 4. Update to Project

Copy the generated code to the corresponding directory in the agents-api repository:

```bash
# Source path: ./java/src/main/java/io/openkruise/agents/client/runtime/envd
# Target path: agents-api/runtime/java/src/main/java/io/openkruise/agents/client/runtime/envd
```

📦 **Target repository path**: [`runtime/java/src/main/java/io/openkruise/agents/client/runtime/envd`](../runtime/java/src/main/java/io/openkruise/agents/client/runtime/envd)

### Code Synchronization Notes

> ⚠️ **Important**: Since the Java SDK has not yet been published to Maven Central, the `e2b/java` module directly
> depends on the envd code in `runtime/java`.

**Synchronization Rules**:

When `runtime/java/.../runtime` related code is modified, it must be synchronized to the corresponding directory in
`e2b/java/.../runtime`:

```
runtime/java/src/main/java/io/openkruise/agents/client/runtime
    ↓ Sync to
e2b/java/src/main/java/io/openkruise/agents/client/e2b/runtime
```

**Reasons**:

- The `e2b/java` module depends on the runtime code in `runtime/java`
- Before Maven publication, both modules need to maintain code consistency to avoid compilation errors or runtime
  exceptions

> 💡 **Future Optimization**: Once the runtime package is published to Maven Central, you only need to add the runtime
> dependency in `e2b/java`'s `pom.xml`, and manual code synchronization will no longer be required.

## Go Code Generation

### Sandbox API Code Generation

#### 1. Enter the Specification Directory

```bash
# If not already cloned
git clone https://github.com/e2b-dev/E2B.git
cd E2B
```

#### 2. Modify OpenAPI Specification

> ⚠️ **Note**: The Go code generated from the original OpenAPI specification has naming conflicts. The following field
> names in `spec/openapi.yml` need to be modified first:
> - `NewSandbox` → `CreateSandboxRequest`
> - `NewTeamAPIKey` → `CreateTeamAPIKeyRequest`
> - `NewVolume` → `CreateVolumeRequest`

#### 3. Generate Go Client

Execute in the `E2B` directory:

```bash
openapi-generator generate \
  -i ./spec/openapi.yml \
  -g go \
  -o ./api \
  --additional-properties=\
packageName=client,\
withGoMod=false,\
generateInterfaces=true,\
contextApi=true,\
returnError=true,\
enumClassPrefix=true
```

#### 4. Update to Project

Copy the generated code to the corresponding directory in the agents-api repository:

```bash
# Source path: ./api
# Target path: agents-api/e2b/api
```

📦 **Target repository path**: [`e2b/api`](../e2b/api)

### envd gRPC Code Generation

#### 1. Enter the Specification Directory

```bash
# If not already cloned
git clone https://github.com/e2b-dev/E2B.git
cd E2B/spec/envd
```

#### 2. Configure buf Generation Rules

Create a `buf.gen.go.yaml` file in the `spec/envd` directory:

```yaml
version: v1
plugins:
  - plugin: go
    out: ./go
    opt: paths=source_relative
  - plugin: connect-go
    out: ./go
    opt: paths=source_relative

managed:
  enabled: true
  optimize_for: SPEED
  go_package_prefix:
    default: github.com/openkruise/agents-api/runtime/envd
```

#### 3. Generate Go gRPC Code

Execute in the `E2B/spec/envd` directory:

```bash
buf generate --template buf.gen.go.yaml
```

#### 4. Update to Project

Copy the generated code to the corresponding directory in the agents-api repository:

```bash
# Source path: ./go/envd
# Target path: agents-api/runtime/envd
```

📦 **Target repository path**: [`runtime/envd`](../runtime/envd)
