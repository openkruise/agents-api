"""kruise_agents SDK.

Two layers:

- Resource management (this package's ``Template`` + ``models``): Kruise
  Python SDK calling the sandbox-manager resource API at
  ``/agents.kruise.io/v1alpha1``. End users never hold Kubernetes
  credentials.
- E2B compatibility (``patch_e2b`` and friends, imported by module path):
  monkey patches that point the community E2B SDK at sandbox-manager for
  Sandbox instance lifecycle.
"""

from .client import KruiseClient, default_client
from .errors import (
    AuthenticationError,
    BadRequestError,
    ConflictError,
    KruiseApiError,
    KruiseError,
    NotFoundError,
    PermissionDeniedError,
    TemplateBuildFailedError,
    UnsupportedOperationError,
    WaitTimeoutError,
)
from .template import Template, TemplateBuildHandle

__all__ = [
    # resource APIs
    "Template",
    "TemplateBuildHandle",
    # client
    "KruiseClient",
    "default_client",
    # errors
    "KruiseError",
    "KruiseApiError",
    "BadRequestError",
    "AuthenticationError",
    "PermissionDeniedError",
    "NotFoundError",
    "ConflictError",
    "UnsupportedOperationError",
    "TemplateBuildFailedError",
    "WaitTimeoutError",
]
