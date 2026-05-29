package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    secondary = NeonCyan,
    tertiary = RadiantGold,
    background = CosmicBackground,
    surface = CosmicSurface,
    surfaceVariant = CosmicSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = CosmicBackground,
    onTertiary = CosmicBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    secondary = Color(0xFF0D9488),
    tertiary = Color(0xFFCA8A04),
    background = Color(0xFFF9FAFB),
    surface = Color.White,
    surfaceVariant = Color(0xFFF3F4F6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFE5E7EB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark mode for beautiful visual identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
