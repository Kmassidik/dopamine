package app.dopamine.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.dopamine.shared.resources.*
import org.jetbrains.compose.resources.Font

@Composable
actual fun rememberDopamineFonts(): DopamineFonts {
    val fredoka = FontFamily(Font(Res.font.fredoka_medium, FontWeight.Medium), Font(Res.font.fredoka_bold, FontWeight.Bold))
    val nunito = FontFamily(
        Font(Res.font.nunito_regular, FontWeight.Normal),
        Font(Res.font.nunito_semibold, FontWeight.SemiBold),
        Font(Res.font.nunito_bold, FontWeight.Bold),
    )
    val mono = FontFamily(Font(Res.font.jetbrains_mono_regular, FontWeight.Normal), Font(Res.font.jetbrains_mono_bold, FontWeight.Bold))
    return remember(fredoka, nunito, mono) { DopamineFonts(fredoka, nunito, mono) }
}

@Composable
actual fun PlatformPreviewSetup() {}
