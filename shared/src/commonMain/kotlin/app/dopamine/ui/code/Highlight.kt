package app.dopamine.ui.code

enum class Tok { PLAIN, KEYWORD, STRING, NUMBER, COMMENT }

data class Span(val start: Int, val end: Int, val tok: Tok)

private val KEYWORDS = "if|elif|else|for|while|in|def|return|and|or|not|True|False|None|print|break|continue|import|from|as|pass|len|range|input|int|str|float"
private val PYTHON = Regex("""(#[^\n]*)|("[^"\n]*"|'[^'\n]*')|(\b\d+(?:\.\d+)?\b)|(\b(?:$KEYWORDS)\b)""")

/** A tiny highlighter. Only Python is coloured; any other language is shown plain. Pure Kotlin so it is unit-tested. */
fun highlight(code: String, language: String?): List<Span> {
    if (language?.lowercase() != "python") return emptyList()
    return PYTHON.findAll(code).map { m ->
        val tok = when {
            m.groups[1] != null -> Tok.COMMENT
            m.groups[2] != null -> Tok.STRING
            m.groups[3] != null -> Tok.NUMBER
            else -> Tok.KEYWORD
        }
        Span(m.range.first, m.range.last + 1, tok)
    }.toList()
}
