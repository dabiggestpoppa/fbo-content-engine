# 🔄 FBO Content Engine — Recovery Manifest

**Recovered 2026-08-19.** This repo is the rebuilt home of the MAD Content Farm after the
Aug 17, 2026 workspace cleanup deleted `projects/content/` from `larger-lab` and the
`content-farm/*` gitlinks were purged from GitHub.

## Recovered ✅

### The content farm itself (the "brain")
`content-farm/` — 47 files restored from larger-lab git history
(commit `2ef20eb5`, pre-cleanup state, deleted by `edd00d13`):

- `README.md`, `CIVITAI_INTEGRATION.md` — farm philosophy, tool stack, revenue targets
- `config/` — accounts, civitai-token, content/crawler/analytics YAMLs
- `scripts/` — civitai_scraper, remix_pipeline, posting_queue, translate_content,
  unified_crawler, content_generator, content_tracker, farm_status,
  dy_auto_engage.js, xhs_auto_engage.js
- `templates/` — captions + cooking/finance/fitness/lifestyle/tech templates
- `coordination/` — content calendar, strategy, status, posting schedule
- `logs/`, `output/`, `reports/` — run history and generated content samples

### Tool repos (the "muscle") — `tools/`
All re-cloned at the **exact commits** recorded in larger-lab's git history.
Nested `.git` dirs were removed — code is vendored here so it can't vanish again.

| Repo | Recorded SHA | Recovered from | Match |
|------|-------------|----------------|-------|
| MediaCrawler | `13b00f7a` | NanmiCoder/MediaCrawler | ✅ exact |
| MoneyPrinterPlus | `7b8ef292` | ddean2009/MoneyPrinterPlus (renamed from harry0703) | ✅ exact |
| GroupControlApp | `df977bf6` | DeekeScript/GroupControlApp | ✅ exact |
| DeekeScriptVscodePlugins | `83872223` | DeekeScript/DeekeScriptVscodePlugins | ✅ exact |
| deeke-uid | `be3f36df` | DeekeScript/deeke-uid | ✅ exact |
| shortLink | `98ab5f94` | DeekeScript/shortLink | ✅ exact |
| Spider_XHS | *(no SHA recorded)* | cv-cat/Spider_XHS (fork; original NanmiCoder repo deleted) | ⚠️ approximate |

### Already here
- `fbo-content-engine` = the recovered **notebooklm-py** gitlink
  (recorded SHA `b8548cbd` is in this repo's history) — rebranded & extended.

## Truly lost ❌ (structure preserved as stubs)

These were your own repos — deleted from GitHub, no local copy, no fork anywhere.
Gitlink SHAs below are the last recorded commits (from larger-lab history, commits
`67590c25` / `cfe2245e`). Stubs live in `sites/`, `design/`, `github-repos/`.

| Repo | Last SHA | Notes |
|------|----------|-------|
| sites/fbo-codegraph | `e3143cb7` | code graph site |
| sites/fbo-prediction-pulse | `69925bab` | prediction market site |
| sites/fbo-skills | `8afc1514` | skills site |
| sites/fbo-voice | `9057a41a` | voice content site |
| sites/reclip | `37effdc4` | video clipping site |
| design/open-design | `596abaae` | design assets (partial node_modules copy survived; source gone) |
| github-repos/RuView | `992c2b25` | unknown |
| github-repos/ai-polymarket-agent | `f035e47b` | polymarket AI agent |
| github-repos/codegraph | `16c73e2b` | code graph tool |
| github-repos/dograh | `a81cccc6` | unknown |
| github-repos/skills | `694fa303` | agent skills |

Also gone: `projects/content/deekescript/` (exists at
`Desktop/projects/@deekeScript`), `deeke-uid` ✅ recovered, and the `shortLink` gitlink ✅ recovered.

## How to avoid this again

- This repo now **vendors** tool code instead of gitlinks — deletions can't orphan it.
- Larger-lab's `projects/content/content-farm/` was also restored to the worktree
  (`Desktop/larger-lab/projects/content/content-farm/`, 47 files, uncommitted) as a second copy.
