#!/bin/bash

# Tool to check if source URLs are still alive
# usage: ./check_discontinued.sh "url1" "url2" ...

for url in "$@"; do
    echo -n "Checking $url ... "
    status=$(curl -o /dev/null -s -w "%{http_code}" -L -H "User-Agent: Mozilla/5.0" --max-time 10 "$url")
    if [ "$status" == "200" ]; then
        echo "✅ ALIVE"
    elif [ "$status" == "403" ]; then
        echo "🛡️ CLOUDFLARE/FORBIDDEN (Needs investigation)"
    elif [ "$status" == "404" ]; then
        echo "❌ DISCONTINUED (404)"
    else
        echo "⚠️ UNCERTAIN ($status)"
    fi
done
