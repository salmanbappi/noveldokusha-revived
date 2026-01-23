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

/**
 * Hybrid Narrator: Prefers Microsoft Edge Neural TTS (Free, High Quality)
 * Falls back to Gemini 2.5 TTS if local python/edge-tts fails.
 */
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
        }
    )

    private var mediaPlayer: MediaPlayer? = null
    private val cacheDir = File(context.cacheDir, "hybrid_tts_v1").apply { deleteRecursively(); mkdirs() }
    
    private val audioQueue = ConcurrentLinkedQueue<Triple<String, File, String>>()
    private var isPlaying = false
    
    // Default to a high-quality Edge Voice
    private var currentEdgeVoice: String = "en-US-AvaNeural"
    private var currentMoodPrompt: String = "professional audiobook narrator"

    fun setMood(mood: String) {
        this.currentEdgeVoice = when(mood.lowercase()) {
            "bedtime" -> "en-US-EmmaNeural"
            "action" -> "en-US-AndrewNeural"
            "horror" -> "en-GB-RyanNeural"
            else -> "en-US-AvaNeural"
        }
        this.currentMoodPrompt = mood
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
        // TRY EDGE TTS FIRST (FREE & UNLIMITED)
        val edgeFile = fetchEdgeAudio(utteranceId, text)
        if (edgeFile != null) return@withContext edgeFile

        // FALLBACK TO GEMINI
        return@withContext fetchGeminiAudio(utteranceId, text)
    }

    private fun fetchEdgeAudio(utteranceId: String, text: String): File? {
        return try {
            val file = File(cacheDir, "edge_$utteranceId.mp3")
            // Use the bundled python script (Assumes python3 and edge-tts are available in the environment)
            // In a real Android app, this would be an API call or a JNI/Python-bridge call.
            // Since this is a Termux/CLI-managed project, we use the shell bridge.
            val scriptPath = "/data/data/com.termux/files/home/workspace/Noveldokusha/gemini-cli/skills/tts-auditor/scripts/edge_narrator.py"
            
            val process = ProcessBuilder(
                "python3", scriptPath,
                text, currentEdgeVoice, file.absolutePath
            ).start()
            
            val exitCode = process.waitFor()
            if (exitCode == 0 && file.exists()) file else null
        } catch (e: Exception) {
            Log.e("GeminiNarrator", "Edge TTS failed, falling back", e)
            null
        }
    }

    private suspend fun fetchGeminiAudio(utteranceId: String, text: String): File? {
        return try {
            val response = model.generateContent("Narrate: $text")
            val base64Data = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.let { part ->
                val partString = part.toString()
                if (partString.contains("data=")) {
                    partString.substringAfter("data=").substringBefore(",")
                } else null
            } ?: return null

            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val file = File(cacheDir, "gemini_$utteranceId.mp3")
            FileOutputStream(file).use { it.write(bytes) }
            file
        } catch (e: Exception) {
            Log.e("GeminiNarrator", "Gemini Fallback failed", e)
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