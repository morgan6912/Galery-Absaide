@file:OptIn(ExperimentalMaterial3Api::class)
package com.absaide.gallery.presentation.admin

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absaide.gallery.data.model.Artwork
import com.absaide.gallery.data.model.Role
import com.absaide.gallery.data.model.User
import com.absaide.gallery.data.repository.SessionStore
import com.absaide.gallery.domain.usecase.*
import com.absaide.gallery.presentation.artist.NetworkImage
import com.absaide.gallery.presentation.navigation.Screen
import com.absaide.gallery.presentation.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getArtworksUseCase: GetArtworksUseCase,
    private val deleteArtworkUseCase: DeleteArtworkUseCase,
    private val updateRoleUseCase: UpdateRoleUseCase
) {
    var users          by mutableStateOf<List<User>>(emptyList())
    var artworks       by mutableStateOf<List<Artwork>>(emptyList())
    var isLoading      by mutableStateOf(false)
    var errorMessage   by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var searchUsers    by mutableStateOf("")
    var searchArtworks by mutableStateOf("")
    var filterRole     by mutableStateOf<Role?>(null)

    val filteredUsers get() = users.filter {
        (searchUsers.isBlank() || it.name.contains(searchUsers, true) ||
                it.email.contains(searchUsers, true)) &&
                (filterRole == null || it.role == filterRole)
    }
    val filteredArtworks get() = artworks.filter {
        searchArtworks.isBlank() ||
                it.title.contains(searchArtworks, true) ||
                it.artistName.contains(searchArtworks, true)
    }
    val totalAdmins  get() = users.count { it.role == Role.ADMIN }
    val totalArtists get() = users.count { it.role == Role.ARTIST }
    val totalUsers   get() = users.count { it.role == Role.USER }

    fun loadAll() { loadUsers(); loadArtworks() }

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
        deleteUserUseCase(id)
            .onSuccess { successMessage = "Usuario eliminado"; loadUsers() }
            .onFailure { errorMessage = it.message }
    }
    fun deleteArtwork(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        deleteArtworkUseCase(id)
            .onSuccess { successMessage = "Obra eliminada"; loadArtworks() }
            .onFailure { errorMessage = it.message }
    }
    fun updateRole(id: Int, newRole: Role) = CoroutineScope(Dispatchers.Main).launch {
        updateRoleUseCase(id, newRole)
            .onSuccess {
                successMessage = "Rol actualizado a ${newRole.name}"
                loadUsers()
            }
            .onFailure { errorMessage = "Error al cambiar rol: ${it.message}" }
    }
}

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadAll() }
    val admin = SessionStore.currentUser

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(220.dp)
                        .background(
                            Brush.verticalGradient(listOf(
                                TealPrimary.copy(alpha = 0.45f),
                                PurplePrimary.copy(alpha = 0.20f),
                                MaterialTheme.colorScheme.background
                            ))
                        )
                ) {
                    TextButton(onClick = onLogout,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) { Text("SALIR", color = GrayText, fontSize = 12.sp) }
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileAvatar(emoji = "👑", size = 84, borderColor = TealPrimary)
                        PhotoHint()
                        Spacer(Modifier.height(8.dp))
                        NicknameEditor(realName = admin?.name ?: "Administrador")
                        Text(admin?.email ?: "", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = TealPrimary.copy(alpha = 0.2f)) {
                            Text("  ✦ ADMINISTRADOR  ", color = TealPrimary, fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard2("👥", "Usuarios", viewModel.users.size.toString(), TealPrimary, Modifier.weight(1f))
                    StatCard2("🖼", "Obras", viewModel.artworks.size.toString(), PurplePrimary, Modifier.weight(1f))
                    StatCard2("🎨", "Artistas", viewModel.totalArtists.toString(), Color(0xFFFF9800), Modifier.weight(1f))
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Distribución de Roles",
                            style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary),
                            modifier = Modifier.padding(bottom = 16.dp))
                        val total = viewModel.users.size.toFloat().coerceAtLeast(1f)
                        listOf(
                            Triple("Administradores", viewModel.totalAdmins,  TealPrimary),
                            Triple("Artistas",        viewModel.totalArtists, Color(0xFFFF9800)),
                            Triple("Usuarios",        viewModel.totalUsers,   PurplePrimary)
                        ).forEach { (label, count, color) ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                    Text("$count (${((count.toFloat() / total) * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodyMedium, color = color)
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / total },
                                    modifier = Modifier.fillMaxWidth().height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = color,
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Panel de Control",
                        style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary),
                        modifier = Modifier.padding(bottom = 12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdminActionCard("👥", "Gestionar Usuarios",
                            "${viewModel.users.size} usuarios registrados", TealPrimary
                        ) { navController.navigate(Screen.AdminUsers.route) }
                        AdminActionCard("🖼", "Gestionar Obras",
                            "${viewModel.artworks.size} obras publicadas", PurplePrimary
                        ) { navController.navigate(Screen.AdminArtworks.route) }
                        AdminActionCard("🌐", "Ver Galería",
                            "Explorar todas las obras publicadas", Color(0xFF2196F3)
                        ) { navController.navigate(Screen.AdminGallery.route) }
                        AdminActionCard("📊", "Reportes Detallados",
                            "Estadísticas completas de la plataforma", Color(0xFFFF9800)
                        ) { navController.navigate(Screen.AdminReports.route) }
                        AdminActionCard("♿", "Ajustes de Accesibilidad",
                            "Adapta la interfaz a tus necesidades", Color(0xFF9C27B0)
                        ) { navController.navigate(Screen.Accessibility.route) }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            item {
                Text("Obras Recientes",
                    style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            items(viewModel.artworks.take(3)) { artwork ->
                ArtworkMiniCard(artwork = artwork, onDelete = null)
            }
            if (viewModel.artworks.size > 3) {
                item {
                    TextButton(onClick = { navController.navigate(Screen.AdminArtworks.route) },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Ver todas las obras (${viewModel.artworks.size}) →", color = TealPrimary)
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun AdminUsersScreen(
    viewModel: AdminViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadUsers() }
    var showDeleteDialog by remember { mutableStateOf<User?>(null) }

    viewModel.successMessage?.let { msg ->
        LaunchedEffect(msg) {
            delay(2500)
            viewModel.successMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Usuarios",
                    style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary)) },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("←", color = GrayText, fontSize = 20.sp)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp)) {

            // Toast de éxito
            viewModel.successMessage?.let {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = TealPrimary.copy(alpha = 0.15f)),
                    shape    = RoundedCornerShape(10.dp)
                ) {
                    Text("✅ $it", modifier = Modifier.padding(12.dp),
                        color = TealPrimary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.searchUsers,
                onValueChange = { viewModel.searchUsers = it },
                label = { Text("Buscar usuario...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Text("🔍", fontSize = 16.sp) },
                trailingIcon = {
                    if (viewModel.searchUsers.isNotBlank())
                        IconButton(onClick = { viewModel.searchUsers = "" }) {
                            Text("✕", color = GrayText)
                        }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = TealPrimary,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "Todos", Role.ADMIN to "Admin",
                    Role.ARTIST to "Artistas", Role.USER to "Usuarios")
                    .forEach { (role, label) ->
                        FilterChip(
                            selected = viewModel.filterRole == role,
                            onClick  = { viewModel.filterRole = role },
                            label    = { Text(label, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor     = Color.White)
                        )
                    }
            }
            Spacer(Modifier.height(8.dp))
            Text("${viewModel.filteredUsers.size} usuarios encontrados",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viewModel.filteredUsers) { user ->
                        UserAdminCard(
                            user         = user,
                            onDelete     = { showDeleteDialog = user },
                            onChangeRole = { newRole -> viewModel.updateRole(user.id, newRole) }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    showDeleteDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar usuario") },
            text  = { Text("¿Eliminar a ${user.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteUser(user.id); showDeleteDialog = null },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

// ── UserAdminCard con selector de rol ─────────────────────────────────────
@Composable
fun UserAdminCard(
    user: User,
    onDelete: () -> Unit,
    onChangeRole: (Role) -> Unit
) {
    val roleColor = when (user.role) {
        Role.ADMIN  -> TealPrimary
        Role.ARTIST -> Color(0xFFFF9800)
        else        -> PurplePrimary
    }
    val roleEmoji = when (user.role) {
        Role.ADMIN  -> "👑"
        Role.ARTIST -> "🎨"
        else        -> "👤"
    }
    var showRolePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text(roleEmoji, fontSize = 24.sp) }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(user.name, style = MaterialTheme.typography.titleLarge)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = roleColor.copy(alpha = 0.15f)) {
                        Text("  ${user.role.name}  ", color = roleColor,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 2.dp))
                    }
                }

                // Botón cambiar rol
                IconButton(onClick = { showRolePicker = !showRolePicker }) {
                    Text("✏️", fontSize = 18.sp)
                }
                // Botón eliminar
                IconButton(onClick = onDelete) {
                    Text("🗑", fontSize = 18.sp)
                }
            }

            // Selector de rol expandible
            if (showRolePicker) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                Text("Cambiar rol:", color = GrayText, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(Role.USER,   "👤", "Usuario"),
                        Triple(Role.ARTIST, "🎨", "Artista"),
                        Triple(Role.ADMIN,  "👑", "Admin")
                    ).forEach { (role, emoji, label) ->
                        val isCurrentRole = user.role == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isCurrentRole) roleColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = if (isCurrentRole) 2.dp else 1.dp,
                                    color = if (isCurrentRole) roleColor
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = !isCurrentRole) {
                                    onChangeRole(role)
                                    showRolePicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                Text(emoji, fontSize = 20.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(label, fontSize = 11.sp,
                                    color = if (isCurrentRole) roleColor else GrayText)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick  = { showRolePicker = false },
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Cancelar", color = GrayText, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun AdminArtworksScreen(
    viewModel: AdminViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadArtworks() }
    var showDeleteDialog by remember { mutableStateOf<Artwork?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Obras",
                    style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary)) },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("←", color = GrayText, fontSize = 20.sp)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.searchArtworks,
                onValueChange = { viewModel.searchArtworks = it },
                label = { Text("Buscar por título o artista...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Text("🔍", fontSize = 16.sp) },
                trailingIcon = {
                    if (viewModel.searchArtworks.isNotBlank())
                        IconButton(onClick = { viewModel.searchArtworks = "" }) {
                            Text("✕", color = GrayText)
                        }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = TealPrimary,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Spacer(Modifier.height(8.dp))
            Text("${viewModel.filteredArtworks.size} obras",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            } else if (viewModel.filteredArtworks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖼", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No se encontraron obras",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.filteredArtworks) { artwork ->
                        ArtworkAdminCard(artwork = artwork,
                            onDelete = { showDeleteDialog = artwork })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    showDeleteDialog?.let { artwork ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar obra") },
            text  = { Text("¿Eliminar \"${artwork.title}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteArtwork(artwork.id); showDeleteDialog = null },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun AdminReportsScreen(viewModel: AdminViewModel, onLogout: () -> Unit) {
    LaunchedEffect(Unit) { viewModel.loadAll() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes",
                    style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary)) },
                actions = {
                    TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Estadísticas de la Plataforma",
                    style = MaterialTheme.typography.headlineSmall.copy(color = TealPrimary))
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("👥", "Total usuarios", viewModel.users.size.toString(), Modifier.weight(1f))
                    MetricCard("🖼", "Total obras", viewModel.artworks.size.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("🎨", "Artistas", viewModel.totalArtists.toString(), Modifier.weight(1f))
                    MetricCard("👑", "Admins", viewModel.totalAdmins.toString(), Modifier.weight(1f))
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Artistas más activos",
                            style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary),
                            modifier = Modifier.padding(bottom = 12.dp))
                        val artistWorkCount = viewModel.artworks
                            .groupBy { it.artistName }
                            .mapValues { it.value.size }
                            .entries.sortedByDescending { it.value }.take(5)
                        if (artistWorkCount.isEmpty()) {
                            Text("No hay datos disponibles",
                                style = MaterialTheme.typography.bodyMedium)
                        } else {
                            val maxCount = artistWorkCount.maxOf { it.value }.toFloat()
                            artistWorkCount.forEachIndexed { index, (artist, count) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text("${index + 1}.", color = TealPrimary,
                                        modifier = Modifier.width(24.dp), fontSize = 14.sp)
                                    Text(artist, modifier = Modifier.width(120.dp),
                                        fontSize = 13.sp, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis)
                                    LinearProgressIndicator(
                                        progress = { count.toFloat() / maxCount },
                                        modifier = Modifier.weight(1f).height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = TealPrimary,
                                        trackColor = MaterialTheme.colorScheme.outline
                                            .copy(alpha = 0.3f)
                                    )
                                    Text(" $count", fontSize = 13.sp, color = TealPrimary)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StatCard2(icon: String, label: String, value: String, color: Color,
              modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(color = color))
            Text(label, style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AdminActionCard(icon: String, title: String, subtitle: String,
                    color: Color, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) { Text(icon, fontSize = 24.sp) }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Text("›", fontSize = 24.sp, color = color)
        }
    }
}

@Composable
fun ArtworkAdminCard(artwork: Artwork, onDelete: (() -> Unit)?) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                if (artwork.imageUrl.isNotBlank()) NetworkImage(artwork.imageUrl)
                else Text("🖼", fontSize = 32.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artwork.title, style = MaterialTheme.typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("🎨 ${artwork.artistName}", style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (artwork.description.isNotBlank())
                    Text(artwork.description, style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
            }
            if (onDelete != null)
                IconButton(onClick = onDelete) { Text("🗑", fontSize = 20.sp) }
        }
    }
}

@Composable
fun ArtworkMiniCard(artwork: Artwork, onDelete: (() -> Unit)?) {
    ArtworkAdminCard(artwork = artwork, onDelete = onDelete)
}

@Composable
fun MetricCard(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium
                .copy(color = TealPrimary))
            Text(label, style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center)
        }
    }
}