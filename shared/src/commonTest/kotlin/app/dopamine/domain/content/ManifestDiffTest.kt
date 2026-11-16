package app.dopamine.domain.content

import app.dopamine.domain.model.CourseDto
import app.dopamine.domain.model.ImageDto
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.NotesRef
import app.dopamine.domain.model.UnitDto
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

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

    @Test fun lessonThatMovedToANewPathIsRefetchedEvenWhenVersionAndHashAreUnchanged() {
        val local = manifest(LessonRef("a", "a", "old/path/a.json", 1, "same", 100, 1))
        val remote = manifest(LessonRef("a", "a", "content/new/path/a.json", 1, "same", 100, 1))
        val onDisk = setOf("old/path/a.json")
        val d = ManifestDiff.compute(local, remote) { it in onDisk }
        assertEquals(listOf("a"), d.changedLessons.map { it.id })
        assertEquals(emptyList(), d.newLessons)
        assertEquals(emptyList(), d.removedIds)
    }

    @Test fun unchangedLessonWhoseFileIsPresentIsNotRefetched() {
        val m = manifest(ref("a"))
        assertEquals(0, ManifestDiff.compute(m, m) { true }.changedLessons.size)
    }

    @Test fun referencedPathsListsLessonsNotesImagesAndTheManifest() {
        val m = Manifest(
            1, "v", 1, emptyList(),
            listOf(CourseDto("c", "m", "C", "", 1, listOf(UnitDto("u", "U", 1, listOf(ref("a"), ref("b")), NotesRef("n", "N", "m/c/u/notes.json", 1, "n", 10))))),
            listOf(ImageDto("content/images/x.svg", "h", 10)),
        )
        assertEquals(
            setOf("manifest.json", "m/c/u/a.json", "m/c/u/b.json", "m/c/u/notes.json", "content/images/x.svg"),
            ManifestDiff.referencedPaths(m),
        )
    }

    @Test fun pathSafety() {
        assertTrue(SafePath.isSafe("math/algebra-1/linear-eq/lesson-01.json"))
        assertTrue(SafePath.isSafe("images/number-line.svg"))
        assertTrue(SafePath.isSafe("content/math/algebra-1/linear-eq/lesson-01.json"))
        assertTrue(SafePath.isSafe("content/images/number-line.svg"))
        listOf(
            "../etc/passwd.json", "/abs/lesson.json", "a/../b.json", "a//b.json", "a/b.txt",
            "a/b.json/../../c.json", "..\\x.json", "a/.hidden.json", "",
        ).forEach { assertFalse(SafePath.isSafe(it), "should reject $it") }
    }

    private fun withNotes(n: NotesRef?) = Manifest(
        1, "v", 1, emptyList(),
        listOf(CourseDto("c", "m", "C", "", 1, listOf(UnitDto("u", "U", 1, listOf(ref("a")), n)))),
        emptyList(),
    )

    private fun notes(v: Int = 1, sha: String = "n") = NotesRef("n-1", "Notes", "m/c/u/notes.json", v, sha, 100)

    @Test fun newNotesAreFetched() {
        val d = ManifestDiff.compute(withNotes(null), withNotes(notes()))
        assertEquals(1, d.notesToFetch.size)
        assertEquals(1, d.downloadCount)
    }

    @Test fun changedNotesAreFetchedButUnchangedAreNot() {
        assertEquals(1, ManifestDiff.compute(withNotes(notes()), withNotes(notes(v = 2))).notesToFetch.size)
        assertEquals(1, ManifestDiff.compute(withNotes(notes()), withNotes(notes(sha = "z"))).notesToFetch.size)
        assertEquals(0, ManifestDiff.compute(withNotes(notes()), withNotes(notes())).notesToFetch.size)
    }

    @Test fun missingNotesFileIsFetchedAgain() {
        val d = ManifestDiff.compute(withNotes(notes()), withNotes(notes())) { false }
        assertEquals(1, d.notesToFetch.size)
    }
}
