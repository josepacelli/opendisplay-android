package io.github.josepacelli.opendisplay.ui.theme

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

/** Container alpha for [io.github.josepacelli.opendisplay.ui.SettingsDialog] and
 * [io.github.josepacelli.opendisplay.ui.AboutDialog] — echoes the landing page's frosted-glass
 * header without an actual blur (issue #109: real backdrop blur isn't feasible here). Applied to
 * `colorScheme.surfaceContainerHigh`, not `surface` — [DarkColors] pins `surface` to
 * [Color.Black], same as the app's own background, so alpha over it reads as solid black with
 * no visible glass; `surfaceContainerHigh` keeps the Material3 default dark-grey tone (never
 * overridden here), giving the translucency something to actually show against. */
const val DialogContainerAlpha = 0.85f

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
