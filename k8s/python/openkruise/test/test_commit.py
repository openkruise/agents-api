import pytest
from kubernetes.client import V1ObjectMeta
from kubernetes.client.exceptions import ApiException

from agents.models.commit import Commit, Spec as CommitSpec
from helpers import GROUP, VERSION, NAMESPACE


PLURAL = "commits"


def test_commit_crud(k8s_api, unique_name, cleanup):
    """Test Commit CRUD operations: Create -> Get -> List -> Delete -> verify deletion"""
    print("=== Test Commit CRUD Operations ===")
    name = f"{unique_name}-commit"

    # Create Commit
    print(f"  Step: Creating Commit '{name}'")
    commit = Commit(
        apiVersion=f"{GROUP}/{VERSION}",
        kind="Commit",
        metadata=V1ObjectMeta(name=name, namespace=NAMESPACE),
        spec=CommitSpec(
            podName="test-pod",
            containerName="test-container",
            image="registry.example.com/test-image:v1"
        )
    )

    body = commit.model_dump(exclude_unset=True, by_alias=True)
    created = k8s_api.create_namespaced_custom_object(
        group=GROUP,
        version=VERSION,
        namespace=NAMESPACE,
        plural=PLURAL,
        body=body
    )
    assert created["metadata"]["name"] == name
    cleanup(GROUP, VERSION, NAMESPACE, PLURAL, name)

    # Get Commit
    print(f"  Step: Verifying Commit '{name}' is created")
    fetched = k8s_api.get_namespaced_custom_object(
        group=GROUP,
        version=VERSION,
        namespace=NAMESPACE,
        plural=PLURAL,
        name=name
    )
    assert fetched["metadata"]["name"] == name
    assert fetched["spec"]["podName"] == "test-pod"
    assert fetched["spec"]["containerName"] == "test-container"
    assert fetched["spec"]["image"] == "registry.example.com/test-image:v1"

    # List Commits
    print(f"  Step: Listing Commits")
    listed = k8s_api.list_namespaced_custom_object(
        group=GROUP,
        version=VERSION,
        namespace=NAMESPACE,
        plural=PLURAL
    )
    assert any(item["metadata"]["name"] == name for item in listed["items"])

    # Delete Commit
    print(f"  Step: Deleting Commit '{name}'")
    k8s_api.delete_namespaced_custom_object(
        group=GROUP,
        version=VERSION,
        namespace=NAMESPACE,
        plural=PLURAL,
        name=name
    )

    # Verify deletion
    print(f"  Step: Verifying deletion of '{name}'")
    with pytest.raises(ApiException) as exc_info:
        k8s_api.get_namespaced_custom_object(
            group=GROUP,
            version=VERSION,
            namespace=NAMESPACE,
            plural=PLURAL,
            name=name
        )
    assert exc_info.value.status == 404


def test_commit_with_optional_fields(k8s_api, unique_name, cleanup):
    """Test Commit with registryAuth and timeoutSeconds"""
    print("=== Test Commit with optional fields ===")
    name = f"{unique_name}-commit-opts"

    print(f"  Step: Creating Commit '{name}' with optional fields")
    commit = Commit(
        apiVersion=f"{GROUP}/{VERSION}",
        kind="Commit",
        metadata=V1ObjectMeta(name=name, namespace=NAMESPACE),
        spec=CommitSpec(
            podName="test-pod",
            containerName="test-container",
            image="registry.example.com/test-image:v2",
            timeoutSeconds=300,
            squashLayer=0,
            registryAuth={"secrets": ["my-registry-secret"]}
        )
    )

    body = commit.model_dump(exclude_unset=True, by_alias=True)
    created = k8s_api.create_namespaced_custom_object(
        group=GROUP,
        version=VERSION,
        namespace=NAMESPACE,
        plural=PLURAL,
        body=body
    )
    assert created["metadata"]["name"] == name
    cleanup(GROUP, VERSION, NAMESPACE, PLURAL, name)

    # Verify optional fields
    print(f"  Step: Verifying optional fields for '{name}'")
    got = k8s_api.get_namespaced_custom_object(
        group=GROUP, version=VERSION, namespace=NAMESPACE, plural=PLURAL, name=name
    )
    assert got["spec"]["timeoutSeconds"] == 300
    assert got["spec"]["registryAuth"]["secrets"] == ["my-registry-secret"]
