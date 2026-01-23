package my.noveldokusha.networking

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CloudflareInterceptor(private val context: Context) : Interceptor {

    private val handler = Handler(Looper.getMainLooper())
    private val cookieManager = CookieManager.getInstance()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Check if we are being blocked by Cloudflare (403 Forbidden or 503 Service Unavailable with specific headers)
        if (response.code == 403 || response.code == 503) {
            val body = response.peekBody(1024 * 10).string()
            if (body.contains("cloudflare") || body.contains("cf-challenge")) {
                response.close()
                
                // Trigger WebView bypass
                val bypassResult = resolveChallenge(request.url.toString())
                if (bypassResult != null) {
                    val newRequest = request.newBuilder()
                        .header("Cookie", bypassResult.cookies)
                        .header("User-Agent", bypassResult.userAgent)
                        .build()
                    return chain.proceed(newRequest)
                }
            }
        }

        return response
    }

    private fun resolveChallenge(url: String): BypassResult? {
        val latch = CountDownLatch(1)
        var result: BypassResult? = null

        handler.post {
            val webView = WebView(context)
            webView.settings.javaScriptEnabled = true
            webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val cookies = cookieManager.getCookie(url)
                    if (cookies != null && (cookies.contains("cf_clearance") || cookies.contains("__cf_bm"))) {
                        result = BypassResult(cookies, view?.settings?.userAgentString ?: "")
                        latch.countDown()
                        webView.destroy()
                    }
                }
            }
            webView.loadUrl(url)
        }

        latch.await(30, TimeUnit.SECONDS)
        return result
    }

    data class BypassResult(val cookies: String, val userAgent: String)
}
