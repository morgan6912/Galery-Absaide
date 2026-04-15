package com.absaide.gallery.data.datasource

import com.absaide.gallery.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class RemoteDataSource(private val client: HttpClient) {
    private val base = ApiConfig.BASE_URL

    suspend fun login(req: LoginRequest): AuthResponse =
        client.post("$base/login") {
            contentType(ContentType.Application.Json); setBody(req)
        }.body()

    suspend fun register(req: RegisterRequest): AuthResponse =
        client.post("$base/register") {
            contentType(ContentType.Application.Json); setBody(req)
        }.body()

    suspend fun getArtworks(token: String): List<Artwork> =
        client.get("$base/artworks") { bearerAuth(token) }.body()

    suspend fun createArtwork(token: String, req: ArtworkRequest): Artwork =
        client.post("$base/artworks") {
            bearerAuth(token); contentType(ContentType.Application.Json); setBody(req)
        }.body()

    suspend fun deleteArtwork(token: String, id: Int): Boolean =
        client.delete("$base/artworks/$id") { bearerAuth(token) }.status.isSuccess()

    suspend fun getUsers(token: String): List<User> =
        client.get("$base/users") { bearerAuth(token) }.body()

    suspend fun deleteUser(token: String, id: Int): Boolean =
        client.delete("$base/users/$id") { bearerAuth(token) }.status.isSuccess()

    suspend fun updateRole(token: String, id: Int, role: Role): Boolean =
        client.put("$base/users/$id/role") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(mapOf("role" to role.name))
        }.status.isSuccess()

    suspend fun getFavorites(token: String): List<Artwork> =
        client.get("$base/favorites") { bearerAuth(token) }.body()

    suspend fun addFavorite(token: String, artworkId: Int): Favorite =
        client.post("$base/favorites") {
            bearerAuth(token); contentType(ContentType.Application.Json)
            setBody(mapOf("artworkId" to artworkId))
        }.body()

    suspend fun removeFavorite(token: String, artworkId: Int): Boolean =
        client.delete("$base/favorites/$artworkId") { bearerAuth(token) }.status.isSuccess()

    suspend fun addInterest(token: String, artworkId: Int): InterestDto =
        client.post("$base/interests") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(InterestRequest(artworkId))
        }.body()

    suspend fun removeInterest(token: String, artworkId: Int): Boolean =
        client.delete("$base/interests/$artworkId") { bearerAuth(token) }.status.isSuccess()

    suspend fun getMyInterests(token: String): List<Artwork> =
        client.get("$base/interests/mine") { bearerAuth(token) }.body()

    suspend fun sendMessage(token: String, req: MessageRequest): MessageDto {
        val raw = client.post("$base/messages") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(req)
        }.bodyAsText()
        return try {
            val idRegex      = Regex("\"id\"\\s*:\\s*(\\d+)")
            val senderRegex  = Regex("\"senderId\"\\s*:\\s*(\\d+)")
            val contentRegex = Regex("\"content\"\\s*:\\s*\"([^\"]+)\"")
            val id       = idRegex.find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val senderId = senderRegex.find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val content  = contentRegex.find(raw)?.groupValues?.get(1) ?: req.content
            MessageDto(id, senderId, "", req.receiverId, req.artworkId, "", content, "")
        } catch (e: Exception) {
            MessageDto(0, 0, "", req.receiverId, req.artworkId, "", req.content, "")
        }
    }

    suspend fun getReceivedMessages(token: String): List<MessageDto> =
        client.get("$base/messages/received") { bearerAuth(token) }.body()

    suspend fun getSentMessages(token: String): List<MessageDto> =
        client.get("$base/messages/sent") { bearerAuth(token) }.body()

    suspend fun addReaction(token: String, artworkId: Int, emoji: String): Boolean {
        val raw = client.post("$base/reactions") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(ReactionRequest(artworkId, emoji))
        }.bodyAsText()
        return raw.contains("\"success\":true") || raw.contains("\"success\": true")
    }

    suspend fun removeReaction(token: String, artworkId: Int): Boolean =
        client.delete("$base/reactions/$artworkId") {
            bearerAuth(token)
        }.status.isSuccess()

    suspend fun getReactions(artworkId: Int): List<ReactionCountDto> = try {
        client.get("$base/reactions/artwork/$artworkId").body()
    } catch (e: Exception) { emptyList() }

    suspend fun getMyReaction(token: String, artworkId: Int): String? = try {
        val raw = client.get("$base/reactions/mine/$artworkId") {
            bearerAuth(token)
        }.bodyAsText()
        val regex = Regex("\"emoji\"\\s*:\\s*\"([^\"]+)\"")
        val result = regex.find(raw)?.groupValues?.get(1)
        if (result.isNullOrBlank()) null else result
    } catch (e: Exception) { null }

    suspend fun followArtist(token: String, artistId: Int): Boolean {
        val raw = client.post("$base/follows/$artistId") {
            bearerAuth(token)
        }.bodyAsText()
        return raw.contains("\"success\":true") || raw.contains("\"success\": true")
    }

    suspend fun unfollowArtist(token: String, artistId: Int): Boolean =
        client.delete("$base/follows/$artistId") {
            bearerAuth(token)
        }.status.isSuccess()

    suspend fun getFollowingArtists(token: String): List<ArtistPublicDto> = try {
        client.get("$base/follows/following") { bearerAuth(token) }.body()
    } catch (e: Exception) { emptyList() }

    suspend fun getAllArtists(token: String): List<ArtistPublicDto> = try {
        client.get("$base/follows/artists") { bearerAuth(token) }.body()
    } catch (e: Exception) { emptyList() }

    // ── Upload Image — Cloudinary ─────────────────────────────────────────
    suspend fun uploadImage(token: String, bytes: ByteArray, fileName: String): String {
        val cloudName    = "dkn0uaome"
        val uploadPreset = "galery_absaide"

        val response = client.post("https://api.cloudinary.com/v1_1/$cloudName/image/upload") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("upload_preset", uploadPreset)
                        append(
                            "file", bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                )
            )
        }
        val raw      = response.bodyAsText()
        val urlRegex = Regex("\"secure_url\"\\s*:\\s*\"([^\"]+)\"")
        return urlRegex.find(raw)?.groupValues?.get(1)?.replace("\\/", "/")
            ?: throw Exception("No se encontró URL en la respuesta de Cloudinary")
    }
}