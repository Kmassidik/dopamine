package app.dopamine.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.dopamine.shared.resources.*
import org.jetbrains.compose.resources.Font

/** Headings, numbers and buttons: chunky and playful. */
@Composable fun fredoka(): FontFamily = FontFamily(
    Font(Res.font.fredoka_medium, FontWeight.Medium),
    Font(Res.font.fredoka_bold, FontWeight.Bold),
)

/** Body text and questions: round but calm, easy to read for minutes. */
@Composable fun nunito(): FontFamily = FontFamily(
    Font(Res.font.nunito_regular, FontWeight.Normal),
    Font(Res.font.nunito_semibold, FontWeight.SemiBold),
    Font(Res.font.nunito_bold, FontWeight.Bold),
)

/** Code: monospace, for code blocks and `inline code`. */
@Composable fun jetBrainsMono(): FontFamily = FontFamily(
    Font(Res.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(Res.font.jetbrains_mono_bold, FontWeight.Bold),
)

private fun style(family: FontFamily, size: Int, line: Int, weight: FontWeight, spacing: Double = 0.0) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp,
)

/** Display 40, headline 28, title 24/18/16, body 16/14, label 17/13/12. */
@Composable fun dopamineTypography(): Typography {
    val fredoka = fredoka()
    val nunito = nunito()
    return remember(fredoka, nunito) { buildTypography(fredoka, nunito) }
}

private fun buildTypography(Fredoka: FontFamily, Nunito: FontFamily) = Typography(
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
