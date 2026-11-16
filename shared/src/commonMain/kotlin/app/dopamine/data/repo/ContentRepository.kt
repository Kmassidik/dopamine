package app.dopamine.data.repo

import app.dopamine.data.content.ContentStore
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import app.dopamine.platform.SimpleLock
import app.dopamine.platform.IoDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ContentRepository(private val store: ContentStore) {
    private val _manifest = MutableStateFlow<Manifest?>(null)
    val manifest: StateFlow<Manifest?> = _manifest.asStateFlow()

    suspend fun load() = withContext(IoDispatcher) {
        store.ensureSeeded()
        _manifest.value = store.readManifest()
    }

    suspend fun reload() = withContext(IoDispatcher) {
        _manifest.value = store.readManifest()
    }

    suspend fun notes(ref: app.dopamine.domain.model.NotesRef) = withContext(IoDispatcher) { store.readNotes(ref) }

    private val svgLock = SimpleLock()
    private val svgCache = HashMap<String, app.dopamine.ui.svg.SvgDoc>()

    /** A parsed picture, or null when it is missing or outside the allowed subset. Parsed once per process. */
    suspend fun svg(path: String): app.dopamine.ui.svg.SvgDoc? = svgLock.withLock { svgCache[path] } ?: withContext(IoDispatcher) {
        store.readImageText(path)?.let { app.dopamine.ui.svg.SvgParser.parse(it) }?.also { d -> svgLock.withLock { svgCache[path] = d } }
    }

    suspend fun lesson(ref: LessonRef): Lesson? = withContext(IoDispatcher) { store.readLesson(ref) }
}
