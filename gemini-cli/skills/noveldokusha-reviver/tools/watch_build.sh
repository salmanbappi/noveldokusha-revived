#!/bin/bash
# Tool to push changes to GitHub and watch the build
# Usage: ./watch_build.sh "Commit message"

COMMIT_MSG="${1:-Auto-update by Gemini}"

# Add and commit
git add .
git commit -m "$COMMIT_MSG"
git push

# Get the latest run ID
echo "Waiting for workflow run to start..."
sleep 5
LATEST_RUN_ID=$(gh run list --limit 1 --json databaseId --jq '.[0].databaseId')

if [ -z "$LATEST_RUN_ID" ]; then
    echo "No run found."
    exit 1
fi

echo "Watching run ID: $LATEST_RUN_ID"
gh run watch "$LATEST_RUN_ID"

# Check exit status
if [ $? -ne 0 ]; then
    echo "Build FAILED! Fetching logs..."
    gh run view "$LATEST_RUN_ID" --log-failed
else
    echo "Build SUCCESS!"
fi
