import sys
import cloudscraper
from bs4 import BeautifulSoup
import json
import time

SOURCES = {
    "LightNovelPub": {
        "url": "https://www.lightnovelpub.com/novel/shadow-slave-16051515",
        "selectors": {
            "title": "h1.novel-title",
            "cover": ".fixed-img img",
            "chapters": "li.chapter-item a"
        }
    },
    "ScribbleHub": {
        "url": "https://www.scribblehub.com/series/10700/mother-of-learning/",
        "selectors": {
            "title": ".fic_title",
            "cover": ".fic_image img",
            "chapters": ".toc_a"
        }
    },
    "NovelUpdates": {
        "url": "https://www.novelupdates.com/series/lord-of-the-mysteries/",
        "selectors": {
            "title": ".seriestitlenwrap",
            "cover": ".seriesimg img",
            "chapters": ".chp-release"
        }
    }
}

def audit():
    # Use cloudscraper to bypass initial CF wall
    scraper = cloudscraper.create_scraper(browser={'browser': 'chrome', 'platform': 'windows', 'mobile': False})
    
    report = {}
    for name, data in SOURCES.items():
        print(f"Auditing {name} (Bypassing Cloudflare)...")
        try:
            res = scraper.get(data['url'], timeout=20)
            if res.status_code != 200:
                report[name] = f"FAILED: HTTP {res.status_code} (Cloudflare might be too strong)"
                continue
            
            soup = BeautifulSoup(res.text, 'html.parser')
            checks = {}
            for key, sel in data['selectors'].items():
                found = soup.select(sel)
                checks[key] = f"OK ({len(found)} found)" if found else "MISSING"
            
            report[name] = checks
        except Exception as e:
            report[name] = f"ERROR: {str(e)}"
        time.sleep(2)
    
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    audit()