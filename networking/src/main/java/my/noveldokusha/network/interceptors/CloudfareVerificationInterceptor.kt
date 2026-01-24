package my.noveldokusha.network.interceptors

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Robust Cloudflare Verification Interceptor using WebView.
 * Handles 403/503 responses by solving challenges in a headless WebView.
 */
class CloudFareVerificationInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    private val handler = Handler(Looper.getMainLooper())
    private val cookieManager = CookieManager.getInstance()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Check if blocked by Cloudflare (403 or 503)
        if (response.code == 403 || response.code == 503) {
            val body = response.peekBody(1024 * 10).string() // Peak to avoid consuming
            if (body.contains("cloudflare") || body.contains("cf-challenge") || body.contains("just a moment")) {
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
            try {
                val webView = WebView(context)
                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }
                
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val cookies = cookieManager.getCookie(url)
                        // Check for clearance cookie or if we passed the challenge
                        if (cookies != null && (cookies.contains("cf_clearance") || !view?.title?.lowercase()?.contains("just a moment")!!)) {
                            result = BypassResult(cookies, view?.settings?.userAgentString ?: "")
                            latch.countDown()
                            // Don't destroy immediately to ensure cookies persist? No, manager handles it.
                        }
                    }
                }
                webView.loadUrl(url)
            } catch (e: Exception) {
                latch.countDown()
            }
        }

        try {
            latch.await(20, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            // Timed out
        }
        
        return result
    }

    data class BypassResult(val cookies: String, val userAgent: String)
}
