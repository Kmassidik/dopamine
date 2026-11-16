package app.dopamine.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.client.plugins.HttpTimeout

actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    followRedirects = false
    expectSuccess = false
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }
}

// NSURLErrorNotConnectedToInternet (-1009) and NSURLErrorCannotFindHost (-1003)
internal actual fun isHostUnreachable(e: Throwable): Boolean =
    e is DarwinHttpRequestException && e.origin.code in setOf(-1009L, -1003L)
