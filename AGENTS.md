# AGENTS.md

This file provides guidance for AI agents working on the `agents-api` repository.

## Project Overview

`agents-api` is the canonical, read-only location for Kruise Agents API definitions and Go client. It provides stable Go types for serializing/deserializing Kruise Agents resources, intended for direct use by consumers.

**Governance**: All changes must originate in `openkruise/agents` (source repo); this repo is synced read-only.

**Versioning**: Follows `v0.x.z` scheme — `x` matches Kruise Agents major/minor; `z` increments for bugfixes or cherry-picks.

The repository hosts two API groups:

- `agents.kruise.io/v1alpha1` — sandbox workload resources (types in `agents/v1alpha1/`)
- `security.agents.kruise.io/v1alpha1` — agent identity/authentication resources (types in `security/v1alpha1/`)

## Repository Structure

```
agents-api/
├── agents/v1alpha1/         # agents.kruise.io API types (synced from upstream api/v1alpha1)
├── agents/crds/             # CRD YAML manifests (synced from upstream config/crd/bases)
├── security/v1alpha1/       # security.agents.kruise.io API types (synced from upstream api/security/v1alpha1)
├── client/                  # Auto-generated Go client (clientset, informers, listers)
├── e2b/                     # E2B control-plane Go SDK (api/ is OpenAPI-generated) + Java/Python SDK sources
├── runtime/                 # Agent-runtime Go SDK (envd/ is protobuf-generated, code interpreter support)
├── k8s/                     # K8s client SDKs: Java (java/) and Python (python/), generated from CRDs
├── examples/                # Usage examples (e2b, runtime, SandboxClaim)
├── docs/                    # SDK codegen and publishing guides
├── hack/                    # Build scripts and code generation tooling
├── test/e2e/                # Ginkgo E2E tests against a Kind cluster
├── cmd/, pkg/               # Populated by the OpenAPI schema pipeline (make gen-openapi-schema)
└── .github/workflows/       # CI workflows
```

### Key Directories

- **`agents/v1alpha1/`** — `agents.kruise.io` type definitions, synced from upstream. Contains CRD types:
  - `sandbox_types.go` — Sandbox resource
  - `sandboxset_types.go` — SandboxSet resource
  - `sandboxclaim_types.go` — SandboxClaim resource
  - `sandboxtemplate_types.go` — SandboxTemplate resource
  - `sandboxupdateops_types.go` — SandboxUpdateOps resource
  - `checkpoint_types.go` — Checkpoint resource
  - `commit_types.go` — Commit resource
  - `poolautoscaler_types.go` — PoolAutoscaler resource
  - `securityprofile_types.go` — SecurityProfile / GlobalSecurityProfile resources
  - `trafficpolicy_types.go` — TrafficPolicy / GlobalTrafficPolicy resources
  - `mount_types.go` — Mount types
  - `annotations.go`, `e2b_annotations.go`, `labels.go` — Shared constants
  - `zz_generated.deepcopy.go` — Auto-generated deep copy methods (do not edit)
  - `groupversion_info.go` — Group version registration

- **`security/v1alpha1/`** — `security.agents.kruise.io` type definitions, synced from upstream `api/security/v1alpha1`:
  - `agentidentity_types.go` — AgentIdentity resource (namespace-scoped agent identity)
  - `agentauthenticationconfig_types.go` — AgentAuthenticationConfig resource (trusted OIDC/JWT issuers)
  - `groupversion_info.go` — Group version registration (carries locally-injected helpers, see below)
  - `zz_generated.deepcopy.go` — Auto-generated deep copy methods (do not edit)

  Note: `security/` lives at the repo top level (mirroring upstream `api/security`) because `kube::codegen::gen_client` only discovers API groups laid out as `<group>/<version>` directly under the scan root; a nested `agents/security/v1alpha1` layout makes client-gen look for a non-existent package.

- **`client/`** — Auto-generated Kubernetes client libraries. **Do not edit manually.** Regenerated via `make generate`.
  - `clientset/` — Typed client for both API groups
  - `informers/` — Watch/informer implementations
  - `listers/` — Resource lister implementations

- **`e2b/`** — Go SDK for the E2B control-plane API
  - `api/` — Auto-generated E2B API models (OpenAPI-based, do not edit)
  - `java/`, `python/` — Java and Python SDK sources
  - `sandbox.go`, `sandbox_api.go`, `config.go` — Hand-written Go SDK surface

- **`runtime/`** — Go SDK for the agent runtime
  - `envd/` — Protobuf-generated envd service clients (do not edit)
  - `client.go`, `commands.go`, `codeinterpreter.go` — Hand-written runtime client
  - `java/` — Java SDK sources

- **`k8s/`** — Multi-language K8s client SDKs, generated from the CRD YAMLs
  - `java/` — Java client models (published to Maven Central)
  - `python/` — Python client models (published to PyPI)

## Tech Stack

- **Language**: Go 1.25.0
- **Module**: `github.com/openkruise/agents-api`
- **Core Dependencies**:
  - `k8s.io/api`, `k8s.io/apimachinery`, `k8s.io/client-go` v0.35.0
  - `k8s.io/code-generator` v0.35.0
  - `k8s.io/kube-openapi` v0.0.0-20250910181357-589584f1c912
  - `sigs.k8s.io/controller-runtime` v0.21.0
  - `connectrpc.com/connect` v1.19.2
  - `github.com/go-bindata/go-bindata` v3.1.2
  - `google.golang.org/protobuf` v1.36.9
- **Code Generation Tools**:
  - `controller-gen` v0.18.0 (downloaded to `./bin/`)
  - `openapi-gen` (version derived from go.mod)

## Build Commands

All commands should be run from the project root directory.

```bash
# Run go vet across all packages
make vet

# Regenerate Go client code (clientset, informers, listers, deep-copy)
make generate

# Sync CRD YAML + Go types from upstream openkruise/agents@master
make update-upstream

# Generate Java SDK from CRD definitions (requires JDK 8+)
make generate-java

# Generate Python SDK from CRD definitions (requires yq + datamodel-codegen)
make generate-python

# Full pipeline: update-upstream + Go client + Java/Python SDKs + type patches
make generate-all

# Fix generated SDK types (AnyType -> K8s concrete types)
make patch-sdk-types

# Generate OpenAPI schema
make gen-schema-only      # schema only
make gen-openapi-schema   # full pipeline

# E2E tests on a local Kind cluster
make setup-test-e2e
make test-e2e             # all SDKs; or test-e2e-go / test-e2e-java / test-e2e-python
make cleanup-test-e2e

# E2B Python patch combination tests
make test-e2b-patch
```

### Code Generation Details

- `make generate` runs `hack/generate_client.sh`, which:
  1. Runs `go mod vendor`
  2. Uses `kube::codegen::gen_helpers` to generate deep-copy and helper methods
  3. Uses `kube::codegen::gen_client` to generate clientset/informers/listers for both `agents/` and `security/` groups
  4. Copies generated output back into `./client/`

- `make update-upstream` runs `hack/update_upstream.sh`, which syncs from `openkruise/agents@master` via a git shallow clone (sparse-checkout when git >= 2.25, plain `--depth 1` fallback):
  - `config/crd/bases` → `agents/crds/`
  - `api/v1alpha1` → `agents/v1alpha1/`
  - `api/security/v1alpha1` → `security/v1alpha1/`

  Git is used instead of the GitHub contents API because it reuses the global `http.proxy` setting and is not subject to anonymous API rate limits. The legacy curl-based implementation is kept at `hack/update_upstream_curl.sh` for reference (do not use). The sync is idempotent and re-applies the security patches below on fresh upstream files.

- `make generate-all` chains `update-upstream` → `generate_client.sh --skip-update` → `generate_java_sdk.sh --skip-update` → `generate_python_sdk.sh --skip-update` → `patch_sdk_types.sh`.

- Java SDK generation (`hack/generate_java_sdk.sh`) requires a `--package-overrides` mapping for the security group (`io.kruise.agents.security.v1alpha1`); without it the security CRD classes land in a default package and are silently dropped by the copy step.

- `make openapi-gen` downloads the `openapi-gen` binary to `./bin/` if not present.

## Important Rules

### Do Not Edit Auto-Generated Code

The following files/directories are auto-generated and must not be edited by hand:
- `agents/v1alpha1/zz_generated.deepcopy.go`
- `security/v1alpha1/zz_generated.deepcopy.go`
- `client/` (entire directory — regenerated by `make generate`)
- `e2b/api/` (generated from E2B OpenAPI spec)
- `runtime/envd/` (generated from protobuf definitions)
- `k8s/java/`, `k8s/python/` generated models (regenerated by `make generate-java` / `make generate-python`)

### Editing API Types

Types under `agents/v1alpha1/` and `security/v1alpha1/` are synced from upstream — do not hand-edit them; changes must originate in `openkruise/agents` and be pulled in via `make update-upstream`. After any type change lands here:
1. Run `make generate` to regenerate deep-copy methods and client code
2. Run `make vet` to verify no issues
3. For OpenAPI schema changes, run `make gen-openapi-schema`

### Locally-Injected Patches on security/ Types

Upstream ships the `security.agents.kruise.io` types without the markers the Go client codegen needs (its own `client/` lacks a security clientset too). `hack/update_upstream.sh` therefore injects these patches after every sync (idempotent):
- `// +genclient` above `AgentIdentity` and `AgentAuthenticationConfig`
- `SchemeGroupVersion` alias and `Resource()` helper in `security/v1alpha1/groupversion_info.go`

Do not remove these lines when touching synced files — the generated clientset/listers fail to compile without them (`undefined: securityv1alpha1.SchemeGroupVersion / Resource`). Drop the patches once upstream ships the same markers/helpers.

### Kubernetes API Conventions

- API groups: `agents.kruise.io`, `security.agents.kruise.io`
- API version: `v1alpha1`
- Types use standard Kubernetes `metav1.TypeMeta` and `metav1.ObjectMeta` embeddings
- Use `+kubebuilder:` markers for CRD schema annotations
- Use `+k8s:deepcopy-gen` markers for deep-copy generation
- Follow [Kubernetes API conventions](https://github.com/kubernetes/community/blob/master/contributors/devel/sig-architecture/api-conventions.md)

## CI Workflows

- **`ci-vet.yaml`** (Go Vet) — runs `go vet` on push and PR
- **`schema-update.yaml`** (Update schema files when api changes) — runs `make gen-openapi-schema` on push to `master` when `agents/**` or `security/**` files change, then auto-commits the updated schemas
- **`e2e-k8s-sdk.yaml`** (E2E K8S SDK Tests) — Go/Java/Python K8s SDK E2E tests on a Kind cluster (push, PR, manual)
- **`test-e2b-python.yaml`** (E2B Python Patch Tests) — E2B Python patch combination matrix tests (push, PR, manual)
- **`generate-crd.yml`** (CRD Java Model Generate) — manual workflow to generate Java CRD models from CRD YAML sources
- **`publish-k8s-client.yml`** (Publish K8s Client to Maven Central) — manual
- **`publish-e2b-client.yml`** (Publish E2B Client to Maven Central) — manual
- **`publish-e2b-python-client.yml`** (Publish E2B Python Client to PyPI) — manual
- **`publish-runtime-client.yml`** (Publish Runtime Client to Maven Central) — manual

## Linting and Validation

```bash
# Run go vet (the primary lint tool for this project)
make vet

# Alternatively:
go vet ./...
```

There is no golangci-lint or similar tool configured. Use `go vet` for validation.

## Git and Branch Conventions

- Default branch: `master`
- This is a read-only synced repo — changes should originate in `openkruise/agents`
- The `.gitignore` excludes: `bin/`, `vendor/`, `*.test`, `*.out`, `.idea/`
