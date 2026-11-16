package app.dopamine.platform

import kotlin.time.Clock

/** Small things that differ per platform. Everything else in the shared code is plain Kotlin. */
expect fun logW(tag: String, message: String)
expect fun logI(tag: String, message: String)

/** A reentrant lock (Android: ReentrantLock, iOS: NSRecursiveLock). */
expect class SimpleLock() {
    fun <T> withLock(block: () -> T): T
}

/** The device's UTC offset in milliseconds at [atMillis], for "what local day is it" (streaks). */
expect fun localUtcOffsetMillis(atMillis: Long): Long

fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** Tiny persistent key-value store (Android: SharedPreferences, iOS: NSUserDefaults). */
interface KeyValueStore {
    fun getString(key: String): String?
    fun getLong(key: String, default: Long): Long
    fun putString(key: String, value: String?)
    fun putLong(key: String, value: Long)
}

/** Where the bundled seed content lives (Android: assets/content, iOS: the app bundle's content folder). */
interface SeedSource {
    /** Names directly inside [path] (relative to the seed root); empty for a file or a missing path. */
    fun children(path: String): List<String>
    fun read(path: String): ByteArray
}

/** The dispatcher for blocking file and database work. */
expect val IoDispatcher: kotlinx.coroutines.CoroutineDispatcher
