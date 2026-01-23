#!/bin/bash

# Tool to fetch HTML from a URL and save it to a temporary file
# Useful for analyzing the structure of new light novel sources

URL=$1
OUTPUT_FILE=$2

if [ -z "$URL" ]; then
    echo "Usage: ./fetch_html.sh <URL> [output_file]"
    exit 1
fi

if [ -z "$OUTPUT_FILE" ]; then
    OUTPUT_FILE="/data/data/com.termux/files/home/.gemini/tmp/fetched_page.html"
fi

# Use curl with a common User-Agent to avoid simple bot detection
curl -L -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36" "$URL" > "$OUTPUT_FILE"

echo "HTML fetched and saved to $OUTPUT_FILE"
