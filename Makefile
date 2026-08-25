CURRENT_DIR=$(shell pwd)
# Get the currently used golang install path (in GOPATH/bin, unless GOBIN is set)
ifeq (,$(shell go env GOBIN))
GOBIN=$(shell go env GOPATH)/bin
else
GOBIN=$(shell go env GOBIN)
endif

# Run go vet against code
vet:
	go vet ./...

# Generate code
generate: controller-gen
	#$(CONTROLLER_GEN) object:headerFile="hack/boilerplate.go.txt" paths="./..."
	@hack/generate_client.sh

CONTROLLER_GEN = $(shell pwd)/bin/controller-gen
controller-gen: ## Download controller-gen locally if necessary.
ifeq ("$(shell $(CONTROLLER_GEN) --version 2> /dev/null)", "Version: v0.16.5")
else
	rm -rf $(CONTROLLER_GEN)
	$(call go-get-tool,$(CONTROLLER_GEN),sigs.k8s.io/controller-tools/cmd/controller-gen@v0.16.5)
endif

OPENAPI_GEN = $(shell pwd)/bin/openapi-gen
module=$(shell go list -f '{{.Module}}' k8s.io/kube-openapi/cmd/openapi-gen | awk '{print $$1}')
module_version=$(shell go list -m $(module) | awk '{print $$NF}' | head -1)
openapi-gen: ## Download openapi-gen locally if necessary.
ifeq ("$(shell command -v $(OPENAPI_GEN) 2> /dev/null)", "")
	$(call go-get-tool,$(OPENAPI_GEN),k8s.io/kube-openapi/cmd/openapi-gen@$(module_version))
else
	@echo "openapi-gen is already installed."
endif

# go-get-tool will 'go get' any package $2 and install it to $1.
PROJECT_DIR := $(shell dirname $(abspath $(lastword $(MAKEFILE_LIST))))
define go-get-tool
@[ -f $(1) ] || { \
set -e ;\
TMP_DIR=$$(mktemp -d) ;\
cd $$TMP_DIR ;\
go mod init tmp ;\
echo "Downloading $(2)" ;\
GOBIN=$(PROJECT_DIR)/bin go install $(2) ;\
rm -rf $$TMP_DIR ;\
}
endef

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

.PHONY: test-e2b-patch
test-e2b-patch:
	cd e2b/python && pip install -e ".[dev]" && pytest tests/ -v

# ==================== Schema Generation ====================

.PHONY: gen-schema-only
gen-schema-only:
	go run cmd/gen-schema/main.go

.PHONY: gen-openapi-schema
gen-openapi-schema: gen-all-openapi
	go run cmd/gen-schema/main.go

.PHONY: gen-all-openapi
	@hack/generate_openapi.sh
