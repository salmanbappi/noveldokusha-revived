package my.noveldokusha.network

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import my.noveldokusha.core.AppInternalState
import my.noveldokusha.network.interceptors.CloudfareVerificationInterceptor
import my.noveldokusha.network.interceptors.DecodeResponseInterceptor
import my.noveldokusha.network.interceptors.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkClient {
    suspend fun call(request: Request.Builder, followRedirects: Boolean = false): Response
    suspend fun get(url: String): Response
    suspend fun get(url: Uri.Builder): Response
    suspend fun post(url: String, params: Map<String, String>): Response
}

@Singleton
class ScraperNetworkClient @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appInternalState: AppInternalState,
) : NetworkClient {

    private val cacheDir = File(appContext.cacheDir, "network_cache")
    private val cacheSize = 5L * 1024 * 1024

    private val cookieJar = ScraperCookieJar()

    private val okhttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor {
            Timber.v(it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Interceptor to add Referer header based on the URL host.
     * Many light novel sites block image requests without a valid Referer.
     */
    private val refererInterceptor = Interceptor { chain ->
        val request = chain.request()
        val url = request.url
        val host = url.host
        
        val newRequest = if (request.header("Referer") == null) {
            request.newBuilder()
                .addHeader("Referer", "https://$host/")
                .build()
        } else {
            request
        }
        chain.proceed(newRequest)
    }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (appInternalState.isDebugMode) {
                    addInterceptor(okhttpLoggingInterceptor)
                }
            }
            .addInterceptor(UserAgentInterceptor("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"))
            .addInterceptor(refererInterceptor)
            .addInterceptor(DecodeResponseInterceptor())
            .addInterceptor(CloudfareVerificationInterceptor(appContext))
            .cookieJar(cookieJar)
            .cache(Cache(cacheDir, cacheSize))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val clientWithRedirects: OkHttpClient by lazy {
        client.newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    override suspend fun call(request: Request.Builder, followRedirects: Boolean): Response {
        return if (followRedirects) clientWithRedirects.call(request) else client.call(request)
    }

    override suspend fun get(url: String) = call(getRequest(url))
    override suspend fun get(url: Uri.Builder) = call(getRequest(url.toString()))

    override suspend fun post(url: String, params: Map<String, String>): Response {
        val body = okhttp3.FormBody.Builder().apply {
            params.forEach { (key, value) -> add(key, value) }
        }.build()
        return call(getRequest(url).post(body))
    }
}