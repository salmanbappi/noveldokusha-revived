package my.noveldoksuha.coreui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun Theme(
    themeProvider: ThemeProvider,
    content: @Composable () -> @Composable Unit,
) {
    val followSystemsTheme by themeProvider.followSystem(rememberCoroutineScope())
    val selectedTheme by themeProvider.currentTheme(rememberCoroutineScope())
    val customBg by themeProvider.appPreferences.CUSTOM_THEME_BACKGROUND_COLOR.flow().collectAsStateInitial(
        themeProvider.appPreferences.CUSTOM_THEME_BACKGROUND_COLOR.value
    )
    val customText by themeProvider.appPreferences.CUSTOM_THEME_TEXT_COLOR.flow().collectAsStateInitial(
        themeProvider.appPreferences.CUSTOM_THEME_TEXT_COLOR.value
    )

    val isSystemThemeLight = !isSystemInDarkTheme()
    val theme: Themes = when (followSystemsTheme) {
        true -> when {
            isSystemThemeLight && !selectedTheme.isLight -> Themes.LIGHT
            !isSystemThemeLight && selectedTheme.isLight -> Themes.DARK
            else -> selectedTheme
        }
        false -> selectedTheme
    }
    InternalTheme(
        theme = theme,
        customBg = Color(customBg),
        customText = Color(customText),
        content = content,
    )
}

@Composable
fun InternalTheme(
    theme: Themes = if (isSystemInDarkTheme()) Themes.DARK else Themes.LIGHT,
    customBg: Color = Color(0xFFFDF6E3),
    customText: Color = Color(0xFF657B83),
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        Themes.LIGHT -> light_colorScheme
        Themes.DARK -> dark_colorScheme
        Themes.BLACK -> black_colorScheme
        Themes.SEPIA -> sepia_colorScheme
        Themes.NORD -> nord_colorScheme
        Themes.CUSTOM -> custom_colorScheme(customBg, customText)
    }

    val appColor = when (theme) {
        Themes.LIGHT -> light_appColor
        Themes.DARK -> dark_appColor
        Themes.BLACK -> black_appColor
        Themes.SEPIA -> sepia_appColor
        Themes.NORD -> nord_appColor
        Themes.CUSTOM -> custom_appColor(customBg, customText)
    }

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = theme.isLight || (theme == Themes.CUSTOM && customBg.luminance() > 0.5)
    )
    val textSelectionColors = remember {
        TextSelectionColors(
            handleColor = ColorAccent,
            backgroundColor = ColorAccent.copy(alpha = 0.3f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides colorScheme.onSurface,
            LocalAppColor provides appColor,
            LocalTextSelectionColors provides textSelectionColors,
            content = content
        )
    }
}
