package my.noveldokusha.features.reader.ui.settingDialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.withContext
import my.noveldoksuha.coreui.components.MyOutlinedTextField
import my.noveldoksuha.coreui.components.MySlider
import my.noveldoksuha.coreui.composableActions.debouncedAction
import my.noveldoksuha.coreui.theme.InternalTheme
import my.noveldoksuha.coreui.theme.colorApp
import my.noveldoksuha.coreui.theme.rememberMutableStateOf
import my.noveldokusha.core.appPreferences.VoicePredefineState
import my.noveldokusha.features.reader.features.TextToSpeechSettingData
import my.noveldokusha.reader.R
import my.noveldokusha.text_to_speech.VoiceData


@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VoiceReaderSettingDialog(
    state: TextToSpeechSettingData
) {
    var openVoicesDialog by rememberSaveable { mutableStateOf(false) }
    var showSavedVoices by rememberSaveable { mutableStateOf(false) }

    Column {
        AnimatedVisibility(visible = state.isLoadingChapter.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorApp.tintedSurface
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Text(
                    text = stringResource(R.string.voice_reader_settings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Player voice parameters
                AnimatedVisibility(visible = !state.isPlaying.value) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MySlider(
                            value = state.voicePitch.value,
                            valueRange = 0.1f..5f,
                            onValueChange = state.setVoicePitch,
                            text = stringResource(R.string.voice_pitch) + ": %.2f".format(state.voicePitch.value),
                        )
                        MySlider(
                            value = state.voiceSpeed.value,
                            valueRange = 0.1f..5f,
                            onValueChange = state.setVoiceSpeed,
                            text = stringResource(R.string.voice_speed) + ": %.2f".format(state.voiceSpeed.value),
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }

                // Player settings buttons
                AnimatedVisibility(visible = !state.isPlaying.value) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Presets & Voices",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            if (state.customSavedVoices.value.isNotEmpty()) {
                                Text(
                                    text = "${state.customSavedVoices.value.size} Saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Presets
                            InputChip(
                                selected = state.voiceSpeed.value == 0.8f && state.voicePitch.value == 0.9f,
                                onClick = { 
                                    state.setVoiceSpeed(0.8f)
                                    state.setVoicePitch(0.9f)
                                },
                                label = { Text("Calm") },
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                            InputChip(
                                selected = state.voiceSpeed.value == 1.0f && state.voicePitch.value == 1.0f,
                                onClick = { 
                                    state.setVoiceSpeed(1.0f)
                                    state.setVoicePitch(1.0f)
                                },
                                label = { Text("Natural") },
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                            InputChip(
                                selected = state.voiceSpeed.value == 1.5f && state.voicePitch.value == 1.1f,
                                onClick = { 
                                    state.setVoiceSpeed(1.5f)
                                    state.setVoicePitch(1.1f)
                                },
                                label = { Text("Fast") },
                                border = null,
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            AssistChip(
                                label = { Text(text = stringResource(id = R.string.start_here)) },
                                onClick = debouncedAction { state.playFirstVisibleItem() },
                                leadingIcon = { Icon(Icons.Filled.CenterFocusWeak, null, modifier = Modifier.size(18.dp)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = null
                            )
                            AssistChip(
                                label = { Text(text = stringResource(id = R.string.focus)) },
                                onClick = debouncedAction { state.scrollToActiveItem() },
                                leadingIcon = { Icon(Icons.Filled.CenterFocusStrong, null, modifier = Modifier.size(18.dp)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = null
                            )
                            AssistChip(
                                label = { Text(text = "Voices") },
                                onClick = { openVoicesDialog = true },
                                leadingIcon = { Icon(Icons.Filled.RecordVoiceOver, null, modifier = Modifier.size(18.dp)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = null
                            )
                            AssistChip(
                                label = { Text(text = stringResource(R.string.saved)) },
                                onClick = { showSavedVoices = !showSavedVoices },
                                leadingIcon = { Icon(Icons.Filled.Bookmarks, null, modifier = Modifier.size(18.dp)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (showSavedVoices) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                                    labelColor = if (showSavedVoices) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                border = null
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = showSavedVoices) {
                    SavedVoicesSection(
                        list = state.customSavedVoices.value,
                        currentVoice = state.activeVoice.value,
                        currentVoiceSpeed = state.voiceSpeed.value,
                        currentVoicePitch = state.voicePitch.value,
                        onPredefinedSelected = {
                            state.setVoiceSpeed(it.speed)
                            state.setVoicePitch(it.pitch)
                            state.setVoiceId(it.voiceId)
                        },
                        setCustomSavedVoices = state.setCustomSavedVoices
                    )
                }

                VoiceSelectorDialog(
                    availableVoices = state.availableVoices,
                    currentVoice = state.activeVoice.value,
                    inputTextFilter = rememberSaveable { mutableStateOf("") },
                    setVoice = state.setVoiceId,
                    isDialogOpen = openVoicesDialog,
                    setDialogOpen = { openVoicesDialog = it }
                )

                Spacer(modifier = Modifier.heightIn(8.dp))

                // Player playback buttons
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        val alpha by animateFloatAsState(
                            targetValue = if (state.isThereActiveItem.value) 1f else 0.5f,
                            label = ""
                        )
                        IconButton(
                            onClick = debouncedAction(waitMillis = 1000) { state.playPreviousChapter() },
                            enabled = state.isThereActiveItem.value,
                            modifier = Modifier.alpha(alpha),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FastRewind,
                                contentDescription = "Previous Chapter",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = debouncedAction(waitMillis = 100) { state.playPreviousItem() },
                            enabled = state.isThereActiveItem.value,
                            modifier = Modifier.alpha(alpha),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                                contentDescription = "Previous Sentence",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Play/Pause Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { state.setPlaying(!state.isPlaying.value) }
                        ) {
                            AnimatedContent(
                                targetState = state.isPlaying.value,
                                label = ""
                            ) { target ->
                                Icon(
                                    imageVector = if (target) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (target) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = debouncedAction(waitMillis = 100) { state.playNextItem() },
                            enabled = state.isThereActiveItem.value,
                            modifier = Modifier.alpha(alpha),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.NavigateNext,
                                contentDescription = "Next Sentence",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = debouncedAction(waitMillis = 1000) { state.playNextChapter() },
                            enabled = state.isThereActiveItem.value,
                            modifier = Modifier.alpha(alpha),
                        ) {
                            Icon(
                                Icons.Rounded.FastForward,
                                contentDescription = "Next Chapter",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedVoicesSection(
    list: List<VoicePredefineState>,
    currentVoice: VoiceData?,
    currentVoiceSpeed: Float,
    currentVoicePitch: Float,
    onPredefinedSelected: (VoicePredefineState) -> Unit,
    setCustomSavedVoices: (List<VoicePredefineState>) -> Unit,
) {
    var expandedAddNextEntry by rememberMutableStateOf(false)
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                IconButton(
                    onClick = { expandedAddNextEntry = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        null, 
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            items(list) { voice ->
                var showDeleteDialog by remember { mutableStateOf(false) }
                
                InputChip(
                    selected = false,
                    onClick = { onPredefinedSelected(voice) },
                    label = { 
                        Text(
                            text = voice.savedName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            null,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { showDeleteDialog = true }
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
                
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text(text = stringResource(R.string.delete_voice)) },
                        text = { Text("Are you sure you want to delete '${voice.savedName}'?") },
                        confirmButton = {
                            FilledTonalButton(
                                onClick = {
                                    showDeleteDialog = false
                                    setCustomSavedVoices(list.filter { it != voice })
                                }
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDeleteDialog = false }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }
        }
        
        if (list.isEmpty()) {
            Text(
                text = "No voices saved yet. Click + to save current settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }
    }

    if (expandedAddNextEntry) {
        var name by rememberMutableStateOf(value = "")
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            delay(300)
            focusRequester.requestFocus()
        }
        AlertDialog(
            tonalElevation = 6.dp,
            onDismissRequest = { expandedAddNextEntry = false },
            title = { 
                Text(
                    text = "Save Configuration", 
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter a name for the current voice settings (Pitch: %.2f, Speed: %.2f)".format(currentVoicePitch, currentVoiceSpeed),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    MyOutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeHolderText = "e.g. Bedtime Reading",
                        modifier = Modifier.focusRequester(focusRequester).fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    enabled = name.isNotBlank(),
                    onClick = onClick@{
                        val voice = currentVoice ?: return@onClick
                        val state = VoicePredefineState(
                            savedName = name,
                            voiceId = voice.id,
                            speed = currentVoiceSpeed,
                            pitch = currentVoicePitch
                        )
                        setCustomSavedVoices(list + state)
                        expandedAddNextEntry = false
                    }
                ) {
                    Text(text = stringResource(R.string.save_voice))
                }
            },
            dismissButton = {
                Button(onClick = { expandedAddNextEntry = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
private fun VoiceSelectorDialog(
    availableVoices: List<VoiceData>,
    currentVoice: VoiceData?,
    inputTextFilter: MutableState<String>,
    setVoice: (voiceId: String) -> Unit,
    isDialogOpen: Boolean,
    setDialogOpen: (Boolean) -> Unit,
) {
    val voicesSorted = remember { mutableStateListOf<VoiceData>() }
    LaunchedEffect(availableVoices) {
        withContext(Dispatchers.Default) {
            availableVoices.sortedWith(
                compareBy<VoiceData> { it.language }
                    .thenByDescending { it.quality }
                    .thenBy { it.needsInternet }
            )
        }.let { voicesSorted.addAll(it) }
    }

    val voicesFiltered = remember {
        mutableStateListOf<VoiceData>().apply { addAll(availableVoices) }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { inputTextFilter.value }
            .debounce(200)
            .collectLatest {
                val items = withContext(Dispatchers.Default) {
                    if (inputTextFilter.value.isEmpty()) {
                        voicesSorted
                    } else {
                        voicesSorted.filter { voice ->
                            voice.language.contains(it, ignoreCase = true)
                        }
                    }
                }
                voicesFiltered.clear()
                voicesFiltered.addAll(items)
            }
    }

    val listState = rememberLazyListState()

    val inputFocusRequester = remember { FocusRequester() }

    if (isDialogOpen) Dialog(
        onDismissRequest = { setDialogOpen(false) }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .shadow(16.dp, MaterialTheme.shapes.extraLarge),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                stickyHeader {
                    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Select Voice",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { setDialogOpen(false) }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            MyOutlinedTextField(
                                value = inputTextFilter.value,
                                onValueChange = { inputTextFilter.value = it },
                                placeHolderText = stringResource(R.string.search_voice_by_language),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(inputFocusRequester)
                            )
                        }
                    }
                    LaunchedEffect(Unit) {
                        delay(300)
                        inputFocusRequester.requestFocus()
                    }
                }


                if (voicesFiltered.isEmpty()) item {
                    Text(
                        text = stringResource(R.string.no_matches),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    )
                }

                items(voicesFiltered) { voice ->
                    val selected by remember { derivedStateOf { voice.id == currentVoice?.id } }
                    ListItem(
                        modifier = Modifier
                            .clickable(enabled = !selected) {
                                setVoice(voice.id)
                            },
                        headlineContent = {
                            Text(
                                text = voice.language,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for (star in 0..4) {
                                    val yay = voice.quality > star * 100
                                    Icon(
                                        imageVector = if (yay) Icons.Filled.StarRate else Icons.Outlined.StarBorder,
                                        contentDescription = null,
                                        tint = if (yay) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                if (voice.needsInternet) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Online",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            if (selected) {
                                Icon(
                                    Icons.Default.RecordVoiceOver, 
                                    null, 
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = voice.id.takeLast(8),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Preview(group = "dialog")
@Composable
private fun VoiceSelectorDialogContentPreview() {
    InternalTheme {
        VoiceSelectorDialog(
            availableVoices = (0..7).map {
                VoiceData(
                    id = "$it",
                    language = "English",
                    needsInternet = (it % 2) == 0,
                    quality = (it * 100) % 501
                )
            },
            setVoice = {},
            inputTextFilter = remember { mutableStateOf("") },
            currentVoice = VoiceData(
                id = "2",
                language = "English",
                needsInternet = false,
                quality = 100
            ),
            setDialogOpen = {},
            isDialogOpen = true
        )
    }
}
