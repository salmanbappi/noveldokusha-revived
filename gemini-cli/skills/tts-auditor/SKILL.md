---
name: tts-auditor
description: Specialized in auditing and improving the Text-to-Speech (TTS) experience in Noveldokusha. Use this skill when fixing playback issues, improving text normalization, or adding new TTS engines.
---

# Tts Auditor

## Overview
The TTS Auditor ensures a smooth, immersive reading experience by optimizing how text is processed and spoken. It focuses on text splitting, voice selection, and handling of special characters/artifacts in scraped content.

## Workflow

### 1. Playback Audit
- **Check:** Are there long pauses? Is the speech robotic?
- **Action:** Adjust `delimiterAwareTextSplitter` settings or check voice quality (`needsInternet`).

### 2. Text Normalization
- **Scenario:** TTS reads "Chapter 1 dot 2" instead of "Chapter 1 point 2" or trips over emojis.
- **Action:** Implement regex-based cleaning before passing text to `speak()`.

### 3. Artifact Filtering
- **Scenario:** TTS reads "Subscribe to our Patreon" ads embedded in text.
- **Action:** Use Gemini-based cleaning or refined Jsoup selectors to remove non-story content.

## Resources

### scripts/
- `test_tts_flow.sh`: Simulates a TTS session for debugging.

### references/
- `android_tts_api.md`: Reference for `android.speech.tts`.
- `normalization_rules.json`: Common regex patterns for cleaning novel text.