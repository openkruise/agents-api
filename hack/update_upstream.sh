#!/usr/bin/env bash
# Sync the latest CRD YAML and Go type definitions from the upstream
# openkruise/agents repository on GitHub.
#
# The sync goes through a git shallow clone (sparse-checkout when the local
# git supports it) instead of the GitHub contents API: git automatically
# reuses the global http.proxy setting and is not subject to anonymous API
# rate limits, so the script works on both Linux servers and proxied dev
# machines where curl has no proxy configured.
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
#   - git
#
# Usage:
#   ./hack/update_upstream.sh              # update both CRD and Go types
#   ./hack/update_upstream.sh --crds-only  # update CRD files only
#   ./hack/update_upstream.sh --types-only # update Go type files only
#
# The legacy curl-based implementation is kept in hack/update_upstream_curl.sh
# for reference.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# ── Configuration ─────────────────────────────────────────────────────────────
UPSTREAM_REPO="openkruise/agents"
UPSTREAM_BRANCH="master"
UPSTREAM_URL="https://github.com/${UPSTREAM_REPO}.git"

CRD_REMOTE_PATH="config/crd/bases"
TYPES_REMOTE_PATH="api/v1alpha1"
SECURITY_TYPES_REMOTE_PATH="api/security/v1alpha1"

CRD_LOCAL_DIR="${PROJECT_ROOT}/agents/crds"
TYPES_LOCAL_DIR="${PROJECT_ROOT}/agents/v1alpha1"
SECURITY_TYPES_LOCAL_DIR="${PROJECT_ROOT}/security/v1alpha1"

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

# ── Clone upstream ────────────────────────────────────────────────────────────
# Sparse partial clone when the local git supports it (git >= 2.25), plain
# shallow clone otherwise. Both fetch exactly one commit from the branch.
CLONE_ROOT=$(mktemp -d)
CLONE_DIR="${CLONE_ROOT}/upstream"
trap 'rm -rf "${CLONE_ROOT}"' EXIT

echo "==> Cloning ${UPSTREAM_REPO}@${UPSTREAM_BRANCH} (shallow)..."
if git clone -h 2>&1 | grep -q -- '--sparse'; then
    git clone --quiet --depth 1 --filter=blob:none --sparse \
        --branch "${UPSTREAM_BRANCH}" "${UPSTREAM_URL}" "${CLONE_DIR}"
    (cd "${CLONE_DIR}" && git sparse-checkout set \
        "${CRD_REMOTE_PATH}" "${TYPES_REMOTE_PATH}" "${SECURITY_TYPES_REMOTE_PATH}")
else
    # Old git: fall back to a plain shallow clone (full tree, one commit).
    git clone --quiet --depth 1 --branch "${UPSTREAM_BRANCH}" "${UPSTREAM_URL}" "${CLONE_DIR}"
fi

if [[ ! -d "${CLONE_DIR}" ]]; then
    echo "ERROR: failed to clone ${UPSTREAM_URL}."
    exit 1
fi

# ── Helper: copy upstream files into the repo ─────────────────────────────────
# Usage: copy_upstream_dir <remote_path> <local_dir> <file_extension>
copy_upstream_dir() {
    local remote_path="$1"
    local local_dir="$2"
    local file_ext="$3"

    local src_dir="${CLONE_DIR}/${remote_path}"
    if [[ ! -d "${src_dir}" ]]; then
        echo "  ERROR: ${remote_path} not found in the upstream clone."
        exit 1
    fi

    mkdir -p "${local_dir}"

    local count=0
    for f in "${src_dir}"/*"${file_ext}"; do
        [[ -e "${f}" ]] || continue
        echo "    ↓ $(basename "${f}")"
        cp -f "${f}" "${local_dir}/"
        count=$((count + 1))
    done

    echo "  Synced ${count} files to ${local_dir}"
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
    copy_upstream_dir "${CRD_REMOTE_PATH}" "${CRD_LOCAL_DIR}" ".yaml"
fi

if [[ "${UPDATE_TYPES}" == "true" ]]; then
    echo ""
    echo "--- Updating Go type definitions (agents.kruise.io) ---"
    copy_upstream_dir "${TYPES_REMOTE_PATH}" "${TYPES_LOCAL_DIR}" ".go"

    echo ""
    echo "--- Updating Go type definitions (security.agents.kruise.io) ---"
    copy_upstream_dir "${SECURITY_TYPES_REMOTE_PATH}" "${SECURITY_TYPES_LOCAL_DIR}" ".go"

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
