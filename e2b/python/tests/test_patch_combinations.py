"""Combination coverage for the two independent patches.

Matrix (patch_e2b x patch_traffic_access_token):

    patch_e2b   traffic token    behavior
    ---------   --------------   ---------------------------------------
    yes         yes              private protocol + JWT refresh (the
                                 default combination, covered by
                                 test_patch_e2b.py)
    no          yes              native protocol + JWT refresh: the token
                                 patch only reads api_url and injects
                                 headers, so it never needs patch_e2b
    yes         no               private protocol; tokens pass through
                                 unchanged and nothing refreshes them
    no          no               plain upstream SDK (out of scope)

The patch_e2b-only test must observe the SDK before the traffic token patch
is armed. pytest collects files alphabetically and this file sorts before
test_patch_e2b.py, so a full-suite run guarantees that; when the patch is
already armed (e.g. a hand-picked test order), that test skips itself.
"""

import base64
import json
import time
from datetime import datetime, timezone

import httpx
import pytest
from e2b import ConnectionConfig
from e2b_code_interpreter.code_interpreter_async import AsyncSandbox
from e2b_code_interpreter.code_interpreter_sync import Sandbox
from packaging.version import Version

import kruise_agents.patch_traffic_token as patch_module
from kruise_agents.patch_e2b import patch_e2b
from kruise_agents.traffic_token import TrafficAccessToken


def jwt(exp: float, iat: float) -> str:
    payload = base64.urlsafe_b64encode(
        json.dumps({"exp": exp, "iat": iat}).encode()
    ).rstrip(b"=")
    return f"header.{payload.decode()}.signature"


def config() -> ConnectionConfig:
    return ConnectionConfig(
        api_key="e2b_" + "0" * 40,
        validate_api_key=False,
        api_url="https://example.test/kruise/api",
        debug=True,
        extra_sandbox_headers={"E2b-Sandbox-Id": "sandbox-1"},
    )


def native_config() -> ConnectionConfig:
    # No /kruise prefix: what the SDK uses without patch_e2b.
    return ConnectionConfig(
        api_key="e2b_" + "0" * 40,
        validate_api_key=False,
        api_url="https://api.example.test",
        debug=True,
        extra_sandbox_headers={"E2b-Sandbox-Id": "sandbox-1"},
    )


def token_result(token: str, expires_at: float) -> TrafficAccessToken:
    return TrafficAccessToken(
        token,
        datetime.fromtimestamp(expires_at, timezone.utc),
    )


def test_patch_e2b_alone_keeps_traffic_tokens_native(monkeypatch):
    """patch_e2b without the traffic token patch: URLs are rewritten to the
    private protocol, but even a real JWT is stored and returned exactly as
    upstream does — no manager, no refresh entry point, no data-plane token
    hooks. Token management is armed solely by patch_traffic_access_token."""
    if patch_module._PATCHED:
        pytest.skip("traffic token patch already armed process-wide")
    monkeypatch.setenv("E2B_DOMAIN", "example.test")
    patch_e2b(validate_key=False)
    now = time.time()
    token = jwt(now + 3600, now)
    sandbox = Sandbox(
        sandbox_id="sandbox-1",
        sandbox_domain="example.test",
        envd_version=Version("0.2.0"),
        envd_access_token=None,
        traffic_access_token=token,
        connection_config=config(),
    )

    # patch_e2b did rewrite the URLs — routing and credentials are orthogonal.
    assert "kruise/sandbox-1" in sandbox.envd_api_url

    assert sandbox.traffic_access_token == token
    assert not hasattr(sandbox, patch_module._SYNC_MANAGER_ATTRIBUTE)
    assert not hasattr(sandbox, patch_module._ASYNC_MANAGER_ATTRIBUTE)
    assert not hasattr(
        sandbox.connection_config, patch_module._SYNC_MANAGER_ATTRIBUTE
    )
    assert not hasattr(type(sandbox), "refresh_traffic_access_token")

    envd_client = sandbox.files._envd_api
    assert not envd_client.event_hooks["request"]
    envd_client.close()

    jupyter_client = sandbox._client
    assert not jupyter_client.event_hooks["request"]
    jupyter_client.close()


def test_traffic_token_patch_alone_supports_native_protocol(monkeypatch):
    """patch_traffic_access_token without patch_e2b: the refresh endpoint
    follows the native api_url, and every data-plane surface still reads the
    latest token before sending."""
    monkeypatch.delenv("E2B_API_URL", raising=False)
    patch_module.patch_traffic_access_token()
    now = time.time()
    refreshed_token = jwt(now + 7200, now)
    seen = []

    def refresh(config_arg, sandbox_id):
        seen.append((config_arg.api_url, sandbox_id))
        return token_result(refreshed_token, now + 7200)

    monkeypatch.setattr(patch_module, "_sync_refresh", refresh)
    sandbox = Sandbox(
        sandbox_id="sandbox-1",
        sandbox_domain="example.test",
        envd_version=Version("0.2.0"),
        envd_access_token=None,
        traffic_access_token=jwt(now + 3600, now),
        connection_config=native_config(),
    )

    assert patch_module._refresh_url(native_config(), "sandbox-1") == (
        "https://api.example.test/sandboxes/sandbox-1/traffic-access-token"
    )
    assert sandbox.refresh_traffic_access_token(force=True) == refreshed_token
    assert seen == [("https://api.example.test", "sandbox-1")]
    assert sandbox.traffic_access_token == refreshed_token

    request = httpx.Request("POST", "https://49983-sandbox-1.example.test")
    patch_module._sync_request_hook(sandbox.connection_config)(request)
    assert request.headers[patch_module._TRAFFIC_TOKEN_HEADER] == refreshed_token

    envd_client = sandbox.files._envd_api
    assert envd_client.event_hooks["request"]
    envd_client.close()

    class Context:
        def __init__(self):
            self.request_headers = {}

    context = Context()
    interceptor = patch_module._TrafficTokenInterceptor(sandbox.connection_config)
    assert (
        interceptor.intercept_unary_sync(
            lambda request, _ctx: request, "rpc-result", context
        )
        == "rpc-result"
    )
    assert (
        context.request_headers[patch_module._TRAFFIC_TOKEN_HEADER] == refreshed_token
    )

    jupyter_client = sandbox._client
    assert jupyter_client.event_hooks["request"]
    jupyter_client.close()


@pytest.mark.asyncio
async def test_async_traffic_token_patch_alone_supports_native_protocol(monkeypatch):
    monkeypatch.delenv("E2B_API_URL", raising=False)
    patch_module.patch_traffic_access_token()
    now = time.time()
    refreshed_token = jwt(now + 7200, now)
    seen = []

    async def refresh(config_arg, sandbox_id):
        seen.append((config_arg.api_url, sandbox_id))
        return token_result(refreshed_token, now + 7200)

    monkeypatch.setattr(patch_module, "_async_refresh", refresh)
    sandbox = AsyncSandbox(
        sandbox_id="sandbox-1",
        sandbox_domain="example.test",
        envd_version=Version("0.2.0"),
        envd_access_token=None,
        traffic_access_token=jwt(now + 3600, now),
        connection_config=native_config(),
    )

    assert await sandbox.refresh_traffic_access_token(force=True) == refreshed_token
    assert seen == [("https://api.example.test", "sandbox-1")]
    assert sandbox.traffic_access_token == refreshed_token

    request = httpx.Request("POST", "https://49999-sandbox-1.example.test")
    await patch_module._async_request_hook(sandbox.connection_config)(request)
    assert request.headers[patch_module._TRAFFIC_TOKEN_HEADER] == refreshed_token

    jupyter_client = sandbox._client
    assert jupyter_client.event_hooks["request"]
    await jupyter_client.aclose()

    await sandbox._envd_api.aclose()
