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
    background: Color,
    surface: Color,
    surfaceRaised: Color,
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
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceRaised,
    onSurfaceVariant = textSecondary,
    surfaceTint = primary,
    inverseSurface = onSurface,
    inverseOnSurface = background,
    error = error,
    onError = onError,
    errorContainer = error.copy(alpha = 0.1f),
    onErrorContainer = error,
    outline = borderVisible,
    outlineVariant = border,
    scrim = Color.Black,
    surfaceBright = surfaceRaised,
    surfaceDim = surface,
    surfaceContainerLowest = background,
    surfaceContainerLow = surface,
    surfaceContainer = surface,
    surfaceContainerHigh = surfaceRaised,
    surfaceContainerHighest = surfaceRaised,
)

val light_colorScheme = createColorScheme(
    primary = NDAccent,
    onPrimary = NDLightTextDisplay,
    background = NDLightBlack,
    surface = NDLightSurface,
    surfaceRaised = NDLightSurfaceRaised,
    onSurface = NDLightTextPrimary,
    border = NDLightBorder,
    borderVisible = NDLightBorderVisible,
    textSecondary = NDLightTextSecondary,
    textDisabled = NDLightTextDisabled
)

val dark_colorScheme = createColorScheme(
    primary = NDAccent,
    onPrimary = NDTextDisplay,
    background = NDBlack,
    surface = NDSurface,
    surfaceRaised = NDSurfaceRaised,
    onSurface = NDTextPrimary,
    border = NDBorder,
    borderVisible = NDBorderVisible,
    textSecondary = NDTextSecondary,
    textDisabled = NDTextDisabled
)
