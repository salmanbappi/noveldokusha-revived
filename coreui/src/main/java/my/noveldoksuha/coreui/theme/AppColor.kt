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
    tabSurface = surface.mix(onSurface, 0.1f),
    bookSurface = surface.mix(onSurface, 0.1f),
    checkboxPositive = Color(0xFF4CAF50),
    checkboxNegative = Color(0xFFF44336),
    checkboxNeutral = onSurface,
    tintedSurface = surface.mix(accent, 0.15f),
    tintedSelectedSurface = surface.mix(accent, 0.25f),
)

val light_appColor = createAppColor(Grey25, ColorAccent, Grey900)
val dark_appColor = createAppColor(Grey900, ColorAccent, Grey50)
val black_appColor = createAppColor(Grey1000, ColorAccent, Grey50)

val nord_appColor = createAppColor(Nord0, Nord8, Nord4)
val sepia_appColor = createAppColor(SepiaBg, Color(0xFF795548), SepiaFg)

fun custom_appColor(background: Color, text: Color): AppColor {
    val isLight = background.luminance() > 0.5
    val accent = if (isLight) ColorAccent else Sky400
    return createAppColor(background, accent, text)
}

val LocalAppColor = compositionLocalOf { light_appColor }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.colorApp: AppColor
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColor.current