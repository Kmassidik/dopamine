package app.dopamine.platform

import android.content.Context
import android.util.Log
import java.util.TimeZone
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock as kWithLock

actual fun logW(tag: String, message: String) { Log.w(tag, message) }
actual fun logI(tag: String, message: String) { Log.i(tag, message) }

actual class SimpleLock {
    private val lock = ReentrantLock()
    actual fun <T> withLock(block: () -> T): T = lock.kWithLock(block)
}

actual fun localUtcOffsetMillis(atMillis: Long): Long = TimeZone.getDefault().getOffset(atMillis).toLong()

class SharedPrefsKeyValueStore(context: Context, name: String) : KeyValueStore {
    private val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun getLong(key: String, default: Long): Long = prefs.getLong(key, default)
    override fun putString(key: String, value: String?) { prefs.edit().putString(key, value).apply() }
    override fun putLong(key: String, value: Long) { prefs.edit().putLong(key, value).apply() }
}

class AssetsSeedSource(private val context: Context, private val root: String = "content") : SeedSource {
    override fun children(path: String): List<String> = context.assets.list("$root/$path".trimEnd('/')).orEmpty().toList()
    override fun read(path: String): ByteArray = context.assets.open("$root/$path".trimEnd('/')).use { it.readBytes() }
}
actual val IoDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO

actual fun relativeTimeText(thenMillis: Long, nowMillis: Long): String =
    android.text.format.DateUtils.getRelativeTimeSpanString(thenMillis, nowMillis, android.text.format.DateUtils.MINUTE_IN_MILLIS).toString()
