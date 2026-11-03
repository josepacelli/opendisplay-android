package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.josepacelli.opendisplay.R
import io.github.josepacelli.opendisplay.net.PerfHudPosition
import io.github.josepacelli.opendisplay.net.PhoneReceiver
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/** From this width up, Settings switches from a tab row to a section list beside the content —
 * the same two-pane shape Android's own Settings app uses on tablets. */
private val TWO_PANE_MIN_WIDTH = 840.dp

/** Width of the section list in the two-pane layout. */
private val SECTION_LIST_WIDTH = 280.dp

/** Choices offered by [ConnectedMenuIdlePicker], in seconds. */
private val CONNECTED_MENU_IDLE_OPTIONS = listOf(2, 5, 10, 15)

/** Bar length of the longest idle-delay option; shorter ones scale down from it. */
private val IDLE_BAR_MAX_WIDTH = 72.dp

/** Aspect ratio of drawable/banner.jpg (1600x639) — keeps the "Sobre" banner uncropped. */
private const val BANNER_ASPECT_RATIO = 1600f / 639f

/** Tab indices for [SettingsDialog] — in tab-row order (Details first). */
const val SETTINGS_TAB_DETAILS = 0
const val SETTINGS_TAB_GENERAL = 1
const val SETTINGS_TAB_ABOUT = 2
const val SETTINGS_TAB_CHANGELOG = 3

/**
 * Full-screen settings, traditional Android style: a top app bar and four sections — a tab row on
 * phones, a section list beside the content on tablets (840dp+) — splitting
 * read-only reference info ("Details" — Status/Network/How to connect, previously a
 * collapsible section), actionable settings ("General", grouped under category headers), and
 * app info ("About" — version/license/links, merged back in from the standalone `AboutDialog`
 * split out in issue #48; tabs solve the "dialog got too long" problem that split was
 * addressing, so the separate dialog isn't needed anymore, issue #112), and a release history
 * ("Changelog", issue #142). Opened from [IdleContent] while disconnected, or from
 * [ConnectedMenu]'s Settings button once video is flowing (#151) — Disconnect, zoom and
 * immersive fullscreen are also reachable from that same floating menu, so the fullscreen
 * toggle exists in both places: here for setting it up front, there for flipping it mid-session.
 *
 * Note: The "Save" button in the TopAppBar has been removed. All toggles and pickers are applied
 * and saved immediately. Only the Device Name has a dedicated "Save" button next to its text field.
 *
 * Rendered as an edge-to-edge [Dialog] rather than [androidx.compose.material3.AlertDialog]
 * so it can fill the screen while still getting back-press-to-dismiss for free.
 * [Modifier.safeDrawingPadding] on the root [Column] keeps the top app bar clear of the status
 * bar — a `Dialog`'s window is forced edge-to-edge too once `targetSdk` crosses 35 (Android 15+),
 * same root cause [ReceiverScreen] already works around for the video surface (issue #144).
 *
 * @param receiver the session whose settings are shown/edited.
 * @param initialTab which tab is selected when the screen opens — one of the `SETTINGS_TAB_*` constants.
 * @param onDismiss called when the screen should close (back press/gesture).
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
    val connectedMenuEnabled by receiver.connectedMenuEnabled.collectAsState()
    val connectedMenuIdleSeconds by receiver.connectedMenuIdleSeconds.collectAsState()
    
    var draftName by remember { mutableStateOf(currentName) }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val addressHint = remember { receiver.localAddressHint() }
    val addressUnavailableMessage = remember { receiver.addressUnavailableMessage() }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val screenColor = MaterialTheme.colorScheme.background
        Surface(modifier = Modifier.fillMaxSize(), color = screenColor) {
            Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = screenColor),
                )
                val sections = listOf(
                    SETTINGS_TAB_DETAILS to stringResource(R.string.settings_section_details),
                    SETTINGS_TAB_GENERAL to stringResource(R.string.settings_tab_general),
                    SETTINGS_TAB_ABOUT to stringResource(R.string.settings_section_about),
                    SETTINGS_TAB_CHANGELOG to stringResource(R.string.settings_tab_changelog),
                )
                val content = @Composable {
                    when (selectedTab) {
                        SETTINGS_TAB_DETAILS -> DetailsTab(
                            connected = connected,
                            addressHint = addressHint,
                            addressUnavailableMessage = addressUnavailableMessage,
                        )
                        SETTINGS_TAB_GENERAL -> GeneralTab(
                            draftName = draftName,
                            onNameChange = { draftName = it },
                            currentName = currentName,
                            onSaveName = { 
                                receiver.setServiceName(it)
                                // draftName is now in sync with currentName after flow emits
                            },
                            showNotification = showNotification,
                            onToggleNotification = receiver::setShowNotification,
                            showPerfHud = showPerfHud,
                            onTogglePerfHud = receiver::setShowPerfHud,
                            perfHudPosition = perfHudPosition,
                            onPerfHudPositionChange = receiver::setPerfHudPosition,
                            immersiveFullscreen = immersiveFullscreen,
                            onToggleImmersive = receiver::setImmersiveFullscreen,
                            pipEnabled = pipEnabled,
                            onTogglePip = receiver::setPipEnabled,
                            zoomEnabled = zoomEnabled,
                            onToggleZoom = receiver::setZoomEnabled,
                            connectedMenuEnabled = connectedMenuEnabled,
                            onToggleConnectedMenu = receiver::setConnectedMenuEnabled,
                            connectedMenuIdleSeconds = connectedMenuIdleSeconds,
                            onConnectedMenuIdleSecondsChange = receiver::setConnectedMenuIdleSeconds,
                        )
                        SETTINGS_TAB_ABOUT -> AboutTab()
                        else -> ChangelogTab()
                    }
                }
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (maxWidth >= TWO_PANE_MIN_WIDTH) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.width(SECTION_LIST_WIDTH).padding(horizontal = 12.dp, vertical = 8.dp)) {
                                for ((tab, label) in sections) {
                                    NavigationDrawerItem(
                                        label = { Text(label) },
                                        selected = selectedTab == tab,
                                        onClick = { selectedTab = tab },
                                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f).fillMaxHeight()
                                    .verticalScroll(rememberScrollState()).padding(end = 24.dp, bottom = 24.dp),
                            ) { content() }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            SecondaryTabRow(selectedTabIndex = selectedTab, containerColor = screenColor) {
                                for ((tab, label) in sections) {
                                    Tab(selected = selectedTab == tab, onClick = { selectedTab = tab }, text = { Text(label) })
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            ) { content() }
                        }
                    }
                }
            }
        }
    }
}

/** "General" tab: every actionable setting, grouped under category headers with
 * traditional Android list rows (title/subtitle + trailing control).
 * @param draftName the name field's current value.
 * @param onNameChange called on every keystroke.
 * @param currentName the currently saved name, used to determine if the save button should be enabled.
 * @param onSaveName called when the save button is tapped.
 * @param showNotification current toggle state.
 * @param onToggleNotification called with the new state when the switch is flipped.
 * @param showPerfHud current toggle state.
 * @param onTogglePerfHud called with the new state when the switch is flipped.
 * @param perfHudPosition current corner the perf overlay renders in.
 * @param onPerfHudPositionChange called with the new corner when a different one is picked.
 * @param immersiveFullscreen current toggle state.
 * @param onToggleImmersive called with the new state when the switch is flipped.
 * @param pipEnabled current toggle state.
 * @param onTogglePip called with the new state when the switch is flipped.
 * @param zoomEnabled current toggle state.
 * @param onToggleZoom called with the new state when the switch is flipped.
 * @param connectedMenuEnabled current toggle state.
 * @param onToggleConnectedMenu called with the new state when the switch is flipped.
 * @param connectedMenuIdleSeconds current idle delay, in seconds.
 * @param onConnectedMenuIdleSecondsChange called with the new delay when a different option is picked. */
@Composable
private fun GeneralTab(
    draftName: String,
    onNameChange: (String) -> Unit,
    currentName: String,
    onSaveName: (String) -> Unit,
    showNotification: Boolean,
    onToggleNotification: (Boolean) -> Unit,
    showPerfHud: Boolean,
    onTogglePerfHud: (Boolean) -> Unit,
    perfHudPosition: PerfHudPosition,
    onPerfHudPositionChange: (PerfHudPosition) -> Unit,
    immersiveFullscreen: Boolean,
    onToggleImmersive: (Boolean) -> Unit,
    pipEnabled: Boolean,
    onTogglePip: (Boolean) -> Unit,
    zoomEnabled: Boolean,
    onToggleZoom: (Boolean) -> Unit,
    connectedMenuEnabled: Boolean,
    onToggleConnectedMenu: (Boolean) -> Unit,
    connectedMenuIdleSeconds: Int,
    onConnectedMenuIdleSecondsChange: (Int) -> Unit,
) {
    SettingsGroup {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = draftName,
                onValueChange = onNameChange,
                singleLine = true,
                label = { Text(stringResource(R.string.settings_name_label), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { onSaveName(draftName) },
                // Chỉ bật nút Save khi tên thực sự thay đổi và không được để trống
                enabled = draftName != currentName && draftName.isNotBlank(),
                modifier = Modifier.height(56.dp) // Chiều cao bằng với OutlinedTextField mặc định
            ) {
                Text(stringResource(R.string.settings_save))
            }
        }
        ToggleListItem(stringResource(R.string.settings_notification_show), showNotification, onToggleNotification)
    }

    SettingsCategory(stringResource(R.string.settings_section_video)) {
        ToggleListItem(stringResource(R.string.settings_immersive_show), immersiveFullscreen, onToggleImmersive)
        ToggleListItem(stringResource(R.string.settings_pip_show), pipEnabled, onTogglePip)
        ToggleListItem(stringResource(R.string.settings_zoom_pinch), zoomEnabled, onToggleZoom)
    }

    SettingsCategory(stringResource(R.string.settings_section_connected_menu)) {
        ToggleListItem(stringResource(R.string.settings_connected_menu_show), connectedMenuEnabled, onToggleConnectedMenu)
        Text(
            text = stringResource(R.string.settings_connected_menu_idle_label),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        ConnectedMenuIdlePicker(
            connectedMenuIdleSeconds,
            onConnectedMenuIdleSecondsChange,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        )
    }

    SettingsCategory(stringResource(R.string.settings_section_perf_hud)) {
        ToggleListItem(stringResource(R.string.settings_perf_hud_show), showPerfHud, onTogglePerfHud)
        PerfHudPositionPicker(perfHudPosition, onPerfHudPositionChange, modifier = Modifier.padding(16.dp))
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
            Spacer(modifier = Modifier.height(12.dp))
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
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    SettingsCategory(stringResource(R.string.settings_section_how_to_connect)) {
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

/** "About" tab: app icon, tagline, version/license, a link to this app's Google Play page (where
 * updates show up, #208), and links to this client's repo and the Mac sender's site. Merged back
 * into [SettingsDialog] as a tab (issue #112) — was the standalone `AboutDialog` split out in
 * issue #48 to keep the (then single-column) dialog from getting too long; tabs make that no
 * longer a concern. */
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            uriHandler.openUri("https://play.google.com/store/apps/details?id=${context.packageName}")
        }) {
            Text(stringResource(R.string.update_check_button))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {
                uriHandler.openUri("https://github.com/josepacelli/opendisplay-android")
            }) { Text(stringResource(R.string.about_github)) }
            OutlinedButton(onClick = { uriHandler.openUri("https://opendisplay.app/") }) {
                Text(stringResource(R.string.about_mac_app))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/** "Changelog" tab: the latest release, then everything older rolled into one range entry, each
 * as a version badge + date header followed by bullet highlights. Content lives in
 * [CHANGELOG_ENTRIES]/`changelog_v*` string-arrays, translated per locale like the rest of
 * the app. */
@Composable
private fun ChangelogTab() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
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
    val highlights = stringArrayResource(entry.highlightsRes)
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = "v${entry.version}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                if (entry.dateIso.isNotBlank()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatChangelogDate(entry.dateIso),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            for (highlight in highlights) {
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

/** Formats a `"yyyy-MM-dd"` release date for display in the device's current locale, so
 * [CHANGELOG_ENTRIES] doesn't need a translated date string per language.
 * @param dateIso the release date as `"yyyy-MM-dd"`.
 * @return a locale-formatted medium date, or [dateIso] unchanged if it doesn't parse. */
private fun formatChangelogDate(dateIso: String): String {
    val parsed = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateIso) }.getOrNull() ?: return dateIso
    return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(parsed)
}

/** Corner picker for the performance overlay, drawn as a small map of the screen: tap the
 * corner you want the readout in. Each corner is a 48dp radio target; the chosen one shows the
 * readout's shape filled in, and its name is spelled out underneath.
 * @param selected the corner currently in effect.
 * @param onSelect called with the newly picked corner.
 * @param modifier applied to the picker's outer column. */
@Composable
private fun PerfHudPositionPicker(
    selected: PerfHudPosition,
    onSelect: (PerfHudPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        Triple(PerfHudPosition.TOP_START, Alignment.TopStart, stringResource(R.string.settings_perf_hud_position_top_start)),
        Triple(PerfHudPosition.TOP_END, Alignment.TopEnd, stringResource(R.string.settings_perf_hud_position_top_end)),
        Triple(PerfHudPosition.BOTTOM_START, Alignment.BottomStart, stringResource(R.string.settings_perf_hud_position_bottom_start)),
        Triple(PerfHudPosition.BOTTOM_END, Alignment.BottomEnd, stringResource(R.string.settings_perf_hud_position_bottom_end)),
    )
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp).fillMaxWidth().aspectRatio(16f / 10f)
                .border(2.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                .selectableGroup()
                .padding(4.dp),
        ) {
            for ((position, alignment, label) in options) {
                val isSelected = selected == position
                Box(
                    contentAlignment = alignment,
                    modifier = Modifier
                        .align(alignment)
                        .size(width = 72.dp, height = 48.dp)
                        .clip(MaterialTheme.shapes.small)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(position) })
                        .semantics { contentDescription = label }
                        .padding(8.dp),
                ) {
                    Box(
                        modifier = if (isSelected) {
                            Modifier.size(width = 40.dp, height = 14.dp)
                                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall)
                        } else {
                            Modifier.size(10.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = options.first { it.first == selected }.third,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Fixed set of idle-delay options for [ConnectedMenu] — how long the video must sit untouched
 * before it reappears (issue #151). Each option shows its duration as a bar whose length is
 * proportional to the seconds, so the choice reads as "how long" at a glance.
 * @param selected the delay currently in effect, in seconds.
 * @param onSelect called with the newly picked delay.
 * @param modifier applied to the picker's row. */
@Composable
private fun ConnectedMenuIdlePicker(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val longest = CONNECTED_MENU_IDLE_OPTIONS.max()
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier.fillMaxWidth().selectableGroup(),
    ) {
        for (seconds in CONNECTED_MENU_IDLE_OPTIONS) {
            val isSelected = selected == seconds
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(seconds) }),
            ) {
                Column(modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = stringResource(R.string.settings_connected_menu_idle_seconds, seconds),
                        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(width = IDLE_BAR_MAX_WIDTH * seconds / longest, height = 4.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

/** A titled group of rows: a small header above a rounded surface holding the rows — the
 * grouped-list shape current Android settings use.
 * @param title category heading.
 * @param content the category's rows. */
@Composable
private fun SettingsCategory(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        SettingsGroupSurface(content)
    }
}

/** The rounded surface a set of related rows sits on — [SettingsCategory] without a header.
 * @param content the group's rows. */
@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(top = 24.dp)) { SettingsGroupSurface(content) }
}

/** The rounded surface itself, shared by [SettingsCategory] and [SettingsGroup].
 * @param content the group's rows. */
@Composable
private fun SettingsGroupSurface(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
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
        headlineContent = { Text(label, style = MaterialTheme.typography.bodyLarge) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
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
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}