package app.dopamine.ui.math

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import app.dopamine.R
import app.dopamine.ui.lesson.plainMath

private class Built(val text: AnnotatedString, val content: Map<String, InlineTextContent>, val descentPx: Float)

/**
 * Text with inline LaTeX: anything between `$ ... $` is drawn as math, the rest as normal text.
 * With no `$` it is just a [Text]. The math sits on the text baseline and wraps with the line.
 */
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    if (!text.contains('$')) {
        Text(text, modifier, color = color, style = style, textAlign = textAlign)
        return
    }
    val context = LocalContext.current
    val density = LocalDensity.current
    val face = remember { ResourcesCompat.getFont(context, R.font.nunito_semibold) }
    val ink = if (color == Color.Unspecified) LocalContentColor.current else color
    val sizePx = with(density) { (if (style.fontSize.isSpecified) style.fontSize else 16.sp).toPx() }
    val built = remember(text, sizePx, ink) { build(text, sizePx, face, ink.toArgb(), density) }
    // Math can hang below the baseline (fraction denominators); the line height does not know, so make room.
    val room = with(density) { built.descentPx.toDp() }
    Text(built.text, modifier.padding(bottom = room), color = ink, style = style, textAlign = textAlign, inlineContent = built.content)
}

private fun build(text: String, sizePx: Float, face: android.graphics.Typeface?, argb: Int, density: Density): Built {
    val layout = MathLayout(face, argb)
    val parts = text.split('$')
    val content = HashMap<String, InlineTextContent>()
    var descent = 0f
    val annotated = buildAnnotatedString {
        parts.forEachIndexed { i, part ->
            val isMath = i % 2 == 1 && i < parts.size - 1
            if (!isMath) {
                // An unclosed `$` leaves its tail as plain text.
                append(if (i % 2 == 1) "$part" else part)
                return@forEachIndexed
            }
            val box = layout.layout(MathParser.parse(part), sizePx)
            val id = "m$i"
            descent = maxOf(descent, box.desc)
            val (w, h) = with(density) { box.w.toSp() to box.asc.toSp() }
            content[id] = InlineTextContent(Placeholder(w, h, PlaceholderVerticalAlign.AboveBaseline)) {
                Box(
                    Modifier.fillMaxSize().drawBehind {
                        drawIntoCanvas { box.draw(it.nativeCanvas, 0f, box.asc) }
                    },
                )
            }
            appendInlineContent(id, plainMath(part))
        }
    }
    return Built(annotated, content, descent)
}
