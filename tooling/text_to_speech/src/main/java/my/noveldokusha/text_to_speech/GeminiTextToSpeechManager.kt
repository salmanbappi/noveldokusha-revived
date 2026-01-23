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
import my.noveldokusha.core.SecretConfig

class GeminiTextToSpeechManager<T : Utterance<T>>(
    context: Context,
    initialItemState: T,
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val narrator = GeminiNarrator(context, SecretConfig.GEMINI_API_KEY)
    
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
        // Initialize with a mock voice for Gemini
        availableVoices.add(VoiceData("gemini-voice", "AI Narrator", true, 500))
        activeVoice.value = availableVoices.first()
        scope.launch { serviceLoadedFlow.emit(Unit) }
    }

    fun stop() {
        narrator.stop()
        _queueList.clear()
    }

    fun speak(text: String, textSynthesis: T) {
        _queueList[textSynthesis.utteranceId] = textSynthesis
        
        scope.launch {
            narrator.enqueue(
                utteranceId = textSynthesis.utteranceId,
                text = text,
                onStarted = { id ->
                    val item = _queueList[id] ?: return@enqueue
                    val playingItem = item.copyWithState(Utterance.PlayState.PLAYING)
                    currentActiveItemState.value = playingItem
                    scope.launch { _currentTextSpeakFlow.emit(playingItem) }
                },
                onFinished = { id ->
                    val item = _queueList[id] ?: return@enqueue
                    val finishedItem = item.copyWithState(Utterance.PlayState.FINISHED)
                    _queueList.remove(id)
                    currentActiveItemState.value = finishedItem
                    scope.launch { _currentTextSpeakFlow.emit(finishedItem) }
                }
            )
        }
    }

    fun setCurrentSpeakState(textSynthesis: T) {
        currentActiveItemState.value = textSynthesis
        scope.launch { _currentTextSpeakFlow.emit(textSynthesis) }
    }

    fun trySetVoiceById(id: String): Boolean = true
    fun trySetVoicePitch(value: Float): Boolean = true
    fun trySetVoiceSpeed(value: Float): Boolean = true
    
    fun shutdown() {
        stop()
    }
}
