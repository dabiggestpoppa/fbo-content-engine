"""Name / partial-id resolution for MCP tools.

MCP tools accept a human-friendly ``notebook`` / ``source`` reference and turn it
into a canonical backend id. The matching rules build on the neutral
:func:`notebooklm._app.resolve.resolve_ref` (full/partial-UUID fast-path, exact
id, unique prefix, ambiguous-prefix -> :class:`AmbiguousIdError`) and ADD
case-insensitive **exact-title** matching for human references.

Routing is by token shape:

* A full canonical UUID is returned verbatim with **no list call** (so a tool
  invoked with a concrete id never pays for a list).
* A hex-ish token (``^[0-9a-fA-F-]+$``) takes the id/prefix path via
  ``resolve_ref`` against the listed items.
* Anything else takes the title path: a case-insensitive exact match over the
  items' titles — 0 matches raises the public ``*NotFoundError``, >1 raises
  :class:`AmbiguousIdError` carrying the colliding ids.

Sources are resolved within their notebook's source list. The prefix path's
no-match (``ValidationError`` from ``resolve_ref``) is re-raised as the
domain-specific ``*NotFoundError`` so every miss surfaces uniformly as
``NOT_FOUND`` regardless of which path produced it.

This module imports NO ``click`` / ``rich`` / ``cli`` — only the ``_app``
resolve core and the public exception hierarchy.
"""

from __future__ import annotations

import re
from collections.abc import Sequence
from typing import TYPE_CHECKING, Any

from .._app.resolve import (
    FULL_ID_PATTERN,
    AmbiguousIdError,
    resolve_ref,
    validate_id,
)
from ..exceptions import NotebookNotFoundError, SourceNotFoundError, ValidationError

if TYPE_CHECKING:
    from ..client import NotebookLMClient

__all__ = ["resolve_notebook", "resolve_source"]

#: A token made only of hex digits and dashes routes to the id/prefix path; any
#: other character (a space, a letter outside ``a-f``, punctuation) routes to the
#: title path. Mirrors the plan's ``^[0-9a-fA-F-]+$`` discriminator.
_HEX_ISH = re.compile(r"^[0-9a-fA-F-]+$")

#: Max candidate ids surfaced in an ambiguous-title error message.
_MAX_AMBIGUOUS_CANDIDATES = 5


def _resolve_by_title(
    token: str,
    items: Sequence[Any],
    *,
    not_found: type[NotebookNotFoundError | SourceNotFoundError],
) -> str:
    """Resolve ``token`` by case-insensitive exact title over ``items``.

    Raises ``not_found(token)`` on 0 matches and :class:`AmbiguousIdError` on >1.
    """
    token_lower = token.lower()
    matches = [item for item in items if (item.title or "").lower() == token_lower]

    if len(matches) == 1:
        return str(matches[0].id)

    if not matches:
        raise not_found(token)

    candidate_ids = [str(item.id) for item in matches]
    lines = [f"Ambiguous title '{token}' matches {len(matches)} items:"]
    for item in matches[:_MAX_AMBIGUOUS_CANDIDATES]:
        lines.append(f"  {str(item.id)[:12]}... {item.title or '(untitled)'}")
    if len(matches) > _MAX_AMBIGUOUS_CANDIDATES:
        lines.append(f"  ... and {len(matches) - _MAX_AMBIGUOUS_CANDIDATES} more")
    lines.append("\nUse a more specific title or the id.")
    raise AmbiguousIdError(token, candidate_ids, "\n".join(lines))


def _resolve_by_id_or_prefix(
    token: str,
    items: Sequence[Any],
    *,
    not_found: type[NotebookNotFoundError | SourceNotFoundError],
) -> str:
    """Resolve a hex-ish ``token`` via ``resolve_ref``, mapping no-match to NotFound."""
    try:
        resolution = resolve_ref(
            token,
            items,
            id_of=lambda item: str(item.id),
            title_of=lambda item: item.title,
        )
    except AmbiguousIdError:
        raise
    except ValidationError as exc:
        # resolve_ref raises a bare ValidationError on no-match; surface it as the
        # domain-specific NotFound so every miss classifies as NOT_FOUND.
        raise not_found(token) from exc
    return resolution.id


async def resolve_notebook(client: NotebookLMClient, ref: str) -> str:
    """Resolve a notebook reference (full/partial id or exact title) to its id.

    Args:
        client: The lifespan-bound client.
        ref: A full canonical UUID, a hex id prefix, or an exact (case-insensitive)
            notebook title.

    Returns:
        The notebook's canonical id.

    Raises:
        ValidationError: ``ref`` is empty/whitespace.
        NotebookNotFoundError: No notebook matches ``ref``.
        AmbiguousIdError: ``ref`` matches more than one notebook by prefix or title.
    """
    ref = validate_id(ref, "notebook")
    # Full UUID fast-path — never list.
    if FULL_ID_PATTERN.fullmatch(ref):
        return ref
    items = await client.notebooks.list()
    if _HEX_ISH.match(ref):
        return _resolve_by_id_or_prefix(ref, items, not_found=NotebookNotFoundError)
    return _resolve_by_title(ref, items, not_found=NotebookNotFoundError)


async def resolve_source(client: NotebookLMClient, notebook_id: str, ref: str) -> str:
    """Resolve a source reference within a notebook to its id.

    Args:
        client: The lifespan-bound client.
        notebook_id: The (already-resolved) notebook id the source lives in.
        ref: A full canonical UUID, a hex id prefix, or an exact (case-insensitive)
            source title.

    Returns:
        The source's canonical id.

    Raises:
        ValidationError: ``ref`` is empty/whitespace.
        SourceNotFoundError: No source in the notebook matches ``ref``.
        AmbiguousIdError: ``ref`` matches more than one source by prefix or title.
    """
    ref = validate_id(ref, "source")
    # Full UUID fast-path — never list.
    if FULL_ID_PATTERN.fullmatch(ref):
        return ref
    items = await client.sources.list(notebook_id)
    if _HEX_ISH.match(ref):
        return _resolve_by_id_or_prefix(ref, items, not_found=SourceNotFoundError)
    return _resolve_by_title(ref, items, not_found=SourceNotFoundError)
