# AGENTS.md

This file provides guidance for AI agents working on the `agents-api` repository.

## Project Overview

`agents-api` is the canonical, read-only location for Kruise Agents API definitions and Go client. It provides stable Go types for serializing/deserializing Kruise Agents resources, intended for direct use by consumers.

**Governance**: All changes must originate in `openkruise/agents` (source repo); this repo is synced read-only.

**Versioning**: Follows `v0.x.z` scheme — `x` matches Kruise Agents major/minor; `z` increments for bugfixes or cherry-picks.

## Repository Structure

```
agents-api/
├── agents/v1alpha1/         # Core API type definitions (CRD specs)
├── security/v1alpha1/       # Security API type definitions (synced from upstream api/security)
├── client/                  # Auto-generated Go client (clientset, informers, listers)
├── clients/                 # Multi-language client SDKs (Java, Python)
├── sdk/                     # Go SDK for E2B API and runtime (proto, sandbox, runtime)
├── examples/                # Usage examples (e.g. SandboxClaim)
├── hack/                    # Build scripts and code generation tooling
├── tools.mod / tools.sum    # Code generation tools as Go tool directives
├── vendor/                  # Go dependencies (vendored)
└── .github/workflows/       # CI workflows
```

### Key Directories

- **`agents/v1alpha1/`** — Hand-written API type definitions. Contains CRD types:
  - `sandbox_types.go` — Sandbox resource
  - `sandboxset_types.go` — SandboxSet resource
  - `sandboxclaim_types.go` — SandboxClaim resource
  - `sandboxtemplate_types.go` — SandboxTemplate resource
  - `sandboxupdateops_types.go` — SandboxUpdateOps resource
  - `checkpoint_types.go` — Checkpoint resource
  - `mount_types.go` — Mount types
  - `annotations.go` — Shared annotation constants
  - `e2b_annotations.go` — E2B-specific annotation constants
  - `zz_generated.deepcopy.go` — Auto-generated deep copy methods (do not edit)
  - `groupversion_info.go` — Group version registration (`agents.kruise.io/v1alpha1`)

- **`client/`** — Auto-generated Kubernetes client libraries. **Do not edit manually.** Regenerated via `make generate`.
  - `clientset/` — Typed client for interacting with Agents API resources
  - `informers/` — Watch/informer implementations
  - `listers/` — Resource lister implementations

- **`sdk/`** — Go SDK for E2B API integration and runtime management
  - `proto/api/` — Auto-generated E2B API client (OpenAPI-based, do not edit)
  - `proto/envd/` — Protobuf-generated envd service clients (do not edit)
  - `runtime/` — Runtime client and command handling
  - `sandbox/` — Sandbox management SDK
  - `example/` — SDK usage examples

- **`clients/java/`** — Java client library (CRD-generated models, published to Maven Central)
- **`clients/python/`** — Python client library

## Tech Stack

- **Language**: Go 1.26.0
- **Module**: `github.com/openkruise/agents-api`
- **Core Dependencies**:
  - `k8s.io/api`, `k8s.io/apimachinery`, `k8s.io/client-go` v0.37.2
  - `sigs.k8s.io/controller-runtime` v0.25.2
  - `connectrpc.com/connect` v1.19.2
  - `google.golang.org/protobuf` v1.36.12
- **Code Generation Tools** (pinned as tool directives in `tools.mod`, run via
  `GOWORK=off go run -mod=readonly -modfile=tools.mod`):
  - `sigs.k8s.io/controller-tools/cmd/controller-gen` v0.23.0
  - `k8s.io/code-generator/cmd/{client,lister,informer}-gen` v0.37.2

  Tool versions are kept in sync with the `openkruise/agents` repository so
  both repos produce identical generated output.

## Build Commands

All commands should be run from the project root directory.

```bash
# Run go vet across all packages
make vet

# Regenerate client code (clientset, informers, listers)
make generate

# Tidy go.mod and tools.mod together (run after dependency upgrades)
make deps
```

### Code Generation Details

- `make generate` runs `hack/generate_client.sh`, which:
  1. Optionally updates upstream definitions via `hack/update_upstream.sh`
     (skip with `--skip-update`)
  2. Discovers `+genclient`-tagged API packages under `agents/` and `security/`
  3. Runs `client-gen`, `lister-gen` and `informer-gen` from the tool
     directives in `tools.mod` — no `go mod vendor`, GOPATH staging, or
     `go install` is involved
  4. Publishes the staged output into `./client/`

- Deep-copy files (`zz_generated.deepcopy.go`) are synced verbatim from the
  upstream `openkruise/agents` repository by `hack/update_upstream.sh`
  (upstream generates them with controller-gen); they are not regenerated
  locally.

### tools.mod Sync

`tools.mod` mirrors `go.mod` (same module path, same dependency versions) and
adds tool directives for the code generation tools. When upgrading go.mod
dependencies, apply the same upgrades to `tools.mod` and run
`go mod tidy -modfile=tools.mod` (`make deps` tidies both) so `tools.sum`
stays in sync. Tool versions must also stay aligned with the
`openkruise/agents` repository.

## Important Rules

### Do Not Edit Auto-Generated Code

The following files/directories are auto-generated and must not be edited by hand:
- `agents/v1alpha1/zz_generated.deepcopy.go`
- `client/` (entire directory — regenerated by `make generate`)
- `sdk/proto/api/` (generated from E2B OpenAPI spec)
- `sdk/proto/envd/` (generated from protobuf definitions)

### Editing API Types

When modifying CRD types in `agents/v1alpha1/*_types.go`:
1. Edit only the hand-written type files (not `zz_generated.deepcopy.go`)
2. After changes, run `make generate` to regenerate client code
3. Run `make vet` to verify no issues

### Kubernetes API Conventions

- API group: `agents.kruise.io`
- API version: `v1alpha1`
- Types use standard Kubernetes `metav1.TypeMeta` and `metav1.ObjectMeta` embeddings
- Use `+kubebuilder:` markers for CRD schema annotations (deep-copy methods are generated upstream by controller-gen)
- Follow [Kubernetes API conventions](https://github.com/kubernetes/community/blob/master/contributors/devel/sig-architecture/api-conventions.md)

## CI Workflows

- **`ci-vet.yaml`** — Runs `make vet` on push/PR
- **`generate-crd.yml`** — Manual workflow to generate Java CRD models from CRD YAML sources
- **`publish-maven-central.yml`** — Manual workflow to publish Java client to Maven Central

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
