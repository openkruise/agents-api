# K8s SDK Generation Pipeline

This document describes how to automatically generate Go, Java, and Python SDK code from Kruise Agents CRD definitions,
as well as the subsequent type patching process.

## Overview

The project supports SDK generation for three languages:

| Language | Generator                         | Output Directory |
|----------|-----------------------------------|------------------|
| Go       | k8s code-generator                | `client/`        |
| Java     | Fabric8 java-generator-cli 6.14.0 | `k8s/java/`      |
| Python   | datamodel-codegen (Pydantic v2)   | `k8s/python/`    |

## Architecture

```
openkruise/agents (upstream)
        │
        ▼
hack/update_upstream.sh          ← Pull latest CRD YAML + Go type definitions
        │
        ├──► agents/crds/*.yaml          (CRD definitions)
        └──► agents/v1alpha1/*.go        (Go types)
                │
                ▼
┌───────────────────────────────────────────────────────────┐
│              SDK Code Generation (Two Phases)              │
│                                                           │
│  Phase 1: Raw Code Generation                              │
│    Go     → k8s.io/code-generator (clientset/informers)   │
│    Java   → Fabric8 java-generator-cli                    │
│    Python → datamodel-codegen (Pydantic)                  │
│                                                           │
│  Phase 2: Type Patching (Java/Python only)                │
│    Read rules from k8s/codegen/type_mapping.yaml          │
│    Replace AnyType/Any placeholders with concrete K8s types│
└───────────────────────────────────────────────────────────┘
        │
        ├──► client/ (Go clientset/informers/listers)
        ├──► k8s/java/.../v2/models/*.java
        └──► k8s/python/.../models/*.py
```

## Prerequisites

### General

| Tool       | Purpose                          |
|------------|----------------------------------|
| curl       | Fetch upstream files from GitHub |
| Go 1.25.0+ | Run code generation scripts      |

### Go SDK

| Tool                  | Version | Description                                     |
|-----------------------|---------|-------------------------------------------------|
| Go                    | 1.25.0+ | Run code generation scripts                     |
| k8s.io/code-generator | v0.35.0 | Generate clientset/informers/listers (vendored) |

### Java SDK

| Tool  | Version | Description                                 |
|-------|---------|---------------------------------------------|
| JDK   | 8+      | Run Fabric8 CLI jar                         |
| Maven | 3.x     | Build Java project (optional, compile only) |

The Fabric8 java-generator-cli jar is automatically downloaded to `./bin/` by the script — no manual installation
required.

### Python SDK

| Tool                     | Installation                           | Description                               |
|--------------------------|----------------------------------------|-------------------------------------------|
| Python                   | 3.11+                                  | Run datamodel-codegen and patch scripts   |
| datamodel-code-generator | `pip install datamodel-code-generator` | JSON Schema → Pydantic models             |
| yq                       | `brew install yq`                      | Extract OpenAPI schema from CRD YAML      |
| ruff                     | `pip install ruff`                     | Code formatting (fallback: black + isort) |
| PyYAML                   | `pip install pyyaml`                   | Required by patch scripts                 |

Install Python dependencies:

```bash
pip install datamodel-code-generator ruff pyyaml
brew install yq
```

## Directory Structure

```
agents-api/
├── agents/
│   ├── crds/                          # CRD YAML files (synced by update_upstream.sh)
│   └── v1alpha1/                      # Go type definitions (synced by update_upstream.sh)
├── client/                            # Generated Go client (clientset/informers/listers)
├── k8s/
│   ├── codegen/
│   │   ├── type_mapping.yaml          # Type mapping rules (core configuration)
│   │   ├── patch_java_types.py        # Java type patching script
│   │   └── patch_python_types.py      # Python type patching script
│   ├── java/
│   │   ├── pom.xml                    # Maven project configuration
│   │   └── src/main/java/
│   │       └── io/openkruise/agents/client/v2/models/
│   │           └── *.java             # Generated Java models
│   └── python/
│       ├── pyproject.toml             # Python project configuration
│       └── openkruise/agents/models/
│           └── *.py                   # Generated Python models
├── hack/
│   ├── update_upstream.sh             # Sync CRDs + Go types from upstream
│   ├── generate_client.sh             # Go client generation script
│   ├── generate_java_sdk.sh           # Java SDK generation script
│   ├── generate_python_sdk.sh         # Python SDK generation script
│   └── patch_sdk_types.sh             # Type patching entry script
└── Makefile                           # Build entry point
```

## Quick Start

### Generate All SDKs at Once

```bash
# Automatically pull latest CRDs from upstream GitHub + generate Go/Java/Python + type patching
# Note: GitHub API has rate limits (60 req/hour unauthenticated); frequent runs may trigger rate limiting
make generate-all

# Skip upstream sync, generate + patch based on local files only
make generate-all SKIP_UPDATE=true
```

**Manual upstream update (recommended)**: Due to GitHub API rate limiting, automatic fetching may fail. It is
recommended to manually sync the latest files from the upstream repository to local, then use `SKIP_UPDATE=true` to skip
automatic fetching: after syncing, run `make generate-all SKIP_UPDATE=true` to generate SDKs based on local files.

### Generate Go SDK Only

```bash
make generate
```

Equivalent to:

```bash
hack/generate_client.sh  # Generate Go clientset/informers/listers
```

### Generate Java SDK Only

```bash
make generate-java
```

Equivalent to:

```bash
hack/generate_java_sdk.sh       # Phase 1: Generate raw Java classes
hack/patch_sdk_types.sh --java  # Phase 2: Type patching
```

### Generate Python SDK Only

```bash
make generate-python
```

Equivalent to:

```bash
hack/generate_python_sdk.sh       # Phase 1: Generate Pydantic models
hack/patch_sdk_types.sh --python  # Phase 2: Type patching
```

### Run Type Patching Only

```bash
make patch-sdk-types              # Patch Java + Python
make patch-sdk-types LANG=java    # Patch Java only
make patch-sdk-types LANG=python  # Patch Python only
```

## Phase 1: Raw Code Generation

### Go — k8s.io/code-generator

**Script**: `hack/generate_client.sh`

**How it works**:

1. Reads Go type definitions from `agents/v1alpha1/`
2. Uses k8s.io/code-generator to generate Kubernetes client code:
    - gen_helpers: Generate deep-copy methods (zz_generated.deepcopy.go)
    - gen_client: Generate clientset, informers, listers
3. Outputs to the `client/` directory

**Parameters**:

- `--skip-update`: Skip upstream sync, run generation only

**Generated content**:

- `client/clientset/`: Typed client for interacting with Agents API resources
- `client/informers/`: Watch/informer implementations
- `client/listers/`: Resource lister implementations

**Note**: All code under the `client/` directory is auto-generated and must not be edited manually.

### Java — Fabric8 java-generator-cli

**Script**: `hack/generate_java_sdk.sh`

**How it works**:

1. Reads all CRD YAML files from `agents/crds/`
2. Uses Fabric8 java-generator-cli (v6.14.0) to convert CRDs to Java POJOs
3. Package mapping: `io.kruise.agents.v1alpha1` → `io.openkruise.agents.client.v2.models`
4. Outputs to `k8s/java/src/main/java/io/openkruise/agents/client/v2/models/`

**Parameters**:

- `--skip-update`: Skip upstream sync, run generation only

**Generated content**:

Includes Java type definitions for all CRD resources: Sandbox, SandboxSet, SandboxTemplate, SandboxClaim,
SandboxUpdateOps, Checkpoint, etc.

**Known limitations**:

- CRD fields marked with `x-kubernetes-preserve-unknown-fields: true` are generated as `AnyType`, requiring Phase 2
  patching

### Python — datamodel-codegen

**Script**: `hack/generate_python_sdk.sh`

**How it works**:

1. Iterates over each CRD YAML in `agents/crds/`
2. Uses yq to extract `.spec.versions[0].schema.openAPIV3Schema` as JSON Schema
3. Uses datamodel-codegen to convert JSON Schema to Pydantic v2 models
4. Uses ruff to format the output code
5. Outputs to `k8s/python/openkruise/agents/models/`

**Parameters**:

- `--skip-update`: Skip upstream sync, run generation only

**Generated content**:

Each CRD maps to a Python file (e.g. `sandbox.py`, `sandboxset.py`), containing Pydantic BaseModel class definitions.

**Known limitations**:

- `x-kubernetes-preserve-unknown-fields` fields are generated as `Any` type, requiring Phase 2 patching

## Phase 2: Type Patching

### Why Is Type Patching Needed?

Some CRD fields use `x-kubernetes-preserve-unknown-fields: true`, which prevents code generators from inferring concrete
types, resulting in placeholder types:

- Java: `io.fabric8.kubernetes.api.model.AnyType`
- Python: `Any` or `dict[str, Any]`

These placeholder types do not cause compile/runtime errors, but they lose type safety. The type patching phase replaces
these placeholders with the correct Kubernetes types.

### Type Mapping Rules

Rules are defined in `k8s/codegen/type_mapping.yaml`. The current field mappings are:

| Field                | Go Type                        | Java Patching                         | Python Patching                     |
|----------------------|--------------------------------|---------------------------------------|-------------------------------------|
| metadata             | metav1.ObjectMeta              | Skipped (already correct)             | dict[str, Any] → V1ObjectMeta       |
| template             | *corev1.PodTemplateSpec        | AnyType → PodTemplateSpec             | Any → V1PodTemplateSpec             |
| volumeClaimTemplates | []corev1.PersistentVolumeClaim | AnyType → List<PersistentVolumeClaim> | Any → list[V1PersistentVolumeClaim] |
| patch                | runtime.RawExtension           | AnyType → RawExtension                | Skipped (kept as Any)               |
| podTemplateDelta     | runtime.RawExtension           | AnyType → RawExtension                | Skipped (kept as Any)               |

### How the Patching Scripts Work

**Entry point**: `hack/patch_sdk_types.sh`

When patching runs, the script first prints all planned changes (file, line number, before/after), then applies them:

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

#### Java Patching (patch_java_types.py)

1. Loads Java rules from type_mapping.yaml
2. Sorts fields by name length in descending order (to prevent `template` from matching `volumeClaimTemplates`)
3. For each .java file, uses regex to precisely match field declarations, getters, and setters
4. Replaces AnyType with concrete types
5. Validates no residual placeholder types remain

#### Python Patching (patch_python_types.py)

1. Loads Python rules from type_mapping.yaml
2. For each .py file, uses regex to match field declaration lines
3. Replaces `Any` / `dict[str, Any]` with concrete types
4. Automatically adds `from kubernetes.client.models import ...` imports
5. Injects `model_config = ConfigDict(arbitrary_types_allowed=True)` for classes using K8s types
6. Cleans up unused `Any` imports
7. Formats code using ruff (or black + isort as fallback)

## Upstream Sync

**Script**: `hack/update_upstream.sh`

Downloads the latest definitions from the GitHub repository openkruise/agents (master branch):

| Remote Path             | Local Path       | Description          |
|-------------------------|------------------|----------------------|
| config/crd/bases/*.yaml | agents/crds/     | CRD YAML definitions |
| api/v1alpha1/*.go       | agents/v1alpha1/ | Go type definitions  |

**Parameters**:

- `--crds-only`: Update CRD files only
- `--types-only`: Update Go type files only

Can also be invoked via Makefile:

```bash
make update-upstream
```

**Note**: GitHub API has rate limits (60 req/hour unauthenticated). Frequent runs may trigger rate limiting and cause
fetch failures. You can manually sync the latest files from the upstream repository to local instead.

## Maintenance Guide

### Adding New Type Mapping Rules

When a new `x-kubernetes-preserve-unknown-fields` field is added to a CRD:

1. Edit `k8s/codegen/type_mapping.yaml` and add a new entry to the fields list:

```yaml
  - name: newFieldName
    go_type: "Corresponding Go type (for documentation only)"
    java:
      old_type: "io.fabric8.kubernetes.api.model.AnyType"
      new_type: "io.fabric8.kubernetes.api.model.TargetType"
    python:
      old_pattern: "Any"
      new_type: "V1TargetType"
      import: "V1TargetType"
```

2. If a particular SDK does not need patching, set `skip: true`
3. Run `make generate-all` to regenerate and auto-patch
