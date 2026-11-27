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

"""Typed custom-template declarations; native Kubernetes workload types stay intact."""

from dataclasses import dataclass, fields
from typing import Any, List, Optional, TypeVar, Union

try:
    from kubernetes import client as k8s
except ImportError as exc:
    raise ImportError(
        "Custom Template models require: pip install 'kruise-agents[custom-template]'"
    ) from exc


class _Unset:
    def __repr__(self) -> str:
        return "UNSET"


T = TypeVar("T")
Omittable = Union[T, _Unset, None]

UNSET = _Unset()
"""Omit a field. None emits JSON null; zero and False remain explicit values."""


def _camel(name: str) -> str:
    first, *rest = name.split("_")
    return first + "".join(part.capitalize() for part in rest)


def to_wire(value: Any) -> Any:
    """Serialize declarations without losing omission/null/zero distinctions."""
    if isinstance(value, _Model):
        return value.to_dict()
    if isinstance(value, (list, tuple)):
        return [to_wire(item) for item in value]
    if isinstance(value, dict):
        return {key: to_wire(item) for key, item in value.items() if item is not UNSET}
    # Kubernetes 37+ uses Pydantic; older clients use generated attribute maps.
    # No ApiClient, kubeconfig loading, or Kubernetes connection is necessary.
    if hasattr(value, "model_dump"):
        return value.model_dump(mode="json", by_alias=True, exclude_unset=True)
    if hasattr(value, "attribute_map"):
        return {
            alias: to_wire(getattr(value, name))
            for name, alias in value.attribute_map.items()
            if getattr(value, name) is not None
        }
    if value is UNSET:
        raise ValueError("UNSET cannot be serialized as a standalone value")
    return value


class _Model:
    def to_dict(self) -> dict:
        return {
            _camel(field.name): to_wire(getattr(self, field.name))
            for field in fields(self)
            if getattr(self, field.name) is not UNSET
        }


# Native types expose the whole PodSpec rather than a reduced image/CPU DSL.
Container = k8s.V1Container
PodSpec = k8s.V1PodSpec


@dataclass
class RuntimeConfig(_Model):
    name: str


@dataclass
class HibernateStrategy(_Model):
    type: str = "Snapshot"


@dataclass
class PauseStrategy(_Model):
    type: Omittable[str] = UNSET
    hibernate_strategy: Omittable[HibernateStrategy] = UNSET


@dataclass
class VolumeClaimTemplate(_Model):
    """Only metadata.name/spec are accepted; PVC labels/annotations are rejected."""

    name: str
    spec: k8s.V1PersistentVolumeClaimSpec

    def to_dict(self) -> dict:
        return {"metadata": {"name": self.name}, "spec": to_wire(self.spec)}


@dataclass
class RunCommand(_Model):
    """A shell string or an ordered list the backend joins with &&."""

    command: Union[str, List[str]]
    user: Omittable[Optional[str]] = UNSET


@dataclass
class BusinessProcessOptions(_Model):
    start_policy: str = "after_claim_initialization"


@dataclass
class TemplateBuildOptions(_Model):
    """Build options of the Kubernetes backend, which refuses every field for
    another backend.
    """

    checkpoint: Omittable[bool] = UNSET
    image_cache: Omittable[bool] = UNSET
    business_process: Omittable[BusinessProcessOptions] = UNSET
    run_commands: Omittable[List[RunCommand]] = UNSET


class SandboxTemplateSpec(_Model):
    """Flat PodSpec plus Sandbox policy. Pass a complete native pod_spec or
    containers and native PodSpec keyword fields (node_selector, volumes, etc.).
    Build options travel in the request's options; backend and outer metadata
    are not part of the spec.
    """

    def __init__(
        self,
        *,
        pod_spec: Optional[k8s.V1PodSpec] = None,
        containers: Optional[List[k8s.V1Container]] = None,
        volume_claim_templates: Omittable[List[VolumeClaimTemplate]] = UNSET,
        persistent_contents: Omittable[List[str]] = UNSET,
        runtimes: Omittable[List[RuntimeConfig]] = UNSET,
        pause_strategy: Omittable[PauseStrategy] = UNSET,
        **pod_fields: Any,
    ):
        if pod_spec is not None:
            if containers is not None or pod_fields:
                raise ValueError(
                    "pod_spec cannot be mixed with containers or PodSpec keyword fields"
                )
            self._pod = pod_spec
        else:
            if containers is None:
                raise ValueError("containers or pod_spec is required")
            self._pod = k8s.V1PodSpec(containers=containers, **pod_fields)
        self._policy = {
            "volumeClaimTemplates": volume_claim_templates,
            "persistentContents": persistent_contents,
            "runtimes": runtimes,
            "pauseStrategy": pause_strategy,
        }

    def to_dict(self) -> dict:
        return {
            **to_wire(self._pod),
            **{
                key: to_wire(value)
                for key, value in self._policy.items()
                if value is not UNSET
            },
        }
