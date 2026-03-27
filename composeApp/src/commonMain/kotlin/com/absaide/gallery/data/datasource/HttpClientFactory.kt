package com.absaide.gallery.data.datasource

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(Logging) { level = LogLevel.BODY }
    }
}

object ApiConfig {
    // Emulador Android  → 10.0.2.2
    // Dispositivo físico → IP de tu PC, ej: 192.168.1.100
    // iOS simulator     → localhost
    const val BASE_URL = "http://10.0.2.2:8080"
}
