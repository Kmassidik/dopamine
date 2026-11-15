package app.dopamine.domain.content

/**
 * What this app version understands. It must match `contract.json` in the content repo; `ContractTest`
 * fails when they disagree. When you add a kind of content: build it, bump versionCode and
 * [APP_VERSION], add it here with that version, then update the content repo's tools/standard.py.
 */
object ContentContract {
    const val CONTRACT_VERSION = 1

    /** Equals BuildConfig.VERSION_CODE (checked by ContractTest). */
    const val APP_VERSION = 2

    /** Kind -> app version that introduced it. */
    val QUESTION_TYPES = mapOf("multiple_choice" to 1, "fill_blank" to 1, "type_answer" to 1, "match_pairs" to 1, "order_steps" to 1)
    val PAGE_KINDS = mapOf("text" to 1, "example" to 1, "tip" to 1, "code" to 2)
    val NOTE_KINDS = mapOf("text" to 1, "formula" to 1, "list" to 1, "tip" to 1, "code" to 2)

    fun isLocked(minApp: Int) = minApp > APP_VERSION

    object Limits {
        const val ID_PATTERN = "^[a-z0-9]+(-[a-z0-9]+)*$"
        const val TITLE_MAX = 80
        const val XP_MIN = 1
        const val XP_MAX = 100
        const val QUESTIONS_MIN = 1
        const val QUESTIONS_MAX = 40
        const val TEXT_MAX = 600
        const val CHOICES_MIN = 2
        const val CHOICES_MAX = 5
        const val ANSWER_INDEX_MAX = 4
        const val ACCEPTED_MIN = 1
        const val ACCEPTED_MAX = 12
        const val ACCEPTED_ITEM_MAX = 60
        const val PAIRS_MIN = 2
        const val PAIRS_MAX = 6
        const val STEPS_MIN = 2
        const val STEPS_MAX = 8
        const val LEARN_MAX = 12
        const val PAGE_TITLE_MAX = 80
        const val EXAMPLE_STEPS_MIN = 1
        const val EXAMPLE_STEPS_MAX = 8
        const val NOTES_BLOCKS_MAX = 20
        const val NOTE_ITEMS_MAX = 12
        const val SUMMARY_MAX = 400
        const val OUTCOMES_MAX = 8
        const val REQUIREMENTS_MAX = 6
        const val MINUTES_MAX = 10000
        const val CODE_MAX = 700
        const val CODE_LINES = 12
        const val CODE_WIDTH = 40
    }
}
