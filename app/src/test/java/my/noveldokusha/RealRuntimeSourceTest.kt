package my.noveldokusha

import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.sources.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RealRuntimeSourceTest {

    private class RealNetworkClient : NetworkClient {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build()
                chain.proceed(request)
            }
            .build()

        override suspend fun call(request: Request.Builder, followRedirects: Boolean): okhttp3.Response {
            return client.newCall(request.build()).execute()
        }

        override suspend fun get(url: String): okhttp3.Response {
            val req = Request.Builder().url(url).build()
            return client.newCall(req).execute()
        }

        override suspend fun get(url: android.net.Uri.Builder): okhttp3.Response {
            return get(url.toString())
        }

        override suspend fun post(url: String, params: Map<String, String>): okhttp3.Response {
            val bodyBuilder = okhttp3.FormBody.Builder()
            params.forEach { (k, v) -> bodyBuilder.add(k, v) }
            val req = Request.Builder().url(url).post(bodyBuilder.build()).build()
            return client.newCall(req).execute()
        }
    }

    private val realNetworkClient = RealNetworkClient()

    @Test
    fun `test RoyalRoad search and catalog real runtime`() = kotlinx.coroutines.runBlocking {
        val royalRoad = RoyalRoad(realNetworkClient)
        val catalogRes = royalRoad.getCatalogList(0)
        assertTrue("Catalog list should succeed", catalogRes is Response.Success)
        val catalogList = (catalogRes as Response.Success).data
        assertFalse("Catalog list should not be empty", catalogList.list.isEmpty())
        assertTrue("Title should not be blank", catalogList.list[0].title.isNotBlank())

        val searchRes = royalRoad.getCatalogSearch(0, "lord of the mysteries")
        assertTrue("Search should succeed", searchRes is Response.Success)
        val searchList = (searchRes as Response.Success).data
        assertFalse("Search result should not be empty for 'lord of the mysteries'", searchList.list.isEmpty())
        assertTrue("Found title should contain text", searchList.list[0].title.isNotBlank())
    }

    @Test
    fun `test NovelFull search and catalog real runtime`() = kotlinx.coroutines.runBlocking {
        val novelFull = NovelFull(realNetworkClient)
        val catalogRes = novelFull.getCatalogList(0)
        assertTrue("Catalog list should succeed", catalogRes is Response.Success)

        val searchRes = novelFull.getCatalogSearch(0, "supreme magic")
        assertTrue("Search should succeed with spaces", searchRes is Response.Success)
        val searchList = (searchRes as Response.Success).data
        assertFalse("Search result should not be empty", searchList.list.isEmpty())
    }

    @Test
    fun `test ScribbleHub search and catalog real runtime`() = kotlinx.coroutines.runBlocking {
        val scribbleHub = ScribbleHub(realNetworkClient)
        val searchRes = scribbleHub.getCatalogSearch(0, "system level")
        assertTrue("Search should succeed with spaces", searchRes is Response.Success)
    }
}
