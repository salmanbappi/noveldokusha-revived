# Revival Guardian

🛡️ **Description:** Mandatory validation skill for NovelDokusha project maintenance. Use this to verify resource integrity and Kotlin syntax before pushing changes.

## Workflows

### Pre-Push Validation
Before every `git push` or CI trigger, run:
`./gemini-cli/skills/revival-guardian/scripts/validate_project.sh`

### Error Mitigation
- **Duplicate Resources**: If duplicates are found, move all non-translatable source names to `strings-no-translatable.xml` and ensure they are removed from `strings.xml`.
- **Kotlin Shadowing**: Always use `this@ClassName.property` when modifying state inside `apply` or `run` blocks of Android system classes (like `MediaPlayer`).
- **XML Safety**: Ensure strings with single quotes are wrapped in double quotes (e.g., `<string>"it's safe"</string>`).