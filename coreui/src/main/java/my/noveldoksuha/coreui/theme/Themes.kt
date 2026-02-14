package my.noveldoksuha.coreui.theme

import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import my.noveldoksuha.coreui.R

enum class Themes(
    val isLight: Boolean,
    @StringRes val nameId: Int,
    @StyleRes val themeId: Int,
) {
    LIGHT(
        isLight = true,
        nameId = R.string.theme_name_light,
        themeId = R.style.AppTheme_Light,
    ),
    DARK(
        isLight = false,
        nameId = R.string.theme_name_dark,
        themeId = R.style.AppTheme_BaseDark_Dark,
    ),
    BLACK(
        isLight = false,
        nameId = R.string.theme_name_black,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    MIDNIGHT(
        isLight = false,
        nameId = R.string.theme_name_midnight,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    LAVA(
        isLight = false,
        nameId = R.string.theme_name_lava,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    NORD(
        isLight = false,
        nameId = R.string.theme_name_nord,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    DRACULA(
        isLight = false,
        nameId = R.string.theme_name_dracula,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    SOLARIZED_LIGHT(
        isLight = true,
        nameId = R.string.theme_name_solarized_light,
        themeId = R.style.AppTheme_Light,
    ),
    SOLARIZED_DARK(
        isLight = false,
        nameId = R.string.theme_name_solarized_dark,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    GRUVBOX_DARK(
        isLight = false,
        nameId = R.string.theme_name_gruvbox_dark,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    SEPIA(
        isLight = true,
        nameId = R.string.theme_name_sepia,
        themeId = R.style.AppTheme_Light,
    ),
    EVERFOREST(
        isLight = false,
        nameId = R.string.theme_name_everforest,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    ROSE_PINE(
        isLight = false,
        nameId = R.string.theme_name_rose_pine,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    SAKURA(
        isLight = true,
        nameId = R.string.theme_name_sakura,
        themeId = R.style.AppTheme_Light,
    ),
    FOREST(
        isLight = false,
        nameId = R.string.theme_name_forest,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    CATPPUCCIN_MOCHA(
        isLight = false,
        nameId = R.string.theme_name_catppuccin_mocha,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    CATPPUCCIN_LATTE(
        isLight = true,
        nameId = R.string.theme_name_catppuccin_latte,
        themeId = R.style.AppTheme_Light,
    ),
    MATCHA(
        isLight = true,
        nameId = R.string.theme_name_matcha,
        themeId = R.style.AppTheme_Light,
    ),
    OCEAN(
        isLight = false,
        nameId = R.string.theme_name_ocean,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    PAPER(
        isLight = true,
        nameId = R.string.theme_name_paper,
        themeId = R.style.AppTheme_Light,
    ),
    SAND(
        isLight = true,
        nameId = R.string.theme_name_sand,
        themeId = R.style.AppTheme_Light,
    ),
    CUSTOM(
        isLight = true, // Default to true, will be determined by custom bg
        nameId = R.string.theme_name_custom,
        themeId = R.style.AppTheme_Light,
    );
}
