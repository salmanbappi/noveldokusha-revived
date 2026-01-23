package my.noveldokusha.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Universal Scraper with Cloudflare Worker Fallback
 */
class Scraper {
    
    // User should set their deployed worker URL here or in settings
    private var workerUrl: String = "https://your-worker.workers.dev/?url="

    fun getSelectors(url: String): Map<String, String>? {
        return when {
            url.contains("royalroad.com") -> mapOf(
                "title" to "h1", "cover" to "img.thumbnail", "chapter_list" to "#chapters tbody tr a[href]"
            )
            url.contains("wuxiaworld.com") -> mapOf(
                "title" to "h1", "cover" to "img[src*=covers]", "chapter_list" to "a[href*=-chapter-]"
            )
            url.contains("lightnovelpub.com") -> mapOf(
                "title" to "h1.novel-title", "cover" to ".fixed-img img", "chapter_list" to "li.chapter-item a"
            )
            url.contains("scribblehub.com") -> mapOf(
                "title" to ".fic_title", "cover" to ".fic_image img", "chapter_list" to ".toc_a"
            )
            url.contains("novelupdates.com") -> mapOf(
                "title" to ".seriestitlenwrap", "cover" to ".seriesimg img", "chapter_list" to ".chp-release"
            )
            url.contains("novelfull.com") || url.contains("readnovelfull.com") -> mapOf(
                "title" to "h3.title", "cover" to ".book img", "chapter_list" to "#list-chapter li a"
            )
            else -> null
        }
    }

    suspend fun scrapeBook(url: String): ScrapedBook? {
        val selectors = getSelectors(url) ?: return null
        
        // Strategy: 1. Try Direct, 2. If blocked (403/503), Try Worker Proxy
        var doc = tryFetch(url)
        
        if (doc == null || doc.text().contains("Cloudflare") || doc.text().contains("Just a moment")) {
            doc = tryFetch(workerUrl + url)
        }

        return doc?.let { d ->
            val title = d.selectFirst(selectors["title"]!!)?.text() ?: "Unknown"
            val coverUrl = d.selectFirst(selectors["cover"]!!)?.attr("abs:src") ?: ""
            val chapters = d.select(selectors["chapter_list"]!!).map { 
                ScrapedChapter(it.text(), it.attr("abs:href"))
            }.filter { it.name.isNotEmpty() }

            if (chapters.isEmpty()) null else ScrapedBook(title, coverUrl, chapters)
        }
    }

    private fun tryFetch(url: String): Document? {
        return try {
            Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", url)
                .timeout(30000)
                .get()
        } catch (e: Exception) {
            null
        }
    }
}

data class ScrapedBook(val title: String, val coverUrl: String, val chapters: List<ScrapedChapter>)
data class ScrapedChapter(val name: String, val url: String)
