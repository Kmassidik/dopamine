package app.dopamine.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.dopamine.R
import app.dopamine.ui.theme.ButtonShape
import app.dopamine.ui.theme.PillShape

@DrawableRes
fun iconFor(name: String): Int = when (name) {
    "ic_math" -> R.drawable.ic_math
    "ic_science" -> R.drawable.ic_science
    else -> R.drawable.ic_book // unknown names fall back to a default icon
}

@Composable
fun AppIcon(@DrawableRes id: Int, tint: Color, modifier: Modifier = Modifier, size: Dp = 24.dp, description: String? = null) {
    Icon(painterResource(id), contentDescription = description, tint = tint, modifier = modifier.size(size))
}

/** Chunky pill button with a darker "lip" underneath. Always at least 56dp tall. */
@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    lip: Color,
    content: Color,
    modifier: Modifier = Modifier,
    leadingIcon: Int? = null,
    enabled: Boolean = true,
) {
    val fade = if (enabled) 1f else 0.4f
    Box(modifier.padding(bottom = 4.dp)) {
        Box(Modifier.matchParentSize().offset(y = 4.dp).background(lip.copy(alpha = fade), ButtonShape))
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = ButtonShape,
            color = container.copy(alpha = fade),
            contentColor = content,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        ) {
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    AppIcon(leadingIcon, content, size = 20.dp)
                    Box(Modifier.width(8.dp))
                }
                Text(text, style = MaterialTheme.typography.labelLarge, color = content)
            }
        }
    }
}

@Composable
fun StatChip(@DrawableRes icon: Int, value: String, tint: Color, fill: Color, textColor: Color, description: String) {
    Row(
        Modifier.clip(PillShape).background(fill).heightIn(min = 40.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(icon, tint, size = 20.dp, description = description)
        Box(Modifier.width(6.dp))
        Text(value, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}

@Composable
fun ProgressRing(progress: Float, size: Dp, stroke: Dp, track: Color, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = stroke.toPx()
        val inset = s / 2
        val arcSize = Size(this.size.width - s, this.size.height - s)
        drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(s))
        if (progress > 0f) {
            drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, Offset(inset, inset), arcSize, style = Stroke(s, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun ProgressBar(progress: Float, track: Color, color: Color, modifier: Modifier = Modifier, height: Dp = 12.dp) {
    Box(modifier.fillMaxWidth().height(height).clip(PillShape).background(track)) {
        if (progress > 0f) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(progress.coerceIn(0.04f, 1f)).clip(PillShape).background(color))
        }
    }
}


/** Flat black pill, the primary action on the home screen. */
@Composable
fun InkButton(text: String, onClick: () -> Unit, container: Color, content: Color, modifier: Modifier = Modifier, leadingIcon: Int? = null) {
    Surface(
        onClick = onClick,
        shape = PillShape,
        color = container,
        contentColor = content,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                AppIcon(leadingIcon, content, size = 20.dp)
                Box(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}
