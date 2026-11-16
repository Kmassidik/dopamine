package app.dopamine.data.network

import io.ktor.client.HttpClient

/**
 * The client used for content updates: Android uses the OkHttp engine, iOS the Darwin (NSURLSession) engine.
 * Redirects are off (UpdateManager follows https redirects itself); connect 15 s, each read 30 s like before.
 */
expect fun createHttpClient(): HttpClient
