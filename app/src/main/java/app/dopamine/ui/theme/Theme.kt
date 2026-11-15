package app.dopamine.ui.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalDopamineColors = staticCompositionLocalOf { LightDopamineColors }

object Dopamine {
    val colors: DopamineColors
        @Composable @ReadOnlyComposable get() = LocalDopamineColors.current
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
}

@Composable
fun DopamineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    // Honor the system "remove animations" setting (animator scale 0).
    val systemReduced = Settings.Global.getFloat(
        context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
    ) == 0f

    CompositionLocalProvider(
        LocalDopamineColors provides if (darkTheme) DarkDopamineColors else LightDopamineColors,
        LocalSpacing provides Spacing(),
        LocalReducedMotion provides systemReduced,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = DopamineTypography,
            shapes = DopamineShapes,
            content = content,
        )
    }
}
