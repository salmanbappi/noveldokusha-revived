---
name: scraper-scientist
description: Expert in web scraping, Jsoup selector optimization, and source revival for Noveldokusha. Use this skill when auditing broken sources, fixing CSS selectors, or implementing new scrapers.
---

# Scraper Scientist

## Overview
The Scraper Scientist is specialized in maintaining the high-quality scrapers used in Noveldokusha. It focuses on resilience, accuracy, and efficiency when extracting novel metadata, chapter lists, and content from various web sources.

## Workflow Decision Tree

### 1. Source Audit
- **Scenario:** A source is reported as broken.
- **Action:** Use `deep_audit.py` to check `baseUrl`, `catalogUrl`, and `search`.
- **Outcome:** Identify if it's a 404 (URL change), 403 (Cloudflare/Blocking), or 200 with empty results (Selector change).

### 2. Selector Repair
- **Scenario:** 200 OK but no chapters/title found.
- **Action:** Fetch raw HTML using `fetch_html.sh`, identify new CSS selectors.
- **Tools:** Use browser devtools to find robust selectors (prefer class names over deep IDs).

### 3. Resilience Implementation
- **Scenario:** Site uses dynamic classes or changing structures.
- **Action:** Implement fallback selectors or use partial matches (`[class*="content"]`).

## Guidelines
- **Always use `abs:href` and `abs:src`** to get absolute URLs.
- **Prefer `selectFirst()`** for single elements to avoid unnecessary list creation.
- **Handle pagination carefully** - check for "Next" button presence to set `isLastPage`.
- **Text Extraction:** Use `TextExtractor.get(element)` to ensure clean text with preserved spacing.

## Resources

### scripts/
- `deep_audit.py`: Audits multiple sources at once.
- `fetch_html.sh`: Helper to get raw HTML for local debugging.

### references/
- `jsoup_cheatsheet.md`: Common Jsoup patterns.
- `noveldokusha_source_interface.md`: Documentation on `SourceInterface`.