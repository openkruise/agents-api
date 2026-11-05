"""HTTP client for the sandbox-manager resource management API.

End users never talk to the Kubernetes API Server: every resource operation
(Template) goes through sandbox-manager's ``/agents.kruise.io/v1alpha1``
HTTP API with ``X-API-Key`` authentication. The server derives the
namespace from the API key's team; callers cannot select a namespace.
"""

from __future__ import annotations

import os
import threading
from typing import Any, Optional
from urllib.parse import urlsplit

import httpx

from .errors import api_error_from_response

API_PREFIX = "/agents.kruise.io/v1alpha1"

# The SDK deliberately reuses the E2B SDK environment variables instead of
# introducing its own: a deployment that already points the E2B SDK at
# sandbox-manager (see patch_e2b) needs no extra configuration here.
API_KEY_ENV = "E2B_API_KEY"
API_URL_ENV = "E2B_API_URL"
DEFAULT_BASE_URL = "http://127.0.0.1:8997"


def _origin_from_url(url: str) -> str:
    """Extract the origin (scheme://host:port) from an API URL.

    ``E2B_API_URL`` usually carries the E2B API path set by ``patch_e2b``
    (e.g. ``https://host/kruise/api``); the resource API lives on the same
    origin under ``/agents.kruise.io/v1alpha1``, so only the origin is kept.
    A bare origin (``http://host:8997``) passes through unchanged.
    """

    parts = urlsplit(url)
    return f"{parts.scheme}://{parts.netloc}"


class KruiseClient:
    """Synchronous client for the Kruise resource management API.

    Args:
        api_key: sandbox-manager API key. Falls back to ``E2B_API_KEY``.
        base_url: sandbox-manager origin (e.g. ``https://kruise.example.com``).
            Falls back to the origin extracted from ``E2B_API_URL``, then
            ``http://127.0.0.1:8997``.
        timeout: per-request timeout in seconds.
        http_client: optional preconfigured ``httpx.Client`` (e.g. with a
            custom transport for testing).
    """

    def __init__(
        self,
        api_key: Optional[str] = None,
        base_url: Optional[str] = None,
        timeout: float = 60.0,
        http_client: Optional[httpx.Client] = None,
    ) -> None:
        self.api_key = api_key or os.environ.get(API_KEY_ENV) or ""
        if base_url:
            self.base_url = base_url.rstrip("/")
        elif env_url := os.environ.get(API_URL_ENV):
            self.base_url = _origin_from_url(env_url)
        else:
            self.base_url = DEFAULT_BASE_URL
        self._client = http_client or httpx.Client(timeout=timeout)

    def request(
        self,
        method: str,
        path: str,
        body: Optional[dict[str, Any]] = None,
        *,
        timeout: Optional[float] = None,
    ) -> dict[str, Any]:
        """Send a request under the resource API prefix and return the JSON body.

        Raises a :class:`kruise_agents.errors.KruiseApiError` subclass for
        non-2xx responses.
        """

        url = f"{self.base_url}{API_PREFIX}{path}"
        headers = {}
        if self.api_key:
            headers["X-API-Key"] = self.api_key
        request_options: dict[str, Any] = {}
        if timeout is not None:
            request_options["timeout"] = httpx.Timeout(**{
                phase: min(value, timeout) if value is not None else timeout
                for phase, value in self._client.timeout.as_dict().items()
            })
        response = self._client.request(method, url, json=body, headers=headers, **request_options)
        payload: Optional[dict[str, Any]] = None
        raw_body: Optional[str] = None
        try:
            payload = response.json()
        except ValueError:
            raw_body = response.text[:512] if response.text else None
        if response.status_code >= 300:
            raise api_error_from_response(response.status_code, payload, raw_body)
        return payload if isinstance(payload, dict) else {}

    def close(self) -> None:
        self._client.close()

    def __enter__(self) -> "KruiseClient":
        return self

    def __exit__(self, *_exc: object) -> None:
        self.close()


_default_client: Optional[KruiseClient] = None
_default_client_lock = threading.Lock()


def default_client() -> KruiseClient:
    """Process-wide client built from the E2B environment variables (lazily created)."""

    global _default_client  # noqa: PLW0603
    if _default_client is None:
        with _default_client_lock:
            if _default_client is None:
                _default_client = KruiseClient()
    return _default_client
