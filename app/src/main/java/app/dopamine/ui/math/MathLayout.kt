package app.dopamine.ui.math

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import kotlin.math.max

/** A laid-out piece of math: size above/below the baseline, and how to draw it at (x, baselineY). */
class MBox(val w: Float, val asc: Float, val desc: Float, val draw: (Canvas, Float, Float) -> Unit)

/** Measures and draws a [MathNode] tree on an Android Canvas. Vector-style, so no bitmap cache is needed. */
class MathLayout(private val face: Typeface?, private val color: Int) {

    private fun paint(size: Float, italic: Boolean) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = this@MathLayout.color
        textSize = size
        typeface = face
        if (italic) textSkewX = -0.2f
    }

    fun layout(n: MathNode, s: Float): MBox = when (n) {
        is MathNode.Sym -> sym(n, s)
        is MathNode.Row -> row(n, s)
        is MathNode.Frac -> frac(n, s)
        is MathNode.Sqrt -> sqrt(n, s)
        is MathNode.Script -> script(n, s)
    }

    private fun sym(n: MathNode.Sym, s: Float): MBox {
        if (n.text.isEmpty()) return MBox(0f, 0f, 0f) { _, _, _ -> }
        val p = paint(s, n.kind == Kind.VAR)
        val w = p.measureText(n.text) + if (n.kind == Kind.VAR) s * 0.05f else 0f
        val r = Rect()
        p.getTextBounds(n.text, 0, n.text.length, r)
        val asc = max(-r.top.toFloat(), s * 0.5f)
        val desc = max(r.bottom.toFloat(), 0f)
        return MBox(w, asc, desc) { c, x, y -> c.drawText(n.text, x, y, p) }
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
        return MBox(total, asc, desc) { c, ox, oy -> parts.forEach { (dx, b) -> b.draw(c, ox + dx, oy) } }
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
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = this@MathLayout.color; style = Paint.Style.FILL }
        return MBox(w, asc, desc) { c, x, y ->
            num.draw(c, x + (w - num.w) / 2, y - numBase)
            den.draw(c, x + (w - den.w) / 2, y - denBase)
            c.drawRect(x + pad / 2, y - axis - bar / 2, x + w - pad / 2, y - axis + bar / 2, line)
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
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = this@MathLayout.color
            style = Paint.Style.STROKE
            strokeWidth = thick
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        return MBox(w, asc, body.desc) { c, x, y ->
            val ox = x + indexW
            val top = y - asc + thick / 2
            val path = Path().apply {
                moveTo(ox + s * 0.02f, y - s * 0.28f)
                lineTo(ox + s * 0.16f, y - s * 0.34f)
                lineTo(ox + s * 0.34f, y + body.desc)
                lineTo(ox + hook - s * 0.02f, top)
                lineTo(x + w, top)
            }
            c.drawPath(path, stroke)
            index?.draw(c, x, y - asc * 0.45f)
            body.draw(c, ox + hook, y)
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
        return MBox(w, asc, desc) { c, x, y ->
            base.draw(c, x, y)
            sup?.draw(c, x + base.w + s * 0.02f, y - supShift)
            sub?.draw(c, x + base.w + s * 0.02f, y + subShift)
        }
    }
}
