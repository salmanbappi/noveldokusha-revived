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
import my.noveldokusha.network.postRequest
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUrlBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class MeioNovel(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "meionovel"
    override val nameStrId = R.string.source_name_meio_novel
    override val baseUrl = "https://meionovels.com"
    override val catalogUrl = "https://meionovels.com/novel/"
    override val language = LanguageCode.INDONESIAN

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".entry-content")?.let { TextExtractor.get(it) } ?: ""
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".thumb img")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".entry-content")?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .select(".eplister li a")
                .map {
                    ChapterResult(
                        title = it.selectFirst(".epl-title")?.text() ?: it.text(),
                        url = it.attr("abs:href")
                    )
                }
        }
    }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = if (page == 1) catalogUrl else "${catalogUrl}page/$page/"
            val doc = networkClient.get(url).toDocument()
            doc.select(".list-novel .row, .listupd .bs")
                .mapNotNull {
                    val link = it.selectFirst("a") ?: return@mapNotNull null
                    BookResult(
                        title = it.selectFirst(".tt, h3, .title")?.text() ?: "",
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = if (page == 1) "$baseUrl?s=$input" else "$baseUrl/page/$page/?s=$input"
            val doc = networkClient.get(url).toDocument()
            doc.select(".listupd .bs")
                .mapNotNull {
                    val link = it.selectFirst("a") ?: return@mapNotNull null
                    BookResult(
                        title = it.selectFirst(".tt")?.text() ?: "",
                        url = link.attr("abs:href"),
                        coverImageUrl = it.selectFirst("img")?.attr("abs:src") ?: ""
                    )
                }
                .let { PagedList(it, index, it.isEmpty()) }
        }
    }
}
