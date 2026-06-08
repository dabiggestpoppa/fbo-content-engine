"""Guardrail: no test package dir may share a name with an installed top-level package.

In pytest's default `prepend` import mode a packaged test dir becomes importable
under its own basename; if that basename matches an installed distribution (e.g.
the `mcp` SDK), it shadows it and breaks `import <name>` everywhere. See
docs/plans/2026-06-08-mcp-server-redesign-design.md section 6.
"""
from __future__ import annotations

import importlib.util
from pathlib import Path

TESTS_ROOT = Path(__file__).resolve().parents[1]


def test_no_test_package_dir_shadows_installed_package() -> None:
    offenders = []
    for init in TESTS_ROOT.rglob("__init__.py"):
        pkg_dir = init.parent
        # Only dirs whose parent is NOT itself a package can become top-level.
        if (pkg_dir.parent / "__init__.py").exists():
            continue
        name = pkg_dir.name
        spec = importlib.util.find_spec(name)
        if spec is not None and "site-packages" in str(spec.origin or ""):
            offenders.append(f"{pkg_dir} shadows installed package '{name}' ({spec.origin})")
    assert not offenders, "Test dirs shadow installed packages:\n" + "\n".join(offenders)
