package app.dopamine.ui.math

sealed interface Seg {
    data class Text(val s: String) : Seg
    data class Math(val s: String) : Seg
    data class Code(val s: String) : Seg
}

/**
 * Splits text into plain text, `$math$` and `` `code` `` pieces. Inside code a `$` is literal and inside math
 * a backtick is literal. A delimiter with no closing partner stays as plain text. Pure Kotlin (unit-tested).
 */
fun splitInline(text: String): List<Seg> {
    val out = ArrayList<Seg>()
    val plain = StringBuilder()
    fun flush() { if (plain.isNotEmpty()) { out.add(Seg.Text(plain.toString())); plain.clear() } }
    var i = 0
    while (i < text.length) {
        val ch = text[i]
        if (ch == '$' || ch == '`') {
            val j = text.indexOf(ch, i + 1)
            if (j > i + 1) {
                flush()
                val inner = text.substring(i + 1, j)
                out.add(if (ch == '$') Seg.Math(inner) else Seg.Code(inner))
                i = j + 1
                continue
            }
        }
        plain.append(ch)
        i++
    }
    flush()
    return out
}
