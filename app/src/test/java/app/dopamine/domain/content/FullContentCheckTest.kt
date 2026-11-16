package app.dopamine.domain.content

import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.Notes
import app.dopamine.ui.svg.SvgParser
import java.io.File
import java.security.MessageDigest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assume.assumeNotNull
import org.junit.Test

/**
 * Runs the app's real strict validators over a full checkout of the content repo, the same checks an Update
 * performs on the phone. Skipped unless CONTENT_DIR points at that checkout:
 *   CONTENT_DIR=/path/to/dopamine-content ./build.sh testDebugUnitTest --tests '*FullContentCheckTest*'
 */
class FullContentCheckTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun sha(b: ByteArray) = MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { "%02x".format(it) }

    @Test fun everyFileInTheContentRepoPassesTheAppsValidators() {
        val dir = System.getenv("CONTENT_DIR")
        assumeNotNull(dir)
        val root = File(dir!!)
        val manifestBytes = File(root, "manifest.json").readBytes()
        val m: Manifest = json.decodeFromString(manifestBytes.decodeToString())
        assertNull(ManifestValidator.validate(m))
        assertEquals(true, manifestBytes.size <= 512 * 1024)
        var lessons = 0
        var notes = 0
        val problems = ArrayList<String>()
        for (c in m.courses) for (u in c.units) {
            for (ref in u.lessons) {
                val bytes = File(root, ref.path).readBytes()
                if (bytes.size.toLong() != ref.sizeBytes || sha(bytes) != ref.sha256) problems += "${ref.id}: hash or size differs from the manifest"
                val lesson: Lesson = json.decodeFromString(bytes.decodeToString())
                LessonValidator.validate(lesson, ref)?.let { problems += "${ref.id}: $it" }
                if (ContentContract.isLocked(ref.minApp)) problems += "${ref.id}: needs a newer app than this one"
                lessons++
            }
            u.notes?.let { ref ->
                val bytes = File(root, ref.path).readBytes()
                if (bytes.size.toLong() != ref.sizeBytes || sha(bytes) != ref.sha256) problems += "${ref.id}: hash or size differs from the manifest"
                val n: Notes = json.decodeFromString(bytes.decodeToString())
                NotesValidator.validate(n, ref)?.let { problems += "${ref.id}: $it" }
                notes++
            }
        }
        for (img in m.images) assertNotNull("${img.path} does not parse", SvgParser.parse(File(root, img.path).readText()))
        println("FULL CONTENT CHECK: $lessons lessons, $notes notes, ${m.images.size} images, ${problems.size} problems")
        assertEquals(emptyList<String>(), problems)
    }
}
