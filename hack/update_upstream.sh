#!/usr/bin/env bash
# Download the latest CRD YAML and Go type definitions from the upstream
# openkruise/agents repository on GitHub.
#
# Sources:
#   - CRD:      github.com/openkruise/agents/config/crd/bases       → agents/crds/
#   - Go types: github.com/openkruise/agents/api/v1alpha1            → agents/v1alpha1/
#   - Go types: github.com/openkruise/agents/api/security/v1alpha1  → security/v1alpha1/
#
# Notes:
#   - security/ lives at the repo top level (next to agents/, mirroring
#     upstream api/security) because kube::codegen::gen_client only
#     discovers API groups laid out as <group>/<version> directly under
#     the scan root; a nested agents/security/v1alpha1 layout makes
#     client-gen look for a non-existent package.
#   - The security group types are patched locally after download (see
#     patch_genclient_marker and patch_groupversion_helpers below) so that
#     kube::codegen::gen_client also emits a working clientset for them:
#     upstream has not added the +genclient marker, SchemeGroupVersion alias
#     or Resource() helper to that group yet (its own client/ lacks it too).
#
# Prerequisites:
#   - curl
#
# Usage:
#   ./hack/update_upstream.sh              # update both CRD and Go types
#   ./hack/update_upstream.sh --crds-only  # update CRD files only
#   ./hack/update_upstream.sh --types-only # update Go type files only

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# ── Configuration ─────────────────────────────────────────────────────────────
UPSTREAM_REPO="openkruise/agents"
UPSTREAM_BRANCH="master"
RAW_BASE="https://raw.githubusercontent.com/${UPSTREAM_REPO}/${UPSTREAM_BRANCH}"
API_BASE="https://api.github.com/repos/${UPSTREAM_REPO}/contents"

CRD_REMOTE_PATH="config/crd/bases"
TYPES_REMOTE_PATH="api/v1alpha1"
SECURITY_TYPES_REMOTE_PATH="api/security/v1alpha1"

CRD_LOCAL_DIR="${PROJECT_ROOT}/agents/crds"
TYPES_LOCAL_DIR="${PROJECT_ROOT}/agents/v1alpha1"
SECURITY_TYPES_LOCAL_DIR="${PROJECT_ROOT}/security/v1alpha1"

# Add auth header only if token is set; otherwise run anonymously.
CURL_AUTH=()
if [[ -n "${GITHUB_TOKEN:-}" ]]; then
    CURL_AUTH=(-H "Authorization: Bearer ${GITHUB_TOKEN}")
fi

# ── Parse arguments ───────────────────────────────────────────────────────────
UPDATE_CRDS=true
UPDATE_TYPES=true

for arg in "$@"; do
    case "${arg}" in
        --crds-only)
            UPDATE_TYPES=false
            ;;
        --types-only)
            UPDATE_CRDS=false
            ;;
        --help|-h)
            echo "Usage: $0 [--crds-only] [--types-only]"
            exit 0
            ;;
        *)
            echo "Unknown argument: ${arg}"
            exit 1
            ;;
    esac
done

# ── Helper: download all files in a GitHub directory ──────────────────────────
# Usage: download_github_dir <remote_path> <local_dir> <file_extension>
download_github_dir() {
    local remote_path="$1"
    local local_dir="$2"
    local file_ext="$3"

    echo "  Fetching file list from GitHub API: ${API_BASE}/${remote_path} ..."

    local file_list
    file_list=$(curl -fsSL "${CURL_AUTH[@]}" "${API_BASE}/${remote_path}?ref=${UPSTREAM_BRANCH}" \
        | grep '"name"' \
        | sed 's/.*"name": *"\([^"]*\)".*/\1/')

    if [[ -z "${file_list}" ]]; then
        echo "  ERROR: Failed to fetch file list from GitHub API."
        echo "  You may be rate-limited. Try again later or set GITHUB_TOKEN."
        exit 1
    fi

    mkdir -p "${local_dir}"

    local count=0
    for filename in ${file_list}; do
        # Filter by extension if specified
        if [[ -n "${file_ext}" && "${filename}" != *"${file_ext}" ]]; then
            continue
        fi

        local url="${RAW_BASE}/${remote_path}/${filename}"
        echo "    ↓ ${filename}"
        curl -fsSL "${CURL_AUTH[@]}" -o "${local_dir}/${filename}" "${url}"
        count=$((count + 1))
    done

    echo "  Downloaded ${count} files to ${local_dir}"
}

# ── Helper: inject the +genclient marker into a synced type file ──────────────
# client-gen only generates clients for root types marked with +genclient,
# and upstream has not added the marker to the security group types yet.
# Inject it right above each root type declaration so "make generate"
# produces a clientset for the security group too. Idempotent: skips types
# that already carry the marker. Drop this once upstream ships +genclient.
patch_genclient_marker() {
    local file="$1" kind="$2"
    if grep -B1 "^type ${kind} struct {" "${file}" | grep -q '^// +genclient'; then
        echo "    +genclient already present on ${kind}"
        return
    fi
    sed -i "s|^type ${kind} struct {|// +genclient\ntype ${kind} struct {|" "${file}"
    echo "    injected +genclient into ${kind}"
}

# ── Helper: add the client-gen convention symbols to groupversion_info.go ────
# The generated clientset (setConfigDefaults) and listers reference
# SchemeGroupVersion and Resource(), which upstream ships for the agents
# group but not for the security group. Add both, mirroring the agents
# group's groupversion_info.go. Idempotent. Drop this once upstream ships
# the same helpers.
patch_groupversion_helpers() {
    local file="$1"
    if grep -q 'SchemeGroupVersion' "${file}"; then
        echo "    SchemeGroupVersion already present"
    else
        sed -i 's|^\t// SchemeBuilder is used to add go types|\tSchemeGroupVersion = GroupVersion\n\n\t// SchemeBuilder is used to add go types|' "${file}"
        echo "    injected SchemeGroupVersion"
    fi
    if grep -q 'func Resource(' "${file}"; then
        echo "    Resource() already present"
    else
        cat >> "${file}" <<'EOF'

// Resource is required by pkg/client/listers/...
func Resource(resource string) schema.GroupResource {
	return SchemeGroupVersion.WithResource(resource).GroupResource()
}
EOF
        echo "    injected Resource()"
    fi
}

# ── Main ──────────────────────────────────────────────────────────────────────
echo "==> Updating upstream definitions from ${UPSTREAM_REPO}@${UPSTREAM_BRANCH}"

if [[ "${UPDATE_CRDS}" == "true" ]]; then
    echo ""
    echo "--- Updating CRD YAML files ---"
    download_github_dir "${CRD_REMOTE_PATH}" "${CRD_LOCAL_DIR}" ".yaml"
fi

if [[ "${UPDATE_TYPES}" == "true" ]]; then
    echo ""
    echo "--- Updating Go type definitions (agents.kruise.io) ---"
    download_github_dir "${TYPES_REMOTE_PATH}" "${TYPES_LOCAL_DIR}" ".go"

    echo ""
    echo "--- Updating Go type definitions (security.agents.kruise.io) ---"
    download_github_dir "${SECURITY_TYPES_REMOTE_PATH}" "${SECURITY_TYPES_LOCAL_DIR}" ".go"

    # Remove leftovers from the old (broken) agents/security/v1alpha1
    # layout so client-gen does not pick the package up twice.
    if [[ -d "${PROJECT_ROOT}/agents/security" ]]; then
        echo "Removing obsolete ${PROJECT_ROOT}/agents/security (moved to security/)"
        rm -rf "${PROJECT_ROOT}/agents/security"
    fi

    patch_genclient_marker "${SECURITY_TYPES_LOCAL_DIR}/agentidentity_types.go" "AgentIdentity"
    patch_genclient_marker "${SECURITY_TYPES_LOCAL_DIR}/agentauthenticationconfig_types.go" "AgentAuthenticationConfig"
    patch_groupversion_helpers "${SECURITY_TYPES_LOCAL_DIR}/groupversion_info.go"
fi

echo ""
echo "==> Upstream update complete!"
