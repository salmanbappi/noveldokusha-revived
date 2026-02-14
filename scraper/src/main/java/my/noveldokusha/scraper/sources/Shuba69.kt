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
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

/**
 * Novel main page (chapter list) example:
 * https://www.69shuba.pro/book/12345.htm
 * Chapter url example:
 * https://www.69shuba.pro/txt/12345/67890
 */
class Shuba69(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "69shuba"
    override val nameStrId = R.string.source_name_69shuba
    override val baseUrl = "https://www.69shuba.pro"
    override val catalogUrl = "https://www.69shuba.pro/novellist.htm"
    override val language = LanguageCode.CHINESE

    // Use GBK encoding for this site
    private val charset = "GBK"

    override suspend fun getChapterTitle(doc: Document): String? =
        doc.selectFirst(".txtnav h1")?.text()

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".txtnav")?.let { element ->
            // Remove navigation and ads
            element.select("h1, .top_navigation, .bottom_navigation, script").remove()
            element.html()
        } ?: ""
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument(charset)
            doc.selectFirst(".bookimg2 img")?.attr("src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument(charset)
            doc.selectFirst(".navtxt")?.text()
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            // The chapter list is usually on a separate page or bottom of the book page
            val novelId = bookUrl.substringAfterLast("/").substringBefore(".")
            val chapterListUrl = "$baseUrl/book/$novelId/"
            
            val doc = networkClient.get(chapterListUrl).toDocument(charset)
            doc.select(".catalog li a").map {
                ChapterResult(
                    title = it.text(),
                    url = it.attr("abs:href")
                )
            }
        }
    }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "https://www.69shuba.pro/novellist_$page.htm"

            val doc = networkClient.get(url).toDocument(charset)
            val books = doc.select(".newbook li, .library li").mapNotNull {
                val link = it.selectFirst("a") ?: return@mapNotNull null
                val title = it.selectFirst(".newbookname, h3")?.text() ?: link.text()
                val bookCover = it.selectFirst("img")?.attr("src") ?: ""
                
                BookResult(
                    title = title,
                    url = link.attr("abs:href"),
                    coverImageUrl = bookCover
                )
            }

            PagedList(
                list = books,
                index = index,
                isLastPage = books.isEmpty()
            )
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            if (input.isBlank() || index > 0)
                return@tryConnect PagedList.createEmpty(index = index)

            val url = "$baseUrl/modules/article/search.php?searchkey=$input"
            val doc = networkClient.get(url).toDocument(charset)
            
            val books = doc.select(".newbook li, .library li").mapNotNull {
                val link = it.selectFirst("a") ?: return@mapNotNull null
                val title = it.selectFirst(".newbookname, h3")?.text() ?: link.text()
                val bookCover = it.selectFirst("img")?.attr("src") ?: ""
                
                BookResult(
                    title = title,
                    url = link.attr("abs:href"),
                    coverImageUrl = bookCover
                )
            }

            PagedList(
                list = books,
                index = index,
                isLastPage = true
            )
        }
    }
}