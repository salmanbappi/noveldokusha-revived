package my.noveldokusha.scraper.sources

import my.noveldokusha.scraper.R
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult

class Novelhall(private val networkClient: NetworkClient) : SourceInterface.Catalog {
    override val id = "novelhall"
    override val nameStrId = R.string.source_name_novelhall
    override val baseUrl = "https://www.novelhall.com/"
    override val catalogUrl = "https://www.novelhall.com/all/ranking.html"
    override val language = LanguageCode.ENGLISH

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> {
        return Response.Success(emptyList()) // Selector based logic in Scraper.kt
    }

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> {
        return Response.Success(PagedList(emptyList(), index, true))
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> {
        return Response.Success(PagedList(emptyList(), index, true))
    }
}