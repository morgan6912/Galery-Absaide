package com.absaide.gallery.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Paleta Ábside ─────────────────────────────────────────────────────────
val Black         = Color(0xFF000000)
val BlackSurface  = Color(0xFF0D0D0D)
val BlackCard     = Color(0xFF1A1A1A)
val BlackElevated = Color(0xFF242424)

// Colores principales del logo
val TealPrimary   = Color(0xFF00A896)  // Verde teal — ÁBSIDE
val TealLight     = Color(0xFF4DD0C4)  // Teal claro
val TealDark      = Color(0xFF007A6C)  // Teal oscuro
val TealGlow      = Color(0x4400A896)  // Teal transparente

val PurplePrimary = Color(0xFF6B2D8B)  // Morado — arco + subtítulo
val PurpleLight   = Color(0xFF9B5CB8)  // Morado claro
val PurpleDark    = Color(0xFF4A1D63)  // Morado oscuro
val PurpleGlow    = Color(0x446B2D8B)  // Morado transparente

val WhiteText     = Color(0xFFF5F5F5)
val GrayText      = Color(0xFF9E9E9E)
val GrayBorder    = Color(0xFF2E2E2E)

// Mantener compatibilidad con código existente
val PinkPrimary   = TealPrimary
val PinkLight     = TealLight
val PinkDark      = TealDark
val PinkGlow      = TealGlow

// ── Paletas daltonismo ────────────────────────────────────────────────────
val ProtanopiaScheme = darkColorScheme(
    primary          = Color(0xFF0057B8),
    onPrimary        = Color(0xFFFFFFFF),
    background       = Color(0xFF000000),
    onBackground     = Color(0xFFF5F5F5),
    surface          = Color(0xFF0D0D0D),
    onSurface        = Color(0xFFF5F5F5),
    surfaceVariant   = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline          = Color(0xFF2E2E2E),
    secondary        = Color(0xFFFFD700)
)

val DeuteranopiaScheme = darkColorScheme(
    primary          = Color(0xFF0057B8),
    onPrimary        = Color(0xFFFFFFFF),
    background       = Color(0xFF000000),
    onBackground     = Color(0xFFF5F5F5),
    surface          = Color(0xFF0D0D0D),
    onSurface        = Color(0xFFF5F5F5),
    surfaceVariant   = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline          = Color(0xFF2E2E2E),
    secondary        = Color(0xFFFF8C00)
)

val TritanopiaScheme = darkColorScheme(
    primary          = Color(0xFFCC3300),
    onPrimary        = Color(0xFFFFFFFF),
    background       = Color(0xFF000000),
    onBackground     = Color(0xFFF5F5F5),
    surface          = Color(0xFF0D0D0D),
    onSurface        = Color(0xFFF5F5F5),
    surfaceVariant   = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF9E9E9E),
    outline          = Color(0xFF2E2E2E),
    secondary        = Color(0xFF00AA44)
)

val HighContrastScheme = lightColorScheme(
    primary          = Color(0xFF000000),
    onPrimary        = Color(0xFFFFFFFF),
    background       = Color(0xFFFFFFFF),
    onBackground     = Color(0xFF000000),
    surface          = Color(0xFFF5F5F5),
    onSurface        = Color(0xFF000000),
    surfaceVariant   = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF000000),
    outline          = Color(0xFF000000),
    secondary        = Color(0xFF000000)
)

// ── Tipografía adaptativa ─────────────────────────────────────────────────
fun getTypography(mode: TextSizeMode): Typography {
    val scale = when (mode) {
        TextSizeMode.SMALL  -> 0.85f
        TextSizeMode.NORMAL -> 1.00f
        TextSizeMode.LARGE  -> 1.25f
        TextSizeMode.XLARGE -> 1.50f
    }
    return Typography(
        displayMedium  = TextStyle(fontSize = (45 * scale).sp, fontWeight = FontWeight.Black),
        headlineLarge  = TextStyle(fontSize = (32 * scale).sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        headlineMedium = TextStyle(fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold),
        headlineSmall  = TextStyle(fontSize = (24 * scale).sp, fontWeight = FontWeight.SemiBold),
        titleLarge     = TextStyle(fontSize = (22 * scale).sp, fontWeight = FontWeight.SemiBold),
        bodyLarge      = TextStyle(fontSize = (16 * scale).sp, color = WhiteText),
        bodyMedium     = TextStyle(fontSize = (14 * scale).sp, color = GrayText),
        labelLarge     = TextStyle(fontSize = (14 * scale).sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
    )
}

// ── Tema principal ────────────────────────────────────────────────────────
@Composable
fun GaleryAbsaideTheme(content: @Composable () -> Unit) {
    val settings = AccessibilitySettings

    val colorScheme = when {
        settings.highContrast -> HighContrastScheme
        settings.colorBlindMode == ColorBlindMode.PROTANOPIA   -> ProtanopiaScheme
        settings.colorBlindMode == ColorBlindMode.DEUTERANOPIA -> DeuteranopiaScheme
        settings.colorBlindMode == ColorBlindMode.TRITANOPIA   -> TritanopiaScheme
        else -> darkColorScheme(
            primary          = TealPrimary,
            onPrimary        = WhiteText,
            secondary        = PurplePrimary,
            onSecondary      = WhiteText,
            background       = Black,
            onBackground     = WhiteText,
            surface          = BlackSurface,
            onSurface        = WhiteText,
            surfaceVariant   = BlackCard,
            onSurfaceVariant = GrayText,
            outline          = GrayBorder,
            error            = Color(0xFFCF6679)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = getTypography(settings.textSize),
        content     = content
    )
}