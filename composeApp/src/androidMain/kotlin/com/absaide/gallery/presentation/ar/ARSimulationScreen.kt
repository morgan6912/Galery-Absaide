package com.absaide.gallery.presentation.ar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absaide.gallery.presentation.theme.*

@Composable
fun ARSimulationScreen(
    artworkImageUrl: String,
    artworkTitle: String,
    onBack: () -> Unit
) {
    var artworkSize by remember { mutableStateOf(200.dp) }
    var showOnWall  by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BlackSurface)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlackCard)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp, color = WhiteText)
                    }
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Ver en tu espacio",
                            style = MaterialTheme.typography.titleLarge.copy(color = WhiteText))
                        Text("Tu dispositivo no soporta AR",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD4C4A8))
                    .border(2.dp, GrayBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏠 Vista previa en pared",
                    color = Color(0xFFAA9980),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
                    fontSize = 12.sp)

                if (showOnWall) {
                    Box(
                        modifier = Modifier
                            .size(artworkSize)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BlackCard)
                            .border(8.dp, Color(0xFF8B7355), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🖼", fontSize = (artworkSize.value * 0.3f).sp)
                            Text(artworkTitle,
                                color = GrayText, fontSize = 10.sp,
                                modifier = Modifier.padding(4.dp), maxLines = 1)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👆", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Toca \"Colocar obra\" para ver\ncómo quedaría en tu pared",
                            color = Color(0xFFAA9980),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!showOnWall) {
                    Button(
                        onClick = { showOnWall = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                    ) { Text("🖼 Colocar obra en la pared",
                        style = MaterialTheme.typography.labelLarge) }
                } else {
                    Text("Ajustar tamaño:", color = GrayText, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Pequeña" to 120.dp, "Mediana" to 200.dp, "Grande" to 280.dp)
                            .forEach { (label, size) ->
                                val selected = artworkSize == size
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) PinkPrimary else BlackCard)
                                        .border(1.dp,
                                            if (selected) PinkPrimary else GrayBorder,
                                            RoundedCornerShape(10.dp))
                                        .clickable { artworkSize = size },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label,
                                        color = if (selected) WhiteText else GrayText,
                                        fontSize = 13.sp)
                                }
                            }
                    }

                    Button(
                        onClick = { showOnWall = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlackCard)
                    ) { Text("🔄 Quitar", color = GrayText) }
                }
            }
        }
    }
}

