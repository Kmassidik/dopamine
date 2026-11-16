package app.dopamine.ui.lesson

private val FRAC = Regex("""\\frac\{([^{}]*)\}\{([^{}]*)\}""")
private val SQRT = Regex("""\\sqrt\{([^{}]*)\}""")

/** Stand-in until the native LaTeX renderer exists: turns the common bits into readable text. */
fun plainMath(src: String): String {
    var t = src.replace("$", "")
    repeat(3) { t = FRAC.replace(t) { "${it.groupValues[1]}/${it.groupValues[2]}" } }
    t = SQRT.replace(t) { "√${it.groupValues[1]}" }
    return t.replace("\\_", "_").replace("\\cdot", "·").replace("\\times", "×")
        .replace("\\pi", "π").replace("\\le", "≤").replace("\\ge", "≥")
        .replace("^2", "²").replace("^3", "³").replace("\\", "")
}
