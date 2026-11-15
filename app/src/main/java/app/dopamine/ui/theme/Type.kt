package app.dopamine.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.dopamine.R

/** Headings, numbers and buttons: chunky and playful. */
val Fredoka = FontFamily(
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_bold, FontWeight.Bold),
)

/** Body text and questions: round but calm, easy to read for minutes. */
val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

/** Code: monospace, for code blocks and `inline code`. */
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
)

private fun style(family: FontFamily, size: Int, line: Int, weight: FontWeight, spacing: Double = 0.0) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp,
)

/** Display 40, headline 28, title 24/18/16, body 16/14, label 17/13/12. */
val DopamineTypography = Typography(
    displayLarge = style(Fredoka, 40, 46, FontWeight.Bold),
    displayMedium = style(Fredoka, 32, 38, FontWeight.Bold),
    headlineMedium = style(Fredoka, 28, 34, FontWeight.Bold),
    titleLarge = style(Fredoka, 24, 30, FontWeight.Bold),
    titleMedium = style(Fredoka, 18, 24, FontWeight.Medium),
    titleSmall = style(Fredoka, 16, 22, FontWeight.Medium),
    bodyLarge = style(Nunito, 16, 24, FontWeight.Normal),
    bodyMedium = style(Nunito, 14, 21, FontWeight.Normal),
    labelLarge = style(Fredoka, 17, 22, FontWeight.Bold, 0.2),
    labelMedium = style(Nunito, 13, 18, FontWeight.SemiBold),
    labelSmall = style(Nunito, 12, 16, FontWeight.SemiBold, 0.2),
)
