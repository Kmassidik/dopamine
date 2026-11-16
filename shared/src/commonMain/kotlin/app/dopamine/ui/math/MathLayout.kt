package app.dopamine.ui.math

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import kotlin.math.max

/** A laid-out piece of math: size above/below the baseline, and how to draw it at (x, baselineY). */
class MBox(val w: Float, val asc: Float, val desc: Float, val draw: DrawScope.(Float, Float) -> Unit)

/** Measures and draws a [MathNode] tree with Compose text measuring and a DrawScope. Vector-style, so no bitmap cache is needed. */
class MathLayout(
    private val measurer: TextMeasurer,
    private val family: FontFamily,
    private val color: Color,
    private val density: Density,
) {

    private fun style(size: Float) = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.SemiBold,
        fontSize = with(density) { size.toSp() },
        color = color,
    )

    /** Compose cannot report the ink bounds of text, so the height above the baseline is estimated from the glyphs. */
    private fun inkAscent(t: String, s: Float): Float =
        if (t.any { it.isUpperCase() || it.isDigit() || it in TALL || it.code > 127 }) s * 0.72f else s * 0.49f

    private fun inkDescent(t: String, s: Float): Float = if (t.any { it in DEEP }) s * 0.2f else 0f

    fun layout(n: MathNode, s: Float): MBox = when (n) {
        is MathNode.Sym -> sym(n, s)
        is MathNode.Row -> row(n, s)
        is MathNode.Frac -> frac(n, s)
        is MathNode.Sqrt -> sqrt(n, s)
        is MathNode.Script -> script(n, s)
    }

    private fun sym(n: MathNode.Sym, s: Float): MBox {
        if (n.text.isEmpty()) return MBox(0f, 0f, 0f) { _, _ -> }
        val italic = n.kind == Kind.VAR
        val layout = measurer.measure(n.text, style(s))
        val w = layout.getLineRight(0) + if (italic) s * 0.05f else 0f
        val asc = max(inkAscent(n.text, s), s * 0.5f)
        val desc = inkDescent(n.text, s)
        val baseline = layout.firstBaseline
        return MBox(w, asc, desc) { x, y ->
            if (italic) {
                // Slanted like the old Paint.textSkewX = -0.2.
                val skew = Matrix().apply { this[0, 1] = -0.2f }
                withTransform({ translate(x, y); transform(skew) }) { drawText(layout, topLeft = Offset(0f, -baseline)) }
            } else {
                drawText(layout, topLeft = Offset(x, y - baseline))
            }
        }
    }

    private fun row(n: MathNode.Row, s: Float): MBox {
        val parts = ArrayList<Pair<Float, MBox>>()
        var x = 0f
        var asc = 0f
        var desc = 0f
        var prev: Kind? = null
        for (item in n.items) {
            val kind = (item as? MathNode.Sym)?.kind
            val unary = prev == null || prev == Kind.OP || prev == Kind.REL || prev == Kind.OPEN
            val pad = when {
                kind == Kind.REL -> s * 0.28f
                kind == Kind.OP && !unary -> s * 0.2f
                else -> 0f
            }
            x += pad
            val b = layout(item, s)
            parts.add(x to b)
            x += b.w + pad
            asc = max(asc, b.asc)
            desc = max(desc, b.desc)
            prev = kind ?: Kind.TEXT
        }
        val total = x
        return MBox(total, asc, desc) { ox, oy -> parts.forEach { (dx, b) -> with(b) { draw(ox + dx, oy) } } }
    }

    private fun frac(n: MathNode.Frac, s: Float): MBox {
        val sub = s * 0.88f
        val num = layout(n.num, sub)
        val den = layout(n.den, sub)
        val axis = s * 0.27f
        val bar = max(1.5f, s * 0.07f)
        val pad = s * 0.12f
        val gap = s * 0.14f
        val w = max(num.w, den.w) + 2 * pad
        val numBase = axis + bar / 2 + gap + num.desc
        val denBase = -(den.asc + gap + bar / 2 - axis)
        val asc = numBase + num.asc
        val desc = -denBase + den.desc
        return MBox(w, asc, desc) { x, y ->
            with(num) { draw(x + (w - num.w) / 2, y - numBase) }
            with(den) { draw(x + (w - den.w) / 2, y - denBase) }
            drawRect(color, Offset(x + pad / 2, y - axis - bar / 2), androidx.compose.ui.geometry.Size(w - pad, bar))
        }
    }

    private fun sqrt(n: MathNode.Sqrt, s: Float): MBox {
        val body = layout(n.body, s)
        val gap = s * 0.12f
        val thick = max(1.5f, s * 0.06f)
        val hook = s * 0.55f
        val asc = body.asc + gap + thick
        val index = n.index?.let { layout(it, s * 0.55f) }
        val indexW = index?.w ?: 0f
        val w = indexW + hook + body.w + s * 0.1f
        val stroke = Stroke(width = thick, cap = StrokeCap.Round, join = StrokeJoin.Round)
        return MBox(w, asc, body.desc) { x, y ->
            val ox = x + indexW
            val top = y - asc + thick / 2
            val path = Path().apply {
                moveTo(ox + s * 0.02f, y - s * 0.28f)
                lineTo(ox + s * 0.16f, y - s * 0.34f)
                lineTo(ox + s * 0.34f, y + body.desc)
                lineTo(ox + hook - s * 0.02f, top)
                lineTo(x + w, top)
            }
            drawPath(path, color, style = stroke)
            index?.let { with(it) { draw(x, y - asc * 0.45f) } }
            with(body) { draw(ox + hook, y) }
        }
    }

    private fun script(n: MathNode.Script, s: Float): MBox {
        val base = layout(n.base, s)
        val small = s * 0.7f
        val sup = n.sup?.let { layout(it, small) }
        val sub = n.sub?.let { layout(it, small) }
        val supShift = max(s * 0.36f, base.asc * 0.62f)
        val subShift = s * 0.22f
        val sw = max(sup?.w ?: 0f, sub?.w ?: 0f)
        val w = base.w + sw + s * 0.04f
        val asc = max(base.asc, (sup?.let { supShift + it.asc } ?: 0f))
        val desc = max(base.desc, (sub?.let { subShift + it.desc } ?: 0f))
        return MBox(w, asc, desc) { x, y ->
            with(base) { draw(x, y) }
            sup?.let { with(it) { draw(x + base.w + s * 0.02f, y - supShift) } }
            sub?.let { with(it) { draw(x + base.w + s * 0.02f, y + subShift) } }
        }
    }
}

private const val TALL = "bdfhijklt()[]{}|/\\!?\u2022"
private const val DEEP = "gjpqy,;()[]{}|/"
