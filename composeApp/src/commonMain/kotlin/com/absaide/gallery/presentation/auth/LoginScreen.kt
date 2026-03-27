package com.absaide.gallery.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absaide.gallery.data.model.Role
import com.absaide.gallery.domain.usecase.LoginUseCase
import com.absaide.gallery.presentation.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) {
    var email        by mutableStateOf("")
    var password     by mutableStateOf("")
    var isLoading    by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun login(onSuccess: (Role) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true; errorMessage = null
            loginUseCase(email, password)
                .onSuccess { onSuccess(it.user.role) }
                .onFailure { errorMessage = it.message ?: "Error al iniciar sesión" }
            isLoading = false
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: (Role) -> Unit, onNavigateToRegister: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Black, BlackSurface, Black)))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(60.dp))
            Text("✦", fontSize = 52.sp, color = PinkPrimary)
            Spacer(Modifier.height(16.dp))
            Text("GALERY", style = MaterialTheme.typography.displayMedium.copy(color = WhiteText, letterSpacing = 12.sp))
            Text("ABSAIDE", style = MaterialTheme.typography.headlineLarge.copy(color = PinkPrimary, letterSpacing = 8.sp))
            Spacer(Modifier.height(8.dp))
            Text("Arte digital sin límites", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))

            AbsaideTextField(viewModel.email,    { viewModel.email = it },    "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(Modifier.height(16.dp))
            AbsaideTextField(viewModel.password, { viewModel.password = it }, "Contraseña", isPassword = true)

            viewModel.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(32.dp))
            AbsaideButton("Iniciar sesión", { viewModel.login(onLoginSuccess) }, Modifier.fillMaxWidth(), loading = viewModel.isLoading)
            Spacer(Modifier.height(16.dp))
            AbsaideOutlinedButton("Crear cuenta", onNavigateToRegister, Modifier.fillMaxWidth())
            Spacer(Modifier.height(60.dp))
        }
    }
}
