package my.noveldokusha.text_to_speech

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.LinkedBlockingQueue

class OnlineNarrator(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null
    
    // Use a standard property with private setter
    var isPlaying = false
        private set

    private val audioQueue = LinkedBlockingQueue<String>()
    private var mediaPlayer: MediaPlayer? = null

    fun speak(text: String, voiceId: String, speed: Float, pitch: Float) {
        scope.launch {
            try {
                Log.d("OnlineNarrator", "Synthesizing: $text")
                // Format pitch and rate for EdgeTTS
                // Rate: +50% or -50%
                val ratePercent = ((speed - 1.0f) * 100).toInt()
                val rateStr = if (ratePercent >= 0) "+$ratePercent%" else "$ratePercent%"
                
                // Pitch: +50Hz or -50Hz (simplified)
                val pitchHz = ((pitch - 1.0f) * 50).toInt()
                val pitchStr = if (pitchHz >= 0) "+${pitchHz}Hz" else "${pitchHz}Hz"

                val audioData = EdgeTts.synthesize(text, voiceId, rateStr, pitchStr)
                
                if (audioData.isNotEmpty()) {
                    val file = File(context.cacheDir, "tts_${System.currentTimeMillis()}.mp3")
                    FileOutputStream(file).use { it.write(audioData) }
                    
                    audioQueue.offer(file.absolutePath)
                    
                    withContext(Dispatchers.Main) {
                        playNext()
                    }
                }
            } catch (e: Exception) {
                Log.e("OnlineNarrator", "Error synthesizing text", e)
            }
        }
    }

    private fun playNext() {
        if (isPlaying || audioQueue.isEmpty()) return
        
        val path = audioQueue.poll() ?: return
        isPlaying = true
        
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    // This explicitly sets the property of the class instance
                    this@OnlineNarrator.isPlaying = false
                    it.release()
                    // Delete temp file
                    try { File(path).delete() } catch(e: Exception) {}
                    playNext()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("OnlineNarrator", "Error playing audio", e)
            isPlaying = false
            playNext()
        }
    }

    fun stop() {
        isPlaying = false
        audioQueue.clear()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // Ignore
        }
        job?.cancel()
    }

    fun release() {
        stop()
        scope.cancel()
    }
}