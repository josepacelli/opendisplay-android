# SDD — Remediation of security findings (2026-08-04)

Design for fixing the three actionable findings from [`SECURITY.md`](SECURITY.md). SCR-001,
SCR-004 and SCR-006 are excluded on purpose — they require diverging from the wire protocol the
Mac app speaks, which this repo cannot do unilaterally (see `CLAUDE.md`, "Protocolo — fonte da
verdade"). All three fixes below are local to the Android app, don't touch framing/handshake, and
don't require a corresponding change on the Mac side.

## Goals

- Close SCR-002 (bitmap decode DoS) and SCR-003 (unvalidated redirect URL) without changing any
  byte on the wire.
- Close SCR-005 (backup hardening) with a manifest-only change.
- No new dependencies, no new abstractions — each fix stays inside the function that has the
  problem, per the project's `simplicidade` convention.

## Non-goals

- Peer authentication, TLS, or any change to `Framing`/`AnnexB`/`WireProtocol` (that's SCR-001,
  accepted risk, would break Mac compatibility).
- Changing what `installId` is or how it's advertised (SCR-004, accepted risk, protocol requires
  a stable per-device id).
- Restricting which interface `ServerSocket` binds to (SCR-006, accepted risk, would break the
  documented USB/`adb forward` path in `CLAUDE.md`).

---

## SCR-002: Validate cursor sprite dimensions before decoding

**File:** `app/src/main/java/io/github/josepacelli/opendisplay/ui/CursorOverlay.kt`

**Current behavior:** `decodeSprite` calls `BitmapFactory.decodeByteArray` directly on
attacker-reachable bytes (`cursorImg` control message, base64-decoded in
`PhoneReceiver.handleCursorImage`). A crafted PNG with a small file size but a huge `IHDR` can
force an oversized allocation and crash the app (OOM) before any error handling has a chance to
kick in.

**Design:** Two-pass decode. First pass with `inJustDecodeBounds = true` (no pixel allocation,
just reads the header) to get `outWidth`/`outHeight`, reject anything outside a sane cursor-sprite
range before the real decode ever allocates memory. A remote cursor is, by definition, small —
128px is already generous for a hotspot sprite; cap at 256 to leave headroom without being
meaningless.

```kotlin
private const val MAX_SPRITE_DIMENSION_PX = 256

private fun decodeSprite(image: CursorImage): DecodedSprite? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(image.png, 0, image.png.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0 ||
        bounds.outWidth > MAX_SPRITE_DIMENSION_PX || bounds.outHeight > MAX_SPRITE_DIMENSION_PX
    ) {
        Log.warn("cursor sprite rejected: ${bounds.outWidth}x${bounds.outHeight}")
        return null
    }
    val bitmap = try {
        BitmapFactory.decodeByteArray(image.png, 0, image.png.size)?.asImageBitmap()
    } catch (e: Exception) {
        Log.warn("failed to decode cursor sprite", e)
        null
    } ?: return null
    return DecodedSprite(bitmap, image.normalizedWidth, image.normalizedHeight, image.anchorX, image.anchorY)
}
```

**Testing:** Unit test feeding a synthetic PNG byte array with `IHDR` claiming e.g. 8000×8000 but
a tiny file — assert `decodeSprite` returns `null` and never calls the allocating decode path.
Existing valid-sprite fixtures (if any) must still decode correctly at their real size.

**Compatibility:** None — purely local decode logic, no wire format change. A real Mac never
sends sprites anywhere near 256px, so this has zero effect on the legitimate path.

---

## SCR-003: Validate the `store` URL before it can ever be acted on

**File:** `app/src/main/java/io/github/josepacelli/opendisplay/net/PhoneReceiver.kt`

**Current behavior:** `handleControlJson`'s `UPDATE_REQUIRED` branch reads `obj.optString("store")`
straight from an unauthenticated peer into `PeerSignal.UpdateAndroid.storeUrl`. Nothing renders it
as a clickable link today, so there's no live exploit — but the field is a landmine for whoever
wires up an "Update" button later without re-reading this finding.

**Design:** Validate at the point of ingestion, not at the point of use — the wire boundary is the
right place to enforce this, so future UI code never has to remember to. Only accept an `https://`
URL whose host is the project's own domains (its GitHub org or the Play Store, if the app is ever
published there). Anything else collapses to `null`, same as if `store` had never been sent.

```kotlin
// companion object of PhoneReceiver, alongside the other constants
private val ALLOWED_STORE_HOSTS = setOf("github.com", "play.google.com")

private fun sanitizedStoreUrl(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val uri = try {
        java.net.URI(raw)
    } catch (e: Exception) {
        return null
    }
    if (uri.scheme != "https") return null
    if (uri.host !in ALLOWED_STORE_HOSTS) return null
    return raw
}
```

```kotlin
WireMessage.UPDATE_REQUIRED -> {
    val message = obj.optString(
        "message",
        "Atualize o OpenDisplay para continuar usando este segundo display.",
    )
    val store = sanitizedStoreUrl(if (obj.has("store")) obj.optString("store") else null)
    _peerSignal.value = PeerSignal.UpdateAndroid(message, store)
}
```

**Testing:** Unit test `sanitizedStoreUrl` with: a legit `https://github.com/...` URL (passes), a
non-https URL, a `javascript:` URL, an arbitrary host (`https://evil.example`), and `null`/blank
input — all four bad cases must return `null`.

**Compatibility:** None — `storeUrl` is presentation-layer state derived from the message, never
sent back on the wire.

---

## SCR-005: Harden backup extraction

**File:** `app/src/main/AndroidManifest.xml`

**Current behavior:** `android:allowBackup="true"` with no `dataExtractionRules`/
`fullBackupContent`, so the app's `SharedPreferences` (`installId`, the mDNS service name) can be
pulled via `adb backup` on an unlocked, debuggable device.

**Design:** Add a `dataExtractionRules.xml` that excludes the `opendisplay` preferences file from
both device-to-device transfer and cloud/`adb` backup, and point the manifest at it. Simpler than
flipping `allowBackup` to `false` outright — this way if some future version adds something
genuinely worth backing up (e.g. a UI preference), it opts in explicitly instead of the whole app
being backup-blind forever.

`app/src/main/res/xml/data_extraction_rules.xml` (new file):
```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="sharedpref" path="opendisplay.xml" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="sharedpref" path="opendisplay.xml" />
    </device-transfer>
</data-extraction-rules>
```

`AndroidManifest.xml` — add the attribute to the existing `<application>` tag:
```xml
<application
    android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    ...>
```

**Testing:** `./gradlew assembleDebug` (manifest merger validates the XML reference); manual
check with `adb backup` on a debug build, confirm the `opendisplay` prefs file is absent from the
extracted archive.

**Compatibility:** None — `minSdk` 26 is well above the API 31 floor for
`dataExtractionRules`; on API < 31 the attribute is simply ignored (that's `fullBackupContent`'s
job pre-31, not worth adding given the low sensitivity of the data involved).

---

## Rollout

All three are independent, no ordering dependency. Land as one PR (small, same theme, easy to
review together) or three — either works; there's no reason to split given the total diff is
under ~40 lines across 3 files plus one new XML resource.

## Out of scope (accepted risks, no design here)

- **SCR-001** (no peer auth) — would require a pairing/handshake step the Mac app doesn't speak.
- **SCR-004** (install ID over mDNS) — the Mac uses this id to correlate reconnects; removing it
  breaks reconnection behavior.
- **SCR-006** (listener bound to all interfaces) — required for the USB/`adb forward` path
  documented in `CLAUDE.md`.

If any of these ever need to change, it has to start upstream in `peetzweg/opendisplay`'s
protocol, not in this client alone.
