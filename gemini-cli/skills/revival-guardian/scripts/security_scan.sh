#!/bin/bash

TARGET_DIR="${1:-.}"

echo "Scanning for security patterns in $TARGET_DIR..."

# 1. Secrets
echo "🔑 Checking for potential hardcoded secrets..."
grep -rE "API_KEY|api_key|password|secret|token" "$TARGET_DIR" --include="*.kt" --include="*.xml" --exclude-dir="build" --exclude-dir=".git" | grep -v "Example" | grep -v "Test" > secrets_report.txt

if [ -s secrets_report.txt ]; then
    echo "⚠️  Potential secrets found:"
    cat secrets_report.txt
    rm secrets_report.txt
else
    echo "✅ No obvious secrets found."
    rm -f secrets_report.txt
fi

# 2. Unsafe functions
echo "🧨 Checking for unsafe function usage..."
grep -r "Runtime.getRuntime().exec" "$TARGET_DIR" --include="*.kt" > unsafe_report.txt

if [ -s unsafe_report.txt ]; then
    echo "🚨 Unsafe Code Exec found:"
    cat unsafe_report.txt
    rm unsafe_report.txt
else
    echo "✅ No unsafe code execution found."
    rm -f unsafe_report.txt
fi

echo "Scan complete."
