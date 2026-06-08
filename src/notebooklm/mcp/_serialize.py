"""JSON-able serialization for the MCP wire shape.

The MCP server owns its own wire shape (not the CLI's ``--json``), but the
recursive object-graph -> JSON-able conversion is identical, so this module is a
thin re-export of the single neutral source of truth,
:func:`notebooklm._app.serialize.to_jsonable`. Importing through here keeps the
MCP adapters' import sites uniform (``from .._serialize import to_jsonable``)
and gives a single seam if the MCP layer ever needs a wire-specific wrapper.
"""

from __future__ import annotations

from .._app.serialize import to_jsonable

__all__ = ["to_jsonable"]
