package com.absaide.gallery.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absaide.gallery.data.model.Artwork
import com.absaide.gallery.data.repository.SessionStore
import com.absaide.gallery.domain.usecase.*
import com.absaide.gallery.presentation.navigation.Screen
import com.absaide.gallery.presentation.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(
    private val getArtworksUseCase: GetArtworksUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase
) {
    var artworks    by mutableStateOf<List<Artwork>>(emptyList())
    var favorites   by mutableStateOf<List<Artwork>>(emptyList())
    var favoriteIds by mutableStateOf<Set<Int>>(emptySet())
    var isLoading   by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    val filteredArtworks get() = if (searchQuery.isBlank()) artworks
        else artworks.filter { it.title.contains(searchQuery, true) || it.artistName.contains(searchQuery, true) }

    fun loadArtworks() = CoroutineScope(Dispatchers.Main).launch {
        isLoading = true
        getArtworksUseCase().onSuccess { artworks = it }
        isLoading = false
    }
    fun loadFavorites() = CoroutineScope(Dispatchers.Main).launch {
        getFavoritesUseCase().onSuccess { favorites = it; favoriteIds = it.map { a -> a.id }.toSet() }
    }
    fun toggleFavorite(artworkId: Int) = CoroutineScope(Dispatchers.Main).launch {
        if (artworkId in favoriteIds) {
            removeFavoriteUseCase(artworkId).onSuccess { favoriteIds = favoriteIds - artworkId; favorites = favorites.filter { it.id != artworkId } }
        } else {
            addFavoriteUseCase(artworkId).onSuccess { favoriteIds = favoriteIds + artworkId }
        }
    }
}

@Composable
fun UserBottomBar(navController: NavController, current: String) {
    NavigationBar(containerColor = BlackSurface, tonalElevation = 0.dp) {
        listOf(
            Triple(Screen.UserGallery.route,   "🖼",  "Galería"),
            Triple(Screen.UserSearch.route,    "🔍", "Buscar"),
            Triple(Screen.UserFavorites.route, "♥",  "Favoritos"),
            Triple(Screen.UserProfile.route,   "👤", "Perfil")
        ).forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = current == route,
                onClick  = { if (current != route) navController.navigate(route) { launchSingleTop = true } },
                icon  = { Text(icon,  fontSize = 22.sp) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PinkPrimary, selectedTextColor = PinkPrimary,
                    unselectedIconColor = GrayText,  unselectedTextColor = GrayText, indicatorColor = PinkGlow)
            )
        }
    }
}

@Composable
fun UserGalleryScreen(viewModel: UserViewModel, navController: NavController, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadArtworks(); viewModel.loadFavorites() }
    Scaffold(topBar = { GalleryTopBar("Galery Absaide", onLogout) },
        bottomBar = { UserBottomBar(navController, Screen.UserGallery.route) }, containerColor = Black) { pad ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PinkPrimary) }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { SectionHeader("Explorar Galería") }
                items(viewModel.artworks) { artwork ->
                    ArtworkCard(title = artwork.title, artistName = artwork.artistName, imageUrl = artwork.imageUrl,
                        isFavorite = artwork.id in viewModel.favoriteIds,
                        onFavoriteClick = { viewModel.toggleFavorite(artwork.id) })
                }
                if (viewModel.artworks.isEmpty()) {
                    item { Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                        Text("No hay obras disponibles", style = MaterialTheme.typography.bodyMedium)
                    }}
                }
            }
        }
    }
}

@Composable
fun UserSearchScreen(viewModel: UserViewModel, navController: NavController, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadArtworks() }
    Scaffold(topBar = { GalleryTopBar("Buscar", onLogout) },
        bottomBar = { UserBottomBar(navController, Screen.UserSearch.route) }, containerColor = Black) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            OutlinedTextField(value = viewModel.searchQuery, onValueChange = { viewModel.searchQuery = it },
                label = { Text("Buscar por título o artista…", color = GrayText) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary, unfocusedBorderColor = GrayBorder,
                    focusedTextColor = WhiteText, unfocusedTextColor = WhiteText,
                    cursorColor = PinkPrimary, focusedContainerColor = BlackCard,
                    unfocusedContainerColor = BlackCard, focusedLabelColor = PinkPrimary),
                leadingIcon = { Text("🔍", fontSize = 18.sp) })
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.filteredArtworks) { artwork ->
                    ArtworkCard(title = artwork.title, artistName = artwork.artistName, imageUrl = artwork.imageUrl,
                        isFavorite = artwork.id in viewModel.favoriteIds,
                        onFavoriteClick = { viewModel.toggleFavorite(artwork.id) })
                }
            }
        }
    }
}

@Composable
fun UserFavoritesScreen(viewModel: UserViewModel, navController: NavController, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadFavorites() }
    Scaffold(topBar = { GalleryTopBar("Favoritos", onLogout) },
        bottomBar = { UserBottomBar(navController, Screen.UserFavorites.route) }, containerColor = Black) { pad ->
        if (viewModel.favorites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♡", fontSize = 64.sp, color = PinkPrimary); Spacer(Modifier.height(16.dp))
                    Text("Aún no tienes favoritos", style = MaterialTheme.typography.headlineSmall)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { SectionHeader("Mis Favoritos (${viewModel.favorites.size})") }
                items(viewModel.favorites) { artwork ->
                    ArtworkCard(title = artwork.title, artistName = artwork.artistName, imageUrl = artwork.imageUrl,
                        isFavorite = true, onFavoriteClick = { viewModel.toggleFavorite(artwork.id) })
                }
            }
        }
    }
}

@Composable
fun UserProfileScreen(viewModel: UserViewModel, navController: NavController, onLogout: () -> Unit) {
    val user = SessionStore.currentUser
    LaunchedEffect(Unit) { viewModel.loadFavorites() }
    Scaffold(topBar = { GalleryTopBar("Mi Perfil", onLogout) },
        bottomBar = { UserBottomBar(navController, Screen.UserProfile.route) }, containerColor = Black) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Box(Modifier.size(110.dp).background(PinkGlow, CircleShape), contentAlignment = Alignment.Center) {
                Text("👤", fontSize = 52.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text(user?.name  ?: "Usuario", style = MaterialTheme.typography.headlineMedium)
            Text(user?.email ?: "",         style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = PinkGlow) {
                Text("  ${user?.role?.name ?: "USUARIO"}  ", color = PinkPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
            Spacer(Modifier.height(40.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♥", fontSize = 28.sp)
                    Text(viewModel.favorites.size.toString(), style = MaterialTheme.typography.headlineMedium.copy(color = PinkPrimary))
                    Text("Favoritos", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(40.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)) {
                Text("CERRAR SESIÓN", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
