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
@Serializable
data class InterestRequest(val artworkId: Int)

@Serializable
data class InterestDto(val id: Int, val userId: Int, val artworkId: Int)

@Serializable
data class MessageRequest(
    val receiverId: Int,
    val artworkId: Int,
    val content: String
)

@Serializable
data class MessageDto(
    val id: Int,
    val senderId: Int,
    val senderName: String,
    val receiverId: Int,
    val artworkId: Int,
    val artworkTitle: String,
    val content: String,
    val createdAt: String
)
@Serializable
data class ReactionDto(
    val id: Int,
    val userId: Int,
    val artworkId: Int,
    val emoji: String
)

@Serializable
data class ReactionRequest(
    val artworkId: Int,
    val emoji: String
)

@Serializable
data class ReactionCountDto(
    val emoji: String,
    val count: Int
)

@Serializable
data class FollowDto(
    val id: Int,
    val followerId: Int,
    val artistId: Int
)

@Serializable
data class ArtistPublicDto(
    val id: Int,
    val name: String,
    val email: String,
    val artworkCount: Int,
    val followerCount: Int,
    val isFollowing: Boolean
)
