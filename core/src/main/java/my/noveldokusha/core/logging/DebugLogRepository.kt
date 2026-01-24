package my.noveldokusha.core.logging

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque
import javax.inject.Inject
import javax.inject.Singleton

data class LogEntry(
    val timestamp: Long,
    val priority: Int,
    val tag: String?,
    val message: String,
    val throwable: Throwable?
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

@Singleton
class DebugLogRepository @Inject constructor() {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val logBuffer = ConcurrentLinkedDeque<LogEntry>()
    private val maxLogs = 1000

    fun addLog(entry: LogEntry) {
        logBuffer.addFirst(entry)
        if (logBuffer.size > maxLogs) {
            logBuffer.removeLast()
        }
        _logs.value = logBuffer.toList()
    }

    fun clear() {
        logBuffer.clear()
        _logs.value = emptyList()
    }
}

class DebugLogTree(private val repository: DebugLogRepository) : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        repository.addLog(LogEntry(System.currentTimeMillis(), priority, tag, message, t))
    }
}
