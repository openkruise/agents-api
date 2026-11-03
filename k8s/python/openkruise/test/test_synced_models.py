import pytest

from agents.models import (
    AgentRole,
    AgentRoleBinding,
    CredentialProvider,
    Sandbox,
    SandboxClaim,
    SandboxTemplate,
    SecurityProfile,
)


@pytest.mark.parametrize(
    "model,payload",
    [
        (
            AgentRole,
            {
                "spec": {
                    "rules": [
                        {
                            "actions": ["ExchangePrincipalToken"],
                            "resources": ["AgentIdentity/agent"],
                        }
                    ]
                }
            },
        ),
        (
            AgentRoleBinding,
            {
                "spec": {
                    "agentIdentityRef": {"name": "agent"},
                    "agentRoleRef": {
                        "apiGroup": "security.agents.kruise.io",
                        "kind": "AgentRole",
                        "name": "role",
                    },
                }
            },
        ),
        (
            CredentialProvider,
            {
                "spec": {
                    "type": "oauth2",
                    "oauth2": {
                        "client": {
                            "clientId": "client",
                            "clientSecret": {
                                "secretKeyRef": {"name": "oauth", "key": "secret"}
                            },
                        },
                        "discovery": {
                            "issuer": "https://issuer.example.com",
                            "authorizationEndpoint": "https://issuer.example.com/authorize",
                            "tokenEndpoint": "https://issuer.example.com/token",
                        },
                        "grantType": "userFederation",
                        "userFederation": {
                            "callbackBaseUrl": "https://callback.example.com",
                            "defaultScopes": ["read"],
                        },
                    },
                }
            },
        ),
        (
            Sandbox,
            {
                "spec": {
                    "lifecycle": {
                        "prePause": {
                            "exec": {"command": ["sync"]},
                            "timeoutSeconds": 10,
                        },
                        "postResume": {"exec": {"command": ["true"]}},
                    }
                }
            },
        ),
        (
            SandboxClaim,
            {
                "spec": {
                    "templateName": "pool",
                    "autoPausePolicy": {"pause": {}},
                    "probes": [{"name": "idle", "exec": {"command": ["true"]}}],
                }
            },
        ),
        (
            SandboxTemplate,
            {
                "spec": {
                    "backend": "k8s",
                    "buildOptions": {
                        "steps": [
                            {
                                "command": "echo ready",
                                "cwd": "/app",
                                "env": {"MODE": "test"},
                            }
                        ]
                    },
                },
                "status": {
                    "phase": "Ready",
                    "artifacts": [{"type": "Checkpoint", "name": "snapshot"}],
                },
            },
        ),
        (
            SecurityProfile,
            {
                "spec": {
                    "selector": {"matchLabels": {"app": "test"}},
                    "rules": [
                        {
                            "name": "tls",
                            "match": [{"domains": ["example.com"]}],
                            "actions": {"tls": {"action": "Passthrough"}},
                        }
                    ],
                }
            },
        ),
    ],
)
def test_synced_fields_round_trip(model, payload):
    resource = model.model_validate(payload)
    assert resource.model_dump(mode="json", exclude_unset=True) == payload
