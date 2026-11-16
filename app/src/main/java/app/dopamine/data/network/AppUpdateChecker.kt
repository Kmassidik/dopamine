package app.dopamine.data.network

import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class AppUpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class AppRelease(val version: String, val url: String)

@Serializable
private data class GithubRelease(val tag_name: String, val html_url: String)

object ReleaseVersion {
    fun isNewer(candidate: String, current: String): Boolean {
        val a = parts(candidate)
        val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val diff = a.getOrElse(i) { 0 }.compareTo(b.getOrElse(i) { 0 })
            if (diff != 0) return diff > 0
        }
        return false
    }

    private fun parts(version: String): List<Int> = version.removePrefix("v").split('.').map {
        it.toIntOrNull() ?: throw AppUpdateException("The release has an invalid version number.")
    }
}

/** Runs only after the user taps Check for app update. GitHub excludes drafts and prereleases here. */
class AppUpdateChecker(
    private val client: OkHttpClient,
    private val endpoint: String,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun check(): AppRelease = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(endpoint)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Dopamine-Android")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw AppUpdateException("Could not check for an app update. Try again later.")
                val bytes = response.body.bytes()
                val release = try {
                    json.decodeFromString<GithubRelease>(bytes.decodeToString())
                } catch (e: Exception) {
                    throw AppUpdateException("The release server sent an invalid response.", e)
                }
                if (!release.html_url.startsWith("https://github.com/Kmassidik/dopamine/releases/")) {
                    throw AppUpdateException("The release server sent an invalid download address.")
                }
                AppRelease(release.tag_name.removePrefix("v"), release.html_url)
            }
        } catch (e: UnknownHostException) {
            throw AppUpdateException("You appear to be offline. Connect to the internet and try again.", e)
        } catch (e: IOException) {
            throw AppUpdateException("Could not check for an app update. Try again later.", e)
        }
    }
}
