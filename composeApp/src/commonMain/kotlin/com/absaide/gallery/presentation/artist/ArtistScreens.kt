package com.absaide.gallery.presentation.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.absaide.gallery.data.datasource.RemoteDataSource
import com.absaide.gallery.data.model.Artwork
import com.absaide.gallery.data.model.MessageDto
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
    private val deleteArtworkUseCase: DeleteArtworkUseCase,
    private val remote: RemoteDataSource
) {
    var artworks           by mutableStateOf<List<Artwork>>(emptyList())
    var messages           by mutableStateOf<List<MessageDto>>(emptyList())
    var isLoading          by mutableStateOf(false)
    var errorMessage       by mutableStateOf<String?>(null)
    var successMessage     by mutableStateOf<String?>(null)
    var uploadTitle        by mutableStateOf("")
    var uploadDescription  by mutableStateOf("")
    var uploadImageUrl     by mutableStateOf("")
    var selectedImageBytes by mutableStateOf<ByteArray?>(null)
    var selectedImageName  by mutableStateOf("")

    fun loadMyWorks() {
        val id = SessionStore.currentUser?.id ?: return
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true
            getArtworksUseCase().onSuccess { artworks = it.filter { a -> a.artistId == id } }
            isLoading = false
        }
    }

    fun loadMessages() = CoroutineScope(Dispatchers.Main).launch {
        try {
            val token = SessionStore.token ?: return@launch
            val msgs = remote.getReceivedMessages(token)
            messages = msgs
        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    fun sendReply(receiverId: Int, artworkId: Int, content: String, onSuccess: () -> Unit) =
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val token = SessionStore.token ?: return@launch
                remote.sendMessage(
                    token,
                    com.absaide.gallery.data.model.MessageRequest(receiverId, artworkId, content)
                )
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al enviar: ${e.message}"
            }
        }

    fun uploadArtwork(onSuccess: () -> Unit) {
        if (uploadTitle.isBlank()) { errorMessage = "El título es obligatorio"; return }
        if (uploadImageUrl.isBlank() && selectedImageBytes == null) {
            errorMessage = "Debes seleccionar una imagen o pegar una URL"; return
        }
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true; errorMessage = null
            var finalUrl = uploadImageUrl
            if (selectedImageBytes != null) {
                try {
                    val token = SessionStore.token
                        ?: run { errorMessage = "Sesión expirada"; isLoading = false; return@launch }
                    finalUrl = remote.uploadImage(token, selectedImageBytes!!,
                        selectedImageName.ifBlank { "imagen.jpg" })
                } catch (e: Exception) {
                    errorMessage = "Error al subir imagen: ${e.message}"
                    isLoading = false; return@launch
                }
            }
            createArtworkUseCase(uploadTitle, uploadDescription, finalUrl)
                .onSuccess {
                    successMessage = "¡Obra publicada! ✓"
                    uploadTitle = ""; uploadDescription = ""
                    uploadImageUrl = ""; selectedImageBytes = null
                    onSuccess()
                }
                .onFailure { errorMessage = it.message ?: "Error al publicar" }
            isLoading = false
        }
    }

    fun deleteArtwork(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        deleteArtworkUseCase(id).onSuccess { loadMyWorks() }
    }
}

// ── Perfil ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistProfileScreen(
    viewModel: ArtistViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    val user = SessionStore.currentUser
    LaunchedEffect(Unit) { viewModel.loadMyWorks(); viewModel.loadMessages() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                        .background(Brush.verticalGradient(listOf(
                            TealPrimary.copy(alpha = 0.4f),
                            PurplePrimary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background)))
                ) {
                    TextButton(onClick = onLogout,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) { Text("SALIR", color = GrayText, fontSize = 12.sp) }

                    Column(modifier = Modifier.align(Alignment.Center).padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileAvatar(emoji = "🎨", size = 88, borderColor = TealPrimary)
                        PhotoHint()
                        Spacer(Modifier.height(8.dp))
                        NicknameEditor(realName = user?.name ?: "Artista")
                        Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = TealPrimary.copy(alpha = 0.2f)) {
                            Text("  ✦ ARTISTA  ", color = TealPrimary, fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickStatCard("🖼", viewModel.artworks.size.toString(), "Obras", Modifier.weight(1f))
                    QuickStatCard("✉️", viewModel.messages.size.toString(), "Mensajes", Modifier.weight(1f))
                    QuickStatCard("❤️", "—", "Me interesa", Modifier.weight(1f))
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Mi espacio",
                        style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary),
                        modifier = Modifier.padding(vertical = 8.dp))
                    ArtistActionCard("📤", "Subir nueva obra", "Publica tu arte en la galería",
                        TealPrimary) { navController.navigate(Screen.ArtistUpload.route) }
                    ArtistActionCard("🖼", "Mis obras", "${viewModel.artworks.size} obras publicadas",
                        Color(0xFF4CAF50)) { navController.navigate(Screen.ArtistMyWorks.route) }
                    ArtistActionCard("✉️", "Mensajes recibidos",
                        "${viewModel.messages.size} mensajes de usuarios",
                        Color(0xFF2196F3)) { navController.navigate(Screen.ArtistMessages.route) }
                    ArtistActionCard("🌐", "Explorar galería", "Ve lo que otros artistas publican",
                        Color(0xFF9C27B0)) { navController.navigate(Screen.ArtistGallery.route) }
                    ArtistActionCard("📈", "Estadísticas", "Rendimiento de tus obras",
                        Color(0xFFFF9800)) { navController.navigate(Screen.ArtistStats.route) }
                    ArtistActionCard("♿", "Accesibilidad", "Adapta la interfaz a tus necesidades",
                        Color(0xFF9C27B0)) { navController.navigate(Screen.Accessibility.route) }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Mensajes del artista ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistMessagesScreen(
    viewModel: ArtistViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadMessages() }
    var replyTo by remember { mutableStateOf<MessageDto?>(null) }
    var replyText by remember { mutableStateOf("") }
    var replySent by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mensajes recibidos",
                            style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary))
                        Text("${viewModel.messages.size} mensajes",
                            style = MaterialTheme.typography.bodyMedium)
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
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (viewModel.messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp)) {
                    Text("✉️", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes mensajes aún",
                        style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Cuando un usuario te escriba aparecerá aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.messages) { msg ->
                    ArtistMessageCard(
                        msg = msg,
                        onReply = {
                            replyTo = msg
                            replyText = ""
                            replySent = false
                        }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    // ── Diálogo responder ──────────────────────────────────────────────
    replyTo?.let { msg ->
        Dialog(
            onDismissRequest = { replyTo = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (replySent) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✅", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("¡Respuesta enviada!",
                                    style = MaterialTheme.typography.titleLarge
                                        .copy(color = TealPrimary))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { replyTo = null; replySent = false; replyText = "" },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) { Text("Cerrar") }
                    } else {
                        // Mensaje original
                        Text("Responder a ${msg.senderName}",
                            style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary))
                        Spacer(Modifier.height(4.dp))
                        Text("Sobre: ${msg.artworkTitle}",
                            style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))

                        // Mensaje original resumido
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = TealPrimary.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Mensaje original:",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TealPrimary), fontSize = 11.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            label = { Text("Escribe tu respuesta...") },
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { replyTo = null; replyText = "" },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("Cancelar") }

                            Button(
                                onClick = {
                                    if (replyText.isNotBlank()) {
                                        viewModel.sendReply(
                                            msg.senderId,
                                            msg.artworkId,
                                            replyText
                                        ) { replySent = true }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                enabled = replyText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) { Text("Responder ✉️") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistMessageCard(msg: MessageDto, onReply: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(TealPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text("👤", fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(msg.senderName, style = MaterialTheme.typography.titleLarge)
                    Text("Sobre: ${msg.artworkTitle}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TealPrimary),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(msg.createdAt.take(10),
                    style = MaterialTheme.typography.bodyMedium, fontSize = 11.sp)
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(10.dp))
            Text(msg.content, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onReply,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) { Text("↩ Responder") }
        }
    }
}

// ── Subir obra ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistUploadScreen(viewModel: ArtistViewModel, navController: NavController) {
    val imagePicker = rememberImagePicker { bytes, name ->
        viewModel.selectedImageBytes = bytes
        viewModel.selectedImageName  = name
        viewModel.uploadImageUrl     = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subir Obra", style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary)) },
                navigationIcon = { TextButton(onClick = { navController.popBackStack() }) { Text("← Volver", color = GrayText) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Nueva Obra", style = MaterialTheme.typography.headlineSmall.copy(color = TealPrimary))
                Text("Comparte tu arte con la galería", style = MaterialTheme.typography.bodyMedium)
            }
            item { AbsaideTextField(viewModel.uploadTitle, { viewModel.uploadTitle = it }, "Título de la obra *") }
            item { AbsaideTextField(viewModel.uploadDescription, { viewModel.uploadDescription = it }, "Descripción (opcional)") }

            item {
                Text("Imagen *", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { imagePicker.launch() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp,
                        if (viewModel.selectedImageBytes != null) TealPrimary
                        else MaterialTheme.colorScheme.outline)
                ) {
                    Row(modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (viewModel.selectedImageBytes != null) "✅" else "📁", fontSize = 24.sp)
                        Column {
                            Text(
                                if (viewModel.selectedImageBytes != null) "Imagen seleccionada"
                                else "Seleccionar del dispositivo",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (viewModel.selectedImageBytes != null) TealPrimary
                                else MaterialTheme.colorScheme.onSurface)
                            Text(
                                if (viewModel.selectedImageBytes != null) viewModel.selectedImageName
                                else "JPG, PNG, WEBP",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                    Text("  o pega una URL  ", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                }
                Spacer(Modifier.height(12.dp))
                AbsaideTextField(
                    value = viewModel.uploadImageUrl,
                    onValueChange = {
                        viewModel.uploadImageUrl = it
                        if (it.isNotBlank()) viewModel.selectedImageBytes = null
                    },
                    label = "https://i.imgur.com/..."
                )
            }

            if (viewModel.uploadImageUrl.isNotBlank() || viewModel.selectedImageBytes != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (viewModel.selectedImageBytes != null)
                                ImageFromBytes(viewModel.selectedImageBytes!!)
                            else NetworkImage(viewModel.uploadImageUrl)
                        }
                    }
                }
            }

            item {
                viewModel.errorMessage?.let {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                    }
                }
                viewModel.successMessage?.let {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1B5E20).copy(alpha = 0.2f))) {
                        Text(it, color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                    }
                }
            }

            item {
                AbsaideButton(
                    text = if (viewModel.isLoading) "Publicando..." else "Publicar obra",
                    onClick = { viewModel.uploadArtwork { navController.popBackStack() } },
                    modifier = Modifier.fillMaxWidth(), loading = viewModel.isLoading
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Mis obras ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistMyWorksScreen(
    viewModel: ArtistViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadMyWorks() }
    var showDeleteDialog by remember { mutableStateOf<Artwork?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Obras", style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary))
                        Text("${viewModel.artworks.size} publicadas", style = MaterialTheme.typography.bodyMedium)
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
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.ArtistUpload.route) },
                containerColor = TealPrimary, shape = CircleShape) {
                Text("+", color = Color.White, fontSize = 28.sp)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else if (viewModel.artworks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp)) {
                    Text("🎨", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Aún no tienes obras", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Toca el botón + para publicar tu primera obra",
                        style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(viewModel.artworks) { artwork ->
                    ArtworkArtistCard(artwork = artwork, onDelete = { showDeleteDialog = artwork })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    showDeleteDialog?.let { artwork ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar obra") },
            text  = { Text("¿Eliminar \"${artwork.title}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = { viewModel.deleteArtwork(artwork.id); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") } }
        )
    }
}

// ── Estadísticas ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistStatsScreen(
    viewModel: ArtistViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.loadMyWorks() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary)) },
                navigationIcon = { TextButton(onClick = { navController.popBackStack() }) { Text("←", color = GrayText, fontSize = 20.sp) } },
                actions = { TextButton(onClick = onLogout) { Text("SALIR", color = GrayText, fontSize = 12.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Resumen de actividad",
                style = MaterialTheme.typography.headlineSmall.copy(color = TealPrimary)) }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBigCard("🖼", viewModel.artworks.size.toString(), "Obras publicadas", TealPrimary, Modifier.weight(1f))
                    StatBigCard("❤️", "—", "Me interesa", Color(0xFFE91E63), Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBigCard("♥", "—", "Favoritos", Color(0xFFFF5722), Modifier.weight(1f))
                    StatBigCard("✉️", viewModel.messages.size.toString(), "Mensajes", Color(0xFF2196F3), Modifier.weight(1f))
                }
            }
            if (viewModel.artworks.isNotEmpty()) {
                item {
                    Text("Mis obras recientes",
                        style = MaterialTheme.typography.titleLarge.copy(color = TealPrimary),
                        modifier = Modifier.padding(top = 8.dp))
                }
                items(viewModel.artworks.take(3)) { artwork ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center) {
                                if (artwork.imageUrl.isNotBlank()) NetworkImage(artwork.imageUrl)
                                else Text("🖼", fontSize = 28.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(artwork.title, style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(artwork.description.ifBlank { "Sin descripción" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Componentes ────────────────────────────────────────────────────────────
@Composable
fun QuickStatCard(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(color = TealPrimary))
            Text(label, style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center, fontSize = 11.sp)
        }
    }
}

@Composable
fun StatBigCard(icon: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(color = color))
            Text(label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ArtistActionCard(icon: String, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 26.sp)
            }
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
fun ArtworkArtistCard(artwork: Artwork, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                if (artwork.imageUrl.isNotBlank()) NetworkImage(artwork.imageUrl)
                else Text("🖼", fontSize = 32.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artwork.title, style = MaterialTheme.typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(artwork.description.ifBlank { "Sin descripción" },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = TealPrimary.copy(alpha = 0.15f)) {
                    Text("  Publicada  ", color = TealPrimary, fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 2.dp))
                }
            }
            IconButton(onClick = onDelete) { Text("🗑", fontSize = 20.sp) }
        }
    }
}