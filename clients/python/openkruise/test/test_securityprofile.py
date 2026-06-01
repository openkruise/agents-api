import pytest
from kubernetes.client import V1ObjectMeta
from kubernetes.client.exceptions import ApiException

from agents.models.securityprofile import (
    SecurityProfile,
    Spec as SecurityProfileSpec,
    Rule as SecurityProfileRule,
    Actions as SecurityProfileActions,
    Block as SecurityProfileBlock,
    MatchItem as SecurityProfileMatch,
    Selector as SecurityProfileSelector,
)
from helpers import GROUP, VERSION, NAMESPACE


PLURAL = "securityprofiles"


def test_securityprofile_crud(k8s_api, unique_name, cleanup):
    """Test SecurityProfile CRUD operations: create -> get -> patch labels -> list -> delete"""
    print("=== Test SecurityProfile CRUD Operations ===")
    name = f"{unique_name}-sp"

    # Create SecurityProfile
    print("  Step: Creating SecurityProfile")
    sp = SecurityProfile(
        apiVersion=f"{GROUP}/{VERSION}",
        kind="SecurityProfile",
        metadata=V1ObjectMeta(name=name, namespace=NAMESPACE, labels={"app": "e2e-test-python"}),
        spec=SecurityProfileSpec(
            selector=SecurityProfileSelector(
                matchLabels={"app": "test-agent"}
            ),
            rules=[
                SecurityProfileRule(
                    name="block-external",
                    match=[SecurityProfileMatch(domains=["*.evil.com"])],
                    actions=SecurityProfileActions(
                        block=SecurityProfileBlock(statusCode=403)
                    ),
                )
            ],
        ),
    )

    body = sp.model_dump(exclude_unset=True, by_alias=True)
    created = k8s_api.create_namespaced_custom_object(
        group=GROUP,
        version=VERSION,
        namespace=NAMESPACE,
        plural=PLURAL,
        body=body,
    )
    assert created["metadata"]["name"] == name
    cleanup(GROUP, VERSION, NAMESPACE, PLURAL, name)

    # Get SecurityProfile
    print(f"  Step: Verifying SecurityProfile '{name}' is created")
    got = k8s_api.get_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, name=name
    )
    assert got["metadata"]["name"] == name
    assert got["spec"]["selector"]["matchLabels"]["app"] == "test-agent"
    assert got["spec"]["rules"][0]["name"] == "block-external"

    # Patch labels
    print(f"  Step: Patching labels for '{name}'")
    patch_body = {"metadata": {"labels": {"app": "e2e-test-python", "patched": "true"}}}
    patched = k8s_api.patch_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, name=name, body=patch_body
    )
    assert patched["metadata"]["labels"].get("patched") == "true"

    # List SecurityProfiles
    print("  Step: Listing SecurityProfiles")
    listed = k8s_api.list_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL
    )
    assert any(item["metadata"]["name"] == name for item in listed["items"])

    # Delete SecurityProfile
    print(f"  Step: Deleting SecurityProfile '{name}'")
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


def test_securityprofile_with_bypass_rule(k8s_api, unique_name, cleanup):
    """Test SecurityProfile with bypass action"""
    print("=== Test SecurityProfile with bypass rule ===")
    name = f"{unique_name}-sp-bypass"

    print("  Step: Creating SecurityProfile with bypass rule")
    sp = SecurityProfile(
        apiVersion=f"{GROUP}/{VERSION}",
        kind="SecurityProfile",
        metadata=V1ObjectMeta(name=name, namespace=NAMESPACE),
        spec=SecurityProfileSpec(
            selector=SecurityProfileSelector(
                matchLabels={"app": "trusted-agent"}
            ),
            rules=[
                SecurityProfileRule(
                    name="allow-internal",
                    match=[SecurityProfileMatch(domains=["*.internal.company.com"])],
                    actions=SecurityProfileActions(bypass=True),
                )
            ],
        ),
    )

    body = sp.model_dump(exclude_unset=True, by_alias=True)
    created = k8s_api.create_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, body=body
    )
    assert created["metadata"]["name"] == name
    cleanup(GROUP, VERSION, NAMESPACE, PLURAL, name)

    # Verify spec
    print(f"  Step: Verifying spec for '{name}'")
    got = k8s_api.get_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, name=name
    )
    assert got["spec"]["rules"][0]["name"] == "allow-internal"
    assert got["spec"]["rules"][0]["actions"]["bypass"] is True
    assert got["spec"]["rules"][0]["match"][0]["domains"] == ["*.internal.company.com"]
