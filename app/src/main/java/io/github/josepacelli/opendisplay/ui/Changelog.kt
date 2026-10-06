package io.github.josepacelli.opendisplay.ui

import io.github.josepacelli.opendisplay.R

/** One entry in the Settings "Changelog" tab — either a single release or a rolled-up range of
 * older ones (see [CHANGELOG_OLDER_RANGE_RES]).
 * @param version e.g. `"0.0.48"` for a single release, or `"0.0.37 – 0.0.49"` for a range —
 * without the `v` prefix, rendered as `"v$version"`.
 * @param dateIso release date as `"yyyy-MM-dd"` — formatted for display with the device's
 * locale (see `formatChangelogDate` in `SettingsDialog.kt`), so no per-language date strings.
 * Blank for a range entry, which shows no date.
 * @param highlightsRes string-array resource with this entry's condensed, translated bullet
 * points (`changelog_v*`/[CHANGELOG_OLDER_RANGE_RES] in `strings.xml`). */
data class ChangelogEntry(val version: String, val dateIso: String, val highlightsRes: Int)

/** Resource name backing the rolled-up older-versions entry — kept stable across releases since
 * its range grows each time. */
private const val CHANGELOG_OLDER_RANGE_RES = R.array.changelog_older_versions

/** Only the latest release gets its own dated entry with a full bullet list. Everything before
 * it collapses into one entry spanning `"v0.0.37 – v0.0.<latest - 1>"`, all bullets
 * concatenated newest-first into [CHANGELOG_OLDER_RANGE_RES] — keeps the tab from growing one
 * translated string-array per release forever. Update on every new release: bump the latest
 * entry, and fold what used to be latest into the top of `changelog_older_versions` (all 7
 * `strings.xml` locales) with its range upper bound advanced. Condensed from the actual GitHub
 * release notes (`gh release view vX.X.X`). */
val CHANGELOG_ENTRIES = listOf(
    ChangelogEntry("0.0.50", "2026-10-05", R.array.changelog_v0_0_50),
    ChangelogEntry("0.0.37 – 0.0.49", "", CHANGELOG_OLDER_RANGE_RES),
)
