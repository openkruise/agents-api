from kubernetes import client, config
from . import SandboxSet

class SandboxSetClient:
    def __init__(self, namespace="default"):
        self.group = "agents.kruise.io"
        self.version = "v1alpha1"
        self.plural = "sandboxsets"
        self.namespace = namespace
        self.kind = "SandboxSet"
        try:
            config.load_incluster_config()
        except config.ConfigException:
            config.load_kube_config()
        self.api = client.CustomObjectsApi()

    def create_sandboxset(self, sandboxset: SandboxSet) -> dict:
        sandboxset.apiVersion = f"{self.group}/{self.version}"
        sandboxset.kind = self.kind
        # Pydantic -> dict
        body = sandboxset.model_dump(exclude_unset=True, by_alias=True)
        return self.api.create_namespaced_custom_object(
            group=self.group,
            version=self.version,
            namespace=self.namespace,
            plural=self.plural,
            body=body
        )

    def get_sandboxset(self, name: str) -> dict:
        """获取指定名称的 SandboxSet 资源"""
        return self.api.get_namespaced_custom_object(
            group=self.group,
            version=self.version,
            namespace=self.namespace,
            plural=self.plural,
            name=name
        )

    def update_sandboxset(self, name: str, body: dict) -> dict:
        """更新指定名称的 SandboxSet 资源"""
        return self.api.patch_namespaced_custom_object(
            group=self.group,
            version=self.version,
            namespace=self.namespace,
            plural=self.plural,
            name=name,
            body=body
        )

    def delete_sandboxset(self, name: str, grace_period_seconds: int = None) -> dict:
        """删除指定名称的 SandboxSet 资源"""
        return self.api.delete_namespaced_custom_object(
            group=self.group,
            version=self.version,
            namespace=self.namespace,
            plural=self.plural,
            name=name,
            grace_period_seconds=grace_period_seconds
        )
