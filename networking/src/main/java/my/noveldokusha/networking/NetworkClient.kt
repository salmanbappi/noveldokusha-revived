package my.noveldokusha.networking

import android.content.Context
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class NetworkClient(private val context: Context) {

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(refererInterceptor)
            .addInterceptor(CloudflareInterceptor(context)) // Automatic Cloudflare Bypass
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val refererInterceptor = { chain: okhttp3.Interceptor.Chain ->
        val request = chain.request()
        val host = request.url.host
        val newRequest = request.newBuilder()
            .header("Referer", "https://$host/")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()
        chain.proceed(newRequest)
    }
}
