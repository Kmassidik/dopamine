package app.dopamine.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.staticCompositionLocalOf

/** Motion tokens. Every animation reads LocalReducedMotion and degrades to an instant change. */
object Motion {
    const val FAST_MS = 120
    const val MEDIUM_MS = 240
    const val SLOW_MS = 400
    const val INK_WIPE_MS = 360
    val EaseOutBrush = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EaseInOutSoft = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
}

val LocalReducedMotion = staticCompositionLocalOf { false }
