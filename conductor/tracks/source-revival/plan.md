# Implementation Plan - Source Revival

The goal is to audit and fix all novel sources in `scraper/src/main/java/my/noveldokusha/scraper/sources`.

## Phase 1: Audit
- [x] List all sources
- [x] Run basic audit (check baseUrl, catalogUrl, search)
- [x] Identify broken sources

## Phase 2: Fix (One by One)
- [ ] BestLightNovel (DEAD - Parking)
- [x] BoxNovel (Updated to Webflow)
- [ ] IndoWebnovel (BROKEN - Cloudflare)
- [ ] KoreanNovelsMTL (DEAD - Parking)
- [x] LightNovelPub (OK)
- [x] LightNovelWorld (Updated to .org)
- [x] LightNovelsTranslations (OK)
- [x] MeioNovel (URL and Pagination fixed)
- [x] NovelBin (Updated to .com)
- [x] NovelFull (OK)
- [x] NovelUpdates (OK)
- [x] Novelhall (Search fixed)
- [x] Novelku (OK)
- [x] ReadNovelFull (Search fixed)
- [x] Reddit (TIMEOUT - Likely blocked)
- [x] RoyalRoad (OK)
- [ ] SakuraNovel (BROKEN - Cloudflare)
- [x] ScribbleHub (OK)
- [x] WbNovel (OK)
- [x] Wuxia (OK)
- [x] WuxiaWorld (OK)
- [ ] _1stKissNovel (DEAD - Parking)

## Phase 3: Verification
- [ ] Verify each fix with a test script