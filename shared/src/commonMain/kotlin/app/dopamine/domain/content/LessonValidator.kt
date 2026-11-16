package app.dopamine.domain.content

import app.dopamine.domain.content.ContentContract.Limits as L
import app.dopamine.domain.model.LearnPage
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.NoteBlock
import app.dopamine.domain.model.Notes
import app.dopamine.domain.model.NotesRef
import app.dopamine.domain.model.Question

/*
 * Strict validators. They enforce the same shapes and limits as the content repo's JSON Schemas
 * (see ContentContract and ContractTest). Unknown FIELDS are ignored, unknown KINDS are rejected
 * unless the file declares a minApp above this app's version (then it is only locked, not read).
 * Each returns a human-readable problem, or null when the file is usable.
 */

private val ID = Regex(L.ID_PATTERN)
private val SVG = Regex("^(?!/)(?!.*\\.\\.)[A-Za-z0-9_\\-/]+\\.svg$")
private val SHA = Regex("^[0-9a-f]{64}$")

private fun badId(id: String) = !ID.matches(id)
private fun badText(s: String?, max: Int = L.TEXT_MAX) = s == null || s.isEmpty() || s.length > max

object LessonValidator {
    fun validate(lesson: Lesson, ref: LessonRef? = null, appVersion: Int = ContentContract.APP_VERSION): String? {
        if (ref != null && (lesson.id != ref.id || lesson.version != ref.version)) {
            return "id or version differs from the manifest"
        }
        if (lesson.minApp > appVersion) return null // locked: the app will not read it
        if (badId(lesson.id)) return "bad lesson id"
        if (lesson.version < 1) return "bad version"
        if (lesson.title.isEmpty() || lesson.title.length > L.TITLE_MAX) return "bad title"
        if (lesson.xp !in L.XP_MIN..L.XP_MAX) return "xp out of range"
        if (lesson.questions.size !in L.QUESTIONS_MIN..L.QUESTIONS_MAX) return "needs ${L.QUESTIONS_MIN} to ${L.QUESTIONS_MAX} questions"
        if (lesson.learn.size > L.LEARN_MAX) return "too many learn pages"
        val pageIds = HashSet<String>()
        for (p in lesson.learn) {
            if (!pageIds.add(p.id)) return "duplicate learn page id ${p.id}"
            page(p, appVersion)?.let { return "learn page ${p.id}: $it" }
        }
        val seen = HashSet<String>()
        for (q in lesson.questions) {
            if (!seen.add(q.id)) return "duplicate question id ${q.id}"
            question(q, appVersion)?.let { return "question ${q.id}: $it" }
        }
        return null
    }

    private fun page(p: LearnPage, appVersion: Int): String? {
        if (badId(p.id)) return "bad id"
        val since = ContentContract.PAGE_KINDS[p.kind] ?: return "unknown kind ${p.kind}"
        if (since > appVersion) return "kind ${p.kind} needs a newer app (set minApp)"
        if (p.title != null && badText(p.title, L.PAGE_TITLE_MAX)) return "bad title"
        if (p.body != null && badText(p.body)) return "bad body"
        if (p.image != null && !SVG.matches(p.image)) return "bad image path"
        if (p.image != null && (p.alt == null || p.alt.length !in L.ALT_MIN..L.ALT_MAX)) return "an image needs an alt description of ${L.ALT_MIN} to ${L.ALT_MAX} characters"
        return when (p.kind) {
            "text", "tip" -> if (p.body == null) "needs a body" else null
            "example" -> when {
                p.title == null -> "needs a title"
                p.steps == null || p.steps.size !in L.EXAMPLE_STEPS_MIN..L.EXAMPLE_STEPS_MAX -> "needs ${L.EXAMPLE_STEPS_MIN} to ${L.EXAMPLE_STEPS_MAX} steps"
                p.steps.any { badText(it) } -> "bad step text"
                else -> null
            }
            "code" -> when {
                p.title == null -> "needs a title"
                badText(p.code, L.CODE_MAX) -> "needs code of up to ${L.CODE_MAX} characters"
                else -> null
            }
            else -> null
        }
    }

    private fun question(q: Question, appVersion: Int): String? {
        if (badId(q.id)) return "bad id"
        val since = ContentContract.QUESTION_TYPES[q.type] ?: return "unknown type ${q.type}"
        if (since > appVersion) return "type ${q.type} needs a newer app (set minApp)"
        if (badText(q.explanation)) return "needs an explanation"
        if (q.prompt != null && badText(q.prompt)) return "bad prompt"
        if (q.image != null && !SVG.matches(q.image)) return "bad image path"
        if (q.image != null && (q.alt == null || q.alt.length !in L.ALT_MIN..L.ALT_MAX)) return "an image needs an alt description of ${L.ALT_MIN} to ${L.ALT_MAX} characters"
        return when (q.type) {
            "multiple_choice", "fill_blank" -> {
                val c = q.choices
                when {
                    q.prompt == null -> "missing prompt"
                    c == null || c.size !in L.CHOICES_MIN..L.CHOICES_MAX -> "needs ${L.CHOICES_MIN} to ${L.CHOICES_MAX} choices"
                    c.any { badText(it) } -> "bad choice text"
                    c.toSet().size != c.size -> "duplicate choices"
                    q.answerIndex == null || q.answerIndex !in c.indices || q.answerIndex > L.ANSWER_INDEX_MAX -> "answerIndex out of range"
                    else -> null
                }
            }
            "type_answer" -> {
                val a = q.accepted
                when {
                    q.prompt == null -> "missing prompt"
                    a == null || a.size !in L.ACCEPTED_MIN..L.ACCEPTED_MAX -> "needs ${L.ACCEPTED_MIN} to ${L.ACCEPTED_MAX} accepted answers"
                    a.any { it.isEmpty() || it.length > L.ACCEPTED_ITEM_MAX } -> "bad accepted answer"
                    else -> null
                }
            }
            "match_pairs" -> {
                val p = q.pairs
                when {
                    p == null || p.size !in L.PAIRS_MIN..L.PAIRS_MAX -> "needs ${L.PAIRS_MIN} to ${L.PAIRS_MAX} pairs"
                    p.any { it.size != 2 || badText(it[0]) || badText(it[1]) } -> "bad pair"
                    p.map { it[0] }.toSet().size != p.size || p.map { it[1] }.toSet().size != p.size -> "duplicate items in pairs"
                    else -> null
                }
            }
            "order_steps" -> {
                val s = q.steps
                when {
                    s == null || s.size !in L.STEPS_MIN..L.STEPS_MAX -> "needs ${L.STEPS_MIN} to ${L.STEPS_MAX} steps"
                    s.any { badText(it) } -> "bad step text"
                    s.toSet().size != s.size -> "duplicate steps"
                    else -> null
                }
            }
            else -> null
        }
    }
}

object NotesValidator {
    fun validate(notes: Notes, ref: NotesRef? = null, appVersion: Int = ContentContract.APP_VERSION): String? {
        if (ref != null && (notes.id != ref.id || notes.version != ref.version)) return "id or version differs from the manifest"
        if (notes.minApp > appVersion) return null
        if (badId(notes.id)) return "bad notes id"
        if (notes.title.isEmpty() || notes.title.length > L.TITLE_MAX) return "bad title"
        if (notes.blocks.isEmpty()) return "no blocks"
        if (notes.blocks.size > L.NOTES_BLOCKS_MAX) return "too many blocks"
        val seen = HashSet<String>()
        for (b in notes.blocks) {
            if (!seen.add(b.id)) return "duplicate block id ${b.id}"
            block(b, appVersion)?.let { return "block ${b.id}: $it" }
        }
        return null
    }

    private fun block(b: NoteBlock, appVersion: Int): String? {
        if (badId(b.id)) return "bad id"
        val since = ContentContract.NOTE_KINDS[b.kind] ?: return "unknown kind ${b.kind}"
        if (since > appVersion) return "kind ${b.kind} needs a newer app (set minApp)"
        if (b.title != null && badText(b.title, L.PAGE_TITLE_MAX)) return "bad title"
        if (b.body != null && badText(b.body)) return "bad body"
        val items = b.items
        if (items != null && (items.size !in 1..L.NOTE_ITEMS_MAX || items.any { badText(it) })) return "bad list items"
        return when (b.kind) {
            "text", "tip", "formula" -> if (b.body == null) "needs a body" else null
            "list" -> if (items == null) "needs items" else null
            "code" -> if (b.title == null || badText(b.code, L.CODE_MAX)) "needs a title and code of up to ${L.CODE_MAX} characters" else null
            else -> null
        }
    }
}

object ManifestValidator {
    fun validate(m: Manifest): String? {
        val ids = HashSet<String>()
        for (c in m.categories) if (badId(c.id)) return "bad category id ${c.id}"
        for (course in m.courses) {
            if (badId(course.id)) return "bad course id ${course.id}"
            if (m.categories.none { it.id == course.categoryId }) return "course ${course.id} has an unknown category"
            course.overview?.let { ov ->
                if (ov.summary.length > L.SUMMARY_MAX) return "course ${course.id}: summary too long"
                if (ov.outcomes.size > L.OUTCOMES_MAX || ov.requirements.size > L.REQUIREMENTS_MAX) return "course ${course.id}: too many list items"
                if (ov.minutes !in 0..L.MINUTES_MAX) return "course ${course.id}: bad minutes"
            }
            for (unit in course.units) {
                if (badId(unit.id)) return "bad unit id ${unit.id}"
                for (l in unit.lessons) {
                    if (!ids.add(l.id)) return "duplicate lesson id ${l.id}"
                    fileRef(l.id, l.path, l.version, l.sha256, l.sizeBytes, l.minApp)?.let { return it }
                }
                unit.notes?.let { fileRef(it.id, it.path, it.version, it.sha256, it.sizeBytes, it.minApp)?.let { p -> return p } }
            }
        }
        for (img in m.images) {
            if (!SafePath.isSafe(img.path)) return "unsafe image path"
            if (!SHA.matches(img.sha256) || img.sizeBytes <= 0) return "bad image entry ${img.path}"
        }
        return null
    }

    private fun fileRef(id: String, path: String, version: Int, sha: String, size: Long, minApp: Int): String? = when {
        badId(id) -> "bad id $id"
        !SafePath.isSafe(path) -> "unsafe path for $id"
        version < 1 || minApp < 1 -> "bad version for $id"
        !SHA.matches(sha) || size <= 0 -> "bad hash or size for $id"
        else -> null
    }
}
