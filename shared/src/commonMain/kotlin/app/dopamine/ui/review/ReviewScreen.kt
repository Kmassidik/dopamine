package app.dopamine.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.dopamine.shared.resources.*
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.theme.Dopamine

/** Placeholder until spaced review exists. Says so plainly. */
@Composable
fun ReviewScreen() {
    val c = Dopamine.colors
    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            IconImage(Res.drawable.mascot_sleep, size = 140.dp)
            Text(stringResource(Res.string.review_empty_title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
            Text(stringResource(Res.string.review_empty_body), style = MaterialTheme.typography.bodyLarge, color = c.inkSoft, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
