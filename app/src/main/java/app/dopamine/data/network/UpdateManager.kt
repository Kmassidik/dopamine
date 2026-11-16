package app.dopamine.data.network

import app.dopamine.data.content.ContentPrune
import app.dopamine.data.content.ContentStore
import app.dopamine.domain.content.LessonValidator
import app.dopamine.domain.content.ManifestDiff
import app.dopamine.domain.content.SafePath
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Notes
import app.dopamine.domain.content.ManifestValidator
import app.dopamine.domain.content.NotesValidator
import app.dopamine.domain.model.Manifest
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.UnknownHostException
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class UpdateException(message: String) : Exception(message)

data class UpdateResult(val newCount: Int, val updatedCount: Int, val removedCount: Int, val upToDate: Boolean)

/**
 * User-initiated only. Downloads into a staged copy, verifies everything, then swaps atomically.
 * Any failure discards the stage and leaves the current content untouched.
 */
class UpdateManager(
    private val store: ContentStore,
    private val client: OkHttpClient,
    private val baseUrl: () -> String,
    private val appVersion: Int,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private class Fetched(val code: Int, val bytes: ByteArray?, val etag: String?)

    suspend fun update(onProgress: (done: Int, total: Int) -> Unit): UpdateResult = withContext(Dispatchers.IO) {
        val base = baseUrl().trim().let { if (it.endsWith("/")) it else "$it/" }
        if (!base.startsWith("https://")) throw UpdateException("The content address must start with https://. Check it in Settings.")
        try {
            run(base, onProgress)
        } catch (e: UnknownHostException) {
            throw UpdateException("Offline, using local content.")
        } catch (e: IOException) {
            throw UpdateException("Could not reach the content server. Your lessons are unchanged. Try again later.")
        }
    }

    private suspend fun run(base: String, onProgress: (Int, Int) -> Unit): UpdateResult {
        val local = store.readManifest()
        onProgress(0, 0)
        val head = get(base + "manifest.json", MAX_MANIFEST, store.etag().takeIf { local != null })
        if (head.code == 304) return UpdateResult(0, 0, 0, upToDate = true)
        val manifestBytes = head.bytes ?: throw UpdateException("The server sent an empty manifest.")
        val remote: Manifest = try {
            store.parseManifest(manifestBytes)
        } catch (e: SerializationException) {
            throw UpdateException("The content manifest is not valid. Your lessons are unchanged.")
        }
        if (remote.schemaVersion != SUPPORTED_SCHEMA || remote.minAppVersion > appVersion) {
            throw UpdateException("This content needs a newer version of the app. Please update the app.")
        }
        ManifestValidator.validate(remote)?.let { throw UpdateException("The content manifest failed a check ($it). Your lessons are unchanged.") }
        if (local == remote) {
            store.saveMeta(head.etag, now())
            return UpdateResult(0, 0, 0, upToDate = true)
        }

        val diff = ManifestDiff.compute(local, remote) { store.hasFile(it) }
        val paths = diff.newLessons.map { it.path } + diff.changedLessons.map { it.path } + diff.imagesToFetch.map { it.path } + diff.notesToFetch.map { it.path }
        paths.firstOrNull { !SafePath.isSafe(it) }?.let { throw UpdateException("The manifest contains an unsafe path. Update stopped.") }

        val total = diff.downloadCount
        var done = 0
        onProgress(0, total)
        val stage = store.newStage()
        try {
            for (ref in diff.newLessons + diff.changedLessons) {
                currentCoroutineContext().ensureActive()
                val bytes = download(base, ref.path, ref.sha256, ref.sizeBytes, MAX_LESSON)
                val lesson: Lesson = try {
                    json.decodeFromString(bytes.decodeToString())
                } catch (e: SerializationException) {
                    throw UpdateException("Lesson \"${ref.title}\" is not valid. Update stopped, nothing changed.")
                }
                LessonValidator.validate(lesson, ref, appVersion)?.let {
                    throw UpdateException("Lesson \"${ref.title}\" failed a check ($it). Update stopped, nothing changed.")
                }
                store.writeStaged(stage, ref.path, bytes)
                onProgress(++done, total)
            }
            for (ref in diff.notesToFetch) {
                currentCoroutineContext().ensureActive()
                val bytes = download(base, ref.path, ref.sha256, ref.sizeBytes, MAX_LESSON)
                val notes: Notes = try {
                    json.decodeFromString(bytes.decodeToString())
                } catch (e: SerializationException) {
                    throw UpdateException("Notes \"${ref.title}\" are not valid. Update stopped, nothing changed.")
                }
                NotesValidator.validate(notes, ref, appVersion)?.let {
                    throw UpdateException("Notes \"${ref.title}\" failed a check ($it). Update stopped, nothing changed.")
                }
                store.writeStaged(stage, ref.path, bytes)
                onProgress(++done, total)
            }
            for (img in diff.imagesToFetch) {
                currentCoroutineContext().ensureActive()
                store.writeStaged(stage, img.path, download(base, img.path, img.sha256, img.sizeBytes, MAX_IMAGE))
                onProgress(++done, total)
            }
            store.writeStaged(stage, "manifest.json", manifestBytes)
            ContentPrune.prune(stage, ManifestDiff.referencedPaths(remote))
            currentCoroutineContext().ensureActive()
            store.swapIn(stage)
        } catch (e: Throwable) {
            store.discardStage()
            throw e
        }
        store.saveMeta(head.etag, now())
        return UpdateResult(diff.newLessons.size, diff.changedLessons.size + diff.notesToFetch.size, diff.removedIds.size, upToDate = false)
    }

    private fun download(base: String, path: String, sha: String, declared: Long, max: Int): ByteArray {
        if (declared <= 0 || declared > max) throw UpdateException("A file in the manifest is too large. Update stopped.")
        val bytes = get(base + path, max, null).bytes ?: throw UpdateException("The server sent an empty file.")
        if (bytes.size.toLong() != declared || sha256(bytes) != sha) {
            throw UpdateException("A downloaded file failed its integrity check. Update stopped, nothing changed.")
        }
        return bytes
    }

    private fun get(url: String, max: Int, etag: String?): Fetched {
        var attempt = 0
        while (true) {
            try {
                return once(url, max, etag)
            } catch (e: UnknownHostException) {
                throw e
            } catch (e: IOException) {
                if (++attempt >= ATTEMPTS) throw e
                Thread.sleep(400L shl attempt)
            }
        }
    }

    private fun once(url: String, max: Int, etag: String?): Fetched {
        val req = Request.Builder().url(url).apply { if (etag != null) header("If-None-Match", etag) }.build()
        client.newCall(req).execute().use { resp ->
            if (!resp.request.url.isHttps) throw UpdateException("The server redirected to an insecure address. Update stopped.")
            if (resp.code == 304) return Fetched(304, null, etag)
            if (!resp.isSuccessful) throw UpdateException("The content server answered ${resp.code}. Try again later.")
            val out = ByteArrayOutputStream()
            val buf = ByteArray(8192)
            resp.body.byteStream().use { input ->
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    if (out.size() > max) throw UpdateException("A file is larger than allowed. Update stopped.")
                }
            }
            return Fetched(resp.code, out.toByteArray(), resp.header("ETag"))
        }
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    companion object {
        const val SUPPORTED_SCHEMA = 1
        const val MAX_MANIFEST = 512 * 1024
        const val MAX_LESSON = 256 * 1024
        const val MAX_IMAGE = 1024 * 1024
        const val ATTEMPTS = 3
    }
}
