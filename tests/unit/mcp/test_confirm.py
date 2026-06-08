"""Unit tests for the MCP confirmation + annotation helpers."""

from __future__ import annotations

from mcp.types import ToolAnnotations

from notebooklm.mcp._confirm import DESTRUCTIVE, READ_ONLY, needs_confirmation


def test_needs_confirmation_wraps_preview() -> None:
    preview = {"action": "delete", "notebook_id": "nb-1", "title": "Doomed"}
    result = needs_confirmation(preview)
    assert result == {"status": "needs_confirmation", "preview": preview}


def test_needs_confirmation_preserves_preview_identity_contents() -> None:
    preview = {"a": 1, "nested": {"b": [2, 3]}}
    result = needs_confirmation(preview)
    assert result["status"] == "needs_confirmation"
    assert result["preview"] == preview


def test_read_only_annotation() -> None:
    assert isinstance(READ_ONLY, ToolAnnotations)
    assert READ_ONLY.readOnlyHint is True
    # A read-only tool must not also be flagged destructive.
    assert READ_ONLY.destructiveHint in (None, False)


def test_destructive_annotation() -> None:
    assert isinstance(DESTRUCTIVE, ToolAnnotations)
    assert DESTRUCTIVE.destructiveHint is True
    # A destructive tool is not read-only.
    assert DESTRUCTIVE.readOnlyHint in (None, False)


def test_annotations_are_distinct() -> None:
    assert READ_ONLY is not DESTRUCTIVE
    assert READ_ONLY.readOnlyHint != DESTRUCTIVE.readOnlyHint or READ_ONLY != DESTRUCTIVE
