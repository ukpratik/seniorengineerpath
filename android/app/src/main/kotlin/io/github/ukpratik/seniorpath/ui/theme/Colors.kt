package io.github.ukpratik.seniorpath.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// The site's tokens (src/styles/tokens.css) mapped to Material 3 roles. Keep both in step.

val LightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFECEEFF),
    onPrimaryContainer = Color(0xFF3730A3),
    secondary = Color(0xFF475569),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1E7EF),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFF047857),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE2F5EC),
    onTertiaryContainer = Color(0xFF047857),
    error = Color(0xFFB91C1C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFCEBEB),
    onErrorContainer = Color(0xFFB91C1C),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEEF2F7),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFEEF2F7),
    surfaceContainerHigh = Color(0xFFE1E7EF),
    surfaceContainerHighest = Color(0xFFE1E7EF),
    outline = Color(0xFF7B8799),
    outlineVariant = Color(0xFFD5DCE6),
)

val DarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF8F88FF),
    onPrimary = Color(0xFF0B1020),
    primaryContainer = Color(0xFF1F2250),
    onPrimaryContainer = Color(0xFFC9C5FF),
    secondary = Color(0xFFAAB5C8),
    onSecondary = Color(0xFF0B1020),
    secondaryContainer = Color(0xFF26314A),
    onSecondaryContainer = Color(0xFFE7ECF5),
    tertiary = Color(0xFF4ADE9A),
    onTertiary = Color(0xFF0B1020),
    tertiaryContainer = Color(0xFF0E2A21),
    onTertiaryContainer = Color(0xFF4ADE9A),
    error = Color(0xFFF87171),
    onError = Color(0xFF0B1020),
    errorContainer = Color(0xFF2D1518),
    onErrorContainer = Color(0xFFF87171),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE7ECF5),
    surface = Color(0xFF131A2B),
    onSurface = Color(0xFFE7ECF5),
    surfaceVariant = Color(0xFF1A2336),
    onSurfaceVariant = Color(0xFFAAB5C8),
    surfaceContainerLowest = Color(0xFF0B1020),
    surfaceContainerLow = Color(0xFF131A2B),
    surfaceContainer = Color(0xFF1A2336),
    surfaceContainerHigh = Color(0xFF26314A),
    surfaceContainerHighest = Color(0xFF26314A),
    outline = Color(0xFF5D6B87),
    outlineVariant = Color(0xFF2A3550),
)

/** Colours Material has no role for: the eight track ("metro line") colours, decorative only. */
@Immutable
data class SpColors(val tracks: List<Color>) {
    /** Track [index] is 1-based, as in the manifest. */
    fun track(index: Int): Color = tracks[(index - 1).mod(tracks.size)]
}

val LightSp = SpColors(
    listOf(0xFF2563EB, 0xFF0E7490, 0xFF7C3AED, 0xFFC2410C, 0xFFBE123C, 0xFF15803D, 0xFFA16207, 0xFFC026D3).map(::Color),
)
val DarkSp = SpColors(
    listOf(0xFF60A5FA, 0xFF22D3EE, 0xFFA78BFA, 0xFFFB923C, 0xFFFB7185, 0xFF4ADE80, 0xFFFACC15, 0xFFE879F9).map(::Color),
)

val LocalSpColors = staticCompositionLocalOf { LightSp }
