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

    suspend fun load() = withContext(Dispatchers.IO) {
        store.ensureSeeded()
        _manifest.value = store.readManifest()
    }

    suspend fun reload() = withContext(Dispatchers.IO) {
        _manifest.value = store.readManifest()
    }

    suspend fun lesson(ref: LessonRef): Lesson? = withContext(Dispatchers.IO) { store.readLesson(ref) }
}
