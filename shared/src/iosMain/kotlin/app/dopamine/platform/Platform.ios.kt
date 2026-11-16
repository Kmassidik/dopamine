package app.dopamine.platform

import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlinx.coroutines.IO
import platform.Foundation.NSLog
import platform.Foundation.NSRecursiveLock
import platform.Foundation.NSTimeZone
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSDate
import platform.Foundation.localTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun logW(tag: String, message: String) { NSLog("W/$tag: $message") }
actual fun logI(tag: String, message: String) { NSLog("I/$tag: $message") }

actual class SimpleLock {
    private val lock = NSRecursiveLock()
    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        try { return block() } finally { lock.unlock() }
    }
}

actual fun localUtcOffsetMillis(atMillis: Long): Long =
    NSTimeZone.localTimeZone.secondsFromGMTForDate(NSDate.dateWithTimeIntervalSince1970(atMillis / 1000.0)) * 1000L

class UserDefaultsKeyValueStore(private val prefix: String) : KeyValueStore {
    private val d = NSUserDefaults.standardUserDefaults
    override fun getString(key: String): String? = d.stringForKey("$prefix.$key")
    override fun getLong(key: String, default: Long): Long =
        if (d.objectForKey("$prefix.$key") == null) default else d.integerForKey("$prefix.$key")
    override fun putString(key: String, value: String?) {
        if (value == null) d.removeObjectForKey("$prefix.$key") else d.setObject(value, "$prefix.$key")
    }
    override fun putLong(key: String, value: Long) { d.setInteger(value, "$prefix.$key") }
}

/** The seed content folder inside the app bundle (added to the Xcode project as a folder reference named "content"). */
class BundleSeedSource(private val root: Path) : SeedSource {
    private val fs = FileSystem.SYSTEM
    override fun children(path: String): List<String> {
        val p = if (path.isEmpty()) root else root / path
        return if (fs.metadataOrNull(p)?.isDirectory == true) fs.list(p).map { it.name } else emptyList()
    }
    override fun read(path: String): ByteArray = fs.read(root / path) { readByteArray() }
}

actual val IoDispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.IO
