package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.add
import my.noveldokusha.network.addPath
import my.noveldokusha.network.ifCase
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUrlBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class BoxNovel(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "box_novel"
    override val nameStrId = R.string.source_name_box_novel
    override val baseUrl = "https://boxnovel.webflow.io"
    override val catalogUrl = "https://boxnovel.webflow.io/novel"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".chapter-text.w-richtext")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst("img.image-10")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".novel-temp-summary")
                ?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .select(".chaplistcol a")
                .map {
                    ChapterResult(
                        title = it.selectFirst(".chaptitle")?.text() ?: it.text(),
                        url = it.attr("abs:href")
                    )
                }
                .reversed()
        }
    }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        if (index > 0) return@withContext Response.Success(PagedList.createEmpty(index))
        tryConnect {
            val doc = networkClient.get(catalogUrl).toDocument()
            doc.select(".novelcollection")
                .mapNotNull {
                    val link = it.selectFirst(".books a") ?: return@mapNotNull null
                    val title = it.selectFirst(".books0detail.title")?.text() ?: ""
                    val cover = it.selectFirst(".book-cover")?.attr("abs:src") ?: ""
                    BookResult(
                        title = title,
                        url = link.attr("abs:href"),
                        coverImageUrl = cover
                    )
                }
                .let {
                    PagedList(
                        list = it,
                        index = index,
                        isLastPage = true // Webflow demo site seems static
                    )
                }
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        if (index > 0) return@withContext Response.Success(PagedList.createEmpty(index))
        tryConnect {
            val url = baseUrl.toUrlBuilderSafe()
                .addPath("search")
                .add("query", input)
                .toString()

            val doc = networkClient.get(url).toDocument()
            // Search results on webflow are a bit different, but let's try a generic selector
            doc.select(".search-result-items > div")
                .mapNotNull {
                    val link = it.selectFirst("a") ?: return@mapNotNull null
                    BookResult(
                        title = link.text(),
                        url = link.attr("abs:href"),
                        coverImageUrl = "" // Search results usually don't have images on Webflow search
                    )
                }
                .let {
                    PagedList(
                        list = it,
                        index = index,
                        isLastPage = true
                    )
                }
        }
    }
}