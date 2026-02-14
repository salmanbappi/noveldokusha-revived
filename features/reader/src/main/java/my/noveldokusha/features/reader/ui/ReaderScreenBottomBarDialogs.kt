package my.noveldokusha.features.reader.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.theme.Themes
import my.noveldokusha.core.appPreferences.AppPreferences
import my.noveldokusha.features.reader.ui.settingDialogs.MoreSettingDialog
import my.noveldokusha.features.reader.ui.settingDialogs.StyleSettingDialog
import my.noveldokusha.features.reader.ui.settingDialogs.TranslatorSettingDialog
import my.noveldokusha.features.reader.ui.settingDialogs.VoiceReaderSettingDialog

@Composable
internal fun ReaderScreenBottomBarDialogs(
    state: ReaderScreenState,
    appPreferences: AppPreferences,
    onTextFontChanged: (String) -> Unit,
    onTextSizeChanged: (Float) -> Unit,
    onSelectableTextChange: (Boolean) -> Unit,
    onFollowSystem: (Boolean) -> Unit,
    onThemeSelected: (Themes) -> Unit,
    onKeepScreenOn: (Boolean) -> Unit,
    onFullScreen: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = state.settings
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(Modifier.padding(horizontal = 24.dp)) {
            AnimatedContent(targetState = settings.selectedSetting.value, label = "") { target ->
                when (target) {
                    ReaderScreenState.Settings.Type.LiveTranslation -> TranslatorSettingDialog(
                        state = settings.liveTranslation
                    )
                    ReaderScreenState.Settings.Type.TextToSpeech -> VoiceReaderSettingDialog(
                        state = settings.textToSpeech
                    )
                    ReaderScreenState.Settings.Type.Style -> {
                        StyleSettingDialog(
                            state = settings.style,
                            onFollowSystemChange = onFollowSystem,
                            onThemeChange = onThemeSelected,
                            onTextFontChange = onTextFontChanged,
                            onTextSizeChange = onTextSizeChanged,
                            appPreferences = appPreferences
                        )
                    }
                    ReaderScreenState.Settings.Type.More -> MoreSettingDialog(
                        readingTimer = state.readingTimer.value,
                        allowTextSelection = settings.isTextSelectable.value,
                        onAllowTextSelectionChange = onSelectableTextChange,
                        keepScreenOn = settings.keepScreenOn.value,
                        onKeepScreenOn = onKeepScreenOn,
                        fullScreen = settings.fullScreen.value,
                        onFullScreen = onFullScreen,
                        autoScrollSpeed = settings.autoScrollSpeed.value,
                        onAutoScrollChange = { settings.autoScrollSpeed.value = it }
                    )
                    ReaderScreenState.Settings.Type.None -> Unit
                }
            }
        }
    }
}
