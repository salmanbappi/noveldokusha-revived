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
 * https://www.twkan.com/novel/12345.html
 * Chapter url example:
 * https://www.twkan.com/novel/pagea/12345_1.html
 */
class Twkan(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "twkan"
    override val nameStrId = R.string.source_name_twkan
    override val baseUrl = "https://www.twkan.com"
    override val catalogUrl = "https://www.twkan.com/sort/0/1.html"
    override val language = LanguageCode.CHINESE

    override suspend fun getChapterTitle(doc: Document): String? =
        doc.selectFirst("h1")?.text()

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".content, #content")?.let { element ->
            element.select("script, .ads").remove()
            element.html()
        } ?: ""
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            doc.selectFirst(".book-img img")?.attr("src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            doc.selectFirst(".book-intro")?.text()
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            val chapters = mutableListOf<ChapterResult>()
            
            // Twkan often has multiple pages of chapters
            doc.select(".chapter-list a").forEach {
                chapters.add(
                    ChapterResult(
                        title = it.text(),
                        url = it.attr("abs:href")
                    )
                )
            }
            
            chapters
        }
    }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "$baseUrl/sort/0/$page.html"

            val doc = networkClient.get(url).toDocument()
            val books = doc.select(".book-list li").mapNotNull { item ->
                val link = item.selectFirst(".book-name a") ?: return@mapNotNull null
                val title = link.text()
                val bookUrl = link.attr("abs:href")
                val bookCover = item.selectFirst(".book-img img")?.attr("src") ?: ""
                
                BookResult(
                    title = title,
                    url = bookUrl,
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

            val url = "$baseUrl/search.html?searchkey=$input"
            val doc = networkClient.get(url).toDocument()
            
            val books = doc.select(".book-list li").mapNotNull { item ->
                val link = item.selectFirst(".book-name a") ?: return@mapNotNull null
                val title = link.text()
                val bookUrl = link.attr("abs:href")
                val bookCover = item.selectFirst(".book-img img")?.attr("src") ?: ""
                
                BookResult(
                    title = title,
                    url = bookUrl,
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