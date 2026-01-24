package my.noveldokusha.scraper

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.databases.BakaUpdates
import my.noveldokusha.scraper.databases.NovelUpdates
import my.noveldokusha.scraper.sources.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import my.noveldokusha.networking.FlareSolverrClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Scraper @Inject constructor(
    private val networkClient: NetworkClient,
    @ApplicationContext private val context: Context
) {
    
    val databasesList: Array<DatabaseInterface> = arrayOf(
        BakaUpdates(networkClient),
        NovelUpdates(networkClient)
    )
...
    suspend fun scrapeBook(url: String): ScrapedBook? {
        val selectors = getSelectors(url) ?: return null
        val doc = tryFetch(url)
        return doc?.let {
            val title = it.selectFirst(selectors["title"]!!)?.text() ?: "Unknown"
            var coverUrl = it.selectFirst(selectors["cover"]!!)?.let {
                it.attr("abs:src").takeIf { s -> s.isNotEmpty() } ?: it.attr("abs:data-src")
            } ?: ""
            if (coverUrl.isEmpty()) {
                coverUrl = it.selectFirst("img[alt*='${title.take(10)}']")?.attr("abs:src") ?: ""
            }
            val chapters = it.select(selectors["chapter_list"]!!).map { 
                ScrapedChapter(it.text(), it.attr("abs:href"))
            }.filter { it.name.isNotEmpty() && it.url.isNotEmpty() }
            if (chapters.isEmpty() && !url.contains("wuxiaworld.com")) return null
            ScrapedBook(title, coverUrl, chapters)
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