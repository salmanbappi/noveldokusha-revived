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
    border: Color,
    borderVisible: Color,
    textSecondary: Color,
    textDisabled: Color,
    error: Color = Color(0xFFD71921),
    onError: Color = Color.White
) = ColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = surface,
    onPrimaryContainer = onSurface,
    inversePrimary = onPrimary,
    secondary = primary,
    onSecondary = onPrimary,
    secondaryContainer = surface,
    onSecondaryContainer = onSurface,
    tertiary = primary,
    onTertiary = onPrimary,
    tertiaryContainer = surface,
    onTertiaryContainer = onSurface,
    background = surface,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surface,
    onSurfaceVariant = textSecondary,
    surfaceTint = primary,
    inverseSurface = onSurface,
    inverseOnSurface = surface,
    error = error,
    onError = onError,
    errorContainer = error.copy(alpha = 0.1f),
    onErrorContainer = error,
    outline = borderVisible,
    outlineVariant = border,
    scrim = Color.Black,
    surfaceBright = surface,
    surfaceDim = surface,
    surfaceContainerLowest = surface,
    surfaceContainerLow = surface,
    surfaceContainer = surface,
    surfaceContainerHigh = surface,
    surfaceContainerHighest = surface,
)

val light_colorScheme = createColorScheme(
    primary = NDAccent,
    onPrimary = NDLightTextDisplay,
    surface = NDLightSurface,
    onSurface = NDLightTextPrimary,
    border = NDLightBorder,
    borderVisible = NDLightBorderVisible,
    textSecondary = NDLightTextSecondary,
    textDisabled = NDLightTextDisabled
)

val dark_colorScheme = createColorScheme(
    primary = NDAccent,
    onPrimary = NDTextDisplay,
    surface = NDSurface,
    onSurface = NDTextPrimary,
    border = NDBorder,
    borderVisible = NDBorderVisible,
    textSecondary = NDTextSecondary,
    textDisabled = NDTextDisabled
)

val black_colorScheme = createColorScheme(
    primary = NDAccent,
    onPrimary = NDTextDisplay,
    surface = NDBlack,
    onSurface = NDTextPrimary,
    border = NDBorder,
    borderVisible = NDBorderVisible,
    textSecondary = NDTextSecondary,
    textDisabled = NDTextDisabled
)

val nord_colorScheme = createColorScheme(
    primary = NDAccent,
    onPrimary = Nord4,
    surface = Nord0,
    onSurface = Nord4,
    border = Nord0.mix(Nord4, 0.15f),
    borderVisible = Nord0.mix(Nord4, 0.3f),
    textSecondary = Nord4.copy(alpha = 0.7f),
    textDisabled = Nord4.copy(alpha = 0.4f)
)

val sepia_colorScheme = createColorScheme(
    primary = NDAccent,
    onPrimary = SepiaFg,
    surface = SepiaBg,
    onSurface = SepiaFg,
    border = SepiaBg.mix(SepiaFg, 0.15f),
    borderVisible = SepiaBg.mix(SepiaFg, 0.3f),
    textSecondary = SepiaFg.copy(alpha = 0.7f),
    textDisabled = SepiaFg.copy(alpha = 0.4f)
)

fun custom_colorScheme(background: Color, text: Color): ColorScheme {
    val isLight = background.luminance() > 0.5
    return createColorScheme(
        primary = NDAccent,
        onPrimary = text,
        surface = background,
        onSurface = text,
        border = if (isLight) NDLightBorder else NDBorder,
        borderVisible = if (isLight) NDLightBorderVisible else NDBorderVisible,
        textSecondary = text.copy(alpha = 0.7f),
        textDisabled = text.copy(alpha = 0.4f)
    )
}
