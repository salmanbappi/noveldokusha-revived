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
    secondaryContainer = primary.mix(surface, 0.2f),
    onSecondaryContainer = primary,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = surface.mix(tertiary, 0.1f),
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
    surfaceBright = surface.mix(Color.White, 0.05f),
    surfaceDim = surface.mix(Color.Black, 0.05f),
    surfaceContainerLowest = surface,
    surfaceContainerLow = surface.mix(onSurface, 0.04f),
    surfaceContainer = surface.mix(onSurface, 0.08f),
    surfaceContainerHigh = surface.mix(onSurface, 0.12f),
    surfaceContainerHighest = surface.mix(onSurface, 0.16f),
)

val light_colorScheme = createColorScheme(ColorAccent, Color.White, Grey25, Grey900)
val dark_colorScheme = createColorScheme(ColorAccent, Grey25, Grey900, Grey50)
val black_colorScheme = createColorScheme(ColorAccent, Grey25, Grey1000, Grey50)
val midnight_colorScheme = createColorScheme(Sky400, Slate100, Slate900, Slate100)
val lava_colorScheme = createColorScheme(Red500, Zinc100, Zinc900, Zinc100)

val nord_colorScheme = createColorScheme(Nord8, Nord0, Nord0, Nord4)
val dracula_colorScheme = createColorScheme(DraculaPink, DraculaBackground, DraculaBackground, DraculaForeground)
val solarized_light_colorScheme = createColorScheme(SolBlue, SolBase3, SolBase3, SolBase00)
val solarized_dark_colorScheme = createColorScheme(SolBlue, SolBase03, SolBase03, SolBase0)
val gruvbox_dark_colorScheme = createColorScheme(GruvOrange, GruvBg0, GruvBg0, GruvFg0)
val sepia_colorScheme = createColorScheme(SepiaFg, SepiaBg, SepiaBg, SepiaFg)
val everforest_colorScheme = createColorScheme(EverforestGreen, EverforestBg, EverforestBg, EverforestFg)
val rose_pine_colorScheme = createColorScheme(RosePineGold, RosePineBg, RosePineBg, RosePineFg)
val sakura_colorScheme = createColorScheme(SakuraPink, SakuraFg, SakuraBg, SakuraFg)
val forest_colorScheme = createColorScheme(ForestAccent, ForestFg, ForestBg, ForestFg)
val catppuccin_mocha_colorScheme = createColorScheme(CatMochaMauve, CatMochaBase, CatMochaBase, CatMochaText)
val catppuccin_latte_colorScheme = createColorScheme(CatLatteMauve, CatLatteBase, CatLatteBase, CatLatteText)
val matcha_colorScheme = createColorScheme(MatchaGreen, MatchaFg, MatchaBg, MatchaFg)
val ocean_colorScheme = createColorScheme(OceanBlue, OceanBg, OceanBg, OceanFg)
val paper_colorScheme = createColorScheme(Color(0xFF555555), PaperBg, PaperBg, PaperFg)
val sand_colorScheme = createColorScheme(SandFg, SandBg, SandBg, SandFg)

fun custom_colorScheme(background: Color, text: Color): ColorScheme {
    val isLight = background.luminance() > 0.5
    val primary = if (isLight) ColorAccent else Sky400
    return createColorScheme(primary, background, background, text)
}