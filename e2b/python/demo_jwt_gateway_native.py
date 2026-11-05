"""End-to-end demo of Traffic JWT over the native E2B protocol.

This is demo_jwt_gateway.py without patch_e2b: the SDK keeps its native
protocol (https://api.{E2B_DOMAIN} for the management API,
{port}-{sandbox_id}.{E2B_DOMAIN} for the data plane) and only the traffic
token patch runs on top of it — the two patches are independent (see README
"Combining the patches").

Deployment prerequisites:

1. jwt-e2e-oidc-provider deployed in sandbox-system, reachable in-cluster.
2. sandbox-gateway upgraded with:
     gateway.envoy.pluginConfig.enableJwtAuth=true
     gateway.envoy.oidc.discoveryUrl=https://jwt-e2e-oidc-provider.sandbox-system.svc:8443/...
     gateway.envoy.oidc.caConfigMap.namespace/name pointing at the provider CA.
3. kubectl on PATH with access to the sandbox resources.
4. Wildcard DNS + TLS for {port}-{sandbox_id}.{E2B_DOMAIN} pointing at the
   gateway, and api.{E2B_DOMAIN} (or E2B_API_URL) routed to sandbox-manager —
   the deployment costs patch_e2b exists to avoid.

Because the OSS sandbox-manager issues opaque UUID tokens, this demo mirrors
the upstream E2E suite: it issues a real JWT through the external provider and
injects it into the SDK client. Run on the Linux host:

    python demo_jwt_gateway_native.py
"""

import os
import subprocess
import sys
import time

import httpx

from e2b_code_interpreter import Sandbox

import kruise_agents.patch_traffic_token as patch_module
from kruise_agents.traffic_token import TrafficAccessToken, parse_expiration

if not os.environ.get("E2B_DOMAIN"):
    raise SystemExit("E2B_DOMAIN must be set to the deployment domain")

# Native protocol, JWT only — no patch_e2b. Point the management API at
# sandbox-manager through the SDK's own env vars instead; E2B_VALIDATE_API_KEY
# is the native equivalent of patch_e2b(validate_key=False). The data plane
# keeps {port}-{sandbox_id}.{E2B_DOMAIN} URLs and the SDK forces https there,
# so the deployment needs wildcard TLS (SSL_CERT_FILE selects the trusted CA
# bundle for both planes).
os.environ.setdefault("E2B_API_URL", f"https://api.{os.environ['E2B_DOMAIN']}")
os.environ.setdefault("E2B_VALIDATE_API_KEY", "false")
patch_module.patch_traffic_access_token()

PROVIDER_SERVICE = os.environ.get("JWT_PROVIDER_SERVICE", "jwt-e2e-oidc-provider")
PROVIDER_NAMESPACE = os.environ.get("JWT_PROVIDER_NAMESPACE", "sandbox-system")
FORWARD_PORT = int(os.environ.get("JWT_PROVIDER_FORWARD_PORT", "18080"))
ISSUE_URL = f"http://127.0.0.1:{FORWARD_PORT}/issue"
# Short-lived tokens make the automatic pre-expiry refresh observable.
TOKEN_VALIDITY_SECONDS = int(os.environ.get("JWT_TOKEN_VALIDITY_SECONDS", "65"))


def kubectl(*args: str) -> str:
    result = subprocess.run(
        ["kubectl", *args], capture_output=True, text=True, check=True
    )
    return result.stdout.strip()


def sandbox_uid(sandbox_id: str) -> str:
    namespace, name = sandbox_id.split("--", 1)
    return kubectl(
        "get", "sandbox", name, "-n", namespace, "-o", "jsonpath={.metadata.uid}"
    )


def issue_traffic_token(sandbox_id: str, sandbox_uid: str, validity: int) -> TrafficAccessToken:
    response = httpx.post(
        ISSUE_URL,
        json={
            "sandboxId": sandbox_id,
            "sandboxUid": sandbox_uid,
            "validitySeconds": validity,
        },
        timeout=10,
    )
    response.raise_for_status()
    body = response.json()
    return TrafficAccessToken(
        token=body["accessToken"],
        expires_at=parse_expiration(body["accessTokenExpiration"]),
    )


def client_with_traffic_jwt(sandbox: Sandbox, token: str) -> Sandbox:
    """Rebuild the client as if CreateSandbox had returned the issued JWT.

    Mirrors the upstream E2E helper: the OSS sandbox-manager returns an opaque
    UUID token, so the externally issued JWT is injected at client init, which
    is exactly the path the traffic-token patch hooks into.
    """
    return Sandbox(
        sandbox_id=sandbox.sandbox_id,
        sandbox_domain=sandbox.sandbox_domain,
        envd_version=sandbox._envd_version,
        envd_access_token=sandbox._envd_access_token,
        traffic_access_token=token,
        connection_config=sandbox.connection_config,
    )


def describe_token(token: str) -> str:
    from kruise_agents.traffic_token import expiration_from_jwt

    expires_at, issued_at = expiration_from_jwt(token)
    issued = f", issued {issued_at:%H:%M:%S}" if issued_at else ""
    return f"...{token[-16:]} (expires {expires_at:%H:%M:%S}{issued})"


print("[setup] creating sandbox with the JWT opt-in annotation")
sandbox = Sandbox.create(
    template="code-interpreter",
    timeout=600,
    metadata={patch_module._JWT_AUTH_METADATA_KEY: "true"},
)
print(f"sandbox created: {sandbox.sandbox_id}")
print(f"[setup] manager injected token from CreateSandbox: {sandbox.traffic_access_token!r}")
print("       (opaque UUID from the OSS sandbox-manager; the gateway rejects it)")

forward = subprocess.Popen(
    [
        "kubectl",
        "port-forward",
        f"svc/{PROVIDER_SERVICE}",
        f"{FORWARD_PORT}:8080",
        "-n",
        PROVIDER_NAMESPACE,
    ],
    stdout=subprocess.DEVNULL,
    stderr=subprocess.DEVNULL,
)
try:
    for _ in range(30):
        try:
            httpx.get(f"http://127.0.0.1:{FORWARD_PORT}/", timeout=1)
            break
        except httpx.HTTPError:
            time.sleep(1)
    else:
        raise RuntimeError("port-forward to the JWT provider did not become ready")

    uid = sandbox_uid(sandbox.sandbox_id)
    print(f"[setup] sandbox UID: {uid}")

    print("\n[1] data-plane with the opaque UUID token: the gateway must reject it")
    try:
        sandbox.commands.run("echo should-not-pass")
    except Exception as exc:  # noqa: BLE001 - the exact SDK error type is secondary
        print(f"    rejected as expected: {exc}")

    print("\n[2] data-plane with an externally issued Traffic JWT")
    issued = issue_traffic_token(sandbox.sandbox_id, uid, TOKEN_VALIDITY_SECONDS)
    print(f"    issued JWT: {describe_token(issued.token)}")
    jwt_client = client_with_traffic_jwt(sandbox, issued.token)
    result = jwt_client.commands.run("echo 'hello with traffic jwt'")
    print(f"    command output: {result.stdout.strip()}")
    execution = jwt_client.run_code("print('hello from jupyter with traffic jwt')")
    print(f"    jupyter output: {''.join(execution.logs.stdout).strip()}")

    print(f"\n[3] automatic refresh with {TOKEN_VALIDITY_SECONDS}s validity")
    print("    (the OSS manager's refresh endpoint returns UUIDs, so refresh is")
    print("     routed to the external issuer, which plays the role of the")
    print("     enterprise identity provider a JWT-enabled manager would use)")
    sid = sandbox.sandbox_id

    def external_refresh(_config, _sandbox_id):
        return issue_traffic_token(sid, uid, TOKEN_VALIDITY_SECONDS)

    patch_module._sync_refresh = external_refresh

    initial = issue_traffic_token(sandbox.sandbox_id, uid, TOKEN_VALIDITY_SECONDS)
    rotation_client = client_with_traffic_jwt(sandbox, initial.token)
    print(f"    initial JWT: {describe_token(initial.token)}")

    deadline = time.monotonic() + max(90, TOKEN_VALIDITY_SECONDS * 2)
    current = initial.token
    refreshes = 0
    while time.monotonic() < deadline:
        result = rotation_client.commands.run("echo keepalive")
        if result.stdout.strip() != "keepalive":
            raise RuntimeError("data plane broke during rotation watch")
        token = rotation_client.traffic_access_token
        if token != current:
            refreshes += 1
            print(f"    refreshed -> {describe_token(token)} (data plane still up)")
            current = token
            if refreshes >= 2:
                break
        time.sleep(2)
    print(f"    observed {refreshes} automatic refresh(es); data plane stayed alive")
finally:
    forward.terminate()
    try:
        forward.wait(timeout=5)
    except subprocess.TimeoutExpired:
        forward.kill()
    print("\n[cleanup] killing sandbox")
    try:
        sandbox.kill()
    except Exception as exc:  # noqa: BLE001 - best-effort cleanup
        print(f"    kill failed (sandbox may have timed out already): {exc}", file=sys.stderr)
