import pytest
from agents.models import (
    AgentRole,
    AgentRoleBinding,
    CredentialProvider,
    GlobalSecurityProfile,
    Sandbox,
    SandboxClaim,
    SandboxTemplate,
    SecurityProfile,
)
from pydantic import ValidationError


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


@pytest.mark.parametrize("model", [SecurityProfile, GlobalSecurityProfile])
def test_tcp_rules_round_trip(model):
    payload = {
        "spec": {
            "selector": {},
            "tcpRules": [
                {
                    "name": "allow-host",
                    "match": [
                        {"cidrs": ["140.205.162.5", "2001:db8::1"], "ports": [443]}
                    ],
                    "action": "Allow",
                },
                {
                    "name": "deny-other",
                    "match": [{"cidrs": ["0.0.0.0/0", "::/0"]}],
                    "action": "Deny",
                },
            ],
        }
    }
    resource = model.model_validate(payload)
    assert resource.model_dump(mode="json", exclude_unset=True) == payload


@pytest.mark.parametrize("model", [SecurityProfile, GlobalSecurityProfile])
def test_tcp_rules_reject_lowercase_action(model):
    with pytest.raises(ValidationError):
        model.model_validate(
            {
                "spec": {
                    "selector": {},
                    "tcpRules": [{"name": "deny", "match": [{}], "action": "deny"}],
                }
            }
        )
