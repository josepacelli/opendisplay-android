package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.josepacelli.opendisplay.R
import io.github.josepacelli.opendisplay.net.PhoneReceiver

private val WIDE_LAYOUT_MIN_WIDTH = 600.dp
private val NARROW_DIALOG_MAX_WIDTH = 420.dp
private val WIDE_DIALOG_MAX_WIDTH = 640.dp

/**
 * Grouped like the upstream iOS client's `SettingsView` (`Form` with
 * sections), adapted to what's actually true on Android: no USB / Metal
 * renderer / Local Network permission items — this app only ever listens
 * on plain TCP, so there's nothing OS-specific to toggle. Shown only while
 * disconnected; once video is flowing this app has no chrome at all.
 *
 * Reference-only sections (Status/Network/How to connect) collapse behind
 * [DetailsSection] so the dialog stays short — see RATIONALE.md (issue
 * #48). On a wide window ([WIDE_LAYOUT_MIN_WIDTH]+) the actionable settings
 * lay out in two columns instead of stacking.
 *
 * @param receiver the session whose settings are shown/edited.
 * @param onDismiss called when the dialog should close (Cancel, Save, or scrim tap).
 */
@Composable
fun SettingsDialog(receiver: PhoneReceiver, onDismiss: () -> Unit) {
    val currentName by receiver.serviceName.collectAsState()
    val connected by receiver.connected.collectAsState()
    val showNotification by receiver.showNotification.collectAsState()
    val showPerfHud by receiver.showPerfHud.collectAsState()
    val immersiveFullscreen by receiver.immersiveFullscreen.collectAsState()
    var draftName by remember { mutableStateOf(currentName) }
    var detailsExpanded by remember { mutableStateOf(false) }
    val addressHint = remember { receiver.localAddressHint() }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val versionName = remember {
        @Suppress("DEPRECATION")
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }
    val wide = LocalConfiguration.current.screenWidthDp.dp >= WIDE_LAYOUT_MIN_WIDTH

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(
                modifier = Modifier
                    .widthIn(max = if (wide) WIDE_DIALOG_MAX_WIDTH else NARROW_DIALOG_MAX_WIDTH)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (wide) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            NotificationSection(showNotification, receiver::setShowNotification)
                            PerfHudSection(showPerfHud, receiver::setShowPerfHud)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            ImmersiveSection(immersiveFullscreen, receiver::setImmersiveFullscreen)
                            NameSection(draftName) { draftName = it }
                        }
                    }
                } else {
                    NotificationSection(showNotification, receiver::setShowNotification)
                    PerfHudSection(showPerfHud, receiver::setShowPerfHud)
                    ImmersiveSection(immersiveFullscreen, receiver::setImmersiveFullscreen)
                    NameSection(draftName) { draftName = it }
                }

                DetailsSection(expanded = detailsExpanded, onToggleExpanded = { detailsExpanded = !detailsExpanded }) {
                    StatusSection(connected)
                    NetworkSection(addressHint)
                    HowToConnectSection()
                }

                AboutSection(versionName) { uriHandler.openUri("https://github.com/josepacelli/opendisplay-android") }
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

/** Connection status — read-only, lives inside [DetailsSection].
 * @param connected whether a Mac is currently connected. */
@Composable
private fun StatusSection(connected: Boolean) {
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
}

/** @param showNotification current toggle state.
 * @param onToggle called with the new state when the switch is flipped. */
@Composable
private fun NotificationSection(showNotification: Boolean, onToggle: (Boolean) -> Unit) {
    SettingsSection(stringResource(R.string.settings_section_notification)) {
        ToggleRow(stringResource(R.string.settings_notification_show), showNotification, onToggle)
    }
}

/** @param showPerfHud current toggle state.
 * @param onToggle called with the new state when the switch is flipped. */
@Composable
private fun PerfHudSection(showPerfHud: Boolean, onToggle: (Boolean) -> Unit) {
    SettingsSection(stringResource(R.string.settings_section_perf_hud)) {
        ToggleRow(stringResource(R.string.settings_perf_hud_show), showPerfHud, onToggle)
    }
}

/** @param immersiveFullscreen current toggle state.
 * @param onToggle called with the new state when the switch is flipped. */
@Composable
private fun ImmersiveSection(immersiveFullscreen: Boolean, onToggle: (Boolean) -> Unit) {
    SettingsSection(stringResource(R.string.settings_section_immersive)) {
        ToggleRow(stringResource(R.string.settings_immersive_show), immersiveFullscreen, onToggle)
    }
}

/** @param draftName the name field's current (unsaved) value.
 * @param onNameChange called on every keystroke. */
@Composable
private fun NameSection(draftName: String, onNameChange: (String) -> Unit) {
    SettingsSection(stringResource(R.string.settings_section_name)) {
        Text(text = stringResource(R.string.settings_name_label), style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = draftName,
            onValueChange = onNameChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
    }
}

/** @param addressHint this device's local `ip:port`, or `null` if unavailable — lives inside [DetailsSection]. */
@Composable
private fun NetworkSection(addressHint: String?) {
    SettingsSection(stringResource(R.string.settings_section_network)) {
        Text(text = stringResource(R.string.settings_manual_hint), style = MaterialTheme.typography.bodySmall)
        Text(
            text = addressHint ?: stringResource(R.string.settings_address_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Static connection instructions — lives inside [DetailsSection]. */
@Composable
private fun HowToConnectSection() {
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
}

/** @param versionName this build's version name, or `null` if it couldn't be read.
 * @param onGithubClick called when the GitHub link is tapped. */
@Composable
private fun AboutSection(versionName: String?, onGithubClick: () -> Unit) {
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
            TextButton(onClick = onGithubClick) { Text(stringResource(R.string.about_github)) }
        }
    }
}

/** A titled group of rows inside [SettingsDialog], with an optional trailing divider.
 * @param title section heading.
 * @param showDivider whether to draw a divider below the section.
 * @param content the section's rows. */
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

/** A label + [Switch] row, the shared shape of every boolean setting in this dialog.
 * @param label what the switch controls.
 * @param checked its current state.
 * @param onCheckedChange called with the new state when flipped. */
@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** A label/value pair on one row, right-aligned value.
 * @param label the row's left-aligned label.
 * @param value the row's right-aligned value. */
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

/** Collapsed-by-default group for reference-only settings (Status/Network/How to
 * connect) — nothing in here is editable, so it doesn't need to always be visible.
 * @param expanded whether the group's content is currently shown.
 * @param onToggleExpanded called when the header is tapped.
 * @param content the collapsible sections. */
@Composable
private fun DetailsSection(expanded: Boolean, onToggleExpanded: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpanded),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_section_details),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (expanded) "▾" else "▸",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (expanded) {
            Column(modifier = Modifier.padding(top = 6.dp)) { content() }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}
