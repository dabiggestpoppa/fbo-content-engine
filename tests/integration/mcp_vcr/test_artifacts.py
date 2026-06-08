"""MCP artifact-tool VCR test (reuse-only).

``artifact_list`` over ``artifacts_list.yaml`` — the studio-artifact list wire
shape (``{"notebook_id", "artifacts": [...]}``). ``client.artifacts.list``
issues ``LIST_ARTIFACTS`` (``gArtLc``) + the note-backed mind-map merge
``GET_NOTES_AND_MIND_MAPS`` (``cFji9``), both recorded in the cassette.

The tool is invoked with the cassette's recorded full-UUID notebook id so the
resolver skips its ``LIST_NOTEBOOKS`` preflight.

(An ``artifact_download`` test was DELIBERATELY DROPPED: the
``_app.download.execute_download`` flow lists artifacts to select the latest
(``client.artifacts.list`` → ``gArtLc`` + ``cFji9``) AND then re-lists inside
``download_report`` (``gArtLc``), so it issues ``gArtLc`` *twice* — but every
``artifacts_download_*`` cassette holds only one ``gArtLc`` interaction. The CLI
``cli_vcr`` download tests only pass because ``assert_command_success`` tolerates
the resulting exit-1; this suite asserts the *real* serialized wire shape, which
that pairing cannot satisfy without re-recording. Per the reuse-only discipline,
the download shape is dropped rather than forced.)
"""

from __future__ import annotations

import pytest

from tests.integration.conftest import skip_no_cassettes
from vcr_config import notebooklm_vcr

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
