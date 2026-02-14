package my.noveldokusha.scraper.sources

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

class LightNovelPub(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "lightnovelpub"
    override val nameStrId = R.string.source_name_lightnovelpub
    override val baseUrl = "https://lightnovelpub.me"
    override val catalogUrl = "https://lightnovelpub.me/list/most-popular-novels"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst("#chapter-container")?.let { TextExtractor.get(it) } 
            ?: doc.selectFirst("#chapter-content")?.let { TextExtractor.get(it) }
            ?: doc.selectFirst(".chapter-content")?.let { TextExtractor.get(it) }
            ?: doc.selectFirst(".chr-c")?.let { TextExtractor.get(it) }
            ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".fixed-img img, .book-img img")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".summary .content, .description .content, #panel-des")?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val chapters = mutableListOf<ChapterResult>()
            
            // Normalize URL to /book/ if needed
            val normalizedUrl = if (bookUrl.contains("/novel/")) bookUrl.replace("/novel/", "/book/") else bookUrl
            
            // Try fetching from the main page first
            val mainDoc = networkClient.get(normalizedUrl).toDocument()
            mainDoc.select(".ul-list5 a, .chapter-list a, .list-chapter a").forEach {
                val title = it.selectFirst(".chapter-title, .title")?.text() ?: it.text()
                if (title.isNotBlank()) {
                    chapters.add(
                        ChapterResult(
                            title = title,
                            url = it.attr("abs:href")
                        )
                    )
                }
            }
            
            // If empty, try the /chapters subpage
            if (chapters.isEmpty()) {
                val chaptersUrl = if (normalizedUrl.endsWith("/")) "${normalizedUrl}chapters" else "$normalizedUrl/chapters"
                val chapDoc = networkClient.get(chaptersUrl).toDocument()
                chapDoc.select(".ul-list5 a, .chapter-list a, .list-chapter a").forEach {
                    val title = it.selectFirst(".chapter-title, .title")?.text() ?: it.text()
                    if (title.isNotBlank()) {
                        chapters.add(
                            ChapterResult(
                                title = title,
                                url = it.attr("abs:href")
                            )
                        )
                    }
                }
            }
            
            chapters.distinctBy { it.url }
        }
    }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "$catalogUrl?page=$page"
            val doc = networkClient.get(url).toDocument()
            doc.select(".li")
                .mapNotNull {
                    val link = it.selectFirst("h3.tit a") ?: return@mapNotNull null
                    val bookCover = it.selectFirst("img")?.attr("abs:src") ?: it.selectFirst("img")?.attr("abs:data-src") ?: ""
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href").replace("/novel/", "/book/"),
                        coverImageUrl = bookCover
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = if (page == 1) "https://lightnovelpub.me/search/" else "https://lightnovelpub.me/search/page/$page"
            val doc = networkClient.post(url, mapOf("searchkey" to input)).toDocument()
            doc.select(".li")
                .mapNotNull {
                    val link = it.selectFirst("h3.tit a") ?: return@mapNotNull null
                    val bookCover = it.selectFirst("img")?.attr("abs:src") ?: it.selectFirst("img")?.attr("abs:data-src") ?: ""
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href").replace("/novel/", "/book/"),
                        coverImageUrl = bookCover
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }
}