package com.absaide.gallery.presentation.artist

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

class ArtistViewModel(
    private val getArtworksUseCase: GetArtworksUseCase,
    private val createArtworkUseCase: CreateArtworkUseCase,
    private val deleteArtworkUseCase: DeleteArtworkUseCase
) {
    var artworks          by mutableStateOf<List<Artwork>>(emptyList())
    var isLoading         by mutableStateOf(false)
    var errorMessage      by mutableStateOf<String?>(null)
    var successMessage    by mutableStateOf<String?>(null)
    var uploadTitle       by mutableStateOf("")
    var uploadDescription by mutableStateOf("")
    var uploadImageUrl    by mutableStateOf("")

    fun loadMyWorks() {
        val id = SessionStore.currentUser?.id ?: return
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true
            getArtworksUseCase().onSuccess { artworks = it.filter { a -> a.artistId == id } }
            isLoading = false
        }
    }
    fun uploadArtwork(onSuccess: () -> Unit) {
        if (uploadTitle.isBlank() || uploadImageUrl.isBlank()) { errorMessage = "Título e imagen obligatorios"; return }
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true
            createArtworkUseCase(uploadTitle, uploadDescription, uploadImageUrl)
                .onSuccess { successMessage = "Obra subida ✓"; uploadTitle = ""; uploadDescription = ""; uploadImageUrl = ""; onSuccess() }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }
    fun deleteArtwork(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        deleteArtworkUseCase(id).onSuccess { loadMyWorks() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistProfileScreen(viewModel: ArtistViewModel, navController: NavController, onLogout: () -> Unit) {
    val user = SessionStore.currentUser
    Scaffold(topBar = { GalleryTopBar("Mi Perfil", onLogout) }, containerColor = Black) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(24.dp))
            Box(Modifier.size(100.dp).background(PinkGlow, CircleShape), contentAlignment = Alignment.Center) {
                Text("🎨", fontSize = 48.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(user?.name ?: "", style = MaterialTheme.typography.headlineMedium)
            Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium)
            Surface(shape = RoundedCornerShape(20.dp), color = PinkGlow, modifier = Modifier.padding(top = 6.dp)) {
                Text("  ARTISTA  ", color = PinkPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
            }
            Spacer(Modifier.height(40.dp))
            ArtistMenuBtn("📤  Subir Obra")    { navController.navigate(Screen.ArtistUpload.route) }
            Spacer(Modifier.height(12.dp))
            ArtistMenuBtn("🖼  Mis Obras")     { navController.navigate(Screen.ArtistMyWorks.route) }
            Spacer(Modifier.height(12.dp))
            ArtistMenuBtn("📈  Estadísticas")  { navController.navigate(Screen.ArtistStats.route) }
        }
    }
}

@Composable
fun ArtistMenuBtn(text: String, onClick: () -> Unit) {
    Button(onClick, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BlackCard)) {
        Text(text, color = WhiteText, fontSize = 15.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistUploadScreen(viewModel: ArtistViewModel, navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Subir Obra", style = MaterialTheme.typography.headlineSmall.copy(color = PinkPrimary)) },
            navigationIcon = { TextButton({ navController.popBackStack() }) { Text("← Volver", color = GrayText) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackSurface)) },
        containerColor = Black
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeader("Nueva Obra")
            AbsaideTextField(viewModel.uploadTitle,       { viewModel.uploadTitle = it },       "Título de la obra")
            AbsaideTextField(viewModel.uploadDescription, { viewModel.uploadDescription = it }, "Descripción")
            AbsaideTextField(viewModel.uploadImageUrl,    { viewModel.uploadImageUrl = it },    "URL de la imagen")
            viewModel.errorMessage?.let   { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            viewModel.successMessage?.let { Text(it, color = PinkLight, fontSize = 13.sp) }
            AbsaideButton("Subir obra", { viewModel.uploadArtwork { navController.popBackStack() } },
                Modifier.fillMaxWidth(), loading = viewModel.isLoading)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistMyWorksScreen(viewModel: ArtistViewModel, navController: NavController, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadMyWorks() }
    Scaffold(
        topBar = { GalleryTopBar("Mis Obras", onLogout) },
        floatingActionButton = {
            FloatingActionButton({ navController.navigate(Screen.ArtistUpload.route) }, containerColor = PinkPrimary) {
                Text("+", color = WhiteText, fontSize = 24.sp)
            }
        },
        containerColor = Black
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { SectionHeader("Mis Obras (${viewModel.artworks.size})") }
            if (viewModel.artworks.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No tienes obras aún.\nSube tu primera obra ✦", style = MaterialTheme.typography.bodyMedium)
                }}
            }
            items(viewModel.artworks) { artwork ->
                ArtworkCard(title = artwork.title, artistName = artwork.description, imageUrl = artwork.imageUrl)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistStatsScreen(viewModel: ArtistViewModel, navController: NavController, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadMyWorks() }
    Scaffold(topBar = { GalleryTopBar("Estadísticas", onLogout) }, containerColor = Black) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeader("Mis Estadísticas")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BlackCard)) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖼", fontSize = 28.sp); Spacer(Modifier.height(4.dp))
                        Text(viewModel.artworks.size.toString(), style = MaterialTheme.typography.headlineMedium.copy(color = PinkPrimary))
                        Text("Obras", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Card(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BlackCard)) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("♥", fontSize = 28.sp); Spacer(Modifier.height(4.dp))
                        Text("—", style = MaterialTheme.typography.headlineMedium.copy(color = PinkPrimary))
                        Text("Likes", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}