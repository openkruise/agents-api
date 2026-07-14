# SDK 代码生成指南

本文档介绍如何为 Kruise Agents API 生成多语言 SDK 客户端代码。

## 概览

项目支持三种语言的 SDK 生成：

| 语言     | 生成工具                              | 输出目录          |
|--------|-----------------------------------|---------------|
| Go     | k8s code-generator                | `client/`     |
| Java   | Fabric8 java-generator-cli 6.14.0 | `k8s/java/`   |
| Python | datamodel-codegen（Pydantic v2）    | `k8s/python/` |

## 前置依赖

### Go

- Go 1.21+

### Java

- JDK 8+
- Fabric8 java-generator-cli（脚本自动下载）

### Python

- Python 3.11+
- 安装依赖：

```bash
pip install datamodel-code-generator ruff pyyaml
brew install yq
```

## 快速开始

### 一键生成所有 SDK

```bash
# 从上游同步 CRD + 生成所有 SDK + 自动修补类型
make generate-all

# 跳过上游同步，仅生成 + 修补
make generate-all SKIP_UPDATE=true
```

> **⚠️ 注意：** 上游同步通过 GitHub API 下载 CRD 文件，未携带 Token 时可能触发限流（HTTP 403）。如果遇到限流，可以手动将最新的
> CRD YAML 文件放到 `agents/crds/` 目录下，然后使用 `SKIP_UPDATE=true` 跳过自动同步。

### 单独生成某个语言

```bash
# Go 客户端
make generate

# Java SDK
make generate-java

# Python SDK
make generate-python
```

### 仅执行类型修补

```bash
# 修补所有 SDK
make patch-sdk-types

# 仅修补 Java
make patch-sdk-types LANG=java

# 仅修补 Python
make patch-sdk-types LANG=python
```

## 生成流程详解

### Go 客户端

使用 Kubernetes 官方 `code-generator` 生成 clientset、lister、informer。

```
hack/generate_client.sh
  → k8s.io/code-generator（clientset / lister / informer）
  → 输出到 client/
```

### Java SDK

使用 Fabric8 java-generator-cli 从 CRD YAML 生成 Java 模型类。

```
hack/generate_java_sdk.sh
  → Fabric8 java-generator-cli
  → 输出到 k8s/java/.../models/
  → hack/patch_sdk_types.sh --java（类型修补）
```

**生成的 Java 模型**位于：

```
k8s/java/src/main/java/io/openkruise/agents/client/v2/models/
```

包含 Sandbox、SandboxSet、SandboxTemplate、SandboxClaim、SandboxUpdateOps、Checkpoint 等所有 CRD 资源的 Java 类型定义。

### Python SDK

使用 datamodel-codegen 从 CRD JSON Schema 生成 Pydantic v2 模型。

```
hack/generate_python_sdk.sh
  → datamodel-codegen（Pydantic v2）
  → ruff 格式化
  → 输出到 k8s/python/.../models/
  → hack/patch_sdk_types.sh --python（类型修补 + ruff 格式化）
```

**生成的 Python 模型**位于：

```
k8s/python/openkruise/agents/models/
```

每个 CRD 对应一个 Python 文件（如 `sandbox.py`、`sandboxset.py`），包含 Pydantic BaseModel 类定义。

## 类型修补机制

### 为什么需要类型修补？

CRD 中标记了 `x-kubernetes-preserve-unknown-fields: true` 的字段，在代码生成时会丢失具体类型信息：

| 字段                     | Go 类型                            | Java 生成结果   | Python 生成结果        |
|------------------------|----------------------------------|-------------|--------------------|
| `template`             | `*corev1.PodTemplateSpec`        | `AnyType` ❌ | `Any` ❌            |
| `volumeClaimTemplates` | `[]corev1.PersistentVolumeClaim` | `AnyType` ❌ | `Any` ❌            |
| `metadata`             | `metav1.ObjectMeta`              | ✅ 正常        | `dict[str, Any]` ❌ |
| `patch`                | `runtime.RawExtension`           | `AnyType` ❌ | ✅ 保留 Any           |
| `podTemplateDelta`     | `runtime.RawExtension`           | `AnyType` ❌ | ✅ 保留 Any           |

类型修补脚本会自动将这些占位符类型替换为正确的 Kubernetes 类型。

### 修补规则配置

所有替换规则集中维护在 `k8s/codegen/type_mapping.yaml`：

```yaml
fields:
  - name: template
    go_type: "*corev1.PodTemplateSpec"
    java:
      old_type: "io.fabric8.kubernetes.api.model.AnyType"
      new_type: "io.fabric8.kubernetes.api.model.PodTemplateSpec"
    python:
      old_pattern: "Any"
      new_type: "V1PodTemplateSpec"
      import: "V1PodTemplateSpec"
```

**新增字段规则**时，只需在此 YAML 文件中添加一条记录，然后重新执行 `make generate-all` 即可。

### 修补执行流程

执行修补时，脚本会先打印所有即将修改的位置和内容，然后再执行替换：

```
==> SDK Type Patching (rules: type_mapping.yaml)
==> Patching Java SDK types...
  Planned Java type changes:
    SandboxSetSpec.java:81  template: private AnyType template;
      → private PodTemplateSpec template;
    ...
  Patched 4 Java file(s)
==> Patching Python SDK types...
  Planned Python type changes:
    sandbox.py:223  template: template: Any | None = None
      →  template: V1PodTemplateSpec | None = None
    ...
  Patched 6 Python file(s)
```

## 项目结构

```
agents-api/
├── agents/crds/                    # CRD YAML 定义（数据源）
├── client/                         # Go clientset / lister / informer
├── k8s/
│   ├── codegen/                    # 代码生成后处理工具
│   │   ├── type_mapping.yaml       # 类型替换规则配置
│   │   ├── patch_java_types.py     # Java 类型修补脚本
│   │   └── patch_python_types.py   # Python 类型修补脚本
│   ├── java/                       # Java SDK
│   └── python/                     # Python SDK
├── hack/
│   ├── generate_client.sh          # Go 客户端生成
│   ├── generate_java_sdk.sh        # Java SDK 生成
│   ├── generate_python_sdk.sh      # Python SDK 生成
│   ├── patch_sdk_types.sh          # 类型修补入口脚本
│   └── update_upstream.sh          # 上游 CRD 同步
└── Makefile                        # 构建入口
```
