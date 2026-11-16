package app.dopamine.ui.math

import app.dopamine.ui.math.MathNode.Frac
import app.dopamine.ui.math.MathNode.Row
import app.dopamine.ui.math.MathNode.Script
import app.dopamine.ui.math.MathNode.Sqrt
import app.dopamine.ui.math.MathNode.Sym
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class MathParserTest {
    private fun items(src: String) = (MathParser.parse(src) as Row).items

    @Test fun simpleEquation() {
        assertEquals(
            listOf(Sym("x", Kind.VAR), Sym("+", Kind.OP), Sym("3", Kind.NUM), Sym("=", Kind.REL), Sym("7", Kind.NUM)),
            items("x+3=7"),
        )
    }

    @Test fun fractionTakesTwoGroups() {
        val f = items("\\frac{a+c}{b}").single() as Frac
        assertEquals(3, (f.num as Row).items.size)
        assertEquals(Row(listOf(Sym("b", Kind.VAR))), f.den)
    }

    @Test fun fractionWithSingleCharArguments() {
        val f = items("\\frac12").single() as Frac
        assertEquals(Sym("1", Kind.NUM), f.num)
    }

    @Test fun superscriptAndSubscript() {
        val s = items("x_1^2").single() as Script
        assertEquals(Sym("x", Kind.VAR), s.base)
        assertEquals(Sym("2", Kind.NUM), s.sup)
        assertEquals(Sym("1", Kind.NUM), s.sub)
    }

    @Test fun squareRootWithIndex() {
        val r = items("\\sqrt[3]{27}").single() as Sqrt
        assertTrue(r.index != null)
    }

    @Test fun greekAndOperators() {
        assertEquals(Sym("π", Kind.VAR), items("\\pi").single())
        assertEquals(Sym("≤", Kind.REL), items("\\le").single())
        assertEquals(Sym("−", Kind.OP), items("-").single())
    }

    @Test fun degreeCircleIsKnown() {
        assertEquals(Sym("∘", Kind.TEXT), items("\\circ").single())
    }

    @Test fun leftRightAreDroppedButDelimitersStay() {
        val texts = items("\\left(x\\right)").filterIsInstance<Sym>().map { it.text }.filter { it.isNotEmpty() }
        assertEquals(listOf("(", "x", ")"), texts)
    }

    @Test fun unknownCommandBecomesText() {
        assertEquals(Sym("foo", Kind.TEXT), items("\\foo").single())
    }

    @Test fun brokenInputNeverThrows() {
        listOf("{x", "x}", "\\frac{1}", "\\frac", "^", "x^", "\\", "\\sqrt[", "{{{{", "}}}}", "x_{", "\\text{abc").forEach {
            MathParser.parse(it)
        }
    }

    @Test fun deepNestingAndLongInputAreCapped() {
        MathParser.parse("{".repeat(5000) + "x" + "}".repeat(5000))
        MathParser.parse("x".repeat(100_000))
        val deep = "\\frac{".repeat(200) + "x" + "}{1}".repeat(200)
        MathParser.parse(deep)
    }

    @Test fun textCommandKeepsRawText() {
        assertEquals(Sym("km/h", Kind.TEXT), items("\\text{km/h}").single())
    }
}
