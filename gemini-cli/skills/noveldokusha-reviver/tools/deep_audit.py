import requests
from bs4 import BeautifulSoup
import sys

# Modern User-Agent (Same as App)
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Referer": "https://google.com"
}

SOURCES = [
    {
        "name": "RoyalRoad",
        "url": "https://www.royalroad.com/fiction/21220/mother-of-learning",
        "selectors": {
            "title": "h1",
            "chapters": "tr[data-url]", # My latest fix
            "chapters_fallback": ".table tr a[href*='/fiction/']"
        }
    },
    {
        "name": "WuxiaWorld",
        "url": "https://www.wuxiaworld.com/novel/against-the-gods",
        "selectors": {
            "title": "h1",
            "chapters": ".chapter-item a"
        }
    },
    {
        "name": "NovelBin",
        "url": "https://novelbin.me/novel-book/martial-god-asura",
        "selectors": {
            "title": ".title",
            "chapters": "ul.list-chapter li a"
        }
    },
    {
        "name": "NovelFull",
        "url": "https://novelfull.com/reincarnation-of-the-strongest-sword-god.html",
        "selectors": {
            "title": "h3.title",
            "chapters": ".list-chapter li a"
        }
    },
    {
        "name": "ReadNovelFull",
        "url": "https://readnovelfull.com/reincarnation-of-the-strongest-sword-god-v2.html",
        "selectors": {
            "title": "h3.title",
            "chapters": ".list-chapter li a"
        }
    },
    {
        "name": "LightNovelPub",
        "url": "https://lightnovelpub.me/novel/shadow-slave-19072354",
        "selectors": {
            "title": ".novel-title",
            "chapters": ".chapter-list a"
        }
    },
    {
        "name": "ScribbleHub",
        "url": "https://www.scribblehub.com/series/10700/tree-of-aeons-an-isekai-story/",
        "selectors": {
            "title": ".fic_title",
            "chapters": ".toc_a"
        }
    },
    {
        "name": "Novelhall",
        "url": "https://novelhall.com/perfect-world-10608/",
        "selectors": {
            "title": "h1",
            "chapters": ".book-catalog ul li a"
        }
    },
    {
        "name": "WuxiaClick",
        "url": "https://www.wuxia.click/novel/martial-world",
        "selectors": {
            "title": "h1",
            "chapters": "#chapters a[href]"
        }
    }
]

def test_source(source):
    print(f"Testing {source['name']}...", end=" ")
    try:
        response = requests.get(source['url'], headers=HEADERS, timeout=15)
        if response.status_code != 200:
            print(f"FAILED (Status {response.status_code})")
            return

        soup = BeautifulSoup(response.content, 'html.parser')
        
        # Check Title
        title = soup.select_one(source['selectors']['title'])
        if not title:
            print("FAILED (Title not found)")
            return
            
        # Check Chapters
        chapters = soup.select(source['selectors']['chapters'])
        if not chapters and 'chapters_fallback' in source['selectors']:
             chapters = soup.select(source['selectors']['chapters_fallback'])

        if len(chapters) > 0:
            print(f"PASSED (Found {len(chapters)} chapters)")
        else:
            print("FAILED (No chapters found)")
            # Debug: print first 500 chars of body to see if blocked
            # print(response.text[:500])

    except Exception as e:
        print(f"ERROR: {e}")

if __name__ == "__main__":
    print("Starting Deep Audit...")
    for source in SOURCES:
        test_source(source)
