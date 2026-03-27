package com.absaide.gallery.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Int = 0, val name: String, val email: String, val role: Role)

@Serializable
enum class Role { ADMIN, ARTIST, USER }

@Serializable
data class Artwork(
    val id: Int = 0, val title: String, val description: String,
    val artistId: Int, val imageUrl: String, val artistName: String = ""
)

@Serializable
data class Favorite(val id: Int = 0, val userId: Int, val artworkId: Int)

@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class RegisterRequest(val name: String, val email: String, val password: String, val role: Role)
@Serializable data class ArtworkRequest(val title: String, val description: String, val imageUrl: String)
@Serializable data class AuthResponse(val token: String, val user: User)
