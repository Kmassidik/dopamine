package app.dopamine.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.dopamine.shared.R
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect

private val fonts = DopamineFonts(
    fredoka = FontFamily(Font(R.font.fredoka_medium, FontWeight.Medium), Font(R.font.fredoka_bold, FontWeight.Bold)),
    nunito = FontFamily(
        Font(R.font.nunito_regular, FontWeight.Normal),
        Font(R.font.nunito_semibold, FontWeight.SemiBold),
        Font(R.font.nunito_bold, FontWeight.Bold),
    ),
    mono = FontFamily(Font(R.font.jetbrains_mono_regular, FontWeight.Normal), Font(R.font.jetbrains_mono_bold, FontWeight.Bold)),
)

@Composable
actual fun rememberDopamineFonts(): DopamineFonts = fonts

@Composable
actual fun PlatformPreviewSetup() {
    if (LocalInspectionMode.current) PreviewContextConfigurationEffect()
}
