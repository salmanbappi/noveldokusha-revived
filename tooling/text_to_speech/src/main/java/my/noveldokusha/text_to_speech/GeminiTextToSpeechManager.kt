package my.noveldokusha.text_to_speech

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

// Wrapper for System TTS (Offline)
class GeminiTextToSpeechManager<T : Utterance<T>>(
    context: Context,
    initialItemState: T
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val systemTts = TextToSpeechManager(context, initialItemState)
    
    val availableVoices = mutableStateListOf<VoiceData>()
    val activeVoice = systemTts.activeVoice
    val voiceSpeed = systemTts.voiceSpeed
    val voicePitch = systemTts.voicePitch
    
    // API Compatibility: Expose underlying flows and properties
    val currentTextSpeakFlow = systemTts.currentTextSpeakFlow
    val currentActiveItemState = systemTts.currentActiveItemState
    val serviceLoadedFlow = systemTts.serviceLoadedFlow
    val queueList get() = systemTts.queueList
    
    init {
        scope.launch {
            systemTts.serviceLoadedFlow.collect {
                refreshVoices()
            }
        }
    }
    
    private fun refreshVoices() {
        availableVoices.clear()
        
        // Add System Voices
        availableVoices.addAll(systemTts.availableVoices)
    }
    
    fun speak(text: String, textSynthesis: T) {
        systemTts.speak(text, textSynthesis)
    }
    
    fun stop() {
        systemTts.stop()
    }

    fun shutdown() {
        stop()
        systemTts.service.shutdown() // Fixed: calling shutdown on the underlying service
    }
    
    fun trySetVoiceById(id: String): Boolean {
        return systemTts.trySetVoiceById(id)
    }
    
    fun setCurrentSpeakState(textSynthesis: T) {
        systemTts.setCurrentSpeakState(textSynthesis)
    }
    
    // Delegate other methods
    fun trySetVoiceSpeed(value: Float) = systemTts.trySetVoiceSpeed(value)
    fun trySetVoicePitch(value: Float) = systemTts.trySetVoicePitch(value)
}