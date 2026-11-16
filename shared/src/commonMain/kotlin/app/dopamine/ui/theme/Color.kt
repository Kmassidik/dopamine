package app.dopamine.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Three accents, each with a fill, a text-safe tone, a soft tint and a "lip" for chunky buttons. */
@Immutable
class DopamineColors(
    val bg: Color, val surface: Color, val ink: Color, val inkSoft: Color, val line: Color,
    val onAccent: Color,
    val green: Color, val greenText: Color, val greenTint: Color, val greenLip: Color,
    val yellow: Color, val yellowText: Color, val yellowTint: Color, val yellowLip: Color,
    val pink: Color, val pinkText: Color, val pinkTint: Color, val pinkLip: Color,
)

val LightDopamineColors = DopamineColors(
    bg = Color(Palette.WHITE), surface = Color(Palette.WHITE), ink = Color(Palette.INK),
    inkSoft = Color(Palette.INK_SOFT), line = Color(Palette.LINE), onAccent = Color(Palette.INK),
    green = Color(Palette.GREEN), greenText = Color(Palette.GREEN_DEEP),
    greenTint = Color(Palette.GREEN_TINT), greenLip = Color(Palette.GREEN_LIP),
    yellow = Color(Palette.YELLOW), yellowText = Color(Palette.YELLOW_DEEP),
    yellowTint = Color(Palette.YELLOW_TINT), yellowLip = Color(Palette.YELLOW_LIP),
    pink = Color(Palette.PINK), pinkText = Color(Palette.PINK_DEEP),
    pinkTint = Color(Palette.PINK_TINT), pinkLip = Color(Palette.PINK_LIP),
)

val DarkDopamineColors = DopamineColors(
    bg = Color(Palette.N_BG), surface = Color(Palette.N_SURFACE), ink = Color(Palette.N_INK),
    inkSoft = Color(Palette.N_INK_SOFT), line = Color(Palette.N_LINE), onAccent = Color(Palette.N_BG),
    green = Color(Palette.N_GREEN), greenText = Color(Palette.N_GREEN_TEXT),
    greenTint = Color(Palette.N_GREEN_TINT), greenLip = Color(Palette.N_GREEN_LIP),
    yellow = Color(Palette.N_YELLOW), yellowText = Color(Palette.N_YELLOW),
    yellowTint = Color(Palette.N_YELLOW_TINT), yellowLip = Color(Palette.N_YELLOW_LIP),
    pink = Color(Palette.N_PINK), pinkText = Color(Palette.N_PINK_TEXT),
    pinkTint = Color(Palette.N_PINK_TINT), pinkLip = Color(Palette.N_PINK_LIP),
)

val LightScheme: ColorScheme = lightColorScheme(
    primary = Color(Palette.GREEN), onPrimary = Color(Palette.INK),
    primaryContainer = Color(Palette.GREEN_TINT), onPrimaryContainer = Color(Palette.INK),
    secondary = Color(Palette.YELLOW), onSecondary = Color(Palette.INK),
    tertiary = Color(Palette.PINK), onTertiary = Color(Palette.INK),
    background = Color(Palette.WHITE), onBackground = Color(Palette.INK),
    surface = Color(Palette.WHITE), onSurface = Color(Palette.INK),
    surfaceVariant = Color(Palette.GREEN_TINT), onSurfaceVariant = Color(Palette.INK_SOFT),
    outline = Color(Palette.LINE), outlineVariant = Color(Palette.LINE),
    error = Color(Palette.PINK_DEEP), onError = Color(Palette.WHITE),
)

val DarkScheme: ColorScheme = darkColorScheme(
    primary = Color(Palette.N_GREEN), onPrimary = Color(Palette.N_BG),
    primaryContainer = Color(Palette.N_GREEN_TINT), onPrimaryContainer = Color(Palette.N_INK),
    secondary = Color(Palette.N_YELLOW), onSecondary = Color(Palette.N_BG),
    tertiary = Color(Palette.N_PINK), onTertiary = Color(Palette.N_BG),
    background = Color(Palette.N_BG), onBackground = Color(Palette.N_INK),
    surface = Color(Palette.N_SURFACE), onSurface = Color(Palette.N_INK),
    surfaceVariant = Color(Palette.N_GREEN_TINT), onSurfaceVariant = Color(Palette.N_INK_SOFT),
    outline = Color(Palette.N_LINE), outlineVariant = Color(Palette.N_LINE),
    error = Color(Palette.N_PINK_TEXT), onError = Color(Palette.N_BG),
)
