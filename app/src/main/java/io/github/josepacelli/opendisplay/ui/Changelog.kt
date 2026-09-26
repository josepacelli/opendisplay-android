package io.github.josepacelli.opendisplay.ui

import io.github.josepacelli.opendisplay.R

/** One released version's entry in the Settings "Changelog" tab.
 * @param version e.g. `"0.0.47"`, without the `v` prefix.
 * @param dateIso release date as `"yyyy-MM-dd"` — formatted for display with the device's
 * locale (see `formatChangelogDate` in `SettingsDialog.kt`), so no per-language date strings.
 * @param highlightsRes string-array resource with this version's condensed, translated bullet
 * points (`changelog_v*` in `strings.xml`, one array per version per locale). */
data class ChangelogEntry(val version: String, val dateIso: String, val highlightsRes: Int)

/** Every release from v0.0.37 (the oldest still relevant) to the current version, newest
 * first. Condensed from the actual GitHub release notes (`gh release view vX.X.X`) — update
 * when cutting a new release, adding a matching `changelog_v*` string-array to every
 * `strings.xml` locale file. */
val CHANGELOG_ENTRIES = listOf(
    ChangelogEntry("0.0.47", "2026-09-26", R.array.changelog_v0_0_47),
    ChangelogEntry("0.0.46", "2026-09-24", R.array.changelog_v0_0_46),
    ChangelogEntry("0.0.45", "2026-09-24", R.array.changelog_v0_0_45),
    ChangelogEntry("0.0.44", "2026-09-23", R.array.changelog_v0_0_44),
    ChangelogEntry("0.0.43", "2026-09-23", R.array.changelog_v0_0_43),
    ChangelogEntry("0.0.42", "2026-09-23", R.array.changelog_v0_0_42),
    ChangelogEntry("0.0.41", "2026-09-18", R.array.changelog_v0_0_41),
    ChangelogEntry("0.0.40", "2026-09-17", R.array.changelog_v0_0_40),
    ChangelogEntry("0.0.39", "2026-09-16", R.array.changelog_v0_0_39),
    ChangelogEntry("0.0.38", "2026-09-12", R.array.changelog_v0_0_38),
    ChangelogEntry("0.0.37", "2026-09-08", R.array.changelog_v0_0_37),
)
