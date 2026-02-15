package my.noveldokusha.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.theme.InternalTheme
import my.noveldoksuha.coreui.theme.PreviewThemes
import my.noveldoksuha.coreui.theme.Themes
import my.noveldokusha.settings.sections.AppUpdates
import my.noveldokusha.settings.sections.LibraryAutoUpdate
import my.noveldokusha.settings.sections.SettingsBackup
import my.noveldokusha.settings.sections.SettingsData
import my.noveldokusha.settings.sections.SettingsExternalSources
import my.noveldokusha.settings.sections.SettingsTheme
import my.noveldokusha.settings.sections.SettingsTranslationModels

@Composable
internal fun SettingsScreenBody(
    state: SettingsScreenState,
    modifier: Modifier = Modifier,
    onFollowSystem: (Boolean) -> Unit,
    onThemeSelected: (Themes) -> Unit,
    onCleanDatabase: () -> Unit,
    onCleanImageFolder: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreData: () -> Unit,
    onDownloadTranslationModel: (lang: String) -> Unit,
    onRemoveTranslationModel: (lang: String) -> Unit,
    onCheckForUpdatesManual: () -> Unit,
    onDebugLogs: () -> Unit,
    onExternalSourcesUriSelected: (String) -> Unit = {},
    onManageRepositories: () -> Unit = {},
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        // Section: Appearance
        SettingHeader("Appearance & Reading")
        SettingsTheme(
            currentFollowSystem = state.followsSystemTheme.value,
            currentTheme = state.currentTheme.value,
            onFollowSystemChange = onFollowSystem,
            onCurrentThemeChange = onThemeSelected
        )
        
        if (state.isTranslationSettingsVisible.value) {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            SettingsTranslationModels(
                translationModelsStates = state.translationModelsStates,
                onDownloadTranslationModel = onDownloadTranslationModel,
                onRemoveTranslationModel = onRemoveTranslationModel
            )
        }

        Spacer(Modifier.height(16.dp))
        
        // Section: Library
        SettingHeader("Library & Automation")
        LibraryAutoUpdate(state = state.libraryAutoUpdate)
        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        AppUpdates(
            state = state.updateAppSetting,
            onCheckForUpdatesManual = onCheckForUpdatesManual
        )

        Spacer(Modifier.height(16.dp))

        // Section: Data
        SettingHeader("Data Management")
        SettingsBackup(
            onBackupData = onBackupData,
            onRestoreData = onRestoreData
        )
        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        SettingsData(
            databaseSize = state.databaseSize.value,
            imagesFolderSize = state.imageFolderSize.value,
            onCleanDatabase = onCleanDatabase,
            onCleanImageFolder = onCleanImageFolder
        )

        Spacer(Modifier.height(16.dp))

        // Section: Advanced
        SettingHeader("Advanced")
        SettingsExternalSources(
            currentUri = state.externalSourcesDirectoryUri.value,
            onUriSelected = onExternalSourcesUriSelected,
            onManageRepositories = onManageRepositories
        )
        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        androidx.compose.material3.ListItem(
            headlineContent = { Text("Debug Logs", style = MaterialTheme.typography.bodyLarge) },
            supportingContent = { Text("View system logs for troubleshooting", style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier.clickable(onClick = onDebugLogs),
            colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
        
        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "(°.°)",
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun SettingHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    )
}


@PreviewThemes
@Composable
private fun Preview() {
    val isDark = isSystemInDarkTheme()
    val theme = remember { mutableStateOf(if (isDark) Themes.DARK else Themes.LIGHT) }
    InternalTheme(theme.value) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreenBody(
                state = SettingsScreenState(
                    followsSystemTheme = remember { mutableStateOf(true) },
                    currentTheme = theme,
                    databaseSize = remember { mutableStateOf("1 MB") },
                    imageFolderSize = remember { mutableStateOf("10 MB") },
                    isTranslationSettingsVisible = remember { mutableStateOf(true) },
                    translationModelsStates = remember { mutableStateListOf() },
                    updateAppSetting = SettingsScreenState.UpdateApp(
                        currentAppVersion = "1.0.0",
                        appUpdateCheckerEnabled = remember { mutableStateOf(true) },
                        showNewVersionDialog = remember {
                            mutableStateOf(
                                null
                            )
                        },
                        checkingForNewVersion = remember { mutableStateOf(true) },
                    ),
                    libraryAutoUpdate = SettingsScreenState.LibraryAutoUpdate(
                        autoUpdateEnabled = remember { mutableStateOf(true) },
                        autoUpdateIntervalHours = remember { mutableIntStateOf(24) },
                        preFetchNextChapterEnabled = remember { mutableStateOf(true) }
                    ),
                    externalSourcesDirectoryUri = remember { mutableStateOf("") }
                ),
                onFollowSystem = { },
                onThemeSelected = { },
                onCleanDatabase = { },
                onCleanImageFolder = { },
                onBackupData = { },
                onRestoreData = { },
                onDownloadTranslationModel = { },
                onRemoveTranslationModel = { },
                onCheckForUpdatesManual = { },
                onDebugLogs = { },
            )
        }
    }
}