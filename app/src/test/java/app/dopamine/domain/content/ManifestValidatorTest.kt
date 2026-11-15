package app.dopamine.domain.content

import app.dopamine.domain.model.CategoryDto
import app.dopamine.domain.model.CourseDto
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.Overview
import app.dopamine.domain.model.UnitDto
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ManifestValidatorTest {
    private val sha = "a".repeat(64)
    private fun ref(id: String = "l1", path: String = "m/c/u/lesson-01.json", sha: String = this.sha, minApp: Int = 1) =
        LessonRef(id, "T", path, 1, sha, 10, 1, minApp)

    private fun manifest(ref: LessonRef = ref(), ov: Overview? = null, cat: String = "m") = Manifest(
        1, "v", 1, listOf(CategoryDto("m", "M", "ic_math", "#000000", 1)),
        listOf(CourseDto("c", cat, "C", "", 1, listOf(UnitDto("u", "U", 1, listOf(ref))), ov)), emptyList(),
    )

    @Test fun validManifestPasses() = assertNull(ManifestValidator.validate(manifest()))
    @Test fun unsafePathFails() = assertNotNull(ManifestValidator.validate(manifest(ref(path = "../x.json"))))
    @Test fun badHashFails() = assertNotNull(ManifestValidator.validate(manifest(ref(sha = "zz"))))
    @Test fun unknownCategoryFails() = assertNotNull(ManifestValidator.validate(manifest(cat = "nope")))
    @Test fun overlongSummaryFails() = assertNotNull(ManifestValidator.validate(manifest(ov = Overview("x".repeat(401)))))
    @Test fun badMinAppFails() = assertNotNull(ManifestValidator.validate(manifest(ref(minApp = 0))))
}
