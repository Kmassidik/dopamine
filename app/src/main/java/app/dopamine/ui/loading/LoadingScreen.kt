package app.dopamine.ui.loading

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.dopamine.R
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.LocalReducedMotion

/** Shown while the profile loads: the Bot on a yellow disc, the name, and three bouncing dots. */
@Composable
fun LoadingScreen() {
    val c = Dopamine.colors
    val reduced = LocalReducedMotion.current
    Box(Modifier.fillMaxSize().background(c.bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(168.dp).clip(CircleShape).background(c.yellow), contentAlignment = Alignment.Center) {
                IconImage(R.drawable.mascot_wave, size = 132.dp)
            }
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displayLarge, color = c.ink, modifier = Modifier.padding(top = 24.dp))
            Text(stringResource(R.string.loading_tagline), style = MaterialTheme.typography.bodyLarge, color = c.inkSoft, modifier = Modifier.padding(top = 4.dp))
            Row(Modifier.padding(top = 32.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(c.green, c.yellow, c.pink).forEachIndexed { i, color -> Dot(color, i, reduced) }
            }
        }
    }
}

@Composable
private fun Dot(color: Color, index: Int, reduced: Boolean) {
    val y = if (reduced) 0f else rememberInfiniteTransition(label = "dots").animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(450), RepeatMode.Reverse, StartOffset(index * 150)),
        label = "dot",
    ).value
    Box(
        Modifier
            .size(14.dp)
            .graphicsLayer { translationY = -10.dp.toPx() * y }
            .clip(CircleShape)
            .background(color),
    )
}
