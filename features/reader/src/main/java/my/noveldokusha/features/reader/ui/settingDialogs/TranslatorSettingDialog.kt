package my.noveldokusha.features.reader.ui.settingDialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import my.noveldoksuha.coreui.theme.clickableWithUnboundedIndicator
import my.noveldoksuha.coreui.theme.colorApp
import my.noveldoksuha.coreui.theme.ifCase
import my.noveldokusha.features.reader.features.LiveTranslationSettingData
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import my.noveldoksuha.coreui.theme.InternalTheme
import my.noveldokusha.features.reader.ui.ReaderScreenState
import my.noveldokusha.strings.R
import my.noveldokusha.text_translator.domain.TranslationModelState

@Composable
internal fun TranslatorSettingDialog(
    state: LiveTranslationSettingData
) {
    var modelSelectorExpanded by rememberSaveable { mutableStateOf(false) }
    var modelSelectorExpandedForTarget by rememberSaveable { mutableStateOf(false) }
    var rowSize by remember { mutableStateOf(Size.Zero) }
    
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
            // Header with Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Translate, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp).size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.live_translation),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                FilterChip(
                    selected = state.enable.value,
                    label = { 
                        Text(if (state.enable.value) "On" else "Off") 
                    },
                    onClick = { state.onEnable(!state.enable.value) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            // Language Selection Area
            AnimatedVisibility(
                visible = state.enable.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { layoutCoordinates ->
                                rowSize = layoutCoordinates.size.toSize()
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .padding(vertical = 8.dp)
                        ) {
                            // Source Language
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickableWithUnboundedIndicator {
                                        modelSelectorExpanded = !modelSelectorExpanded
                                        modelSelectorExpandedForTarget = false
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.source.value?.locale?.displayLanguage
                                        ?: stringResource(R.string.language_source_empty_text),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.ifCase(state.source.value == null) { alpha(0.5f) },
                                )
                            }

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowRightAlt, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Target Language
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickableWithUnboundedIndicator {
                                        modelSelectorExpanded = !modelSelectorExpanded
                                        modelSelectorExpandedForTarget = true
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.target.value?.locale?.displayLanguage
                                        ?: stringResource(R.string.language_target_empty_text),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.ifCase(state.target.value == null) { alpha(0.5f) },
                                )
                            }
                        }

                        // Dropdown Menu
                        DropdownMenu(
                            expanded = modelSelectorExpanded,
                            onDismissRequest = { modelSelectorExpanded = false },
                            offset = DpOffset(0.dp, 0.dp),
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .width(with(LocalDensity.current) { rowSize.width.toDp() })
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    if (modelSelectorExpandedForTarget) state.onTargetChange(null)
                                    else state.onSourceChange(null)
                                    modelSelectorExpanded = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.language_clear_selection),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )

                            HorizontalDivider()

                            state.listOfAvailableModels.forEach { item ->
                                DropdownMenuItem(
                                    onClick = {
                                        if (modelSelectorExpandedForTarget) state.onTargetChange(item)
                                        else state.onSourceChange(item)
                                        modelSelectorExpanded = false
                                    },
                                    enabled = item.available,
                                    trailingIcon = {
                                        when {
                                            item.downloadingFailed -> IconButton(
                                                onClick = { state.onDownloadTranslationModel(item.language) },
                                            ) {
                                                Icon(
                                                    Icons.Outlined.CloudDownload,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                            item.downloading -> IconButton(
                                                onClick = { },
                                                enabled = false
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            item.available -> IconButton(
                                                onClick = { state.onDownloadTranslationModel(item.language) },
                                            ) {
                                                Icon(Icons.Filled.CloudDownload, null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                            else -> IconButton(
                                                onClick = { state.onDownloadTranslationModel(item.language) },
                                            ) {
                                                Icon(Icons.Outlined.CloudDownload, null)
                                            }
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = item.locale.displayLanguage,
                                            fontWeight = if (item.available) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
