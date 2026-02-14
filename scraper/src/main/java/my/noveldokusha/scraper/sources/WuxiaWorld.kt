package my.noveldokusha.scraper.sources

import com.google.gson.GsonBuilder
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

    private val gson = GsonBuilder().setLenient().create()

    private fun extractJson(scriptData: String, key: String): JsonObject? {
        val startIdx = scriptData.indexOf(key)
        if (startIdx == -1) return null
        
        val eqIdx = scriptData.indexOf("=", startIdx)
        if (eqIdx == -1) return null
        
        var jsonStart = -1
        for (i in eqIdx until scriptData.length) {
            if (scriptData[i] == '{' || scriptData[i] == '[') {
                jsonStart = i
                break
            }
        }
        if (jsonStart == -1) return null
        
        var braceCount = 0
        var jsonEnd = -1
        for (i in jsonStart until scriptData.length) {
            val c = scriptData[i]
            if (c == '{' || c == '[') braceCount++
            else if (c == '}' || c == ']') braceCount--
            
            if (braceCount == 0) {
                jsonEnd = i + 1
                break
            }
        }
        
        return if (jsonEnd != -1) {
            val jsonStr = scriptData.substring(jsonStart, jsonEnd)
            gson.fromJson(jsonStr, JsonObject::class.java)
        } else null
    }

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
                val json = extractJson(scriptData, "__REACT_QUERY_STATE__")
                if (json != null) {
                    val queries = json.getAsJsonArray("queries")
                    var novelId = -1
                    var novelSlug = ""
                    
                    for (query in queries) {
                        val state = query.asJsonObject.getAsJsonObject("state")
                        val data = state?.getAsJsonObject("data")
                        if (data != null) {
                            val item = if (data.has("item")) data.getAsJsonObject("item") else null
                            if (item != null) {
                                novelId = item.get("id").asInt
                                novelSlug = item.get("slug").asString
                            }
                        }
                    }

                    if (novelId != -1) {
                        val apiUrl = "https://api2.wuxiaworld.com/query/chapter-list?novelId=$novelId"
                        val response = networkClient.get(apiUrl)
                        val responseBody = response.body?.string() ?: ""
                        val apiJson = gson.fromJson(responseBody, JsonObject::class.java)
                        val chapterGroups = apiJson.getAsJsonArray("chapterGroups")
                        val chapters = mutableListOf<ChapterResult>()
                        chapterGroups?.forEach { group ->
                            group.asJsonObject.getAsJsonArray("chapterList").forEach { chapter ->
                                val c = chapter.asJsonObject
                                chapters.add(
                                    ChapterResult(
                                        title = c.get("name").asString,
                                        url = baseUrl + "/novel/" + novelSlug + "/" + c.get("slug").asString
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
                val json = extractJson(scriptData, "__REACT_QUERY_STATE__")
                if (json != null) {
                    val queries = json.getAsJsonArray("queries")
                    queries?.forEach { query ->
                        val state = query.asJsonObject.getAsJsonObject("state")
                        val data = state?.getAsJsonObject("data")
                        if (data != null && data.has("pages")) {
                            val pages = data.getAsJsonArray("pages")
                            val books = mutableListOf<BookResult>()
                            pages.forEach { page ->
                                page.asJsonObject.getAsJsonArray("items")?.forEach { item ->
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
                val json = extractJson(scriptData, "__REACT_QUERY_STATE__")
                if (json != null) {
                    val queries = json.getAsJsonArray("queries")
                    for (query in queries) {
                        val state = query.asJsonObject.getAsJsonObject("state")
                        val data = state?.getAsJsonObject("data")
                        if (data != null && (data.has("items") || data.has("pages"))) {
                            val books = mutableListOf<BookResult>()
                            val items = if (data.has("items")) data.getAsJsonArray("items") else if (data.has("pages")) {
                                val booksInPages = mutableListOf<BookResult>()
                                data.getAsJsonArray("pages").forEach { page ->
                                    page.asJsonObject.getAsJsonArray("items")?.forEach { item ->
                                        val b = item.asJsonObject
                                        booksInPages.add(
                                            BookResult(
                                                title = b.get("name").asString,
                                                url = baseUrl + "/novel/" + b.get("slug").asString,
                                                coverImageUrl = b.getAsJsonObject("coverUrl")?.get("value")?.asString ?: ""
                                            )
                                        )
                                    }
                                }
                                if (booksInPages.isNotEmpty()) return@tryConnect PagedList(booksInPages, index, true)
                                null
                            } else null
                            
                            items?.forEach { item ->
                                val b = item.asJsonObject
                                books.add(
                                    BookResult(
                                        title = b.get("name").asString,
                                        url = baseUrl + "/novel/" + b.get("slug").asString,
                                        coverImageUrl = b.getAsJsonObject("coverUrl")?.get("value")?.asString ?: ""
                                    )
                                )
                            }
                            if (books.isNotEmpty()) return@tryConnect PagedList(books, index, true)
                        }
                    }
                }
            }
            
            // HTML Fallback for Search
            doc.select(".novel-item, .search-result, .item").mapNotNull {
                val link = it.selectFirst("a[href*='/novel/']") ?: return@mapNotNull null
                val title = it.selectFirst(".title, h3, h4")?.text() ?: ""
                val cover = it.selectFirst("img")?.attr("abs:src") ?: ""
                if (title.isEmpty()) return@mapNotNull null
                BookResult(
                    title = title,
                    url = link.attr("abs:href"),
                    coverImageUrl = cover
                )
            }.let {
                if (it.isNotEmpty()) PagedList(it, index, true)
                else PagedList.createEmpty(index)
            }
        }
    }
}