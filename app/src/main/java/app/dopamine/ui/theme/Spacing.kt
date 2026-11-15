package app.dopamine.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 8dp grid. */
class Spacing(
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 16.dp,
    val l: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val gutter: Dp = 20.dp,
    val card: Dp = 20.dp,
    val gap: Dp = 12.dp,
    val section: Dp = 24.dp,
    val minTouch: Dp = 48.dp,
    val button: Dp = 56.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
