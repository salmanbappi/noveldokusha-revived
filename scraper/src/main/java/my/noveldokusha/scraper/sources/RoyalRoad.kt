package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class RoyalRoad(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "royalroad"
    override val nameStrId = R.string.source_name_royalroad
    override val baseUrl = "https://royalroad.com"
    override val catalogUrl = "https://www.royalroad.com/fictions/best-rated"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".chapter-content")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".thumbnail")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".description")?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .select("#chapters tbody tr[data-url]")
                .map {
                    ChapterResult(
                        title = it.selectFirst("a")?.text() ?: "",
                        url = it.selectFirst("a")?.attr("abs:href") ?: ""
                    )
                }
        }
    }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "$catalogUrl?page=$page"
            val doc = networkClient.get(url).toDocument()
            doc.select(".fiction-list-item")
                .mapNotNull {
                    val link = it.selectFirst(".fiction-title a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "https://www.royalroad.com/fictions/search?title=$input&page=$page"
            val doc = networkClient.get(url).toDocument()
            doc.select(".fiction-list-item")
                .mapNotNull {
                    val link = it.selectFirst(".fiction-title a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }
}