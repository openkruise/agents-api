#!/usr/bin/env bash
# Generate Python SDK models from local CRD YAML using datamodel-codegen.
#
# Prerequisites:
#   - Python 3.11+
#   - pip install datamodel-code-generator black isort
#   - yq (https://github.com/mikefarah/yq)
#   - CRD YAML files present in agents/crds/
#
# Usage:
#   ./hack/generate_python_sdk.sh                # update upstream + generate
#   ./hack/generate_python_sdk.sh --skip-update  # skip upstream update, generate only
#   make generate-python
#
# NOTE: This script only generates raw Pydantic models.
#       Field type refinements (e.g. Any → V1PodTemplateSpec) are handled
#       separately via hack/patch_sdk_types.sh.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# ── Parse arguments ───────────────────────────────────────────────────────────
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

# ── Configuration ─────────────────────────────────────────────────────────────
CRD_SOURCE_DIR="${PROJECT_ROOT}/agents/crds"
MODELS_DIR="${PROJECT_ROOT}/k8s/python/openkruise/agents/models"

WORK_DIR=$(mktemp -d)
trap "rm -rf ${WORK_DIR}" EXIT
SCHEMAS_DIR="${WORK_DIR}/schemas"
mkdir -p "${SCHEMAS_DIR}"

# ── Pre-flight checks ────────────────────────────────────────────────────────
echo "==> Pre-flight checks..."

for cmd in yq datamodel-codegen ruff; do
    if ! command -v "${cmd}" &>/dev/null; then
        echo "ERROR: ${cmd} is not installed."
        echo "  pip install datamodel-code-generator ruff"
        echo "  brew install yq"
        exit 1
    fi
done

if [[ ! -d "${CRD_SOURCE_DIR}" ]]; then
    echo "ERROR: CRD source directory not found: ${CRD_SOURCE_DIR}"
    exit 1
fi

CRD_COUNT=$(find "${CRD_SOURCE_DIR}" -name '*.yaml' -type f | wc -l | tr -d ' ')
echo "  CRDs: ${CRD_COUNT} files in ${CRD_SOURCE_DIR}"

# ── Generate Pydantic models ──────────────────────────────────────────────────
echo "==> Generating Pydantic models..."

for crd_file in "${CRD_SOURCE_DIR}"/*.yaml; do
    kind=$(yq '.spec.names.kind' < "${crd_file}")
    kind_lower=$(echo "${kind}" | tr '[:upper:]' '[:lower:]')

    schema_json="${SCHEMAS_DIR}/${kind_lower}_schema.json"
    output_model="${MODELS_DIR}/${kind_lower}.py"

    echo "  ${kind} ($(basename "${crd_file}"))"

    yq '.spec.versions[0].schema.openAPIV3Schema' -o=json < "${crd_file}" > "${schema_json}"

    datamodel-codegen \
        --input "${schema_json}" \
        --output "${output_model}" \
        --input-file-type jsonschema \
        --target-python-version 3.11 \
        --use-schema-description \
        --use-field-description \
        --field-constraints \
        --keep-model-order \
        --class-name "${kind}"

    sed -i 's/regex=/pattern=/g' "${output_model}"

    ruff check --fix --select I -q "${output_model}" 2>/dev/null || true
    ruff format -q "${output_model}"
done

# ── Regenerate package exports ──────────────────────────────────────────────
# Rebuild models/__init__.py so newly added CRDs are re-exported
# automatically (no manual maintenance).
echo "==> Regenerating models/__init__.py exports..."
python3 "${PROJECT_ROOT}/k8s/codegen/generate_python_init.py" "${MODELS_DIR}"

# ── Summary ───────────────────────────────────────────────────────────────────
PY_FILE_COUNT=$(find "${MODELS_DIR}" -name '*.py' ! -name '__init__.py' | wc -l | tr -d ' ')
echo ""
echo "========================================="
echo " Python SDK generation complete!"
echo " Tool:      datamodel-codegen"
echo " Source:    ${CRD_SOURCE_DIR} (${CRD_COUNT} CRD files)"
echo " Generated: ${PY_FILE_COUNT} model files"
echo " Location:  ${MODELS_DIR}"
echo "========================================="
echo ""
echo "NOTE: Run 'make patch-sdk-types LANG=python' to refine field types"
echo "      (e.g. Any → V1PodTemplateSpec, V1ObjectMeta, etc.)"
