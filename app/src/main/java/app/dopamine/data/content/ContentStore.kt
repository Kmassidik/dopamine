package app.dopamine.data.content

import android.content.Context
import android.util.Log
import app.dopamine.domain.content.LessonValidator
import app.dopamine.domain.content.NotesValidator
import app.dopamine.domain.content.SafePath
import app.dopamine.domain.model.Notes
import app.dopamine.domain.model.NotesRef
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import java.io.File
import kotlinx.serialization.json.Json

/** Owns filesDir/content. All content, bundled or downloaded, is read from here. */
class ContentStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val prefs = context.getSharedPreferences("content_meta", Context.MODE_PRIVATE)

    val root: File get() = File(context.filesDir, "content")
    private val stageDir: File get() = File(context.filesDir, "content.stage")
    private val oldDir: File get() = File(context.filesDir, "content.old")

    fun parseManifest(bytes: ByteArray): Manifest = json.decodeFromString(bytes.decodeToString())

    fun readManifest(): Manifest? = try {
        parseManifest(File(root, "manifest.json").readBytes())
    } catch (e: Exception) {
        Log.w(TAG, "manifest unreadable: ${e.message}")
        null
    }

    /** First launch, or a missing or corrupt content dir: copy the bundled pack into place. */
    @Synchronized
    fun ensureSeeded() {
        if (readManifest() != null) return
        val tmp = File(context.filesDir, "content.seed")
        tmp.deleteRecursively()
        copyAsset("content", tmp)
        root.deleteRecursively()
        check(tmp.renameTo(root)) { "could not place bundled content" }
        Log.i(TAG, "seeded from bundled content")
    }

    private fun copyAsset(path: String, dest: File) {
        val children = context.assets.list(path).orEmpty()
        if (children.isEmpty()) {
            dest.parentFile?.mkdirs()
            context.assets.open(path).use { i -> dest.outputStream().use { o -> i.copyTo(o) } }
        } else {
            dest.mkdirs()
            children.forEach { copyAsset("$path/$it", File(dest, it)) }
        }
    }

    fun readLesson(ref: LessonRef): Lesson? = try {
        val lesson: Lesson = json.decodeFromString(File(root, ref.path).readText())
        val problem = LessonValidator.validate(lesson, null)
        if (problem != null) {
            Log.w(TAG, "skipping ${ref.id}: $problem")
            null
        } else {
            lesson
        }
    } catch (e: Exception) {
        Log.w(TAG, "skipping ${ref.id}: ${e.message}")
        null
    }

    fun readNotes(ref: NotesRef): Notes? = try {
        val notes: Notes = json.decodeFromString(File(root, ref.path).readText())
        val problem = NotesValidator.validate(notes, null)
        if (problem != null) { Log.w(TAG, "skipping notes ${ref.id}: $problem"); null } else notes
    } catch (e: Exception) {
        Log.w(TAG, "skipping notes ${ref.id}: ${e.message}")
        null
    }

    /** The text of an image file, only for a plain relative `.svg` path and a small file. */
    fun readImageText(path: String): String? = try {
        if (!SafePath.isSafe(path) || !path.endsWith(".svg")) null
        else File(root, path).takeIf { it.isFile && it.length() <= 16 * 1024 }?.readText()
    } catch (e: Exception) {
        Log.w(TAG, "image unreadable $path: ${e.message}")
        null
    }

    fun hasFile(rel: String) = File(root, rel).isFile

    // --- update support -----------------------------------------------------

    /** A full copy of the current content that downloads are applied to before the swap. */
    fun newStage(): File {
        stageDir.deleteRecursively()
        root.copyRecursively(stageDir, overwrite = true)
        return stageDir
    }

    fun writeStaged(stage: File, rel: String, bytes: ByteArray) {
        val f = File(stage, rel)
        check(f.canonicalPath.startsWith(stage.canonicalPath + File.separator)) { "path escapes stage" }
        f.parentFile?.mkdirs()
        f.writeBytes(bytes)
    }

    fun discardStage() {
        stageDir.deleteRecursively()
    }

    /** Swap staged content in. On failure the old content is put back. */
    @Synchronized
    fun swapIn(stage: File) {
        oldDir.deleteRecursively()
        if (root.exists() && !root.renameTo(oldDir)) error("could not move current content aside")
        if (!stage.renameTo(root)) {
            oldDir.renameTo(root)
            error("could not move new content into place")
        }
        oldDir.deleteRecursively()
    }

    fun etag(): String? = prefs.getString("etag", null)
    fun lastUpdatedAt(): Long = prefs.getLong("updatedAt", 0L)
    fun saveMeta(etag: String?, now: Long) {
        prefs.edit().putString("etag", etag).putLong("updatedAt", now).apply()
    }

    private companion object {
        const val TAG = "ContentStore"
    }
}
