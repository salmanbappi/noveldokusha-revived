package my.noveldokusha.settings.debug

import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import my.noveldoksuha.coreui.BaseActivity
import my.noveldoksuha.coreui.theme.Theme

@AndroidEntryPoint
class DebugLogsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme(themeProvider = themeProvider) {
                DebugLogsScreen(onBack = { finish() })
            }
        }
    }
}
