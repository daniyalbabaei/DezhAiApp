package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ChatGptEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A34),
    onPrimaryContainer = EmeraldLight,
    secondary = DezhAiTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = DezhAiPurple,
    background = DarkBg,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextGray,
    outline = DarkBorder,
    outlineVariant = Color(0xFF383838)
)

private val LightColorScheme = lightColorScheme(
    primary = ChatGptEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = DezhAiTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = DezhAiPurple,
    background = LightBg,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF52525B),
    outline = LightBorder,
    outlineVariant = Color(0xFFD4D4D8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep ChatGPT iconic palette consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // ChatGPT shines best in modern dark mode by default, or follows system

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
