package com.peetzweg.opensidecar.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF6FCF97)

private val DarkColors = darkColorScheme(
    primary = Accent,
    background = Color.Black,
    surface = Color.Black,
)

private val LightColors = lightColorScheme(
    primary = Accent,
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
