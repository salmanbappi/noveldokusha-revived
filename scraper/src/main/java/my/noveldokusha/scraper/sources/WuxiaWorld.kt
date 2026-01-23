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

class WuxiaWorld(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "wuxiaworld"
    override val nameStrId = R.string.source_name_wuxiaworld
    override val baseUrl = "https://www.wuxiaworld.com"
    override val catalogUrl = "https://www.wuxiaworld.com/novels"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst("#chapter-content")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".novel-cover img")?.attr("abs:src")
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
                .select(".chapter-item a")
                .map {
                    ChapterResult(
                        title = it.text(),
                        url = it.attr("abs:href")
                    )
                }
        }
    }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(catalogUrl).toDocument()
            doc.select(".novel-item")
                .mapNotNull {
                    val link = it.selectFirst("a") ?: return@mapNotNull null
                    BookResult(
                        title = it.selectFirst(".title")?.text() ?: "",
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, true) } // WuxiaWorld listing is often one-page or infinite scroll
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val url = "https://www.wuxiaworld.com/search?query=$input"
            val doc = networkClient.get(url).toDocument()
            doc.select(".novel-item")
                .mapNotNull {
                    val link = it.selectFirst("a") ?: return@mapNotNull null
                    BookResult(
                        title = it.selectFirst(".title")?.text() ?: "",
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, true) }
        }
    }
}