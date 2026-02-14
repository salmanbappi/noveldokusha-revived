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
            val slug = bookUrl.substringAfterLast("/")
            val chapters = mutableListOf<ChapterResult>()

            // Try to fetch novel detail from API first
            try {
                val detailUrl = "https://wuxia.click/api/novels/$slug/"
                val detailResponse = networkClient.get(detailUrl)
                val detailBody = detailResponse.body?.string()
                if (detailBody != null) {
                    val detailJson = gson.fromJson(detailBody, JsonObject::class.java)
                    val chapterList = detailJson.getAsJsonArray("chapterList")
                    chapterList?.forEach { chapter ->
                        val c = chapter.asJsonObject
                        chapters.add(
                            ChapterResult(
                                title = c.get("name").asString,
                                url = baseUrl + "/novel/" + slug + "/" + c.get("slug").asString
                            )
                        )
                    }
                    if (chapters.isNotEmpty()) return@tryConnect chapters
                }
            } catch (e: Exception) {
                // Ignore
            }

            // Fallback to Chapters API
            try {
                val apiUrl = "https://wuxia.click/api/novels/$slug/chapters?page=1"
                val response = networkClient.get(apiUrl)
                val responseBody = response.body?.string()
                if (responseBody != null && responseBody.trim().startsWith("{")) {
                    val apiJson = gson.fromJson(responseBody, JsonObject::class.java)
                    apiJson.getAsJsonArray("items")?.forEach { chapter ->
                        val c = chapter.asJsonObject
                        chapters.add(
                            ChapterResult(
                                title = c.get("name").asString,
                                url = baseUrl + "/novel/" + slug + "/" + c.get("slug").asString
                            )
                        )
                    }
                    if (chapters.isNotEmpty()) return@tryConnect chapters
                }
            } catch (e: Exception) {
                // Ignore
            }

            val doc = networkClient.get(bookUrl).toDocument()
            val scriptData = doc.select("#__NEXT_DATA__").firstOrNull()?.html()
            if (scriptData != null) {
                val json = gson.fromJson(scriptData, JsonObject::class.java)
                val queries = json.getAsJsonObject("props")
                    ?.getAsJsonObject("pageProps")
                    ?.getAsJsonObject("dehydratedState")
                    ?.getAsJsonArray("queries")
                
                queries?.forEach { query ->
                    val state = query.asJsonObject.getAsJsonObject("state")
                    val data = state?.getAsJsonObject("data")
                    if (data != null) {
                        val item = if (data.has("item")) data.getAsJsonObject("item") else data
                        val chapterList = if (item.has("chapterList")) item.getAsJsonArray("chapterList") else null
                        chapterList?.forEach { chapter ->
                            val c = chapter.asJsonObject
                            chapters.add(
                                ChapterResult(
                                    title = c.get("name").asString,
                                    url = baseUrl + "/novel/" + slug + "/" + c.get("slug").asString
                                )
                            )
                        }
                    }
                }
                if (chapters.isNotEmpty()) return@tryConnect chapters.distinctBy { it.url }
            }
            
            doc.select(".list-chapter a, #list-chapter a, #chapters a, #chapter-list a, .list-truyen a").map {
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
                    val data = query.asJsonObject.getAsJsonObject("state")?.get("data")
                    if (data != null) {
                        val books = mutableListOf<BookResult>()
                        if (data.isJsonArray) {
                            data.asJsonArray.forEach { entry ->
                                val entryObj = entry.asJsonObject
                                val novels = entryObj.getAsJsonArray("novels") ?: entryObj.getAsJsonArray("items")
                                novels?.forEach { novel ->
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
                        }
                        if (books.isNotEmpty()) return@tryConnect PagedList(books.distinctBy { it.url }, index, true)
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
                    val data = query.asJsonObject.getAsJsonObject("state")?.get("data")
                    if (data != null) {
                        val books = mutableListOf<BookResult>()
                        if (data.isJsonArray) {
                            data.asJsonArray.forEach { entry ->
                                val entryObj = entry.asJsonObject
                                val novels = entryObj.getAsJsonArray("novels") ?: entryObj.getAsJsonArray("items") ?: entryObj.getAsJsonArray("results")
                                novels?.forEach { novel ->
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
                        } else if (data.isJsonObject) {
                            val results = data.asJsonObject.getAsJsonArray("results") ?: data.asJsonObject.getAsJsonArray("items") ?: data.asJsonObject.getAsJsonObject("data")?.getAsJsonArray("results")
                            results?.forEach { novel ->
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
                if (books.isNotEmpty()) return@tryConnect PagedList(books.distinctBy { it.url }, index, true)
            }
            
            // HTML Fallback
            doc.select(".list-novel .row, .list-truyen .row, .item").mapNotNull {
                val link = it.selectFirst("a[href*='/novel/']") ?: return@mapNotNull null
                val title = it.selectFirst("h3, .title, .tit")?.text() ?: link.text()
                val cover = it.selectFirst("img")?.attr("abs:src") ?: ""
                BookResult(
                    title = title,
                    url = link.attr("abs:href"),
                    coverImageUrl = cover
                )
            }.let {
                if (it.isNotEmpty()) PagedList(it.distinctBy { b -> b.url }, index, true)
                else PagedList.createEmpty(index)
            }
        }
    }
}
        }
    }
}