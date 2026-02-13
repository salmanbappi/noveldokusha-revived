package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.add
import my.noveldokusha.network.addPath
import my.noveldokusha.network.ifCase
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUrlBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class BoxNovel(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "box_novel"
    override val nameStrId = R.string.source_name_box_novel
    override val baseUrl = "https://boxnovel.com"
    override val catalogUrl = "https://boxnovel.com/novel"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".read-container, .chapter-text.w-richtext")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".summary_image img, img.image-10")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".description-summary, .novel-temp-summary, .summary__content")
                ?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            val chapters = doc.select("li.wp-manga-chapter a, .chaplistcol a")
                .map {
                    ChapterResult(
                        title = it.selectFirst(".chaptitle")?.text() ?: it.text(),
                        url = it.attr("abs:href")
                    )
                }
            
            if (chapters.isEmpty()) {
                // Try ajax for Madara
                val id = doc.selectFirst("#manga-chapters-holder")?.attr("data-id")
                if (id != null) {
                    val ajaxUrl = "https://boxnovel.com/wp-admin/admin-ajax.php"
                    val res = networkClient.post(ajaxUrl, mapOf("action" to "manga_get_chapters", "manga" to id))
                    return@tryConnect res.toDocument().select("li.wp-manga-chapter a").map {
                        ChapterResult(it.text(), it.attr("abs:href"))
                    }.reversed()
                }
            }
            chapters.reversed()
        }
    }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "https://boxnovel.com/novel/page/$page/"
            val doc = networkClient.get(url).toDocument()
            val books = doc.select(".page-item-detail, .novelcollection")
                .mapNotNull {
                    val link = it.selectFirst(".item-thumb a, .books a") ?: return@mapNotNull null
                    val title = it.selectFirst(".post-title h3 a, .books0detail.title")?.text() ?: ""
                    val cover = it.selectFirst("img")?.attr("abs:src") ?: ""
                    BookResult(title, link.attr("abs:href"), cover)
                }
            PagedList(books, index, books.isEmpty())
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "https://boxnovel.com/page/$page/?s=$input&post_type=wp-manga"
            val doc = networkClient.get(url).toDocument()
            val books = doc.select(".c-tabs-item__content, .search-result-items > div")
                .mapNotNull {
                    val link = it.selectFirst(".post-title h3 a, a") ?: return@mapNotNull null
                    val title = link.text()
                    val cover = it.selectFirst("img")?.attr("abs:src") ?: ""
                    BookResult(title, link.attr("abs:href"), cover)
                }
            PagedList(books, index, books.isEmpty())
        }
    }
}