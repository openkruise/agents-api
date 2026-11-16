"""Custom protocol tests with real E2B config/HTTP construction, no live cluster."""

import inspect
import json
import os

import httpx
import pytest
from e2b import ConnectionConfig
from e2b_code_interpreter import Sandbox as E2BSandbox

k8s = pytest.importorskip("kubernetes.client")

from openkruise_agents import (
    Sandbox,
    Template,
    TemplateAPIError,
    TemplateProtocolError,
    TemplateStateError,
    TemplateWaitTimeout,
    patch_e2b,
)
from openkruise_agents import template as module
from openkruise_agents.models import (
    UNSET,
    AutoPausePolicy,
    CapacityPolicy,
    CapacityScalingRules,
    Container,
    CronScalingPolicy,
    ExecAction,
    IngressTrafficRule,
    LifecycleAction,
    PausePolicy,
    PauseStrategy,
    PodSpec,
    PoolAutoscalerOptions,
    Probe,
    ProbedIdleStateRule,
    ProbedScheduleTimeRule,
    ResumePolicy,
    RuntimeConfig,
    SandboxLifecycle,
    SandboxTemplateSpec,
    TemplateBuildOptions,
    VolumeClaimTemplate,
    WarmPoolOptions,
)

ID = "abcdefghijklmnopqrst"
OTHER_ID = "01234567890123456789"


def minimal_spec() -> SandboxTemplateSpec:
    return SandboxTemplateSpec(containers=[Container(name="main", image="python:3.12")])


def summary(status="Pending", template_id=ID, name="demo", **extra):
    return {"name": name, "templateId": template_id, "status": status, **extra}


@pytest.fixture
def mock_transport(monkeypatch):
    monkeypatch.setenv(
        "E2B_API_URL", "https://example.test:8443/kruise/api?old=1#fragment"
    )
    monkeypatch.setenv("E2B_DOMAIN", "example.test")
    monkeypatch.setenv("E2B_API_KEY", "team-key")
    monkeypatch.setenv("E2B_VALIDATE_API_KEY", "false")
    monkeypatch.setenv("E2B_DEBUG", "false")
    requests = []
    responder = [lambda _request: httpx.Response(200, json=summary("Ready"))]

    def handle(request):
        requests.append(request)
        return responder[0](request)

    transport = httpx.MockTransport(handle)
    from e2b.api import client_sync

    monkeypatch.setattr(client_sync, "get_transport", lambda _config: transport)
    return requests, responder


@pytest.mark.parametrize(
    "url,expected",
    [
        ("http://example.test/kruise/api", "http://example.test:8081"),
        ("https://example.test:443/other/path?x=1#f", "https://example.test:8081"),
        ("http://[::1]:8080/kruise/api", "http://[::1]:8081"),
        ("https://[2001:db8::1]/x", "https://[2001:db8::1]:8081"),
    ],
)
def test_origin_uses_final_e2b_url(url, expected):
    assert module._origin(url) == expected


@pytest.mark.parametrize(
    "url",
    [
        "example.test/path",
        "ftp://example.test",
        "https://user:secret@example.test",
        "http://example.test:bad",
    ],
)
def test_invalid_origin_fails_without_fallback(url):
    with pytest.raises(ValueError):
        module._origin(url)


def test_full_workflow_and_legacy_consumption(mock_transport, monkeypatch):
    requests, responder = mock_transport
    patch_e2b(https=False, validate_key=False)
    original_url = os.environ["E2B_API_URL"]
    observed = ["Pending", "Ready"]

    def serve(request):
        if request.url.port == 8081:
            assert request.headers["X-API-KEY"] == "team-key"
            if request.method == "POST":
                assert (
                    json.loads(request.content)["spec"]["containers"][0]["image"]
                    == "python:3.12"
                )
                return httpx.Response(201, json=summary())
            if request.method == "DELETE":
                return httpx.Response(204)
            if request.url.path == "/v1/templates":
                return httpx.Response(
                    200, json=[summary("Ready"), summary("Unknown", None, "empty")]
                )
            assert request.url.params["templateId"] == ID
            return httpx.Response(
                200, json=summary(observed.pop(0) if observed else "Ready")
            )
        # Real existing E2B Sandbox.create/kill; no custom consumption route.
        assert request.url.path.startswith("/kruise/api/sandboxes")
        if request.method == "POST":
            assert json.loads(request.content)["templateID"] == ID
            return httpx.Response(
                201,
                json={
                    "templateID": ID,
                    "sandboxID": "sbx-1",
                    "clientID": "client",
                    "envdVersion": "0.2.0",
                    "domain": "example.test",
                },
            )
        return httpx.Response(204)

    responder[0] = serve
    built = Template.build(
        name="demo",
        spec=minimal_spec(),
    )
    assert built.status == "Pending"
    assert built.wait_until_ready(timeout=1, interval=0.001) is built
    assert Template.get("demo", template_id=ID).id == ID
    assert len(Template.list()) == 2
    sandbox = Sandbox.create(template=built.id)
    assert sandbox.sandbox_id == "sbx-1"
    sandbox.kill()
    Template.delete("demo")
    assert os.environ["E2B_API_URL"] == original_url
    assert Sandbox is E2BSandbox
    assert requests[0].url.path == "/v1/templates"


@pytest.mark.skipif(
    "api_headers" not in inspect.signature(ConnectionConfig).parameters,
    reason="api_headers is not an existing option in this E2B version",
)
def test_auth_headers_transport_and_environment_preserved(mock_transport):
    requests, _ = mock_transport
    original = os.environ["E2B_API_URL"]
    Template.get(
        "demo",
        api_headers={"Authorization": "Bearer custom", "User-Agent": "test-agent"},
        access_token="fallback",
    )
    assert requests[0].headers["Authorization"] == "Bearer custom"
    assert requests[0].headers["User-Agent"] == "test-agent"
    assert requests[0].url.port == 8081
    assert requests[0].url.query == b""
    assert requests[0].url.fragment == ""
    assert os.environ["E2B_API_URL"] == original


def test_warm_pool_presence_and_all_policy_fields():
    assert TemplateBuildOptions().to_dict() == {}
    assert TemplateBuildOptions(warm_pool=None).to_dict() == {"warmPool": None}
    assert WarmPoolOptions().to_dict() == {}
    assert WarmPoolOptions(size=0, autoscaler=None).to_dict() == {
        "size": 0,
        "autoscaler": None,
    }
    assert WarmPoolOptions(enabled=False).to_dict() == {"enabled": False}
    for invalid in [
        WarmPoolOptions(enabled=False, size=0),
        WarmPoolOptions(enabled=False, size=None),
        WarmPoolOptions(enabled=False, autoscaler=None),
        WarmPoolOptions(size=None),
    ]:
        with pytest.raises(ValueError):
            invalid.to_dict()
    auto = PoolAutoscalerOptions(
        max_replicas=10,
        min_replicas=0,
        capacity_policy=CapacityPolicy(
            target_available="50%",
            tolerance=0,
            scale_up=CapacityScalingRules(stabilization_window_seconds=60),
            scale_down=CapacityScalingRules(stabilization_window_seconds=300),
        ),
        cron_policies=[CronScalingPolicy("peak", "0 9 * * *", 8, "UTC")],
    ).to_dict()
    assert auto == {
        "maxReplicas": 10,
        "minReplicas": 0,
        "capacityPolicy": {
            "targetAvailable": "50%",
            "tolerance": 0,
            "scaleUp": {"stabilizationWindowSeconds": 60},
            "scaleDown": {"stabilizationWindowSeconds": 300},
        },
        "cronPolicies": [
            {
                "name": "peak",
                "schedule": "0 9 * * *",
                "targetReplicas": 8,
                "timeZone": "UTC",
            }
        ],
    }


def test_full_podspec_pvc_and_sandbox_policy_are_flattened():
    spec = SandboxTemplateSpec(
        pod_spec=PodSpec(
            containers=[
                Container(
                    name="main",
                    image="python:3.12",
                    command=["sleep", "3600"],
                    env=[k8s.V1EnvVar(name="MODE", value="test")],
                    resources=k8s.V1ResourceRequirements(limits={"cpu": "1"}),
                    volume_mounts=[k8s.V1VolumeMount(name="data", mount_path="/data")],
                )
            ],
            init_containers=[Container(name="init", image="busybox:1.36")],
            node_selector={"role": "worker"},
            security_context=k8s.V1PodSecurityContext(run_as_user=1000),
            volumes=[
                k8s.V1Volume(
                    name="data",
                    persistent_volume_claim=k8s.V1PersistentVolumeClaimVolumeSource(
                        claim_name="data"
                    ),
                )
            ],
        ),
        volume_claim_templates=[
            VolumeClaimTemplate(
                "data",
                k8s.V1PersistentVolumeClaimSpec(
                    access_modes=["ReadWriteOnce"],
                    resources=k8s.V1VolumeResourceRequirements(
                        requests={"storage": "1Gi"}
                    ),
                ),
            )
        ],
        persistent_contents=["filesystem"],
        runtimes=[RuntimeConfig("agent-runtime")],
        pause_strategy=PauseStrategy(type="Stop"),
        probes=[
            Probe(
                "idle",
                k8s.V1Probe(
                    _exec=ExecAction(command=["echo", "idle"]), period_seconds=10
                ),
                "main",
            )
        ],
        auto_pause_policy=AutoPausePolicy(
            pause=PausePolicy(ProbedIdleStateRule("idle", "idle", "5m")),
            resume=ResumePolicy(
                ProbedScheduleTimeRule("next", "unix", "5m"), IngressTrafficRule("10m")
            ),
        ),
        lifecycle=SandboxLifecycle(
            pre_pause=LifecycleAction(
                exec=ExecAction(command=["sync"]), timeout_seconds=10
            )
        ),
    ).to_dict()
    assert "podSpec" not in spec and "template" not in spec
    assert spec["nodeSelector"] == {"role": "worker"}
    assert spec["containers"][0]["resources"]["limits"] == {"cpu": "1"}
    assert spec["volumeClaimTemplates"][0]["metadata"] == {"name": "data"}
    assert spec["probes"][0]["exec"]["command"] == ["echo", "idle"]
    assert spec["lifecycle"]["prePause"]["exec"]["command"] == ["sync"]
    assert (
        spec["autoPausePolicy"]["pause"]["whenProbedIdleState"]["thresholdDuration"]
        == "5m"
    )
    with pytest.raises(ValueError):
        SandboxTemplateSpec(pod_spec=PodSpec(containers=[]), containers=[])


@pytest.mark.parametrize(
    "options,expected",
    [
        (UNSET, "omitted"),
        (None, None),
        (TemplateBuildOptions(), {}),
        (
            TemplateBuildOptions(warm_pool=WarmPoolOptions(size=0)),
            {"warmPool": {"size": 0}},
        ),
    ],
)
def test_build_serialization_presence(mock_transport, options, expected):
    requests, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(201, json=summary())
    Template.build(
        name="demo",
        spec=minimal_spec(),
        options=options,
    )
    body = json.loads(requests[0].content)
    if expected == "omitted":
        assert "options" not in body
    else:
        assert body["options"] == expected


@pytest.mark.parametrize("status", ["Failed", "Deleting"])
def test_wait_terminal_state(mock_transport, status):
    _, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(
        200, json=summary(status, message="definition invalid")
    )
    handle = Template(name="demo", template_id=ID, status="Pending")
    with pytest.raises(TemplateStateError) as error:
        handle.wait_until_ready(timeout=1)
    assert error.value.status == status


@pytest.mark.parametrize("status", [401, 403, 404, 400, 409])
def test_wait_terminal_http_error(mock_transport, status):
    requests, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(
        status, json={"code": status, "message": "terminal", "request_id": "r1"}
    )
    with pytest.raises(TemplateAPIError) as error:
        Template(name="demo", template_id=ID, status="Pending").wait_until_ready(
            timeout=1
        )
    assert len(requests) == 1
    assert error.value.request_id == "r1"


def test_wait_retries_transient_and_binds_version(mock_transport):
    requests, responder = mock_transport
    attempts = [
        httpx.ConnectError("offline"),
        httpx.Response(503, json={"message": "retry"}),
        httpx.Response(200, json=summary("Ready")),
    ]

    def serve(_request):
        value = attempts.pop(0)
        if isinstance(value, Exception):
            raise value
        return value

    responder[0] = serve
    handle = Template(name="demo", template_id=ID, status="Pending")
    assert handle.wait_until_ready(timeout=1, interval=0.001) is handle
    assert all(request.url.params["templateId"] == ID for request in requests)
    assert all(request.method == "GET" for request in requests)


def test_wait_wrong_version_and_empty_claim(mock_transport):
    requests, responder = mock_transport
    empty = Template(name="empty", template_id=None, status="Unknown")
    with pytest.raises(ValueError):
        empty.wait_until_ready()
    assert not requests
    responder[0] = lambda _request: httpx.Response(200, json=summary("Ready", OTHER_ID))
    with pytest.raises(TemplateProtocolError):
        Template(name="demo", template_id=ID, status="Pending").wait_until_ready(
            timeout=1
        )


def test_wait_total_budget_caps_request_and_sleep(mock_transport, monkeypatch):
    requests, responder = mock_transport
    clock = [0.0]
    monkeypatch.setattr(module.time, "monotonic", lambda: clock[0])
    monkeypatch.setattr(
        module.time, "sleep", lambda delay: clock.__setitem__(0, clock[0] + delay)
    )
    responder[0] = lambda _request: httpx.Response(503)
    with pytest.raises(TemplateWaitTimeout):
        Template(name="demo", template_id=ID, status="Pending").wait_until_ready(
            timeout=3, interval=2
        )
    assert clock[0] == 3
    assert len(requests) == 2
    assert requests[0].extensions["timeout"]["read"] == 3
    assert requests[1].extensions["timeout"]["read"] == 1
    assert all(request.method == "GET" for request in requests)


@pytest.mark.parametrize(
    "payload",
    [
        {},
        {"name": "demo", "status": "Ready"},
        summary("invalid"),
        summary("Ready", "bad-id"),
        summary("Ready", None),
    ],
)
def test_invalid_success_response(mock_transport, payload):
    _, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(200, json=payload)
    with pytest.raises(TemplateProtocolError):
        Template.get("demo")


def test_failed_is_data_for_get_not_http_failure(mock_transport):
    _, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(
        200, json=summary("Failed", message="invalid")
    )
    assert Template.get("demo").status == "Failed"


def test_delete_does_not_retry_503_or_lost_reply(mock_transport):
    requests, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(
        503, json={"message": "cleanup continues"}
    )
    with pytest.raises(TemplateAPIError):
        Template.delete("demo")
    assert len(requests) == 1
    responder[0] = lambda _request: (_ for _ in ()).throw(
        httpx.ReadTimeout("lost response")
    )
    with pytest.raises(httpx.ReadTimeout):
        Template.delete("demo")
    assert len(requests) == 2


def test_no_public_url_client_or_namespace_options(mock_transport):
    requests, _ = mock_transport
    for option in ["api_url", "url", "port", "client", "namespace", "domain"]:
        with pytest.raises(TypeError):
            Template.get("demo", **{option: "forbidden"})
    assert not requests


def test_wait_keeps_handle_connection_snapshot(mock_transport, monkeypatch):
    requests, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(201, json=summary())
    handle = Template.build(
        name="demo",
        spec=minimal_spec(),
    )
    monkeypatch.setenv("E2B_API_URL", "http://unrelated.test/kruise/api")
    monkeypatch.setenv("E2B_API_KEY", "different-team")
    responder[0] = lambda _request: httpx.Response(200, json=summary("Ready"))
    handle.wait_until_ready(timeout=1, interval=0.001)
    assert requests[-1].url.host == "example.test"
    assert requests[-1].url.scheme == "https"
    assert requests[-1].headers["X-API-KEY"] == "team-key"


def test_late_ready_does_not_escape_wait_budget(mock_transport, monkeypatch):
    _, responder = mock_transport
    clock = [0.0]
    monkeypatch.setattr(module.time, "monotonic", lambda: clock[0])

    def serve(_request):
        clock[0] = 2
        return httpx.Response(200, json=summary("Ready"))

    responder[0] = serve
    with pytest.raises(TemplateWaitTimeout):
        Template(name="demo", template_id=ID, status="Pending").wait_until_ready(
            timeout=1
        )


def test_post_failure_is_not_retried_or_rolled_back(mock_transport):
    requests, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(
        503, json={"message": "partial creation"}
    )
    with pytest.raises(TemplateAPIError):
        Template.build(
            name="demo",
            spec=minimal_spec(),
        )
    assert len(requests) == 1 and requests[0].method == "POST"


@pytest.mark.parametrize(
    "response",
    [
        httpx.Response(204),
        httpx.Response(200, text="not-json"),
        httpx.Response(200, json={}),
    ],
)
def test_invalid_list_success(mock_transport, response):
    _, responder = mock_transport
    responder[0] = lambda _request: response
    with pytest.raises(TemplateProtocolError):
        Template.list()


def test_delete_requires_empty_204(mock_transport):
    _, responder = mock_transport
    responder[0] = lambda _request: httpx.Response(200, json={})
    with pytest.raises(TemplateProtocolError):
        Template.delete("demo")


def test_wait_handles_disabled_request_timeout(mock_transport):
    requests, _ = mock_transport
    handle = Template.get("demo", template_id=ID, request_timeout=0)
    handle.wait_until_ready(timeout=1)
    assert requests[0].extensions["timeout"]["read"] is None
    assert 0 < requests[-1].extensions["timeout"]["read"] <= 1


def test_config_does_not_mutate_caller_headers():
    headers = {"X-Custom": "value"}
    module._config({"headers": headers})
    assert headers == {"X-Custom": "value"}


def test_template_does_not_close_e2b_shared_transport(mock_transport):
    from e2b.api.client_sync import get_transport

    shared = get_transport(ConnectionConfig())
    closed = []
    shared.close = lambda: closed.append(True)
    Template.get("demo")
    Template.get("demo")
    assert not closed


@pytest.mark.parametrize(
    "metadata",
    [
        None,
        {},
        {"labels": {}},
        {
            "labels": {"app.kubernetes.io/name": "worker"},
            "annotations": {"description": "free text"},
        },
    ],
)
def test_template_metadata_workflow(mock_transport, metadata):
    requests, responder = mock_transport
    expected = metadata or {}

    def serve(request):
        if request.method == "POST":
            assert json.loads(request.content)["metadata"] == metadata
            return httpx.Response(201, json=summary(metadata=expected))
        if request.url.path == "/v1/templates":
            return httpx.Response(
                200,
                json=[
                    summary("Ready", metadata=expected),
                    summary("Unknown", None, "empty", metadata=expected),
                ],
            )
        return httpx.Response(200, json=summary("Ready", metadata=expected))

    responder[0] = serve
    handle = Template.build(name="demo", spec=minimal_spec(), metadata=metadata)
    assert handle.metadata == expected
    if metadata and metadata.get("labels"):
        handle.metadata["labels"]["app.kubernetes.io/name"] = "changed"
        assert metadata["labels"]["app.kubernetes.io/name"] == "worker"
    handle.wait_until_ready(timeout=2, interval=0.01)
    assert handle.metadata == expected
    assert Template.get("demo").metadata == expected
    assert [item.metadata for item in Template.list()] == [expected, expected]


@pytest.mark.parametrize(
    "metadata",
    [
        "scalar",
        [],
        {"name": "other"},
        {"namespace": "team"},
        {"uid": "fake"},
        {"ownerReferences": []},
        {"finalizers": []},
        {"Labels": {}},
        {"labels": None},
        {"annotations": []},
        {"labels": {1: "value"}},
        {"labels": {"app": 1}},
        {"annotations": {"app": None}},
        {"labels": {"agents.kruise.io/sandbox-id": "fake"}},
        {"annotations": {"sandbox.agents.kruise.io/hash-immutable-part": "fake"}},
        {"annotations": {"E2B.AGENTS.KRUISE.IO/secret": "fake"}},
    ],
)
def test_template_metadata_rejected_before_http(mock_transport, metadata):
    requests, _ = mock_transport
    with pytest.raises((TypeError, ValueError)):
        Template.build(name="demo", spec=minimal_spec(), metadata=metadata)
    assert requests == []


@pytest.mark.parametrize(
    "metadata",
    [
        None,
        [],
        {"namespace": "team"},
        {"labels": {"app": None}},
        {"annotations": {"agents.kruise.io/template-allocation": "secret"}},
    ],
)
def test_template_invalid_response_metadata(mock_transport, metadata):
    _, responder = mock_transport
    responder[0] = lambda request: httpx.Response(
        200, json=summary("Ready", metadata=metadata)
    )
    with pytest.raises(TemplateProtocolError):
        Template.get("demo")


def test_template_metadata_omitted_and_unset(mock_transport):
    requests, responder = mock_transport
    responder[0] = lambda request: httpx.Response(201, json=summary())
    for kwargs in ({}, {"metadata": UNSET}):
        handle = Template.build(name="demo", spec=minimal_spec(), **kwargs)
        assert "metadata" not in json.loads(requests[-1].content)
        assert handle.metadata == {}
