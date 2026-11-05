"""KruiseTemplate resource API.

Create a logical Template and poll its Build::

    build = Template.build(name="runtime", spec=...)
    build.wait_until_ready()
"""

from __future__ import annotations

import time
from typing import Optional
from urllib.parse import quote

import httpx
from pydantic import PrivateAttr

from .client import KruiseClient, default_client
from .errors import KruiseApiError, TemplateBuildFailedError, WaitTimeoutError
from .models import (
    SandboxTemplatePhase,
    SandboxTemplateSpec,
    SandboxTemplateStatus,
    TemplateBackend,
    TemplateBuild,
)

# Transient polling failures (gateway errors / connection resets) are retried
# with exponential backoff before surfacing; Build polling runs for minutes.
_RETRYABLE_STATUS = frozenset({502, 503, 504})
_MAX_TRANSIENT_RETRIES = 5


class Template:
    """Entry point for building a logical SandboxTemplate."""

    @staticmethod
    def build(
        *,
        name: str,
        spec: SandboxTemplateSpec,
        backend: Optional[TemplateBackend] = TemplateBackend.SUBSTRATE,
        client: Optional[KruiseClient] = None,
    ) -> "TemplateBuildHandle":
        """Create the logical SandboxTemplate and start an async Build."""

        effective_client = client or default_client()
        body: dict[str, object] = {
            "name": name,
            "spec": spec.model_dump(by_alias=True, exclude_none=True),
        }
        if backend is not None:
            body["backend"] = backend.value

        response = effective_client.request("POST", "/templates", body=body)
        return TemplateBuildHandle._from_response(name, response, effective_client)


class TemplateBuildHandle(TemplateBuild):
    """``TemplateBuild`` plus the client context needed for polling."""

    _client: Optional[KruiseClient] = PrivateAttr(default=None)

    @classmethod
    def _from_response(
        cls,
        name: str,
        response: dict,
        client: Optional[KruiseClient] = None,
    ) -> "TemplateBuildHandle":
        status = response.get("status")
        if isinstance(status, dict):
            parsed = SandboxTemplateStatus.model_validate(status)
            handle = cls(
                template_id=name,
                status=parsed.phase,
                artifact=parsed.artifact_ref,
                message=parsed.message,
            )
        else:
            handle = cls(template_id=name)
        handle._client = client
        return handle

    def wait_until_ready(self, timeout: float = 600.0, interval: float = 2.0) -> "TemplateBuildHandle":
        """Poll ``GET /templates/{id}`` until phase is Ready.

        Transient polling failures (502/503/504 or connection errors) are
        retried with exponential backoff, up to ``_MAX_TRANSIENT_RETRIES``
        consecutive attempts, without exceeding the deadline.

        Raises :class:`TemplateBuildFailedError` when the Build reaches
        Failed, :class:`WaitTimeoutError` when ``timeout`` expires, and the
        last :class:`KruiseApiError` when retries are exhausted.
        """

        client = self._client or default_client()
        deadline = time.monotonic() + timeout
        transient_retries = 0
        backoff = interval
        while True:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise WaitTimeoutError(f"template {self.template_id}", "Ready", timeout)
            try:
                response = client.request(
                    "GET", f"/templates/{quote(self.template_id, safe='')}", timeout=remaining
                )
            except KruiseApiError as exc:
                if exc.status_code not in _RETRYABLE_STATUS:
                    raise
                transient_retries += 1
                if transient_retries > _MAX_TRANSIENT_RETRIES:
                    raise
            except httpx.TransportError:
                transient_retries += 1
                if transient_retries > _MAX_TRANSIENT_RETRIES:
                    raise KruiseApiError(503, f"transport error while polling template build: {transient_retries} consecutive failures")
            else:
                transient_retries = 0
                backoff = interval
                latest = TemplateBuildHandle._from_response(self.template_id, response)
                self.status = latest.status
                self.artifact = latest.artifact
                self.message = latest.message
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise WaitTimeoutError(f"template {self.template_id}", "Ready", timeout)
            if transient_retries == 0:
                if self.status is SandboxTemplatePhase.READY:
                    return self
                if self.status is SandboxTemplatePhase.FAILED:
                    raise TemplateBuildFailedError(self.template_id, self.message)
            delay = min(backoff, 30.0) if transient_retries else interval
            time.sleep(min(delay, remaining))
            if transient_retries:
                backoff = min(backoff * 2, 30.0)
