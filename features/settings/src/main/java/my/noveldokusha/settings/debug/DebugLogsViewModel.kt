package my.noveldokusha.settings.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import my.noveldokusha.core.logging.DebugLogRepository
import my.noveldokusha.core.logging.LogEntry
import javax.inject.Inject

@HiltViewModel
class DebugLogsViewModel @Inject constructor(
    private val debugLogRepository: DebugLogRepository
) : ViewModel() {

    val logs: StateFlow<List<LogEntry>> = debugLogRepository.logs

    fun clearLogs() {
        viewModelScope.launch {
            debugLogRepository.clear()
        }
    }
}
