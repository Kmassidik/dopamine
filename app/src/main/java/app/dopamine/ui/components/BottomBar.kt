package app.dopamine.ui.components

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.dopamine.R
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.PillShape

enum class Tab(val route: String, @DrawableRes val icon: Int, @StringRes val label: Int) {
    Home("home", R.drawable.ic_home, R.string.tab_home),
    Review("review", R.drawable.ic_review, R.string.tab_review),
    Settings("settings", R.drawable.ic_settings, R.string.tab_settings),
}

/**
 * The bottom bar: frosted glass that runs to the very bottom of the screen, behind the phone's own navigation
 * buttons. A blurred copy of the content behind it (Android 12+; [backdrop] is the recorded screen content) sits
 * under a light white tint, with a hairline on top. Older phones, and previews, get a more opaque tint instead.
 * Three equal tabs with icon and label; the selected one is a yellow chip.
 */
@Composable
fun BottomBar(selected: Tab?, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier, backdrop: GraphicsLayer? = null) {
    val c = Dopamine.colors
    var top by remember { mutableFloatStateOf(0f) }
    val blur = backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    Box(modifier.fillMaxWidth().onGloballyPositioned { top = it.positionInRoot().y }) {
        if (blur) {
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        clip = true
                        renderEffect = BlurEffect(36f, 36f, TileMode.Clamp)
                    }
                    .drawBehind { translate(0f, -top) { drawLayer(backdrop!!) } },
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = if (blur) 0.58f else 0.94f))
                .drawBehind { drawLine(Color.Black.copy(alpha = 0.10f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx()) },
        )
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tab.entries.forEach { tab ->
                val on = tab == selected
                Column(
                    Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (on) c.yellow else Color.Transparent)
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(tab) })
                        .heightIn(min = 56.dp)
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    IconImage(tab.icon, size = 28.dp, bob = false)
                    Text(
                        stringResource(tab.label),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (on) c.onAccent else c.ink.copy(alpha = 0.78f),
                    )
                }
            }
        }
    }
}
