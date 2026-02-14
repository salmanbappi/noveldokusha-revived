package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.add
import my.noveldokusha.network.addPath
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUrlBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.network.tryFlatConnect
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

/**
 * Novel main page (chapter list) example:
 * https://readnovelfull.com/reincarnation-of-the-strongest-sword-god-v2.html
 * Chapter url example:
 * https://readnovelfull.com/reincarnation-of-the-strongest-sword-god/chapter-1-starting-over-v1.html
 */
class ReadNovelFull(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "read_novel_full"
    override val nameStrId = R.string.source_name_read_novel_full
    override val baseUrl = "https://readnovelfull.com"
    override val catalogUrl = "https://readnovelfull.com/novel-list/most-popular-novel"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst("#chapter-content, #chr-content, .chapter-c, .chr-c")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".book img")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument().selectFirst("#tab-description")
                ?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val doc = networkClient.get(bookUrl).toDocument()
            val id = doc.selectFirst("#rating")?.attr("data-novel-id")
            val chapters = mutableListOf<ChapterResult>()
            
            if (id != null) {
                try {
                    val url = "https://readnovelfull.com/ajax/chapter-archive?novelId=$id"
                    networkClient.get(url).toDocument()
                        .select("a[href]")
                        .mapTo(chapters) {
                            ChapterResult(
                                title = it.text(),
                                url = it.attr("abs:href")
                            )
                        }
                } catch (e: Exception) {
                    // Fallback
                }
            }
            
            if (chapters.isEmpty()) {
                doc.select("#list-chapter li a, .list-chapter li a")
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

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryFlatConnect {
            val page = index + 1
            val url = "$catalogUrl?page=$page"
            val doc = networkClient.get(url).toDocument()
            parseToBooks(doc, index)
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryFlatConnect {
            if (input.isBlank())
                return@tryFlatConnect Response.Success(PagedList.createEmpty(index = index))

            val page = index + 1
            // Use standard search path
            val url = "https://readnovelfull.com/novel-list/search?keyword=$input&page=$page"
            val doc = networkClient.get(url).toDocument()
            parseToBooks(doc, index)
        }
    }

    private suspend fun parseToBooks(
        doc: Document,
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            doc.select(".list-novel .row, .list-truyen .row")
                .mapNotNull {
                    val link = it.selectFirst("h3.novel-title a, h3.truyen-title a, h3.title a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let {
                    PagedList(
                        list = it,
                        index = index,
                        isLastPage = it.isEmpty() || doc.select("ul.pagination li.next.disabled").isNotEmpty()
                    )
                }
        }
    }
}
