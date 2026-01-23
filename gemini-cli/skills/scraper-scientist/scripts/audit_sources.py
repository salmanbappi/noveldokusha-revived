import sys
import requests
from bs4 import BeautifulSoup
import json
import time

SOURCES = {
    "BestLightNovel": {
        "url": "https://bestlightnovel.com/book/lord_of_the_mysteries_novel",
        "selectors": {
            "title": "h1",
            "cover": ".info_image img",
            "chapters": ".chapter-list .row"
        }
    },
    "BoxNovel": {
        "url": "https://boxnovel.com/novel/the-beginning-after-the-end/",
        "selectors": {
            "title": ".post-title h1",
            "cover": ".summary_image img",
            "chapters": ".wp-manga-chapter"
        }
    },
    "RoyalRoad": {
        "url": "https://www.royalroad.com/fiction/21220/mother-of-learning",
        "selectors": {
            "title": "h1",
            "cover": ".thumbnail",
            "chapters": ".table-striped tbody tr"
        }
    },
    "WuxiaWorld": {
        "url": "https://www.wuxiaworld.com/novel/against-the-gods",
        "selectors": {
            "title": "h1",
            "cover": "img.mx-auto",
            "chapters": ".chapter-item"
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
                found = soup.select_one(sel)
                checks[key] = "OK" if found else "MISSING"
            
            report[name] = checks
        except Exception as e:
            report[name] = f"ERROR: {str(e)}"
        time.sleep(1)
    
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    audit()
