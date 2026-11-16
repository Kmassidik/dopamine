package app.dopamine.data

import app.dopamine.data.content.ContentStore
import app.dopamine.data.network.UpdateException
import app.dopamine.data.network.UpdateManager
import app.dopamine.domain.model.CategoryDto
import app.dopamine.domain.model.CourseDto
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import app.dopamine.domain.model.Question
import app.dopamine.domain.model.UnitDto
import app.dopamine.platform.KeyValueStore
import app.dopamine.platform.SeedSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.toByteString
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private class MemoryKeyValueStore : KeyValueStore {
    val map = HashMap<String, Any?>()
    override fun getString(key: String) = map[key] as String?
    override fun getLong(key: String, default: Long) = map[key] as Long? ?: default
    override fun putString(key: String, value: String?) { map[key] = value }
    override fun putLong(key: String, value: Long) { map[key] = value }
}

private class MapSeed(private val files: Map<String, ByteArray>) : SeedSource {
    override fun children(path: String): List<String> {
        val prefix = if (path.isEmpty()) "" else "$path/"
        return files.keys.filter { it.startsWith(prefix) && it != path }.map { it.removePrefix(prefix).substringBefore('/') }.distinct()
    }
    override fun read(path: String) = files.getValue(path)
}

class UpdateManagerTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val base = "https://content.example/"
    private val l1Path = "m/c/u/lesson-01.json"
    private val l2Path = "m/c/u/lesson-02.json"

    private fun lesson(id: String) = Lesson(id, 1, "Title $id", 10, questions = listOf(
        Question("q1", "multiple_choice", prompt = "p", choices = listOf("a", "b"), answerIndex = 0, explanation = "e"),
    ))

    private fun bytes(l: Lesson) = json.encodeToString(l).encodeToByteArray()
    private fun sha(b: ByteArray) = b.toByteString().sha256().hex()
    private fun ref(id: String, path: String, b: ByteArray, order: Int, declared: Long = b.size.toLong()) =
        LessonRef(id, "Title $id", path, 1, sha(b), declared, order)

    private fun manifest(vararg refs: LessonRef, minApp: Int = 1) = Manifest(
        1, "v", minApp, listOf(CategoryDto("m", "M", "ic_math", "#000000", 1)),
        listOf(CourseDto("c", "m", "C", "", 1, listOf(UnitDto("u", "U", 1, refs.toList())))), emptyList(),
    )

    private val l1 = bytes(lesson("l1"))
    private val l2 = bytes(lesson("l2"))
    private val localManifest = manifest(ref("l1", l1Path, l1, 1))
    private val remoteManifest = manifest(ref("l1", l1Path, l1, 1), ref("l2", l2Path, l2, 2))

    private class Env(val store: ContentStore, val meta: MemoryKeyValueStore, val fs: FakeFileSystem)

    private fun env(): Env {
        val fs = FakeFileSystem()
        val meta = MemoryKeyValueStore()
        val seed = MapSeed(mapOf("manifest.json" to json.encodeToString(localManifest).encodeToByteArray(), l1Path to l1))
        val store = ContentStore(fs, "/data".toPath(), seed, meta)
        store.ensureSeeded()
        return Env(store, meta, fs)
    }

    private fun manager(env: Env, appVersion: Int = 1, handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): UpdateManager {
        val client = HttpClient(MockEngine(handler)) { followRedirects = false; expectSuccess = false }
        return UpdateManager(env.store, client, { base }, appVersion, now = { 1234L }, retryDelayMs = { 0L })
    }

    private fun MockRequestHandleScope.ok(b: ByteArray, etag: String? = null) =
        respond(b, HttpStatusCode.OK, if (etag == null) headersOf() else headersOf(HttpHeaders.ETag, etag))

    private fun serve(files: Map<String, ByteArray>, manifest: Manifest = remoteManifest): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { req ->
        val path = req.url.encodedPath.removePrefix("/")
        when {
            path == "manifest.json" -> ok(json.encodeToString(manifest).encodeToByteArray(), "\"e2\"")
            files.containsKey(path) -> ok(files.getValue(path))
            else -> respond(ByteArray(0), HttpStatusCode.NotFound)
        }
    }

    private fun assertOldContentIntact(e: Env) {
        assertEquals(localManifest, e.store.readManifest())
        assertTrue(e.store.hasFile(l1Path))
        assertFalse(e.store.hasFile(l2Path))
        assertFalse(e.fs.exists("/data/content.stage".toPath()), "stage must be cleaned up")
    }

    @Test fun newLessonIsDownloadedVerifiedAndSwappedIn() = runTest {
        val e = env()
        val progress = ArrayList<Pair<Int, Int>>()
        val r = manager(e, handler = serve(mapOf(l2Path to l2))).update { d, t -> progress += d to t }
        assertEquals(1, r.newCount)
        assertFalse(r.upToDate)
        assertEquals(remoteManifest, e.store.readManifest())
        assertTrue(e.store.hasFile(l2Path))
        assertEquals("\"e2\"", e.store.etag())
        assertEquals(1234L, e.store.lastUpdatedAt())
        assertEquals(1 to 1, progress.last())
    }

    @Test fun badHashRollsBack() = runTest {
        val e = env()
        val tampered = bytes(lesson("l2")).also { it[it.size - 3] = 'X'.code.toByte() } // same size, different bytes
        val ex = assertFailsWith<UpdateException> { manager(e, handler = serve(mapOf(l2Path to tampered))).update { _, _ -> } }
        assertTrue("integrity" in ex.message!!, ex.message)
        assertOldContentIntact(e)
        assertEquals(null, e.store.etag())
    }

    @Test fun interruptedDownloadKeepsOldContent() = runTest {
        val e = env()
        var attempts = 0
        val ex = assertFailsWith<UpdateException> {
            manager(e) { req ->
                if (req.url.encodedPath.endsWith("manifest.json")) ok(json.encodeToString(remoteManifest).encodeToByteArray())
                else { attempts++; throw IOException("connection reset") }
            }.update { _, _ -> }
        }
        assertTrue("Could not reach" in ex.message!!, ex.message)
        assertEquals(UpdateManager.ATTEMPTS, attempts)
        assertOldContentIntact(e)
    }

    @Test fun redirectToHttpIsRefused() = runTest {
        val e = env()
        val ex = assertFailsWith<UpdateException> {
            manager(e) { respond(ByteArray(0), HttpStatusCode.Found, headersOf(HttpHeaders.Location, "http://evil.example/manifest.json")) }.update { _, _ -> }
        }
        assertTrue("insecure" in ex.message!!, ex.message)
        assertOldContentIntact(e)
    }

    @Test fun redirectToHttpsIsFollowed() = runTest {
        val e = env()
        val handler = serve(mapOf(l2Path to l2))
        val r = manager(e) { req ->
            if (req.url.host == "content.example") respond(ByteArray(0), HttpStatusCode.MovedPermanently, headersOf(HttpHeaders.Location, "https://mirror.example/" + req.url.encodedPath.removePrefix("/")))
            else handler(req)
        }.update { _, _ -> }
        assertEquals(1, r.newCount)
    }

    @Test fun httpBaseUrlIsRefusedBeforeAnyRequest() = runTest {
        val e = env()
        var called = false
        val client = HttpClient(MockEngine { called = true; respond("") }) { followRedirects = false }
        val m = UpdateManager(e.store, client, { "http://content.example/" }, 1)
        assertFailsWith<UpdateException> { m.update { _, _ -> } }
        assertFalse(called)
    }

    @Test fun fileLargerThanAllowedIsRejected() = runTest {
        val e = env()
        val big = ByteArray(UpdateManager.MAX_LESSON + 1) { 'a'.code.toByte() }
        // The manifest claims a small file, but the server sends far more.
        val m = manifest(ref("l1", l1Path, l1, 1), ref("l2", l2Path, l2, 2))
        val ex = assertFailsWith<UpdateException> { manager(e, handler = serve(mapOf(l2Path to big), m)).update { _, _ -> } }
        assertTrue("larger than allowed" in ex.message!!, ex.message)
        assertOldContentIntact(e)
    }

    @Test fun manifestDeclaringAHugeFileIsRejected() = runTest {
        val e = env()
        val m = manifest(ref("l1", l1Path, l1, 1), ref("l2", l2Path, l2, 2, declared = UpdateManager.MAX_LESSON + 1L))
        val ex = assertFailsWith<UpdateException> { manager(e, handler = serve(mapOf(l2Path to l2), m)).update { _, _ -> } }
        assertTrue("too large" in ex.message!!, ex.message)
        assertOldContentIntact(e)
    }

    @Test fun notModifiedMeansUpToDate() = runTest {
        val e = env()
        e.store.saveMeta("\"e1\"", 1L)
        var sentEtag: String? = null
        val r = manager(e) { req -> sentEtag = req.headers[HttpHeaders.IfNoneMatch]; respond(ByteArray(0), HttpStatusCode.NotModified) }.update { _, _ -> }
        assertTrue(r.upToDate)
        assertEquals("\"e1\"", sentEtag)
        assertOldContentIntact(e)
    }

    @Test fun serverErrorLeavesContentAlone() = runTest {
        val e = env()
        val ex = assertFailsWith<UpdateException> { manager(e) { respond(ByteArray(0), HttpStatusCode.InternalServerError) }.update { _, _ -> } }
        assertTrue("500" in ex.message!!)
        assertOldContentIntact(e)
    }

    @Test fun contentNeedingANewerAppIsRefused() = runTest {
        val e = env()
        val ex = assertFailsWith<UpdateException> {
            manager(e, handler = serve(mapOf(l2Path to l2), manifest(ref("l1", l1Path, l1, 1), minApp = 99))).update { _, _ -> }
        }
        assertTrue("newer version" in ex.message!!)
        assertOldContentIntact(e)
    }

    @Test fun lessonThatFailsValidationIsRejected() = runTest {
        val e = env()
        val bad = bytes(lesson("l2").copy(xp = 5000))
        val m = manifest(ref("l1", l1Path, l1, 1), ref("l2", l2Path, bad, 2))
        val ex = assertFailsWith<UpdateException> { manager(e, handler = serve(mapOf(l2Path to bad), m)).update { _, _ -> } }
        assertTrue("failed a check" in ex.message!!, ex.message)
        assertOldContentIntact(e)
        assertNotNull(e.store.readManifest())
    }
}
