package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.josepacelli.opendisplay.R
import io.github.josepacelli.opendisplay.net.PerfHudPosition
import io.github.josepacelli.opendisplay.net.PhoneReceiver

private val WIDE_LAYOUT_MIN_WIDTH = 600.dp

/** Aspect ratio of drawable/banner.jpg (1600x639) — keeps the "Sobre" banner uncropped. */
private const val BANNER_ASPECT_RATIO = 1600f / 639f

/** Tab indices for [SettingsDialog] — in tab-row order (Details first). */
const val SETTINGS_TAB_DETAILS = 0
const val SETTINGS_TAB_GENERAL = 1
const val SETTINGS_TAB_ABOUT = 2
const val SETTINGS_TAB_CHANGELOG = 3

/**
 * Full-screen settings, traditional Android style: a top app bar and a tab row splitting
 * read-only reference info ("Details" — Status/Network/How to connect, previously a
 * collapsible section), actionable settings ("General", grouped under category headers), and
 * app info ("About" — version/license/links, merged back in from the standalone `AboutDialog`
 * split out in issue #48; tabs solve the "dialog got too long" problem that split was
 * addressing, so the separate dialog isn't needed anymore, issue #112), and a release history
 * ("Changelog", issue #142). Shown only while disconnected; once video is flowing this app has
 * no chrome at all.
 *
 * Rendered as an edge-to-edge [Dialog] rather than [androidx.compose.material3.AlertDialog]
 * so it can fill the screen while still getting back-press-to-dismiss for free.
 *
 * @param receiver the session whose settings are shown/edited.
 * @param initialTab which tab is selected when the screen opens — one of the `SETTINGS_TAB_*` constants.
 * @param onDismiss called when the screen should close (back press/gesture, or Save).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(receiver: PhoneReceiver, initialTab: Int = SETTINGS_TAB_GENERAL, onDismiss: () -> Unit) {
    val currentName by receiver.serviceName.collectAsState()
    val connected by receiver.connected.collectAsState()
    val showNotification by receiver.showNotification.collectAsState()
    val showPerfHud by receiver.showPerfHud.collectAsState()
    val perfHudPosition by receiver.perfHudPosition.collectAsState()
    val immersiveFullscreen by receiver.immersiveFullscreen.collectAsState()
    val pipEnabled by receiver.pipEnabled.collectAsState()
    val zoomEnabled by receiver.zoomEnabled.collectAsState()
    var draftName by remember { mutableStateOf(currentName) }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val addressHint = remember { receiver.localAddressHint() }
    val addressUnavailableMessage = remember { receiver.addressUnavailableMessage() }
    val wide = LocalConfiguration.current.screenWidthDp.dp >= WIDE_LAYOUT_MIN_WIDTH

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val screenColor = MaterialTheme.colorScheme.surfaceContainerLow
        Surface(modifier = Modifier.fillMaxSize(), color = screenColor) {
            Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                TopAppBar(
                    title = { Text(stringResource(R.string.settings_title)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.settings_cancel),
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                receiver.setServiceName(draftName)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(end = 12.dp),
                        ) { Text(stringResource(R.string.settings_save)) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = screenColor),
                )
                SecondaryTabRow(selectedTabIndex = selectedTab, containerColor = screenColor) {
                    Tab(
                        selected = selectedTab == SETTINGS_TAB_DETAILS,
                        onClick = { selectedTab = SETTINGS_TAB_DETAILS },
                        text = { Text(stringResource(R.string.settings_section_details)) },
                    )
                    Tab(
                        selected = selectedTab == SETTINGS_TAB_GENERAL,
                        onClick = { selectedTab = SETTINGS_TAB_GENERAL },
                        text = { Text(stringResource(R.string.settings_tab_general)) },
                    )
                    Tab(
                        selected = selectedTab == SETTINGS_TAB_ABOUT,
                        onClick = { selectedTab = SETTINGS_TAB_ABOUT },
                        text = { Text(stringResource(R.string.settings_section_about)) },
                    )
                    Tab(
                        selected = selectedTab == SETTINGS_TAB_CHANGELOG,
                        onClick = { selectedTab = SETTINGS_TAB_CHANGELOG },
                        text = { Text(stringResource(R.string.settings_tab_changelog)) },
                    )
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
                ) {
                    when (selectedTab) {
                        SETTINGS_TAB_DETAILS -> DetailsTab(
                            connected = connected,
                            addressHint = addressHint,
                            addressUnavailableMessage = addressUnavailableMessage,
                        )
                        SETTINGS_TAB_GENERAL -> GeneralTab(
                            draftName = draftName,
                            onNameChange = { draftName = it },
                            showNotification = showNotification,
                            onToggleNotification = receiver::setShowNotification,
                            showPerfHud = showPerfHud,
                            onTogglePerfHud = receiver::setShowPerfHud,
                            perfHudPosition = perfHudPosition,
                            onPerfHudPositionChange = receiver::setPerfHudPosition,
                            wide = wide,
                            immersiveFullscreen = immersiveFullscreen,
                            onToggleImmersive = receiver::setImmersiveFullscreen,
                            pipEnabled = pipEnabled,
                            onTogglePip = receiver::setPipEnabled,
                            zoomEnabled = zoomEnabled,
                            onToggleZoom = receiver::setZoomEnabled,
                        )
                        SETTINGS_TAB_ABOUT -> AboutTab()
                        else -> ChangelogTab()
                    }
                }
            }
        }
    }
}

/** "General" tab: every actionable setting, grouped under category headers with
 * traditional Android list rows (title/subtitle + trailing control).
 * @param draftName the name field's current (unsaved) value.
 * @param onNameChange called on every keystroke.
 * @param showNotification current toggle state.
 * @param onToggleNotification called with the new state when the switch is flipped.
 * @param showPerfHud current toggle state.
 * @param onTogglePerfHud called with the new state when the switch is flipped.
 * @param perfHudPosition current corner the perf overlay renders in.
 * @param onPerfHudPositionChange called with the new corner when a different one is picked.
 * @param wide whether there's room to lay the four corner options out in one row instead of two.
 * @param immersiveFullscreen current toggle state.
 * @param onToggleImmersive called with the new state when the switch is flipped.
 * @param pipEnabled current toggle state.
 * @param onTogglePip called with the new state when the switch is flipped.
 * @param zoomEnabled current toggle state.
 * @param onToggleZoom called with the new state when the switch is flipped. */
@Composable
private fun GeneralTab(
    draftName: String,
    onNameChange: (String) -> Unit,
    showNotification: Boolean,
    onToggleNotification: (Boolean) -> Unit,
    showPerfHud: Boolean,
    onTogglePerfHud: (Boolean) -> Unit,
    perfHudPosition: PerfHudPosition,
    onPerfHudPositionChange: (PerfHudPosition) -> Unit,
    wide: Boolean,
    immersiveFullscreen: Boolean,
    onToggleImmersive: (Boolean) -> Unit,
    pipEnabled: Boolean,
    onTogglePip: (Boolean) -> Unit,
    zoomEnabled: Boolean,
    onToggleZoom: (Boolean) -> Unit,
) {
    SettingsCategory(stringResource(R.string.settings_section_name)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_name_label),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            OutlinedTextField(value = draftName, onValueChange = onNameChange, singleLine = true, modifier = Modifier.weight(1f))
        }
    }

    SettingsCategory(stringResource(R.string.settings_section_notification)) {
        ToggleListItem(stringResource(R.string.settings_notification_show), showNotification, onToggleNotification)
    }

    SettingsCategory(stringResource(R.string.settings_section_immersive)) {
        ToggleListItem(stringResource(R.string.settings_immersive_show), immersiveFullscreen, onToggleImmersive)
    }

    SettingsCategory(stringResource(R.string.settings_section_perf_hud)) {
        ToggleListItem(stringResource(R.string.settings_perf_hud_show), showPerfHud, onTogglePerfHud)
        Spacer(modifier = Modifier.height(8.dp))
        PerfHudPositionPicker(perfHudPosition, onPerfHudPositionChange, wide, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))
    }

    SettingsCategory(stringResource(R.string.settings_section_pip)) {
        ToggleListItem(stringResource(R.string.settings_pip_show), pipEnabled, onTogglePip)
    }

    SettingsCategory(stringResource(R.string.settings_section_video), showDivider = false) {
        ToggleListItem(stringResource(R.string.settings_zoom_pinch), zoomEnabled, onToggleZoom)
    }
}

/** "Details" tab: read-only reference info — status, network address, and static
 * connection instructions. Was a collapsible section inside a single dialog
 * (issue #48); now its own always-expanded tab (issue #112).
 * @param connected whether a Mac is currently connected.
 * @param addressHint this device's local `ip:port`, or `null` if unavailable.
 * @param addressUnavailableMessage text shown instead of [addressHint] when it's `null`. */
@Composable
private fun DetailsTab(connected: Boolean, addressHint: String?, addressUnavailableMessage: String) {
    SettingsCategory(stringResource(R.string.settings_section_status)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            LabeledRow(
                stringResource(R.string.settings_status_listening),
                stringResource(R.string.settings_status_listening_value, PhoneReceiver.DEFAULT_PORT),
            )
            Spacer(modifier = Modifier.height(8.dp))
            LabeledRow(
                stringResource(R.string.settings_status_connection),
                stringResource(
                    if (connected) R.string.settings_status_connection_connected
                    else R.string.settings_status_connection_waiting,
                ),
            )
        }
    }

    SettingsCategory(stringResource(R.string.settings_section_network)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(text = stringResource(R.string.settings_manual_hint), style = MaterialTheme.typography.bodySmall)
            Text(
                text = addressHint ?: addressUnavailableMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }

    SettingsCategory(stringResource(R.string.settings_section_how_to_connect), showDivider = false) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
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
}

/** "About" tab: app icon, tagline, version/license, and links to this client's repo and the
 * Mac sender's site. Merged back into [SettingsDialog] as a tab (issue #112) — was the
 * standalone `AboutDialog` split out in issue #48 to keep the (then single-column) dialog from
 * getting too long; tabs make that no longer a concern. */
@Composable
private fun AboutTab() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val versionName = remember {
        @Suppress("DEPRECATION")
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.banner),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                .aspectRatio(BANNER_ASPECT_RATIO)
                .clip(RoundedCornerShape(12.dp)),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(R.drawable.app_icon_display),
            contentDescription = null,
            modifier = Modifier.size(88.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.about_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = versionName?.let { stringResource(R.string.about_version, it) }
                ?: stringResource(R.string.about_version_unknown),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {
                uriHandler.openUri("https://github.com/josepacelli/opendisplay-android")
            }) { Text(stringResource(R.string.about_github)) }
            OutlinedButton(onClick = { uriHandler.openUri("https://opendisplay.app/") }) {
                Text(stringResource(R.string.about_mac_app))
            }
        }
    }
}

/** "Changelog" tab: every release from v0.0.36 to the current version, newest first, as a
 * version badge + date header followed by bullet highlights. Content lives in [CHANGELOG_ENTRIES]
 * and stays in English regardless of device locale (see that file's doc comment for why). */
@Composable
private fun ChangelogTab() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        CHANGELOG_ENTRIES.forEachIndexed { index, entry ->
            ChangelogEntryCard(entry)
            if (index != CHANGELOG_ENTRIES.lastIndex) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/** One release's card inside [ChangelogTab]: version badge + date, then its bullet highlights.
 * @param entry the release to render. */
@Composable
private fun ChangelogEntryCard(entry: ChangelogEntry) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = "v${entry.version}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            for (highlight in entry.highlights) {
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(text = highlight, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/** Four-corner picker for the performance overlay — one row of four [FilterChip]s when
 * there's room ([wide]), two rows of two otherwise, laid out the same way the corners
 * appear on screen (top row above bottom row).
 * @param selected the corner currently in effect.
 * @param onSelect called with the newly picked corner.
 * @param wide lay all four chips out in a single row instead of two.
 * @param modifier applied to the picker's outer column. */
@Composable
private fun PerfHudPositionPicker(
    selected: PerfHudPosition,
    onSelect: (PerfHudPosition) -> Unit,
    wide: Boolean,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        PerfHudPosition.TOP_START to stringResource(R.string.settings_perf_hud_position_top_start),
        PerfHudPosition.TOP_END to stringResource(R.string.settings_perf_hud_position_top_end),
        PerfHudPosition.BOTTOM_START to stringResource(R.string.settings_perf_hud_position_bottom_start),
        PerfHudPosition.BOTTOM_END to stringResource(R.string.settings_perf_hud_position_bottom_end),
    )
    val rows = if (wide) listOf(options) else options.chunked(2)
    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = (if (index == 0) Modifier else Modifier.padding(top = 6.dp)).fillMaxWidth(),
            ) {
                for ((position, label) in row) {
                    PositionChip(label, selected == position) { onSelect(position) }
                }
            }
        }
    }
}

/** One selectable corner option inside [PerfHudPositionPicker].
 * @param label the corner's display name.
 * @param selected whether this is the currently active corner.
 * @param onClick called when tapped. */
@Composable
private fun PositionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
    )
}

/** A titled group of rows, the traditional Android "preference category" shape: a small
 * primary-colored header followed by its rows, with an optional trailing divider.
 * @param title category heading.
 * @param showDivider whether to draw a divider below the category.
 * @param content the category's rows. */
@Composable
private fun SettingsCategory(title: String, showDivider: Boolean = true, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
        if (showDivider) HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

/** A full-width, tap-anywhere-to-toggle row with a trailing [Switch] — the standard shape
 * of every boolean setting in the traditional Android Settings app.
 * @param label what the switch controls.
 * @param checked its current state.
 * @param onCheckedChange called with the new state when flipped (by tapping the row or the switch). */
@Composable
private fun ToggleListItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onCheckedChange(!checked) },
    )
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
