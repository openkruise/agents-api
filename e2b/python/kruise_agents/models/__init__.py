"""Public SDK models.

Kruise-owned Template build models are exported here. The Template spec is
intentionally NOT modeled: it passes through :class:`SandboxTemplateSpec`
as raw Kubernetes JSON (see :mod:`kruise_agents.models.template`).
"""

from .template import (
    SandboxTemplateArtifact,
    SandboxTemplatePhase,
    SandboxTemplateSpec,
    SandboxTemplateStatus,
    TemplateArtifactRef,
    TemplateBackend,
    TemplateBuild,
    TemplateCheckpointRef,
    TemplateProjection,
)

__all__ = [
    "SandboxTemplateArtifact",
    "SandboxTemplatePhase",
    "SandboxTemplateSpec",
    "SandboxTemplateStatus",
    "TemplateArtifactRef",
    "TemplateBackend",
    "TemplateBuild",
    "TemplateCheckpointRef",
    "TemplateProjection",
]
