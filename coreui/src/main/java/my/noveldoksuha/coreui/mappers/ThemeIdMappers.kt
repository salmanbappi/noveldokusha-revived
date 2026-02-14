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
        PreferenceThemes.Nord -> Themes.NORD
        PreferenceThemes.Dracula -> Themes.DRACULA
        PreferenceThemes.SolarizedLight -> Themes.SOLARIZED_LIGHT
        PreferenceThemes.SolarizedDark -> Themes.SOLARIZED_DARK
        PreferenceThemes.GruvboxDark -> Themes.GRUVBOX_DARK
        PreferenceThemes.Sepia -> Themes.SEPIA
        PreferenceThemes.Everforest -> Themes.EVERFOREST
        PreferenceThemes.RosePine -> Themes.ROSE_PINE
        PreferenceThemes.Sakura -> Themes.SAKURA
        PreferenceThemes.Forest -> Themes.FOREST
        PreferenceThemes.CatppuccinMocha -> Themes.CATPPUCCIN_MOCHA
        PreferenceThemes.CatppuccinLatte -> Themes.CATPPUCCIN_LATTE
        PreferenceThemes.Matcha -> Themes.MATCHA
        PreferenceThemes.Ocean -> Themes.OCEAN
        PreferenceThemes.Paper -> Themes.PAPER
        PreferenceThemes.Sand -> Themes.SAND
        PreferenceThemes.Custom -> Themes.CUSTOM
    }

val Themes.toPreferenceTheme
    get() = when (this) {
        Themes.LIGHT -> PreferenceThemes.Light
        Themes.DARK -> PreferenceThemes.Dark
        Themes.BLACK -> PreferenceThemes.Black
        Themes.MIDNIGHT -> PreferenceThemes.Midnight
        Themes.LAVA -> PreferenceThemes.Lava
        Themes.NORD -> PreferenceThemes.Nord
        Themes.DRACULA -> PreferenceThemes.Dracula
        Themes.SOLARIZED_LIGHT -> PreferenceThemes.SolarizedLight
        Themes.SOLARIZED_DARK -> PreferenceThemes.SolarizedDark
        Themes.GRUVBOX_DARK -> PreferenceThemes.GruvboxDark
        Themes.SEPIA -> PreferenceThemes.Sepia
        Themes.EVERFOREST -> PreferenceThemes.Everforest
        Themes.ROSE_PINE -> PreferenceThemes.RosePine
        Themes.SAKURA -> PreferenceThemes.Sakura
        Themes.FOREST -> PreferenceThemes.Forest
        Themes.CATPPUCCIN_MOCHA -> PreferenceThemes.CatppuccinMocha
        Themes.CATPPUCCIN_LATTE -> PreferenceThemes.CatppuccinLatte
        Themes.MATCHA -> PreferenceThemes.Matcha
        Themes.OCEAN -> PreferenceThemes.Ocean
        Themes.PAPER -> PreferenceThemes.Paper
        Themes.SAND -> PreferenceThemes.Sand
        Themes.CUSTOM -> PreferenceThemes.Custom
    }
