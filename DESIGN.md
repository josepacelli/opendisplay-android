---
name: OpenDisplay Android
description: Quiet navy desk hardware that turns an Android device into a Mac monitor, then gets out of the way. The app and its landing page share one world.
colors:
  navy-ground: "#0E1526"
  navy-deep: "#0A101E"
  navy-panel: "#141C30"
  navy-container: "#172036"
  navy-raised: "#1B2440"
  navy-raised-high: "#222C47"
  navy-selected: "#26314F"
  navy-hairline: "#2A3452"
  slate-outline: "#5A6580"
  slate-muted: "#9AA4B8"
  mist-secondary: "#C3CBDA"
  mist-ink: "#E8ECF4"
  signal-blue: "#4C8DFF"
  fault-red: "#FF6B5E"
  fault-container: "#5C1F1A"
  fault-on-container: "#FFDAD5"
  paper: "#F2F5FA"
  paper-ground: "#E3E8F2"
  paper-container: "#EBEFF6"
  paper-high: "#D8DFEC"
  paper-hairline: "#C9D1E0"
  paper-outline: "#7D879B"
  paper-muted: "#4F5970"
  paper-secondary: "#3F4A63"
  signal-blue-light: "#1E5BD8"
  fault-red-light: "#B3261E"
  fault-container-light: "#F9DEDC"
  overlay-strip: "#0E1526E0"
  overlay-danger: "#E5484D"
  brand-sky-light: "#7CC0FF"
  brand-sky: "#3A7BFF"
  brand-sky-deep: "#1A3FA8"
typography:
  display:
    fontFamily: "Hanken Grotesk, sans-serif"
    fontSize: "88sp"
    fontWeight: 300
    lineHeight: "92sp"
    letterSpacing: "-1.5sp"
  display-medium:
    fontFamily: "Hanken Grotesk, sans-serif"
    fontSize: "56sp"
    fontWeight: 300
    lineHeight: "60sp"
    letterSpacing: "-0.8sp"
  display-small:
    fontFamily: "Hanken Grotesk, sans-serif"
    fontSize: "36sp"
    fontWeight: 300
    lineHeight: "44sp"
  headline:
    fontFamily: "Hanken Grotesk, sans-serif"
    fontSize: "28sp"
    fontWeight: 400
    lineHeight: "36sp"
  title:
    fontFamily: "Hanken Grotesk, sans-serif"
    fontSize: "22sp"
    fontWeight: 500
    lineHeight: "28sp"
  title-medium:
    fontFamily: "Hanken Grotesk, sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "24sp"
    letterSpacing: "0.15sp"
  title-small:
    fontFamily: "Hanken Grotesk, sans-serif"
    fontSize: "14sp"
    fontWeight: 600
    lineHeight: "20sp"
    letterSpacing: "0.1sp"
  body:
    fontFamily: "Roboto, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
    letterSpacing: "0.5sp"
  body-medium:
    fontFamily: "Roboto, sans-serif"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "20sp"
    letterSpacing: "0.25sp"
  label:
    fontFamily: "Roboto, sans-serif"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: "20sp"
    letterSpacing: "0.1sp"
  label-medium:
    fontFamily: "Roboto, sans-serif"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "16sp"
    letterSpacing: "0.5sp"
  label-small:
    fontFamily: "Roboto, sans-serif"
    fontSize: "11sp"
    fontWeight: 500
    lineHeight: "16sp"
    letterSpacing: "0.5sp"
  web-display:
    fontFamily: "Hanken Grotesk Variable, Inter Variable, sans-serif"
    fontSize: "5.5rem"
    fontWeight: 300
    lineHeight: 0.95
    letterSpacing: "-0.03em"
  web-headline:
    fontFamily: "Hanken Grotesk Variable, Inter Variable, sans-serif"
    fontSize: "2.75rem"
    fontWeight: 300
    lineHeight: 1.05
    letterSpacing: "-0.02em"
  web-title:
    fontFamily: "Hanken Grotesk Variable, Inter Variable, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 400
    lineHeight: 1.33
  web-title-small:
    fontFamily: "Hanken Grotesk Variable, Inter Variable, sans-serif"
    fontSize: "1.125rem"
    fontWeight: 500
    lineHeight: 1.55
  web-body:
    fontFamily: "Inter Variable, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.625
  web-body-small:
    fontFamily: "Inter Variable, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.625
  web-caption:
    fontFamily: "Inter Variable, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 400
    lineHeight: 1.33
rounded:
  xs: "6dp"
  sm: "10dp"
  md: "16dp"
  lg: "20dp"
  xl: "28dp"
  full: "9999dp"
  web-md: "0.8rem"
  web-lg: "1rem"
  web-xl: "28px"
spacing:
  hair: "4dp"
  xs: "8dp"
  sm: "12dp"
  md: "16dp"
  lg: "20dp"
  xl: "24dp"
  2xl: "32dp"
  3xl: "40dp"
  panel: "56dp"
  web-gutter: "24px"
  web-heading-gap: "40px"
  web-section-sm: "48px"
  web-section: "64px"
  web-column: "1200px"
  web-strip-max: "1680px"
components:
  idle-panel:
    backgroundColor: "{colors.navy-panel}"
    rounded: "{rounded.xl}"
    padding: "56dp"
  button-primary:
    backgroundColor: "{colors.mist-ink}"
    textColor: "{colors.navy-ground}"
    rounded: "{rounded.full}"
    height: "52dp"
  button-tonal:
    backgroundColor: "{colors.navy-raised-high}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.full}"
    height: "52dp"
  step-card:
    backgroundColor: "{colors.navy-raised}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.lg}"
    padding: "20dp"
  step-number:
    backgroundColor: "{colors.mist-ink}"
    textColor: "{colors.navy-ground}"
    rounded: "{rounded.full}"
    size: "36dp"
  settings-group:
    backgroundColor: "{colors.navy-panel}"
    rounded: "{rounded.lg}"
    padding: "4dp 0"
  length-chip:
    backgroundColor: "transparent"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.sm}"
    padding: "10dp 12dp"
  length-chip-selected:
    backgroundColor: "{colors.navy-selected}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.sm}"
  version-tag:
    backgroundColor: "{colors.navy-selected}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.xs}"
    padding: "4dp 10dp"
  overlay-strip:
    backgroundColor: "{colors.overlay-strip}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.xl}"
    padding: "8dp"
  overlay-button:
    backgroundColor: "{colors.navy-raised}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.full}"
    size: "52dp"
  overlay-button-on:
    backgroundColor: "{colors.mist-ink}"
    textColor: "{colors.navy-ground}"
    rounded: "{rounded.full}"
    size: "52dp"
  overlay-button-danger:
    backgroundColor: "{colors.overlay-danger}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.full}"
    size: "52dp"
  perf-hud:
    backgroundColor: "{colors.overlay-strip}"
    textColor: "{colors.mist-ink}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.sm}"
    padding: "6dp 10dp"
  unstable-pill:
    backgroundColor: "{colors.overlay-strip}"
    textColor: "{colors.mist-ink}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.full}"
    padding: "12dp 18dp"
  problem-banner:
    backgroundColor: "{colors.fault-container}"
    textColor: "{colors.fault-on-container}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.lg}"
    padding: "16dp"
  web-button-primary:
    backgroundColor: "{colors.mist-ink}"
    textColor: "{colors.navy-ground}"
    rounded: "{rounded.full}"
    height: "44px"
    padding: "0 20px"
  web-button-secondary:
    backgroundColor: "{colors.navy-raised}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.full}"
    height: "44px"
    padding: "0 20px"
  web-install-bar:
    backgroundColor: "{colors.navy-panel}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.full}"
    padding: "12px 12px 12px 20px"
  web-install-bar-stacked:
    backgroundColor: "{colors.navy-panel}"
    textColor: "{colors.mist-ink}"
    rounded: "{rounded.web-xl}"
    padding: "20px"
  web-step-number:
    backgroundColor: "{colors.mist-ink}"
    textColor: "{colors.navy-ground}"
    rounded: "{rounded.full}"
    size: "28px"
  web-ruled-item:
    backgroundColor: "transparent"
    textColor: "{colors.slate-muted}"
    typography: "{typography.web-body-small}"
    padding: "20px 0 0"
  web-section-heading:
    textColor: "{colors.mist-ink}"
    typography: "{typography.web-headline}"
    width: "44rem"
  web-framed-media:
    backgroundColor: "{colors.navy-ground}"
    rounded: "{rounded.web-lg}"
---

# Design System: OpenDisplay Android

## Overview

**Creative North Star: "The Indicator Lamp"**

The idle screen is built like a piece of desk hardware: one rounded navy panel, a single indicator lamp that tells the connection story, and the device's network name set large and light, because that name is the one thing the user has to find on the Mac. Everything else is quiet: two numbered steps, the network address and version in small tabular figures, Settings and About as pill buttons. Once the Mac connects, the panel unmounts and the screen is the Mac's desktop. The only chrome left is a slim dark strip and an optional HUD.

The palette is navy and blue, taken from the blue app icon. Saturated colour is spent only on state: signal blue when listening, red for a problem, slate when off. The one material is a perforated dot grille behind the steps. Depth comes from tonal steps of navy, not shadows. Components are stock Material 3 (Button, FilledTonalButton, Switch, TopAppBar, tabs) restyled by the theme, so the app reads as native Android.

This build diverges from the original direction contract in five places, all on purpose. (1) Graphite and amber became navy and blue, because the owner asked for a dark blue that matches the app icon. (2) The display face is Hanken Grotesk, not Roboto Flex: Roboto is the platform family and can't be the app's own display voice. It ships as four static weight files because `FontVariation` on a resource font did not apply weight on the test device. (3) The planned three-stage strip (Listening / Mac found / Streaming) became three lamp colours. The app has no real "Mac found" state, and the idle screen unmounts on connect, so a "Streaming" stage would never be seen. (4) Network and version meta sit bottom-left of the idle panel, in the slot the stage strip held, instead of in a card on the grille side. (5) Overlay chrome stays fixed dark navy whatever the system theme, because it sits on the Mac's desktop.

**Key Characteristics:**
- One panel, one lamp, one large name. The lamp colour is the status, paired with a pill status readout.
- Navy tonal layering with 1dp `navy-hairline` (`paper-hairline`) surface borders, no shadows.
- Hanken Grotesk light for display, semibold for small titles. Body and labels use the system face.
- Tabular figures on every number: port, IP, version, step numbers, delays, HUD.
- Pill buttons. Off-white filled primary on dark, navy filled primary on light.
- Overlay chrome on video is always dark navy, translucent, bordered with a 1dp hairline, and hides itself.
- The landing page (`web/`) is the same world on paper and navy: ink primary, blue only for links, the seam and live state, Hanken Grotesk light headings, bento cards with 1px hairline borders, and a hardware showcase of the product's job.

### The landing page

The site extends the app's world rather than starting a new one. Its one idea is the product's job drawn once: under a light display headline ("One desktop. Two screens."), a full-bleed strip shows the Mac's desktop running past its own edge onto a tablet, with a glowing blue seam between them and a real captured window sitting across the seam. Right under it, a two-step pill bar installs both apps. Everything below is plain proof on a 1200px column: how it works, real screenshots, install, features, comparison, FAQ, contributing, support.

It is built in React with Tailwind v4 and shadcn/ui, retokened onto the navy and paper families. Like the app, it is flat, quiet and type-led. Content is always visible; the only entrance motion is the strip window's one slide. Where the build differs from its direction contract, the build is recorded here: the headline tops out at 88px (5.5rem), not the ~96px the contract named. The app's dot grille carries over as the site's one material: 2px dots on a 16px grid in `--grille`, filling the right 38% of the headline block from `lg`.

## Colors

A cool navy family in two schemes (dark and light), with one signal blue and one red, both used only to report state.

### Primary
- **Mist Ink** (`mist-ink`): primary in the dark scheme. Text on navy, the filled Settings button, step number discs, and the "on" state of overlay buttons.
- **Navy Ground** (`navy-ground`): primary in the light scheme, and the dark scheme's background and surface. Also the ink on Mist Ink fills.

### Secondary
- **Mist Secondary** (`mist-secondary`) / **Paper Secondary** (`paper-secondary`): secondary text roles in dark and light. Used sparingly.

### Tertiary
- **Signal Blue** (`signal-blue`, light scheme `signal-blue-light`): the listening lamp and the status line next to it. This is the only blue in the app, and it always means "waiting for the Mac, all good".
- **Fault Red** (`fault-red`, light scheme `fault-red-light`): the problem lamp and status line (no WiFi, VPN active). The problem banner uses its container pair (`fault-container` / `fault-on-container`, light `fault-container-light`).

### Neutral
- **Navy Panel** (`navy-panel`): the idle panel and settings groups (surfaceContainerLow).
- **Navy Container / Raised / Raised High** (`navy-container`, `navy-raised`, `navy-raised-high`): tonal steps for step cards (`navy-raised`) and tonal buttons (`navy-raised-high`).
- **Navy Deep** (`navy-deep`): lowest container.
- **Navy Selected** (`navy-selected`): primaryContainer. Selected length chips, version tags.
- **Navy Hairline** (`navy-hairline`): outlineVariant. Dividers, grille dots, chip borders, the overlay strip border.
- **Slate Outline** (`slate-outline`): outline, and the "off" lamp in the dark scheme.
- **Slate Muted** (`slate-muted`): secondary text (onSurfaceVariant), and the "off" lamp in the light scheme.
- **Paper family** (`paper`, `paper-ground`, `paper-container`, `paper-high`, `paper-hairline`, `paper-outline`, `paper-muted`): the light scheme's surface, background, containers, divider, outline and muted text, mapped role for role onto the navy family.
- **Overlay Strip** (`overlay-strip`): Navy Ground at about 88% alpha. Background of the connected menu, perf HUD and unstable pill, in both themes.
- **Overlay Danger** (`overlay-danger`): the Disconnect button and the unstable-connection dot on the overlay.

### Named Rules
**The Lamp Rule.** Saturated colour names a connection state and nothing else: blue is listening, red is a problem, slate is off. No accent on decoration, headings or icons.

**The Desktop Rule.** Anything drawn over the Mac's video uses the fixed overlay navy (`OverlayColors`), never the theme scheme. Whatever sits behind it is someone else's desktop.

### Site token mapping

`web/src/index.css` defines the shadcn variables with values from the families above, in two schemes. Light is the default `:root`; dark applies through `prefers-color-scheme: dark` unless `data-theme="light"`, or always with `data-theme="dark"`.

| Site variable | Light | Dark |
|---|---|---|
| `--background` | `paper-ground` | `navy-ground` |
| `--card`, `--popover` | `paper` | `navy-panel` |
| `--foreground` | `navy-ground` | `mist-ink` |
| `--primary` / `--primary-foreground` | `navy-ground` / `paper` | `mist-ink` / `navy-ground` |
| `--secondary`, `--muted` | `paper-high` | `navy-raised` |
| `--muted-foreground` | `paper-muted` | `slate-muted` |
| `--border`, `--input`, `--grille` | `paper-hairline` | `navy-hairline` |
| `--accent`, `--seam` | `signal-blue-light` | `signal-blue` |
| `--ring` | `signal-blue-light` at 45% | `signal-blue` at 50% |
| `--destructive` | `fault-red-light` | `fault-red` |

Primary is ink, not blue: the filled button is navy on paper and mist on navy, exactly as in the app. The scrollbar is `--border` on `--background`. Text selection is the seam colour with white text.

The hero strip's desktop is the brand mark's screen sky (`brand-sky-light` to `brand-sky` at 60% to `brand-sky-deep`, top to bottom) with a `#BFE0FF` horizon line at 70% at 62% height. The strip's bezel is fixed `navy-raised` and the Mac's menu bar `navy-ground` at 55%, in both themes: the strip is a picture of hardware, so like the overlay chrome it doesn't follow the scheme.

**The Seam Rule.** On the site, signal blue (`--accent` / `--seam`) marks only links, the seam between the two screens, text selection and focus rings. Buttons, headings, icons and numbers stay ink or muted. This is the site's form of the Lamp Rule: blue means "this goes somewhere" or "this is live", never decoration.

**The Ink Primary Rule.** The primary action is filled with ink (`--primary`), never with blue. Navy on paper, mist on navy.

## Typography

**Display Font:** Hanken Grotesk (static instances at 300, 400, 500, 600, bundled in `res/font/`)
**Body Font:** system sans (Roboto on stock Android)

**Character:** Hanken Grotesk is a calm, slightly technical grotesk, used light at display size so the device name reads like a model plate. Body and labels stay on the system face so longer text and controls feel like the rest of Android.

### Hierarchy
- **Display** (300, 88/92sp, -1.5sp tracking): the device name on the wide idle panel. Drops to Display Medium (56/60sp) in the single-column layout, and to Display Small (36/44sp) on short or narrow screens.
- **Headline** (400 at large/medium, 500 at small; Material sizes): available in the scale. The current screens barely use it.
- **Title** (500, 22/28sp): app bar titles.
- **Title Medium / Small** (600, 16sp and 14sp): the lamp's status line (with tabular figures), step numbers, settings category headers in muted ink.
- **Body** (system 400, 16/24sp): step text, the "this is the name on the Mac" line. Body Medium (14/20sp) for banners and settings secondary text.
- **Label** (system 500, 14/20sp): button text, meta lines, length-chip values. Label Medium (12/16sp) for the perf HUD. Label Small (11/16sp) for the captions under overlay buttons.

### Named Rules
**The Tabular Figures Rule.** Every number on screen sets `fontFeatureSettings = "tnum"`: port, IP:port, version, step numbers, delays, HUD readouts. Numbers must not jitter as they change.

**The Two Faces Rule.** Hanken Grotesk carries display, headline and title. Body and label stay on the system face. Don't put Hanken on running text, and don't put the system face at display size.

### Site type

The site self-hosts both faces from `@fontsource-variable`: Hanken Grotesk Variable as `--font-heading` (every `h1`, `h2`, `h3`), Inter Variable as `--font-sans` for everything else. Inter stands in for the app's system face; the Two Faces Rule holds unchanged.

- **Web Display** (300, 3.25rem / 4.5rem at `sm` / 5.5rem at `lg`, line-height 0.95, -0.03em, max 14ch): the hero headline only, top-left on the column.
- **Web Headline** (300, 2rem / 2.75rem at `sm`, 1.05, -0.02em, balanced, max 44rem): every section title.
- **Web Title** (400, 1.5rem): the two install steps.
- **Web Title Small** (500, 1.125rem): feature names.
- **Web Body** (Inter 400, 1rem, 1.625; 1.125rem at `sm` in the hero lead): leads and section subs, in muted ink, capped at 60ch.
- **Web Body Small** (Inter 400, 0.875rem, 1.625): feature, credit and install descriptions, nav links, table cells.
- **Web Caption** (Inter 400, 0.75rem): the install bar's specs line, install notes, footer locales and disclaimer.

Download labels carrying a version and the step numbers use `tabular-nums`, per the Tabular Figures Rule.

## Layout

The idle screen is a single panel inset 16dp from the safe drawing area, at the `xl` radius. When the window is landscape and at least 600dp wide, it splits into two equal columns. The left column holds the status block (lamp, status line, device name, explainer), vertically centred, with the network/version meta pinned to the bottom. The right column has the grille filling it, with the step card and the buttons centred on top and the buttons aligned to the end. Panel padding is 56dp, or 24dp when height is under 480dp (a phone on its side). Below that breakpoint, the panel becomes one scrolling column with 24dp padding: status block, steps, meta, buttons (end-aligned), then a 96dp strip of grille as the closing material.

Settings is a full-screen dialog with a top app bar and tabs. The content is capped at 720dp wide and centred, with 16dp side padding. Groups are separated by 24dp.

Spacing uses a 4dp base: 4, 8, 12, 16, 20, 24, 32, 40, 56. The contract asked for a strict 8dp grid; the build also uses 12 and 20 (and 6 and 10 inside small chips). Those values are now part of the system.

### Site layout

One centred column, 1200px max with 24px side gutters, shared by the header, hero text, install bar, every section and the footer. Only the hero strip breaks out of it. Sections sit on a 48px vertical rhythm (64px from `sm`), with 40px between a section heading and its content. Headings and body text are left-aligned on the column; only the footer is centred. Anchored sections keep a 76px scroll margin for the sticky header.

Collections use structured bento cards (`bg-card`, 1px `--border` contour, `rounded-2xl`) in a grid of 1 / 2 (`sm`) / 3 (`lg`) columns. Features, how it works, screenshots (with click-to-expand lightbox preview), install steps and contributor credits all use this surface grammar.

Breakpoints are Tailwind's: `sm` 640px (larger type, two-column lists), `lg` 1024px (three-column lists, the install bar becomes one row, the strip switches to aspect-ratio sizing, the grille appears), `xl` 1280px (the inline nav appears; below it, a menu button). The brand's "Android" suffix hides under 400px.

Connected, the video box is aspect-correct and centred on black. Overlay chrome is placed against it: the menu docks to a draggable screen edge, the HUD goes to the corner the user picked, banners sit top-centre at 32dp, and the unstable pill sits bottom-centre at 24dp.

## Elevation & Depth

Flat. No shadows or elevation anywhere in the UI code. Depth is tonal: background `navy-ground`, then the panel (`navy-panel`), then raised cards (`navy-raised`) and tonal buttons (`navy-raised-high`). The light scheme does the same with the paper family. On the video, overlays get their separation from translucent navy plus a 1dp `navy-hairline` border, not from a shadow.

### Named Rules
**The Tonal Step Rule.** To lift something, move it one step up the navy (or paper) container ramp. Don't add a shadow.

### Site depth

The site is flat too, with two deliberate exceptions, both inside the hero strip. The window crop floats over the desktop with `box-shadow: 0 18px 40px rgba(0,0,0,0.35)`, because it is a window on a desktop. The seam glows with `box-shadow: 0 0 18px var(--seam)`, because it is the live joint between the screens. The sticky header and the open menus use translucency plus backdrop blur (`backdrop-blur-xl`, `backdrop-saturate-150`) over a hairline border, not elevation.

## Shapes

Rounded, soft-cornered hardware. Material shape scale: extraSmall 6dp (version tags), small 10dp (length chips, HUD), medium 16dp (screen-map picker frame), large 20dp (step card, settings groups, problem banner), extraLarge 28dp (idle panel, overlay strip). Every button, overlay action, step number, lamp and status dot is a full circle or pill. Borders are 1dp hairlines, except the screen-map picker frame, which uses a 2dp outline to read as a monitor bezel.

On the site, `--radius` is 1rem. Calls to action are full pills (both hero downloads, the install-section downloads, the back-to-top button, step numbers). The install bar is a pill from `lg` and a 28px-radius panel when it stacks. Framed media (screenshots, the comparison table, the strip window) uses 1rem (`web-lg`). Header utilities (GitHub link, theme toggle, menu button, language switch) are soft 0.8rem rectangles. The hero strip repeats the app's hardware geometry: a 10px `navy-raised` bezel with 28px outer corners.

## Components

### Buttons
- **Shape:** full pill, minimum height 52dp, a 20dp leading icon with an 8dp gap.
- **Primary (filled):** Settings. `mist-ink` on navy in dark, `navy-ground` on paper in light.
- **Tonal:** About. `navy-raised-high` with ink text. It sits before the primary in a row with a 12dp gap.
- **Outlined:** used only for the About tab's link buttons.
- **States:** Material 3 defaults (ripple, state layers). No custom hover or press styling.

### Chips
- **Idle-delay length chips:** a row of radio-role chips at the `sm` radius with a 1dp border. Each one shows the delay in seconds (Label, tabular) over a 4dp bar whose length is proportional to the delay, up to 72dp. The delay is drawn as a length. Selected: `navy-selected` fill, primary-coloured border and bar. Unselected: transparent, `navy-hairline` border, outline-coloured bar. Minimum height 48dp.
- **Version tag:** `navy-selected` at the `xs` radius, 4/10dp padding, on changelog cards.

### Cards / Containers
- **Idle panel:** `navy-panel`, `xl` radius, 56dp padding (24dp when short).
- **Step card:** `navy-raised`, `lg` radius. Each row is 20dp padding, a 36dp `mist-ink` number disc with tabular Title Small, a 20dp gap, then Body text. Rows are split by an inset `navy-hairline` divider starting at 76dp.
- **Settings group:** `navy-panel`, `lg` radius, 4dp vertical padding, rows inside. A category header in Title Small, muted, sits above it with 16dp inset.
- **Changelog card:** container surface, 20dp padding, version tag plus bullet list.

### Navigation
Settings uses a stock Material 3 TopAppBar (close icon, title, a filled Save action) and tabs: Details / General / About / What's new. No custom navigation chrome.

### Indicator Lamp (signature)
A 64dp canvas: a 12dp-radius solid core in the state colour, with two static halos at 18% alpha (+8dp) and 10% alpha (+18dp). While listening, a 2dp stroke ring pulses outward 20dp and fades from 45% alpha over 2200ms (LinearOutSlowIn, repeating). Next to it, 16dp away, the status line is Title Medium tabular in the same state colour (muted ink when off).

### Grille (signature)
Perforated material: 2dp-radius dots on a 16dp square grid in `navy-hairline` (`paper-hairline` in light). It fills the right half of the wide idle panel, or a 96dp band at the bottom of the stacked layout. It is the panel's only texture.

### Connected Menu (overlay)
A pill strip docked to a screen edge: `overlay-strip` fill, 1dp `navy-hairline` border, `xl` radius, 8dp padding, 10dp gaps. A 48dp grip toggles it open. 52dp circular actions (zoom, fullscreen, settings, disconnect) carry Label Small captions underneath, up to 68dp wide. Off is `navy-raised`, on is `mist-ink` with navy icon, Disconnect is `overlay-danger`. A 1dp hairline separates Disconnect from the rest. Expand: fade 200ms plus expand 250ms FastOutSlowIn. Collapse: fade 120ms plus shrink 160ms. Background colour changes animate. It hides on touch or cursor activity and comes back after the user-set idle delay.

### Perf HUD and Unstable Pill (overlay)
HUD: tabular Label Medium on `overlay-strip` at the `sm` radius, 6/10dp padding, in the user-chosen corner. Unstable pill: `overlay-strip` full pill, 12/18dp padding, a 10dp `overlay-danger` dot, then Body Medium.

### Screen-Map Picker
The HUD position is picked on a 16:10 miniature screen (up to 280dp wide, 2dp outline frame, `md` radius). Each corner is a 72x48dp radio target. The selected corner shows a 40x14dp readout bar; the others show a 10dp `navy-hairline` dot.

### Cursor
The fallback remote cursor is a 5dp-radius white dot with a 1.5dp black stroke. It is plain black and white on purpose, not themed, because it has to show up on any desktop. Decoded sprites from the Mac are drawn as-is.

### Icons
Material Symbols Rounded as vector drawables (`ic_*.xml`), 24dp viewport, tinted by context: 18dp in meta lines, 20dp in buttons and banners.


### Site: Hero Extended Strip (signature)
The product's job as one picture, marked `role="img"` with a translated label. Two screens side by side: the Mac's desktop (flex 2.2) and the tablet (flex 1), both the brand sky with a horizon line; a translucent navy menu bar across the Mac side; small white uppercase labels ("Your Mac", "Your Android tablet") at their bottom-left, as captions inside the picture. The tablet has a 10px `navy-raised` bezel with 28px right corners, open on the seam side. A 3px seam in `--seam` with its glow sits at 68.75% and overhangs the strip by 12px top and bottom. The window is `hero-window.webp`, a real crop of a mirroring screenshot (provenance in `hero-window.webp.json`), at 38% wide (32% from `lg`), 1rem corners, straddling the seam.
- **Size:** full-bleed, 200px tall (260px at `sm`); from `lg` the height follows a 1440:280 aspect ratio, capped at 1680px wide and centred, so ultrawide screens get the same picture, not a thin band. Past 1680px the Mac side gets its own 10px bezel and 28px left corners, so the strip reads as a closed object instead of a cut-off band.
- **Motion:** on load the window slides once from the Mac side onto the seam (`translateX(-22%)` to 0, 1100ms, `cubic-bezier(0.16, 1, 0.3, 1)`, 200ms delay). With `prefers-reduced-motion: reduce` it is static.

### Site: Install Bar
Two numbered steps right under the strip, in order: 1 On your Mac, then Download for Mac (secondary pill); an arrow (from `lg`); 2 On your Android, then Download APK with the current version (primary pill). The specs line ("Free · GPL-3.0 · Android 8.0+" and the alpha note) sits right-aligned at the end in caption type, its first line in full ink. Card surface with a hairline border, pill from `lg` with 12px/20px insets, a 28px-radius stacked panel below. Step numbers are 28px ink discs with Hanken 600 tabular figures. Download buttons are 44px pills with a 16px download icon. The Install section repeats the same two steps as a two-column ruled list with a Web Title heading, description, the primary pill and a caption note.

### Site: Section Heading
A Web Headline title with an optional muted Web Body line under it, left on the column, 40px above the content. Nothing sits above the title.

### Site: Ruled List
1px `--border` top rule, then 16px (credits), 20px (features) or 24px (install) above the content. Features: a 20px muted line icon, a Web Title Small name, a Web Body Small description. Credits: the contributor's handle in ink, then the text with inline links. No card fill, no avatars.

### Site: Comparison Table
shadcn table inside a 1rem hairline frame that scrolls sideways on narrow screens. Rows: price, open source, account required, same protocol, central server; columns: OpenDisplay, Duet, Spacedesk. Each cell gets a mark (16px check or cross) whose favour comes from the `FAVOURABLE` array in `Compare.tsx`: a product fact kept in code, not in the seven locale files, so a translation can't flip a claim. Favourable cells read in ink, the rest muted with a 60% muted cross.

### Site: FAQ
shadcn accordion: hairline-separated items, the question in 1rem medium with a chevron at the end, the answer in muted text capped at 70ch with signal-blue inline links.

### Site: Header and Footer
Sticky header, `--background` at 60% with backdrop blur over a 40% hairline: the logo mark and "OpenDisplay Android" (suffix muted), section links in muted Web Body Small (the active section, tracked by `IntersectionObserver`, in medium ink), a GitHub link with a star count, the theme toggle (light, dark, auto, the icons swapping with a 300ms scale and rotate, off under reduced motion) and the language switch. Below `xl` the links move into a blurred dropdown. Footer: a hairline top rule, centred muted links, a locale row with the current one in semibold ink, and a disclaimer caption.

### Site: Legal Pages
Code of conduct, contributing and privacy share the theme in a 42rem column with no header: title and subtitle, muted body at 0.95rem with signal-blue links, code and pre blocks on `--secondary` with a hairline, the locale row and a back link.

## Brand mark

The app icon, About banner, site favicon/logo/banner/og-image and Play Store graphics all come
from one authored vector mark (sources in `brand/`, issue #173): the "D" is a tablet, its
counter a screen showing a horizon, and a light window with a cursor is being dragged into it
from the left — the product's job, extending the Mac's desktop onto a second display.

- **Colours:** frame gradient `#8FCBFF` → `#3A6CF0`, screen sky `#7CC0FF` → `#3A7BFF` →
  `#1A3FA8`, a blurred `#3A7BFF` glow at 60%, on a radial navy ground `#1C2A4D` → `#0A101E`.
  The window's three title-bar dots are the one place red/amber/green appear outside a
  connection state: brand art, not UI, so the Lamp Rule doesn't apply to it.
- **Wordmark:** "Open" in mist ink, "Display" in signal blue, Hanken Grotesk 500, set as outlines
  so raster exports don't depend on an installed font. No tagline baked into images — the site
  ships in 7 languages.
- **Android:** adaptive icon with a vector radial background, a raster foreground at xxxhdpi
  (the glow can't be expressed in VectorDrawable) and a vector monochrome layer for themed icons.
- **Never:** Apple or Android logos in brand art.

## Do's and Don'ts

### Do:
- **Do** let the lamp carry state: `signal-blue` listening, `fault-red` problem, slate off, and colour the status line to match.
- **Do** set every number with tabular figures (`tnum`).
- **Do** lift surfaces by stepping up the navy/paper container ramp: panel, raised, raised-high.
- **Do** use `OverlayColors` (translucent navy, hairline border, pill shapes) for anything on top of the video, in both themes.
- **Do** keep Hanken Grotesk light (300) at display size for the device name, and semibold (600) for small titles.
- **Do** use stock Material 3 components restyled by the theme, with pill buttons at least 52dp tall and 48dp touch targets.
- **Do** build the site from the shared tokens in `web/src/index.css` (paper in light, navy in dark) and fill the primary action with ink.
- **Do** keep site content on the 1200px column, left-aligned, with a Web Headline section title and an optional muted line under it.
- **Do** lay site collections out as ruled lists: a 1px top hairline per item, no card fill.
- **Do** show site content by default. The hero window's one slide is the only entrance motion, and reduced motion turns it off.
- **Do** keep comparison verdicts in code (`FAVOURABLE`) and only the wording in the locale files.

### Don't:
- **Don't** use blue or red as decoration. If a colour isn't reporting connection state, it should be navy, mist or slate.
- **Don't** add shadows or elevation to separate surfaces.
- **Don't** theme overlay chrome from the system scheme, or show the idle panel while connected.
- **Don't** use the system face (Roboto) at display size, or Hanken Grotesk for body text.
- **Don't** add a stage strip or a "Mac found" state. The app can't detect it, and the lamp already covers what it can.
- **Don't** use signal blue on the site for buttons, fills, headings or icons. It belongs to links, the seam, selection and focus.
- **Don't** put an eyebrow, kicker or label above a section title.
- **Don't** hide content behind scroll-triggered reveals.
- **Don't** use third-party avatars or profile images. Credits are text handles.
- **Don't** let the hero strip stretch into a thin band on wide screens: keep the 1680px cap and the aspect ratio.
