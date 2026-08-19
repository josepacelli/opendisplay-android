package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
 * Grouped like the upstream iOS client's `SettingsView` (`Form` with
 * sections), adapted to what's actually true on Android: no USB / Metal
 * renderer / Local Network permission items — this app only ever listens
 * on plain TCP, so there's nothing OS-specific to toggle. Shown only while
 * disconnected; once video is flowing this app has no chrome at all.
 */
@Composable
fun SettingsDialog(receiver: PhoneReceiver, onDismiss: () -> Unit) {
    val currentName by receiver.serviceName.collectAsState()
    val connected by receiver.connected.collectAsState()
    val showNotification by receiver.showNotification.collectAsState()
    val showPerfHud by receiver.showPerfHud.collectAsState()
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
            Column(modifier = Modifier.widthIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                SettingsSection(stringResource(R.string.settings_section_status)) {
                    LabeledRow(
                        stringResource(R.string.settings_status_listening),
                        stringResource(R.string.settings_status_listening_value, PhoneReceiver.DEFAULT_PORT),
                    )
                    LabeledRow(
                        stringResource(R.string.settings_status_connection),
                        stringResource(
                            if (connected) R.string.settings_status_connection_connected
                            else R.string.settings_status_connection_waiting,
                        ),
                    )
                }

                SettingsSection(stringResource(R.string.settings_section_notification)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.settings_notification_show),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                        )
                        Switch(checked = showNotification, onCheckedChange = { receiver.setShowNotification(it) })
                    }
                }

                SettingsSection(stringResource(R.string.settings_section_perf_hud)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.settings_perf_hud_show),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                        )
                        Switch(checked = showPerfHud, onCheckedChange = { receiver.setShowPerfHud(it) })
                    }
                }

                SettingsSection(stringResource(R.string.settings_section_name)) {
                    Text(
                        text = stringResource(R.string.settings_name_label),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }

                SettingsSection(stringResource(R.string.settings_section_network)) {
                    Text(
                        text = stringResource(R.string.settings_manual_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = addressHint ?: stringResource(R.string.settings_address_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                SettingsSection(stringResource(R.string.settings_section_how_to_connect)) {
                    Text(stringResource(R.string.settings_howto_wifi), style = MaterialTheme.typography.bodySmall)
                    Text(
                        stringResource(R.string.settings_howto_rotate),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        stringResource(R.string.settings_howto_touch),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                SettingsSection(stringResource(R.string.settings_section_about), showDivider = false) {
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

@Composable
private fun SettingsSection(title: String, showDivider: Boolean = true, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.padding(top = 6.dp)) { content() }
        if (showDivider) HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
