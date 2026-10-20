#!/usr/bin/env bash
# Generate Java SDK v2 from CRD YAML definitions using Fabric8 java-generator-cli.
#
# Prerequisites:
#   - JDK 8+ installed
#   - CRD YAML files present in agents/crds/
#
# Usage:
#   ./hack/generate_java_sdk.sh                # update upstream + generate
#   ./hack/generate_java_sdk.sh --skip-update  # skip upstream update, generate only
#   make generate-java
#
# NOTE: This script only generates raw Java classes.
#       Field type refinements (e.g. AnyType → PodTemplateSpec) are handled
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
FABRIC8_VERSION="6.14.0"
CRD_SOURCE_DIR="${PROJECT_ROOT}/agents/crds"
JAVA_TARGET_DIR="${PROJECT_ROOT}/k8s/java/src/main/java"
TARGET_PACKAGE="io.openkruise.agents.client.v2.models"
CLI_JAR_DIR="${PROJECT_ROOT}/bin"
CLI_JAR="${CLI_JAR_DIR}/java-generator-cli-${FABRIC8_VERSION}.jar"
MAVEN_REPO_URL="https://repo1.maven.org/maven2/io/fabric8/java-generator-cli/${FABRIC8_VERSION}/java-generator-cli-${FABRIC8_VERSION}.jar"

# ── Pre-flight checks ────────────────────────────────────────────────────────
echo "==> Pre-flight checks..."

if ! command -v java &>/dev/null; then
    echo "ERROR: java is not installed. Please install JDK 8+."
    exit 1
fi
echo "  Java: $(java -version 2>&1 | head -1)"

if [[ ! -d "${CRD_SOURCE_DIR}" ]]; then
    echo "ERROR: CRD source directory not found: ${CRD_SOURCE_DIR}"
    exit 1
fi

CRD_COUNT=$(find "${CRD_SOURCE_DIR}" -name '*.yaml' | wc -l | tr -d ' ')
if [[ "${CRD_COUNT}" -eq 0 ]]; then
    echo "ERROR: No CRD YAML files found in ${CRD_SOURCE_DIR}"
    exit 1
fi
echo "  CRDs: ${CRD_COUNT} files in ${CRD_SOURCE_DIR}"

# ── Download CLI jar if not present ───────────────────────────────────────────
if [[ ! -f "${CLI_JAR}" ]]; then
    echo "==> Downloading Fabric8 java-generator-cli ${FABRIC8_VERSION}..."
    mkdir -p "${CLI_JAR_DIR}"
    curl -fsSL -o "${CLI_JAR}" "${MAVEN_REPO_URL}"
fi

# ── Generate Java classes ─────────────────────────────────────────────────────
V2_MODELS_DIR="${JAVA_TARGET_DIR}/io/openkruise/agents/client/v2/models"
TARGET_PACKAGE_DIR=$(echo "${TARGET_PACKAGE}" | tr '.' '/')

echo "==> Generating Java SDK v2..."

GEN_TEMP_DIR=$(mktemp -d)
trap "rm -rf ${GEN_TEMP_DIR}" EXIT

# Both API groups are remapped into the single target package:
#   agents.kruise.io         → io.kruise.agents.v1alpha1
#   security.agents.kruise.io → io.kruise.agents.security.v1alpha1
# Without the second override the security CRD classes would land in their
# default package and be silently dropped when copying to V2_MODELS_DIR.
java -jar "${CLI_JAR}" \
    --source "${CRD_SOURCE_DIR}" \
    --target "${GEN_TEMP_DIR}" \
    --package-overrides io.kruise.agents.v1alpha1="${TARGET_PACKAGE}" \
    --package-overrides io.kruise.agents.security.v1alpha1="${TARGET_PACKAGE}" \
    --enum-uppercase \
    --skip-generated-annotations

GENERATED_ROOT="${GEN_TEMP_DIR}/${TARGET_PACKAGE_DIR}"

if [[ ! -d "${GENERATED_ROOT}" ]]; then
    echo "ERROR: Expected generated directory not found: ${GENERATED_ROOT}"
    find "${GEN_TEMP_DIR}" -type f
    exit 1
fi

# Install generated files
rm -rf "${V2_MODELS_DIR}"
mkdir -p "${V2_MODELS_DIR}"
cp -R "${GENERATED_ROOT}"/* "${V2_MODELS_DIR}/"

# ── Summary ───────────────────────────────────────────────────────────────────
JAVA_FILE_COUNT=$(find "${V2_MODELS_DIR}" -name '*.java' | wc -l | tr -d ' ')
echo ""
echo "========================================="
echo " Java SDK v2 generation complete!"
echo " Tool:      Fabric8 java-generator-cli ${FABRIC8_VERSION}"
echo " Source:    ${CRD_SOURCE_DIR} (${CRD_COUNT} CRD files)"
echo " Generated: ${JAVA_FILE_COUNT} Java files"
echo " Package:   ${TARGET_PACKAGE}"
echo " Location:  ${V2_MODELS_DIR}"
echo "========================================="
echo ""
echo "NOTE: Run 'make patch-sdk-types LANG=java' to refine field types"
echo "      (e.g. AnyType → PodTemplateSpec, RawExtension, etc.)"
