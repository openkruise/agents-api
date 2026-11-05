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

### Combining the patches

The two patches are independent: `patch_e2b` decides where requests are sent
(URL routing), while `patch_traffic_access_token` decides how the Traffic JWT
is stored, refreshed, and attached to requests. They combine freely:

| `patch_e2b` | `patch_traffic_access_token` | Result |
|-------------|------------------------------|--------|
| yes | yes | Private protocol + JWT refresh (the combination above) |
| no | yes | Native E2B protocol + JWT refresh (below) |
| yes | no | Private protocol; tokens pass through as-is, nothing refreshes them |
| no | no | Plain upstream SDK |

To run Traffic JWT refresh on the native protocol, skip `patch_e2b` and point
the SDK at sandbox-manager through its own configuration. The refresh endpoint
is derived from `api_url`
(`POST {api_url}/sandboxes/{sandbox_id}/traffic-access-token`), so it follows
whichever protocol the client speaks:

```python
import os

# Management API: sandbox-manager's native entry point.
os.environ["E2B_API_URL"] = "https://api.your-domain.com"
# OpenKruise keys are plain UUIDs; skip the SDK's local e2b_ format check
# (the native equivalent of patch_e2b(validate_key=False)).
os.environ["E2B_VALIDATE_API_KEY"] = "false"

from kruise_agents.patch_traffic_token import patch_traffic_access_token

patch_traffic_access_token()
```

A native-protocol deployment still needs wildcard DNS and TLS for the data
plane (`{port}-{sandbox_id}.{domain}` subdomains) — the deployment costs
`patch_e2b` exists to avoid. The two combinations are exercised end-to-end by
`demo_jwt_gateway.py` (private protocol) and `demo_jwt_gateway_native.py`
(native protocol, traffic token patch only).

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

