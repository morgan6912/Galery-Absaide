package com.absaide.gallery.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absaide.gallery.data.model.Artwork
import com.absaide.gallery.data.model.User
import com.absaide.gallery.domain.usecase.*
import com.absaide.gallery.presentation.navigation.Screen
import com.absaide.gallery.presentation.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getArtworksUseCase: GetArtworksUseCase,
    private val deleteArtworkUseCase: DeleteArtworkUseCase
) {
    var users        by mutableStateOf<List<User>>(emptyList())
    var artworks     by mutableStateOf<List<Artwork>>(emptyList())
    var isLoading    by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadUsers() = CoroutineScope(Dispatchers.Main).launch {
        isLoading = true
        getUsersUseCase().onSuccess { users = it }.onFailure { errorMessage = it.message }
        isLoading = false
    }
    fun loadArtworks() = CoroutineScope(Dispatchers.Main).launch {
        isLoading = true
        getArtworksUseCase().onSuccess { artworks = it }.onFailure { errorMessage = it.message }
        isLoading = false
    }
    fun deleteUser(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        deleteUserUseCase(id).onSuccess { loadUsers() }
    }
    fun deleteArtwork(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        deleteArtworkUseCase(id).onSuccess { loadArtworks() }
    }
}

@Composable
fun AdminDashboardScreen(viewModel: AdminViewModel, navController: NavController, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadUsers(); viewModel.loadArtworks() }
    Scaffold(topBar = { GalleryTopBar("Panel Admin", onLogout) }, containerColor = Black) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { SectionHeader("Dashboard") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Usuarios", viewModel.users.size.toString(),    Modifier.weight(1f))
                    StatCard("Obras",    viewModel.artworks.size.toString(), Modifier.weight(1f))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { AdminMenuBtn("👥  Gestionar Usuarios") { navController.navigate(Screen.AdminUsers.route) } }
            item { AdminMenuBtn("🖼  Gestionar Obras")    { navController.navigate(Screen.AdminArtworks.route) } }
            item { AdminMenuBtn("📊  Reportes")           { navController.navigate(Screen.AdminReports.route) } }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BlackCard)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.displayMedium.copy(color = PinkPrimary))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AdminMenuBtn(text: String, onClick: () -> Unit) {
    Button(onClick, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BlackCard)) {
        Text(text, color = WhiteText, fontSize = 15.sp)
    }
}

@Composable
fun AdminUsersScreen(viewModel: AdminViewModel, navController: NavController, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadUsers() }
    Scaffold(topBar = { GalleryTopBar("Usuarios", onLogout) }, containerColor = Black) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SectionHeader("Gestionar Usuarios") }
            items(viewModel.users) { user ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackCard)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(user.name,  style = MaterialTheme.typography.titleLarge)
                            Text(user.email, style = MaterialTheme.typography.bodyMedium)
                            Text(user.role.name, color = PinkPrimary, fontSize = 12.sp)
                        }
                        IconButton({ viewModel.deleteUser(user.id) }) { Text("🗑", fontSize = 20.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminArtworksScreen(viewModel: AdminViewModel, navController: NavController, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadArtworks() }
    Scaffold(topBar = { GalleryTopBar("Obras", onLogout) }, containerColor = Black) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { SectionHeader("Gestionar Obras (${viewModel.artworks.size})") }
            items(viewModel.artworks) { artwork ->
                ArtworkCard(title = artwork.title, artistName = artwork.artistName, imageUrl = artwork.imageUrl)
            }
        }
    }
}

@Composable
fun AdminReportsScreen(viewModel: AdminViewModel, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadUsers(); viewModel.loadArtworks() }
    Scaffold(topBar = { GalleryTopBar("Reportes", onLogout) }, containerColor = Black) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { SectionHeader("Reportes del Sistema") }
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackCard)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("👥", fontSize = 36.sp, modifier = Modifier.padding(end = 16.dp))
                        Column(Modifier.weight(1f)) { Text("Total Usuarios", style = MaterialTheme.typography.bodyMedium) }
                        Text(viewModel.users.size.toString(), style = MaterialTheme.typography.displayMedium.copy(color = PinkPrimary))
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BlackCard)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🖼", fontSize = 36.sp, modifier = Modifier.padding(end = 16.dp))
                        Column(Modifier.weight(1f)) { Text("Total Obras", style = MaterialTheme.typography.bodyMedium) }
                        Text(viewModel.artworks.size.toString(), style = MaterialTheme.typography.displayMedium.copy(color = PinkPrimary))
                    }
                }
            }
        }
    }
}
