package my.noveldokusha.text_to_speech

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

// Wrapper that combines System TTS (Offline) and Edge TTS (Online)
class GeminiTextToSpeechManager<T : Utterance<T>>(
    context: Context,
    initialItemState: T
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val systemTts = TextToSpeechManager(context, initialItemState)
    private val narrator = OnlineNarrator(context)
    
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
        
        // 1. Add System Voices (Offline capable)
        availableVoices.addAll(systemTts.availableVoices.map { 
            it.copy(id = "sys:${it.id}") // Prefix to distinguish
        })
        
        // 2. Add Edge TTS Voices (Online)
        availableVoices.addAll(EdgeTts.getVoices().map {
            it.copy(id = "edge:${it.id}")
        })
    }
    
    fun speak(text: String, textSynthesis: T) {
        val voiceId = activeVoice.value?.id ?: ""
        
        if (voiceId.startsWith("edge:")) {
            // Use Online Narrator
            val realId = voiceId.removePrefix("edge:")
            narrator.speak(text, realId, voiceSpeed.floatValue, voicePitch.floatValue)
            // Manually update state since OnlineNarrator doesn't have the same granular callbacks yet
            // This is a simplification
            systemTts.setCurrentSpeakState(textSynthesis.copyWithState(Utterance.PlayState.PLAYING))
        } else {
            // Use System TTS
            // If ID starts with "sys:", strip it, otherwise let it be (default)
            val realId = voiceId.removePrefix("sys:")
            if (realId.isNotEmpty() && realId != voiceId) {
                // We don't need to set it here if trySetVoiceById was called, but to be safe
                systemTts.trySetVoiceById(realId)
            }
            systemTts.speak(text, textSynthesis)
        }
    }
    
    fun stop() {
        systemTts.stop()
        narrator.stop()
    }

    fun shutdown() {
        stop()
        narrator.release()
    }
    
    fun trySetVoiceById(id: String): Boolean {
        val voice = availableVoices.find { it.id == id } ?: return false
        activeVoice.value = voice
        
        if (id.startsWith("sys:")) {
            return systemTts.trySetVoiceById(id.removePrefix("sys:"))
        }
        // For Edge TTS, we just set the active voice property, success is implied if found
        return true
    }
    
    fun setCurrentSpeakState(textSynthesis: T) {
        systemTts.setCurrentSpeakState(textSynthesis)
    }
    
    // Delegate other methods
    fun trySetVoiceSpeed(value: Float) = systemTts.trySetVoiceSpeed(value)
    fun trySetVoicePitch(value: Float) = systemTts.trySetVoicePitch(value)
}