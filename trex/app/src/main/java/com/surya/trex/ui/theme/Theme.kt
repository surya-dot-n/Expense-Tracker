package com.surya.trex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


// ============================================================
// LIGHT THEME
// ============================================================

private val LightColorScheme = lightColorScheme(

    // Primary
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E3A8A),

    // Secondary
    secondary = Color(0xFF475569),
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),

    // Tertiary
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color.White,

    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF581C87),

    // Background
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF0F172A),

    // Surface
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),

    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),

    // Borders
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),

    // Error
    error = Color(0xFFDC2626),
    onError = Color.White,

    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)


// ============================================================
// DARK THEME
// ============================================================

private val DarkColorScheme = darkColorScheme(

    // Primary
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1220),

    primaryContainer = Color(0xFF1D4ED8),
    onPrimaryContainer = Color(0xFFDBEAFE),

    // Secondary
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0B1220),

    secondaryContainer = Color(0xFF334155),
    onSecondaryContainer = Color(0xFFE2E8F0),

    // Tertiary
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF1E1B4B),

    tertiaryContainer = Color(0xFF5B21B6),
    onTertiaryContainer = Color(0xFFF3E8FF),

    // Background
    background = Color(0xFF080D16),
    onBackground = Color(0xFFF8FAFC),

    // Surface
    surface = Color(0xFF101826),
    onSurface = Color(0xFFF8FAFC),

    surfaceVariant = Color(0xFF182333),
    onSurfaceVariant = Color(0xFFCBD5E1),

    // Borders
    outline = Color(0xFF526176),
    outlineVariant = Color(0xFF334155),

    // Error
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF450A0A),

    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA)
)


// ============================================================
// TREX THEME
// ============================================================

@Composable
fun TrexTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {

    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}