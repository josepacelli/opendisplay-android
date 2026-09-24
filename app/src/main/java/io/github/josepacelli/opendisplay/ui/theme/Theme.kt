package io.github.josepacelli.opendisplay.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF6FCF97)
private val Destructive = Color(0xFFEF4444)

private val DarkColors = darkColorScheme(
    primary = Accent,
    error = Destructive,
    background = Color(0xFF0F172A),
    surface = Color(0xFF0F172A),
)

private val LightColors = lightColorScheme(
    primary = Accent,
    error = Destructive,
)

/**
 * Deliberately minimal: this app is a fullscreen video receiver, not a
 * content-heavy UI, so there is no design-system investment here yet.
 * Adaptive layout for phone vs. tablet is CLAUDE.md's phase 9, not this.
 */
@Composable
fun OpenDisplayTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
