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
    
    // Combine flows? For now, we mainly rely on System TTS for state tracking
    val currentTextSpeakFlow = systemTts.currentTextSpeakFlow
    val currentActiveItemState = systemTts.currentActiveItemState
    
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
                systemTts.trySetVoiceById(realId)
            }
            systemTts.speak(text, textSynthesis)
        }
    }
    
    fun stop() {
        systemTts.stop()
        narrator.stop()
    }
    
    fun setVoiceId(id: String) {
        val voice = availableVoices.find { it.id == id } ?: return
        activeVoice.value = voice
        
        if (id.startsWith("sys:")) {
            systemTts.trySetVoiceById(id.removePrefix("sys:"))
        }
    }
    
    // Delegate other methods
    fun trySetVoiceSpeed(value: Float) = systemTts.trySetVoiceSpeed(value)
    fun trySetVoicePitch(value: Float) = systemTts.trySetVoicePitch(value)
}
