package io.github.josepacelli.opendisplay.ui

/** One released version's entry in the Settings "Changelog" tab.
 * @param version e.g. `"0.0.47"`, without the `v` prefix.
 * @param date release date as shown on screen, e.g. `"Sep 26, 2026"`.
 * @param highlights condensed bullet points — kept in English regardless of the device's
 * locale, matching this project's release-notes convention (see CLAUDE.md), since translating
 * every release's notes into six more languages would be an ongoing cost out of proportion
 * with a solo-maintainer project. */
data class ChangelogEntry(val version: String, val date: String, val highlights: List<String>)

/** Every release from v0.0.36 (the oldest still relevant — nothing before it shipped a
 * public build) to the current version, newest first. Condensed from the actual GitHub
 * release notes (`gh release view vX.X.X`) — update when cutting a new release. */
val CHANGELOG_ENTRIES = listOf(
    ChangelogEntry(
        version = "0.0.47",
        date = "Sep 26, 2026",
        highlights = listOf(
            "Fixed: the WiFi listener could resolve a VPN's tunnel address instead of the real LAN IP when a VPN ran alongside WiFi — now fails closed instead, with a clear \"VPN active\" message.",
            "Fixed inconsistent label font size in Settings > General.",
            "Performance overlay now defaults to the bottom-right corner.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.46",
        date = "Sep 24, 2026",
        highlights = listOf(
            "Dark theme refinements: near-black slate background instead of pure black, plus a proper error color token.",
            "Fixed a WCAG AA contrast failure on the landing page's \"Download APK\" button in light mode.",
            "Settings screen and splash screen now actually use the new slate palette.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.45",
        date = "Sep 24, 2026",
        highlights = listOf(
            "Rebrand: new app icon, logo and banner across the app and the landing page.",
            "Fixed the launcher icon getting clipped by circular launcher masks (e.g. Samsung One UI).",
            "CI hardened: workflow triggers scoped by path, GitHub Actions pinned to a commit SHA.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.44",
        date = "Sep 23, 2026",
        highlights = listOf(
            "App launcher icon redesigned with the official Apple and Android marks.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.43",
        date = "Sep 23, 2026",
        highlights = listOf(
            "Settings screen rebuilt as a full-screen, traditional Android layout with tabs.",
            "Fixed delayed typing and cursor updates on a mostly static extended display.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.42",
        date = "Sep 23, 2026",
        highlights = listOf(
            "Settings and About dialogs get a translucent, frosted-glass-style container.",
            "Fixed the header overlapping the theme/language switcher on medium-width screens.",
            "Landing page rewritten in React + Vite + TypeScript + Tailwind CSS v4 + shadcn/ui.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.41",
        date = "Sep 18, 2026",
        highlights = listOf(
            "Fixed translation errors in the Code of Conduct/Contributing pages.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.40",
        date = "Sep 17, 2026",
        highlights = listOf(
            "Protocol parity with the latest Mac sender: decode-ceiling negotiation, UDP cursor side channel, real stylus pressure and tilt.",
            "Korean translation added.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.39",
        date = "Sep 16, 2026",
        highlights = listOf(
            "Korean landing page.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.38",
        date = "Sep 12, 2026",
        highlights = listOf(
            "Simplified Chinese and Japanese added to the app.",
            "Landing page becomes multilingual, with its own page per language.",
        ),
    ),
    ChangelogEntry(
        version = "0.0.37",
        date = "Sep 8, 2026",
        highlights = listOf(
            "Fixed mDNS not re-announcing after a WiFi IP change.",
            "Fixed a MediaCodec reconfigure throttle edge case that could leave the decoder misconfigured.",
            "Security hardening: bounded cursor hotspot values, sanitized control-message logging.",
        ),
    ),
)
