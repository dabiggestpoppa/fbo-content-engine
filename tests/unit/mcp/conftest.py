"""Fixtures for MCP tool/server unit tests.

The MCP server is exercised through FastMCP's in-memory :class:`fastmcp.Client`
against a server whose lifespan yields a mocked ``NotebookLMClient`` (injected
via the ``client_factory`` seam). Tests configure the mock's namespace methods
(``mock_client.notebooks.list = AsyncMock(return_value=...)``) and assert on the
serialized ``structured_content`` plus that the right API method was called.

Return values can be plain local ``@dataclass`` fakes — ``to_jsonable`` converts
any dataclass — so tests need not construct real core types.
"""

from __future__ import annotations

import contextlib
from collections.abc import AsyncIterator, Callable
from typing import Any
from unittest.mock import AsyncMock, MagicMock

import pytest
from fastmcp import Client, FastMCP

from notebooklm.mcp.server import create_server

# Public client namespaces the tools reach through. Each is a MagicMock whose
# async methods tests override with AsyncMock.
_NAMESPACES = (
    "notebooks",
    "sources",
    "chat",
    "artifacts",
    "research",
    "notes",
    "sharing",
    "labels",
    "settings",
    "mind_maps",
)


@pytest.fixture
def mock_client() -> MagicMock:
    """A ``MagicMock`` standing in for ``NotebookLMClient`` with namespace attrs."""
    client = MagicMock()
    for namespace in _NAMESPACES:
        setattr(client, namespace, MagicMock())
    return client


def _server_for(mock_client: MagicMock) -> FastMCP:
    @contextlib.asynccontextmanager
    async def factory() -> AsyncIterator[MagicMock]:
        yield mock_client

    return create_server(client_factory=factory)


@pytest.fixture
def server_factory(mock_client: MagicMock) -> Callable[[], FastMCP]:
    """Return a zero-arg builder for a server bound to ``mock_client``."""
    return lambda: _server_for(mock_client)


@pytest.fixture
def mcp_call(mock_client: MagicMock) -> Callable[..., Any]:
    """Return ``async (tool_name, args=None) -> ToolResult`` against the mock."""

    async def _call(tool_name: str, args: dict[str, Any] | None = None) -> Any:
        async with Client(_server_for(mock_client)) as client:
            return await client.call_tool(tool_name, args or {})

    return _call


@pytest.fixture
def mcp_list_tools(mock_client: MagicMock) -> Callable[[], Any]:
    """Return ``async () -> list[Tool]`` (the registered tool manifest)."""

    async def _list() -> Any:
        async with Client(_server_for(mock_client)) as client:
            return await client.list_tools()

    return _list


__all__ = ["AsyncMock"]  # re-exported for convenience in tool tests
