package com.absaide.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.absaide.gallery.presentation.navigation.AppNavHost
import com.absaide.gallery.presentation.theme.GaleryAbsaideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaleryAbsaideTheme {
                AppNavHost()
            }
        }
    }
}
