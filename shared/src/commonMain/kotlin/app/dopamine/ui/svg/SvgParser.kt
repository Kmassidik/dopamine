package app.dopamine.ui.svg

private class Reject(msg: String) : Exception(msg)

private val NUM = Regex("""[-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?""")
private val NUMS = Regex("""\s*$NUM(?:[\s,]+$NUM)*\s*""")
private val NUMERIC = setOf("x", "y", "x1", "y1", "x2", "y2", "cx", "cy", "r", "rx", "ry", "width", "height", "stroke-width")
private val FILLED = setOf("rect", "circle", "ellipse", "polyline", "polygon", "path", "text")
private val ENTITY = Regex("&([A-Za-z0-9#]*);")
private val BARE_AMP = Regex("&(?![A-Za-z0-9#]*;)")
private val CAPS = setOf("butt", "round", "square")
private val JOINS = setOf("miter", "round", "bevel")

/**
 * Reads an image in the strict subset the content repo allows (see SvgSubset, tools/svgcheck.py there).
 * Anything outside the subset makes it return null: the app then shows the image's description instead.
 */
object SvgParser {
    fun parse(src: String): SvgDoc? = try {
        Reader(src).document()
    } catch (e: Reject) {
        null
    } catch (e: NumberFormatException) {
        null
    }

    private class Reader(private val s: String) {
        private var i = 0
        private var count = 0
        private val shapes = ArrayList<SvgShape>()
        private var width = 0f
        private var height = 0f

        init {
            if (s.encodeToByteArray().size > SvgSubset.MAX_BYTES) reject("too large")
            val low = s.lowercase()
            if ("<!doctype" in low || "<!entity" in low) reject("doctype")
        }

        private fun reject(why: String): Nothing = throw Reject(why)

        fun document(): SvgDoc {
            skipMisc(allowDeclaration = true)
            if (!s.startsWith("<", i)) reject("no root")
            element(1, SvgStyle(), root = true)
            skipMisc(allowDeclaration = false)
            if (i < s.length) reject("trailing content")
            if (width <= 0f) reject("no viewBox")
            return SvgDoc(width, height, shapes)
        }

        private fun skipWs() { while (i < s.length && s[i].isWhitespace()) i++ }

        private fun skipMisc(allowDeclaration: Boolean) {
            var declaration = allowDeclaration
            while (true) {
                skipWs()
                when {
                    s.startsWith("<?xml", i) && declaration -> {
                        val end = s.indexOf("?>", i); if (end < 0) reject("bad declaration"); i = end + 2; declaration = false
                    }
                    s.startsWith("<?", i) -> reject("processing instruction")
                    s.startsWith("<!--", i) -> {
                        val end = s.indexOf("-->", i); if (end < 0) reject("bad comment"); i = end + 3
                    }
                    else -> return
                }
            }
        }

        private fun name(): String {
            val st = i
            while (i < s.length && (s[i].isLetterOrDigit() || s[i] == '-' || s[i] == '_' || s[i] == ':')) i++
            if (i == st) reject("bad name")
            return s.substring(st, i)
        }

        private fun decode(v: String): String {
            if ('&' !in v) return v
            if (BARE_AMP.containsMatchIn(v)) reject("bad entity")
            return ENTITY.replace(v) {
                when (it.groupValues[1]) {
                    "lt" -> "<"; "gt" -> ">"; "quot" -> "\""; "apos" -> "'"; "amp" -> "&"
                    else -> reject("entity not allowed")
                }
            }
        }

        private fun attrs(): Pair<Map<String, String>, Boolean> {
            val out = LinkedHashMap<String, String>()
            while (true) {
                skipWs()
                if (s.startsWith("/>", i)) { i += 2; return out to true }
                if (s.startsWith(">", i)) { i += 1; return out to false }
                val k = name()
                skipWs()
                if (!s.startsWith("=", i)) reject("attribute without value")
                i++
                skipWs()
                val q = s.getOrNull(i) ?: reject("eof")
                if (q != '"' && q != '\'') reject("unquoted attribute")
                val end = s.indexOf(q, i + 1)
                if (end < 0) reject("unterminated attribute")
                if (out.put(k, decode(s.substring(i + 1, end))) != null) reject("duplicate attribute")
                i = end + 1
            }
        }

        private fun num(v: String): Float {
            if (!NUM.matches(v)) reject("not a number: $v")
            return v.toFloat()
        }

        private fun paint(v: String): SvgPaint {
            val ok = v in SvgSubset.COLORS || v.lowercase() in SvgSubset.COLORS && v.lowercase() != "currentcolor"
            if (!ok) reject("colour not allowed: $v")
            return when {
                v == "none" -> SvgPaint.None
                v == "currentColor" -> SvgPaint.Current
                else -> SvgPaint.Rgb((0xFF000000L or v.substring(1).toLong(16)).toInt())
            }
        }

        private fun styled(base: SvgStyle, a: Map<String, String>): SvgStyle {
            var st = base
            a["fill"]?.let { st = st.copy(fill = paint(it)) }
            a["stroke"]?.let { st = st.copy(stroke = paint(it)) }
            a["stroke-width"]?.let { st = st.copy(strokeWidth = num(it)) }
            a["stroke-linecap"]?.let { if (it !in CAPS) reject("cap"); st = st.copy(cap = it) }
            a["stroke-linejoin"]?.let { if (it !in JOINS) reject("join"); st = st.copy(join = it) }
            a["stroke-dasharray"]?.let {
                st = if (it == "none") st.copy(dash = emptyList()) else {
                    if (!NUMS.matches(it)) reject("dash")
                    st.copy(dash = it.trim().split(Regex("[\\s,]+")).map { n -> n.toFloat() })
                }
            }
            a["opacity"]?.let { val o = num(it); if (o !in 0f..1f) reject("opacity"); st = st.copy(opacity = st.opacity * o) }
            a["font-size"]?.let {
                val f = num(it); if (f < SvgSubset.FONT_SIZE_MIN || f > SvgSubset.FONT_SIZE_MAX) reject("font size"); st = st.copy(fontSize = f)
            }
            a["font-weight"]?.let { if (it != "normal" && it != "bold") reject("weight"); st = st.copy(bold = it == "bold") }
            a["text-anchor"]?.let { if (it != "start" && it != "middle" && it != "end") reject("anchor"); st = st.copy(anchor = it) }
            return st
        }

        private fun element(depth: Int, inherited: SvgStyle, root: Boolean) {
            if (!s.startsWith("<", i)) reject("expected element")
            i++
            val tag = name()
            if (++count > SvgSubset.MAX_ELEMENTS) reject("too many elements")
            if (tag !in SvgSubset.ELEMENTS) reject("element not allowed: $tag")
            if (depth > SvgSubset.MAX_DEPTH) reject("nested too deep")
            if (root && tag != "svg") reject("root must be svg")
            if (!root && tag == "svg") reject("nested svg")
            val (a, selfClosed) = attrs()
            for ((k, v) in a) {
                val rootOk = root && (k == "viewBox" || k == "xmlns" || k == "width" || k == "height")
                if (k !in SvgSubset.ATTRIBUTES && !rootOk) reject("attribute not allowed: $k")
                if ("url(" in v.lowercase() || "javascript:" in v.lowercase()) reject("url in attribute")
                if (rootOk && k != "viewBox") continue
                when {
                    k in NUMERIC -> num(v)
                    k == "points" && !NUMS.matches(v) -> reject("points")
                }
            }
            if (root) {
                val parts = a["viewBox"]?.replace(",", " ")?.trim()?.split(Regex("\\s+")) ?: reject("no viewBox")
                if (parts.size != 4 || parts.any { !NUM.matches(it) }) reject("viewBox")
                if (parts[0].toFloat() != 0f || parts[1].toFloat() != 0f) reject("viewBox origin")
                width = parts[2].toFloat(); height = parts[3].toFloat()
                if (width < SvgSubset.VIEWBOX_MIN || width > SvgSubset.VIEWBOX_MAX || height < SvgSubset.VIEWBOX_MIN || height > SvgSubset.VIEWBOX_MAX) reject("viewBox size")
            }
            val st = styled(inherited, a)
            if (tag in FILLED && st.fill == null) reject("shape without explicit fill")
            fun f(k: String) = a[k]?.let { num(it) } ?: 0f
            when (tag) {
                "line" -> shapes.add(SvgShape.Line(f("x1"), f("y1"), f("x2"), f("y2"), st))
                "rect" -> shapes.add(SvgShape.Rect(f("x"), f("y"), f("width"), f("height"), f("rx"), f("ry"), st))
                "circle" -> shapes.add(SvgShape.Circle(f("cx"), f("cy"), f("r"), st))
                "ellipse" -> shapes.add(SvgShape.Ellipse(f("cx"), f("cy"), f("rx"), f("ry"), st))
                "polyline", "polygon" -> {
                    val pts = (a["points"] ?: reject("points missing")).trim().split(Regex("[\\s,]+")).map { it.toFloat() }
                    if (pts.size % 2 != 0 || pts.size < 4) reject("points")
                    shapes.add(SvgShape.Poly(pts, tag == "polygon", st))
                }
                "path" -> shapes.add(SvgShape.Path(parsePath(a["d"] ?: reject("path without d")) ?: reject("path data"), st))
            }
            if (selfClosed) {
                if (tag == "text") reject("empty text")
                return
            }
            if (tag == "text") {
                val end = s.indexOf('<', i)
                if (end < 0) reject("eof")
                val content = decode(s.substring(i, end)).trim()
                if (content.isEmpty()) reject("empty text")
                i = end
                shapes.add(SvgShape.Text(f("x"), f("y"), content, st))
                closeTag(tag)
                return
            }
            while (true) {
                skipWs()
                when {
                    s.startsWith("<!--", i) -> { val end = s.indexOf("-->", i); if (end < 0) reject("comment"); i = end + 3 }
                    s.startsWith("</", i) -> { closeTag(tag); return }
                    s.startsWith("<", i) -> element(depth + 1, st, root = false)
                    else -> reject("stray text")
                }
            }
        }

        private fun closeTag(tag: String) {
            if (!s.startsWith("</", i)) reject("expected closing tag")
            i += 2
            if (name() != tag) reject("mismatched tag")
            skipWs()
            if (!s.startsWith(">", i)) reject("bad closing tag")
            i++
        }
    }
}
