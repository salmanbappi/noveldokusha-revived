package my.noveldokusha.text_to_speech

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Free Online Narrator using Google Translate TTS API (Reliable, No Keys)
 */
class OnlineNarrator(
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mediaPlayer: MediaPlayer? = null
    private val cacheDir = File(context.cacheDir, "online_tts_v1").apply { deleteRecursively(); mkdirs() }
    
    private val audioQueue = ConcurrentLinkedQueue<Triple<String, File, String>>()
    private var isPlaying = false
    
    private var currentLanguage: String = "en"

    fun setMood(mood: String) {
        // Moods not supported by basic Google TTS, but we keep the signature
    }

    fun stop() {
        this@OnlineNarrator.isPlaying = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        audioQueue.clear()
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    fun enqueue(utteranceId: String, text: String, onStarted: (String) -> Unit, onFinished: (String) -> Unit) {
        scope.launch {
            val audioFile = fetchAudio(utteranceId, text) ?: return@launch
            audioQueue.add(Triple(utteranceId, audioFile, text))
            
            if (!this@OnlineNarrator.isPlaying) {
                withContext(Dispatchers.Main) {
                    playNext(onStarted, onFinished)
                }
            }
        }
    }

    private suspend fun fetchAudio(utteranceId: String, text: String): File? = withContext(Dispatchers.IO) {
        try {
            val encodedText = URLEncoder.encode(text.take(200), "UTF-8")
            val urlString = "https://translate.google.com/translate_tts?ie=UTF-8&q=$encodedText&tl=$currentLanguage&client=tw-ob"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            
            val file = File(cacheDir, "tts_$utteranceId.mp3")
            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0) file else null
        } catch (e: Exception) {
            Log.e("OnlineNarrator", "TTS Fetch failed", e)
            null
        }
    }

    private fun playNext(onStarted: (String) -> Unit, onFinished: (String) -> Unit) {
        if (!this@OnlineNarrator.isPlaying && audioQueue.isEmpty()) return

        val next = audioQueue.poll() ?: run {
            this@OnlineNarrator.isPlaying = false
            return
        }
        
        val (utteranceId, file, _) = next
        this@OnlineNarrator.isPlaying = true
        
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnPreparedListener { 
                start()
                onStarted(utteranceId)
            }
            setOnCompletionListener {
                this@OnlineNarrator.isPlaying = false
                onFinished(utteranceId)
                playNext(onStarted, onFinished)
            }
            setOnErrorListener { _, _, _ ->
                this@OnlineNarrator.isPlaying = false
                onFinished(utteranceId)
                playNext(onStarted, onFinished)
                true
            }
            prepareAsync()
        }
    }

    fun shutdown() {
        stop()
        scope.cancel()
    }
}
