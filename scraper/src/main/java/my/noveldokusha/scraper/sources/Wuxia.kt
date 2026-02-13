package my.noveldokusha.scraper.sources

import com.google.gson.Gson
import com.google.gson.JsonObject
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

class Wuxia(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "wuxia"
    override val nameStrId = R.string.source_name_wuxia
    override val baseUrl = "https://wuxia.click"
    override val catalogUrl = "https://wuxia.click"
    override val language = LanguageCode.ENGLISH

    private val gson = Gson()

    override suspend fun getChapterTitle(doc: Document): String? = null

    override suspend fun getChapterText(
        doc: Document
    ): String = withContext(Dispatchers.Default) {
        doc.selectFirst("div.panel-body.article")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".imageCover")
                ?.selectFirst("img[src]")
                ?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst("div[itemprop=description]")
                ?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            doc.select("#chapters a[href]").map {
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
        if (index > 0)
            return@withContext Response.Success(PagedList.createEmpty(index = index))

        tryConnect {
            val doc = networkClient.get(catalogUrl).toDocument()
            val scriptData = doc.select("#__NEXT_DATA__").firstOrNull()?.html()
            if (scriptData != null) {
                val json = gson.fromJson(scriptData, JsonObject::class.java)
                val queries = json.getAsJsonObject("props")
                    ?.getAsJsonObject("pageProps")
                    ?.getAsJsonObject("dehydratedState")
                    ?.getAsJsonArray("queries")
                
                queries?.forEach { query ->
                    val data = query.asJsonObject.getAsJsonObject("state")?.getAsJsonArray("data")
                    if (data != null) {
                        val books = mutableListOf<BookResult>()
                        data.forEach { category ->
                            category.asJsonObject.getAsJsonArray("novels")?.forEach { novel ->
                                val n = novel.asJsonObject
                                books.add(
                                    BookResult(
                                        title = n.get("name").asString,
                                        url = baseUrl + "/novel/" + n.get("slug").asString,
                                        coverImageUrl = n.get("image")?.takeIf { !it.isJsonNull }?.asString ?: ""
                                    )
                                )
                            }
                        }
                        if (books.isNotEmpty()) return@tryConnect PagedList(books, index, true)
                    }
                }
            }
            
            // Fallback
            PagedList.createEmpty(index)
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        if (input.isBlank() || index > 0)
            return@withContext Response.Success(PagedList.createEmpty(index = index))

        tryConnect {
            val url = "$baseUrl/search?q=$input"
            val doc = networkClient.get(url).toDocument()
            val scriptData = doc.select("#__NEXT_DATA__").firstOrNull()?.html()
            if (scriptData != null) {
                val json = gson.fromJson(scriptData, JsonObject::class.java)
                val queries = json.getAsJsonObject("props")
                    ?.getAsJsonObject("pageProps")
                    ?.getAsJsonObject("dehydratedState")
                    ?.getAsJsonArray("queries")
                
                queries?.forEach { query ->
                    val data = query.asJsonObject.getAsJsonObject("state")?.getAsJsonArray("data")
                    if (data != null) {
                        val books = mutableListOf<BookResult>()
                        data.forEach { novel ->
                            val n = novel.asJsonObject
                            books.add(
                                BookResult(
                                    title = n.get("name").asString,
                                    url = baseUrl + "/novel/" + n.get("slug").asString,
                                    coverImageUrl = n.get("image")?.takeIf { !it.isJsonNull }?.asString ?: ""
                                )
                            )
                        }
                        if (books.isNotEmpty()) return@tryConnect PagedList(books, index, true)
                    }
                }
            }
            PagedList.createEmpty(index)
        }
    }
}