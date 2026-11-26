#!/usr/bin/env bash
set -o errexit
set -o nounset
set -o pipefail

# Usage:
#   ./hack/generate_client.sh                # update upstream + generate
#   ./hack/generate_client.sh --skip-update  # skip upstream update, generate only

# Generates clientset, listers and informers in module mode: the code
# generators are pinned as tool directives in tools.mod and run via
# `GOWORK=off go run -mod=readonly -modfile=tools.mod`, so no `go mod
# vendor`, GOBIN, GOPATH staging, or GO111MODULE=off flow is involved.
# GOWORK=off and -mod=readonly resolve the generators from tools.mod,
# bypassing any active Go workspace (incompatible with -modfile) and the
# main module's vendor tree (which lacks the tool-only dependencies).
#
# Deepcopy (zz_generated.deepcopy.go) is NOT generated here: it is synced
# verbatim from the upstream openkruise/agents repository by
# hack/update_upstream.sh (upstream generates it with controller-gen), so
# regenerating it locally would only drift the license headers.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${SCRIPT_ROOT}"

# The generators shell out to `go list` sub-processes when parsing the input
# packages; those inherit the environment but not the -mod flag, so a stale
# vendor tree in the main module breaks them with "inconsistent vendoring".
# GOFLAGS=-mod=readonly propagates the vendor bypass to every nested go
# command, matching the -mod=readonly used for the generator invocations.
GOFLAGS="${GOFLAGS:+${GOFLAGS} }-mod=readonly"
export GOFLAGS

SKIP_UPDATE=false
for arg in "$@"; do
    case "${arg}" in
        --skip-update) SKIP_UPDATE=true ;;
        *) echo "Unknown argument: ${arg}" >&2; exit 1 ;;
    esac
done

if [[ "${SKIP_UPDATE}" == "false" ]]; then
    echo "==> Updating upstream definitions (CRD + Go types)..."
    "${SCRIPT_DIR}/update_upstream.sh"
fi

BOILERPLATE="${SCRIPT_ROOT}/hack/boilerplate.go.txt"
OUTPUT_PKG="github.com/openkruise/agents-api/client"
PLURAL_EXCEPTIONS="SandboxUpdateOps:sandboxupdateops"
V="${KUBE_VERBOSE:-0}"

# Both API roots are scanned for +genclient tagged packages: agents/ (the
# agents.kruise.io group) and security/ (the security.agents.kruise.io
# group, which lives at the repo top level and only exists after
# hack/update_upstream.sh has synced it; guard so --skip-update runs on a
# fresh checkout don't fail on the missing directory). This mirrors the
# discovery logic of kube_codegen.sh's gen_client.
api_roots=()
for root in agents security; do
    if [[ -d "${SCRIPT_ROOT}/${root}" ]]; then
        api_roots+=("${root}")
    else
        echo "Skipping missing API root: ${root}"
    fi
done

if [ "${#api_roots[@]}" == 0 ]; then
    printf '%s\n' "No API roots found under ${SCRIPT_ROOT}" >&2
    exit 1
fi

input_pkgs=()
for root in "${api_roots[@]}"; do
    while read -r dir; do
        # -mod=readonly ignores the main module's vendor directory: a stale
        # vendor tree (e.g. left behind by the pre-tools.mod flow) must not
        # break package import-path resolution with "inconsistent
        # vendoring" errors.
        pkg="$(cd "${dir}" && go list -mod=readonly -find .)"
        input_pkgs+=("${pkg}")
    done < <(
        ( grep -l --null \
            -e '^\s*//\s*+genclient' \
            -r "${SCRIPT_ROOT}/${root}" \
            --include '*.go' \
            --exclude-dir .git \
            --exclude-dir _output \
            --exclude-dir vendor \
            || true
        ) | while read -r -d $'\0' F; do dirname "${F}"; done \
          | LC_ALL=C sort -u \
          | while read -r dir; do
                leaf="$(basename "${dir}")"
                if grep -E -q '^v[0-9]+((alpha|beta)[0-9]+)?$' <<< "${leaf}"; then
                    echo "${dir}"
                fi
            done
    )
done

if [ "${#input_pkgs[@]}" == 0 ]; then
    printf '%s\n' "No +genclient tagged APIs found under ${api_roots[*]}" >&2
    exit 1
fi

inputs=()
for pkg in "${input_pkgs[@]}"; do
    inputs+=(--input "${pkg}")
done

# Generate into a staging directory and publish only complete output.
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

echo "Generating client code for ${#input_pkgs[@]} targets"
GOWORK=off go run -mod=readonly -modfile=tools.mod k8s.io/code-generator/cmd/client-gen \
    -v "${V}" \
    --go-header-file "${BOILERPLATE}" \
    --output-dir "${TMP_DIR}/clientset" \
    --output-pkg "${OUTPUT_PKG}/clientset" \
    --clientset-name "versioned" \
    --apply-configuration-package "" \
    --input-base "" \
    --plural-exceptions "${PLURAL_EXCEPTIONS}" \
    --prefers-protobuf=false \
    "${inputs[@]}"

echo "Generating lister code for ${#input_pkgs[@]} targets"
GOWORK=off go run -mod=readonly -modfile=tools.mod k8s.io/code-generator/cmd/lister-gen \
    -v "${V}" \
    --go-header-file "${BOILERPLATE}" \
    --output-dir "${TMP_DIR}/listers" \
    --output-pkg "${OUTPUT_PKG}/listers" \
    --plural-exceptions "${PLURAL_EXCEPTIONS}" \
    "${input_pkgs[@]}"

echo "Generating informer code for ${#input_pkgs[@]} targets"
GOWORK=off go run -mod=readonly -modfile=tools.mod k8s.io/code-generator/cmd/informer-gen \
    -v "${V}" \
    --go-header-file "${BOILERPLATE}" \
    --output-dir "${TMP_DIR}/informers" \
    --output-pkg "${OUTPUT_PKG}/informers" \
    --versioned-clientset-package "${OUTPUT_PKG}/clientset/versioned" \
    --listers-package "${OUTPUT_PKG}/listers" \
    --plural-exceptions "${PLURAL_EXCEPTIONS}" \
    "${input_pkgs[@]}"

rm -rf "${SCRIPT_ROOT}/client/clientset" "${SCRIPT_ROOT}/client/informers" "${SCRIPT_ROOT}/client/listers"
mkdir -p "${SCRIPT_ROOT}/client"
cp -R "${TMP_DIR}/clientset" "${TMP_DIR}/listers" "${TMP_DIR}/informers" "${SCRIPT_ROOT}/client/"
