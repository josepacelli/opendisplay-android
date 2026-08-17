package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.josepacelli.opendisplay.R
import io.github.josepacelli.opendisplay.net.PhoneReceiver

/**
 * The only "settings" this receiver has: the mDNS name the Mac's WiFi picker
 * shows, and — for when mDNS doesn't reach it (some networks block
 * multicast) — this device's own IP address to type into the Mac app's host
 * override by hand. Shown only while disconnected; once video is flowing
 * this app has no chrome at all.
 */
@Composable
fun SettingsDialog(receiver: PhoneReceiver, onDismiss: () -> Unit) {
    val currentName by receiver.serviceName.collectAsState()
    var draftName by remember { mutableStateOf(currentName) }
    val addressHint = remember { receiver.localAddressHint() }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val versionName = remember {
        @Suppress("DEPRECATION")
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.settings_name_label),
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                )
                Text(
                    text = stringResource(R.string.settings_manual_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = addressHint ?: stringResource(R.string.settings_address_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = versionName?.let { stringResource(R.string.about_version, it) }
                            ?: stringResource(R.string.about_version_unknown),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    TextButton(onClick = {
                        uriHandler.openUri("https://github.com/josepacelli/opendisplay-android")
                    }) { Text(stringResource(R.string.about_github)) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                receiver.setServiceName(draftName)
                onDismiss()
            }) { Text(stringResource(R.string.settings_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
        },
    )
}
