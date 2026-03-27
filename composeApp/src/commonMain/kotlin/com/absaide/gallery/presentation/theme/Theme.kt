package com.absaide.gallery.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Black         = Color(0xFF000000)
val BlackSurface  = Color(0xFF0D0D0D)
val BlackCard     = Color(0xFF1A1A1A)
val BlackElevated = Color(0xFF242424)
val PinkPrimary   = Color(0xFFFF2D78)
val PinkLight     = Color(0xFFFF6FA8)
val PinkDark      = Color(0xFFCC1155)
val PinkGlow      = Color(0x44FF2D78)
val WhiteText     = Color(0xFFF5F5F5)
val GrayText      = Color(0xFF9E9E9E)
val GrayBorder    = Color(0xFF2E2E2E)

@Composable
fun GaleryAbsaideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = PinkPrimary, onPrimary = WhiteText,
            background = Black, onBackground = WhiteText,
            surface = BlackSurface, onSurface = WhiteText,
            surfaceVariant = BlackCard, onSurfaceVariant = GrayText,
            outline = GrayBorder, error = Color(0xFFCF6679)
        ),
        typography = Typography(
            displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Black),
            headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            headlineMedium= TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
            headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
            titleLarge    = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            bodyLarge     = TextStyle(fontSize = 16.sp, color = WhiteText),
            bodyMedium    = TextStyle(fontSize = 14.sp, color = GrayText),
            labelLarge    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        ),
        content = content
    )
}
