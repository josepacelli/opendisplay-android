package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.josepacelli.opendisplay.R

/**
 * Standalone About screen — version, license, and links to this client's
 * repo and the Mac sender's site. Split out of [SettingsDialog] (issue #48
 * follow-up): version/license/links aren't a setting, and folding them into
 * that dialog only made it longer for no reason.
 *
 * @param onDismiss called when the dialog should close.
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val versionName = remember {
        @Suppress("DEPRECATION")
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_section_about)) },
        text = {
            Column(modifier = Modifier.widthIn(max = 420.dp)) {
                Text(
                    text = versionName?.let { stringResource(R.string.about_version, it) }
                        ?: stringResource(R.string.about_version_unknown),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        uriHandler.openUri("https://github.com/josepacelli/opendisplay-android")
                    }) { Text(stringResource(R.string.about_github)) }
                    TextButton(onClick = { uriHandler.openUri("https://opendisplay.app/") }) {
                        Text(stringResource(R.string.about_mac_app))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.about_close)) }
        },
    )
}
