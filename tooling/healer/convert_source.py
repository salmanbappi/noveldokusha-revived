import json
import sys
import os

def json_to_kotlin(json_path):
    with open(json_path, 'r') as f:
        config = json.load(f)
    
    class_name = config['name'].replace(' ', '').replace('(', '').replace(')', '')
    source_id = config['id']
    base_url = config['baseUrl']
    catalog_url = config['catalogUrl']
    language = config.get('language', 'ENGLISH').upper()
    selectors = config['selectors']

    template = f"""package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.toDocument
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

class {class_name}(private val networkClient: NetworkClient) : SourceInterface.Catalog {{
    override val id = "{source_id}"
    override val baseUrl = "{base_url}"
    override val catalogUrl = "{catalog_url}"
    override val language = LanguageCode.{language}

    override suspend fun getChapterTitle(doc: Document): String =
        doc.selectFirst("{selectors.get('chapterTitle', 'h1')}")?.text() ?: ""

    override suspend fun getChapterText(doc: Document): String =
        doc.selectFirst("{selectors.get('chapterText', '.content')}")?.let {{
            TextExtractor.get(it)
        }} ?: ""

    override suspend fun getBookDescription(bookUrl: String): Response<String?> =
        tryConnect {{
            networkClient.get(bookUrl).toDocument()
                .selectFirst("{selectors.get('bookDescription', '.description')}")?.let {{
                    TextExtractor.get(it)
                }}
        }}

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> =
        tryConnect {{
            networkClient.get(bookUrl).toDocument()
                .select("{selectors.get('chapterItem', 'a')}")
                .map {{ ChapterResult(it.text(), it.attr("abs:href")) }}
        }}

    override suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>> =
        getPagesList(index, if (index == 0) catalogUrl else catalogUrl.replace("{{page}}", (index + 1).toString()))

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> =
        getPagesList(index, "$baseUrl/search?q=$input")

    private suspend fun getPagesList(index: Int, url: String): Response<PagedList<BookResult>> =
        tryConnect {{
            val doc = networkClient.get(url).toDocument()
            val items = doc.select("{selectors.get('bookItem', '.item')}")
                .map {{
                    BookResult(
                        title = it.selectFirst("{selectors.get('bookItemTitle', 'h3')}")?.text() ?: "",
                        url = it.selectFirst("{selectors.get('bookItemUrl', 'a')}")?.attr("abs:href") ?: "",
                        coverImageUrl = it.selectFirst("{selectors.get('bookItemCover', 'img')}")?.attr("abs:src") ?: ""
                    )
                }}
            PagedList(items, index, doc.selectFirst("{selectors.get('nextPage', '.next')}") == null)
        }}

    private suspend fun <T> tryConnect(block: suspend () -> T): Response<T> {{
        return try {{
            Response.Success(block())
        }} catch (e: Exception) {{
            Response.Error(e.message ?: "Unknown error", e)
        }}
    }}
}}
"""
    output_path = f"scraper/src/main/java/my/noveldokusha/scraper/sources/{class_name}.kt"
    with open(output_path, 'w') as f:
        f.write(template)
    print(f"Generated {output_path}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 convert_source.py <path_to_json>")
    else:
        json_to_kotlin(sys.argv[1])
