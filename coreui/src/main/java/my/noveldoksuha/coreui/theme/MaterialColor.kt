package my.noveldoksuha.coreui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun ColorScheme.isLightTheme() = background.luminance() > 0.5

private fun createColorScheme(
    primary: Color,
    onPrimary: Color,
    surface: Color,
    onSurface: Color,
    secondary: Color = primary,
    onSecondary: Color = onPrimary,
    tertiary: Color = surface,
    onTertiary: Color = onSurface,
    error: Color = Error600,
    onError: Color = Color.White
) = ColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = surface.mix(primary, 0.1f),
    onPrimaryContainer = onSurface,
    inversePrimary = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = surface.mix(secondary, 0.15f),
    onSecondaryContainer = onSurface,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = surface.mix(tertiary, 0.15f),
    onTertiaryContainer = onSurface,
    background = surface,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surface.mix(onSurface, 0.05f),
    onSurfaceVariant = onSurface.copy(alpha = 0.7f),
    surfaceTint = primary,
    inverseSurface = onSurface,
    inverseOnSurface = surface,
    error = error,
    onError = onError,
    errorContainer = error.copy(alpha = 0.1f),
    onErrorContainer = error,
    outline = onSurface.copy(alpha = 0.5f),
    outlineVariant = onSurface.copy(alpha = 0.2f),
    scrim = Color.Black,
    surfaceBright = surface.mix(Color.White, 0.95f),
    surfaceDim = surface.mix(Color.Black, 0.95f),
    surfaceContainerLowest = surface,
    surfaceContainerLow = surface.mix(onSurface, 0.96f),
    surfaceContainer = surface.mix(onSurface, 0.92f),
    surfaceContainerHigh = surface.mix(onSurface, 0.88f),
    surfaceContainerHighest = surface.mix(onSurface, 0.84f),
)

val light_colorScheme = createColorScheme(ColorAccent, Color.White, Grey25, Grey900, secondary = ColorAccent, tertiary = ColorNotice)
val dark_colorScheme = createColorScheme(ColorAccent, Grey25, Grey900, Grey50, secondary = ColorAccent, tertiary = ColorNotice)
val black_colorScheme = createColorScheme(ColorAccent, Grey25, Grey1000, Grey50, secondary = ColorAccent, tertiary = ColorNotice)
val midnight_colorScheme = createColorScheme(Sky400, Slate100, Slate900, Slate100, secondary = Sky400)
val lava_colorScheme = createColorScheme(Red500, Zinc100, Zinc900, Zinc100, secondary = Red500)

val nord_colorScheme = createColorScheme(Nord8, Nord0, Nord0, Nord4, secondary = Nord9, tertiary = Nord10)
val dracula_colorScheme = createColorScheme(DraculaPink, DraculaBackground, DraculaBackground, DraculaForeground, secondary = DraculaPurple, tertiary = DraculaCyan)
val solarized_light_colorScheme = createColorScheme(SolBlue, SolBase3, SolBase3, SolBase00, secondary = SolCyan, tertiary = SolViolet)
val solarized_dark_colorScheme = createColorScheme(SolBlue, SolBase03, SolBase03, SolBase0, secondary = SolCyan, tertiary = SolViolet)
val gruvbox_dark_colorScheme = createColorScheme(GruvOrange, GruvBg0, GruvBg0, GruvFg0, secondary = GruvBlue, tertiary = GruvAqua)
val sepia_colorScheme = createColorScheme(SepiaFg, SepiaBg, SepiaBg, SepiaFg, secondary = SepiaAccent)
val everforest_colorScheme = createColorScheme(EverforestGreen, EverforestBg, EverforestBg, EverforestFg, secondary = EverforestBlue)
val rose_pine_colorScheme = createColorScheme(RosePineGold, RosePineBg, RosePineBg, RosePineFg, secondary = RosePineRose, tertiary = RosePineLove)
val sakura_colorScheme = createColorScheme(SakuraPink, SakuraFg, SakuraBg, SakuraFg, secondary = SakuraRed)
val forest_colorScheme = createColorScheme(ForestAccent, ForestFg, ForestBg, ForestFg, secondary = ForestLight)
val catppuccin_mocha_colorScheme = createColorScheme(CatMochaMauve, CatMochaBase, CatMochaBase, CatMochaText, secondary = CatMochaBlue, tertiary = CatMochaRed)
val catppuccin_latte_colorScheme = createColorScheme(CatLatteMauve, CatLatteBase, CatLatteBase, CatLatteText, secondary = CatLatteBlue)
val matcha_colorScheme = createColorScheme(MatchaGreen, MatchaFg, MatchaBg, MatchaFg, secondary = MatchaGreenDark)
val ocean_colorScheme = createColorScheme(OceanBlue, OceanBg, OceanBg, OceanFg, secondary = OceanCyan, tertiary = OceanPurple)
val paper_colorScheme = createColorScheme(Color(0xFF555555), PaperBg, PaperBg, PaperFg, secondary = Color(0xFF777777))
val sand_colorScheme = createColorScheme(SandFg, SandBg, SandBg, SandFg, secondary = Color(0xFF8D6E63))

fun custom_colorScheme(background: Color, text: Color): ColorScheme {
    val isLight = background.luminance() > 0.5
    val primary = if (isLight) ColorAccent else Sky400
    return createColorScheme(primary, background, background, text)
}