package app.dopamine.data.content

import app.dopamine.domain.content.LessonValidator
import app.dopamine.domain.content.NotesValidator
import app.dopamine.domain.content.SafePath
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.Notes
import app.dopamine.domain.model.NotesRef
import app.dopamine.platform.KeyValueStore
import app.dopamine.platform.SeedSource
import app.dopamine.platform.SimpleLock
import app.dopamine.platform.logI
import app.dopamine.platform.logW
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

/**
 * Owns <filesDir>/content. All content, bundled or downloaded, is read from here.
 * [fs] and [filesDir] are the platform's file system and private app folder; [seed] is the bundled content;
 * [meta] keeps the ETag and the time of the last update.
 */
class ContentStore(
    private val fs: FileSystem,
    private val filesDir: Path,
    private val seed: SeedSource,
    private val meta: KeyValueStore,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val lock = SimpleLock()

    val root: Path get() = filesDir / "content"
    private val stageDir: Path get() = filesDir / "content.stage"
    private val oldDir: Path get() = filesDir / "content.old"

    fun parseManifest(bytes: ByteArray): Manifest = json.decodeFromString(bytes.decodeToString())

    fun readManifest(): Manifest? = try {
        parseManifest(fs.read(root / "manifest.json") { readByteArray() })
    } catch (e: Exception) {
        logW(TAG, "manifest unreadable: ${e.message}")
        null
    }

    /** First launch, or a missing or corrupt content dir: copy the bundled pack into place. */
    fun ensureSeeded() = lock.withLock {
        if (readManifest() != null) return@withLock
        val tmp = filesDir / "content.seed"
        fs.deleteRecursively(tmp)
        fs.createDirectories(filesDir)
        copySeed("", tmp)
        fs.deleteRecursively(root)
        fs.atomicMove(tmp, root)
        logI(TAG, "seeded from bundled content")
    }

    private fun copySeed(path: String, dest: Path) {
        val children = seed.children(path)
        if (children.isEmpty()) {
            dest.parent?.let { fs.createDirectories(it) }
            fs.write(dest) { write(seed.read(path)) }
        } else {
            fs.createDirectories(dest)
            children.forEach { copySeed(if (path.isEmpty()) it else "$path/$it", dest / it) }
        }
    }

    private fun readText(rel: String): String = fs.read(root / rel) { readUtf8() }

    fun readLesson(ref: LessonRef): Lesson? = try {
        val lesson: Lesson = json.decodeFromString(readText(ref.path))
        val problem = LessonValidator.validate(lesson, null)
        if (problem != null) {
            logW(TAG, "skipping ${ref.id}: $problem")
            null
        } else {
            lesson
        }
    } catch (e: Exception) {
        logW(TAG, "skipping ${ref.id}: ${e.message}")
        null
    }

    fun readNotes(ref: NotesRef): Notes? = try {
        val notes: Notes = json.decodeFromString(readText(ref.path))
        val problem = NotesValidator.validate(notes, null)
        if (problem != null) { logW(TAG, "skipping notes ${ref.id}: $problem"); null } else notes
    } catch (e: Exception) {
        logW(TAG, "skipping notes ${ref.id}: ${e.message}")
        null
    }

    /** The text of an image file, only for a plain relative `.svg` path and a small file. */
    fun readImageText(path: String): String? = try {
        if (!SafePath.isSafe(path) || !path.endsWith(".svg")) {
            null
        } else {
            val meta = fs.metadataOrNull(root / path)
            if (meta?.isRegularFile == true && (meta.size ?: Long.MAX_VALUE) <= 16 * 1024) readText(path) else null
        }
    } catch (e: Exception) {
        logW(TAG, "image unreadable $path: ${e.message}")
        null
    }

    fun hasFile(rel: String) = fs.metadataOrNull(root / rel)?.isRegularFile == true

    // --- update support -----------------------------------------------------

    /** A full copy of the current content that downloads are applied to before the swap. */
    fun newStage(): Path {
        fs.deleteRecursively(stageDir)
        copyTree(root, stageDir)
        return stageDir
    }

    private fun copyTree(from: Path, to: Path) {
        val m = fs.metadataOrNull(from) ?: return
        if (m.isDirectory) {
            fs.createDirectories(to)
            fs.list(from).forEach { copyTree(it, to / it.name) }
        } else {
            to.parent?.let { fs.createDirectories(it) }
            fs.copy(from, to)
        }
    }

    fun writeStaged(stage: Path, rel: String, bytes: ByteArray) {
        // The manifest checks paths with SafePath already; this is the second lock on the same door.
        require(SafePath.isSafe(rel) || rel == "manifest.json") { "path escapes stage" }
        val f = stage / rel
        val normalized = f.normalized()
        check(normalized.toString().startsWith(stage.normalized().toString() + "/")) { "path escapes stage" }
        normalized.parent?.let { fs.createDirectories(it) }
        fs.write(normalized) { write(bytes) }
    }

    fun discardStage() {
        fs.deleteRecursively(stageDir)
    }

    /** Swap staged content in. On failure the old content is put back. */
    fun swapIn(stage: Path) = lock.withLock {
        fs.deleteRecursively(oldDir)
        if (fs.exists(root)) {
            try {
                fs.atomicMove(root, oldDir)
            } catch (e: Exception) {
                error("could not move current content aside")
            }
        }
        try {
            fs.atomicMove(stage, root)
        } catch (e: Exception) {
            if (fs.exists(oldDir)) fs.atomicMove(oldDir, root)
            error("could not move new content into place")
        }
        fs.deleteRecursively(oldDir)
    }

    fun etag(): String? = meta.getString("etag")
    fun lastUpdatedAt(): Long = meta.getLong("updatedAt", 0L)
    fun saveMeta(etag: String?, now: Long) {
        meta.putString("etag", etag)
        meta.putLong("updatedAt", now)
    }

    private companion object {
        const val TAG = "ContentStore"
    }
}
