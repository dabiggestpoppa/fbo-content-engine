# 🎬 Content Farm — Presentation, Video & Image Tools

> **Last Updated:** 2026-06-12 | All tools from your GitHub list + tools-catalog

---

## 📊 Presentations & Decks

### Open Design (Already Forked ✅)
**Location:** `content-farm/design/open-design/`  
**Stars:** 63.9k | **License:** Apache-2.0

**Presentation Skills:**
| Skill | Description |
|-------|-------------|
| `deck-guizang-editorial` | Magazine-style web PPT |
| `deck-open-slide-canvas` | Open slide canvas |
| `deck-swiss-international` | Swiss International-style deck |
| `frontend-slides` | Frontend slide decks |
| `slides` | General slide generation |

**Output Formats:** HTML, PDF, PPTX, MP4  
**Design Systems:** 150 brand-grade (Linear, Stripe, Vercel, Airbnb, Apple, Tesla, Notion, etc.)  
**Plugins:** 261 official plugins

```bash
# Generate a pitch deck
od plugin run web-prototype --brief "Seed round pitch deck for MAD LABS"

# Generate slides
od plugin run slides --brief "Q2 2026 trading performance review"
```

### D2 Diagrams (Already Forked ✅)
**Location:** `tools/d2/`  
**Stars:** 24.4k | **License:** MPL-2.0

Text-to-diagram for architecture docs, flowcharts, system diagrams.

```bash
d2 input.d2 output.svg
d2 input.d2 output.png
```

---

## 🎬 Video Tools

### ReClip (Already Forked ✅)
**Location:** `content-farm/sites/reclip/`  
**Stars:** 6.1k | **License:** MIT

Self-hosted video downloader. 1000+ sites. MP4/MP3. Single Python file backend.

```bash
cd content-farm/sites/reclip
pip install -r requirements.txt
python app.py
# Open http://localhost:8899
```

### Open Design Video Skills (Already Forked ✅)
| Skill | Description |
|-------|-------------|
| `8-bit-orbit-video-template` | 8-bit style video |
| `swiss-user-research-video-template` | User research video |
| `weread-year-in-review-video-template` | Year in review video |
| `video-hyperframes` | HTML → MP4 motion graphics |
| `video-downloader` | Video download skill |
| `fal-video-edit` | AI video editing (Fal.ai) |

### Open Design Video Plugins
| Plugin | Description |
|--------|-------------|
| `video-templates/` | 50+ video templates (HyperFrames, Seedance, Veo) |
| `image-templates/` | 45+ image prompt templates |

### External Video Tools (From Your List)
| Tool | URL | Purpose |
|------|-----|---------|
| cobalt.tools | https://cobalt.tools | Universal video/audio downloader (YT, TikTok, IG, Twitter) |
| yt-dlp | https://github.com/yt-dlp/yt-dlp | CLI YouTube downloader, 4K, subtitles |
| 4K Video Downloader | https://www.4kdownload.com | Desktop YouTube downloader |
| greenvideo.cc | https://greenvideo.cc | Bilibili, Weibo, Xiaohongshu |
| tiktokio.bio | https://tiktokio.bio | Watermark-free TikTok |
| savefrom.net | https://en1.savefrom.net/ | Instagram/Facebook |
| openshorts | (search GitHub) | AI video clipping + subtitles (Opus Clip alternative) |
| openscreen | (search GitHub) | Screen recorder (Screen Studio alternative) |

---

## 🖼️ Image Tools

### Open Design Image Skills (Already Forked ✅)
| Skill | Description |
|-------|-------------|
| `imagegen` | AI image generation |
| `imagegen-frontend-web` | Web-focused image generation |
| `imagegen-frontend-mobile` | Mobile-focused image generation |
| `imagen` | Google Imagen integration |
| `image-enhancer` | Image enhancement |
| `image-to-code-skill` | Image → code conversion |
| `fal-image-edit` | AI image editing (Fal.ai) |
| `venice-image-generate` | Venice AI image generation |
| `venice-image-edit` | Venice AI image editing |
| `ecommerce-image-workflow` | E-commerce image pipeline |
| `pixelbin-media` | Media management |

### Open Design Image Plugins
| Plugin | Description |
|--------|-------------|
| `image-templates/` | 45+ image prompt templates (editorial, cinematic, product, portrait) |

### External Image Tools (From Your List)
| Tool | URL | Purpose |
|------|-----|---------|
| Photopea | https://photopea.com | Browser Photoshop (PSD support) |
| MagicEraser | https://magiceraser.org | Image watermark removal |
| MagicEraser (video) | https://magiceraser.org/remove-watermark-from-video/ | Video watermark removal |
| TinyWow | https://tinywow.com | 300+ free PDF/image/video/AI tools |

---

## 🎙️ Voice & Audio

### Dograh (Already Forked ✅)
**Location:** `vtuber_integration/dograh/`  
**Stars:** 4.4k | **License:** BSD-2-Clause

Self-hosted voice AI platform. TTS/STT, telephony, MCP-native.

### VoiceBox (Just Forked ✅)
**Location:** `tools/voicebox/`  
**Replaces:** ElevenLabs ($22/mo) + WisprFlow ($15/mo)

Local AI voice cloning. 7 TTS engines, 23 languages. Nothing leaves your machine.

### OmniVoice Studio (Not Yet Forked)
**Replaces:** ElevenLabs  
**Features:** Voice cloning, design, dubbing, dictation. 646 languages. Desktop app.

### Whisper (Not Yet Forked)
**Replaces:** Otter ($17/mo)  
**Features:** Open-source STT. 99 languages, translation, timestamps. Local.

---

## 📱 Social Media Templates

### Content Engine (Already Exists ✅)
**Location:** `content-engine/templates/`

| Template | Description |
|----------|-------------|
| `TIKTOK_TEMPLATE.md` | TikTok content template |
| `TWEET_TEMPLATE.md` | Twitter/X post template |

### Open Design Social Skills
| Skill | Description |
|-------|-------------|
| `social-x-post-card` | X/Twitter post card |
| `social-reddit-card` | Reddit post card |
| `social-spotify-card` | Spotify-style card |

---

## 🔧 Content Processing

### PDF Tools
| Tool | Location | Purpose |
|------|----------|---------|
| `extract_pdfs.py` | `quant-lab/` | PDF text extraction |
| `extract_pdf_text.py` | `quant-lab/` | PDF text extraction |
| `extract_pdf_decision_trees.py` | `quant-lab/` | PDF decision tree extraction |
| `generate_pdf_report.py` | `quant-lab/reports/` | PDF report generation |
| `ocr_scanned_pdfs.py` | `scripts/` | OCR for scanned PDFs |

### Open Design Output Formats
| Format | Skill/Plugin |
|--------|-------------|
| HTML | `web-prototype`, `frontend-slides` |
| PDF | Built-in export |
| PPTX | `deck-*` skills |
| MP4 | `video-hyperframes`, `video-templates` |
| PNG/JPG | `imagegen`, `image-templates` |

---

## 🎯 Recommended Workflow

### Create a Presentation
```bash
# 1. Generate deck with Open Design
cd content-farm/design/open-design
od plugin run deck-swiss-international --brief "MAD LABS Q2 Report"

# 2. Export to PPTX/PDF
# (built into Open Design)

# 3. Add diagrams with D2
d2 architecture.d2 architecture.svg
```

### Create Social Media Content
```bash
# 1. Generate image
od plugin run imagegen --brief "Dark scientific dashboard visualization"

# 2. Create post card
od plugin run social-x-post-card --brief "New trading system live"

# 3. Download reference videos
cd content-farm/sites/reclip
python app.py  # Use web UI at localhost:8899
```

### Create Video Content
```bash
# 1. Download source videos
# (ReClip or cobalt.tools)

# 2. Generate shorts with Open Design
od plugin run video-hyperframes --brief "Trading highlights reel"

# 3. Add animations with dotlottie
# (VTuber integration)
```

---

## 📋 Fork Status

| Repo | Forked | Location |
|------|--------|----------|
| nexu-io/open-design | ✅ | `content-farm/design/open-design/` |
| averygan/reclip | ✅ | `content-farm/sites/reclip/` |
| LottieFiles/dotlottie-web | ✅ | `vtuber_integration/dotlottie-web/` |
| dograh-hq/dograh | ✅ | `vtuber_integration/dograh/` |
| jamiepine/voicebox | ✅ | `tools/voicebox/` |
| capcom6/android-sms-gateway | ✅ | `tools/sms-gateway/` |
| terrastruct/d2 | ✅ | `tools/d2/` |
| mattpocock/skills | ✅ | `skills/` |
| Leonxlnx/taste-skill | ✅ | `skills/taste-skill/` |
| virgiliojr94/book-to-skill | ✅ | `core/cognition/procedural/book-to-skill/` |
| microsoft/markitdown | ✅ | `core/parser/markitdown/` |
| opendataloader-project | ✅ | `core/parser/odl-pdf/` |
| run-llama/liteparse | ✅ | `core/parser/liteparse/` |
| datalab-to/chandra | ✅ | `core/parser/chandra/` |
| RyanCodrai/turbovec | ✅ | `core/semantic/vector/turbovec/` |
| teng-lin/notebooklm-py | ✅ | `content-farm/github-repos/notebooklm-py/` |
| Thysrael/Horizon | ✅ | `core/research/horizon/` |
| debpalash/OmniVoice-Studio | ❌ | Needs forking |
| unslothai/unsloth | ❌ | Needs forking |
