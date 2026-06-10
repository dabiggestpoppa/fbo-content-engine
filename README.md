# 🎬 FBO Content Engine

**Automated content generation from trading research.**

Turn any trading research, market analysis, or financial data into podcasts, videos, slide decks, quizzes, infographics, flashcards, and more — automatically.

Powered by [notebooklm-py](https://github.com/teng-lin/notebooklm-py). Rebranded and extended by [FBO](https://github.com/dabiggestpoppa).

## What It Does

Feed it your trading research → get back:

| Output | Format | Use Case |
|--------|--------|----------|
| 🎙️ **Audio Overviews** | MP3/MP4 | YouTube, Podcasts, TikTok voiceovers |
| 🎬 **Video Overviews** | MP4 | YouTube, TikTok, Reels |
| 📊 **Slide Decks** | PDF/PPTX | Instagram Carousels, LinkedIn |
| 📈 **Infographics** | PNG | Instagram, Twitter |
| ❓ **Quizzes** | JSON/MD/HTML | Twitter Threads, Blog |
| 🃏 **Flashcards** | JSON/MD/HTML | Instagram Stories, Blog |
| 📝 **Reports** | Markdown | Blog, Newsletter, LinkedIn |
| 📋 **Data Tables** | CSV | Twitter, Blog |
| 🧠 **Mind Maps** | JSON | Instagram Carousels, YouTube |

## Quick Start

```bash
pip install fbo-content-engine

# Set up authentication
notebooklm auth login

# Create a notebook from your research
notebooklm notebook create --title "BTC Market Analysis Week 32"

# Add sources (URLs, PDFs, YouTube, text)
notebooklm source add --notebook <id> --url "https://example.com/btc-analysis"

# Generate content
notebooklm generate audio --notebook <id> --format deep-dive --language en
notebooklm generate video --notebook <id> --format explainer --style cinematic
notebooklm generate slides --notebook <id> --format detailed
notebooklm generate infographic --notebook <id> --orientation portrait
notebooklm generate quiz --notebook <id> --difficulty medium --count 10
```

## Content Pipeline for Trading

```python
from fbo_content_engine import Notebook, ContentPipeline

# Load your research
pipeline = ContentPipeline()
notebook = notebook.create("Weekly Market Report")

# Add trading research sources
notebook.add_sources([
    "https://tradingview.com/analysis/btcusd",
    "https://coindesk.com/markets/2024/06/10/...",
    "./my_btc_analysis.pdf",
])

# Generate all content formats at once
outputs = pipeline.generate_all(notebook, formats=[
    "audio", "video", "slides", "infographic", "quiz", "report"
])

# outputs.audio → MP3 for YouTube/Podcast
# outputs.video → MP4 for TikTok/Reels
# outputs.slides → PDF for Instagram carousel
# outputs.infographic → PNG for Twitter
# outputs.quiz → Twitter thread content
# outputs.report → Blog post markdown
```

## Why FBO Content Engine?

- **One input, 10+ outputs** — Feed it research once, get content for every platform
- **Trading-optimized** — Pre-configured templates for market analysis, trade recaps, educational content
- **Batch mode** — Generate content for entire content calendars in one run
- **Download everything** — All artifacts saved locally, no cloud lock-in

## Credits

Based on [notebooklm-py](https://github.com/teng-lin/notebooklm-py) by Teng Lin. Extended and maintained by FBO.

## License

MIT
