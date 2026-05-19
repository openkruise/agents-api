#!/usr/bin/env bash

# Usage:
#   ./hack/generate_client.sh                # update upstream + generate
#   ./hack/generate_client.sh --skip-update  # skip upstream update, generate only

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

SKIP_UPDATE=false
for arg in "$@"; do
    case "${arg}" in
        --skip-update) SKIP_UPDATE=true ;;
    esac
done

if [[ "${SKIP_UPDATE}" == "false" ]]; then
    echo "==> Updating upstream definitions (CRD + Go types)..."
    "${SCRIPT_DIR}/update_upstream.sh"
fi

go mod vendor
retVal=$?
if [ $retVal -ne 0 ]; then
    exit $retVal
fi

set -e
TMP_DIR=$(mktemp -d)
mkdir -p "${TMP_DIR}"/src/github.com/openkruise/agents-api
cp -r ./{agents,hack,vendor,go.mod,.git} "${TMP_DIR}"/src/github.com/openkruise/agents-api/

chmod +x "${TMP_DIR}"/src/github.com/openkruise/agents-api/vendor/k8s.io/code-generator/generate-internal-groups.sh
echo "tmp_dir: ${TMP_DIR}"

SCRIPT_ROOT="${TMP_DIR}"/src/github.com/openkruise/agents-api
CODEGEN_PKG=${CODEGEN_PKG:-"${SCRIPT_ROOT}/vendor/k8s.io/code-generator"}

echo "source ${CODEGEN_PKG}/kube_codegen.sh"
source "${CODEGEN_PKG}/kube_codegen.sh"

echo "gen_helpers"
GOPATH=${TMP_DIR} GO111MODULE=off kube::codegen::gen_helpers \
    --boilerplate "${SCRIPT_ROOT}/hack/boilerplate.go.txt" \
    "${SCRIPT_ROOT}"

echo "gen_client"
GOPATH=${TMP_DIR} GO111MODULE=off kube::codegen::gen_client \
    --with-watch \
    --plural-exceptions "SandboxUpdateOps:SandboxUpdateOps" \
    --output-dir "${SCRIPT_ROOT}/client" \
    --output-pkg "github.com/openkruise/agents-api/client" \
    --boilerplate "${SCRIPT_ROOT}/hack/boilerplate.go.txt" \
    "${SCRIPT_ROOT}"

mkdir -p ./client
rm -rf ./client/{clientset,informers,listers}
mv "${TMP_DIR}"/src/github.com/openkruise/agents-api/client/* ./client/
