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
        Themes.MIDNIGHT -> midnight_colorScheme
        Themes.LAVA -> lava_colorScheme
        Themes.NORD -> nord_colorScheme
        Themes.DRACULA -> dracula_colorScheme
        Themes.SOLARIZED_LIGHT -> solarized_light_colorScheme
        Themes.SOLARIZED_DARK -> solarized_dark_colorScheme
        Themes.GRUVBOX_DARK -> gruvbox_dark_colorScheme
        Themes.SEPIA -> sepia_colorScheme
        Themes.EVERFOREST -> everforest_colorScheme
        Themes.ROSE_PINE -> rose_pine_colorScheme
        Themes.SAKURA -> sakura_colorScheme
        Themes.FOREST -> forest_colorScheme
        Themes.CATPPUCCIN_MOCHA -> catppuccin_mocha_colorScheme
        Themes.CATPPUCCIN_LATTE -> catppuccin_latte_colorScheme
        Themes.MATCHA -> matcha_colorScheme
        Themes.OCEAN -> ocean_colorScheme
        Themes.PAPER -> paper_colorScheme
        Themes.SAND -> sand_colorScheme
        Themes.CUSTOM -> custom_colorScheme(customBg, customText)
    }

    val appColor = when (theme) {
        Themes.LIGHT -> light_appColor
        Themes.DARK -> dark_appColor
        Themes.BLACK -> black_appColor
        Themes.MIDNIGHT -> midnight_appColor
        Themes.LAVA -> lava_appColor
        Themes.NORD -> nord_appColor
        Themes.DRACULA -> dracula_appColor
        Themes.SOLARIZED_LIGHT -> solarized_light_appColor
        Themes.SOLARIZED_DARK -> solarized_dark_appColor
        Themes.GRUVBOX_DARK -> gruvbox_dark_appColor
        Themes.SEPIA -> sepia_appColor
        Themes.EVERFOREST -> everforest_appColor
        Themes.ROSE_PINE -> rose_pine_appColor
        Themes.SAKURA -> sakura_appColor
        Themes.FOREST -> forest_appColor
        Themes.CATPPUCCIN_MOCHA -> catppuccin_mocha_appColor
        Themes.CATPPUCCIN_LATTE -> catppuccin_latte_appColor
        Themes.MATCHA -> matcha_appColor
        Themes.OCEAN -> ocean_appColor
        Themes.PAPER -> paper_appColor
        Themes.SAND -> sand_appColor
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
