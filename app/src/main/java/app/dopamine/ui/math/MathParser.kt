package app.dopamine.ui.math

/** How a symbol is spaced and styled. Pure Kotlin so it can be unit-tested on the JVM. */
enum class Kind { VAR, NUM, OP, REL, TEXT, OPEN, CLOSE }

sealed interface MathNode {
    data class Sym(val text: String, val kind: Kind) : MathNode
    data class Row(val items: List<MathNode>) : MathNode
    data class Frac(val num: MathNode, val den: MathNode) : MathNode
    data class Sqrt(val body: MathNode, val index: MathNode?) : MathNode
    data class Script(val base: MathNode, val sup: MathNode?, val sub: MathNode?) : MathNode
}

/**
 * Parses the LaTeX subset lessons use: fractions, roots, super/subscripts, greek letters, common
 * operators and `\left \right`. It never throws: unknown commands render as plain text, stray or
 * missing braces are tolerated, and input length and nesting depth are capped.
 */
object MathParser {
    const val MAX_LEN = 400
    const val MAX_DEPTH = 12

    fun parse(src: String): MathNode = Parser(src.take(MAX_LEN)).parseAll()

    private val SYMBOLS: Map<String, Pair<String, Kind>> = buildMap {
        fun put(kind: Kind, vararg pairs: Pair<String, String>) = pairs.forEach { (k, v) -> put(k, v to kind) }
        put(Kind.OP, "cdot" to "·", "times" to "×", "div" to "÷", "pm" to "±", "mp" to "∓")
        put(Kind.REL, "le" to "≤", "leq" to "≤", "ge" to "≥", "geq" to "≥", "ne" to "≠", "neq" to "≠",
            "approx" to "≈", "equiv" to "≡", "to" to "→", "rightarrow" to "→", "Rightarrow" to "⇒",
            "leftarrow" to "←", "in" to "∈")
        put(Kind.TEXT, "infty" to "∞", "degree" to "°", "circ" to "∘", "ldots" to "…", "dots" to "…", "cdots" to "…",
            "sum" to "∑", "int" to "∫", "prod" to "∏", "partial" to "∂", "nabla" to "∇", "angle" to "∠",
            "triangle" to "△", "perp" to "⊥", "parallel" to "∥",
            "Delta" to "Δ", "Omega" to "Ω", "Sigma" to "Σ", "Pi" to "Π", "Theta" to "Θ")
        put(Kind.VAR, "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ", "epsilon" to "ε",
            "eta" to "η", "theta" to "θ", "lambda" to "λ", "mu" to "μ", "pi" to "π", "rho" to "ρ",
            "sigma" to "σ", "tau" to "τ", "phi" to "φ", "omega" to "ω")
    }

    private val FUNCTIONS = setOf("sin", "cos", "tan", "log", "ln", "lim", "min", "max", "exp", "det")
    private val RAW_TEXT = setOf("text", "mathrm", "textbf", "mathbf", "operatorname")
    private val THIN = "\u2009"

    private class Parser(private val s: String) {
        private var i = 0
        private var depth = 0

        fun parseAll(): MathNode = MathNode.Row(row(false))

        private fun row(inBrace: Boolean): List<MathNode> {
            val out = ArrayList<MathNode>()
            while (i < s.length) {
                val ch = s[i]
                when {
                    ch == '}' -> { if (inBrace) return out; i++ }
                    ch.isWhitespace() -> i++
                    ch == '^' || ch == '_' -> {
                        i++
                        val arg = scriptArg()
                        out.add(MathNode.Script(MathNode.Sym("", Kind.TEXT), if (ch == '^') arg else null, if (ch == '_') arg else null))
                    }
                    else -> out.add(withScripts(atom()))
                }
            }
            return out
        }

        private fun withScripts(base: MathNode): MathNode {
            var sup: MathNode? = null
            var sub: MathNode? = null
            while (i < s.length && (s[i] == '^' || s[i] == '_')) {
                val up = s[i] == '^'
                i++
                val arg = scriptArg()
                if (up && sup == null) sup = arg else if (!up && sub == null) sub = arg
            }
            return if (sup == null && sub == null) base else MathNode.Script(base, sup, sub)
        }

        private fun scriptArg(): MathNode {
            skipSpaces()
            if (i >= s.length) return MathNode.Sym("", Kind.TEXT)
            val ch = s[i]
            return when {
                ch == '{' -> group()
                ch == '}' -> MathNode.Sym("", Kind.TEXT)
                ch.isDigit() -> { i++; MathNode.Sym(ch.toString(), Kind.NUM) }
                else -> atom()
            }
        }

        private fun group(): MathNode {
            i++ // '{'
            if (depth >= MAX_DEPTH) {
                skipBalanced()
                return MathNode.Sym("…", Kind.TEXT)
            }
            depth++
            val items = row(true)
            if (i < s.length && s[i] == '}') i++
            depth--
            return MathNode.Row(items)
        }

        /** Called just after an opening brace: skip to its match. */
        private fun skipBalanced() {
            var open = 1
            while (i < s.length && open > 0) {
                when (s[i]) { '{' -> open++; '}' -> open-- }
                i++
            }
        }

        private fun atom(): MathNode {
            val ch = s[i]
            return when {
                ch == '{' -> group()
                ch == '\\' -> command()
                ch.isDigit() || (ch == '.' && i + 1 < s.length && s[i + 1].isDigit()) -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    MathNode.Sym(s.substring(start, i), Kind.NUM)
                }
                ch.isLetter() -> { i++; MathNode.Sym(ch.toString(), Kind.VAR) }
                else -> { i++; symbol(ch) }
            }
        }

        private fun symbol(ch: Char): MathNode = when (ch) {
            '+' -> MathNode.Sym("+", Kind.OP)
            '-' -> MathNode.Sym("−", Kind.OP)
            '*' -> MathNode.Sym("·", Kind.OP)
            '=', '<', '>' -> MathNode.Sym(ch.toString(), Kind.REL)
            '(', '[' -> MathNode.Sym(ch.toString(), Kind.OPEN)
            ')', ']' -> MathNode.Sym(ch.toString(), Kind.CLOSE)
            '\'' -> MathNode.Sym("′", Kind.TEXT)
            else -> MathNode.Sym(ch.toString(), Kind.TEXT)
        }

        private fun command(): MathNode {
            i++ // backslash
            if (i >= s.length) return MathNode.Sym("\\", Kind.TEXT)
            if (!s[i].isLetter()) {
                val c = s[i++]
                return when (c) {
                    ',', ';', ':', ' ', '!' -> MathNode.Sym(THIN, Kind.TEXT)
                    '|' -> MathNode.Sym("‖", Kind.TEXT)
                    '\\' -> MathNode.Sym(" ", Kind.TEXT)
                    else -> MathNode.Sym(c.toString(), Kind.TEXT)
                }
            }
            val start = i
            while (i < s.length && s[i].isLetter()) i++
            val name = s.substring(start, i)
            return when {
                name == "frac" || name == "dfrac" || name == "tfrac" -> MathNode.Frac(arg(), arg())
                name == "sqrt" -> {
                    skipSpaces()
                    var index: MathNode? = null
                    if (i < s.length && s[i] == '[') {
                        val end = s.indexOf(']', i)
                        if (end > i) {
                            index = Parser(s.substring(i + 1, end)).parseAll()
                            i = end + 1
                        }
                    }
                    MathNode.Sqrt(arg(), index)
                }
                name in RAW_TEXT -> MathNode.Sym(rawGroup(), Kind.TEXT)
                name == "left" || name == "right" -> {
                    if (i < s.length && s[i] == '.') i++
                    MathNode.Sym("", Kind.TEXT)
                }
                name in FUNCTIONS -> MathNode.Sym(name, Kind.TEXT)
                else -> SYMBOLS[name]?.let { MathNode.Sym(it.first, it.second) } ?: MathNode.Sym(name, Kind.TEXT)
            }
        }

        private fun arg(): MathNode {
            skipSpaces()
            if (i >= s.length || s[i] == '}') return MathNode.Sym("", Kind.TEXT)
            if (s[i].isDigit()) return MathNode.Sym(s[i++].toString(), Kind.NUM) // `\frac12` means 1 over 2
            return atom()
        }

        private fun rawGroup(): String {
            skipSpaces()
            if (i >= s.length) return ""
            if (s[i] != '{') return s[i++].toString()
            i++
            val start = i
            var open = 1
            while (i < s.length && open > 0) {
                when (s[i]) { '{' -> open++; '}' -> open-- }
                i++
            }
            return s.substring(start, if (open == 0) i - 1 else i)
        }

        private fun skipSpaces() {
            while (i < s.length && s[i].isWhitespace()) i++
        }
    }
}
