import sys
import requests
from bs4 import BeautifulSoup
import json

def probe(url, selectors):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Referer': url
    }
    try:
        response = requests.get(url, headers=headers, timeout=15)
        if response.status_code != 200:
            return {"error": f"HTTP {response.status_code}", "url": url}
        
        soup = BeautifulSoup(response.text, 'html.parser')
        results = {}
        for key, selector in selectors.items():
            element = soup.select_one(selector)
            if element:
                if key == "cover":
                    results[key] = element.get('src') or element.get('data-src') or element.get('abs:src')
                else:
                    results[key] = element.get_text(strip=True)[:100] + "..."
            else:
                results[key] = "NOT_FOUND"
        return results
    except Exception as e:
        return {"error": str(e), "url": url}

if __name__ == "__main__":
    # Usage: python probe.py <url> <json_selectors>
    url = sys.argv[1]
    selectors = json.loads(sys.argv[2])
    print(json.dumps(probe(url, selectors), indent=2))
