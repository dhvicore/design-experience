package com.example.designex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NightColors = darkColorScheme(
    primary = Accent,
    onPrimary = DeepNavy,
    primaryContainer = Dusk,
    onPrimaryContainer = TextHi,
    secondary = Mint,
    onSecondary = DeepNavy,
    background = DeepNavy,
    onBackground = TextHi,
    surface = Slate,
    onSurface = TextHi,
    surfaceVariant = Slate,
    onSurfaceVariant = TextLo,
    outline = GlassBorder,
    error = Coral,
)

@Composable
fun DesignexTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NightColors, typography = Typography, content = content)
}
