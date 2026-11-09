"""Public SDK models.

Kruise-owned Template build models are exported here. The Template spec is
intentionally NOT modeled: it passes through :class:`SandboxTemplateSpec`
as raw Kubernetes JSON (see :mod:`kruise_agents.models.template`).
"""

from .template import (
    SandboxTemplatePhase,
    SandboxTemplateSpec,
    SandboxTemplateStatus,
    TemplateArtifactRef,
    TemplateBackend,
    TemplateBuild,
    TemplateProjection,
)

__all__ = [
    "SandboxTemplatePhase",
    "SandboxTemplateSpec",
    "SandboxTemplateStatus",
    "TemplateArtifactRef",
    "TemplateBackend",
    "TemplateBuild",
    "TemplateProjection",
]
