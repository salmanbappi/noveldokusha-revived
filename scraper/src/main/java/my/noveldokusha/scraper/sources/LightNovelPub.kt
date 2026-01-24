package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUrlBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class LightNovelPub(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "lightnovelpub"
    override val nameStrId = R.string.source_name_lightnovelpub
    override val baseUrl = "https://lightnovelpub.me"
    override val catalogUrl = "https://lightnovelpub.me/browse/all/popular/all"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst("#chapter-container")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".fixed-img img")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".summary .content")?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val url = if (bookUrl.endsWith("/")) "${bookUrl}chapters" else "$bookUrl/chapters"
            networkClient.get(url).toDocument()
                .select(".chapter-list a")
                .map {
                    ChapterResult(
                        title = it.selectFirst(".chapter-title")?.text() ?: it.text(),
                        url = it.attr("abs:href")
                    )
                }
        }
    }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "$catalogUrl/$page"
            val doc = networkClient.get(url).toDocument()
            doc.select(".novel-item")
                .mapNotNull {
                    val link = it.selectFirst("a") ?: return@mapNotNull null
                    BookResult(
                        title = it.selectFirst(".novel-title")?.text() ?: "",
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
            val url = "https://lightnovelpub.me/search?keyword=$input&page=$page"
            val doc = networkClient.get(url).toDocument()
            doc.select(".novel-item")
                .mapNotNull {
                    val link = it.selectFirst("a") ?: return@mapNotNull null
                    BookResult(
                        title = it.selectFirst(".novel-title")?.text() ?: "",
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }
}
