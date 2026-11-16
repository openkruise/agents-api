"""Requires a team key, configured E2B endpoint, and reachable custom port 8081.
This is an opt-in live example, not an automatically executed test.
"""

import uuid

from openkruise_agents import Sandbox, Template, patch_e2b
from openkruise_agents.models import (
    CapacityPolicy,
    Container,
    PoolAutoscalerOptions,
    RuntimeConfig,
    SandboxTemplateSpec,
    TemplateBuildOptions,
    WarmPoolOptions,
)


def main():
    # Reads existing E2B_DOMAIN/E2B_API_KEY. Set https=False only for plaintext
    # local deployments; HTTPS on :8081 needs its own exposed TLS endpoint.
    patch_e2b(validate_key=False)
    name = "python-demo-" + uuid.uuid4().hex[:8]
    sandbox = None
    template = None
    try:
        template = Template.build(
            name=name,
            spec=SandboxTemplateSpec(
                containers=[Container(name="main", image="python:3.12")],
                runtimes=[RuntimeConfig(name="agent-runtime")],
            ),
            options=TemplateBuildOptions(
                warm_pool=WarmPoolOptions(
                    size=2,
                    autoscaler=PoolAutoscalerOptions(
                        min_replicas=1,
                        max_replicas=10,
                        capacity_policy=CapacityPolicy(target_available=2),
                    ),
                )
            ),
        )
        template.wait_until_ready(timeout=120, interval=2)
        print(Template.get(name, template_id=template.id))
        print(Template.list())
        # Ready is definition-only; pool creation or stock may still be pending.
        # Instance creation stays on the original Sandbox entrypoint.
        sandbox = Sandbox.create(template=template.id)
    finally:
        if sandbox is not None:
            sandbox.kill()
        if template is not None:
            Template.delete(name)
        # A failed POST can leave partial resources. Inspect the failure and
        # explicitly clean up the reserved name; never delete a pre-existing
        # Claim just because build returned 409.


if __name__ == "__main__":
    main()
