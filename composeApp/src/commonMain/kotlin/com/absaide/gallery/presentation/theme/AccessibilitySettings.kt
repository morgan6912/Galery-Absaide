
package com.absaide.gallery.presentation.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Estado global de accesibilidad
// Toda la app lee este objeto para adaptar su UI
object AccessibilitySettings {

    // Alto contraste — fondo blanco, texto negro
    var highContrast by mutableStateOf(false)

    // Modo daltonismo
    var colorBlindMode by mutableStateOf(ColorBlindMode.NONE)

    // Tamaño de texto
    var textSize by mutableStateOf(TextSizeMode.NORMAL)

    // Botones más grandes
    var largeButtons by mutableStateOf(false)

    fun reset() {
        highContrast    = false
        colorBlindMode  = ColorBlindMode.NONE
        textSize        = TextSizeMode.NORMAL
        largeButtons    = false
    }
}

enum class ColorBlindMode {
    NONE,         // Sin filtro
    PROTANOPIA,   // Sin rojo  → usa azul/amarillo
    DEUTERANOPIA, // Sin verde → usa azul/naranja
    TRITANOPIA    // Sin azul  → usa rojo/verde
}

enum class TextSizeMode {
    SMALL,   // 85%
    NORMAL,  // 100%
    LARGE,   // 125%
    XLARGE   // 150%
}
