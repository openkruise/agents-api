"""SandboxTemplate business models.

Kruise-owned Build fields (backend selector, phase, artifact, status) are
typed here. The Template spec — the whole ``PodSpec`` surface such as
``containers``, ``volumes``, scheduling and security — is NOT modeled: it
passes through verbatim as raw Kubernetes JSON on
:class:`SandboxTemplateSpec`, exactly the shapes the Kubernetes API itself
accepts. The server embeds a full ``v1.PodSpec`` and validates it, so the
SDK never duplicates or drifts from the Kubernetes schema.

Wire format: every declared field serializes to camelCase
(``model_dump(by_alias=True)``), matching the Kubernetes JSON convention
used by the sandbox-manager resource API; passthrough fields keep their
spelling unchanged.
"""

from enum import Enum
from typing import Any, Optional

from pydantic import BaseModel, ConfigDict

from ._common import camel_config


class TemplateBackend(str, Enum):
    """Backend selector for ``Template.build``, which defaults to substrate."""

    KUBERNETES = "k8s"
    SUBSTRATE = "substrate"


class SandboxTemplateSpec(BaseModel):
    """Public SandboxTemplate spec, passed through verbatim as raw JSON.

    Pass Kubernetes ``PodSpec`` fields exactly as you would to the
    Kubernetes API::

        SandboxTemplateSpec(
            containers=[{
                "name": "runtime",
                "image": "example/runtime@sha256:<digest>",
                "command": ["python", "-m", "runtime.server"],
                "resources": {"limits": {"cpu": "2", "memory": "4Gi"}},
            }],
        )
    """

    model_config = ConfigDict(extra="allow")


class SandboxTemplatePhase(str, Enum):
    """Lifecycle phase reported in ``SandboxTemplate.status``."""

    PENDING = "Pending"
    BUILDING = "Building"
    READY = "Ready"
    FAILED = "Failed"


class TemplateCheckpointRef(BaseModel):
    """Reference to the checkpoint object backing a k8s artifact."""

    model_config = camel_config

    name: str
    namespace: Optional[str] = None


class TemplateArtifactRef(BaseModel):
    """Current Build artifact; the single source of truth for Create.

    There is no public build identifier: 1 Template = 1 Build, the internal
    ActorTemplate carries the Template name itself.
    """

    model_config = camel_config

    backend: str
    checkpoint_ref: Optional[TemplateCheckpointRef] = None
    actor_template_name: Optional[str] = None


class SandboxTemplateArtifact(BaseModel):
    """One immutable artifact reference of the current Build, by type."""

    model_config = camel_config

    type: str
    name: str


class SandboxTemplateStatus(BaseModel):
    """Public projection of ``SandboxTemplate.status``, written exclusively by
    the SandboxTemplate controller."""

    model_config = {**camel_config, "extra": "allow"}

    observed_generation: Optional[int] = None
    phase: Optional[SandboxTemplatePhase] = None
    artifacts: Optional[list[SandboxTemplateArtifact]] = None
    message: Optional[str] = None
    start_time: Optional[str] = None
    ready_time: Optional[str] = None
    conditions: Optional[list[dict[str, Any]]] = None

    @property
    def artifact_ref(self) -> Optional[TemplateArtifactRef]:
        """The artifact reference derived from the typed artifact list.

        An ``ActorTemplate`` artifact is a substrate build; a ``Checkpoint``
        artifact is a k8s build. Templates without artifacts (Pending or
        rejected builds) have none.
        """
        for artifact in self.artifacts or []:
            if artifact.type == "ActorTemplate":
                return TemplateArtifactRef(backend="substrate", actor_template_name=artifact.name)
            if artifact.type == "Checkpoint":
                return TemplateArtifactRef(backend="k8s", checkpoint_ref=TemplateCheckpointRef(name=artifact.name))
        return None


class TemplateBuild(BaseModel):
    """Handle returned by ``Template.build()`` and refreshed while polling.

    The Template name is the Build identity: 1 Template = 1 Build.
    """

    model_config = {**camel_config, "extra": "allow"}

    template_id: str
    status: Optional[SandboxTemplatePhase] = None
    artifact: Optional[TemplateArtifactRef] = None
    message: Optional[str] = None


class TemplateProjection(BaseModel):
    """A Template as the resource API returns it: the flat spec echo plus the
    controller-converged Build status.

    ``spec`` and ``options`` are raw passthrough JSON — the same shapes the
    create request carried; ``status`` is typed.
    """

    model_config = {**camel_config, "extra": "allow"}

    name: str
    backend: Optional[str] = None
    spec: Optional[dict[str, Any]] = None
    options: Optional[dict[str, Any]] = None
    status: Optional[SandboxTemplateStatus] = None
