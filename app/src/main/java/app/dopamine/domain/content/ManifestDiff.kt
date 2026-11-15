package app.dopamine.domain.content

import app.dopamine.domain.model.ImageDto
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest

data class DiffResult(
    val newLessons: List<LessonRef>,
    val changedLessons: List<LessonRef>,
    val removedIds: List<String>,
    val imagesToFetch: List<ImageDto>,
) {
    val downloadCount get() = newLessons.size + changedLessons.size + imagesToFetch.size
}

object ManifestDiff {
    fun lessons(m: Manifest): List<LessonRef> = m.courses.flatMap { it.units }.flatMap { it.lessons }

    /** [hasFile] reports whether an image is already on disk, so missing files get re-fetched. */
    fun compute(local: Manifest?, remote: Manifest, hasFile: (String) -> Boolean = { true }): DiffResult {
        val localLessons = local?.let { lessons(it) }.orEmpty().associateBy { it.id }
        val remoteLessons = lessons(remote)
        val new = remoteLessons.filter { it.id !in localLessons }
        val changed = remoteLessons.filter { r ->
            localLessons[r.id]?.let { l -> l.version != r.version || l.sha256 != r.sha256 } == true
        }
        val removed = localLessons.keys - remoteLessons.map { it.id }.toSet()
        val localImages = local?.images.orEmpty().associateBy { it.path }
        val images = remote.images.filter { img ->
            localImages[img.path]?.sha256 != img.sha256 || !hasFile(img.path)
        }
        return DiffResult(new, changed, removed.sorted(), images)
    }
}

/** Manifest paths are untrusted. Only plain relative `a/b/c.json|svg` is accepted. */
object SafePath {
    private val pattern = Regex("^[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+)*\\.(json|svg)$")
    fun isSafe(path: String): Boolean = pattern.matches(path)
}
