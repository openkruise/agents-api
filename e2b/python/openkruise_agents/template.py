# Copyright 2026 The Kruise Authors.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Custom /v1/templates protocol. No public client or separate URL configuration."""

import copy
import math
import re
import time
from typing import Any, List
from urllib.parse import urlsplit, urlunsplit

import httpx
from e2b import ConnectionConfig

from .errors import (
    TemplateAPIError,
    TemplateProtocolError,
    TemplateStateError,
    TemplateWaitTimeout,
)

# Private fixed origin port, not a new SDK connection option.
_PORT = 8081
_ALLOWED_CONNECTION_OPTIONS = {
    "api_key",
    "validate_api_key",
    "request_timeout",
    "headers",
    "api_headers",
    "access_token",
    "proxy",
    "logger",
}
_STATUSES = {"Unknown", "Pending", "Building", "Ready", "Failed", "Deleting"}
_NAME = re.compile(r"^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$")
_DEFAULT_OPTIONS = object()


def _metadata(value):
    """Validate shape/ownership locally; the backend validates Kubernetes syntax."""
    if not isinstance(value, dict):
        raise TypeError("metadata must be a dict containing labels and/or annotations")
    if set(value) - {"labels", "annotations"}:
        raise ValueError("metadata only accepts labels and annotations")
    for name, entries in value.items():
        if not isinstance(entries, dict):
            raise TypeError(f"metadata.{name} must be a string map")
        for key, entry in entries.items():
            if not isinstance(key, str) or not isinstance(entry, str):
                raise TypeError(f"metadata.{name} keys and values must be strings")
            domain, separator, _ = key.partition("/")
            domain = domain.lower()
            if separator and (
                domain == "agents.kruise.io" or domain.endswith(".agents.kruise.io")
            ):
                raise ValueError("metadata keys under agents.kruise.io are reserved")
    return copy.deepcopy(value)


def _config(options: dict) -> ConnectionConfig:
    unsupported = set(options) - _ALLOWED_CONNECTION_OPTIONS
    if unsupported:
        raise TypeError(
            f"unsupported Template connection options: {', '.join(sorted(unsupported))}"
        )
    values = dict(options)
    for key in ("headers", "api_headers"):
        if values.get(key) is not None:
            values[key] = dict(values[key])
    return ConnectionConfig(**values)


def _origin(api_url: str) -> str:
    try:
        parts = urlsplit(api_url)
        if parts.scheme not in ("http", "https") or not parts.hostname:
            raise ValueError("an absolute HTTP(S) E2B API URL is required")
        if parts.username is not None or parts.password is not None:
            raise ValueError("userinfo in the E2B API URL is not supported")
        # Force validation of an explicitly supplied port before replacing it.
        _ = parts.port
        host = parts.hostname
        if ":" in host:
            host = f"[{host}]"
        return urlunsplit((parts.scheme, f"{host}:{_PORT}", "", "", ""))
    except ValueError as exc:
        raise ValueError(f"invalid existing E2B API address: {exc}") from exc


def _name(name: str) -> str:
    if not isinstance(name, str) or not _NAME.fullmatch(name):
        raise ValueError("name must be a lowercase DNS label, 1-63 characters")
    return name


class _BorrowedTransport(httpx.BaseTransport):
    """HTTP wrappers close normally without closing E2B's shared connection pool."""

    def __init__(self, transport):
        self._transport = transport

    def handle_request(self, request):
        return self._transport.handle_request(request)

    def close(self):
        pass


def _api_client(config):
    from e2b.api import ApiClient
    from e2b.api.client_sync import get_transport

    return ApiClient(config, transport=_BorrowedTransport(get_transport(config)))


def _request(
    config: ConnectionConfig,
    method: str,
    path: str,
    *,
    body=None,
    timeout=None,
):
    # Clone the finalized E2B config; never mutate its URL, the global environment,
    # or Sandbox routes. Reuse E2B's auth/TLS/proxy/transport construction.
    custom = copy.copy(config)
    custom.api_url = _origin(config.api_url)
    api = _api_client(custom)
    http = api.get_httpx_client()
    request_timeout = config.request_timeout
    if timeout is not None:
        request_timeout = (
            min(request_timeout, timeout) if request_timeout is not None else timeout
        )
    # No application retry for POST or DELETE; ambiguous partial writes are left
    # for explicit user cleanup. Redirects never fall back to a legacy endpoint.
    try:
        response = http.request(
            method,
            path,
            json=body,
            timeout=request_timeout,
            follow_redirects=False,
        )
    finally:
        http.close()
    if response.status_code >= 300:
        try:
            error = response.json()
        except ValueError:
            error = {}
        if not isinstance(error, dict):
            error = {}
        message = error.get("message")
        if not isinstance(message, str):
            message = f"Template API returned HTTP {response.status_code}"
        raise TemplateAPIError(response.status_code, message, error.get("request_id"))
    return response


def _json(response: httpx.Response, expected_code: int):
    if response.status_code != expected_code:
        raise TemplateProtocolError(
            f"expected HTTP {expected_code}, got {response.status_code}"
        )
    try:
        return response.json()
    except ValueError as exc:
        raise TemplateProtocolError("Template API returned invalid JSON") from exc


class Template:
    """A minimal handle: id equals the name, which identifies one template.

    build/get/list/delete reuse existing E2B credentials and connection defaults.
    Only existing authentication/timeout/header/proxy options are accepted; URL,
    port, namespace, and client configuration are deliberately not added.
    """

    def __init__(
        self,
        *,
        name: str,
        template_id: str,
        status: str,
        message=None,
        metadata=None,
        _connection=None,
    ):
        self.name = name
        self.id = template_id
        self.status = status
        self.message = message
        self.metadata = {} if metadata is None else _metadata(metadata)
        self._connection = _connection

    def __repr__(self) -> str:
        return f"Template(name={self.name!r}, id={self.id!r}, status={self.status!r})"

    @classmethod
    def _parse(cls, value: Any, config: ConnectionConfig):
        if not isinstance(value, dict) or not {"name", "templateId", "status"}.issubset(
            value
        ):
            raise TemplateProtocolError(
                "Template response is missing name/templateId/status"
            )
        name, template_id, status = value["name"], value["templateId"], value["status"]
        if not isinstance(name, str) or not _NAME.fullmatch(name):
            raise TemplateProtocolError("invalid template name in response")
        if template_id != name:
            raise TemplateProtocolError("templateId in response must equal the name")
        if not isinstance(status, str) or status not in _STATUSES:
            raise TemplateProtocolError("invalid template status in response")
        message = value.get("message")
        if message is not None and not isinstance(message, str):
            raise TemplateProtocolError("invalid template message in response")
        try:
            metadata = _metadata(value["metadata"]) if "metadata" in value else {}
        except (TypeError, ValueError) as exc:
            raise TemplateProtocolError("invalid user metadata in response") from exc
        return cls(
            name=name,
            template_id=template_id,
            status=status,
            message=message,
            metadata=metadata,
            _connection=config,
        )

    @classmethod
    def build(
        cls,
        *,
        name: str,
        spec,
        metadata=_DEFAULT_OPTIONS,
        options=_DEFAULT_OPTIONS,
        **connection_options,
    ):
        from .models import SandboxTemplateSpec, TemplateBuildOptions, UNSET, to_wire

        _name(name)
        if not isinstance(spec, SandboxTemplateSpec):
            raise TypeError("spec must be SandboxTemplateSpec")
        config = _config(connection_options)
        body = {"name": name, "spec": spec.to_dict()}
        if metadata is not _DEFAULT_OPTIONS and metadata is not UNSET:
            body["metadata"] = None if metadata is None else _metadata(metadata)
        if options is not _DEFAULT_OPTIONS and options is not UNSET:
            if options is not None and not isinstance(options, TemplateBuildOptions):
                raise TypeError("options must be TemplateBuildOptions, None or omitted")
            body["options"] = to_wire(options)
        result = cls._parse(
            _json(_request(config, "POST", "/v1/templates", body=body), 201), config
        )
        if result.name != name or result.status not in {"Pending", "Building"}:
            raise TemplateProtocolError(
                "creation response must report the requested Pending or Building template"
            )
        return result

    @classmethod
    def _get(cls, name: str, config: ConnectionConfig, timeout=None):
        result = cls._parse(
            _json(
                _request(
                    config, "GET", "/v1/templates/" + _name(name), timeout=timeout
                ),
                200,
            ),
            config,
        )
        if result.name != name:
            raise TemplateProtocolError("response does not match the requested name")
        return result

    @classmethod
    def get(cls, name: str, **connection_options):
        return cls._get(name, _config(connection_options))

    @classmethod
    def list(cls, **connection_options) -> List["Template"]:
        config = _config(connection_options)
        value = _json(_request(config, "GET", "/v1/templates"), 200)
        if not isinstance(value, list):
            raise TemplateProtocolError("Template list response must be an array")
        return [cls._parse(item, config) for item in value]

    @staticmethod
    def delete(name: str, **connection_options) -> None:
        """Synchronous 204 only. A lost reply/503 does not cancel durable cleanup.
        Name-only retries are unsafe across same-name recreation; never replay
        an old uncertain deletion after rebuilding that name.
        """
        response = _request(
            _config(connection_options), "DELETE", "/v1/templates/" + _name(name)
        )
        if response.status_code != 204 or response.content:
            raise TemplateProtocolError(
                "Template deletion must return an empty HTTP 204"
            )

    def wait_until_ready(
        self, *, timeout: float = 120, interval: float = 2
    ) -> "Template":
        """Poll this template under a total monotonic time budget.
        Ready is the build state of the definition. Failed, Deleting, auth
        errors, and a server-confirmed 404 terminate immediately. Temporary
        transport/backend errors are retried; timeout has no side effects.
        """
        if (
            not math.isfinite(timeout)
            or timeout <= 0
            or not math.isfinite(interval)
            or interval <= 0
        ):
            raise ValueError("timeout and interval must be finite positive numbers")
        name = self.name
        config = self._connection or _config({})
        deadline = time.monotonic() + timeout
        while True:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise TemplateWaitTimeout(
                    f"Template {name!r} did not become Ready within {timeout}s"
                )
            try:
                current = self._get(name, config, timeout=remaining)
            except TemplateAPIError as exc:
                if exc.status_code not in {429, 500, 502, 503, 504}:
                    raise
            except (
                httpx.TimeoutException,
                httpx.NetworkError,
                httpx.RemoteProtocolError,
            ):
                pass
            else:
                if time.monotonic() >= deadline:
                    raise TemplateWaitTimeout(
                        f"Template {name!r} exceeded its wait budget"
                    )
                self.status, self.message = current.status, current.message
                self.metadata = copy.deepcopy(current.metadata)
                if current.status == "Ready":
                    return self
                if current.status in {"Failed", "Deleting"}:
                    raise TemplateStateError(name, current.status, current.message)
            remaining = deadline - time.monotonic()
            if remaining > 0:
                time.sleep(min(interval, remaining))
