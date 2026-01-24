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
    
    private var workerUrl: String = "https://your-worker.workers.dev/?url="
    private val flareSolverr = FlareSolverrClient()

    val databasesList = arrayOf(
        BakaUpdates(networkClient),
        NovelUpdates(networkClient)
    )

    val sourcesCatalogsList = arrayOf(
        RoyalRoad(networkClient),
        WuxiaWorld(networkClient),
        NovelBin(networkClient),
        NovelFull(networkClient),
        ReadNovelFull(networkClient),
        MeioNovel(networkClient),
        Novelku(networkClient),
        KoreanNovelsMTL(networkClient),
        LightNovelsTranslations(networkClient),
        Wuxia(networkClient),
        IndoWebnovel(networkClient),
        LightNovelWorld(networkClient),
        LightNovelPub(networkClient),
        NovelHall(networkClient),
        SakuraNovel(networkClient),
        ScribbleHub(networkClient),
        WbNovel(networkClient)
    )

    val sourcesCatalogsLanguagesList = sourcesCatalogsList
        .mapNotNull { it.language }
        .distinct()
        .sortedBy { it.iso639_1 }

    fun getCompatibleSourceCatalog(url: String): SourceInterface.Catalog? {
        return sourcesCatalogsList.firstOrNull { url.startsWith(it.baseUrl) }
    }

    fun getCompatibleSource(url: String): SourceInterface? {
        return sourcesCatalogsList.firstOrNull { url.startsWith(it.baseUrl) }
    }

    fun getSelectors(url: String): Map<String, String>? {
        return when {
            url.contains("royalroad.com") -> mapOf(
                "title" to "h1", "cover" to "img.thumbnail", "chapter_list" to "#chapters tbody tr a[href]", "content" to ".chapter-content"
            )
            url.contains("wuxiaworld.com") -> mapOf(
                "title" to "h1", "cover" to "img[src*=covers]", "chapter_list" to "a[href*=-chapter-]", "content" to ".chapter-content"
            )
            url.contains("novelbin.com") -> mapOf(
                "title" to ".title", "cover" to ".book img", "chapter_list" to "li a", "content" to "#chr-content"
            )
            url.contains("novelfull.com") || url.contains("readnovelfull.com") -> mapOf(
                "title" to "h3.title", "cover" to ".book img", "chapter_list" to "#list-chapter li a", "content" to "#chapter-content"
            )
            url.contains("meionovel.id") || url.contains("novelku.id") -> mapOf(
                "title" to "h1.entry-title", "cover" to ".thumb img", "chapter_list" to ".eplister li a", "content" to ".entry-content"
            )
            url.contains("koreanmtl.online") -> mapOf(
                "title" to "h1", "cover" to "img[src*=covers]", "chapter_list" to ".chapter-list a", "content" to ".entry-content"
            )
            url.contains("lightnovelstranslations.com") -> mapOf(
                "title" to "h1", "cover" to "img.wp-post-image", "chapter_list" to ".chapter-item a", "content" to ".novel_text"
            )
            url.contains("wuxia.blog") -> mapOf(
                "title" to "h1", "cover" to "img.img-responsive", "chapter_list" to "#chapter-list a", "content" to "div.panel-body.article"
            )
            url.contains("1stkissnovel.love") || url.contains("boxnovel.com") || url.contains("indowebnovel.id") || 
            url.contains("sakuranovel.id") || url.contains("allnovelupdates.com") || url.contains("wbnovel.com") -> mapOf(
                "title" to ".post-title h1", "cover" to ".summary_image img", "chapter_list" to "li[class=wp-manga-chapter] a", "content" to ".read-container"
            )
            url.contains("lightnovelworld.com") || url.contains("lightnovelpub.com") -> mapOf(
                "title" to "h1.novel-title", "cover" to ".fixed-img img", "chapter_list" to "li.chapter-item a", "content" to "#chapter-container"
            )
            url.contains("scribblehub.com") -> mapOf(
                "title" to ".fic_title", "cover" to ".fic_image img", "chapter_list" to ".toc_a", "content" to "#chp_raw"
            )
            url.contains("bestlightnovel.com") -> mapOf(
                "title" to "h1", "cover" to ".info_image img", "chapter_list" to ".chapter-list a", "content" to "#vung_doc"
            )
            url.contains("novelupdates.com") -> mapOf(
                "title" to ".seriestitlenwrap", "cover" to ".seriesimg img", "chapter_list" to ".chp-release", "content" to "#novelcontent"
            )
            url.contains("novelhall.com") -> mapOf(
                "title" to "h1", "cover" to ".book-img img", "chapter_list" to ".chapter-list a", "content" to "div#htmlContent"
            )
            else -> null
        }
    }

    suspend fun scrapeBook(url: String): ScrapedBook? {
        val selectors = getSelectors(url) ?: return null
        var doc = tryFetch(url)
        if (isBlocked(doc)) {
            val html = flareSolverr.fetch(url)
            if (html != null) doc = Jsoup.parse(html, url)
        }
        if (isBlocked(doc)) {
            doc = tryFetch(workerUrl + url)
        }
        return doc?.let { d ->
            val title = d.selectFirst(selectors["title"]!!)?.text() ?: "Unknown"
            var coverUrl = d.selectFirst(selectors["cover"]!!)?.let {
                it.attr("abs:src").takeIf { s -> s.isNotEmpty() } ?: it.attr("abs:data-src")
            } ?: ""
            if (coverUrl.isEmpty()) {
                coverUrl = d.selectFirst("img[alt*='${title.take(10)}']")?.attr("abs:src") ?: ""
            }
            val chapters = d.select(selectors["chapter_list"]!!).map { 
                ScrapedChapter(it.text(), it.attr("abs:href"))
            }.filter { it.name.isNotEmpty() && it.url.isNotEmpty() }
            if (chapters.isEmpty() && !url.contains("wuxiaworld.com")) return null
            ScrapedBook(title, coverUrl, chapters)
        }
    }

    private fun isBlocked(doc: Document?): Boolean {
        if (doc == null) return true
        val text = doc.text().lowercase()
        val title = doc.title().lowercase()
        return text.contains("cloudflare") || text.contains("just a moment") || 
               title.contains("access denied") || title.contains("attention required") ||
               title.contains("checking your browser")
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
