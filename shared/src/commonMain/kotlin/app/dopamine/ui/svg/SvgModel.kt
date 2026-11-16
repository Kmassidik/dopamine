package app.dopamine.ui.svg

/** Allowed building blocks of an image. Must equal the `svg` block of the content repo's contract.json (ContractTest). */
object SvgSubset {
    val ELEMENTS = setOf("circle", "ellipse", "g", "line", "path", "polygon", "polyline", "rect", "svg", "text")
    val ATTRIBUTES = setOf(
        "cx", "cy", "d", "fill", "font-size", "font-weight", "height", "id", "opacity", "points", "r", "rx", "ry",
        "stroke", "stroke-dasharray", "stroke-linecap", "stroke-linejoin", "stroke-width", "text-anchor",
        "width", "x", "x1", "x2", "y", "y1", "y2",
    )
    const val PATH_COMMANDS = "MmLlHhVvCcQqZz"
    val COLORS = setOf(
        "#14201a", "#17803f", "#22994f", "#35c46f", "#4f5d55", "#8a5f00", "#b8305f", "#d9a21b", "#db5a8a",
        "#e4eae6", "#e4f8ec", "#ff7fae", "#ffc93c", "#ffe4ee", "#fff3cc", "#ffffff", "currentColor", "none",
    )
    const val MAX_BYTES = 6144
    const val MAX_ELEMENTS = 80
    const val MAX_DEPTH = 4
    const val VIEWBOX_MIN = 20
    const val VIEWBOX_MAX = 2000
    const val FONT_SIZE_MIN = 10
    const val FONT_SIZE_MAX = 48
    const val MAX_PATH_SEGMENTS = 500
}

sealed interface SvgPaint {
    data object None : SvgPaint
    /** Drawn in the theme's ink colour. */
    data object Current : SvgPaint
    data class Rgb(val argb: Int) : SvgPaint
}

/** Inherited drawing state. A null fill or stroke means "not set". */
data class SvgStyle(
    val fill: SvgPaint? = null,
    val stroke: SvgPaint? = null,
    val strokeWidth: Float = 1f,
    val cap: String = "butt",
    val join: String = "miter",
    val dash: List<Float> = emptyList(),
    val opacity: Float = 1f,
    val fontSize: Float = 16f,
    val bold: Boolean = false,
    val anchor: String = "start",
)

sealed interface PathSeg {
    data class Move(val x: Float, val y: Float) : PathSeg
    data class Line(val x: Float, val y: Float) : PathSeg
    data class Quad(val x1: Float, val y1: Float, val x: Float, val y: Float) : PathSeg
    data class Cubic(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val x: Float, val y: Float) : PathSeg
    data object Close : PathSeg
}

sealed interface SvgShape {
    val style: SvgStyle
    data class Line(val x1: Float, val y1: Float, val x2: Float, val y2: Float, override val style: SvgStyle) : SvgShape
    data class Rect(val x: Float, val y: Float, val w: Float, val h: Float, val rx: Float, val ry: Float, override val style: SvgStyle) : SvgShape
    data class Circle(val cx: Float, val cy: Float, val r: Float, override val style: SvgStyle) : SvgShape
    data class Ellipse(val cx: Float, val cy: Float, val rx: Float, val ry: Float, override val style: SvgStyle) : SvgShape
    data class Poly(val points: List<Float>, val closed: Boolean, override val style: SvgStyle) : SvgShape
    data class Path(val segs: List<PathSeg>, override val style: SvgStyle) : SvgShape
    data class Text(val x: Float, val y: Float, val text: String, override val style: SvgStyle) : SvgShape
}

data class SvgDoc(val width: Float, val height: Float, val shapes: List<SvgShape>)
