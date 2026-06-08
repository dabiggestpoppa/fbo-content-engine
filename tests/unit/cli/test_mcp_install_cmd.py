"""Tests for the ``notebooklm mcp install <client>`` CLI command.

The command is a thin Click adapter over :mod:`notebooklm._app.mcp_install`:
it resolves the client's config path (overridable via ``--config-path`` for
tests), then atomically read-modify-merges our server block into the file's
``mcpServers`` object without clobbering unrelated keys.

These tests drive it through ``CliRunner`` against a tmp ``--config-path`` so no
real client config is touched.
"""

from __future__ import annotations

import json
from pathlib import Path

import pytest
from click.testing import CliRunner

from notebooklm._app.mcp_install import build_server_block
from notebooklm.notebooklm_cli import cli


@pytest.fixture
def runner() -> CliRunner:
    return CliRunner()


def _read(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def test_install_creates_config(runner: CliRunner, tmp_path: Path) -> None:
    cfg = tmp_path / "mcp.json"
    result = runner.invoke(cli, ["mcp", "install", "cursor", "--config-path", str(cfg)])
    assert result.exit_code == 0, result.output
    data = _read(cfg)
    assert data["mcpServers"]["notebooklm"] == build_server_block()


def test_install_is_idempotent(runner: CliRunner, tmp_path: Path) -> None:
    cfg = tmp_path / "mcp.json"
    first = runner.invoke(cli, ["mcp", "install", "cursor", "--config-path", str(cfg)])
    assert first.exit_code == 0, first.output
    before = _read(cfg)

    second = runner.invoke(cli, ["mcp", "install", "cursor", "--config-path", str(cfg)])
    assert second.exit_code == 0, second.output
    assert _read(cfg) == before
    # The second run reports it was already configured.
    assert "already" in second.output.lower() or "unchanged" in second.output.lower()


def test_install_preserves_unrelated_keys(runner: CliRunner, tmp_path: Path) -> None:
    cfg = tmp_path / "config.json"
    cfg.write_text(
        json.dumps(
            {
                "theme": "dark",
                "mcpServers": {"other": {"command": "node", "args": ["x.js"]}},
            }
        ),
        encoding="utf-8",
    )
    result = runner.invoke(cli, ["mcp", "install", "claude-desktop", "--config-path", str(cfg)])
    assert result.exit_code == 0, result.output
    data = _read(cfg)
    # Unrelated key and pre-existing server survive; ours is added.
    assert data["theme"] == "dark"
    assert data["mcpServers"]["other"] == {"command": "node", "args": ["x.js"]}
    assert data["mcpServers"]["notebooklm"] == build_server_block()


def test_install_updates_stale_block(runner: CliRunner, tmp_path: Path) -> None:
    cfg = tmp_path / "mcp.json"
    cfg.write_text(
        json.dumps({"mcpServers": {"notebooklm": {"command": "old", "args": []}}}),
        encoding="utf-8",
    )
    result = runner.invoke(cli, ["mcp", "install", "windsurf", "--config-path", str(cfg)])
    assert result.exit_code == 0, result.output
    assert _read(cfg)["mcpServers"]["notebooklm"] == build_server_block()
    assert "updat" in result.output.lower()


def test_unknown_client_errors_cleanly(runner: CliRunner, tmp_path: Path) -> None:
    cfg = tmp_path / "mcp.json"
    result = runner.invoke(cli, ["mcp", "install", "emacs", "--config-path", str(cfg)])
    assert result.exit_code != 0
    # Click rejects the bad choice before the body runs; the supported clients
    # are listed in the usage error.
    assert "emacs" in result.output
    assert "cursor" in result.output
    # No file is written for an invalid client.
    assert not cfg.exists()


def test_install_without_config_path_uses_resolved_location(
    runner: CliRunner, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """Without --config-path, the per-client resolved path under HOME is used."""
    home = tmp_path / "home"
    home.mkdir()
    monkeypatch.setattr(Path, "home", classmethod(lambda cls: home))
    result = runner.invoke(cli, ["mcp", "install", "cursor"])
    assert result.exit_code == 0, result.output
    # Linux default for cursor is ~/.config/cursor/mcp.json; on macOS ~/.cursor/.
    candidates = [
        home / ".config" / "cursor" / "mcp.json",
        home / ".cursor" / "mcp.json",
    ]
    written = [c for c in candidates if c.exists()]
    assert written, f"expected a cursor config under {home}"
    assert _read(written[0])["mcpServers"]["notebooklm"] == build_server_block()


def test_install_prints_path(runner: CliRunner, tmp_path: Path) -> None:
    cfg = tmp_path / "mcp.json"
    result = runner.invoke(cli, ["mcp", "install", "cursor", "--config-path", str(cfg)])
    assert result.exit_code == 0
    assert str(cfg) in result.output
