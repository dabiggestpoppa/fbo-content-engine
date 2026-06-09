"""MCP artifact-tool VCR test (reuse-only).

``artifact_list`` over ``artifacts_list.yaml`` — the studio-artifact list wire
shape (``{"notebook_id", "artifacts": [...]}``). ``client.artifacts.list``
issues ``LIST_ARTIFACTS`` (``gArtLc``) + the note-backed mind-map merge
``GET_NOTES_AND_MIND_MAPS`` (``cFji9``), both recorded in the cassette.

``artifact_download`` over ``artifacts_download_report.yaml`` — the typed
``DownloadResult`` wire shape, end-to-end, with the report file actually written.
This pairing was originally DROPPED because the download path issued
``LIST_ARTIFACTS`` (``gArtLc``) *twice* (the executor listed to select, then
``download_report`` re-listed), which can't replay against a single-``gArtLc``
cassette. #1488 collapsed that to a single list (the executor threads the
already-fetched rows into the download method), so the shape now replays cleanly.

The tools are invoked with a full-UUID notebook id so the resolver skips its
``LIST_NOTEBOOKS`` preflight.
"""

from __future__ import annotations

import pytest

from tests.integration.conftest import skip_no_cassettes
from tests.vcr_config import notebooklm_vcr

from .conftest import build_mcp_client

pytestmark = [pytest.mark.vcr, skip_no_cassettes]

# ``artifacts_list.yaml`` was recorded against this notebook. Decorative — the
# matcher keys on rpcids + body shape, never the notebook id.
ARTIFACT_NOTEBOOK_ID = "c3f6285f-1709-44c4-9cd6-e95cf0ea4f5e"


@pytest.mark.asyncio
@notebooklm_vcr.use_cassette("artifacts_list.yaml")
async def test_mcp_artifact_list_over_vcr() -> None:
    """``artifact_list`` returns the recorded artifacts through the real client.

    End-to-end: FastMCP ``Client`` → ``artifact_list`` tool →
    ``client.artifacts.list()`` → recorded ``LIST_ARTIFACTS`` (``gArtLc``) +
    ``GET_NOTES_AND_MIND_MAPS`` (``cFji9``) RPCs.
    """
    async with build_mcp_client() as mcp_client:
        result = await mcp_client.call_tool("artifact_list", {"notebook": ARTIFACT_NOTEBOOK_ID})

    structured = result.structured_content
    assert isinstance(structured, dict)
    assert structured["notebook_id"] == ARTIFACT_NOTEBOOK_ID
    artifacts = structured["artifacts"]
    assert isinstance(artifacts, list)
    assert artifacts, "expected at least one recorded artifact from the cassette"
    first = artifacts[0]
    assert isinstance(first, dict)
    # ``to_jsonable`` serializes the declared ``Artifact`` dataclass fields (the
    # user-facing ``type_id`` / ``kind`` are ``@property``, so they are NOT on
    # the wire). Pin the real serialized fields decoded from the positional row:
    # a non-empty id, a title, the integer artifact-type code, and the status.
    assert first.get("id"), "recorded artifact is missing an id"
    assert "title" in first
    assert isinstance(first.get("_artifact_type"), int), "missing decoded artifact-type code"
    assert isinstance(first.get("status"), int), "missing decoded status code"


@pytest.mark.asyncio
@notebooklm_vcr.use_cassette("artifacts_download_report.yaml")
async def test_mcp_artifact_download_over_vcr(tmp_path) -> None:
    """``artifact_download`` selects + writes the latest report through the real client.

    End-to-end: FastMCP ``Client`` → ``artifact_download`` tool →
    ``execute_download`` (single ``LIST_ARTIFACTS`` post-#1488) →
    ``client.artifacts.download_report`` → recorded download RPC. Asserts the
    typed ``DownloadResult`` wire shape AND that the file was really written
    (a re-introduced double-list would fail the replay, not silently pass).
    """
    out = tmp_path / "report.md"
    async with build_mcp_client() as mcp_client:
        result = await mcp_client.call_tool(
            "artifact_download",
            {
                "notebook": ARTIFACT_NOTEBOOK_ID,
                "artifact_type": "report",
                "path": str(out),
            },
        )

    structured = result.structured_content
    assert isinstance(structured, dict)
    assert structured["outcome"] == "single_downloaded", structured
    assert not structured.get("is_failure"), structured
    assert structured.get("error") is None, structured
    assert structured.get("output_path"), structured
    assert out.exists() and out.stat().st_size > 0, "the report file was not written"
