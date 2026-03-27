package com.absaide.gallery.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.absaide.gallery.data.datasource.HttpClientFactory
import com.absaide.gallery.data.datasource.RemoteDataSource
import com.absaide.gallery.data.repository.*
import com.absaide.gallery.domain.usecase.*
import com.absaide.gallery.presentation.admin.AdminViewModel
import com.absaide.gallery.presentation.admin.AdminDashboardScreen
import com.absaide.gallery.presentation.admin.AdminUsersScreen
import com.absaide.gallery.presentation.admin.AdminArtworksScreen
import com.absaide.gallery.presentation.admin.AdminReportsScreen
import com.absaide.gallery.presentation.artist.ArtistViewModel
import com.absaide.gallery.presentation.artist.ArtistProfileScreen
import com.absaide.gallery.presentation.artist.ArtistUploadScreen
import com.absaide.gallery.presentation.artist.ArtistMyWorksScreen
import com.absaide.gallery.presentation.artist.ArtistStatsScreen
import com.absaide.gallery.presentation.auth.LoginScreen
import com.absaide.gallery.presentation.auth.LoginViewModel
import com.absaide.gallery.presentation.auth.RegisterScreen
import com.absaide.gallery.presentation.auth.RegisterViewModel
import com.absaide.gallery.presentation.user.UserViewModel
import com.absaide.gallery.presentation.user.UserGalleryScreen
import com.absaide.gallery.presentation.user.UserSearchScreen
import com.absaide.gallery.presentation.user.UserFavoritesScreen
import com.absaide.gallery.presentation.user.UserProfileScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    val remote      = remember { RemoteDataSource(HttpClientFactory.create()) }
    val authRepo    = remember { AuthRepositoryImpl(remote) }
    val artworkRepo = remember { ArtworkRepositoryImpl(remote) }
    val userRepo    = remember { UserRepositoryImpl(remote) }
    val favRepo     = remember { FavoriteRepositoryImpl(remote) }

    val loginVM    = remember { LoginViewModel(LoginUseCase(authRepo)) }
    val registerVM = remember { RegisterViewModel(RegisterUseCase(authRepo)) }
    val adminVM    = remember { AdminViewModel(GetUsersUseCase(userRepo), DeleteUserUseCase(userRepo), GetArtworksUseCase(artworkRepo), DeleteArtworkUseCase(artworkRepo)) }
    val artistVM   = remember { ArtistViewModel(GetArtworksUseCase(artworkRepo), CreateArtworkUseCase(artworkRepo), DeleteArtworkUseCase(artworkRepo)) }
    val userVM     = remember { UserViewModel(GetArtworksUseCase(artworkRepo), GetFavoritesUseCase(favRepo), AddFavoriteUseCase(favRepo), RemoveFavoriteUseCase(favRepo)) }

    val logout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }

    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(loginVM,
                onLoginSuccess = { role ->
                    when (role.name) {
                        "ADMIN"  -> navController.navigate(Screen.AdminDashboard.route) { popUpTo(0) }
                        "ARTIST" -> navController.navigate(Screen.ArtistProfile.route)  { popUpTo(0) }
                        else     -> navController.navigate(Screen.UserGallery.route)    { popUpTo(0) }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(registerVM,
                onRegisterSuccess = { role ->
                    when (role.name) {
                        "ADMIN"  -> navController.navigate(Screen.AdminDashboard.route) { popUpTo(0) }
                        "ARTIST" -> navController.navigate(Screen.ArtistProfile.route)  { popUpTo(0) }
                        else     -> navController.navigate(Screen.UserGallery.route)    { popUpTo(0) }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminDashboard.route) { AdminDashboardScreen(adminVM, navController, logout) }
        composable(Screen.AdminUsers.route)     { AdminUsersScreen(adminVM, navController, logout) }
        composable(Screen.AdminArtworks.route)  { AdminArtworksScreen(adminVM, navController, logout) }
        composable(Screen.AdminReports.route)   { AdminReportsScreen(adminVM, logout) }
        composable(Screen.ArtistProfile.route)  { ArtistProfileScreen(artistVM, navController, logout) }
        composable(Screen.ArtistUpload.route)   { ArtistUploadScreen(artistVM, navController) }
        composable(Screen.ArtistMyWorks.route)  { ArtistMyWorksScreen(artistVM, navController, logout) }
        composable(Screen.ArtistStats.route)    { ArtistStatsScreen(artistVM, navController, logout) }
        composable(Screen.UserGallery.route)    { UserGalleryScreen(userVM, navController, logout) }
        composable(Screen.UserSearch.route)     { UserSearchScreen(userVM, navController, logout) }
        composable(Screen.UserFavorites.route)  { UserFavoritesScreen(userVM, navController, logout) }
        composable(Screen.UserProfile.route)    { UserProfileScreen(userVM, navController, logout) }
    }
}
