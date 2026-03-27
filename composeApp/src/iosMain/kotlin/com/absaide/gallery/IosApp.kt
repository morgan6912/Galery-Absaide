package com.absaide.gallery

import androidx.compose.ui.window.ComposeUIViewController
import com.absaide.gallery.presentation.navigation.AppNavHost
import com.absaide.gallery.presentation.theme.GaleryAbsaideTheme

fun MainViewController() = ComposeUIViewController {
    GaleryAbsaideTheme { AppNavHost() }
}
