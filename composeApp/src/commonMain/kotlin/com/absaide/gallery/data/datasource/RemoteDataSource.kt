package com.absaide.gallery.data.datasource

import com.absaide.gallery.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RemoteDataSource(private val client: HttpClient) {
    private val base = ApiConfig.BASE_URL

    suspend fun login(req: LoginRequest): AuthResponse =
        client.post("$base/login") { contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun register(req: RegisterRequest): AuthResponse =
        client.post("$base/register") { contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun getArtworks(token: String): List<Artwork> =
        client.get("$base/artworks") { bearerAuth(token) }.body()

    suspend fun createArtwork(token: String, req: ArtworkRequest): Artwork =
        client.post("$base/artworks") { bearerAuth(token); contentType(ContentType.Application.Json); setBody(req) }.body()

    suspend fun deleteArtwork(token: String, id: Int): Boolean =
        client.delete("$base/artworks/$id") { bearerAuth(token) }.status.isSuccess()

    suspend fun getUsers(token: String): List<User> =
        client.get("$base/users") { bearerAuth(token) }.body()

    suspend fun deleteUser(token: String, id: Int): Boolean =
        client.delete("$base/users/$id") { bearerAuth(token) }.status.isSuccess()

    suspend fun getFavorites(token: String): List<Artwork> =
        client.get("$base/favorites") { bearerAuth(token) }.body()

    suspend fun addFavorite(token: String, artworkId: Int): Favorite =
        client.post("$base/favorites") {
            bearerAuth(token); contentType(ContentType.Application.Json)
            setBody(mapOf("artworkId" to artworkId))
        }.body()

    suspend fun removeFavorite(token: String, artworkId: Int): Boolean =
        client.delete("$base/favorites/$artworkId") { bearerAuth(token) }.status.isSuccess()
}
