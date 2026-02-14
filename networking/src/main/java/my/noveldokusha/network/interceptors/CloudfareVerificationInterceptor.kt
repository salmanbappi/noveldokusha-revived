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
class CloudfareVerificationInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    private val handler = Handler(Looper.getMainLooper())
    private val cookieManager = CookieManager.getInstance()
    private val standardUserAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Check if blocked by Cloudflare or other bot protections (403, 503 OR 200 with challenge)
        val body = response.peekBody(1024 * 10).string().lowercase()
        val isChallenge = response.code == 403 || response.code == 503 || 
            (response.code == 200 && (body.contains("cf-challenge") || body.contains("just a moment") || body.contains("verify you are human") || body.contains("verification required") || body.contains("redirecting...") || body.contains("detecting if you are a bot")))

        if (isChallenge && (body.contains("cloudflare") || body.contains("sucuri") || body.contains("parklogic") || body.contains("javascript") || body.contains("redirect"))) {
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
                    userAgentString = standardUserAgent
                }
                
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val cookies = cookieManager.getCookie(url)
                        val title = view?.title?.lowercase() ?: ""
                        // Check for clearance cookie or if we passed the challenge
                        if (cookies != null && (cookies.contains("cf_clearance") || (!title.contains("just a moment") && !title.contains("verify")))) {
                            result = BypassResult(cookies, view?.settings?.userAgentString ?: standardUserAgent)
                            latch.countDown()
                            webView.destroy()
                        }
                    }
                }
                webView.loadUrl(url)
            } catch (e: Exception) {
                latch.countDown()
            }
        }

        try {
            latch.await(30, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            // Timed out
        }
        
        return result
    }

    data class BypassResult(val cookies: String, val userAgent: String)
}