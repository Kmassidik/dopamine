package app.dopamine.data.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ContentPruneTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun file(root: File, rel: String) = File(root, rel).apply { parentFile.mkdirs(); writeText("x") }

    @Test fun keepsReferencedFilesAndDeletesTheRestAndEmptyFolders() {
        val root = tmp.newFolder("content")
        file(root, "manifest.json")
        file(root, "content/math/a/lesson-01.json")
        file(root, "content/images/x.svg")
        file(root, "math/a/lesson-01.json")          // old path, no longer referenced
        file(root, "images/x.svg")                   // old path, no longer referenced
        val keep = setOf("manifest.json", "content/math/a/lesson-01.json", "content/images/x.svg")

        assertEquals(2, ContentPrune.prune(root, keep))

        assertTrue(File(root, "manifest.json").isFile)
        assertTrue(File(root, "content/math/a/lesson-01.json").isFile)
        assertTrue(File(root, "content/images/x.svg").isFile)
        assertFalse(File(root, "math").exists())
        assertFalse(File(root, "images").exists())
    }

    @Test fun nothingToDeleteLeavesEverythingAlone() {
        val root = tmp.newFolder("content")
        file(root, "manifest.json")
        file(root, "content/a.json")
        assertEquals(0, ContentPrune.prune(root, setOf("manifest.json", "content/a.json")))
        assertTrue(File(root, "content/a.json").isFile)
    }

    @Test fun missingFolderIsFine() {
        assertEquals(0, ContentPrune.prune(File(tmp.root, "nope"), emptySet()))
    }
}
