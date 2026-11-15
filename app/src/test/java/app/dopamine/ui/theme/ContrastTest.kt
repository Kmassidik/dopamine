package app.dopamine.ui.theme

import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {
    private fun lin(c: Int): Double {
        val v = c / 255.0
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun lum(argb: Long): Double {
        val r = ((argb shr 16) and 0xFF).toInt()
        val g = ((argb shr 8) and 0xFF).toInt()
        val b = (argb and 0xFF).toInt()
        return 0.2126 * lin(r) + 0.7152 * lin(g) + 0.0722 * lin(b)
    }

    private fun ratio(a: Long, b: Long): Double {
        val x = lum(a); val y = lum(b)
        return (maxOf(x, y) + 0.05) / (minOf(x, y) + 0.05)
    }

    private fun aa(fg: Long, bg: Long, name: String, min: Double = 4.5) {
        val r = ratio(fg, bg)
        assertTrue("$name contrast %.2f < $min".format(r), r >= min)
    }

    @Test fun light() {
        aa(Palette.INK, Palette.WHITE, "ink/white")
        aa(Palette.INK_SOFT, Palette.WHITE, "soft/white")
        aa(Palette.GREEN_DEEP, Palette.WHITE, "green text/white")
        aa(Palette.GREEN_DEEP, Palette.GREEN_TINT, "green text/tint")
        aa(Palette.YELLOW_DEEP, Palette.WHITE, "yellow text/white")
        aa(Palette.YELLOW_DEEP, Palette.YELLOW_TINT, "yellow text/tint")
        aa(Palette.PINK_DEEP, Palette.WHITE, "pink text/white")
        aa(Palette.PINK_DEEP, Palette.PINK_TINT, "pink text/tint")
        listOf(Palette.GREEN, Palette.YELLOW, Palette.PINK, Palette.GREEN_TINT, Palette.YELLOW_TINT, Palette.PINK_TINT)
            .forEach { aa(Palette.INK, it, "ink on %x".format(it)) }
        listOf(Palette.GREEN_TINT, Palette.YELLOW_TINT, Palette.PINK_TINT)
            .forEach { aa(Palette.INK_SOFT, it, "soft on %x".format(it)) }
    }

    @Test fun dark() {
        listOf(Palette.N_BG, Palette.N_SURFACE).forEach { bg ->
            aa(Palette.N_INK, bg, "ink on %x".format(bg))
            aa(Palette.N_INK_SOFT, bg, "soft on %x".format(bg))
            aa(Palette.N_GREEN_TEXT, bg, "green on %x".format(bg))
            aa(Palette.N_YELLOW, bg, "yellow on %x".format(bg))
            aa(Palette.N_PINK_TEXT, bg, "pink on %x".format(bg))
        }
        aa(Palette.N_GREEN_TEXT, Palette.N_GREEN_TINT, "green/tint")
        aa(Palette.N_YELLOW, Palette.N_YELLOW_TINT, "yellow/tint")
        aa(Palette.N_PINK_TEXT, Palette.N_PINK_TINT, "pink/tint")
        aa(Palette.N_INK_SOFT, Palette.N_GREEN_TINT, "soft/green tint")
        listOf(Palette.N_GREEN, Palette.N_YELLOW, Palette.N_PINK).forEach { aa(Palette.N_BG, it, "bg on %x".format(it)) }
    }
}
