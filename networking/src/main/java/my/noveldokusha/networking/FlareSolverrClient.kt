package my.noveldokusha.networking

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * FlareSolverr Client for bypassing Cloudflare
 * Connects to a local FlareSolverr instance (default port 8191)
 */
class FlareSolverrClient(private val solverUrl: String = "http://localhost:8191/v1") {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun fetch(url: String): String? {
        val json = JSONObject().apply {
            put("cmd", "request.get")
            put("url", url)
            put("maxTimeout", 60000)
        }

        val request = Request.Builder()
            .url(solverUrl)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return null
                val result = JSONObject(body)
                if (result.getString("status") == "ok") {
                    result.getJSONObject("solution").getString("response")
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
