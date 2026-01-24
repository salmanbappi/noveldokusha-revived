package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.scraper.R
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class Novelhall(private val networkClient: NetworkClient) : SourceInterface.Catalog {
    override val id = "novelhall"
    override val nameStrId = R.string.source_name_novelhall
    override val baseUrl = "https://www.novelhall.com"
    override val catalogUrl = "https://www.novelhall.com/all.html"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".entry-content")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".book-img img")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".intro")?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .select(".book-catalog ul li a")
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
            // Novelhall all.html doesn't seem to have standard paging, trying to parse the list
            val doc = networkClient.get(catalogUrl).toDocument()
            doc.select("table.table tr").drop(1) // Drop header
                .mapNotNull {
                    val link = it.selectFirst("td.novel a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = "" // Table doesn't have covers, but details page does
                    )
                }
                .let { PagedList(it, index, true) }
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        if (index > 0) return@withContext Response.Success(PagedList.createEmpty(index))
        tryConnect {
            val url = "https://www.novelhall.com/index.php?s=main/search&q=$input"
            val doc = networkClient.get(url).toDocument()
            doc.select(".list-novel .row")
                .mapNotNull {
                    val link = it.selectFirst("h3 a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, true) }
        }
    }
}