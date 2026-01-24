package my.noveldokusha.features.reader.ui

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import my.noveldoksuha.coreui.theme.colorApp
import kotlin.math.max
import kotlin.math.min

private const val EDGE_ZONE_PERCENTAGE = 0.15f
private const val SENSITIVITY = 0.005f

@Composable
fun ReaderGesturesOverlay(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!isEnabled) {
        content()
        return
    }

    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    // State for UI Feedback
    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackIcon by remember { mutableStateOf(Icons.Default.BrightnessMedium) }
    var feedbackText by remember { mutableStateOf("") }
    
    // Auto-hide feedback
    LaunchedEffect(feedbackVisible, feedbackText) {
        if (feedbackVisible) {
            delay(1500)
            feedbackVisible = false
        }
    }

    // Helper to change brightness
    fun changeBrightness(dragAmount: Float) {
        activity?.let { act ->
            val layoutParams = act.window.attributes
            var currentBrightness = layoutParams.screenBrightness
            if (currentBrightness < 0) {
                try {
                    val sysBrightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                    currentBrightness = sysBrightness / 255f
                } catch (e: Exception) {
                    currentBrightness = 0.5f
                }
            }
            
            // Invert dragAmount because dragging UP (negative Y) should INCREASE brightness
            val newBrightness = (currentBrightness - (dragAmount * SENSITIVITY)).coerceIn(0.01f, 1f)
            
            layoutParams.screenBrightness = newBrightness
            act.window.attributes = layoutParams

            feedbackIcon = Icons.Default.BrightnessMedium
            feedbackText = "${(newBrightness * 100).toInt()}%"
            feedbackVisible = true
        }
    }

    // Helper to change volume
    fun changeVolume(dragAmount: Float) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        
        // Dragging UP (negative Y) increases volume
        // We accumulate a bit of drag to avoid jumpy volume changes since steps are integers
        // A simple approach is using the same logic but mapping to the integer range
        
        // This simple heuristic adds/subtracts 1 based on threshold
        if (Math.abs(dragAmount) > 2) {
             val direction = if (dragAmount < 0) 1 else -1
             val newVolume = (currentVolume + direction).coerceIn(0, maxVolume)
             if (newVolume != currentVolume) {
                 audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                 feedbackIcon = Icons.Default.VolumeUp
                 feedbackText = "$newVolume / $maxVolume"
                 feedbackVisible = true
             }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = maxWidth
        val zoneWidth = width * EDGE_ZONE_PERCENTAGE

        // The actual content
        content()

        // Overlay for Left Zone (Brightness)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(zoneWidth)
                .align(Alignment.CenterStart)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        changeBrightness(dragAmount)
                    }
                }
        )

        // Overlay for Right Zone (Volume)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(zoneWidth)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        changeVolume(dragAmount)
                    }
                }
        )

        // Feedback HUD
        AnimatedVisibility(
            visible = feedbackVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            val isBrightness = feedbackIcon == Icons.Default.BrightnessMedium
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(if (isBrightness) Alignment.CenterStart else Alignment.CenterEnd)
                        .padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(vertical = 20.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = feedbackIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = feedbackText,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
