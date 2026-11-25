"""Shared helpers for the hand-written Kruise business models."""

from pydantic import ConfigDict


def to_camel(name: str) -> str:
    """Convert a snake_case field name to camelCase for wire serialization.

    Field names without underscores are returned unchanged, so generators
    built on this helper never mangle already-camelCase input.
    """

    head, *rest = name.split("_")
    return head + "".join(part[:1].upper() + part[1:] for part in rest)


camel_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)
