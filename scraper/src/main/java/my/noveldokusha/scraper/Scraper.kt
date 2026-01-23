package my.noveldokusha.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Scraper for verified reliable sources after Laboratory Audit (Jan 2026)
 */
class Scraper {
    
    fun getSelectors(url: String): Map<String, String>? {
        return when {
            url.contains("royalroad.com") -> mapOf(
                "title" to "h1",
                "cover" to "img.thumbnail",
                "chapter_list" to ".table-striped tbody tr a",
                "content" to ".chapter-inner"
            )
            url.contains("wuxiaworld.com") -> mapOf(
                "title" to "h1",
                "cover" to "img.mx-auto",
                "chapter_list" to ".chapter-item a",
                "content" to ".chapter-content"
            )
            url.contains("novelbin.com") -> mapOf(
                "title" to ".title",
                "cover" to ".book img",
                "chapter_list" to "li a",
                "content" to "#chr-content"
            )
            else -> null
        }
    }

    suspend fun scrapeBook(url: String): ScrapedBook? {
        val selectors = getSelectors(url) ?: return null
        return try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", url)
                .timeout(15000)
                .get()

            ScrapedBook(
                title = doc.selectFirst(selectors["title"]!!)?.text() ?: "Unknown",
                coverUrl = doc.selectFirst(selectors["cover"]!!)?.attr("abs:src") ?: "",
                chapters = doc.select(selectors["chapter_list"]!!).map { 
                    ScrapedChapter(it.text(), it.attr("abs:href"))
                }
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class ScrapedBook(val title: String, val coverUrl: String, val chapters: List<ScrapedChapter>)
data class ScrapedChapter(val name: String, val url: String)
