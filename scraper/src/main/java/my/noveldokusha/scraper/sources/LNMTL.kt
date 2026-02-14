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
 * https://lnmtl.com/novel/novel-name
 * Chapter url example:
 * https://lnmtl.com/chapter/novel-name-chapter-1
 */
class LNMTL(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "lnmtl"
    override val nameStrId = R.string.source_name_lnmtl
    override val baseUrl = "https://lnmtl.com/"
    override val catalogUrl = "https://lnmtl.com/novel"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterTitle(doc: Document): String? =
        doc.selectFirst(".chapter-title, h1")?.text()

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".chapter-body, .reading-content")?.let { element ->
            element.select("script, .ads, .ad").remove()
            element.html()
        } ?: ""
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            doc.selectFirst(".novel-cover img, .media-left img")?.attr("src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            doc.selectFirst(".description, .summary")?.text()
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            
            // LNMTL often uses JavaScript to load chapters, but some are in the HTML
            val chapters = doc.select(".chapter-list a, .chapters a").map {
                ChapterResult(
                    title = it.text(),
                    url = it.attr("abs:href")
                )
            }
            
            if (chapters.isNotEmpty()) return@tryConnect chapters
            
            // Alternative: extract novel ID and fetch from API if possible
            // This is a simplified version
            emptyList<ChapterResult>()
        }
    }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "https://lnmtl.com/novel?page=$page"

            val doc = networkClient.get(url).toDocument()
            val books = doc.select(".novel-item, .media").mapNotNull {
                val link = it.selectFirst("a") ?: return@mapNotNull null
                val title = it.selectFirst(".novel-name, .media-heading")?.text() ?: link.text()
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
                isLastPage = books.isEmpty() || doc.selectFirst(".pagination .next") == null
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

            // LNMTL search is a bit complex, using simple library filtering for now
            val url = "https://lnmtl.com/novel?search=$input"
            val doc = networkClient.get(url).toDocument()
            
            val books = doc.select(".novel-item, .media").mapNotNull {
                val link = it.selectFirst("a") ?: return@mapNotNull null
                val title = it.selectFirst(".novel-name, .media-heading")?.text() ?: link.text()
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
