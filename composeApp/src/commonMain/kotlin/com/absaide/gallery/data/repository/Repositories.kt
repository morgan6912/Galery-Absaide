package com.absaide.gallery.data.repository

import com.absaide.gallery.data.datasource.RemoteDataSource
import com.absaide.gallery.data.model.*

object SessionStore {
    var token: String = ""
    var currentUser: User? = null
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
}
interface FavoriteRepository {
    suspend fun getFavorites(): Result<List<Artwork>>
    suspend fun addFavorite(artworkId: Int): Result<Favorite>
    suspend fun removeFavorite(artworkId: Int): Result<Boolean>
}

class AuthRepositoryImpl(private val remote: RemoteDataSource) : AuthRepository {
    override suspend fun login(email: String, password: String) = runCatching {
        remote.login(LoginRequest(email, password)).also { SessionStore.token = it.token; SessionStore.currentUser = it.user }
    }
    override suspend fun register(name: String, email: String, password: String, role: Role) = runCatching {
        remote.register(RegisterRequest(name, email, password, role)).also { SessionStore.token = it.token; SessionStore.currentUser = it.user }
    }
}
class ArtworkRepositoryImpl(private val remote: RemoteDataSource) : ArtworkRepository {
    override suspend fun getArtworks() = runCatching { remote.getArtworks(SessionStore.token) }
    override suspend fun createArtwork(title: String, description: String, imageUrl: String) =
        runCatching { remote.createArtwork(SessionStore.token, ArtworkRequest(title, description, imageUrl)) }
    override suspend fun deleteArtwork(id: Int) = runCatching { remote.deleteArtwork(SessionStore.token, id) }
}
class UserRepositoryImpl(private val remote: RemoteDataSource) : UserRepository {
    override suspend fun getUsers() = runCatching { remote.getUsers(SessionStore.token) }
    override suspend fun deleteUser(id: Int) = runCatching { remote.deleteUser(SessionStore.token, id) }
}
class FavoriteRepositoryImpl(private val remote: RemoteDataSource) : FavoriteRepository {
    override suspend fun getFavorites() = runCatching { remote.getFavorites(SessionStore.token) }
    override suspend fun addFavorite(artworkId: Int) = runCatching { remote.addFavorite(SessionStore.token, artworkId) }
    override suspend fun removeFavorite(artworkId: Int) = runCatching { remote.removeFavorite(SessionStore.token, artworkId) }
}
