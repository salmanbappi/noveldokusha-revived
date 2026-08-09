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
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFF0F0F0),
    onSurface = Color(0xFF1C1B1F),
    border = Color(0xFFE0E0E0),
    borderVisible = Color(0xFFCCCCCC),
    textSecondary = Color(0xFF666666),
    textDisabled = Color(0xFF999999)
)

val dark_colorScheme = createColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    surfaceRaised = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    border = Color(0xFF262626),
    borderVisible = Color(0xFF383838),
    textSecondary = Color(0xFFA0A0A0),
    textDisabled = Color(0xFF555555)
)
