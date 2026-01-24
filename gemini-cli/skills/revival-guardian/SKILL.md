---
name: revival-guardian
description: A comprehensive code review and project health guardian. Use this to perform deep static analysis, architectural compliance checks, security scanning, and code quality reviews before finalizing tasks.
---

# Revival Guardian: The Code Reviewer

## Overview
Revival Guardian is now a full-fledged automated code review assistant. It goes beyond simple syntax checks to ensure architectural integrity, code maintainability, and security.

## Capabilities

### 1. Static Code Analysis (`scripts/review_code.py`)
- **Complexity Metrics:** Identifies functions > 50 lines and classes > 500 lines.
- **Deep Nesting:** Flags logic nested more than 4 levels deep.
- **Comment Density:** Warns if code has too few comments (< 10%).

### 2. Architectural Integrity (`scripts/analyze_architecture.py`)
- **Layer Violations:** Ensures `domain` layer does not import `ui` or `framework` dependencies.
- **Circular Dependencies:** Detects potential cycles between modules (basic check).
- **Clean Architecture:** Enforces separation of concerns (e.g., ViewModels shouldn't reference Android View classes directly).

### 3. Security & Safety (`scripts/security_scan.sh`)
- **Secret Detection:** Scans for potential API keys, tokens, or hardcoded passwords.
- **Unsafe Calls:** Flags usage of unsafe functions (e.g., `Runtime.exec`, implicit `PendingIntent`).

### 4. Project Health (`scripts/validate_project.sh`)
- **Resource Integrity:** Checks for duplicate strings and XML errors.
- **Build Configuration:** Validates `build.gradle.kts` consistency.

## Usage

### Run Full Review
To perform a complete project audit:
```bash
./gemini-cli/skills/revival-guardian/scripts/full_review.sh
```

### Run Specific Checks
*   **Code Quality:** `python3 gemini-cli/skills/revival-guardian/scripts/review_code.py`
*   **Architecture:** `python3 gemini-cli/skills/revival-guardian/scripts/analyze_architecture.py`
*   **Safety:** `bash gemini-cli/skills/revival-guardian/scripts/security_scan.sh`

## Review Standards
*   **Zero Tolerance:** Architectural violations (e.g., Domain -> UI) must be fixed immediately.
*   **Warnings:** Complexity and nesting issues should be refactored if possible, but can be suppressed with valid justification.
