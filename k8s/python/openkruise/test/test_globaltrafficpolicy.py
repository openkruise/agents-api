import pytest
from kubernetes.client import V1ObjectMeta
from kubernetes.client.exceptions import ApiException

from agents.models.globaltrafficpolicy import (
    GlobalTrafficPolicy,
    Spec as GlobalTrafficPolicySpec,
    Ingress as GlobalTrafficPolicyIngress,
    Egress as GlobalTrafficPolicyEgress,
    Rule,
    Rule1,
    FromItem1,
    ToItem,
    Port1,
    Selector as GlobalTrafficPolicySelector,
    Workload as GlobalTrafficPolicyWorkload,
)
from helpers import GROUP, VERSION


PLURAL = "globaltrafficpolicies"


def test_globaltrafficpolicy_crud(k8s_api, unique_name, cleanup_cluster):
    """Test GlobalTrafficPolicy CRUD operations (cluster-scoped)"""
    print("=== Test GlobalTrafficPolicy CRUD Operations ===")
    name = f"{unique_name}-gtp"

    # Create GlobalTrafficPolicy
    print(f"  Step: Creating GlobalTrafficPolicy '{name}'")
    gtp = GlobalTrafficPolicy(
        apiVersion=f"{GROUP}/{VERSION}",
        kind="GlobalTrafficPolicy",
        metadata=V1ObjectMeta(name=name, labels={"app": "e2e-test-python"}),
        spec=GlobalTrafficPolicySpec(
            priority=500,
            selector=GlobalTrafficPolicySelector(matchLabels={"app": "global-agent"}),
            ingress=GlobalTrafficPolicyIngress(
                rules=[
                    Rule1(
                        action="allow",
                        from_=[FromItem1(cidr="192.168.0.0/16")],
                        ports=[Port1(protocol="TCP", port=443)],
                    )
                ]
            ),
        ),
    )

    body = gtp.model_dump(exclude_unset=True, by_alias=True)
    created = k8s_api.create_cluster_custom_object(
        group=GROUP, version=VERSION, plural=PLURAL, body=body
    )
    assert created["metadata"]["name"] == name
    cleanup_cluster(GROUP, VERSION, PLURAL, name)

    # Get GlobalTrafficPolicy
    print(f"  Step: Verifying GlobalTrafficPolicy '{name}'")
    got = k8s_api.get_cluster_custom_object(
        group=GROUP, version=VERSION, plural=PLURAL, name=name
    )
    assert got["metadata"]["name"] == name
    assert got["spec"]["priority"] == 500
    assert got["spec"]["ingress"]["rules"][0]["action"] == "allow"

    # List GlobalTrafficPolicies
    print("  Step: Listing GlobalTrafficPolicies")
    listed = k8s_api.list_cluster_custom_object(
        group=GROUP, version=VERSION, plural=PLURAL
    )
    assert any(item["metadata"]["name"] == name for item in listed["items"])

    # Delete GlobalTrafficPolicy
    print(f"  Step: Deleting GlobalTrafficPolicy '{name}'")
    k8s_api.delete_cluster_custom_object(
        group=GROUP, version=VERSION, plural=PLURAL, name=name
    )

    # Verify deletion
    print(f"  Step: Verifying deletion of '{name}'")
    with pytest.raises(ApiException) as exc_info:
        k8s_api.get_cluster_custom_object(
            group=GROUP, version=VERSION, plural=PLURAL, name=name
        )
    assert exc_info.value.status == 404


def test_globaltrafficpolicy_with_workload_peer(k8s_api, unique_name, cleanup_cluster):
    """Test GlobalTrafficPolicy with workload peer selector"""
    print("=== Test GlobalTrafficPolicy with workload peer ===")
    name = f"{unique_name}-gtp-wl"

    print(f"  Step: Creating GlobalTrafficPolicy '{name}' with workload peer")
    gtp = GlobalTrafficPolicy(
        apiVersion=f"{GROUP}/{VERSION}",
        kind="GlobalTrafficPolicy",
        metadata=V1ObjectMeta(name=name),
        spec=GlobalTrafficPolicySpec(
            selector=GlobalTrafficPolicySelector(matchLabels={"app": "global-agent"}),
            egress=GlobalTrafficPolicyEgress(
                rules=[
                    Rule(
                        action="allow",
                        to=[
                            ToItem(
                                workload=GlobalTrafficPolicyWorkload(
                                    namespace="default",
                                    selector={"app": "backend"}
                                )
                            )
                        ],
                    )
                ]
            ),
        ),
    )

    body = gtp.model_dump(exclude_unset=True, by_alias=True)
    created = k8s_api.create_cluster_custom_object(
        group=GROUP, version=VERSION, plural=PLURAL, body=body
    )
    assert created["metadata"]["name"] == name
    cleanup_cluster(GROUP, VERSION, PLURAL, name)

    # Verify workload peer
    print(f"  Step: Verifying workload peer for '{name}'")
    got = k8s_api.get_cluster_custom_object(
        group=GROUP, version=VERSION, plural=PLURAL, name=name
    )
    assert got["spec"]["egress"]["rules"][0]["to"][0]["workload"]["namespace"] == "default"
    assert got["spec"]["egress"]["rules"][0]["to"][0]["workload"]["selector"]["app"] == "backend"
