package app.dopamine.domain.content

import app.dopamine.domain.model.KNOWN_QUESTION_TYPES
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef

object LessonValidator {
    /** Returns a human-readable problem, or null when the lesson is usable. */
    fun validate(lesson: Lesson, ref: LessonRef? = null): String? {
        if (ref != null && (lesson.id != ref.id || lesson.version != ref.version)) {
            return "id or version differs from the manifest"
        }
        if (lesson.questions.isEmpty()) return "no questions"
        val pageIds = HashSet<String>()
        for (p in lesson.learn) {
            if (!pageIds.add(p.id)) return "duplicate learn page id ${p.id}"
            if ((p.body?.length ?: 0) > 2000 || (p.steps?.size ?: 0) > 20) return "learn page ${p.id} is too large"
        }
        val seen = HashSet<String>()
        for (q in lesson.questions) {
            if (!seen.add(q.id)) return "duplicate question id ${q.id}"
            if (q.type !in KNOWN_QUESTION_TYPES) continue // forward compatible: skipped by the player
            val problem = when (q.type) {
                "multiple_choice", "fill_blank" -> {
                    val c = q.choices
                    when {
                        q.prompt.isNullOrBlank() -> "missing prompt"
                        c == null || c.size !in 2..5 -> "needs 2 to 5 choices"
                        q.answerIndex == null || q.answerIndex !in c.indices -> "answerIndex out of range"
                        else -> null
                    }
                }
                "type_answer" -> if (q.prompt.isNullOrBlank() || q.accepted.isNullOrEmpty()) "needs prompt and accepted" else null
                "match_pairs" -> if ((q.pairs?.size ?: 0) < 2 || q.pairs!!.any { it.size != 2 }) "needs 2 or more pairs" else null
                "order_steps" -> if ((q.steps?.size ?: 0) < 2) "needs 2 or more steps" else null
                else -> null
            }
            if (problem != null) return "question ${q.id}: $problem"
        }
        return null
    }
}
