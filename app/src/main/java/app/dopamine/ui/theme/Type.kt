package app.dopamine.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.dopamine.R

val ZenMaru = FontFamily(
    Font(R.font.zen_maru_regular, FontWeight.Normal),
    Font(R.font.zen_maru_medium, FontWeight.Medium),
    Font(R.font.zen_maru_bold, FontWeight.Bold),
)

private fun style(size: Int, line: Int, weight: FontWeight, spacing: Double = 0.0) = TextStyle(
    fontFamily = ZenMaru,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp,
)

/** Display 32, title 22, subtitle 18, body 16, label 14, caption 12. */
val DopamineTypography = Typography(
    displayLarge = style(32, 40, FontWeight.Bold),
    displayMedium = style(32, 40, FontWeight.Bold),
    headlineMedium = style(22, 30, FontWeight.Bold),
    titleLarge = style(22, 30, FontWeight.Bold),
    titleMedium = style(18, 26, FontWeight.Medium),
    titleSmall = style(16, 24, FontWeight.Medium),
    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(14, 22, FontWeight.Normal),
    labelLarge = style(16, 22, FontWeight.Bold, 0.2),
    labelMedium = style(14, 20, FontWeight.Medium),
    labelSmall = style(12, 16, FontWeight.Medium, 0.2),
)
