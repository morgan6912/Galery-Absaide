package com.absaide.gallery.data.repository

import com.absaide.gallery.data.datasource.RemoteDataSource
import com.absaide.gallery.data.model.*

object SessionStore {
    var token: String? = null
    var currentUser: User? = null
    var profilePhotoBytes: ByteArray? = null
    var nickname: String = ""
}

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(name: String, email: String, password: String, role: Role): Result<AuthResponse>
}
interface ArtworkRepository {
    suspend fun getArtworks(): Result<List<Artwork>>
    suspend fun createArtwork(title: String, description: String, imageUrl: String): Result<Artwork>
    suspend fun deleteArtwork(id: Int): Result<Boolean>
}
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun deleteUser(id: Int): Result<Boolean>
    suspend fun updateRole(id: Int, role: Role): Result<Boolean>  // ← nueva
}
interface FavoriteRepository {
    suspend fun getFavorites(): Result<List<Artwork>>
    suspend fun addFavorite(artworkId: Int): Result<Favorite>
    suspend fun removeFavorite(artworkId: Int): Result<Boolean>
}
interface InterestRepository {
    suspend fun addInterest(artworkId: Int): Result<InterestDto>
    suspend fun removeInterest(artworkId: Int): Result<Boolean>
    suspend fun getMyInterests(): Result<List<Artwork>>
}
interface MessageRepository {
    suspend fun sendMessage(receiverId: Int, artworkId: Int, content: String): Result<MessageDto>
    suspend fun getReceived(): Result<List<MessageDto>>
    suspend fun getSent(): Result<List<MessageDto>>
}

class AuthRepositoryImpl(private val remote: RemoteDataSource) : AuthRepository {
    override suspend fun login(email: String, password: String) = runCatching {
        remote.login(LoginRequest(email, password)).also {
            SessionStore.token = it.token
            SessionStore.currentUser = it.user
        }
    }
    override suspend fun register(name: String, email: String, password: String, role: Role) = runCatching {
        remote.register(RegisterRequest(name, email, password, role)).also {
            SessionStore.token = it.token
            SessionStore.currentUser = it.user
        }
    }
}

class ArtworkRepositoryImpl(private val remote: RemoteDataSource) : ArtworkRepository {
    override suspend fun getArtworks() = runCatching {
        remote.getArtworks(SessionStore.token ?: error("No token"))
    }
    override suspend fun createArtwork(title: String, description: String, imageUrl: String) = runCatching {
        remote.createArtwork(SessionStore.token ?: error("No token"), ArtworkRequest(title, description, imageUrl))
    }
    override suspend fun deleteArtwork(id: Int) = runCatching {
        remote.deleteArtwork(SessionStore.token ?: error("No token"), id)
    }
}

class UserRepositoryImpl(private val remote: RemoteDataSource) : UserRepository {
    override suspend fun getUsers() = runCatching {
        remote.getUsers(SessionStore.token ?: error("No token"))
    }
    override suspend fun deleteUser(id: Int) = runCatching {
        remote.deleteUser(SessionStore.token ?: error("No token"), id)
    }
    override suspend fun updateRole(id: Int, role: Role) = runCatching {  // ← nueva
        remote.updateRole(SessionStore.token ?: error("No token"), id, role)
    }
}

class FavoriteRepositoryImpl(private val remote: RemoteDataSource) : FavoriteRepository {
    override suspend fun getFavorites() = runCatching {
        remote.getFavorites(SessionStore.token ?: error("No token"))
    }
    override suspend fun addFavorite(artworkId: Int) = runCatching {
        remote.addFavorite(SessionStore.token ?: error("No token"), artworkId)
    }
    override suspend fun removeFavorite(artworkId: Int) = runCatching {
        remote.removeFavorite(SessionStore.token ?: error("No token"), artworkId)
    }
}

class InterestRepositoryImpl(private val remote: RemoteDataSource) : InterestRepository {
    override suspend fun addInterest(artworkId: Int) = runCatching {
        remote.addInterest(SessionStore.token ?: error("No token"), artworkId)
    }
    override suspend fun removeInterest(artworkId: Int) = runCatching {
        remote.removeInterest(SessionStore.token ?: error("No token"), artworkId)
    }
    override suspend fun getMyInterests() = runCatching {
        remote.getMyInterests(SessionStore.token ?: error("No token"))
    }
}

class MessageRepositoryImpl(private val remote: RemoteDataSource) : MessageRepository {
    override suspend fun sendMessage(receiverId: Int, artworkId: Int, content: String) = runCatching {
        remote.sendMessage(SessionStore.token ?: error("No token"), MessageRequest(receiverId, artworkId, content))
    }
    override suspend fun getReceived() = runCatching {
        remote.getReceivedMessages(SessionStore.token ?: error("No token"))
    }
    override suspend fun getSent() = runCatching {
        remote.getSentMessages(SessionStore.token ?: error("No token"))
    }
}