"""Research MCP tools.

Thin adapters over the research surface:

* ``research_start`` calls ``client.research.start`` directly (web/drive source,
  fast/deep mode) and returns the started task. The neutral
  ``_app.source_research`` core bundles a CLI-shaped start→wait→import workflow
  (rich-coupled importer injection, flag validation); the MCP tool exposes the
  three steps as separate, agent-pollable tools instead, so it drives the client
  API directly.
* ``research_status`` drives the neutral ``_app.research.poll_and_classify`` core
  (a single non-blocking poll classified into render fields).
* ``research_import`` polls the notebook's completed research, then imports its
  sources via ``client.research.import_sources``.

Although the design sketch lists ``research_start(query, …)`` without a notebook
argument, ``client.research.start`` is notebook-scoped (it needs a
``notebook_id``), so the tool takes a ``notebook`` reference — a deliberate
follow-the-code accommodation (the design also routes name/id resolution through
the notebook list).

This module imports NO ``click`` / ``rich`` / ``cli``.
"""

from __future__ import annotations

from typing import Any

from fastmcp import Context

from ..._app import research as research_core
from ..._app.serialize import to_jsonable
from .._confirm import READ_ONLY
from .._context import get_client
from .._errors import mcp_errors
from .._resolve import resolve_notebook

#: Accepted research source / mode discriminators (validated by the client too).
_SOURCES = ("web", "drive")
_MODES = ("fast", "deep")


def register(mcp: Any) -> None:
    """Register the research tools on ``mcp``."""

    @mcp.tool
    async def research_start(
        ctx: Context,
        notebook: str,
        query: str,
        source: str = "web",
        mode: str = "fast",
    ) -> dict[str, Any]:
        """Start a research session in a notebook. Accepts a notebook name or ID.

        Non-blocking: returns the started task; poll ``research_status(notebook)``
        until it reports ``completed``, then ``research_import(notebook, task_id)``
        to add the found sources.

        ``source`` is ``web`` (default) or ``drive``. ``mode`` is ``fast``
        (default) or ``deep`` (deep is web-only).
        """
        client = get_client(ctx)
        with mcp_errors():
            nb_id = await resolve_notebook(client, notebook)
            result = await client.research.start(nb_id, query, source, mode)
            return {"notebook_id": nb_id, **to_jsonable(result)}

    @mcp.tool(annotations=READ_ONLY)
    async def research_status(ctx: Context, notebook: str) -> dict[str, Any]:
        """Check a notebook's research status. Accepts a notebook name or ID.

        Returns ``status`` (no_research|in_progress|completed) plus the found
        ``sources`` and any ``report`` once complete. Poll until ``completed``.
        """
        client = get_client(ctx)
        with mcp_errors():
            nb_id = await resolve_notebook(client, notebook)
            result = await research_core.poll_and_classify(client, nb_id)
            return {
                "notebook_id": nb_id,
                "kind": result.kind,
                "status": result.status,
                "query": result.query,
                "sources": to_jsonable(result.sources),
                "summary": result.summary,
                "report": result.report,
            }

    @mcp.tool
    async def research_import(ctx: Context, notebook: str, task_id: str) -> dict[str, Any]:
        """Import a completed research task's sources into the notebook.

        Accepts a notebook name or ID and the ``task_id`` from ``research_start``.
        Polls the notebook's completed research for its found sources and imports
        them; returns the imported sources (verify with ``source_list``).
        """
        client = get_client(ctx)
        with mcp_errors():
            nb_id = await resolve_notebook(client, notebook)
            status = await research_core.poll_and_classify(client, nb_id)
            imported = await client.research.import_sources(nb_id, task_id, status.sources)
            return {
                "notebook_id": nb_id,
                "task_id": task_id,
                "imported": to_jsonable(imported),
                "sources_found": len(status.sources),
            }
