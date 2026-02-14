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
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class NovelFull(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "novelfull"
    override val nameStrId = R.string.source_name_novelfull
    override val baseUrl = "https://novelfull.com"
    override val catalogUrl = "https://novelfull.com/most-popular"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst("#chapter-content, #chr-content, .chapter-c, .chr-c")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".book img")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".desc-text")?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            val novelId = doc.selectFirst("#rating")?.attr("data-novel-id")
            val chapters = mutableListOf<ChapterResult>()
            
            if (novelId != null) {
                try {
                    val ajaxUrl = "https://novelfull.com/ajax/chapter-archive?novelId=$novelId"
                    networkClient.get(ajaxUrl).toDocument()
                        .select("ul.list-chapter li a")
                        .mapTo(chapters) {
                            ChapterResult(
                                title = it.text(),
                                url = it.attr("abs:href")
                            )
                        }
                } catch (e: Exception) {
                    // Fallback to direct parsing
                }
            }
            
            if (chapters.isEmpty()) {
                doc.select("#list-chapter li a, .list-chapter li a, .list-chapter a")
                    .mapTo(chapters) {
                        ChapterResult(
                            title = it.text(),
                            url = it.attr("abs:href")
                        )
                    }
            }
            chapters
        }
    }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = "$catalogUrl?page=$page"
            val doc = networkClient.get(url).toDocument()
            doc.select(".list-novel .row, .list-truyen .row")
                .mapNotNull {
                    val link = it.selectFirst("h3.novel-title a, h3.truyen-title a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val url = "https://novelfull.com/search?keyword=$input&page=${index + 1}"
            networkClient.get(url).toDocument().select(".list-novel .row, .list-truyen .row")
                .mapNotNull {
                    val link = it.selectFirst("h3.title a, h3.truyen-title a, h3.novel-title a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }
}
