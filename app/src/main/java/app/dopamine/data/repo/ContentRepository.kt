package app.dopamine.data.repo

import app.dopamine.data.content.ContentStore
import app.dopamine.domain.model.Lesson
import app.dopamine.domain.model.LessonRef
import app.dopamine.domain.model.Manifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ContentRepository(private val store: ContentStore) {
    private val _manifest = MutableStateFlow<Manifest?>(null)
    val manifest: StateFlow<Manifest?> = _manifest.asStateFlow()
    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    suspend fun load() = withContext(Dispatchers.IO) {
        _manifest.value = store.readManifest()
        _loaded.value = true
    }

    suspend fun reload() = withContext(Dispatchers.IO) {
        _manifest.value = store.readManifest()
    }

    suspend fun notes(ref: app.dopamine.domain.model.NotesRef) = withContext(Dispatchers.IO) { store.readNotes(ref) }

    private val svgCache = java.util.concurrent.ConcurrentHashMap<String, app.dopamine.ui.svg.SvgDoc>()

    /** A parsed picture, or null when it is missing or outside the allowed subset. Parsed once per process. */
    suspend fun svg(path: String): app.dopamine.ui.svg.SvgDoc? = svgCache[path] ?: withContext(Dispatchers.IO) {
        store.readImageText(path)?.let { app.dopamine.ui.svg.SvgParser.parse(it) }?.also { svgCache[path] = it }
    }

    suspend fun lesson(ref: LessonRef): Lesson? = withContext(Dispatchers.IO) { store.readLesson(ref) }
}
