# Customized E2B SDK patch

This Python library patches the E2B client, converting the native E2B protocol to the OpenKruise Agents private
protocol, thereby simplifying sandbox-manager deployment.

## Installation

### Install from source via git

```bash
# Replace "main" with a specific version tag (e.g., "v0.1.0") from
# https://github.com/openkruise/agents-api/releases to pin a version tag.
pip install git+https://github.com/openkruise/agents-api.git@${VERSION}#subdirectory=e2b/python
```

### Install from Source

```bash
git clone https://github.com/openkruise/agents-api.git
cd e2b/python
pip install -e .
```

## Problem Statement

The E2B SDK requests the backend using the following protocol:

| Protocol                    | Description          | Example                                |
|-----------------------------|----------------------|----------------------------------------|
| api.E2B_DOMAIN              | Management interface | api.e2b.dev                            |
| \<port\>-\<sid\>.E2B_DOMAIN | Sandbox interface    | 49999-i37sc83s52e2cv85h636jjgs.e2b.dev |

Meanwhile, E2B SDK forces the use of HTTPS.

In our practice, we found that in K8s scenarios, this protocol has the following issues:

1. Requires configuring wildcard domain resolution to the management service (sandbox-manager), unable to use methods
   like hosts for resolution.
2. Requires using expensive wildcard certificates.

The above issues simultaneously make deploying a backend service compatible with E2B have a high threshold: not only
increasing user costs, but also making it difficult to automate the setup of an E2E test environment.

## Usage

Requirements:

- Python 3.9 or newer
- `e2b>=2.8.0`
- `e2b-code-interpreter>=2.4.1`

```python
from kruise_agents.patch_e2b import patch_e2b
from e2b_code_interpreter import Sandbox

patch_e2b(https=False)  # rewrite SDK URLs to the private protocol (see below)

if __name__ == "__main__":
    with Sandbox.create() as sbx:
        sbx.run_code("print('hello world')")
```

### What `patch_e2b` does

`patch_e2b` monkey-patches the installed E2B SDK in memory — no E2B code is
modified on disk. After the patch, every request the SDK sends targets the
private protocol instead of E2B's wildcard-domain protocol (see
[Problem Statement](#problem-statement) for why that matters):

| Traffic | E2B native protocol | After `patch_e2b` |
|---|---|---|
| Management API (create / kill / pause / ...) | `https://api.<E2B_DOMAIN>` | `<scheme>://<E2B_DOMAIN>/kruise/api` |
| Sandbox data plane (envd gRPC & HTTP, Jupyter) | `https://<port>-<sid>.<E2B_DOMAIN>` | `<scheme>://<E2B_DOMAIN>/kruise/<sid>/<port>` |

`<scheme>` is `https` by default and `http` when `https=False`.

Parameters:

- `https` — scheme used for both planes. `https=False` additionally forces
  the sandbox envd and Jupyter base URLs to plain HTTP, which is what
  certificate-less setups need (in-cluster Service URLs,
  `kubectl port-forward`).
- `validate_key` — set to `False` to disable the SDK's local API key format
  check. That check only accepts official `e2b_`-prefixed keys, while
  OpenKruise keys are plain UUIDs or admin keys; authentication itself
  happens server-side on sandbox-manager. Only works with `e2b>=2.25.0`.

Call `patch_e2b` once at startup, before creating or connecting any Sandbox.

For end-to-end setup (domain resolution, certificates, port-forward), see the
OpenKruise E2B client guide:
<https://openkruise.io/kruiseagents/user-manuals/e2b-client>

## API Key Compatibility

Official E2B SDKs validate the API key format locally. To feed a raw OpenKruise
Agents API key to such a client, wrap it first:

```python
from kruise_agents.keys import encode_for_e2b_sdk

api_key = encode_for_e2b_sdk("5b14a58f-93f4-4d3e-9a92-2f3e0e1a9e33")
```

The encoding is byte-for-byte compatible with the server-side implementation
in the sandbox-manager repository (`pkg/servers/e2b/keys/compat.go` in
openkruise/agents — the same file `kruise_agents/keys.py` must stay in sync
with); `tests/test_keys.py` locks the format with golden values.
`patch_e2b(validate_key=False)` bypasses the local format check
instead, so this helper is only needed for clients that keep validation
enabled.

## Traffic JWT Refresh

Traffic JWT refresh is an independent, opt-in monkey patch. It requires Python
3.10 or newer, `e2b>=2.35.0,<2.38.0`, and
`e2b-code-interpreter>=2.9.0,<2.10.0`.

```python
from kruise_agents.patch_e2b import patch_e2b
from kruise_agents.patch_traffic_token import patch_traffic_access_token

patch_e2b(https=False)
patch_traffic_access_token()
```

When a Sandbox is configured for Traffic JWT authentication, the patch keeps
its token in memory and refreshes it before expiration. Sync and async envd
HTTP/RPC requests and code-interpreter Jupyter requests read the latest token
immediately before sending. Refreshes for one Sandbox that overlap on the same
sandbox-manager replica are combined into one issuance. The completed result is
not cached by sandbox-manager; a later refresh request issues a new token.
Legacy opaque traffic tokens keep their existing behavior and do not enable
expiration-based refresh.

Connect only resumes or extends the Sandbox and does not issue a token. A
class-level `Sandbox.connect(sandbox_id)` checks the Sandbox metadata with
`get_info()`; for JWT-protected Sandboxes without a token, the first data-plane
request refreshes one before sending.

Sync and async clients refresh on demand immediately before a data-plane
request. Refresh failures continue using the previous token while it remains
valid; once expired, data-plane calls fail locally with
`TrafficAccessTokenExpired` instead of sending a known-invalid credential.

An application can explicitly refresh a token when needed:

```python
token = sandbox.refresh_traffic_access_token(force=True)
token = await async_sandbox.refresh_traffic_access_token(force=True)
```

## Rollout

Sandbox-manager preserves the legacy, approximately 100-year Traffic JWT
validity by default. Deploy this patch, or another client with equivalent
refresh support, before configuring a shorter
`--traffic-access-token-validity` for existing JWT-authenticated workloads.
Clients that do not refresh will lose data-plane access when a short-lived
token expires.

Deploy the lazy-Connect SDK behavior before upgrading sandbox-manager to a
version that no longer issues Traffic JWTs from Connect. Older clients cannot
recover a missing token when reconnecting by Sandbox ID.

## Development

Run the unit test suite with the dev extra:

```bash
cd e2b/python
pip install -e ".[dev]"
pytest tests/ -v
```

To exercise a specific SDK combination, pin either version:
`make test-e2b-patch E2B_VERSION=2.35.0 CODE_INTERPRETER_VERSION=2.9.0` from
the repository root. CI (`.github/workflows/test-e2b-python.yaml`) runs the
suite against every e2b version in the supported range on each pull request.



## Custom Template API (P7)

This is an additive protocol entrypoint, **not** E2B's `Template` build DSL.
Install the workload model extra without renaming the existing package:

```bash
pip install -e '.[custom-template]'
```

```python
from openkruise_agents import Sandbox, Template, patch_e2b

# Existing E2B_DOMAIN / E2B_API_KEY initialization, with an explicit team key.
# Native E2B-format keys can use kruise_agents.keys.encode_for_e2b_sdk.
patch_e2b(https=False, validate_key=False)  # Plaintext local deployment only.
template = Template.build(
    name=name, spec=spec,
    metadata={"labels": {"project": "analysis"},
              "annotations": {"description": "Python workload"}},
)
template.wait_until_ready(timeout=120, interval=2)
# Ready is the build state of the definition.
sandbox = Sandbox.create(template=template.id)
```

`examples/custom_template.py` runs the complete workflow and teardown
(`sandbox.kill()` then `Template.delete(name)`). A failed POST may need
explicit name cleanup after inspecting the error; do not delete a pre-existing
template merely because build returned 409.
Examples are opt-in and require a real deployment; unit tests use mock HTTP only.

### Routing and connections

The custom SDK derives an origin from the **final existing** E2B API URL:
keep HTTP/HTTPS and hostname, replace the port with internal `8081`, and remove
path, query, and fragment. IPv6 is supported. For example,
`https://example.com:8443/kruise/api` becomes `https://example.com:8081/v1/templates`.
Userinfo URLs are rejected. This does **not** mutate `E2B_API_URL`, install the
legacy patch implicitly, or change any instance/data-plane route.

Template requests reuse E2B authentication, headers, proxy, TLS verification,
and transport construction. Existing auth/timeout/header/proxy options may be
passed to operations; no public client, custom URL, port, namespace, or new
connection setting is added. A handle retains the connection snapshot for wait.
The old `kruise_agents` package and the native E2B/Sandbox entrypoints remain
unchanged. Workload models use the optional Kubernetes dependency, not a cluster
client; importing or serializing them does not load kubeconfig.

The deployment must actually expose port 8081. HTTPS requires TLS on that
frontend port. A Service/Ingress mapping 443 to backend 8081 does not alone make
`host:8081` reachable. There is no fallback to `/kruise/api` or a guessed gateway
path. Backend Service/TLS rollout and live acceptance belong to P8.

### Inputs and results

- The backend (Kubernetes or Substrate) is chosen by the API key's binding;
  requests carry no backend selector. A creation returns `Pending` or `Building`.
- Optional top-level `metadata` accepts a dictionary with `labels` and/or
  `annotations` string maps only, not native ObjectMeta. Unknown fields, null
  maps/values, non-string entries and keys under `agents.kruise.io` (including
  subdomains) are rejected. Omitted/UNSET metadata is not sent; `None` sends null.
  The backend validates Kubernetes key/value syntax and bounds the total size.
  Standard `app.kubernetes.io/name` is supported.
- The backend stores the metadata on the template object itself and never
  injects it into the Pod template. A key bound to the Substrate backend cannot
  carry metadata; the request fails with 400.

- `SandboxTemplateSpec` accepts the complete native `kubernetes.client.V1PodSpec`
  via `pod_spec`, or `containers` plus native PodSpec keyword fields. Sandbox
  policy covers runtimes, PVC templates, persistence and pause strategy. The
  HTTP spec is flat.
- `TemplateBuildOptions` carries the Kubernetes-backend build options:
  `checkpoint`, `imageCache`, `businessProcess` and `runCommands`, where a
  `RunCommand` is a shell string or an ordered list plus an optional `user`.
  The backend refuses each of them with 400 for a key bound to another backend.
  Standard container/volume/security fields use native Kubernetes model types,
  not a reduced image/CPU-only DSL.
- PVC input supports `metadata.name`/`spec` only. Kubernetes itself supports
  labels/annotations, but current backend PVC materialization does not propagate
  those fields; the API rejects them rather than accepting ineffective settings.
- `UNSET` omits a field, `None` emits null, and zero/False are preserved.
- Results contain `name`, `id` (wire `templateId`, which equals the name),
  `status`, and optional failure `message`; `metadata` is `{}` unless the server
  returns business metadata. GET may return `Failed` as normal data.
- `wait_until_ready` polls the template by name and shares one monotonic budget
  across requests/sleeps. It caps HTTP phase timeouts by the remaining budget and
  rejects late success. `Ready` succeeds; `Failed`/`Deleting`, auth failures and
  backend-confirmed 404 terminate. Temporary transport/backend errors are retried
  within the budget. Expiry raises `TemplateWaitTimeout` without
  cancelling/deleting resources.
- DELETE accepts a name and succeeds only on empty 204. There is no background
  retry of POST/DELETE: timeout/503 can leave partial creation or durable cleanup.
  Do **not** recreate an uncertainly deleted name and then replay its old DELETE;
  name-only deletion cannot distinguish a template recreated under the same name.
- API errors expose HTTP `status_code`, `message`, and optional `request_id`.
  Malformed success responses raise `TemplateProtocolError`; failed/deleting
  wait results raise `TemplateStateError`. Native HTTP transport errors remain
  available to callers of non-wait operations.

Run SDK tests without any cluster access:

```bash
pip install -e '.[dev,custom-template]'
pytest tests/test_custom_template.py tests/test_keys.py tests/test_patch_e2b.py tests/test_traffic_token.py
```
