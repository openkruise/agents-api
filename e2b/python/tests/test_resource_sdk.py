"""Unit tests for the resource management SDK (Template).

All HTTP traffic is mocked with httpx.MockTransport; no server needed.
"""

from __future__ import annotations

import json

import httpx
import pytest

import kruise_agents.client as client_module
from kruise_agents import (
    ConflictError,
    Template,
    TemplateBuildFailedError,
    WaitTimeoutError,
)
from kruise_agents.client import API_PREFIX
from kruise_agents.models import SandboxTemplateSpec, TemplateBackend

API_BASE = "http://testserver"


def make_client(handler) -> httpx.Client:
    return httpx.Client(transport=httpx.MockTransport(handler))


def make_sdk_client(handler):
    from kruise_agents import KruiseClient

    return KruiseClient(api_key="test-key", base_url=API_BASE, http_client=make_client(handler))


class TestDefaultClient:
    """Users may omit ``client`` entirely: a lazy process-wide client is
    built from the E2B environment variables — no separate Kruise variables.
    """

    def test_default_client_reuses_e2b_env_and_is_singleton(self, monkeypatch):
        monkeypatch.setenv("E2B_API_KEY", "e2b-key")
        # patch_e2b sets E2B_API_URL to the E2B API path; the origin is kept.
        monkeypatch.setenv("E2B_API_URL", "http://env-server/kruise/api")
        monkeypatch.setattr(client_module, "_default_client", None)

        first = client_module.default_client()
        assert first.api_key == "e2b-key"
        assert first.base_url == "http://env-server"
        assert client_module.default_client() is first
        first.close()
        monkeypatch.setattr(client_module, "_default_client", None)

    def test_default_client_accepts_bare_origin(self, monkeypatch):
        monkeypatch.setenv("E2B_API_URL", "http://env-server:8997/")
        monkeypatch.setattr(client_module, "_default_client", None)

        first = client_module.default_client()
        assert first.base_url == "http://env-server:8997"
        first.close()
        monkeypatch.setattr(client_module, "_default_client", None)

    def test_default_client_defaults_without_env(self, monkeypatch):
        monkeypatch.delenv("E2B_API_KEY", raising=False)
        monkeypatch.delenv("E2B_API_URL", raising=False)
        monkeypatch.setattr(client_module, "_default_client", None)

        first = client_module.default_client()
        assert first.api_key == ""
        assert first.base_url == client_module.DEFAULT_BASE_URL
        first.close()
        monkeypatch.setattr(client_module, "_default_client", None)

    def test_explicit_args_override_env(self, monkeypatch):
        monkeypatch.setenv("E2B_API_KEY", "env-key")
        monkeypatch.setenv("E2B_API_URL", "http://env-server/kruise/api")
        explicit = client_module.KruiseClient(api_key="explicit", base_url="http://explicit-server")
        assert explicit.api_key == "explicit"
        assert explicit.base_url == "http://explicit-server"
        explicit.close()

    def test_build_without_client_uses_default_client(self, monkeypatch):
        captured = {}

        def handler(request: httpx.Request) -> httpx.Response:
            captured["auth"] = request.headers.get("X-API-Key")
            captured["url"] = str(request.url)
            return httpx.Response(
                201, json={"name": "no-client", "status": {"phase": "Building"}}
            )

        env_client = client_module.KruiseClient(
            api_key="env-key",
            base_url="http://env-server",
            http_client=make_client(handler),
        )
        monkeypatch.setattr("kruise_agents.template.default_client", lambda: env_client)

        build = Template.build(
            name="no-client",
            spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
        )
        assert captured["url"] == f"http://env-server{API_PREFIX}/templates"
        assert captured["auth"] == "env-key"
        assert build.template_id == "no-client"
        assert build.status is not None and build.status.value == "Building"
        env_client.close()


class TestTemplateBuild:
    def test_build_keyword_style_request_body(self):
        captured = {}

        def handler(request: httpx.Request) -> httpx.Response:
            captured["url"] = str(request.url)
            captured["auth"] = request.headers.get("X-API-Key")
            captured["body"] = json.loads(request.content)
            return httpx.Response(
                201,
                json={
                    "name": "code-interpreter-v2",
                    "status": {"phase": "Building"},
                },
            )

        client = make_sdk_client(handler)
        build = Template.build(
            name="code-interpreter-v2",
            spec=SandboxTemplateSpec(
                containers=[
                    {
                        "name": "runtime",
                        "image": "example/runtime@sha256:abc",
                        "command": ["python", "-m", "runtime.server"],
                        "imagePullPolicy": "IfNotPresent",
                        "resources": {
                            "requests": {"cpu": "1", "memory": "2Gi"},
                            "limits": {"cpu": "2", "memory": "4Gi"},
                        },
                    }
                ],
            ),
            client=client,
        )

        assert captured["url"] == f"{API_BASE}{API_PREFIX}/templates"
        assert captured["auth"] == "test-key"
        body = captured["body"]
        assert body["backend"] == "substrate"
        assert "options" not in body
        assert body["name"] == "code-interpreter-v2"
        container = body["spec"]["containers"][0]
        assert container["resources"]["requests"]["cpu"] == "1"
        # Kubernetes workload fields pass through verbatim, spelling included.
        assert container["imagePullPolicy"] == "IfNotPresent"
        assert build.template_id == "code-interpreter-v2"
        assert build.status is not None

    @pytest.mark.parametrize(
        ("backend", "expected_backend"),
        [(TemplateBackend.KUBERNETES, "k8s"), (TemplateBackend.SUBSTRATE, "substrate"), (None, None)],
    )
    def test_build_explicit_backend(self, backend, expected_backend):
        captured = {}

        def handler(request: httpx.Request) -> httpx.Response:
            captured["body"] = json.loads(request.content)
            return httpx.Response(201, json={"name": "rt", "status": {"phase": "Building"}})

        with make_sdk_client(handler) as client:
            Template.build(
                name="rt",
                backend=backend,
                spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
                client=client,
            )
        if expected_backend is None:
            assert "backend" not in captured["body"]
        else:
            assert captured["body"]["backend"] == expected_backend

    def test_build_duplicate_returns_conflict(self):
        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(409, json={"message": "template already exists"})

        with pytest.raises(ConflictError):
            Template.build(
                name="dup",
                spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "python:3.11"}]),
                client=make_sdk_client(handler),
            )

    def test_spec_metadata_and_unknown_fields_round_trip(self):
        captured = {}

        def handler(request: httpx.Request) -> httpx.Response:
            captured["body"] = json.loads(request.content)
            return httpx.Response(201, json={"name": "rt", "status": {"phase": "Building"}})

        Template.build(
            name="rt",
            spec=SandboxTemplateSpec(
                metadata={"labels": {"team": "platform"}, "annotations": {"tier": "gold"}},
                containers=[{"name": "main", "image": "i"}],
                terminationGracePeriodSeconds=30,
            ),
            client=make_sdk_client(handler),
        )
        spec = captured["body"]["spec"]
        assert spec["metadata"] == {"labels": {"team": "platform"}, "annotations": {"tier": "gold"}}
        assert spec["terminationGracePeriodSeconds"] == 30

    def test_wait_until_ready_polling(self, monkeypatch):
        phases = iter(["Building", "Building", "Ready"])
        requests_seen = []

        def handler(request: httpx.Request) -> httpx.Response:
            requests_seen.append(str(request.url))
            phase = next(phases)
            return httpx.Response(
                200,
                json={
                    "name": "rt",
                    "status": {
                        "phase": phase,
                        "artifactRef": {
                            "backend": "substrate",
                            "actorTemplateName": "rt",
                        },
                    },
                },
            )

        def fast_sleep(_seconds: float) -> None:
            return None

        monkeypatch.setattr("kruise_agents.template.time.sleep", fast_sleep)
        build = Template.build(
            name="rt",
            spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
            client=make_sdk_client(handler),
        )
        result = build.wait_until_ready(timeout=5, interval=0)
        assert result.status is not None and result.status.value == "Ready"
        assert result.artifact is not None and result.artifact.backend == "substrate"
        assert result.artifact.actor_template_name == "rt"
        assert len(requests_seen) == 3

    def test_wait_until_ready_failed_raises(self, monkeypatch):
        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(
                200,
                json={"name": "rt", "status": {"phase": "Failed", "message": "golden snapshot error"}},
            )

        monkeypatch.setattr("kruise_agents.template.time.sleep", lambda _: None)
        build = Template.build(
            name="rt",
            spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
            client=make_sdk_client(handler),
        )
        with pytest.raises(TemplateBuildFailedError):
            build.wait_until_ready(timeout=1, interval=0)

    @pytest.mark.parametrize(
        ("timeout", "interval", "expected_get_times"),
        [(0, 0, []), (1, 0.5, [0, 0.5]), (1, 2, [0])],
    )
    def test_wait_until_ready_timeout(self, monkeypatch, timeout, interval, expected_get_times):
        clock = [0.0]
        get_times = []

        def handler(request: httpx.Request) -> httpx.Response:
            if request.method == "GET":
                get_times.append(clock[0])
            return httpx.Response(200, json={"name": "rt", "status": {"phase": "Building"}})

        def sleep(seconds: float) -> None:
            clock[0] += seconds

        monkeypatch.setattr("kruise_agents.template.time.monotonic", lambda: clock[0])
        monkeypatch.setattr("kruise_agents.template.time.sleep", sleep)
        with make_sdk_client(handler) as client:
            build = Template.build(
                name="rt",
                spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
                client=client,
            )
            with pytest.raises(WaitTimeoutError):
                build.wait_until_ready(timeout=timeout, interval=interval)
        assert get_times == expected_get_times
        assert clock[0] == timeout

    def test_wait_until_ready_rejects_late_ready_response(self, monkeypatch):
        clock = [0.0]
        request_timeouts = []

        def handler(request: httpx.Request) -> httpx.Response:
            if request.method == "POST":
                return httpx.Response(201, json={"name": "rt", "status": {"phase": "Building"}})
            request_timeouts.append(request.extensions["timeout"])
            clock[0] += 2
            return httpx.Response(200, json={"name": "rt", "status": {"phase": "Ready"}})

        monkeypatch.setattr("kruise_agents.template.time.monotonic", lambda: clock[0])
        with client_module.KruiseClient(
            api_key="test-key",
            base_url=API_BASE,
            http_client=httpx.Client(
                transport=httpx.MockTransport(handler),
                timeout=httpx.Timeout(10, connect=0.25, write=0.5, pool=None),
            ),
        ) as client:
            build = Template.build(
                name="rt",
                spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
                client=client,
            )
            with pytest.raises(WaitTimeoutError):
                build.wait_until_ready(timeout=1, interval=2)
        assert request_timeouts == [{"connect": 0.25, "read": 1, "write": 0.5, "pool": 1}]

    @pytest.mark.parametrize(
        ("statuses", "expected_sleeps"),
        [
            ([503, 503, 200], [2, 4]),
            ([200, 200, 200, 200, 503, 200], [2, 2, 2, 2, 2]),
            ([503, 200, 503, 503, 200], [2, 2, 2, 4]),
        ],
    )
    def test_wait_until_ready_retries_transient_5xx(self, monkeypatch, statuses, expected_sleeps):
        clock = [0.0]
        sleeps = []
        get_responses = [
            httpx.Response(
                status,
                json={"message": "upstream unavailable"} if status == 503 else {
                    "name": "rt",
                    "status": {"phase": "Ready" if index == len(statuses) - 1 else "Building"},
                },
            )
            for index, status in enumerate(statuses)
        ]
        attempts = []

        def handler(request: httpx.Request) -> httpx.Response:
            attempts.append(request.method)
            if request.method == "POST":
                return httpx.Response(201, json={"name": "rt", "status": {"phase": "Building"}})
            return get_responses.pop(0)

        def sleep(seconds: float) -> None:
            sleeps.append(seconds)
            clock[0] += seconds

        monkeypatch.setattr("kruise_agents.template.time.monotonic", lambda: clock[0])
        monkeypatch.setattr("kruise_agents.template.time.sleep", sleep)
        with make_sdk_client(handler) as client:
            build = Template.build(
                name="rt",
                spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
                client=client,
            )
            result = build.wait_until_ready(timeout=60, interval=2)
        assert result.status is not None and result.status.value == "Ready"
        assert attempts == ["POST"] + ["GET"] * len(statuses)
        assert sleeps == expected_sleeps

    def test_wait_until_ready_gives_up_after_consecutive_5xx(self, monkeypatch):
        def handler(request: httpx.Request) -> httpx.Response:
            if request.method == "POST":
                return httpx.Response(201, json={"name": "rt", "status": {"phase": "Building"}})
            return httpx.Response(503, json={"message": "upstream unavailable"})

        monkeypatch.setattr("kruise_agents.template.time.sleep", lambda _: None)
        build = Template.build(
            name="rt",
            spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
            client=make_sdk_client(handler),
        )
        with pytest.raises(Exception) as exc_info:
            build.wait_until_ready(timeout=5, interval=0)
        assert exc_info.value.status_code == 503

    def test_non_json_error_body_preserved(self):
        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(
                500,
                content=b"<html>502 Bad Gateway from nginx</html>",
                headers={"content-type": "text/html"},
            )

        with pytest.raises(Exception) as exc_info:
            Template.build(
                name="rt",
                spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
                client=make_sdk_client(handler),
            )
        assert "nginx" in exc_info.value.message

    def test_auth_and_permission_error_mapping(self):
        from kruise_agents import AuthenticationError, PermissionDeniedError

        for status, exc_type in [(401, AuthenticationError), (403, PermissionDeniedError)]:
            def handler(request: httpx.Request, status=status) -> httpx.Response:
                return httpx.Response(status, json={"message": "denied"})

            with pytest.raises(exc_type):
                Template.build(
                    name="rt",
                    spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
                    client=make_sdk_client(handler),
                )

    def test_resource_name_url_encoded_in_get(self):
        seen_paths = []

        def handler(request: httpx.Request) -> httpx.Response:
            seen_paths.append(request.url.raw_path.decode())
            return httpx.Response(200, json={"name": "t", "status": {"phase": "Ready"}})

        build = Template.build(
            name="a:b/c",
            spec=SandboxTemplateSpec(containers=[{"name": "main", "image": "i"}]),
            client=make_sdk_client(handler),
        )
        build.wait_until_ready(timeout=1, interval=0)
        assert seen_paths[-1].endswith("/templates/a%3Ab%2Fc")


class TestTemplateResourceOperations:
    """GET / DELETE / list operations against the resource API."""

    def test_get_returns_projection(self):
        def handler(request: httpx.Request) -> httpx.Response:
            assert request.method == "GET"
            return httpx.Response(
                200,
                json={
                    "name": "rt",
                    "backend": "substrate",
                    "spec": {"containers": [{"name": "main", "image": "example/runtime@sha256:abc"}]},
                    "status": {"phase": "Ready", "artifactRef": {"backend": "substrate", "actorTemplateName": "rt"}},
                },
            )

        projection = Template.get("rt", client=make_sdk_client(handler))
        assert projection.name == "rt"
        assert projection.backend == "substrate"
        assert projection.spec is not None and projection.spec["containers"][0]["name"] == "main"
        assert projection.options is None
        assert projection.status is not None and projection.status.phase is not None
        assert projection.status.phase.value == "Ready"
        assert projection.status.artifact_ref is not None
        assert projection.status.artifact_ref.actor_template_name == "rt"

    def test_get_options_passthrough(self):
        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(
                200,
                json={"name": "rt", "backend": "k8s", "options": {"checkpoint": True}},
            )

        projection = Template.get("rt", client=make_sdk_client(handler))
        assert projection.options == {"checkpoint": True}

    def test_get_not_found(self):
        from kruise_agents import NotFoundError

        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(404, json={"message": "resource not found: rt"})

        with pytest.raises(NotFoundError):
            Template.get("rt", client=make_sdk_client(handler))

    def test_list_returns_projections(self):
        def handler(request: httpx.Request) -> httpx.Response:
            assert request.method == "GET"
            assert request.url.path == f"{API_PREFIX}/templates"
            return httpx.Response(
                200,
                json=[
                    {"name": "mine", "backend": "substrate", "status": {"phase": "Ready"}},
                    {"name": "shared", "backend": "k8s"},
                ],
            )

        templates = Template.list(client=make_sdk_client(handler))
        assert [t.name for t in templates] == ["mine", "shared"]
        assert templates[0].status is not None and templates[0].status.phase is not None
        assert templates[1].status is None

    def test_list_empty(self):
        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(200, json=[])

        assert Template.list(client=make_sdk_client(handler)) == []

    def test_list_tolerates_null_body(self):
        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(200, content=b"null")

        assert Template.list(client=make_sdk_client(handler)) == []

    def test_delete_returns_none_on_204(self):
        seen = []

        def handler(request: httpx.Request) -> httpx.Response:
            seen.append((request.method, request.url.raw_path.decode()))
            return httpx.Response(204)

        assert Template.delete("rt", client=make_sdk_client(handler)) is None
        assert seen == [("DELETE", f"{API_PREFIX}/templates/rt")]

    def test_delete_conflict_on_reference(self):
        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(409, json={"message": "template is referenced by sandboxsets"})

        with pytest.raises(ConflictError):
            Template.delete("rt", client=make_sdk_client(handler))

    def test_get_and_delete_url_encode_names(self):
        seen_paths = []

        def handler(request: httpx.Request) -> httpx.Response:
            seen_paths.append(request.url.raw_path.decode())
            if request.method == "GET":
                return httpx.Response(200, json={"name": "a:b/c"})
            return httpx.Response(204)

        client = make_sdk_client(handler)
        Template.get("a:b/c", client=client)
        Template.delete("a:b/c", client=client)
        assert seen_paths == [
            f"{API_PREFIX}/templates/a%3Ab%2Fc",
            f"{API_PREFIX}/templates/a%3Ab%2Fc",
        ]
