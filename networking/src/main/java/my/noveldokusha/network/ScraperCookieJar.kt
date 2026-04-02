package my.noveldokusha.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

internal class ScraperCookieJar : CookieJar {
    private val manager = CookieManager.getInstance().also {
        it.setAcceptCookie(true)
    }

    private fun getCookieList(url: String?): List<String> {
        url ?: return emptyList()
        return manager.getCookie(url)?.split(";") ?: emptyList()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        
        // Load for exact URL
        getCookieList(url.toString()).forEach { 
            Cookie.parse(url, it)?.let { cookie -> cookies.add(cookie) }
        }
        
        // Also load for base URL (domain) if different
        val baseUrl = "${url.scheme}://${url.host}/"
        if (baseUrl != url.toString()) {
            getCookieList(baseUrl).forEach {
                Cookie.parse(url, it)?.let { cookie ->
                    // Add only if not already present
                    if (cookies.none { c -> c.name == cookie.name }) {
                        cookies.add(cookie)
                    }
                }
            }
        }
        
        return cookies
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        for (cookieEntry in cookies) {
            manager.setCookie(url.toString(), "${cookieEntry.name}=${cookieEntry.value}")
        }
        manager.flush()
    }
}
