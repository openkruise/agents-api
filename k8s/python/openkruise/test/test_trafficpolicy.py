import pytest
from kubernetes.client import V1ObjectMeta
from kubernetes.client.exceptions import ApiException

from agents.models.trafficpolicy import (
    TrafficPolicy,
    Spec as TrafficPolicySpec,
    Ingress as TrafficPolicyIngress,
    Egress as TrafficPolicyEgress,
    Rule,
    Rule1,
    FromItem,
    FromItem1,
    ToItem,
    Port,
    Port1,
    Selector as TrafficPolicySelector,
)
from helpers import GROUP, VERSION, NAMESPACE


PLURAL = "trafficpolicies"


def test_trafficpolicy_crud(k8s_api, unique_name, cleanup):
    """Test TrafficPolicy CRUD operations: create -> get -> list -> delete"""
    print("=== Test TrafficPolicy CRUD Operations ===")
    name = f"{unique_name}-tp"

    # Create TrafficPolicy
    print(f"  Step: Creating TrafficPolicy '{name}'")
    tp = TrafficPolicy(
        apiVersion=f"{GROUP}/{VERSION}",
        kind="TrafficPolicy",
        metadata=V1ObjectMeta(name=name, namespace=NAMESPACE, labels={"app": "e2e-test-python"}),
        spec=TrafficPolicySpec(
            priority=1000,
            selector=TrafficPolicySelector(matchLabels={"app": "test-agent"}),
            ingress=TrafficPolicyIngress(
                rules=[
                    Rule1(
                        action="allow",
                        from_=[FromItem1(cidr="10.0.0.0/8")],
                        ports=[Port1(protocol="TCP", port=80)],
                    )
                ]
            ),
        ),
    )

    body = tp.model_dump(exclude_unset=True, by_alias=True)
    created = k8s_api.create_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, body=body
    )
    assert created["metadata"]["name"] == name
    cleanup(GROUP, VERSION, NAMESPACE, PLURAL, name)

    # Get TrafficPolicy
    print(f"  Step: Verifying TrafficPolicy '{name}'")
    got = k8s_api.get_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, name=name
    )
    assert got["metadata"]["name"] == name
    assert got["spec"]["priority"] == 1000
    assert got["spec"]["ingress"]["rules"][0]["action"] == "allow"

    # List TrafficPolicies
    print("  Step: Listing TrafficPolicies")
    listed = k8s_api.list_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL
    )
    assert any(item["metadata"]["name"] == name for item in listed["items"])

    # Delete TrafficPolicy
    print(f"  Step: Deleting TrafficPolicy '{name}'")
    k8s_api.delete_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, name=name
    )

    # Verify deletion
    print(f"  Step: Verifying deletion of '{name}'")
    with pytest.raises(ApiException) as exc_info:
        k8s_api.get_namespaced_custom_object(
            group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, name=name
        )
    assert exc_info.value.status == 404


def test_trafficpolicy_egress_reject(k8s_api, unique_name, cleanup):
    """Test TrafficPolicy with egress reject rule"""
    print("=== Test TrafficPolicy with egress reject rule ===")
    name = f"{unique_name}-tp-egress"

    print(f"  Step: Creating TrafficPolicy '{name}' with egress reject")
    tp = TrafficPolicy(
        apiVersion=f"{GROUP}/{VERSION}",
        kind="TrafficPolicy",
        metadata=V1ObjectMeta(name=name, namespace=NAMESPACE),
        spec=TrafficPolicySpec(
            selector=TrafficPolicySelector(matchLabels={"app": "test-agent"}),
            egress=TrafficPolicyEgress(
                rules=[
                    Rule(
                        action="reject",
                        to=[ToItem(fqdn="*.evil.com")],
                    )
                ]
            ),
        ),
    )

    body = tp.model_dump(exclude_unset=True, by_alias=True)
    created = k8s_api.create_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, body=body
    )
    assert created["metadata"]["name"] == name
    cleanup(GROUP, VERSION, NAMESPACE, PLURAL, name)

    # Verify egress spec
    print(f"  Step: Verifying egress spec for '{name}'")
    got = k8s_api.get_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, name=name
    )
    assert got["spec"]["egress"]["rules"][0]["action"] == "reject"
    assert got["spec"]["egress"]["rules"][0]["to"][0]["fqdn"] == "*.evil.com"
