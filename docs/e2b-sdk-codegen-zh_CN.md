# E2B SDK 代码生成指南

本文档说明如何从 E2B 原型规范生成 Java 和 Go 客户端代码，包括 Sandbox API（OpenAPI）和 envd gRPC 服务（Protobuf）两部分。

## 前置条件

- [安装 `openapi-generator`](https://openapi-generator.tech/docs/installation)
- [安装 `buf`](https://docs.buf.build/installation)
- 安装 Git 命令行工具，用于将 E2B 规范仓库 clone 到本地，后续生成操作均在本地对应目录下执行

> 💡 **提示**：以下所有代码生成步骤均在本地执行，需要先进入对应的目录再运行生成命令。

## 原型规范来源

| 类型          | 规范地址                                                                                     | 说明             |
|-------------|------------------------------------------------------------------------------------------|----------------|
| Sandbox API | [`e2b-dev/E2B/spec/openapi.yml`](https://github.com/e2b-dev/E2B/blob/main/spec/openapi.yml) | OpenAPI 3.0 规范 |
| envd gRPC   | [`e2b-dev/E2B/spec/envd`](https://github.com/e2b-dev/E2B/tree/main/spec/envd)              | Protobuf 规范目录  |

## Java 代码生成

### Sandbox API 代码生成

#### 1. 克隆 E2B 代码仓库

```bash
git clone https://github.com/e2b-dev/E2B.git
cd E2B
```

#### 2. 生成 Java 客户端

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

#### 3. 更新到项目

将生成的代码复制到 agents-api 仓库的对应目录：

```bash
# 源路径：./api-client/src/main/java/io/openkruise/agents/client/e2b/api
# 目标路径：agents-api/e2b/java/src/main/java/io/openkruise/agents/client/e2b/api
```

📦 **目标仓库路径**：[`e2b/java/src/main/java/io/openkruise/agents/client/e2b/api`](../e2b/java/src/main/java/io/openkruise/agents/client/e2b/api)

### envd gRPC 代码生成

#### 1. 克隆 E2B 代码仓库

```bash
# 如果尚未克隆
git clone https://github.com/e2b-dev/E2B.git
cd spec/envd
```

#### 2. 配置 buf 生成规则

在 `spec/envd` 目录下创建 `buf.gen.java.yaml` 文件：

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

#### 3. 生成 Java gRPC 代码

```bash
buf generate --template buf.gen.java.yaml
```

#### 4. 更新到项目

将生成的代码复制到 agents-api 仓库的对应目录：

```bash
# 源路径：./java/src/main/java/io/openkruise/agents/client/runtime/envd
# 目标路径：agents-api/runtime/java/src/main/java/io/openkruise/agents/client/runtime/envd
```

📦 **目标仓库路径**：[`runtime/java/src/main/java/io/openkruise/agents/client/runtime/envd`](../runtime/java/src/main/java/io/openkruise/agents/client/runtime/envd)

### 代码同步说明

> ⚠️ **重要**：由于 Java SDK 尚未发布到 Maven Central，`e2b/java` 模块会直接依赖 `runtime/java` 中的 envd 代码。

**同步规则**：

当 `runtime/java/.../runtime` 相关代码发生修改时，必须同步更新到 `e2b/java/.../runtime` 的对应目录：

```
runtime/java/src/main/java/io/openkruise/agents/client/runtime
    ↓ 同步到
e2b/java/src/main/java/io/openkruise/agents/client/e2b/runtime
```

**原因**：

- `e2b/java` 模块依赖 `runtime/java` 中的 runtime 代码
- 在 Maven 发布前，两个模块需要保持代码一致性，避免编译错误或运行时异常

> 💡 **后续优化**：待 runtime 包发布到 Maven Central 后，只需在 `e2b/java` 的 `pom.xml` 中引入 runtime 依赖即可，届时不再需要手动同步代码。

## Go 代码生成

### Sandbox API 代码生成

#### 1. 进入规范目录

```bash
# 如果尚未克隆
git clone https://github.com/e2b-dev/E2B.git
cd E2B
```

#### 2. 修改 OpenAPI 规范

> ⚠️ **注意**：原版 OpenAPI 规范生成的 Go 代码存在命名重复问题，需要先修改 `spec/openapi.yml` 中的以下字段名：
> - `NewSandbox` → `CreateSandboxRequest`
> - `NewTeamAPIKey` → `CreateTeamAPIKeyRequest`
> - `NewVolume` → `CreateVolumeRequest`

#### 3. 生成 Go 客户端

在 `E2B` 目录下执行：

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

#### 4. 更新到项目

将生成的代码复制到 agents-api 仓库的对应目录：

```bash
# 源路径：./api
# 目标路径：agents-api/e2b/api
```

📦 **目标仓库路径**：[`e2b/api`](../e2b/api)

### envd gRPC 代码生成

#### 1. 进入规范目录

```bash
# 如果尚未克隆 E2B 规范仓库
git clone https://github.com/e2b-dev/E2B.git
cd E2B/spec/envd
```

#### 2. 配置 buf 生成规则

在 `spec/envd` 目录下创建 `buf.gen.go.yaml` 文件：

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

#### 3. 生成 Go gRPC 代码

在 `E2B/spec/envd` 目录下执行：

```bash
buf generate --template buf.gen.go.yaml
```

#### 4. 更新到项目

将生成的代码复制到 agents-api 仓库的对应目录：

```bash
# 源路径：./go/envd
# 目标路径：agents-api/runtime/envd
```

📦 **目标仓库路径**：[`runtime/envd`](../runtime/envd)
