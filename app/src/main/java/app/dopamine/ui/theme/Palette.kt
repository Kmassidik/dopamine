package app.dopamine.ui.theme

/**
 * Raw design tokens as ARGB longs, so JVM unit tests can check contrast without Compose.
 * Six core colors plus neutrals. See DESIGN.md.
 */
object Palette {
    // Light: washi paper and sumi ink
    const val WASHI = 0xFFF7F1E3
    const val WASHI_RAISED = 0xFFFFFBF2
    const val WASHI_DEEP = 0xFFECE3CE
    const val SUMI = 0xFF23243A
    const val SUMI_SOFT = 0xFF565873
    const val INDIGO = 0xFF34447F
    const val VERMILION = 0xFFA93620
    const val VERMILION_FILL = 0xFFD9482B
    const val MATCHA = 0xFF3F6B34
    const val MATCHA_FILL = 0xFF6F9A5B
    const val SAKURA = 0xFFF3A9BA
    const val SAKURA_SOFT = 0xFFFBE0E6
    const val MATCHA_SOFT = 0xFFDDEBD3
    const val VERMILION_SOFT = 0xFFF8D9D1

    // Dark: deep indigo-black ink
    const val NIGHT = 0xFF12132A
    const val NIGHT_RAISED = 0xFF1C1E3C
    const val NIGHT_DEEP = 0xFF2A2D52
    const val PAPER = 0xFFF0E9D6
    const val PAPER_SOFT = 0xFFB9B7CC
    const val INDIGO_LIGHT = 0xFFA5B2F0
    const val VERMILION_LIGHT = 0xFFFF8A6B
    const val MATCHA_LIGHT = 0xFF9CCB86
    const val SAKURA_LIGHT = 0xFFF7B8C6
    const val MATCHA_NIGHT_SOFT = 0xFF22362A
    const val VERMILION_NIGHT_SOFT = 0xFF3D2230
}
