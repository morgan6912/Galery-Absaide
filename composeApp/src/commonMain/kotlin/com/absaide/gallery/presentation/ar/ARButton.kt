package com.absaide.gallery.presentation.ar

import androidx.compose.runtime.Composable

@Composable
expect fun ARButton(
    artworkImageUrl: String,
    artworkTitle: String,
    artworkDescription: String
)