package my.noveldokusha.scraper.sources

import my.noveldokusha.scraper.R
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.SourceInterface

class Novelhall(networkClient: NetworkClient) : SourceInterface.Catalog(networkClient) {
    override val nameStrId = R.string.source_name_novelhall
    override val baseUrl = "https://www.novelhall.com/"
    override val language = my.noveldokusha.core.LanguageCode.EN
}