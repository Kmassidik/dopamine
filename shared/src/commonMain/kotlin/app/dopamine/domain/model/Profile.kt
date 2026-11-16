package app.dopamine.domain.model

object ProfileName {
    const val MAX = 24

    /** Trimmed, single-spaced, no control characters. Null when empty or too long. */
    fun clean(raw: String): String? {
        val s = raw.filter { !it.isISOControl() }.trim().replace(Regex("\\s+"), " ")
        return if (s.isNotEmpty() && s.length <= MAX) s else null
    }
}
