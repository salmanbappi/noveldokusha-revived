package my.noveldokusha.features.reader.ui.settingDialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import my.noveldokusha.features.reader.features.LiveTranslationSettingData
import my.noveldokusha.features.reader.features.TextToSpeechSettingData

@Composable
internal fun ToolsSettingDialog(
    ttsState: TextToSpeechSettingData,
    translationState: LiveTranslationSettingData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (translationState.isAvailable) {
            TranslatorSettingDialog(state = translationState)
        }
        VoiceReaderSettingDialog(state = ttsState)
    }
}
