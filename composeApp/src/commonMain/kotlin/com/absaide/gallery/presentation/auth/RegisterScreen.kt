package com.absaide.gallery.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absaide.gallery.data.model.Role
import com.absaide.gallery.domain.usecase.RegisterUseCase
import com.absaide.gallery.presentation.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel(private val registerUseCase: RegisterUseCase) {
    var name            by mutableStateOf("")
    var email           by mutableStateOf("")
    var password        by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var selectedRole    by mutableStateOf(Role.USER)
    var isLoading       by mutableStateOf(false)
    var errorMessage    by mutableStateOf<String?>(null)

    fun register(onSuccess: (Role) -> Unit) {
        if (password != confirmPassword) { errorMessage = "Las contraseñas no coinciden"; return }
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Todos los campos son obligatorios"; return
        }
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true; errorMessage = null
            registerUseCase(name, email, password, selectedRole)
                .onSuccess { onSuccess(it.user.role) }
                .onFailure { errorMessage = it.message ?: "Error al registrarse" }
            isLoading = false
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: (Role) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Black, BlackSurface, Black)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            Text("✦", fontSize = 36.sp, color = PinkPrimary)
            Spacer(Modifier.height(12.dp))
            Text(
                "CREAR CUENTA",
                style = MaterialTheme.typography.headlineLarge.copy(color = WhiteText, letterSpacing = 6.sp)
            )
            Text("Únete a la galería", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(40.dp))

            AbsaideTextField(viewModel.name,  { viewModel.name = it },  "Nombre completo")
            Spacer(Modifier.height(16.dp))
            AbsaideTextField(
                viewModel.email, { viewModel.email = it }, "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(16.dp))
            AbsaideTextField(viewModel.password, { viewModel.password = it }, "Contraseña", isPassword = true)
            Spacer(Modifier.height(16.dp))
            AbsaideTextField(viewModel.confirmPassword, { viewModel.confirmPassword = it }, "Confirmar contraseña", isPassword = true)
            Spacer(Modifier.height(16.dp))

            // Selector de rol con botones
            Text("Selecciona tu rol", color = GrayText, fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(Role.USER to "Usuario", Role.ARTIST to "Artista", Role.ADMIN to "Admin")
                    .forEach { (role, label) ->
                        val selected = viewModel.selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) PinkPrimary else BlackCard)
                                .border(1.dp,
                                    if (selected) PinkPrimary else GrayBorder,
                                    RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectedRole = role },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) WhiteText else GrayText,
                                fontSize = 13.sp
                            )
                        }
                    }
            }

            viewModel.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(32.dp))
            AbsaideButton("Registrarse", { viewModel.register(onRegisterSuccess) },
                Modifier.fillMaxWidth(), loading = viewModel.isLoading)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? Iniciar sesión", color = PinkLight, fontSize = 13.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}