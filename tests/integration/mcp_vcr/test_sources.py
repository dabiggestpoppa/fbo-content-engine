"""MCP source-tool VCR tests (reuse-only).

Covers two shapes against existing cassettes:

* ``source_list`` over ``sources_list.yaml`` — the list-tool wire shape
  (``{"notebook_id", "sources": [...]}``), a *different* list than the existing
  ``notebook_list`` VCR test so the suite gains list coverage.
* ``source_delete`` two-step confirmation — ``confirm`` omitted yields a
  ``needs_confirmation`` envelope (only the resolved-source lookup RPC runs, no
  delete); ``confirm=True`` replays the real ``DELETE_SOURCE`` mutation.

Every tool is invoked with a FULL canonical UUID (the cassette's recorded
notebook/source id) so the resolver takes its full-UUID fast path and never adds
an extra ``LIST_*`` RPC the cassette lacks.
"""

from __future__ import annotations

import pytest

from tests.integration.conftest import skip_no_cassettes
from tests.vcr_config import notebooklm_vcr

from .conftest import build_mcp_client

pytestmark = [pytest.mark.vcr, skip_no_cassettes]

# ``sources_list.yaml`` was recorded against this notebook (``GET_NOTEBOOK`` →
# ``rLM1Ne``). The value is decorative — VCR matches on rpcids + body shape, not
# the id — but reusing the recorded id keeps intent obvious.
SOURCES_LIST_NOTEBOOK_ID = "c3f6285f-1709-44c4-9cd6-e95cf0ea4f5e"

# ``sources_delete.yaml`` notebook + a full-UUID source id (the delete body's
# leaf is UUID-normalized by the matcher, so any UUID-shaped id replays).
DELETE_NOTEBOOK_ID = "06f0c5bd-108f-4c8b-8911-34b2acc656de"
DELETE_SOURCE_ID = "ff503bfa-5e39-4281-a1d8-2a66c7b86724"


@pytest.mark.asyncio
@notebooklm_vcr.use_cassette("sources_list.yaml")
async def test_mcp_source_list_over_vcr() -> None:
    """``source_list`` returns the recorded sources through the real client.

    End-to-end: FastMCP ``Client`` → ``source_list`` tool →
    ``client.sources.list()`` → recorded ``GET_NOTEBOOK`` (``rLM1Ne``) RPC. The
    full-UUID notebook ref skips the resolver's ``LIST_NOTEBOOKS`` preflight, so
    the only RPC issued is the one the cassette holds.
    """
    async with build_mcp_client() as mcp_client:
        result = await mcp_client.call_tool("source_list", {"notebook": SOURCES_LIST_NOTEBOOK_ID})

    structured = result.structured_content
    assert isinstance(structured, dict)
    # The tool projects to ``{"notebook_id", "sources": [...]}`` via to_jsonable.
    assert structured["notebook_id"] == SOURCES_LIST_NOTEBOOK_ID
    sources = structured["sources"]
    assert isinstance(sources, list)
    assert sources, "expected at least one recorded source from the cassette"
    first = sources[0]
    assert isinstance(first, dict)
    # Each source carries an id + title decoded from the positional RPC row.
    assert first.get("id"), "recorded source is missing an id"
    assert "title" in first


@pytest.mark.asyncio
async def test_mcp_source_delete_two_step_confirm_over_vcr() -> None:
    """``source_delete`` confirm-gate: preview-then-delete over real cassettes.

    Step 1 (``confirm`` omitted): the tool resolves the source (full UUID, no
    list) then lists sources for the preview title (``GET_NOTEBOOK`` → ``rLM1Ne``,
    replayed from ``sources_list.yaml``) and returns a ``needs_confirmation``
    envelope WITHOUT issuing ``DELETE_SOURCE``.

    Step 2 (``confirm=True``): the tool issues the real ``DELETE_SOURCE``
    (``tGMBJ``) mutation, replayed from ``sources_delete.yaml``, and returns the
    deleted-status envelope.

    Two separate cassettes because the preview path needs the source-list RPC
    (which the delete cassette doesn't hold) while the confirmed path needs the
    delete RPC (which the list cassette doesn't hold).
    """
    # Step 1 — preview only (no delete RPC consumed beyond the list lookup).
    with notebooklm_vcr.use_cassette("sources_list.yaml"):
        async with build_mcp_client() as mcp_client:
            preview = await mcp_client.call_tool(
                "source_delete",
                {"notebook": SOURCES_LIST_NOTEBOOK_ID, "source": DELETE_SOURCE_ID},
            )

    preview_structured = preview.structured_content
    assert isinstance(preview_structured, dict)
    assert preview_structured["status"] == "needs_confirmation"
    inner = preview_structured["preview"]
    assert inner["action"] == "delete_source"
    # The resolved source id is echoed into the preview from the tool input.
    assert inner["source_id"] == DELETE_SOURCE_ID

    # Step 2 — confirmed delete replays the real DELETE_SOURCE mutation.
    with notebooklm_vcr.use_cassette("sources_delete.yaml"):
        async with build_mcp_client() as mcp_client:
            deleted = await mcp_client.call_tool(
                "source_delete",
                {
                    "notebook": DELETE_NOTEBOOK_ID,
                    "source": DELETE_SOURCE_ID,
                    "confirm": True,
                },
            )

    deleted_structured = deleted.structured_content
    assert isinstance(deleted_structured, dict)
    assert deleted_structured["status"] == "deleted"
    assert deleted_structured["notebook_id"] == DELETE_NOTEBOOK_ID
    assert deleted_structured["source_id"] == DELETE_SOURCE_ID
