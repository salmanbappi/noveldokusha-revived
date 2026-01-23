package my.noveldokusha.text_to_speech

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentLinkedQueue

class GeminiNarrator(
    private val context: Context,
    private val apiKey: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash-preview-tts",
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "audio/mp3"
            // Note: The SDK might not support setting response_modalities via DSL yet, 
            // but the model defaults to audio output if text is provided to a TTS model.
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )
    )

    private var mediaPlayer: MediaPlayer? = null
    private val cacheDir = File(context.cacheDir, "gemini_tts_v4").apply { deleteRecursively(); mkdirs() }
    
    private val audioQueue = ConcurrentLinkedQueue<Triple<String, File, String>>() // ID, File, Text
    private var isPlaying = false
    private var currentMood: String = "professional audiobook narrator, calm and steady"

    fun setMood(mood: String) {
        this.currentMood = when(mood.lowercase()) {
            "bedtime" -> "Whisper gently, slow pacing, soothing tone"
            "action" -> "Fast-paced, energetic, intense tone"
            "horror" -> "Deep, ominous, slow and scary tone"
            else -> mood
        }
    }

    fun stop() {
        this@GeminiNarrator.isPlaying = false
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
            
            if (!this@GeminiNarrator.isPlaying) {
                withContext(Dispatchers.Main) {
                    playNext(onStarted, onFinished)
                }
            }
        }
    }

    private suspend fun fetchAudio(utteranceId: String, text: String): File? = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Narrate the following story text.
                Voice Style: $currentMood.
                Pacing: Natural, with pauses for suspense.
                
                Text to narrate:
                "$text"
            """.trimIndent()

            val response = model.generateContent(prompt)
            
            // EXTRACT LOGIC: Based on laboratory findings
            // The response part contains inlineData with base64 encoded audio
            val base64Data = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.let { part ->
                // Accessing the internal data structure of the part
                // In the current SDK version, we need to handle the conversion from the Part object
                // If it's a blob/inlineData, we get its data.
                val partString = part.toString()
                if (partString.contains("data=")) {
                    // Manually extract if SDK doesn't expose it cleanly yet
                    partString.substringAfter("data=").substringBefore(",")
                } else null
            } ?: return@withContext null

            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val file = File(cacheDir, "$utteranceId.mp3")
            FileOutputStream(file).use { it.write(bytes) }
            file
        } catch (e: Exception) {
            Log.e("GeminiNarrator", "Failed to fetch audio for $utteranceId", e)
            null
        }
    }

    private fun playNext(onStarted: (String) -> Unit, onFinished: (String) -> Unit) {
        if (!this@GeminiNarrator.isPlaying && audioQueue.isEmpty()) return

        val next = audioQueue.poll() ?: run {
            this@GeminiNarrator.isPlaying = false
            return
        }
        
        val (utteranceId, file, _) = next
        this@GeminiNarrator.isPlaying = true
        
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnPreparedListener { 
                start()
                onStarted(utteranceId)
            }
            setOnCompletionListener {
                this@GeminiNarrator.isPlaying = false
                onFinished(utteranceId)
                playNext(onStarted, onFinished)
            }
            setOnErrorListener { _, _, _ ->
                this@GeminiNarrator.isPlaying = false
                onFinished(utteranceId)
                playNext(onStarted, onFinished)
                true
            }
            prepareAsync()
        }
    }
}
