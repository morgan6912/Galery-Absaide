package com.absaide.gallery.presentation.ar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.absaide.gallery.presentation.theme.TealPrimary
import com.absaide.gallery.presentation.theme.WhiteText

@Composable
actual fun ARButton(
    artworkImageUrl: String,
    artworkTitle: String,
    artworkDescription: String
) {
    var showAR by remember { mutableStateOf(false) }

    Button(
        onClick  = { showAR = true },
        shape    = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors   = ButtonDefaults.buttonColors(containerColor = TealPrimary)
    ) {
        Text("📷 Ver en mi espacio", color = WhiteText)
    }

    if (showAR) {
        Dialog(
            onDismissRequest = { showAR = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ArtworkARScreen(
                    artworkImageUrl    = artworkImageUrl,
                    artworkTitle       = artworkTitle,
                    artworkDescription = artworkDescription,
                    onBack             = { showAR = false }
                )
            }
        }
    }
}