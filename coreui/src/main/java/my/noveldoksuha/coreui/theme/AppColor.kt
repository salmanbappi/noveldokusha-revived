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
val midnight_appColor = createAppColor(Slate900, Sky400, Slate100)
val lava_appColor = createAppColor(Zinc900, Red500, Zinc100)

val nord_appColor = createAppColor(Nord0, Nord8, Nord4)
val dracula_appColor = createAppColor(DraculaBackground, DraculaPink, DraculaForeground)
val solarized_light_appColor = createAppColor(SolBase3, SolBlue, SolBase00)
val solarized_dark_appColor = createAppColor(SolBase03, SolBlue, SolBase0)
val gruvbox_dark_appColor = createAppColor(GruvBg0, GruvOrange, GruvFg0)
val sepia_appColor = createAppColor(SepiaBg, Color(0xFF795548), SepiaFg)
val everforest_appColor = createAppColor(EverforestBg, EverforestGreen, EverforestFg)
val rose_pine_appColor = createAppColor(RosePineBg, RosePineGold, RosePineFg)
val sakura_appColor = createAppColor(SakuraBg, SakuraPink, SakuraFg)
val forest_appColor = createAppColor(ForestBg, ForestAccent, ForestFg)
val catppuccin_mocha_appColor = createAppColor(CatMochaBase, CatMochaMauve, CatMochaText)
val catppuccin_latte_appColor = createAppColor(CatLatteBase, CatLatteMauve, CatLatteText)
val matcha_appColor = createAppColor(MatchaBg, MatchaGreen, MatchaFg)
val ocean_appColor = createAppColor(OceanBg, OceanBlue, OceanFg)
val paper_appColor = createAppColor(PaperBg, ColorAccent, PaperFg)
val sand_appColor = createAppColor(SandBg, Color(0xFF8D6E63), SandFg)

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
