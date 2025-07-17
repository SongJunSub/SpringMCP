package com.example.springmcp.webadmin.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class UrlShortenerApi(private val baseUrl: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private val BASE_URL = baseUrl // TODO: Make configurable

    suspend fun shortenUrl(request: UrlShortenerRequest): UrlShortenerResponse {
        return client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getLongUrl(shortKey: String): String {
        return client.get("$BASE_URL/$shortKey").body()
    }

    suspend fun getAllUrls(): List<UrlShortenerResponse> {
        // Assuming a new endpoint /api/shorten/urls for getting all URLs
        return client.get("$BASE_URL/urls").body()
    }

    suspend fun deleteUrl(shortKey: String) {
        client.delete("$BASE_URL/$shortKey").body<Unit>()
    }
}

@Serializable
data class UrlShortenerRequest(
    val longUrl: String,
    val customKey: String? = null
)

@Serializable
data class UrlShortenerResponse(
    val shortKey: String,
    val longUrl: String,
    val shortUrl: String,
    val createdAt: String // Assuming ISO 8601 string for simplicity
)