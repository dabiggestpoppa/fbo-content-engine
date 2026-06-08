"""``notebooklm-mcp`` entry point — run the MCP server.

Two transports are supported:

* **stdio** (default): the client speaks JSON-RPC over stdin/stdout. stdout must
  carry *pristine* JSON-RPC, so all logging is pinned to **stderr**.
* **http** (loopback): a local streamable-HTTP server. A bind guard refuses any
  non-loopback ``--host`` unless ``NOTEBOOKLM_MCP_ALLOW_EXTERNAL_BIND=1`` is set,
  so an MCP server is never accidentally exposed to the network.

The auth profile is bound once at startup via ``--profile`` /
``NOTEBOOKLM_PROFILE``. This module imports NO ``click`` / ``rich`` / ``cli``.
"""

from __future__ import annotations

import argparse
import ipaddress
import logging
import os
import sys

from .server import create_server

__all__ = ["main"]

#: Env var that opts a deployment into binding the HTTP transport to a
#: non-loopback interface. Off by default — the server is local-first.
ALLOW_EXTERNAL_BIND_ENV = "NOTEBOOKLM_MCP_ALLOW_EXTERNAL_BIND"

#: Hostnames that are always treated as loopback even though they are not numeric
#: IP literals.
_LOOPBACK_HOSTNAMES = frozenset({"localhost", ""})


def _configure_logging(level: str) -> None:
    """Pin logging to stderr — the stdio transport requires uncontaminated stdout."""
    logging.basicConfig(
        stream=sys.stderr,
        level=getattr(logging, level.upper(), logging.INFO),
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )


def _is_loopback(host: str) -> bool:
    """Return whether ``host`` resolves to a loopback interface."""
    if host in _LOOPBACK_HOSTNAMES:
        return True
    try:
        return ipaddress.ip_address(host).is_loopback
    except ValueError:
        # A non-numeric, non-"localhost" hostname (e.g. a public DNS name) is NOT
        # treated as loopback — fail closed.
        return False


def _check_http_bind_allowed(host: str, *, allow_external: bool) -> None:
    """Refuse to bind the HTTP transport to a non-loopback host unless opted in.

    Raises:
        SystemExit: ``host`` is not loopback and ``allow_external`` is ``False``.
    """
    if _is_loopback(host) or allow_external:
        return
    raise SystemExit(
        f"Refusing to bind the MCP HTTP transport to non-loopback host '{host}'. "
        f"This would expose the server to the network. Set "
        f"{ALLOW_EXTERNAL_BIND_ENV}=1 to override (only behind a trusted proxy)."
    )


def _build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="notebooklm-mcp",
        description="Run the notebooklm-py MCP server.",
    )
    parser.add_argument(
        "--profile",
        default=os.environ.get("NOTEBOOKLM_PROFILE"),
        help="Auth profile to bind for this server process (default: active profile).",
    )
    parser.add_argument(
        "--transport",
        choices=("stdio", "http"),
        default=os.environ.get("NOTEBOOKLM_MCP_TRANSPORT", "stdio"),
        help="Transport: 'stdio' (default) or loopback 'http'.",
    )
    parser.add_argument(
        "--host",
        default=os.environ.get("NOTEBOOKLM_MCP_HOST", "127.0.0.1"),
        help="HTTP bind host (http transport only; loopback unless overridden).",
    )
    parser.add_argument(
        "--port",
        type=int,
        default=int(os.environ.get("NOTEBOOKLM_MCP_PORT", "8000")),
        help="HTTP bind port (http transport only; default: 8000).",
    )
    parser.add_argument(
        "--log-level",
        default=os.environ.get("NOTEBOOKLM_LOG_LEVEL", "INFO"),
        help="Logging level on stderr (default: INFO).",
    )
    return parser


def main(argv: list[str] | None = None) -> None:
    """Parse args, enforce the bind guard, and run the server."""
    args = _build_parser().parse_args(argv)
    _configure_logging(args.log_level)

    server = create_server(profile=args.profile)

    if args.transport == "http":
        allow_external = os.environ.get(ALLOW_EXTERNAL_BIND_ENV) == "1"
        _check_http_bind_allowed(args.host, allow_external=allow_external)
        server.run(transport="http", host=args.host, port=args.port)
    else:
        server.run(transport="stdio")


if __name__ == "__main__":
    main()
