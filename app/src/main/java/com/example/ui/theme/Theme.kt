package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// =========================================================================
// SellersHub & Messenger.ai Theme Specification
// Clean, high-contrast, data-forward e-commerce dashboard aesthetic
// =========================================================================

// Dark Theme: Sleek deep navy/slate container with crisp bright contrasts
private val DarkColorScheme = darkColorScheme(
    primary = SellersHubRoyalBlueDark,
    onPrimary = Color.White,
    primaryContainer = SellersHubNavy800,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = MessengerAiTeal,
    onSecondary = Color.White,
    secondaryContainer = SellersHubNavy700,
    onSecondaryContainer = Color.White,
    tertiary = WarningAmber,
    onTertiary = Color.Black,
    background = SellersHubNavy950,
    onBackground = Color(0xFFF8FAFC),
    surface = SellersHubNavy900,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = SellersHubNavy800,
    onSurfaceVariant = SellersHubSlate400,
    outline = SellersHubNavy700,
    outlineVariant = Color(0xFF334155),
    error = DangerRose,
    onError = Color.White
)

// Light Theme: Default SellersHub style (as requested: Light Mode default)
// Crisp off-white canvas (#F8FAFC), pure white card tiles with sharp borders (#E2E8F0),
// Deep navy text (#0F172A), and vibrant Royal Indigo Blue (#2563EB) accents.
private val LightColorScheme = lightColorScheme(
    primary = SellersHubRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF), // Soft Blue tint
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = MessengerAiTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0FDFA),
    onSecondaryContainer = Color(0xFF0F766E),
    tertiary = MessengerAiPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEEF2FF),
    onTertiaryContainer = Color(0xFF4338CA),
    background = SellersHubSlate50, // Crisp #F8FAFC canvas
    onBackground = SellersHubNavy900, // Rich #0F172A text
    surface = Color.White,
    onSurface = SellersHubNavy900,
    surfaceVariant = SellersHubSlate100, // Soft #F1F5F9 pills and inactive states
    onSurfaceVariant = SellersHubNavy600,
    outline = SellersHubSlate200, // Clean subtle card borders #E2E8F0
    outlineVariant = Color(0xFFE2E8F0),
    error = DangerRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
