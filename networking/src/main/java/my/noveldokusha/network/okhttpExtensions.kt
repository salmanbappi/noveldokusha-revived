package my.noveldokusha.network

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
        cancel()
    }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isActive) continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (continuation.isActive) continuation.resume(response)
        }
    })
}

suspend fun OkHttpClient.call(builder: Request.Builder) = newCall(builder.build()).await()

fun Response.toDocument(): Document = use { resp ->
    Jsoup.parse(resp.body?.string() ?: "", resp.request.url.toString())
}

fun Response.toDocument(charset: String): Document = use { resp ->
    val bytes = resp.body?.bytes() ?: return@use Jsoup.parse("", resp.request.url.toString())
    Jsoup.parse(String(bytes, charset(charset)), resp.request.url.toString())
}

fun Response.toJson(): JsonElement = use { resp ->
    JsonParser.parseString(resp.body?.string() ?: "{}")
}
