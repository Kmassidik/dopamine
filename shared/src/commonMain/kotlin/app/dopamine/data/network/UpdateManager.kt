package app.dopamine.data.network

import app.dopamine.data.content.ContentStore
import app.dopamine.domain.content.LessonValidator
import app.dopamine.domain.content.ManifestDiff
import app.dopamine.domain.content.ManifestValidator
import app.dopamine.domain.content.NotesValidator
import app.dopamine.domain.content.SafePath
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.Notes
import app.dopamine.platform.nowMillis
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.http.URLProtocol
import io.ktor.http.takeFrom
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.toByteString

class UpdateException(message: String) : Exception(message)

data class UpdateResult(val newCount: Int, val updatedCount: Int, val removedCount: Int, val upToDate: Boolean)

/**
 * User-initiated only. Downloads into a staged copy, verifies everything, then swaps atomically.
 * Any failure discards the stage and leaves the current content untouched.
 *
 * The [client] must be created with redirects turned off ([createHttpClient] does): this class follows them itself
 * so that a redirect to a non-https address can be refused.
 */
class UpdateManager(
    private val store: ContentStore,
    private val client: HttpClient,
    private val baseUrl: () -> String,
    private val appVersion: Int,
    private val now: () -> Long = ::nowMillis,
    private val retryDelayMs: (attempt: Int) -> Long = { 400L shl it },
) {
    private val json = Json { ignoreUnknownKeys = true }

    private class Fetched(val code: Int, val bytes: ByteArray?, val etag: String?)

    suspend fun update(onProgress: (done: Int, total: Int) -> Unit): UpdateResult = withContext(Dispatchers.Default) {
        val base = baseUrl().trim().let { if (it.endsWith("/")) it else "$it/" }
        if (!base.startsWith("https://")) throw UpdateException("The content address must start with https://. Check it in Settings.")
        try {
            run(base, onProgress)
        } catch (e: CancellationException) {
            throw e
        } catch (e: UpdateException) {
            throw e
        } catch (e: Exception) {
            if (isHostUnreachable(e)) throw UpdateException("Offline, using local content.")
            if (isNetworkError(e)) throw UpdateException("Could not reach the content server. Your lessons are unchanged. Try again later.")
            throw e
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
            currentCoroutineContext().ensureActive()
            store.swapIn(stage)
        } catch (e: Throwable) {
            store.discardStage()
            throw e
        }
        store.saveMeta(head.etag, now())
        return UpdateResult(diff.newLessons.size, diff.changedLessons.size + diff.notesToFetch.size, diff.removedIds.size, upToDate = false)
    }

    private suspend fun download(base: String, path: String, sha: String, declared: Long, max: Int): ByteArray {
        if (declared <= 0 || declared > max) throw UpdateException("A file in the manifest is too large. Update stopped.")
        val bytes = get(base + path, max, null).bytes ?: throw UpdateException("The server sent an empty file.")
        if (bytes.size.toLong() != declared || sha256(bytes) != sha) {
            throw UpdateException("A downloaded file failed its integrity check. Update stopped, nothing changed.")
        }
        return bytes
    }

    private suspend fun get(url: String, max: Int, etag: String?): Fetched {
        var attempt = 0
        while (true) {
            try {
                return once(url, max, etag)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isHostUnreachable(e) || !isNetworkError(e)) throw e
                if (++attempt >= ATTEMPTS) throw e
                delay(retryDelayMs(attempt))
            }
        }
    }

    /** One request. Redirects are followed by hand, https to https only. */
    private suspend fun once(startUrl: String, max: Int, etag: String?): Fetched {
        var url = startUrl
        var hops = 0
        while (true) {
            if (!url.startsWith("https://")) throw UpdateException("The server redirected to an insecure address. Update stopped.")
            val result: Any = client.prepareGet(url) { if (etag != null) header(HttpHeaders.IfNoneMatch, etag) }.execute { resp ->
                val code = resp.status.value
                when {
                    code in REDIRECTS -> resp.headers[HttpHeaders.Location] ?: throw UpdateException("The server sent a broken redirect. Update stopped.")
                    code == 304 -> Fetched(304, null, etag)
                    !resp.status.isSuccess() -> throw UpdateException("The content server answered $code. Try again later.")
                    else -> Fetched(code, readCapped(resp, max), resp.headers[HttpHeaders.ETag])
                }
            }
            if (result is Fetched) return result
            if (++hops > MAX_REDIRECTS) throw UpdateException("The server redirected too many times. Update stopped.")
            url = resolve(url, result as String)
        }
    }

    private fun resolve(current: String, location: String): String =
        try {
            URLBuilderFor(current, location)
        } catch (e: Exception) {
            throw UpdateException("The server sent a broken redirect. Update stopped.")
        }

    private suspend fun readCapped(resp: HttpResponse, max: Int): ByteArray {
        val declared = resp.headers[HttpHeaders.ContentLength]?.toLongOrNull()
        if (declared != null && declared > max) throw UpdateException("A file is larger than allowed. Update stopped.")
        val channel = resp.bodyAsChannel()
        val out = okio.Buffer()
        val buf = ByteArray(8192)
        while (true) {
            val n = channel.readAvailable(buf, 0, buf.size)
            if (n < 0) break
            out.write(buf, 0, n)
            if (out.size > max) throw UpdateException("A file is larger than allowed. Update stopped.")
        }
        return out.readByteArray()
    }

    private fun sha256(bytes: ByteArray): String = bytes.toByteString().sha256().hex()

    companion object {
        const val SUPPORTED_SCHEMA = 1
        const val MAX_MANIFEST = 512 * 1024
        const val MAX_LESSON = 256 * 1024
        const val MAX_IMAGE = 1024 * 1024
        const val ATTEMPTS = 3
        private const val MAX_REDIRECTS = 5
        private val REDIRECTS = setOf(301, 302, 303, 307, 308)
    }
}

/** Resolves a redirect target (absolute or relative) against the URL it came from. */
internal fun URLBuilderFor(current: String, location: String): String =
    io.ktor.http.URLBuilder(Url(current)).takeFrom(location).buildString()

/** The platform's "no network / unknown host" errors, which are not worth retrying. */
internal expect fun isHostUnreachable(e: Throwable): Boolean

/** Errors worth retrying: connection trouble and timeouts. */
internal fun isNetworkError(e: Throwable): Boolean =
    e is IOException || e is io.ktor.client.plugins.HttpRequestTimeoutException ||
        e is io.ktor.client.network.sockets.ConnectTimeoutException || e is io.ktor.client.network.sockets.SocketTimeoutException
