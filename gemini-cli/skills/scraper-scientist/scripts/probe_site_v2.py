import sys
import cloudscraper
from bs4 import BeautifulSoup
import json

def probe(url, selectors):
    scraper = cloudscraper.create_scraper(browser={'browser': 'chrome', 'platform': 'windows', 'mobile': False})
    try:
        response = scraper.get(url, timeout=15)
        if response.status_code != 200:
            return {"error": f"HTTP {response.status_code}", "url": url}
        
        soup = BeautifulSoup(response.text, 'html.parser')
        results = {}
        for key, selector in selectors.items():
            elements = soup.select(selector)
            if elements:
                if key == "cover":
                    results[key] = elements[0].get('src') or elements[0].get('data-src') or elements[0].get('abs:src')
                elif key == "chapters":
                    results[key] = f"OK ({len(elements)} found)"
                else:
                    results[key] = elements[0].get_text(strip=True)[:100]
            else:
                results[key] = "NOT_FOUND"
        return results
    except Exception as e:
        return {"error": str(e), "url": url}

if __name__ == "__main__":
    url = sys.argv[1]
    selectors = json.loads(sys.argv[2])
    print(json.dumps(probe(url, selectors), indent=2))
