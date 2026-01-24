package my.noveldoksuha.coreui.mappers

import my.noveldoksuha.coreui.theme.Themes
import my.noveldokusha.core.appPreferences.PreferenceThemes

val PreferenceThemes.toTheme
    get() = when (this) {
        PreferenceThemes.Light -> Themes.LIGHT
        PreferenceThemes.Dark -> Themes.DARK
        PreferenceThemes.Black -> Themes.BLACK
        PreferenceThemes.Midnight -> Themes.MIDNIGHT
        PreferenceThemes.Lava -> Themes.LAVA
    }

val Themes.toPreferenceTheme
    get() = when (this) {
        Themes.LIGHT -> PreferenceThemes.Light
        Themes.DARK -> PreferenceThemes.Dark
        Themes.BLACK -> PreferenceThemes.Black
        Themes.MIDNIGHT -> PreferenceThemes.Midnight
        Themes.LAVA -> PreferenceThemes.Lava
    }