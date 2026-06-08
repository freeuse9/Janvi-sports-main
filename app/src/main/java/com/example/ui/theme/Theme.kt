package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SportsGold,
    secondary = SportsGoldSecondary,
    tertiary = SportsGoldLight,
    background = PanelBackground,
    surface = SecondaryPanel,
    surfaceVariant = SurfaceCard,
    onPrimary = TextDarkBlack,
    onSecondary = TextDarkBlack,
    onBackground = TextIvoryWhite,
    onSurface = TextIvoryWhite,
    onSurfaceVariant = TextIvoryWhite,
    outline = BorderAccent,
    error = CrimsonError
)

private val LightColorScheme = lightColorScheme(
    primary = SportsGoldSecondary,
    secondary = SportsGold,
    tertiary = SportsGoldLight,
    background = Color(0xFFF9F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F1F4),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFF101112),
    onSurface = Color(0xFF101112),
    onSurfaceVariant = Color(0xFF202124),
    outline = Color(0xFFD4D6DC),
    error = CrimsonError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark Gold theme by default as it is stunning and requested
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
