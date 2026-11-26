# Run go vet against code
vet:
	go vet ./...

# Generate code
generate: controller-gen
	@hack/generate_client.sh

# controller-gen and the client generators are declared as tool directives
# in tools.mod and run through the module toolchain (GOWORK=off
# go run -mod=readonly -modfile=tools.mod, see hack/generate_client.sh)
# instead of a go-install-tool download to ./bin.
.PHONY: controller-gen
controller-gen: ## Tidy tools.mod, which pins the code generation tools as tool directives.
	go mod tidy -modfile=tools.mod

# Update CRD YAML and Go type definitions from upstream openkruise/agents
.PHONY: update-upstream
update-upstream:
	@hack/update_upstream.sh

# Generate Java SDK from CRD definitions (requires JDK 8+)
.PHONY: generate-java
generate-java:
	@hack/generate_java_sdk.sh
	@hack/patch_sdk_types.sh --java

# Generate Python SDK from CRD definitions (requires yq + datamodel-codegen)
.PHONY: generate-python
generate-python:
	@hack/generate_python_sdk.sh
	@hack/patch_sdk_types.sh --python

# Patch generated SDK types (AnyType → concrete K8s types)
# Usage:
#   make patch-sdk-types               # patch both Java and Python
#   make patch-sdk-types LANG=java     # patch Java only
#   make patch-sdk-types LANG=python   # patch Python only
.PHONY: patch-sdk-types
patch-sdk-types:
ifeq ($(LANG),java)
	@hack/patch_sdk_types.sh --java
else ifeq ($(LANG),python)
	@hack/patch_sdk_types.sh --python
else
	@hack/patch_sdk_types.sh
endif

# Generate all SDKs (Go + Java + Python) and apply type patches
# Usage:
#   make generate-all                   # update upstream + generate all + patch types
#   make generate-all SKIP_UPDATE=true  # skip upstream update, generate + patch only
.PHONY: generate-all
ifeq ($(SKIP_UPDATE),true)
generate-all:
	@hack/generate_client.sh --skip-update
	@hack/generate_java_sdk.sh --skip-update
	@hack/generate_python_sdk.sh --skip-update
	@hack/patch_sdk_types.sh
else
generate-all: update-upstream
	@hack/generate_client.sh --skip-update
	@hack/generate_java_sdk.sh --skip-update
	@hack/generate_python_sdk.sh --skip-update
	@hack/patch_sdk_types.sh
endif

# ==================== E2E Testing ====================

KIND ?= kind
KIND_CLUSTER_NAME ?= ci-testing
KIND_IMAGE ?= kindest/node:v1.32.0
KIND_VERSION ?= v0.22.0

# Setup Kind cluster for e2e tests
.PHONY: setup-test-e2e
setup-test-e2e:
	@echo "Creating Kind cluster..."
	$(KIND) create cluster --name $(KIND_CLUSTER_NAME) --image $(KIND_IMAGE) --config test/kind-conf.yaml || true
	@echo "Installing CRDs..."
	kubectl apply -f agents/crds/

# Run all SDK e2e tests
.PHONY: test-e2e
test-e2e: setup-test-e2e
	bash hack/run-k8s-sdk-e2e-test.sh --sdk all

# Run Go SDK e2e test
.PHONY: test-e2e-go
test-e2e-go: setup-test-e2e
	bash hack/run-k8s-sdk-e2e-test.sh --sdk go

# Run Java SDK e2e test
.PHONY: test-e2e-java
test-e2e-java: setup-test-e2e
	bash hack/run-k8s-sdk-e2e-test.sh --sdk java

# Run Python SDK e2e test
.PHONY: test-e2e-python
test-e2e-python: setup-test-e2e
	bash hack/run-k8s-sdk-e2e-test.sh --sdk python

# Cleanup Kind cluster
.PHONY: cleanup-test-e2e
cleanup-test-e2e:
	@$(KIND) delete cluster --name $(KIND_CLUSTER_NAME)

# ==================== Python E2B Patch Tests ====================

# Pin either variable to test a specific SDK combination, for example:
#   make test-e2b-patch E2B_VERSION=2.35.0 CODE_INTERPRETER_VERSION=2.9.0
# Unpinned runs resolve the newest versions allowed by the dev extra. Keep the
# pin ranges in sync with e2b/python/kruise_agents/patch_traffic_token.py
# (_check_compatibility); CI exercises the full matrix in
# .github/workflows/test-e2b-python.yaml.
.PHONY: test-e2b-patch
test-e2b-patch:
	cd e2b/python && pip install -e ".[dev]" $(if $(E2B_VERSION),e2b==$(E2B_VERSION)) $(if $(CODE_INTERPRETER_VERSION),e2b-code-interpreter==$(CODE_INTERPRETER_VERSION)) && pytest tests/ -v

# ==================== Module Dependencies ====================

# tools.mod mirrors go.mod (same module path, same dependency versions) and
# adds tool directives for the code generation tools: controller-gen
# (sigs.k8s.io/controller-tools) and the client generators
# (k8s.io/code-generator). Whenever dependencies are upgraded in go.mod,
# apply the same upgrades to tools.mod and re-run tidy so both files and
# tools.sum stay in sync; `make deps` tidies both module files. Keep the
# tool versions in sync with the openkruise/agents repository so both
# repos produce identical generated output.
.PHONY: deps
deps:
	go mod tidy
	go mod tidy -modfile=tools.mod
