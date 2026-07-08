import sys
import os
import time
import pytest
from kubernetes import client, config

# Add test directory to path so we can import helpers module
sys.path.insert(0, os.path.dirname(__file__))
# Add parent directory to path so we can import agents package
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from agents import SandboxClient, SandboxSetClient, SandboxClaimClient
from helpers import NAMESPACE


@pytest.fixture(scope="module")
def sandbox_client():
    """Create a SandboxClient instance"""
    return SandboxClient(NAMESPACE)


@pytest.fixture(scope="module")
def sandboxset_client():
    """Create a SandboxSetClient instance"""
    return SandboxSetClient(NAMESPACE)


@pytest.fixture(scope="module")
def sandboxclaim_client():
    """Create a SandboxClaimClient instance"""
    return SandboxClaimClient(NAMESPACE)


@pytest.fixture(scope="module")
def k8s_api():
    """Create a CustomObjectsApi instance for CRDs without dedicated clients"""
    try:
        config.load_incluster_config()
    except config.ConfigException:
        config.load_kube_config()
    return client.CustomObjectsApi()


@pytest.fixture
def unique_name():
    """Generate a unique name for each test"""
    return f"e2e-py-{time.time_ns()}"


@pytest.fixture
def cleanup(k8s_api):
    """Fixture that collects cleanup actions and runs them after test, even on failure."""
    actions = []

    def _register(group, version, namespace, plural, name):
        actions.append((group, version, namespace, plural, name))

    yield _register

    for group, version, namespace, plural, name in reversed(actions):
        try:
            k8s_api.delete_namespaced_custom_object(
                group=group, version=version, namespace=namespace,
                plural=plural, name=name
            )
        except Exception:
            pass


@pytest.fixture
def cleanup_cluster(k8s_api):
    """Fixture that collects cleanup actions for cluster-scoped resources and runs them after test."""
    actions = []

    def _register(group, version, plural, name):
        actions.append((group, version, plural, name))

    yield _register

    for group, version, plural, name in reversed(actions):
        try:
            k8s_api.delete_cluster_custom_object(
                group=group, version=version, plural=plural, name=name
            )
        except Exception:
            pass
