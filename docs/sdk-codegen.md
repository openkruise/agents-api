# SDK Code Generation Guide

This document describes how to generate multi-language SDK client code for the Kruise Agents API.

## Overview

The project supports SDK generation for three languages:

| Language | Generator                         | Output Directory |
|----------|-----------------------------------|------------------|
| Go       | k8s code-generator                | `client/`        |
| Java     | Fabric8 java-generator-cli 6.14.0 | `k8s/java/`      |
| Python   | datamodel-codegen (Pydantic v2)   | `k8s/python/`    |

## Prerequisites

### Go

- Go 1.21+

### Java

- JDK 8+
- Fabric8 java-generator-cli (auto-downloaded by script)

### Python

- Python 3.11+
- Install dependencies:

```bash
pip install datamodel-code-generator ruff pyyaml
brew install yq
```

## Quick Start

### Generate All SDKs at Once

```bash
# Sync CRDs from upstream + generate all SDKs + auto-patch types
make generate-all

# Skip upstream sync, generate + patch only
make generate-all SKIP_UPDATE=true
```

> **⚠️ Note:** Upstream sync downloads CRD files via the GitHub API. Without a token, you may hit rate limiting (HTTP
> 403). If this happens, manually place the latest CRD YAML files into the `agents/crds/` directory, then use
`SKIP_UPDATE=true` to skip the automatic sync.

### Generate a Single Language

```bash
# Go client
make generate

# Java SDK
make generate-java

# Python SDK
make generate-python
```

### Run Type Patching Only

```bash
# Patch all SDKs
make patch-sdk-types

# Patch Java only
make patch-sdk-types LANG=java

# Patch Python only
make patch-sdk-types LANG=python
```

## Generation Workflow

### Go Client

Uses the official Kubernetes `code-generator` to produce clientset, lister, and informer.

```
hack/generate_client.sh
  → k8s.io/code-generator (clientset / lister / informer)
  → Output: client/
```

### Java SDK

Uses Fabric8 java-generator-cli to generate Java model classes from CRD YAML definitions.

```
hack/generate_java_sdk.sh
  → Fabric8 java-generator-cli
  → Output: k8s/java/.../models/
  → hack/patch_sdk_types.sh --java (type patching)
```

**Generated Java models** are located at:

```
k8s/java/src/main/java/io/openkruise/agents/client/v2/models/
```

This includes Java type definitions for all CRD resources: Sandbox, SandboxSet, SandboxTemplate, SandboxClaim,
SandboxUpdateOps, Checkpoint, etc.

### Python SDK

Uses datamodel-codegen to generate Pydantic v2 models from CRD JSON Schema.

```
hack/generate_python_sdk.sh
  → datamodel-codegen (Pydantic v2)
  → ruff formatting
  → Output: k8s/python/.../models/
  → hack/patch_sdk_types.sh --python (type patching + ruff formatting)
```

**Generated Python models** are located at:

```
k8s/python/openkruise/agents/models/
```

Each CRD maps to a Python file (e.g. `sandbox.py`, `sandboxset.py`), containing Pydantic BaseModel class definitions.

## Type Patching Mechanism

### Why Is Type Patching Needed?

CRD fields marked with `x-kubernetes-preserve-unknown-fields: true` lose concrete type information during code
generation:

| Field                  | Go Type                          | Java Generated | Python Generated   |
|------------------------|----------------------------------|----------------|--------------------|
| `template`             | `*corev1.PodTemplateSpec`        | `AnyType` ❌    | `Any` ❌            |
| `volumeClaimTemplates` | `[]corev1.PersistentVolumeClaim` | `AnyType` ❌    | `Any` ❌            |
| `metadata`             | `metav1.ObjectMeta`              | ✅ Correct      | `dict[str, Any]` ❌ |
| `patch`                | `runtime.RawExtension`           | `AnyType` ❌    | ✅ Kept as Any      |
| `podTemplateDelta`     | `runtime.RawExtension`           | `AnyType` ❌    | ✅ Kept as Any      |

The type patching scripts automatically replace these placeholder types with the correct Kubernetes types.

### Patching Rules Configuration

All replacement rules are centrally maintained in `k8s/codegen/type_mapping.yaml`:

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

To **add a new field rule**, simply add a new entry to this YAML file and re-run `make generate-all`.

### Patching Execution Flow

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

## Project Structure

```
agents-api/
├── agents/crds/                    # CRD YAML definitions (data source)
├── client/                         # Go clientset / lister / informer
├── k8s/
│   ├── codegen/                    # Post-generation tooling
│   │   ├── type_mapping.yaml       # Type replacement rules
│   │   ├── patch_java_types.py     # Java type patching script
│   │   └── patch_python_types.py   # Python type patching script
│   ├── java/                       # Java SDK
│   └── python/                     # Python SDK
├── hack/
│   ├── generate_client.sh          # Go client generation
│   ├── generate_java_sdk.sh        # Java SDK generation
│   ├── generate_python_sdk.sh      # Python SDK generation
│   ├── patch_sdk_types.sh          # Type patching entry script
│   └── update_upstream.sh          # Upstream CRD sync
└── Makefile                        # Build entry point
```
