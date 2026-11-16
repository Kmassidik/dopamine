package app.dopamine.ui.svg

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SvgParserTest {
    private fun svg(body: String, root: String = """viewBox="0 0 100 50"""") = """<svg xmlns="http://www.w3.org/2000/svg" $root>$body</svg>"""
    private fun ok(src: String) = SvgParser.parse(src)!!
    private fun rejected(src: String) = assertNull(src, SvgParser.parse(src))

    @Test fun readsASimpleDocument() {
        val d = ok(svg("""<g fill="none" stroke="currentColor" stroke-width="3"><line x1="1" y1="2" x2="30" y2="40"/></g>"""))
        assertEquals(100f, d.width, 0f)
        assertEquals(50f, d.height, 0f)
        val line = d.shapes.single() as SvgShape.Line
        assertEquals(30f, line.x2, 0f)
        assertEquals(SvgPaint.Current, line.style.stroke)
        assertEquals(3f, line.style.strokeWidth, 0f)
    }

    @Test fun realBundledImagesParse() {
        val dir = File("src/main/assets/content/images")
        val files = dir.listFiles { f -> f.name.endsWith(".svg") }!!
        assertTrue(files.isNotEmpty())
        files.forEach { assertNotNull(it.name, SvgParser.parse(it.readText())) }
    }

    @Test fun stylesAreInheritedAndOpacityMultiplies() {
        val d = ok(svg("""<g fill="#35C46F" opacity="0.5" font-size="20" font-weight="bold"><g opacity="0.5"><text x="5" y="6" text-anchor="middle">Hi</text></g></g>"""))
        val t = d.shapes.single() as SvgShape.Text
        assertEquals("Hi", t.text)
        assertEquals(0.25f, t.style.opacity, 0.0001f)
        assertEquals(20f, t.style.fontSize, 0f)
        assertEquals(true, t.style.bold)
        assertEquals("middle", t.style.anchor)
        assertEquals(SvgPaint.Rgb(0xFF35C46F.toInt()), t.style.fill)
    }

    @Test fun textEntitiesAreDecoded() {
        val t = ok(svg("""<text x="1" y="2" fill="currentColor">a &amp; b &lt; c</text>""")).shapes.single() as SvgShape.Text
        assertEquals("a & b < c", t.text)
    }

    @Test fun pathCommands() {
        val p = parsePath("M10 10 l5 0 H30 V20 Z m1 1 L2 2")!!
        assertEquals(PathSeg.Move(10f, 10f), p[0])
        assertEquals(PathSeg.Line(15f, 10f), p[1])
        assertEquals(PathSeg.Line(30f, 10f), p[2])
        assertEquals(PathSeg.Line(30f, 20f), p[3])
        assertEquals(PathSeg.Close, p[4])
        assertEquals(PathSeg.Move(11f, 11f), p[5]) // relative to the start of the closed subpath
        assertEquals(PathSeg.Line(2f, 2f), p[6])
    }

    @Test fun implicitLinetoAfterMove() {
        val p = parsePath("M0 0 10 0 10 10")!!
        assertEquals(listOf(PathSeg.Move(0f, 0f), PathSeg.Line(10f, 0f), PathSeg.Line(10f, 10f)), p)
    }

    @Test fun curves() {
        val p = parsePath("M0 0 C1 2 3 4 5 6 q1 1 2 2")!!
        assertEquals(PathSeg.Cubic(1f, 2f, 3f, 4f, 5f, 6f), p[1])
        assertEquals(PathSeg.Quad(6f, 7f, 7f, 8f), p[2])
    }

    @Test fun badPathsAreRejected() {
        assertNull(parsePath("L10 10"))          // must start with a move
        assertNull(parsePath("M0 0 A5 5 0 0 1 10 10")) // arcs are not allowed
        assertNull(parsePath("M0 0 L10"))        // odd number of coordinates
        assertNull(parsePath("M0 0 Z 5"))        // numbers after Z
        assertNull(parsePath(""))
    }

    @Test fun elementsOutsideTheSubsetAreRejected() {
        rejected(svg("""<script>alert(1)</script>"""))
        rejected(svg("""<image x="0" y="0" width="5" height="5"/>"""))
        rejected(svg("""<g fill="none"><use x="1"/></g>"""))
    }

    @Test fun attributesOutsideTheSubsetAreRejected() {
        rejected(svg("""<rect x="0" y="0" width="5" height="5" fill="#ffffff" transform="scale(2)"/>"""))
        rejected(svg("""<rect x="0" y="0" width="5" height="5" fill="#ffffff" style="fill:red"/>"""))
        rejected(svg("""<rect x="0" y="0" width="5" height="5" fill="#ffffff" onclick="x()"/>"""))
    }

    @Test fun colorsMustBeFromThePalette() {
        rejected(svg("""<rect x="0" y="0" width="5" height="5" fill="red"/>"""))
        rejected(svg("""<rect x="0" y="0" width="5" height="5" fill="#123456"/>"""))
        rejected(svg("""<rect x="0" y="0" width="5" height="5" fill="url(#a)"/>"""))
        rejected(svg("""<rect x="0" y="0" width="5" height="5" fill="CURRENTCOLOR"/>"""))
        assertNotNull(SvgParser.parse(svg("""<rect x="0" y="0" width="5" height="5" fill="#FFC93C"/>""")))
    }

    @Test fun shapesNeedAnExplicitFill() {
        rejected(svg("""<rect x="0" y="0" width="5" height="5"/>"""))
        assertNotNull(SvgParser.parse(svg("""<g fill="none"><rect x="0" y="0" width="5" height="5"/></g>""")))
    }

    @Test fun rootMustHaveAProperViewBox() {
        rejected("""<svg xmlns="http://www.w3.org/2000/svg"><g fill="none"/></svg>""")
        rejected(svg("", root = """viewBox="5 5 100 50""""))
        rejected(svg("", root = """viewBox="0 0 10 10""""))
        rejected(svg("", root = """viewBox="0 0 5000 50""""))
    }

    @Test fun doctypeEntitiesAndProcessingInstructionsAreRejected() {
        rejected("""<!DOCTYPE svg><svg viewBox="0 0 100 50"/>""")
        rejected("""<!ENTITY x "y"><svg viewBox="0 0 100 50"/>""")
        rejected("""<?foo bar?><svg viewBox="0 0 100 50"/>""")
        rejected(svg("""<text x="1" y="2" fill="currentColor">&#65;</text>"""))
        rejected(svg("""<text x="1" y="2" fill="currentColor">&custom;</text>"""))
        assertNotNull(SvgParser.parse("""<?xml version="1.0"?>""" + svg("""<g fill="none"/>""")))
    }

    @Test fun limitsAreEnforced() {
        rejected(svg("""<g fill="none">${"<line x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"/>".repeat(90)}</g>"""))
        rejected(svg("<g fill=\"none\"><g><g><g><line x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"/></g></g></g></g>")) // depth 6
        rejected(svg("""<text x="1" y="2" fill="currentColor">${"x".repeat(7000)}</text>"""))
        rejected(svg("""<text x="1" y="2" fill="currentColor" font-size="80">Hi</text>"""))
    }

    @Test fun brokenXmlIsRejectedNotThrown() {
        listOf("", "<svg", "<svg viewBox=\"0 0 100 50\">", "<svg viewBox=0 0 100 50/>", "not xml", "<svg viewBox=\"0 0 100 50\"></g>").forEach { rejected(it) }
    }

    @Test fun duplicateAttributesAndScriptUrlsAreRejected() {
        rejected(svg("""<line x1="1" x1="2" y1="0" x2="1" y2="1" stroke="currentColor" fill="none"/>"""))
        rejected(svg("""<g fill="none" id="javascript:alert(1)"/>"""))
    }
}
