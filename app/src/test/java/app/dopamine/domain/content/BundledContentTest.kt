package app.dopamine.domain.content

import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.Notes
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The content bundled in the app must pass the strict validators and match its own manifest. */
class BundledContentTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val root = File("src/main/assets/content")

    @Test fun bundledContentIsStrictlyValid() {
        val m: Manifest = json.decodeFromString(File(root, "manifest.json").readText())
        assertNull(ManifestValidator.validate(m))
        var lessons = 0
        for (course in m.courses) for (unit in course.units) {
            for (ref in unit.lessons) {
                val lesson: Lesson = json.decodeFromString(File(root, ref.path).readText())
                assertNull("${ref.id}: ${LessonValidator.validate(lesson, ref)}", LessonValidator.validate(lesson, ref))
                lessons++
            }
            unit.notes?.let { ref ->
                val notes: Notes = json.decodeFromString(File(root, ref.path).readText())
                assertNull("${ref.id}: ${NotesValidator.validate(notes, ref)}", NotesValidator.validate(notes, ref))
            }
        }
        assertTrue(lessons > 0)
        m.images.forEach { assertTrue("missing ${it.path}", File(root, it.path).isFile) }
    }
}
