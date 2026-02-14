package my.noveldokusha.features.reader.ui.settingDialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import my.noveldoksuha.coreui.components.MySlider
import my.noveldoksuha.coreui.theme.Themes
import my.noveldoksuha.coreui.theme.collectAsStateInitial
import my.noveldoksuha.coreui.theme.colorApp
import my.noveldokusha.core.appPreferences.AppPreferences
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
    appPreferences: AppPreferences
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorApp.tintedSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with more expressive typography
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.style),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Default.FormatPaint,
                    null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            // Typography Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabelText("Typography")
                
                // Font Selector - Modernized
                Box(modifier = Modifier.clip(RoundedCornerShape(16.dp))) {
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingContent = { 
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.TextFields, null, modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    DropdownMenu(
                        expanded = showFontsDropdown,
                        onDismissRequest = { showFontsDropdown = false },
                        offset = DpOffset(0.dp, 8.dp),
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .width(with(LocalDensity.current) { rowSize.width.toDp() })
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(20.dp))
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
                                        style = MaterialTheme.typography.bodyLarge,
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Appearance Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelText("Appearance")
                    
                    // Follow System Toggle - Modernized
                    Surface(
                        color = if (state.followSystem.value) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { onFollowSystemChange(!state.followSystem.value) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.follow_system),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = state.followSystem.value,
                                onCheckedChange = onFollowSystemChange,
                                modifier = Modifier.scale(0.8f).height(24.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                if (!state.followSystem.value) {
                    // Theme Grid - More than 16 themes now
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Themes.entries.forEach { theme ->
                                ThemeChip(
                                    selected = theme == state.currentTheme.value,
                                    onClick = { onThemeChange(theme) },
                                    label = stringResource(id = theme.nameId),
                                    isDark = !theme.isLight
                                )
                            }
                        }

                        // Custom Theme Section
                        AnimatedVisibility(visible = state.currentTheme.value == Themes.CUSTOM) {
                            CustomThemeEditor(appPreferences)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun ThemeChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    isDark: Boolean
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = ""
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = ""
    )

    Surface(
        onClick = onClick,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (selected) 4.dp else 0.dp,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CustomThemeEditor(appPreferences: AppPreferences) {
    val bgValue by appPreferences.CUSTOM_THEME_BACKGROUND_COLOR.flow().collectAsStateInitial(appPreferences.CUSTOM_THEME_BACKGROUND_COLOR.value)
    val textValue by appPreferences.CUSTOM_THEME_TEXT_COLOR.flow().collectAsStateInitial(appPreferences.CUSTOM_THEME_TEXT_COLOR.value)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Custom Theme Designer",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ColorPickerItem(
                label = "Background",
                currentColor = Color(bgValue),
                onColorSelected = { appPreferences.CUSTOM_THEME_BACKGROUND_COLOR.value = it.toArgb() },
                modifier = Modifier.weight(1f)
            )
            ColorPickerItem(
                label = "Text Color",
                currentColor = Color(textValue),
                onColorSelected = { appPreferences.CUSTOM_THEME_TEXT_COLOR.value = it.toArgb() },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Preview Box
        Surface(
            color = Color(bgValue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "The quick brown fox jumps over the lazy dog.",
                    color = Color(textValue),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorPickerItem(
    label: String,
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFFFDF6E3), Color(0xFFEEE8D5), Color(0xFFF4ECD8), // Light/Sepia
        Color(0xFFE6D5B8), Color(0xFFF9F7F3), Color(0xFFFFF5F7), // Sand/Paper/Sakura
        Color(0xFFE5E9E0), Color(0xFFCDD6F4), Color(0xFFD8DEE9), // Matcha/Catppuccin/Nord
        Color(0xFF657B83), Color(0xFF5B4636), Color(0xFF4A4031), // Text Colors
        Color(0xFF002B36), Color(0xFF073642), Color(0xFF282828), // Dark
        Color(0xFF1E1E2E), Color(0xFF2E3440), Color(0xFF18181B), // Darkest
        Color(0xFF000000), Color(0xFFFFFFFF)
    )

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Box {
            Surface(
                onClick = { expanded = true },
                color = currentColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {}
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(200.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    FlowRow(
                        maxItemsInEachRow = 5,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (color == currentColor) 2.dp else 0.dp,
                                        color = if (color.luminance() > 0.5) Color.Black else Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onColorSelected(color)
                                        expanded = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
