package my.noveldokusha.features.reader.ui.settingDialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import my.noveldoksuha.coreui.components.MySlider
import my.noveldoksuha.coreui.theme.ColorAccent
import my.noveldoksuha.coreui.theme.Themes
import my.noveldoksuha.coreui.theme.colorApp
import my.noveldokusha.features.reader.tools.FontsLoader
import my.noveldokusha.features.reader.ui.ReaderScreenState
import my.noveldokusha.reader.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StyleSettingDialog(
    state: ReaderScreenState.Settings.StyleSettingsData,
    onTextSizeChange: (Float) -> Unit,
    onTextFontChange: (String) -> Unit,
    onFollowSystemChange: (Boolean) -> Unit,
    onThemeChange: (Themes) -> Unit,
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
                text = stringResource(R.string.style),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            // Text Settings
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Typography",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Font Selector
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                    var showFontsDropdown by rememberSaveable { mutableStateOf(false) }
                    val fontLoader = remember { FontsLoader() }
                    var rowSize by remember { mutableStateOf(Size.Zero) }
                    
                    ListItem(
                        modifier = Modifier
                            .clickable { showFontsDropdown = !showFontsDropdown }
                            .onGloballyPositioned { rowSize = it.size.toSize() },
                        headlineContent = {
                            Text(
                                text = state.textFont.value,
                                fontFamily = fontLoader.getFontFamily(state.textFont.value),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingContent = { 
                            Icon(Icons.Filled.TextFields, null, tint = MaterialTheme.colorScheme.secondary) 
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                    DropdownMenu(
                        expanded = showFontsDropdown,
                        onDismissRequest = { showFontsDropdown = false },
                        offset = DpOffset(0.dp, 0.dp),
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .width(with(LocalDensity.current) { rowSize.width.toDp() })
                    ) {
                        FontsLoader.availableFonts.forEach { item ->
                            DropdownMenuItem(
                                onClick = { 
                                    onTextFontChange(item)
                                    showFontsDropdown = false
                                },
                                text = {
                                    Text(
                                        text = item,
                                        fontFamily = fontLoader.getFontFamily(item),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        }
                    }
                }

                // Text Size Slider
                var currentTextSize by remember { mutableFloatStateOf(state.textSize.value) }
                MySlider(
                    value = currentTextSize,
                    valueRange = 8f..40f,
                    onValueChange = {
                        currentTextSize = it
                        onTextSizeChange(currentTextSize)
                    },
                    text = stringResource(R.string.text_size) + ": %.1f".format(currentTextSize),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Theme Settings
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Follow System Toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.follow_system),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = state.followSystem.value,
                            onCheckedChange = onFollowSystemChange,
                            modifier = Modifier.heightIn(max = 32.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ColorAccent,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                if (!state.followSystem.value) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Themes.entries.forEach { theme ->
                            FilterChip(
                                selected = theme == state.currentTheme.value,
                                onClick = { onThemeChange(theme) },
                                label = { Text(text = stringResource(id = theme.nameId)) },
                                leadingIcon = {
                                    if (theme == state.currentTheme.value) {
                                        Icon(Icons.Outlined.ColorLens, null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}