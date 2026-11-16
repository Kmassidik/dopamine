package app.dopamine.ui.theme

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
actual fun systemReducedMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled()
