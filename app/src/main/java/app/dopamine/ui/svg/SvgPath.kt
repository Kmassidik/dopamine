package app.dopamine.ui.svg

private val TOKEN = Regex("""\s*,?\s*(?:([A-Za-z])|([-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?))""")

/**
 * Parses path data `d` (only M m L l H h V v C c Q q Z z) into absolute segments.
 * Returns null for anything unreadable, outside the subset, or too long.
 */
fun parsePath(d: String): List<PathSeg>? {
    val tokens = ArrayList<Any>() // Char or Float
    var pos = 0
    while (pos < d.length) {
        if (d.substring(pos).isBlank()) break
        val m = TOKEN.matchAt(d, pos) ?: return null
        val letter = m.groups[1]?.value
        val num = m.groups[2]?.value
        if (letter != null) {
            if (letter[0] !in SvgSubset.PATH_COMMANDS) return null
            tokens.add(letter[0])
        } else {
            tokens.add(num!!.toFloatOrNull() ?: return null)
        }
        pos = m.range.last + 1
    }
    val segs = ArrayList<PathSeg>()
    var i = 0
    var cx = 0f
    var cy = 0f
    var sx = 0f
    var sy = 0f
    var cmd: Char? = null
    fun num(): Float? = (tokens.getOrNull(i) as? Float)?.also { i++ }
    while (i < tokens.size) {
        val t = tokens[i]
        if (t is Char) { cmd = t; i++ } else if (cmd == null) return null
        val c = cmd!!
        val rel = c.isLowerCase()
        when (c.uppercaseChar()) {
            'Z' -> { segs.add(PathSeg.Close); cx = sx; cy = sy; if (tokens.getOrNull(i) is Float) return null }
            'M' -> {
                if (tokens.getOrNull(i) !is Float) return null
                var first = true
                while (tokens.getOrNull(i) is Float) {
                    val x = num() ?: return null
                    val y = num() ?: return null
                    val ax = if (rel) cx + x else x
                    val ay = if (rel) cy + y else y
                    if (first) { segs.add(PathSeg.Move(ax, ay)); sx = ax; sy = ay; first = false } else segs.add(PathSeg.Line(ax, ay))
                    cx = ax; cy = ay
                }
            }
            'L' -> {
                if (tokens.getOrNull(i) !is Float) return null
                while (tokens.getOrNull(i) is Float) {
                    val x = num() ?: return null
                    val y = num() ?: return null
                    cx = if (rel) cx + x else x
                    cy = if (rel) cy + y else y
                    segs.add(PathSeg.Line(cx, cy))
                }
            }
            'H' -> {
                if (tokens.getOrNull(i) !is Float) return null
                while (tokens.getOrNull(i) is Float) { val x = num()!!; cx = if (rel) cx + x else x; segs.add(PathSeg.Line(cx, cy)) }
            }
            'V' -> {
                if (tokens.getOrNull(i) !is Float) return null
                while (tokens.getOrNull(i) is Float) { val y = num()!!; cy = if (rel) cy + y else y; segs.add(PathSeg.Line(cx, cy)) }
            }
            'Q' -> {
                if (tokens.getOrNull(i) !is Float) return null
                while (tokens.getOrNull(i) is Float) {
                    val a = num() ?: return null; val b = num() ?: return null
                    val x = num() ?: return null; val y = num() ?: return null
                    val ox = if (rel) cx else 0f; val oy = if (rel) cy else 0f
                    segs.add(PathSeg.Quad(ox + a, oy + b, ox + x, oy + y)); cx = ox + x; cy = oy + y
                }
            }
            'C' -> {
                if (tokens.getOrNull(i) !is Float) return null
                while (tokens.getOrNull(i) is Float) {
                    val a = num() ?: return null; val b = num() ?: return null
                    val c2 = num() ?: return null; val d2 = num() ?: return null
                    val x = num() ?: return null; val y = num() ?: return null
                    val ox = if (rel) cx else 0f; val oy = if (rel) cy else 0f
                    segs.add(PathSeg.Cubic(ox + a, oy + b, ox + c2, oy + d2, ox + x, oy + y)); cx = ox + x; cy = oy + y
                }
            }
        }
        if (segs.size > SvgSubset.MAX_PATH_SEGMENTS) return null
    }
    // A path must start with a move.
    return if (segs.isNotEmpty() && segs.first() is PathSeg.Move) segs else null
}
