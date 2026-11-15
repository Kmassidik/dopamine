package app.dopamine.domain.content

import app.dopamine.domain.model.CourseDto
import app.dopamine.domain.model.ImageDto
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.UnitDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestDiffTest {
    private fun ref(id: String, v: Int = 1, sha: String = "a") =
        LessonRef(id, id, "m/c/u/$id.json", v, sha, 100, 1)

    private fun manifest(vararg lessons: LessonRef, images: List<ImageDto> = emptyList()) = Manifest(
        1, "v", 1, emptyList(),
        listOf(CourseDto("c", "m", "C", "", 1, listOf(UnitDto("u", "U", 1, lessons.toList())))),
        images,
    )

    @Test fun newChangedRemovedUnchanged() {
        val local = manifest(ref("a"), ref("b"), ref("c"))
        val remote = manifest(ref("a"), ref("b", v = 2, sha = "z"), ref("d"))
        val d = ManifestDiff.compute(local, remote)
        assertEquals(listOf("d"), d.newLessons.map { it.id })
        assertEquals(listOf("b"), d.changedLessons.map { it.id })
        assertEquals(listOf("c"), d.removedIds)
        assertEquals(2, d.downloadCount)
    }

    @Test fun sameShaWithNewVersionStillCountsAsChanged() {
        val d = ManifestDiff.compute(manifest(ref("a")), manifest(ref("a", v = 2)))
        assertEquals(1, d.changedLessons.size)
    }

    @Test fun nothingChanged() {
        val m = manifest(ref("a"))
        assertEquals(0, ManifestDiff.compute(m, m).downloadCount)
    }

    @Test fun nullLocalMeansEverythingIsNew() {
        assertEquals(2, ManifestDiff.compute(null, manifest(ref("a"), ref("b"))).newLessons.size)
    }

    @Test fun missingImageFileIsRefetched() {
        val img = ImageDto("images/x.svg", "h", 10)
        val m = manifest(ref("a"), images = listOf(img))
        assertEquals(0, ManifestDiff.compute(m, m) { true }.imagesToFetch.size)
        assertEquals(1, ManifestDiff.compute(m, m) { false }.imagesToFetch.size)
    }

    @Test fun pathSafety() {
        assertTrue(SafePath.isSafe("math/algebra-1/linear-eq/lesson-01.json"))
        assertTrue(SafePath.isSafe("images/number-line.svg"))
        listOf(
            "../etc/passwd.json", "/abs/lesson.json", "a/../b.json", "a//b.json", "a/b.txt",
            "a/b.json/../../c.json", "..\\x.json", "a/.hidden.json", "",
        ).forEach { assertFalse("should reject $it", SafePath.isSafe(it)) }
    }
}
