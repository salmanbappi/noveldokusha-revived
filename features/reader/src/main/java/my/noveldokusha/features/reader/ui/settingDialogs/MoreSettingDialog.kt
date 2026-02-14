package my.noveldokusha.features.reader.ui.settingDialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.theme.ColorAccent
import my.noveldoksuha.coreui.theme.colorApp
import my.noveldokusha.reader.R

@Composable
internal fun MoreSettingDialog(
    readingTimer: String,
    allowTextSelection: Boolean,
    onAllowTextSelectionChange: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOn: (Boolean) -> Unit,
    fullScreen: Boolean,
    onFullScreen: (Boolean) -> Unit,
    autoScrollSpeed: Int,
    onAutoScrollChange: (Int) -> Unit,
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorApp.tintedSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = stringResource(R.string.more),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            // Timer
            ListItem(
                headlineContent = { Text("Reading Timer", style = MaterialTheme.typography.bodyLarge) },
                leadingContent = { 
                    Icon(Icons.Outlined.Timer, null, tint = MaterialTheme.colorScheme.primary) 
                },
                trailingContent = {
                    Text(
                        text = readingTimer,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    headlineColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Toggles
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Auto Scroll
                ListItem(
                    modifier = Modifier.clickable { onAutoScrollChange(if (autoScrollSpeed > 0) 0 else 5) },
                    headlineContent = { Text("Auto Scroll") },
                    leadingContent = { 
                        Icon(Icons.Default.ArrowDownward, null, tint = MaterialTheme.colorScheme.secondary) 
                    },
                    trailingContent = {
                        Switch(
                            checked = autoScrollSpeed > 0,
                            onCheckedChange = { onAutoScrollChange(if (it) 5 else 0) },
                            colors = SwitchDefaults.colors(checkedThumbColor = ColorAccent)
                        )
                    }
                )

                // Text Selection
                ListItem(
                    modifier = Modifier.clickable { onAllowTextSelectionChange(!allowTextSelection) },
                    headlineContent = { Text(stringResource(id = R.string.allow_text_selection)) },
                    leadingContent = { 
                        Icon(Icons.Outlined.TouchApp, null, tint = MaterialTheme.colorScheme.secondary) 
                    },
                    trailingContent = {
                        Switch(
                            checked = allowTextSelection,
                            onCheckedChange = onAllowTextSelectionChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = ColorAccent)
                        )
                    }
                )

                // Keep Screen On
                ListItem(
                    modifier = Modifier.clickable { onKeepScreenOn(!keepScreenOn) },
                    headlineContent = { Text(stringResource(R.string.keep_screen_on)) },
                    leadingContent = { 
                        Icon(Icons.Outlined.LightMode, null, tint = MaterialTheme.colorScheme.secondary) 
                    },
                    trailingContent = {
                        Switch(
                            checked = keepScreenOn,
                            onCheckedChange = onKeepScreenOn,
                            colors = SwitchDefaults.colors(checkedThumbColor = ColorAccent)
                        )
                    }
                )

                // Full Screen
                ListItem(
                    modifier = Modifier.clickable { onFullScreen(!fullScreen) },
                    headlineContent = { Text(stringResource(R.string.features_reader_full_screen)) },
                    leadingContent = { 
                        Icon(Icons.Outlined.Fullscreen, null, tint = MaterialTheme.colorScheme.secondary) 
                    },
                    trailingContent = {
                        Switch(
                            checked = fullScreen,
                            onCheckedChange = onFullScreen,
                            colors = SwitchDefaults.colors(checkedThumbColor = ColorAccent)
                        )
                    }
                )
            }
        }
    }
}
