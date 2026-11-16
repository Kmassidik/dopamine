package app.dopamine.ui.math

import app.dopamine.ui.math.Seg.Code
import app.dopamine.ui.math.Seg.Math
import app.dopamine.ui.math.Seg.Text
import kotlin.test.assertEquals
import kotlin.test.Test

class InlineSplitTest {
    @Test fun plainText() = assertEquals(listOf(Text("hello")), splitInline("hello"))

    @Test fun mathAndText() = assertEquals(listOf(Text("Solve "), Math("x+3=7"), Text(" now")), splitInline("Solve \$x+3=7\$ now"))

    @Test fun codeAndText() = assertEquals(listOf(Text("Use "), Code("print(x)"), Text(" here")), splitInline("Use `print(x)` here"))

    @Test fun mixed() = assertEquals(listOf(Code("x"), Text(" is "), Math("2")), splitInline("`x` is \$2\$"))

    @Test fun dollarInsideCodeIsLiteral() = assertEquals(listOf(Code("a \$ b")), splitInline("`a \$ b`"))

    @Test fun backtickInsideMathIsLiteral() = assertEquals(listOf(Math("a`b")), splitInline("\$a`b\$"))

    @Test fun unclosedDelimitersStayPlain() {
        assertEquals(listOf(Text("cost \$5")), splitInline("cost \$5"))
        assertEquals(listOf(Text("a `b")), splitInline("a `b"))
    }

    @Test fun emptyPairStaysPlain() = assertEquals(listOf(Text("``")), splitInline("``"))
}
