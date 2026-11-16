package app.dopamine.ui.code

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class HighlightTest {
    private fun toks(code: String) = highlight(code, "python").map { code.substring(it.start, it.end) to it.tok }

    @Test fun keywordsStringsNumbersComments() {
        assertEquals(
            listOf("if" to Tok.KEYWORD, "10" to Tok.NUMBER, "print" to Tok.KEYWORD, "\"hi\"" to Tok.STRING, "# note" to Tok.COMMENT),
            toks("if x > 10:\n    print(\"hi\")  # note"),
        )
    }

    @Test fun keywordInsideWordIsNotHighlighted() { assertTrue(toks("pinned = 1").none { it.second == Tok.KEYWORD }) }

    @Test fun commentHidesKeywords() { assertEquals(listOf("# if else" to Tok.COMMENT), toks("# if else")) }

    @Test fun otherLanguagesStayPlain() { assertTrue(highlight("if x:", "rust").isEmpty()) }

    @Test fun nullLanguageIsPlain() { assertTrue(highlight("if x:", null).isEmpty()) }
}
