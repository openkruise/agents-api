"""Golden-value tests for kruise_agents.keys.

The expected strings lock the byte-for-byte wire format that the server-side
compat layer (pkg/servers/e2b/keys/compat.go in the openkruise/agents
sandbox-manager — the implementation keys.py must stay in sync with) must
keep producing. Regenerate additions:

    cd e2b/python && python -c \
        "from kruise_agents.keys import encode_for_e2b_sdk as e; print(e('raw'))"
"""

import hashlib

from kruise_agents.keys import (
    _E2B_SDK_COMPAT_CHECKSUM_SALT,
    _E2B_SDK_COMPAT_MAGIC,
    _E2B_SDK_COMPAT_VERSION,
    _E2B_SDK_PREFIX,
    encode_for_e2b_sdk,
)

_GOLDEN = {
    "": "e2b_6f6b61670100000000425929dd6afb855b",
    "admin-key": "e2b_6f6b6167010000000961646d696e2d6b6579c4341091192d130a",
    "5b14a58f-93f4-4d3e-9a92-2f3e0e1a9e33": (
        "e2b_6f6b61670100000024"
        "35623134613538662d393366342d346433652d396139322d326633653065316139653333"
        "1d5464e1669224e7"
    ),
}


def test_encodes_known_api_keys_to_golden_values():
    for raw_key, encoded in _GOLDEN.items():
        assert encode_for_e2b_sdk(raw_key) == encoded, f"raw key {raw_key!r}"


def test_layout_follows_the_compat_protocol():
    encoded = encode_for_e2b_sdk("raw-key")

    header = encoded[len(_E2B_SDK_PREFIX) :]
    magic = header[:8]
    version = header[8:10]
    length = header[10:18]
    # checksum is 8 bytes -> 16 hex characters, hence -16 (not -8).
    body = header[18:-16]
    checksum = header[-16:]
    raw_bytes = b"raw-key"

    assert magic == _E2B_SDK_COMPAT_MAGIC
    assert version == _E2B_SDK_COMPAT_VERSION
    assert length == f"{len(raw_bytes):08x}"
    assert bytes.fromhex(body) == raw_bytes
    expected_checksum = hashlib.sha256(
        _E2B_SDK_COMPAT_CHECKSUM_SALT.encode("utf-8") + raw_bytes
    ).digest()[:8]
    assert bytes.fromhex(checksum) == expected_checksum


def test_encoding_is_deterministic():
    assert encode_for_e2b_sdk("same-key") == encode_for_e2b_sdk("same-key")
