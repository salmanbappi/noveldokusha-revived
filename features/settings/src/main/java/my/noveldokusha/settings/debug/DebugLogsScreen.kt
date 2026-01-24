package my.noveldokusha.settings.debug

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import my.noveldokusha.core.logging.LogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogsScreen(
    onBack: () -> Unit
) {
    val viewModel: DebugLogsViewModel = viewModel()
    val logs by viewModel.logs.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.scrollToItem(0) // Scroll to top (newest)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Text("Clear")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(logs) { log ->
                LogItem(log)
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun LogItem(log: LogEntry) {
    val color = when (log.priority) {
        Log.ERROR -> Color.Red
        Log.WARN -> Color.Yellow
        Log.INFO -> Color.Green
        Log.DEBUG -> Color.Cyan
        else -> Color.White
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "${log.formattedTime} ${log.tag ?: ""}",
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = log.message,
            color = color,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        if (log.throwable != null) {
            Text(
                text = Log.getStackTraceString(log.throwable),
                color = Color.Red,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
