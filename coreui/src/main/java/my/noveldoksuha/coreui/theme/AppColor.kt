package my.noveldoksuha.coreui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

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

val light_appColor = AppColor(
    tabSurface = Grey75,
    bookSurface = Grey75,
    checkboxPositive = Success500,
    checkboxNegative = Error500,
    checkboxNeutral = Grey900,
    tintedSurface = Grey25.mix(ColorAccent, 0.65f),
    tintedSelectedSurface = Grey25.mix(ColorAccent, 0.75f),
)

val dark_appColor = AppColor(
    tabSurface = Grey800,
    bookSurface = Grey800,
    checkboxPositive = Success500,
    checkboxNegative = Error500,
    checkboxNeutral = Grey900,
    tintedSurface = Grey900.mix(ColorAccent, 0.65f),
    tintedSelectedSurface = Grey900.mix(ColorAccent, 0.75f),
)

val black_appColor = AppColor(
    tabSurface = Grey900,
    bookSurface = Grey900,
    checkboxPositive = Success500,
    checkboxNegative = Error500,
    checkboxNeutral = Grey900,
    tintedSurface = Grey1000.mix(ColorAccent, 0.65f),
    tintedSelectedSurface = Grey1000.mix(ColorAccent, 0.75f),
)

val midnight_appColor = AppColor(
    tabSurface = Slate900,
    bookSurface = Slate900,
    checkboxPositive = Sky400,
    checkboxNegative = Error500,
    checkboxNeutral = Slate100,
    tintedSurface = Slate900.mix(Sky400, 0.15f),
    tintedSelectedSurface = Slate900.mix(Sky400, 0.25f),
)

val lava_appColor = AppColor(
    tabSurface = Zinc900,
    bookSurface = Zinc900,
    checkboxPositive = Red500,
    checkboxNegative = Error500,
    checkboxNeutral = Zinc100,
    tintedSurface = Zinc900.mix(Red500, 0.15f),
    tintedSelectedSurface = Zinc900.mix(Red500, 0.25f),
)

val LocalAppColor = compositionLocalOf { light_appColor }

@Suppress("UnusedReceiverParameter")
val MaterialTheme.colorApp: AppColor
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColor.current