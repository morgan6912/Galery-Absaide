@file:OptIn(ExperimentalMaterial3Api::class)
package com.absaide.gallery.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absaide.gallery.data.datasource.RemoteDataSource
import com.absaide.gallery.data.model.*
import com.absaide.gallery.data.repository.SessionStore
import com.absaide.gallery.domain.usecase.*
import com.absaide.gallery.presentation.artist.NetworkImage
import com.absaide.gallery.presentation.navigation.Screen
import com.absaide.gallery.presentation.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────
class UserViewModel(
    private val getArtworksUseCase: GetArtworksUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val addInterestUseCase: AddInterestUseCase,
    private val removeInterestUseCase: RemoveInterestUseCase,
    private val getMyInterestsUseCase: GetMyInterestsUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getReceivedMessagesUseCase: GetReceivedMessagesUseCase,
    private val remote: RemoteDataSource
) {
    var artworks    by mutableStateOf<List<Artwork>>(emptyList())
    var favorites   by mutableStateOf<List<Artwork>>(emptyList())
    var interests   by mutableStateOf<List<Artwork>>(emptyList())
    var messages    by mutableStateOf<List<MessageDto>>(emptyList())
    var isLoading   by mutableStateOf(false)
    var favoriteIds by mutableStateOf<Set<Int>>(emptySet())
    var interestIds by mutableStateOf<Set<Int>>(emptySet())
    var searchQuery by mutableStateOf("")
    var reactions   by mutableStateOf<Map<Int, List<ReactionCountDto>>>(emptyMap())
    var myReactions by mutableStateOf<Map<Int, String>>(emptyMap())
    var artists     by mutableStateOf<List<ArtistPublicDto>>(emptyList())

    val filteredArtworks get() = if (searchQuery.isBlank()) artworks
    else artworks.filter {
        it.title.contains(searchQuery, true) ||
                it.artistName.contains(searchQuery, true)
    }

    fun loadArtworks() = CoroutineScope(Dispatchers.Main).launch {
        isLoading = true
        getArtworksUseCase().onSuccess { artworks = it }
        isLoading = false
    }

    fun loadFavorites() = CoroutineScope(Dispatchers.Main).launch {
        getFavoritesUseCase().onSuccess {
            favorites   = it
            favoriteIds = it.map { a -> a.id }.toSet()
        }
    }

    fun loadInterests() = CoroutineScope(Dispatchers.Main).launch {
        getMyInterestsUseCase().onSuccess {
            interests   = it
            interestIds = it.map { a -> a.id }.toSet()
        }
    }

    fun loadMessages() = CoroutineScope(Dispatchers.Main).launch {
        getReceivedMessagesUseCase().onSuccess { messages = it }
    }

    fun loadReactions(artworkId: Int) = CoroutineScope(Dispatchers.Main).launch {
        try {
            val token = SessionStore.token ?: return@launch
            val list = remote.getReactions(artworkId)
            reactions = reactions.toMutableMap().also { it[artworkId] = list }
            val myEmoji = remote.getMyReaction(token, artworkId)
            if (myEmoji != null) {
                myReactions = myReactions.toMutableMap().also { it[artworkId] = myEmoji }
            }
        } catch (e: Exception) { }
    }

    fun toggleReaction(artworkId: Int, emoji: String) = CoroutineScope(Dispatchers.Main).launch {
        val token = SessionStore.token ?: return@launch
        try {
            val currentEmoji = myReactions[artworkId]
            if (currentEmoji == emoji) {
                remote.removeReaction(token, artworkId)
                myReactions = myReactions.toMutableMap().also { it.remove(artworkId) }
            } else {
                remote.addReaction(token, artworkId, emoji)
                myReactions = myReactions.toMutableMap().also { it[artworkId] = emoji }
            }
            val list = remote.getReactions(artworkId)
            reactions = reactions.toMutableMap().also { it[artworkId] = list }
        } catch (e: Exception) { }
    }

    fun loadArtists() = CoroutineScope(Dispatchers.Main).launch {
        val token = SessionStore.token ?: return@launch
        try { artists = remote.getAllArtists(token) } catch (e: Exception) { }
    }

    fun toggleFollow(artistId: Int) = CoroutineScope(Dispatchers.Main).launch {
        val token = SessionStore.token ?: return@launch
        try {
            val index = artists.indexOfFirst { it.id == artistId }
            if (index < 0) return@launch
            val artist = artists[index]
            if (artist.isFollowing) remote.unfollowArtist(token, artistId)
            else remote.followArtist(token, artistId)
            val updated = artists.toMutableList()
            updated[index] = artist.copy(
                isFollowing   = !artist.isFollowing,
                followerCount = if (artist.isFollowing) artist.followerCount - 1
                else artist.followerCount + 1
            )
            artists = updated.toList()
        } catch (e: Exception) { }
    }

    fun toggleFavorite(artworkId: Int) = CoroutineScope(Dispatchers.Main).launch {
        if (artworkId in favoriteIds) {
            removeFavoriteUseCase(artworkId).onSuccess { loadFavorites() }
        } else {
            addFavoriteUseCase(artworkId).onSuccess { loadFavorites() }
        }
    }

    fun toggleInterest(artworkId: Int) = CoroutineScope(Dispatchers.Main).launch {
        if (artworkId in interestIds) {
            removeInterestUseCase(artworkId).onSuccess { loadInterests() }
        } else {
            addInterestUseCase(artworkId).onSuccess { loadInterests() }
        }
    }

    fun sendMessage(receiverId: Int, artworkId: Int, content: String) =
        CoroutineScope(Dispatchers.Main).launch {
            sendMessageUseCase(receiverId, artworkId, content)
        }
}

// ── Galería ────────────────────────────────────────────────────────────────
@Composable
fun UserGalleryScreen(
    viewModel: UserViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadArtworks()
        viewModel.loadFavorites()
        viewModel.loadInterests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ÁBSIDE", style = MaterialTheme.typography.titleLarge.copy(
                            color = TealPrimary, letterSpacing = 2.sp))
                        Text("  Galería", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("SALIR", color = GrayText, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { UserBottomBar(navController, Screen.UserGallery.route) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                            .background(Brush.horizontalGradient(listOf(
                                TealPrimary.copy(alpha = 0.15f),
                                PurplePrimary.copy(alpha = 0.10f),
                                MaterialTheme.colorScheme.background)))
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.align(Alignment.CenterStart)) {
                            Text("Explorar Galería",
                                style = MaterialTheme.typography.headlineMedium
                                    .copy(color = MaterialTheme.colorScheme.onBackground))
                            Text("${viewModel.artworks.size} obras disponibles",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (viewModel.artworks.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(60.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🖼", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("No hay obras disponibles",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                items(items = viewModel.artworks, key = { it.id }) { artwork ->
                    GalleryArtworkCard(
                        artwork      = artwork,
                        isFavorite   = artwork.id in viewModel.favoriteIds,
                        isInterested = artwork.id in viewModel.interestIds,
                        reactions    = viewModel.reactions[artwork.id] ?: emptyList(),
                        myReaction   = viewModel.myReactions[artwork.id],
                        onFavoriteClick = { viewModel.toggleFavorite(artwork.id) },
                        onClick = { navController.navigate("artwork/detail/${artwork.id}") }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Tarjeta obra ───────────────────────────────────────────────────────────
@Composable
fun GalleryArtworkCard(
    artwork: Artwork,
    isFavorite: Boolean,
    isInterested: Boolean,
    reactions: List<ReactionCountDto> = emptyList(),
    myReaction: String? = null,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (artwork.imageUrl.isNotBlank()) NetworkImage(artwork.imageUrl)
                else Text("🖼", fontSize = 48.sp)

                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                        .size(40.dp).clip(CircleShape)
                        .background(BlackCard.copy(alpha = 0.7f))
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isFavorite) "♥" else "♡", fontSize = 18.sp,
                        color = if (isFavorite) TealPrimary else WhiteText)
                }

                if (isInterested) {
                    Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurplePrimary.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("❤️ Me interesa", fontSize = 11.sp, color = WhiteText) }
                }

                if (reactions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        reactions.take(4).forEach { r ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BlackCard.copy(alpha = 0.7f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${r.emoji} ${r.count}", fontSize = 11.sp, color = WhiteText)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(artwork.title, style = MaterialTheme.typography.titleLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    Text("por ${artwork.artistName}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TealPrimary),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text("›", fontSize = 28.sp, color = TealPrimary)
            }
        }
    }
}

// ── Buscar ─────────────────────────────────────────────────────────────────
@Composable
fun UserSearchScreen(
    viewModel: UserViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadArtworks()
        viewModel.loadFavorites()
        viewModel.loadInterests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar", style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary)) },
                actions = { TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { UserBottomBar(navController, Screen.UserSearch.route) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad)) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                label = { Text("Buscar obras o artistas...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Text("🔍", fontSize = 18.sp) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotBlank())
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Text("✕", color = GrayText)
                        }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            if (viewModel.searchQuery.isNotBlank()) {
                Text("${viewModel.filteredArtworks.size} resultados para \"${viewModel.searchQuery}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                when {
                    viewModel.searchQuery.isBlank() -> item {
                        Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Escribe para buscar obras o artistas",
                                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    viewModel.filteredArtworks.isEmpty() -> item {
                        Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("😕", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("No se encontraron resultados",
                                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    else -> items(viewModel.filteredArtworks) { artwork ->
                        SearchArtworkCard(artwork = artwork,
                            onClick = { navController.navigate("artwork/detail/${artwork.id}") })
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun SearchArtworkCard(artwork: Artwork, onClick: () -> Unit) {
    Card(onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                if (artwork.imageUrl.isNotBlank()) NetworkImage(artwork.imageUrl)
                else Text("🖼", fontSize = 28.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artwork.title, style = MaterialTheme.typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("por ${artwork.artistName}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TealPrimary), maxLines = 1)
                if (artwork.description.isNotBlank())
                    Text(artwork.description, style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
            }
            Text("›", fontSize = 24.sp, color = TealPrimary)
        }
    }
}

// ── Favoritos ──────────────────────────────────────────────────────────────
@Composable
fun UserFavoritesScreen(
    viewModel: UserViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadFavorites(); viewModel.loadInterests() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary))
                        Text("${viewModel.favorites.size} obras guardadas", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                actions = { TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { UserBottomBar(navController, Screen.UserFavorites.route) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (viewModel.favorites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                    Text("♡", fontSize = 64.sp, color = TealPrimary)
                    Spacer(Modifier.height(16.dp))
                    Text("Aún no tienes favoritos", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Toca el ♡ en cualquier obra para guardarla aquí",
                        style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { navController.navigate(Screen.UserGallery.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp)) { Text("Explorar galería") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(pad),
                verticalArrangement = Arrangement.spacedBy(0.dp)) {
                items(items = viewModel.favorites, key = { it.id }) { artwork ->
                    GalleryArtworkCard(
                        artwork = artwork, isFavorite = true,
                        isInterested = artwork.id in viewModel.interestIds,
                        reactions = viewModel.reactions[artwork.id] ?: emptyList(),
                        myReaction = viewModel.myReactions[artwork.id],
                        onFavoriteClick = { viewModel.toggleFavorite(artwork.id) },
                        onClick = { navController.navigate("artwork/detail/${artwork.id}") }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Perfil ─────────────────────────────────────────────────────────────────
@Composable
fun UserProfileScreen(
    viewModel: UserViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    val user = SessionStore.currentUser
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
        viewModel.loadInterests()
        viewModel.loadMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary)) },
                actions = { TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { UserBottomBar(navController, Screen.UserProfile.route) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad),
            horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)
                    .background(Brush.verticalGradient(listOf(
                        TealPrimary.copy(alpha = 0.3f),
                        PurplePrimary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background))),
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileAvatar(emoji = "👤", size = 88, borderColor = TealPrimary)
                        PhotoHint()
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                NicknameEditor(realName = user?.name ?: "Usuario")
                Spacer(Modifier.height(4.dp))
                Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = TealPrimary.copy(alpha = 0.15f)) {
                    Text("  ${user?.role?.name ?: "USUARIO"}  ", color = TealPrimary, fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileStatCard("♥", viewModel.favorites.size.toString(), "Favoritos",
                        TealPrimary, Modifier.weight(1f))
                    ProfileStatCard("❤️", viewModel.interests.size.toString(), "Me interesa",
                        PurplePrimary, Modifier.weight(1f))
                    ProfileStatCard("✉️", viewModel.messages.size.toString(), "Mensajes",
                        Color(0xFF2196F3), Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
            }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Mi cuenta",
                        style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary),
                        modifier = Modifier.padding(bottom = 4.dp))
                    ProfileActionCard("♥", "Mis favoritos",
                        "${viewModel.favorites.size} obras guardadas", TealPrimary
                    ) { navController.navigate(Screen.UserFavorites.route) }
                    ProfileActionCard("❤️", "Me interesa",
                        "${viewModel.interests.size} obras", PurplePrimary
                    ) { navController.navigate(Screen.UserFavorites.route) }
                    ProfileActionCard("✉️", "Mensajes recibidos",
                        "${viewModel.messages.size} mensajes", Color(0xFF2196F3)
                    ) { navController.navigate(Screen.UserMessages.route) }
                    ProfileActionCard("🎨", "Artistas",
                        "Descubre y sigue artistas", Color(0xFFFF9800)
                    ) { navController.navigate(Screen.UserArtists.route) }
                    ProfileActionCard("♿", "Accesibilidad",
                        "Adapta la interfaz a tus necesidades", Color(0xFF9C27B0)
                    ) { navController.navigate(Screen.Accessibility.route) }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.error)
                    ) { Text("CERRAR SESIÓN", style = MaterialTheme.typography.labelLarge) }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ── Mensajes recibidos ─────────────────────────────────────────────────────
@Composable
fun UserMessagesScreen(
    viewModel: UserViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadMessages() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mensajes", style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary))
                        Text("${viewModel.messages.size} recibidos", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("←", color = GrayText, fontSize = 20.sp)
                    }
                },
                actions = { TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { UserBottomBar(navController, Screen.UserProfile.route) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (viewModel.messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                    Text("✉️", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes mensajes", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Cuando alguien te escriba aparecerá aquí",
                        style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(viewModel.messages) { msg -> MessageCard(msg) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun MessageCard(msg: MessageDto) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(TealPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center) { Text("👤", fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(msg.senderName, style = MaterialTheme.typography.titleLarge)
                    Text("Sobre: ${msg.artworkTitle}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TealPrimary),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(msg.createdAt.take(10), style = MaterialTheme.typography.bodyMedium, fontSize = 11.sp)
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(10.dp))
            Text(msg.content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// ── Artistas ───────────────────────────────────────────────────────────────
@Composable
fun UserArtistsScreen(
    viewModel: UserViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadArtists() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Artistas", style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary))
                        Text("${viewModel.artists.size} artistas", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("←", color = GrayText, fontSize = 20.sp)
                    }
                },
                actions = { TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = { UserBottomBar(navController, Screen.UserProfile.route) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (viewModel.artists.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                    Text("🎨", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No hay artistas aún", style = MaterialTheme.typography.headlineSmall)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items = viewModel.artists, key = { it.id }) { artist ->
                    ArtistPublicCard(
                        artist   = artist,
                        onFollow = { viewModel.toggleFollow(artist.id) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun ArtistPublicCard(artist: ArtistPublicDto, onFollow: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape)
                    .background(TealPrimary.copy(alpha = 0.2f))
                    .border(2.dp, TealPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("🎨", fontSize = 26.sp) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artist.name, style = MaterialTheme.typography.titleLarge)
                Text("${artist.artworkCount} obras · ${artist.followerCount} seguidores",
                    style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onFollow,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (artist.isFollowing)
                        MaterialTheme.colorScheme.surfaceVariant
                    else TealPrimary
                ),
                border = if (artist.isFollowing)
                    androidx.compose.foundation.BorderStroke(1.dp, TealPrimary) else null
            ) {
                Text(
                    if (artist.isFollowing) "Siguiendo" else "Seguir",
                    color = if (artist.isFollowing) TealPrimary else Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ── Bottom Navigation ──────────────────────────────────────────────────────
@Composable
fun UserBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        listOf(
            Triple(Screen.UserGallery.route,   "🖼", "Galería"),
            Triple(Screen.UserSearch.route,    "🔍", "Buscar"),
            Triple(Screen.UserFavorites.route, "♥",  "Favoritos"),
            Triple(Screen.UserProfile.route,   "👤", "Perfil")
        ).forEach { (route, icon, label) ->
            val selected = currentRoute == route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.UserGallery.route)
                            launchSingleTop = true
                        }
                    }
                },
                icon  = { Text(icon, fontSize = 22.sp, color = if (selected) TealPrimary else GrayText) },
                label = { Text(label, fontSize = 11.sp, color = if (selected) TealPrimary else GrayText) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = TealPrimary.copy(alpha = 0.15f))
            )
        }
    }
}

// ── Componentes ────────────────────────────────────────────────────────────
@Composable
fun ProfileStatCard(icon: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 24.sp, color = color)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(color = color))
            Text(label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ProfileActionCard(icon: String, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Text("›", fontSize = 24.sp, color = color)
        }
    }
}