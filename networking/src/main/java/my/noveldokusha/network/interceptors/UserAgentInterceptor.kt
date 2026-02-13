package my.noveldokusha.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response


internal class UserAgentInterceptor(
    private val userAgent: String = DEFAULT_USER_AGENT
) : Interceptor {

    companion object {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val hasNoUserAgent = originalRequest.header("User-Agent").isNullOrBlank()
        val modifiedRequest = if (hasNoUserAgent) {
            originalRequest
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", userAgent)
                .build()
        } else originalRequest
        return chain.proceed(modifiedRequest)
    }
}