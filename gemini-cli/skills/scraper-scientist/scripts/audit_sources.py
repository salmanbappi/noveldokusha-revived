import sys
import requests
from bs4 import BeautifulSoup
import json
import time

SOURCES = {
    "RoyalRoad": {
        "url": "https://www.royalroad.com/fiction/21220/mother-of-learning",
        "selectors": {
            "title": "h1",
            "cover": "img.thumbnail",
            "chapters": "#chapters tbody tr a[href]"
        }
    },
    "WuxiaWorld": {
        "url": "https://www.wuxiaworld.com/novel/against-the-gods",
        "selectors": {
            "title": "h1",
            "cover": "img[src*=covers]",
            "chapters": "a[href*=-chapter-]"
        }
    },
    "NovelBin": {
        "url": "https://novelbin.com/b/shadow-slave",
        "selectors": {
            "title": ".title",
            "cover": ".book img",
            "chapters": "li a"
        }
    }
}

def audit():
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
    }
    report = {}
    for name, data in SOURCES.items():
        print(f"Auditing {name}...")
        try:
            res = requests.get(data['url'], headers=headers, timeout=15)
            if res.status_code != 200:
                report[name] = f"FAILED: HTTP {res.status_code}"
                continue
            
            soup = BeautifulSoup(res.text, 'html.parser')
            checks = {}
            for key, sel in data['selectors'].items():
                found = soup.select(sel)
                # Fallback for cover
                if key == "cover" and not found:
                    title = soup.select_one("h1").get_text(strip=True) if soup.select_one("h1") else ""
                    found = soup.select(f"img[alt='{title}']")
                
                checks[key] = f"OK ({len(found)} found)" if found else "MISSING"
            
            report[name] = checks
        except Exception as e:
            report[name] = f"ERROR: {str(e)}"
        time.sleep(1)
    
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    audit()
