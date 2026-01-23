package my.noveldokusha.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Scraper for verified reliable sources after Laboratory Audit (Jan 2026)
 * Supports: RoyalRoad, WuxiaWorld, NovelBin, NovelFull, ReadNovelFull, MeioNovel, NovelKu, AllNovelUpdates
 */
class Scraper {
    
    fun getSelectors(url: String): Map<String, String>? {
        return when {
            url.contains("royalroad.com") -> mapOf(
                "title" to "h1",
                "cover" to "img.thumbnail",
                "chapter_list" to "#chapters tbody tr a[href]",
                "content" to ".chapter-inner"
            )
            url.contains("wuxiaworld.com") -> mapOf(
                "title" to "h1",
                "cover" to "img[src*=covers]",
                "chapter_list" to "a[href*=-chapter-]",
                "content" to ".chapter-content"
            )
            url.contains("novelbin.com") -> mapOf(
                "title" to ".title",
                "cover" to ".book img",
                "chapter_list" to "li a",
                "content" to "#chr-content"
            )
            url.contains("novelfull.com") || url.contains("readnovelfull.com") -> mapOf(
                "title" to "h3.title",
                "cover" to ".book img",
                "chapter_list" to "#list-chapter li a",
                "content" to "#chapter-content"
            )
            url.contains("meionovel.id") || url.contains("novelku.id") -> mapOf(
                "title" to "h1.entry-title",
                "cover" to ".thumb img",
                "chapter_list" to ".eplister li a",
                "content" to ".entry-content"
            )
            url.contains("allnovelupdates.com") -> mapOf(
                "title" to ".post-title h1",
                "cover" to ".summary_image img",
                "chapter_list" to ".wp-manga-chapter a",
                "content" to ".reading-content"
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

            val title = doc.selectFirst(selectors["title"]!!)?.text() ?: "Unknown"
            
            var coverUrl = doc.selectFirst(selectors["cover"]!!)?.let {
                it.attr("abs:src").takeIf { s -> s.isNotEmpty() } ?: it.attr("abs:data-src")
            } ?: ""
            
            if (coverUrl.isEmpty()) {
                coverUrl = doc.selectFirst("img[alt*='${title.take(10)}']")?.attr("abs:src") ?: ""
            }

            val chapters = doc.select(selectors["chapter_list"]!!).map { 
                ScrapedChapter(it.text(), it.attr("abs:href"))
            }.filter { it.name.isNotEmpty() && it.url.isNotEmpty() }

            if (chapters.isEmpty() && !url.contains("wuxiaworld.com")) return null

            ScrapedBook(title, coverUrl, chapters)
        } catch (e: Exception) {
            null
        }
    }
}

data class ScrapedBook(val title: String, val coverUrl: String, val chapters: List<ScrapedChapter>)
data class ScrapedChapter(val name: String, val url: String)