import sys
import cloudscraper
import requests
import json
import time
import socket

SOURCES = {
    "1stKissNovel": "https://1stkissnovel.love/", # Updated URL
    "BacaLightnovel": "https://bacalightnovel.id/", # Updated URL
    "BestLightNovel": "https://bestlightnovel.com/",
    "BoxNovel": "https://boxnovel.com/",
    "IndoWebnovel": "https://indowebnovel.id/",
    "LightNovelWorld": "https://www.lightnovelworld.com/",
    "LightNovelPub": "https://www.lightnovelpub.com/",
    "MeioNovel": "https://meionovel.id/",
    "MoreNovel": "https://morenovel.net/",
    "MTLNovel": "https://www.mtlnovel.com/",
    "NovelUpdates": "https://www.novelupdates.com/",
    "NovelBin": "https://novelbin.com/",
    "NovelFull": "https://novelfull.com/",
    "NovelHall": "https://www.novelhall.com/",
    "NovelKu": "https://novelku.id/",
    "ReadLightNovel": "https://readlightnovel.me/",
    "ReadNovelFull": "https://readnovelfull.com/",
    "RoyalRoad": "https://www.royalroad.com/",
    "Saikai": "https://www.saikai.com.br/",
    "SakuraNovel": "https://sakuranovel.id/",
    "ScribbleHub": "https://www.scribblehub.com/",
    "Sousetsuka": "https://sousetsuka.com/",
    "WuxiaWorld": "https://www.wuxiaworld.com/"
}

def check_dns(domain):
    try:
        socket.gethostbyname(domain.split('//')[-1].split('/')[0])
        return True
    except:
        return False

def deep_audit():
    scraper = cloudscraper.create_scraper(browser={'browser': 'chrome', 'platform': 'windows', 'mobile': False})
    report = {}
    
    for name, url in SOURCES.items():
        print(f"Deep checking {name} ({url})...")
        status = {"url": url}
        
        # 1. DNS Check
        if not check_dns(url):
            status["dns"] = "FAILED (Domain might be dead)"
            report[name] = status
            continue
        else:
            status["dns"] = "OK"
            
        # 2. Connection Check (Standard)
        try:
            res = requests.get(url, timeout=10, headers={'User-Agent': 'Mozilla/5.0'})
            status["std_http"] = res.status_code
        except Exception as e:
            status["std_http"] = f"ERROR: {str(e)}"
            
        # 3. Connection Check (Cloudscraper)
        try:
            res = scraper.get(url, timeout=15)
            status["cf_http"] = res.status_code
            if res.status_code == 200:
                status["status"] = "✅ ALIVE"
            elif res.status_code == 403:
                status["status"] = "🛡️ PROTECTED (CF Strong)"
            else:
                status["status"] = f"⚠️ UNCERTAIN ({res.status_code})"
        except Exception as e:
            status["cf_http"] = f"ERROR: {str(e)}"
            status["status"] = "❌ DEAD/TIMEOUT"
            
        report[name] = status
        time.sleep(1)
        
    print("\n--- DEEP AUDIT REPORT ---")
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    deep_audit()
