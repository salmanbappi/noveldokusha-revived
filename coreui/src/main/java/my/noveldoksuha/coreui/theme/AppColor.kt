package my.noveldoksuha.coreui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Immutable
data class AppColor(
    val tabSurface: Color,
    val bookSurface: Color,
    val checkboxPositive: Color,
    val checkboxNegative: Color,
    val checkboxNeutral: Color,
    val tintedSurface: Color,
    val tintedSelectedSurface: Color,
)

private fun createAppColor(surface: Color, accent: Color, onSurface: Color) = AppColor(
    tabSurface = surface.mix(onSurface, 0.05f),
    bookSurface = surface.mix(onSurface, 0.05f),
    checkboxPositive = NDSuccess,
    checkboxNegative = NDAccent,
    checkboxNeutral = onSurface,
    tintedSurface = surface.mix(accent, 0.10f),
    tintedSelectedSurface = surface.mix(accent, 0.20f),
)

val light_appColor = createAppColor(NDLightSurface, NDAccent, NDLightTextPrimary)
val dark_appColor = createAppColor(NDSurface, NDAccent, NDTextPrimary)
val black_appColor = createAppColor(NDBlack, NDAccent, NDTextPrimary)

val nord_appColor = createAppColor(Nord0, NDAccent, Nord4)
val sepia_appColor = createAppColor(SepiaBg, NDAccent, SepiaFg)

fun custom_appColor(background: Color, text: Color): AppColor {
    return createAppColor(background, NDAccent, text)
}

fun dynamic_appColor(surface: Color, primary: Color, onSurface: Color): AppColor {
    return createAppColor(surface, primary, onSurface)
}

val LocalAppColor = compositionLocalOf { light_appColor }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.colorApp: AppColor
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColor.current