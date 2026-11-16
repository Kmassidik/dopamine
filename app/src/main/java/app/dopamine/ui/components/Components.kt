package app.dopamine.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import app.dopamine.ui.theme.LocalReducedMotion
import app.dopamine.ui.theme.Motion
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
import app.dopamine.ui.theme.Dopamine
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import app.dopamine.R
import kotlinx.coroutines.delay
import app.dopamine.ui.theme.ButtonShape
import app.dopamine.ui.theme.PillShape

@DrawableRes
fun iconFor(name: String): Int = when (name) {
    "ic_math" -> R.drawable.ic_math
    "ic_science" -> R.drawable.ic_test
    "ic_english" -> R.drawable.ic_english
    "ic_swe" -> R.drawable.ic_swe
    "ic_electronics" -> R.drawable.ic_electronics
    else -> R.drawable.ic_english // unknown names fall back to a default icon
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
fun StatChip(@DrawableRes icon: Int, value: String, fill: Color, textColor: Color, description: String) {
    Row(
        Modifier.clip(PillShape).background(fill).heightIn(min = 40.dp).padding(start = 8.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconImage(icon, size = 28.dp, description = description, bob = false)
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

/**
 * An illustrated icon. Bobs gently for [BOB_CYCLES] cycles after it appears, then rests, so an idle screen draws
 * no frames (battery). Skipped entirely when the system asks for reduced motion.
 */
@Composable
fun IconImage(@DrawableRes id: Int, modifier: Modifier = Modifier, size: Dp = 48.dp, description: String? = null, bob: Boolean = true) {
    val reduced = LocalReducedMotion.current
    val animated = if (bob && !reduced) {
        val y = remember(id) { Animatable(0f) }
        LaunchedEffect(id) {
            delay((id % 9) * 130L)
            val spec = tween<Float>(1100 + (id % 5) * 90, easing = Motion.EaseInOutSoft)
            repeat(BOB_CYCLES) {
                y.animateTo(1f, spec)
                y.animateTo(0f, spec)
            }
        }
        modifier.graphicsLayer {
            translationY = -3.dp.toPx() * y.value
            rotationZ = y.value * 2f
        }
    } else {
        modifier
    }
    Image(painterResource(id), description, animated.size(size))
}

private const val BOB_CYCLES = 2

/** Clickable with a quick squash on press. No squash when reduced motion is on. */
fun Modifier.bounceClick(onClickLabel: String? = null, role: Role? = Role.Button, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val scale by animateFloatAsState(
        if (pressed && !reduced) 0.95f else 1f,
        spring(dampingRatio = 0.45f, stiffness = 600f),
        label = "press",
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, onClickLabel = onClickLabel, role = role, onClick = onClick)
}

/** A soft banner for a result: green for success, pink for a problem. */
@Composable
fun Message(bg: Color, fg: Color, icon: Int, text: String, dismissLabel: String, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(app.dopamine.ui.theme.CardShape).background(bg).padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(icon, fg, size = 24.dp)
        Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp))
        androidx.compose.material3.TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(dismissLabel, color = fg)
        }
    }
}


/** Marks a title so screen-reader users can jump between headings. */
fun Modifier.heading(): Modifier = semantics { heading() }

/** The arrow at the top-left of every inner screen. 48dp, a real button, read as [description]. */
@Composable
fun BackButton(description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(48.dp)
            .clip(CircleShape)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { AppIcon(R.drawable.ic_back, Dopamine.colors.ink) }
}
