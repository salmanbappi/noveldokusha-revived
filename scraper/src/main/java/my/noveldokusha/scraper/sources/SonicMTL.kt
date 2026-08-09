package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.templates.BaseMadaraScraper
import org.jsoup.nodes.Document

class SonicMTL(
    networkClient: NetworkClient
) : BaseMadaraScraper(networkClient) {
    override val id = "sonicmtl"
    override val nameStrId = R.string.source_name_sonicmtl
    override val baseUrl = "https://sonicmtl.com/"
    override val catalogUrl = "https://sonicmtl.com/novel/?m_orderby=latest"
    override val iconUrl = "https://sonicmtl.com/favicon.ico"
    override val language = LanguageCode.ENGLISH
    
    override val catalogOrderBy = "latest"
    
    override suspend fun getChapterText(doc: Document): String = withContext(Dispatchers.Default) {
        val container = doc.selectFirst(".reading-content .text-left")
            ?: doc.selectFirst(".reading-content")
        container?.let { element ->
            element.select(".ad, .c-ads, .custom-code, .body-top-ads, .before-content-ad, .autors-widget, script, style").remove()
            TextExtractor.get(element)
        } ?: ""
    }
}
