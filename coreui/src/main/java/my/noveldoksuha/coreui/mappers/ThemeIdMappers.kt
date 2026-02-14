package my.noveldoksuha.coreui.mappers

import my.noveldoksuha.coreui.theme.Themes
import my.noveldokusha.core.appPreferences.PreferenceThemes

val PreferenceThemes.toTheme
    get() = when (this) {
        PreferenceThemes.Light -> Themes.LIGHT
        PreferenceThemes.Dark -> Themes.DARK
        PreferenceThemes.Black -> Themes.BLACK
        PreferenceThemes.Nord -> Themes.NORD
        PreferenceThemes.Sepia -> Themes.SEPIA
        PreferenceThemes.Custom -> Themes.CUSTOM
    }

val Themes.toPreferenceTheme
    get() = when (this) {
        Themes.LIGHT -> PreferenceThemes.Light
        Themes.DARK -> PreferenceThemes.Dark
        Themes.BLACK -> PreferenceThemes.Black
        Themes.NORD -> PreferenceThemes.Nord
        Themes.SEPIA -> PreferenceThemes.Sepia
        Themes.CUSTOM -> PreferenceThemes.Custom
    }