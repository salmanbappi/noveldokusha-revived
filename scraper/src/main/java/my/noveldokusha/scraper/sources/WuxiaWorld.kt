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

    private val gson = Gson()

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst("#chapter-content")?.let { TextExtractor.get(it) } 
            ?: doc.selectFirst(".chapter-content")?.let { TextExtractor.get(it) }
            ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst("img.novel-cover")?.attr("abs:src")
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
            val doc = networkClient.get(bookUrl).toDocument()
            val scriptData = doc.select("script").find { it.html().contains("__REACT_QUERY_STATE__") }?.html()
            if (scriptData != null) {
                val jsonStr = scriptData.substringAfter("__REACT_QUERY_STATE__ = ").substringBeforeLast(";")
                val json = gson.fromJson(jsonStr, JsonObject::class.java)
                val queries = json.getAsJsonArray("queries")
                for (query in queries) {
                    val state = query.asJsonObject.getAsJsonObject("state")
                    val data = state?.getAsJsonObject("data")
                    if (data != null && data.has("chapterGroups")) {
                        val chapterGroups = data.getAsJsonArray("chapterGroups")
                        val chapters = mutableListOf<ChapterResult>()
                        chapterGroups.forEach { group ->
                            group.asJsonObject.getAsJsonArray("chapters").forEach { chapter ->
                                val c = chapter.asJsonObject
                                chapters.add(
                                    ChapterResult(
                                        title = c.get("name").asString,
                                        url = baseUrl + "/novel/" + data.get("slug").asString + "/" + c.get("slug").asString
                                    )
                                )
                            }
                        }
                        if (chapters.isNotEmpty()) return@tryConnect chapters
                    }
                }
            }
            // Fallback to legacy
            doc.select(".chapter-item a")
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
            val scriptData = doc.select("script").find { it.html().contains("__REACT_QUERY_STATE__") }?.html()
            if (scriptData != null) {
                val jsonStr = scriptData.substringAfter("__REACT_QUERY_STATE__ = ").substringBeforeLast(";")
                val json = gson.fromJson(jsonStr, JsonObject::class.java)
                val queries = json.getAsJsonArray("queries")
                for (query in queries) {
                    val state = query.asJsonObject.getAsJsonObject("state")
                    val data = state?.getAsJsonObject("data")
                    if (data != null && data.has("pages")) {
                        val pages = data.getAsJsonArray("pages")
                        val books = mutableListOf<BookResult>()
                        pages.forEach { page ->
                            page.asJsonObject.getAsJsonArray("items").forEach { item ->
                                val b = item.asJsonObject
                                books.add(
                                    BookResult(
                                        title = b.get("name").asString,
                                        url = baseUrl + "/novel/" + b.get("slug").asString,
                                        coverImageUrl = b.getAsJsonObject("coverUrl")?.get("value")?.asString ?: ""
                                    )
                                )
                            }
                        }
                        if (books.isNotEmpty()) return@tryConnect PagedList(books, index, true)
                    }
                }
            }
            // Fallback
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

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val url = "https://www.wuxiaworld.com/search?query=$input"
            val doc = networkClient.get(url).toDocument()
            // Search might also use the same React state
            val scriptData = doc.select("script").find { it.html().contains("__REACT_QUERY_STATE__") }?.html()
            if (scriptData != null) {
                val jsonStr = scriptData.substringAfter("__REACT_QUERY_STATE__ = ").substringBeforeLast(";")
                val json = gson.fromJson(jsonStr, JsonObject::class.java)
                // Search result parsing logic if it differs from catalog...
                // For now, let's try to reuse or fallback
            }
            
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