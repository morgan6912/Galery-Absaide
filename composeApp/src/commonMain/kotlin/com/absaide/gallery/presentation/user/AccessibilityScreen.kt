package com.absaide.gallery.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.absaide.gallery.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(navController: NavController) {

    val settings = AccessibilitySettings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Accesibilidad",
                        style = MaterialTheme.typography.headlineSmall
                            .copy(color = MaterialTheme.colorScheme.primary)
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← Volver", color = GrayText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Alto Contraste ─────────────────────────────────────────
            item {
                AccessibilityCard(title = "👁 Alto Contraste") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Fondo blanco con texto negro",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Ideal para baja vision",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = settings.highContrast,
                            onCheckedChange = {
                                settings.highContrast = it
                                if (it) settings.colorBlindMode = ColorBlindMode.NONE
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor   = MaterialTheme.colorScheme.primary,
                                checkedTrackColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            // ── Modo Daltonismo ────────────────────────────────────────
            item {
                AccessibilityCard(title = "🎨 Modo Daltonismo") {
                    Text(
                        "Selecciona tu tipo de vision:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ColorBlindMode.NONE         to "Sin filtro (predeterminado)",
                            ColorBlindMode.PROTANOPIA   to "Protanopia (sin rojo)",
                            ColorBlindMode.DEUTERANOPIA to "Deuteranopia (sin verde)",
                            ColorBlindMode.TRITANOPIA   to "Tritanopia (sin azul)"
                        ).forEach { (mode, label) ->
                            val selected = settings.colorBlindMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        settings.colorBlindMode = mode
                                        if (mode != ColorBlindMode.NONE) {
                                            settings.highContrast = false
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                if (selected) {
                                    Text(
                                        "✓",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Tamaño de texto ────────────────────────────────────────
            item {
                AccessibilityCard(title = "🔤 Tamaño de Texto") {
                    Text(
                        "Vista previa: Galery Absaide",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            TextSizeMode.SMALL  to "A",
                            TextSizeMode.NORMAL to "A",
                            TextSizeMode.LARGE  to "A",
                            TextSizeMode.XLARGE to "A"
                        ).forEachIndexed { index, (mode, label) ->
                            val selected = settings.textSize == mode
                            val fontSize = listOf(12, 16, 20, 24)[index]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (selected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { settings.textSize = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text  = label,
                                    fontSize = fontSize.sp,
                                    color = if (selected)
                                        Color.White
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Pequeño", "Normal", "Grande", "Extra").forEach { label ->
                            Text(label, style = MaterialTheme.typography.bodyMedium, fontSize = 10.sp)
                        }
                    }
                }
            }

            // ── Botones más grandes ────────────────────────────────────
            item {
                AccessibilityCard(title = "👆 Botones Más Grandes") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Area de toque ampliada",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Facilita la interaccion con la app",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = settings.largeButtons,
                            onCheckedChange = { settings.largeButtons = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    }
                    if (settings.largeButtons) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "Vista previa del botón",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // ── Restablecer ────────────────────────────────────────────
            item {
                OutlinedButton(
                    onClick = { AccessibilitySettings.reset() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("🔄 Restablecer valores predeterminados")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityCard(title: String, content: @Composable ColumnScope.() -> Unit) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}
