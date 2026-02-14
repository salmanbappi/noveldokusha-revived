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
    SEPIA(
        isLight = true,
        nameId = R.string.theme_name_sepia,
        themeId = R.style.AppTheme_Light,
    ),
    NORD(
        isLight = false,
        nameId = R.string.theme_name_nord,
        themeId = R.style.AppTheme_BaseDark_Black,
    ),
    CUSTOM(
        isLight = true, // Default to true, will be determined by custom bg
        nameId = R.string.theme_name_custom,
        themeId = R.style.AppTheme_Light,
    );
}