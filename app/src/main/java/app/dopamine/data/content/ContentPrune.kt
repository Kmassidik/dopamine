package app.dopamine.data.content

import java.io.File

/**
 * Deletes every file under [dir] whose path (relative to [dir], with forward slashes) is not in [keep], then removes
 * folders that end up empty. Used on the staged copy of the content just before it is swapped in, so files that moved
 * or were removed in the repo do not pile up on the phone. Symbolic links are never followed.
 */
object ContentPrune {
    fun prune(dir: File, keep: Set<String>): Int {
        if (!dir.isDirectory) return 0
        var removed = 0
        dir.walkBottomUp().forEach { f ->
            if (f == dir) return@forEach
            val rel = f.relativeTo(dir).invariantSeparatorsPath
            when {
                java.nio.file.Files.isSymbolicLink(f.toPath()) -> { f.delete(); removed++ }
                f.isFile -> if (rel !in keep) { f.delete(); removed++ }
                f.isDirectory -> if (f.list().isNullOrEmpty()) f.delete()
            }
        }
        return removed
    }
}
