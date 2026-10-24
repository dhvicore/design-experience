package com.nmims.songstudio.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Paper = Color(0xFFF5F5EC)
val Ink = Color(0xFF202B24)
val Lime = Color(0xFFDCEB9E)
val Lavender = Color(0xFFE6DFF3)

@Composable
fun StudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Ink, onPrimary = Color.White,
            background = Paper, surface = Paper, surfaceVariant = Color(0xFFE8EADD),
            secondaryContainer = Lime, onSecondaryContainer = Ink),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 38.sp, fontWeight = FontWeight.Medium),
            headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 29.sp),
            titleLarge = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.SemiBold),
            labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 1.sp)
        ), content = content
    )
}
