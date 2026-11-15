package app.dopamine.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Colors with a meaning in the app that Material's scheme has no slot for. */
@Immutable
class DopamineColors(
    val correct: Color,
    val correctSoft: Color,
    val wrong: Color,
    val wrongSoft: Color,
    val reward: Color,
    val rewardSoft: Color,
    val ink: Color,
    val inkSoft: Color,
    val paperDeep: Color,
)

val LightDopamineColors = DopamineColors(
    correct = Color(Palette.MATCHA),
    correctSoft = Color(Palette.MATCHA_SOFT),
    wrong = Color(Palette.VERMILION),
    wrongSoft = Color(Palette.VERMILION_SOFT),
    reward = Color(Palette.SAKURA),
    rewardSoft = Color(Palette.SAKURA_SOFT),
    ink = Color(Palette.SUMI),
    inkSoft = Color(Palette.SUMI_SOFT),
    paperDeep = Color(Palette.WASHI_DEEP),
)

val DarkDopamineColors = DopamineColors(
    correct = Color(Palette.MATCHA_LIGHT),
    correctSoft = Color(Palette.MATCHA_NIGHT_SOFT),
    wrong = Color(Palette.VERMILION_LIGHT),
    wrongSoft = Color(Palette.VERMILION_NIGHT_SOFT),
    reward = Color(Palette.SAKURA_LIGHT),
    rewardSoft = Color(Palette.NIGHT_DEEP),
    ink = Color(Palette.PAPER),
    inkSoft = Color(Palette.PAPER_SOFT),
    paperDeep = Color(Palette.NIGHT_DEEP),
)

val LightScheme: ColorScheme = lightColorScheme(
    primary = Color(Palette.INDIGO),
    onPrimary = Color(Palette.WASHI_RAISED),
    primaryContainer = Color(Palette.WASHI_DEEP),
    onPrimaryContainer = Color(Palette.SUMI),
    secondary = Color(Palette.MATCHA),
    onSecondary = Color(Palette.WASHI_RAISED),
    tertiary = Color(Palette.SAKURA),
    onTertiary = Color(Palette.SUMI),
    background = Color(Palette.WASHI),
    onBackground = Color(Palette.SUMI),
    surface = Color(Palette.WASHI_RAISED),
    onSurface = Color(Palette.SUMI),
    surfaceVariant = Color(Palette.WASHI_DEEP),
    onSurfaceVariant = Color(Palette.SUMI_SOFT),
    outline = Color(Palette.SUMI),
    outlineVariant = Color(Palette.SUMI_SOFT),
    error = Color(Palette.VERMILION),
    onError = Color(Palette.WASHI_RAISED),
)

val DarkScheme: ColorScheme = darkColorScheme(
    primary = Color(Palette.INDIGO_LIGHT),
    onPrimary = Color(Palette.NIGHT),
    primaryContainer = Color(Palette.NIGHT_DEEP),
    onPrimaryContainer = Color(Palette.PAPER),
    secondary = Color(Palette.MATCHA_LIGHT),
    onSecondary = Color(Palette.NIGHT),
    tertiary = Color(Palette.SAKURA_LIGHT),
    onTertiary = Color(Palette.NIGHT),
    background = Color(Palette.NIGHT),
    onBackground = Color(Palette.PAPER),
    surface = Color(Palette.NIGHT_RAISED),
    onSurface = Color(Palette.PAPER),
    surfaceVariant = Color(Palette.NIGHT_DEEP),
    onSurfaceVariant = Color(Palette.PAPER_SOFT),
    outline = Color(Palette.PAPER_SOFT),
    outlineVariant = Color(Palette.NIGHT_DEEP),
    error = Color(Palette.VERMILION_LIGHT),
    onError = Color(Palette.NIGHT),
)
