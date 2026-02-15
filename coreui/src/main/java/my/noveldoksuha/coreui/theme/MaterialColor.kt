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
    primaryContainer = surface.mix(primary, 0.9f),
    onPrimaryContainer = onSurface,
    inversePrimary = onPrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = surface.mix(secondary, 0.85f),
    onSecondaryContainer = onSurface,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = surface.mix(tertiary, 0.85f),
    onTertiaryContainer = onSurface,
    background = surface,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surface.mix(onSurface, 0.9f),
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

val nord_colorScheme = createColorScheme(Nord8, Nord0, Nord0, Nord4, secondary = Nord9, tertiary = Nord10)
val sepia_colorScheme = createColorScheme(SepiaFg, SepiaBg, SepiaBg, SepiaFg, secondary = SepiaAccent)

fun custom_colorScheme(background: Color, text: Color): ColorScheme {
    val isLight = background.luminance() > 0.5
    val primary = if (isLight) ColorAccent else Sky400
    return createColorScheme(primary, background, background, text)
}
