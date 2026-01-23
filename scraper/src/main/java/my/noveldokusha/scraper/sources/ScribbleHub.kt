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

class ScribbleHub(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "scribblehub"
    override val nameStrId = R.string.source_name_scribblehub
    override val baseUrl = "https://www.scribblehub.com"
    override val catalogUrl = "https://www.scribblehub.com/series-ranking/?sort=rank&order=asc"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst("#chp_raw")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".fic_image img")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".wi_fic_desc")?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .select("li.toc_w a")
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
            val page = index + 1
            val url = "$catalogUrl&p=$page"
            val doc = networkClient.get(url).toDocument()
            doc.select(".search_main_box")
                .mapNotNull {
                    val link = it.selectFirst(".search_title a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst(".search_img img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "https://www.scribblehub.com/?s=$input&post_type=fictionposts&paged=$page"
            val doc = networkClient.get(url).toDocument()
            doc.select(".search_main_box")
                .mapNotNull {
                    val link = it.selectFirst(".search_title a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst(".search_img img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }
}
