# K8s SDK 生成流水线

本文档说明如何从 Kruise Agents CRD 定义自动生成 Go、Java 和 Python SDK 代码，以及后续的类型修正流程。

## 概览

项目支持三种语言的 SDK 生成：

| 语言     | 生成工具                              | 输出目录          |
|--------|-----------------------------------|---------------|
| Go     | k8s code-generator                | `client/`     |
| Java   | Fabric8 java-generator-cli 6.14.0 | `k8s/java/`   |
| Python | datamodel-codegen（Pydantic v2）    | `k8s/python/` |

## 整体架构

```
openkruise/agents (上游)
        │
        ▼
hack/update_upstream.sh          ← 拉取最新 CRD YAML + Go 类型定义
        │
        ├──► agents/crds/*.yaml          (CRD 定义)
        └──► agents/v1alpha1/*.go        (Go 类型)
                │
                ▼
┌───────────────────────────────────────────────────────┐
│              SDK 代码生成（两阶段）                      │
│                                                       │
│  阶段 1: 原始代码生成                                    │
│    Go     → k8s.io/code-generator (clientset/informers)│
│    Java   → Fabric8 java-generator-cli                 │
│    Python → datamodel-codegen (Pydantic)               │
│                                                       │
│  阶段 2: 类型修正 (Type Patching, 仅 Java/Python)       │
│    读取 k8s/codegen/type_mapping.yaml 规则              │
│    将 AnyType/Any 占位符替换为具体的 K8s 类型             │
└───────────────────────────────────────────────────────┘
        │
        ├──► client/ (Go clientset/informers/listers)
        ├──► k8s/java/.../v2/models/*.java
        └──► k8s/python/.../models/*.py
```

## 前置依赖

### 通用

| 工具         | 用途              |
|------------|-----------------|
| curl       | 从 GitHub 拉取上游文件 |
| Go 1.25.0+ | 运行代码生成脚本        |

### Go SDK

| 工具                    | 版本要求    | 说明                                       |
|-----------------------|---------|------------------------------------------|
| Go                    | 1.25.0+ | 运行代码生成脚本                                 |
| k8s.io/code-generator | v0.35.0 | 生成 clientset/informers/listers（已 vendor） |

### Java SDK

| 工具    | 版本要求 | 说明                    |
|-------|------|-----------------------|
| JDK   | 8+   | 运行 Fabric8 CLI jar    |
| Maven | 3.x  | 构建 Java 项目（可选，仅编译时需要） |

Fabric8 java-generator-cli jar 会由脚本自动下载到 `./bin/`，无需手动安装。

### Python SDK

| 工具                       | 安装方式                                   | 说明                              |
|--------------------------|----------------------------------------|---------------------------------|
| Python                   | 3.11+                                  | 运行 datamodel-codegen 和 patch 脚本 |
| datamodel-code-generator | `pip install datamodel-code-generator` | JSON Schema → Pydantic 模型       |
| yq                       | `brew install yq`                      | 从 CRD YAML 提取 OpenAPI schema    |
| ruff                     | `pip install ruff`                     | 代码格式化（可选，降级使用 black + isort）    |
| PyYAML                   | `pip install pyyaml`                   | patch 脚本依赖                      |

安装 Python 依赖：

```bash
pip install datamodel-code-generator ruff pyyaml
brew install yq
```

## 目录结构

```
agents-api/
├── agents/
│   ├── crds/                          # CRD YAML 文件（由 update_upstream.sh 同步）
│   └── v1alpha1/                      # Go 类型定义（由 update_upstream.sh 同步）
├── client/                            # 生成的 Go 客户端（clientset/informers/listers）
├── k8s/
│   ├── codegen/
│   │   ├── type_mapping.yaml          # 类型映射规则（核心配置）
│   │   ├── patch_java_types.py        # Java 类型修正脚本
│   │   └── patch_python_types.py      # Python 类型修正脚本
│   ├── java/
│   │   ├── pom.xml                    # Maven 项目配置
│   │   └── src/main/java/
│   │       └── io/openkruise/agents/client/v2/models/
│   │           └── *.java             # 生成的 Java 模型
│   └── python/
│       ├── pyproject.toml             # Python 项目配置
│       └── openkruise/agents/models/
│           └── *.py                   # 生成的 Python 模型
├── hack/
│   ├── update_upstream.sh             # 从上游同步 CRD + Go 类型
│   ├── generate_client.sh             # Go 客户端生成脚本
│   ├── generate_java_sdk.sh           # Java SDK 生成脚本
│   ├── generate_python_sdk.sh         # Python SDK 生成脚本
│   └── patch_sdk_types.sh             # 类型修正入口脚本
└── Makefile                           # 构建入口
```

## 快速开始

### 一键生成所有 SDK

```bash
# 自动从上游 GitHub 拉取最新 CRD + 生成 Go/Java/Python + 类型修正
# 注意：GitHub API 存在限流（未认证 60 次/小时），频繁执行可能触发限流导致拉取失败
make generate-all

# 跳过上游自动拉取，仅基于本地文件执行生成 + 类型修正
make generate-all SKIP_UPDATE=true
```

**手动更新上游文件（推荐）**：由于 GitHub API 限流，自动拉取可能失败。建议手动从上游仓库同步最新文件到本地后，使用
`SKIP_UPDATE=true` 跳过自动拉取：同步完成后执行 `make generate-all SKIP_UPDATE=true` 即可基于本地文件生成 SDK。

### 单独生成 Go SDK

```bash
make generate
```

等价于：

```bash
hack/generate_client.sh  # 生成 Go clientset/informers/listers
```

### 单独生成 Java SDK

```bash
make generate-java
```

等价于：

```bash
hack/generate_java_sdk.sh       # 阶段 1: 生成原始 Java 类
hack/patch_sdk_types.sh --java  # 阶段 2: 类型修正
```

### 单独生成 Python SDK

```bash
make generate-python
```

等价于：

```bash
hack/generate_python_sdk.sh       # 阶段 1: 生成 Pydantic 模型
hack/patch_sdk_types.sh --python  # 阶段 2: 类型修正
```

### 仅执行类型修正

```bash
make patch-sdk-types              # 修正 Java + Python
make patch-sdk-types LANG=java    # 仅修正 Java
make patch-sdk-types LANG=python  # 仅修正 Python
```

## 阶段 1：原始代码生成

### Go — k8s.io/code-generator

**脚本**: `hack/generate_client.sh`

**工作原理**:

1. 从 `agents/v1alpha1/` 读取 Go 类型定义
2. 使用 k8s.io/code-generator 生成 Kubernetes 客户端代码：
    - gen_helpers：生成 deep-copy 方法（zz_generated.deepcopy.go）
    - gen_client：生成 clientset、informers、listers
3. 输出到 `client/` 目录

**参数**:

- `--skip-update`：跳过上游同步，仅执行生成

**生成内容**:

- `client/clientset/`：类型化客户端，用于与 Agents API 资源交互
- `client/informers/`：Watch/informer 实现
- `client/listers/`：资源列表器实现

**注意**：`client/` 目录下的所有代码都是自动生成的，禁止手动编辑。

### Java — Fabric8 java-generator-cli

**脚本**: `hack/generate_java_sdk.sh`

**工作原理**:

1. 从 `agents/crds/` 读取所有 CRD YAML 文件
2. 使用 Fabric8 java-generator-cli (v6.14.0) 将 CRD 转换为 Java POJO
3. 包名映射：`io.kruise.agents.v1alpha1` → `io.openkruise.agents.client.v2.models`
4. 输出到 `k8s/java/src/main/java/io/openkruise/agents/client/v2/models/`

**参数**:

- `--skip-update`：跳过上游同步，仅执行生成

**生成内容**:

包含 Sandbox、SandboxSet、SandboxTemplate、SandboxClaim、SandboxUpdateOps、Checkpoint 等所有 CRD 资源的 Java 类型定义。

**已知局限**:

- CRD 中标记了 `x-kubernetes-preserve-unknown-fields: true` 的字段会被生成为 `AnyType`，需要阶段 2 修正

### Python — datamodel-codegen

**脚本**: `hack/generate_python_sdk.sh`

**工作原理**:

1. 遍历 `agents/crds/` 中的每个 CRD YAML
2. 使用 yq 提取 `.spec.versions[0].schema.openAPIV3Schema` 为 JSON Schema
3. 使用 datamodel-codegen 将 JSON Schema 转换为 Pydantic v2 模型
4. 使用 ruff 格式化输出代码
5. 输出到 `k8s/python/openkruise/agents/models/`

**参数**:

- `--skip-update`：跳过上游同步，仅执行生成

**生成内容**:

每个 CRD 对应一个 Python 文件（如 `sandbox.py`、`sandboxset.py`），包含 Pydantic BaseModel 类定义。

**已知局限**:

- `x-kubernetes-preserve-unknown-fields` 字段会被生成为 `Any` 类型，需要阶段 2 修正

## 阶段 2：类型修正 (Type Patching)

### 为什么需要类型修正？

CRD 中部分字段使用了 `x-kubernetes-preserve-unknown-fields: true`，代码生成器无法推断其具体类型，会生成占位类型：

- Java: `io.fabric8.kubernetes.api.model.AnyType`
- Python: `Any` 或 `dict[str, Any]`

这些占位类型在编译/运行时不会报错，但会丢失类型安全。类型修正阶段将这些占位类型替换为正确的 Kubernetes 类型。

### 类型映射规则

规则定义在 `k8s/codegen/type_mapping.yaml` 中，当前包含以下字段映射：

| 字段名                  | Go 类型                          | Java 修正                               | Python 修正                           |
|----------------------|--------------------------------|---------------------------------------|-------------------------------------|
| metadata             | metav1.ObjectMeta              | 跳过（已正确处理）                             | dict[str, Any] → V1ObjectMeta       |
| template             | *corev1.PodTemplateSpec        | AnyType → PodTemplateSpec             | Any → V1PodTemplateSpec             |
| volumeClaimTemplates | []corev1.PersistentVolumeClaim | AnyType → List<PersistentVolumeClaim> | Any → list[V1PersistentVolumeClaim] |
| patch                | runtime.RawExtension           | AnyType → RawExtension                | 跳过（保持 Any）                          |
| podTemplateDelta     | runtime.RawExtension           | AnyType → RawExtension                | 跳过（保持 Any）                          |

### 修正脚本工作原理

**入口**: `hack/patch_sdk_types.sh`

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

#### Java 修正 (patch_java_types.py)

1. 加载 type_mapping.yaml 中的 Java 规则
2. 按字段名长度降序排列（避免 template 误匹配 volumeClaimTemplates）
3. 对每个 .java 文件，通过正则精确匹配字段声明、getter、setter
4. 替换 AnyType 为具体类型
5. 验证无残留占位类型

#### Python 修正 (patch_python_types.py)

1. 加载 type_mapping.yaml 中的 Python 规则
2. 对每个 .py 文件，通过正则匹配字段声明行
3. 替换 Any / dict[str, Any] 为具体类型
4. 自动添加 `from kubernetes.client.models import ...` 导入
5. 为使用 K8s 类型的类注入 `model_config = ConfigDict(arbitrary_types_allowed=True)`
6. 清理不再使用的 Any 导入
7. 使用 ruff（或 black + isort）格式化代码

## 上游同步

**脚本**: `hack/update_upstream.sh`

从 GitHub 仓库 openkruise/agents (master 分支) 下载最新定义：

| 远程路径                    | 本地路径             | 说明          |
|-------------------------|------------------|-------------|
| config/crd/bases/*.yaml | agents/crds/     | CRD YAML 定义 |
| api/v1alpha1/*.go       | agents/v1alpha1/ | Go 类型定义     |

**参数**:

- `--crds-only`：仅更新 CRD 文件
- `--types-only`：仅更新 Go 类型文件

也可通过 Makefile 调用：

```bash
make update-upstream
```

**注意**：GitHub API 存在限流（未认证 60 次/小时），频繁执行可能触发限流导致拉取失败，可手动从上游仓库同步最新文件到本地。

## 维护指南

### 新增类型映射规则

当 CRD 中新增了 `x-kubernetes-preserve-unknown-fields` 字段时：

1. 编辑 `k8s/codegen/type_mapping.yaml`，在 fields 列表中添加新条目：

```yaml
  - name: newFieldName
    go_type: "对应的 Go 类型（仅做文档参考）"
    java:
      old_type: "io.fabric8.kubernetes.api.model.AnyType"
      new_type: "io.fabric8.kubernetes.api.model.TargetType"
    python:
      old_pattern: "Any"
      new_type: "V1TargetType"
      import: "V1TargetType"
```

2. 如果某个 SDK 不需要修正，设置 `skip: true`
3. 运行 `make generate-all` 重新生成并自动修正
