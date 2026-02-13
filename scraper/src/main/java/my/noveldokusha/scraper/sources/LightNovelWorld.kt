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
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class LightNovelWorld(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "light_novel_world"
    override val nameStrId = R.string.source_name_light_novel_world
    override val baseUrl = "https://lightnovelworld.org"
    override val catalogUrl = "https://lightnovelworld.org/genre-all/"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        doc.selectFirst(".chapter-container")?.let(TextExtractor::get) ?: ""
    }

    override suspend fun getBookCoverImageUrl(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".book-cover img")?.attr("abs:src")
        }
    }

    override suspend fun getBookDescription(
        bookUrl: String
    ): Response<String?> = withContext(Dispatchers.Default) {
        tryConnect {
            networkClient.get(bookUrl).toDocument()
                .selectFirst(".summary-content")
                ?.let { TextExtractor.get(it) }
        }
    }

    override suspend fun getChapterList(
        bookUrl: String
    ): Response<List<ChapterResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val list = mutableListOf<ChapterResult>()
            val chaptersUrl = if (bookUrl.endsWith("/")) "${bookUrl}chapters/" else "$bookUrl/chapters/"

            for (page in 1..Int.MAX_VALUE) {
                val url = chaptersUrl.toUrlBuilderSafe().add("page", page).toString()

                val doc = networkClient.get(url).toDocument()
                val res = doc.select(".chapter-card")
                    .mapNotNull {
                        val onclick = it.attr("onclick")
                        val path = onclick.substringAfter("'").substringBefore("'")
                        if (path.isEmpty()) return@mapNotNull null
                        ChapterResult(
                            title = it.selectFirst(".chapter-name")?.text() ?: it.text(),
                            url = baseUrl + path
                        )
                    }

                if (res.isEmpty())
                    break
                list.addAll(res)
                
                // Check if there is a next page
                val hasNext = doc.select("#pageSelect option").any { it.attr("value") == (page + 1).toString() }
                if (!hasNext) break
            }
            list
        }
    }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = catalogUrl.toUrlBuilderSafe().add("page", page).toString()
            parseToBooks(networkClient.get(url).toDocument(), index)
        }
    }

    override suspend fun getCatalogSearch(
        index: Int,
        input: String
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val page = index + 1
            val url = baseUrl.toUrlBuilderSafe()
                .addPath("search")
                .add("q", input)
                .add("page", page)
                .toString()
            parseToBooks(networkClient.get(url).toDocument(), index)
        }
    }

    private fun parseToBooks(doc: Document, index: Int): PagedList<BookResult> {
        val books = doc.select(".recommendation-card, .novel-item")
            .mapNotNull {
                val link = it.selectFirst("a.card-cover-link, a[href*='/novel/']")
                var url = link?.attr("abs:href") ?: ""
                if (url.isEmpty()) {
                    val href = link?.attr("href") ?: return@mapNotNull null
                    url = if (href.startsWith("http")) href else baseUrl + (if (href.startsWith("/")) "" else "/") + href
                }
                
                val title = it.selectFirst(".card-title, .novel-title, a[title]")?.text()
                    ?: it.selectFirst("img")?.attr("alt")
                    ?: ""
                
                val cover = it.selectFirst(".card-cover img, .novel-cover img, img")?.attr("abs:src") ?: ""
                BookResult(title, url, cover)
            }
        
        val isLastPage = doc.select("#pageSelect option, ul.pagination li").last()?.`is`(".active, [selected]") ?: true
        
        return PagedList(books, index, isLastPage)
    }
}