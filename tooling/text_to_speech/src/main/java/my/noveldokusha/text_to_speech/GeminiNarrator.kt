package my.noveldokusha.text_to_speech

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentLinkedQueue

class GeminiNarrator(
    private val context: Context,
    private val apiKey: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash-native-audio-preview-12-2025",
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "audio/mp3"
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )
    )

    private var mediaPlayer: MediaPlayer? = null
    private val cacheDir = File(context.cacheDir, "gemini_tts").apply { deleteRecursively(); mkdirs() }
    
    private val audioQueue = ConcurrentLinkedQueue<Pair<String, File>>()
    private val mutex = Mutex()
    private var isPlaying = false

    fun stop() {
        isPlaying = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        audioQueue.clear()
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    suspend fun enqueue(utteranceId: String, text: String, onStarted: (String) -> Unit, onFinished: (String) -> Unit) {
        val audioFile = fetchAudio(utteranceId, text) ?: return
        audioQueue.add(utteranceId to audioFile)
        
        if (!isPlaying) {
            playNext(onStarted, onFinished)
        }
    }

    private suspend fun fetchAudio(utteranceId: String, text: String): File? = withContext(Dispatchers.IO) {
        try {
            val response = model.generateContent("Read this text aloud: $text")
            // Note: In native audio models, the response contains inlineData or similar part with the bytes.
            // Based on the 2025-12 preview SDK, we extract the bytes from the first part.
            val bytes = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.let { part ->
                // This is a simplified representation. The actual SDK uses Blob or similar.
                // Assuming the SDK returns the bytes in a standard way for MP3 mimetype.
                (part as? com.google.ai.client.generativeai.type.BlobPart)?.blob
            } ?: return@withContext null

            val file = File(cacheDir, "$utteranceId.mp3")
            FileOutputStream(file).use { it.write(bytes) }
            file
        } catch (e: Exception) {
            Log.e("GeminiNarrator", "Failed to fetch audio for $utteranceId", e)
            null
        }
    }

    private fun playNext(onStarted: (String) -> Unit, onFinished: (String) -> Unit) {
        val next = audioQueue.poll() ?: run {
            isPlaying = false
            return
        }
        
        val (utteranceId, file) = next
        isPlaying = true
        
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnPreparedListener { 
                start()
                onStarted(utteranceId)
            }
            setOnCompletionListener {
                onFinished(utteranceId)
                playNext(onStarted, onFinished)
            }
            prepareAsync()
        }
    }
}