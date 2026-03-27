package com.absaide.gallery.domain.usecase

import com.absaide.gallery.data.model.Role
import com.absaide.gallery.data.repository.*

class LoginUseCase(private val repo: AuthRepository) { suspend operator fun invoke(email: String, password: String) = repo.login(email, password) }
class RegisterUseCase(private val repo: AuthRepository) { suspend operator fun invoke(name: String, email: String, password: String, role: Role) = repo.register(name, email, password, role) }
class GetArtworksUseCase(private val repo: ArtworkRepository) { suspend operator fun invoke() = repo.getArtworks() }
class CreateArtworkUseCase(private val repo: ArtworkRepository) { suspend operator fun invoke(title: String, desc: String, url: String) = repo.createArtwork(title, desc, url) }
class DeleteArtworkUseCase(private val repo: ArtworkRepository) { suspend operator fun invoke(id: Int) = repo.deleteArtwork(id) }
class GetUsersUseCase(private val repo: UserRepository) { suspend operator fun invoke() = repo.getUsers() }
class DeleteUserUseCase(private val repo: UserRepository) { suspend operator fun invoke(id: Int) = repo.deleteUser(id) }
class GetFavoritesUseCase(private val repo: FavoriteRepository) { suspend operator fun invoke() = repo.getFavorites() }
class AddFavoriteUseCase(private val repo: FavoriteRepository) { suspend operator fun invoke(id: Int) = repo.addFavorite(id) }
class RemoveFavoriteUseCase(private val repo: FavoriteRepository) { suspend operator fun invoke(id: Int) = repo.removeFavorite(id) }
