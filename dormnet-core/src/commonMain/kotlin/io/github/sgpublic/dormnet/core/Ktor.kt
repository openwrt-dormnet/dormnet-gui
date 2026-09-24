package io.github.sgpublic.dormnet.core

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val GlobalJson = Json {
    ignoreUnknownKeys = true
}

val HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(GlobalJson)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10_000
    }
}
