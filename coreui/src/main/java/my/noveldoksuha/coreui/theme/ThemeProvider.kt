package my.noveldoksuha.coreui.theme

import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope
import my.noveldokusha.core.appPreferences.AppPreferences

interface ThemeProvider {
    val appPreferences: AppPreferences

    fun followSystem(stateCoroutineScope: CoroutineScope): State<Boolean>

    fun currentTheme(stateCoroutineScope: CoroutineScope): State<Themes>
}