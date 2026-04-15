package com.absaide.gallery.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Completa todos los campos"; return
        }
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
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (Role) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Logo Ábside ────────────────────────────────────────────
            AbsideLogo()

            Spacer(Modifier.height(48.dp))

            // ── Formulario ─────────────────────────────────────────────
            AbsaideTextField(
                viewModel.email, { viewModel.email = it }, "Email"
            )
            Spacer(Modifier.height(16.dp))
            AbsaideTextField(
                viewModel.password, { viewModel.password = it },
                "Contraseña", isPassword = true
            )

            viewModel.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(32.dp))

            AbsaideButton(
                "Iniciar Sesión",
                { viewModel.login(onLoginSuccess) },
                Modifier.fillMaxWidth(),
                loading = viewModel.isLoading
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TealPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary)
            ) {
                Text(
                    "CREAR CUENTA",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Logo SVG en Compose ────────────────────────────────────────────────────
@Composable
fun AbsideLogo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Arco morado (ícono)
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
                val w = size.width
                val h = size.height
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 8f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
                val purple = PurplePrimary

                // Arco exterior
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.05f, h * 0.95f)
                        lineTo(w * 0.05f, h * 0.45f)
                        cubicTo(
                            w * 0.05f, h * 0.10f,
                            w * 0.95f, h * 0.10f,
                            w * 0.95f, h * 0.45f
                        )
                        lineTo(w * 0.95f, h * 0.95f)
                    },
                    color = purple,
                    style = stroke
                )

                // Arco interior
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.22f, h * 0.95f)
                        lineTo(w * 0.22f, h * 0.50f)
                        cubicTo(
                            w * 0.22f, h * 0.28f,
                            w * 0.78f, h * 0.28f,
                            w * 0.78f, h * 0.50f
                        )
                        lineTo(w * 0.78f, h * 0.95f)
                    },
                    color = purple,
                    style = stroke
                )

                // Línea central
                drawLine(
                    color = purple,
                    start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.62f),
                    end   = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.95f),
                    strokeWidth = 8f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ÁBSIDE en teal
        Text(
            "ÁBSIDE",
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            color = TealPrimary,
            letterSpacing = 4.sp
        )

        // GALERÍA DE ARTE en morado
        Text(
            "GALERÍA DE ARTE",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = PurplePrimary,
            letterSpacing = 3.sp
        )
    }
}