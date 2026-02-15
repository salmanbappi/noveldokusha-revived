package my.noveldokusha.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.toDocument
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

/**
 * A source that loads its configuration from a JSON file.
 * This allows the AI to update sources without recompiling the app.
 */
class DynamicSource(
    override val id: String,
    override val name: String,
    override val baseUrl: String,
    override val catalogUrl: String,
    val selectors: Map<String, String>,
    override val language: LanguageCode?,
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {

    override suspend fun getChapterTitle(doc: Document): String =
        withContext(Dispatchers.Default) {
            doc.selectFirst(selectors["chapterTitle"] ?: "h1")?.text() ?: ""
        }

    override suspend fun getChapterText(doc: Document): String =
        withContext(Dispatchers.Default) {
            doc.selectFirst(selectors["chapterText"] ?: ".content")?.let {
                TextExtractor.get(it)
            } ?: ""
        }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> =
        withContext(Dispatchers.Default) {
            tryConnect {
                networkClient.get(bookUrl).toDocument()
                    .selectFirst(selectors["bookCover"] ?: "img")?.attr("abs:src")
            }
        }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> =
        withContext(Dispatchers.Default) {
            tryConnect {
                networkClient.get(bookUrl).toDocument()
                    .selectFirst(selectors["bookDescription"] ?: ".description")?.let {
                        TextExtractor.get(it)
                    }
            }
        }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> =
        withContext(Dispatchers.Default) {
            tryConnect {
                networkClient.get(bookUrl).toDocument()
                    .select(selectors["chapterItem"] ?: "a")
                    .map { ChapterResult(it.text(), it.attr("abs:href")) }
            }
        }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> =
        withContext(Dispatchers.Default) {
            val url = if (index == 0) catalogUrl else catalogUrl.replace("{page}", (index + 1).toString())
            getPagesList(index, url)
        }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> =
        withContext(Dispatchers.Default) {
            val url = "$baseUrl/search?q=$input" // Simplified for now, can be improved in JSON
            getPagesList(index, url)
        }

    private suspend fun getPagesList(index: Int, url: String): Response<PagedList<BookResult>> =
        tryConnect {
            val doc = networkClient.get(url).toDocument()
            val items = doc.select(selectors["bookItem"] ?: ".item")
                .map {
                    BookResult(
                        title = it.selectFirst(selectors["bookItemTitle"] ?: "h3")?.text() ?: "",
                        url = it.selectFirst(selectors["bookItemUrl"] ?: "a")?.attr("abs:href") ?: "",
                        coverImageUrl = it.selectFirst(selectors["bookItemCover"] ?: "img")?.attr("abs:src") ?: ""
                    )
                }
            PagedList(items, index, doc.selectFirst(selectors["nextPage"] ?: ".next") == null)
        }

    private suspend fun <T> tryConnect(block: suspend () -> T): Response<T> {
        return try {
            Response.Success(block())
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error", e)
        }
    }
}
