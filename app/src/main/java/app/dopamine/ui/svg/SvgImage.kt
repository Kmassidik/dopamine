package app.dopamine.ui.svg

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import app.dopamine.R
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine

/** How the app loads and parses an image by its content path. Provided once, near the top of the navigation. */
val LocalSvgLoader = staticCompositionLocalOf<suspend (String) -> SvgDoc?> { { null } }

/**
 * A picture from the content repo, drawn natively. [alt] is what a screen reader says; when the
 * file cannot be read, the description is shown as text instead so nothing is lost.
 */
@Composable
fun SvgImage(path: String, alt: String, modifier: Modifier = Modifier) {
    val loader = LocalSvgLoader.current
    val state by produceState<Result?>(null, path) { value = Result(loader(path)) }
    val doc = state?.doc
    when {
        state == null -> Unit
        doc == null -> {
            val c = Dopamine.colors
            Box(modifier.fillMaxWidth().clip(CardShape).background(c.line).padding(16.dp)) {
                Text(alt, style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
            }
        }
        else -> SvgDocImage(doc, alt, modifier)
    }
}

private class Result(val doc: SvgDoc?)

/** Draws an already-parsed image (used by SvgImage, and directly by previews and tests). */
@Composable
fun SvgDocImage(doc: SvgDoc, alt: String, modifier: Modifier = Modifier) {
    val ink = Dopamine.colors.ink
    val context = LocalContext.current
    val regular = remember { ResourcesCompat.getFont(context, R.font.nunito_semibold) }
    val bold = remember { ResourcesCompat.getFont(context, R.font.nunito_bold) }
    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(doc.width / doc.height)
            .semantics {
                contentDescription = alt
                role = Role.Image
            },
    ) {
        val scale = size.width / doc.width
        for (shape in doc.shapes) drawShape(shape, scale, ink, regular, bold)
    }
}

private fun color(p: SvgPaint?, ink: Color, opacity: Float): Color? = when (p) {
    null, SvgPaint.None -> null
    SvgPaint.Current -> ink.copy(alpha = opacity)
    is SvgPaint.Rgb -> Color(p.argb).copy(alpha = opacity)
}

private fun stroke(st: SvgStyle, scale: Float) = Stroke(
    width = st.strokeWidth * scale,
    cap = when (st.cap) { "round" -> StrokeCap.Round; "square" -> StrokeCap.Square; else -> StrokeCap.Butt },
    join = when (st.join) { "round" -> StrokeJoin.Round; "bevel" -> StrokeJoin.Bevel; else -> StrokeJoin.Miter },
    pathEffect = if (st.dash.isEmpty()) null else PathEffect.dashPathEffect(FloatArray(if (st.dash.size % 2 == 0) st.dash.size else st.dash.size * 2) { st.dash[it % st.dash.size] * scale }),
)

private fun DrawScope.drawShape(shape: SvgShape, s: Float, ink: Color, regular: android.graphics.Typeface?, bold: android.graphics.Typeface?) {
    val st = shape.style
    val fill = color(st.fill, ink, st.opacity)
    val line = color(st.stroke, ink, st.opacity)
    val strokeStyle = stroke(st, s)
    when (shape) {
        is SvgShape.Line -> line?.let { drawLine(it, Offset(shape.x1 * s, shape.y1 * s), Offset(shape.x2 * s, shape.y2 * s), strokeStyle.width, strokeStyle.cap, strokeStyle.pathEffect) }
        is SvgShape.Rect -> {
            val tl = Offset(shape.x * s, shape.y * s)
            val sz = Size(shape.w * s, shape.h * s)
            val r = CornerRadius(shape.rx * s, (if (shape.ry > 0f) shape.ry else shape.rx) * s)
            fill?.let { drawRoundRect(it, tl, sz, r, Fill) }
            line?.let { drawRoundRect(it, tl, sz, r, strokeStyle) }
        }
        is SvgShape.Circle -> {
            fill?.let { drawCircle(it, shape.r * s, Offset(shape.cx * s, shape.cy * s), style = Fill) }
            line?.let { drawCircle(it, shape.r * s, Offset(shape.cx * s, shape.cy * s), style = strokeStyle) }
        }
        is SvgShape.Ellipse -> {
            val tl = Offset((shape.cx - shape.rx) * s, (shape.cy - shape.ry) * s)
            val sz = Size(shape.rx * 2 * s, shape.ry * 2 * s)
            fill?.let { drawOval(it, tl, sz, style = Fill) }
            line?.let { drawOval(it, tl, sz, style = strokeStyle) }
        }
        is SvgShape.Poly -> {
            val p = Path().apply {
                moveTo(shape.points[0] * s, shape.points[1] * s)
                for (k in 2 until shape.points.size step 2) lineTo(shape.points[k] * s, shape.points[k + 1] * s)
                if (shape.closed) close()
            }
            fill?.let { drawPath(p, it, style = Fill) }
            line?.let { drawPath(p, it, style = strokeStyle) }
        }
        is SvgShape.Path -> {
            val p = Path().apply {
                for (seg in shape.segs) when (seg) {
                    is PathSeg.Move -> moveTo(seg.x * s, seg.y * s)
                    is PathSeg.Line -> lineTo(seg.x * s, seg.y * s)
                    is PathSeg.Quad -> quadraticTo(seg.x1 * s, seg.y1 * s, seg.x * s, seg.y * s)
                    is PathSeg.Cubic -> cubicTo(seg.x1 * s, seg.y1 * s, seg.x2 * s, seg.y2 * s, seg.x * s, seg.y * s)
                    PathSeg.Close -> close()
                }
            }
            fill?.let { drawPath(p, it, style = Fill) }
            line?.let { drawPath(p, it, style = strokeStyle) }
        }
        is SvgShape.Text -> fill?.let { c ->
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = c.toArgb()
                    textSize = st.fontSize * s
                    typeface = if (st.bold) bold else regular
                    textAlign = when (st.anchor) {
                        "middle" -> android.graphics.Paint.Align.CENTER
                        "end" -> android.graphics.Paint.Align.RIGHT
                        else -> android.graphics.Paint.Align.LEFT
                    }
                }
                canvas.nativeCanvas.drawText(shape.text, shape.x * s, shape.y * s, paint)
            }
        }
    }
}
