package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightFinancialColorScheme = lightColorScheme(
    primary = SearchButtonRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBEE),
    onPrimaryContainer = Color(0xFFB71C1C),
    secondary = BannerGoldenGreen,
    onSecondary = Color.White,
    background = LightAppBackground,
    onBackground = TextPrimaryDark,
    surface = LightCardBackground,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = TextSecondaryMuted,
    outline = LightCardBorder,
    error = BannerInvertedRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // 強制使用現代簡潔明亮金融卡片風格 (Light Theme)
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightFinancialColorScheme,
        typography = Typography,
        content = content
    )
}
