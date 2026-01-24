package my.noveldokusha.text_to_speech

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class GeminiTextToSpeechManager<T : Utterance<T>>(
    context: Context,
    initialItemState: T,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val narrator = OnlineNarrator(context)
    
    private val _queueList = mutableMapOf<String, T>()
    private val _currentTextSpeakFlow = MutableSharedFlow<T>()
    
    val availableVoices = mutableStateListOf<VoiceData>()
    val voiceSpeed = mutableFloatStateOf(1f)
    val voicePitch = mutableFloatStateOf(1f)
    val activeVoice = mutableStateOf<VoiceData?>(null)
    val serviceLoadedFlow = MutableSharedFlow<Unit>(replay = 1)

    val queueList = _queueList as Map<String, T>
    val currentTextSpeakFlow = _currentTextSpeakFlow.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed()
    )

    val currentActiveItemState = mutableStateOf(initialItemState)

    init {
        availableVoices.addAll(listOf(
            VoiceData("default", "Online: Default", true, 1000)
        ))
        activeVoice.value = availableVoices.first()
        scope.launch { serviceLoadedFlow.emit(Unit) }
    }

    fun stop() {
        narrator.stop()
        _queueList.clear()
    }

    fun speak(text: String, textSynthesis: T) {
        _queueList[textSynthesis.utteranceId] = textSynthesis
        
        narrator.enqueue(
            utteranceId = textSynthesis.utteranceId,
            text = text,
            onStarted = { id ->
                scope.launch {
                    val item = _queueList[id] ?: return@launch
                    val playingItem = item.copyWithState(Utterance.PlayState.PLAYING)
                    currentActiveItemState.value = playingItem
                    _currentTextSpeakFlow.emit(playingItem)
                }
            },
            onFinished = { id ->
                scope.launch {
                    val item = _queueList[id] ?: return@launch
                    val finishedItem = item.copyWithState(Utterance.PlayState.FINISHED)
                    _queueList.remove(id)
                    currentActiveItemState.value = finishedItem
                    _currentTextSpeakFlow.emit(finishedItem)
                }
            }
        )
    }

    fun setCurrentSpeakState(textSynthesis: T) {
        currentActiveItemState.value = textSynthesis
        scope.launch { _currentTextSpeakFlow.emit(textSynthesis) }
    }

    fun trySetVoiceById(id: String): Boolean {
        narrator.setMood(id)
        val voice = availableVoices.find { it.id == id } ?: return false
        activeVoice.value = voice
        return true
    }

    fun trySetVoicePitch(value: Float): Boolean = true
    fun trySetVoiceSpeed(value: Float): Boolean = true
    
    fun shutdown() {
        stop()
        narrator.shutdown()
    }
}