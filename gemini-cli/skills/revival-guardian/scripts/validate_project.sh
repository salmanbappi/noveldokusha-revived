#!/bin/bash

# Survival Guardian - Project Validation Tool
# Checks for common pitfalls before pushing to CI

echo "🛡️ Starting Revival Guardian Validation..."

# 1. Check for Duplicate String Resources
echo "🔍 Checking for duplicate string resources..."
DUPLICATES=$(cat strings/src/main/res/values/strings.xml strings/src/main/res/values/strings-no-translatable.xml | grep "name=\"" | sed 's/.*name=\"\([^\"]*\").*/\1/' | sort | uniq -d)

if [ -n "$DUPLICATES" ]; then
    echo "❌ ERROR: Duplicate string resources found:"
    echo "$DUPLICATES"
    exit 1
else
    echo "✅ No duplicate strings found."
fi

# 2. Check for invalid unescaped single quotes in XML
echo "🔍 Checking for unescaped single quotes in XML..."
INVALID_QUOTES=$(grep -E "<string[^>]*>[^\"<]*'[^\"<]*</string>" strings/src/main/res/values/strings.xml)
if [ -n "$INVALID_QUOTES" ]; then
    WRONG_STRINGS=$(grep -E "<string[^>]*>[^\"<]*'[^\"<]*</string>" strings/src/main/res/values/strings.xml | grep -v "\">\"")
    if [ -n "$WRONG_STRINGS" ]; then
        echo "❌ ERROR: These strings contain ' but are not wrapped in double quotes:"
        echo "$WRONG_STRINGS"
        exit 1
    else
        echo "✅ All single quotes are properly wrapped in double quotes."
    fi
else
    echo "✅ XML strings are properly quoted/escaped."
fi

# 3. Check for specific Kotlin pitfalls (e.g. shadowing 'isPlaying' in MediaPlayer)
echo "🔍 Checking for Kotlin shadowing pitfalls..."
# Check for reassignment (excluding declarations) without this@
# Look for lines like "isPlaying = true" or "isPlaying = false" that don't start with "private var" or "val"
SHADOWING=$(grep -r "isPlaying = " tooling/text_to_speech/src/main/java/my/noveldokusha/text_to_speech/ | grep -vE "(this@|private var|val )")
if [ -n "$SHADOWING" ]; then
    echo "❌ ERROR: Potential property shadowing in MediaPlayer lambdas detected. Use 'this@GeminiNarrator.isPlaying':"
    echo "$SHADOWING"
    exit 1
else
    echo "✅ No obvious shadowing pitfalls found."
fi

echo "🚀 Validation PASSED! Safe to push."
exit 0
