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
        val (hi, lo) = lum(a).let { x -> lum(b).let { y -> if (x > y) x to y else y to x } }
        return (hi + 0.05) / (lo + 0.05)
    }

    private fun aa(fg: Long, bg: Long, name: String, min: Double = 4.5) {
        val r = ratio(fg, bg)
        assertTrue("$name contrast %.2f < $min".format(r), r >= min)
    }

    @Test fun lightText() {
        listOf(Palette.WASHI, Palette.WASHI_RAISED, Palette.WASHI_DEEP).forEach { bg ->
            aa(Palette.SUMI, bg, "sumi on %x".format(bg))
            aa(Palette.SUMI_SOFT, bg, "sumi-soft on %x".format(bg))
            aa(Palette.INDIGO, bg, "indigo on %x".format(bg))
            aa(Palette.VERMILION, bg, "vermilion on %x".format(bg))
            aa(Palette.MATCHA, bg, "matcha on %x".format(bg))
        }
        aa(Palette.WASHI_RAISED, Palette.INDIGO, "cream on indigo")
        aa(Palette.WASHI_RAISED, Palette.VERMILION, "cream on vermilion")
        aa(Palette.WASHI_RAISED, Palette.MATCHA, "cream on matcha")
        aa(Palette.SUMI, Palette.SAKURA, "sumi on sakura")
        aa(Palette.MATCHA, Palette.MATCHA_SOFT, "matcha on soft")
        aa(Palette.VERMILION, Palette.VERMILION_SOFT, "vermilion on soft")
    }

    @Test fun darkText() {
        listOf(Palette.NIGHT, Palette.NIGHT_RAISED, Palette.NIGHT_DEEP).forEach { bg ->
            aa(Palette.PAPER, bg, "paper on %x".format(bg))
            aa(Palette.PAPER_SOFT, bg, "paper-soft on %x".format(bg))
            aa(Palette.INDIGO_LIGHT, bg, "indigo on %x".format(bg))
            aa(Palette.VERMILION_LIGHT, bg, "vermilion on %x".format(bg))
            aa(Palette.MATCHA_LIGHT, bg, "matcha on %x".format(bg))
            aa(Palette.SAKURA_LIGHT, bg, "sakura on %x".format(bg))
        }
        aa(Palette.NIGHT, Palette.INDIGO_LIGHT, "night on indigo")
        aa(Palette.MATCHA_LIGHT, Palette.MATCHA_NIGHT_SOFT, "matcha on soft")
        aa(Palette.VERMILION_LIGHT, Palette.VERMILION_NIGHT_SOFT, "vermilion on soft")
    }
}
