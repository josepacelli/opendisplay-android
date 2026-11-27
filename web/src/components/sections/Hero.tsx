import { ArrowRight, Download } from 'lucide-react'
import { Trans, useTranslation } from 'react-i18next'

const MAC_DOWNLOAD = 'https://github.com/peetzweg/opendisplay/releases/latest'
const ANDROID_DOWNLOAD =
  'https://play.google.com/store/apps/details?id=io.github.josepacelli.opendisplay'

/** The Mac's desktop running on past its own edge onto the tablet, with a window sitting across
 * the seam: the product's one job, drawn instead of described. Decorative for assistive tech.
 * Capped at 1680px and sized by aspect ratio from `lg` up, so ultrawide screens get the same
 * picture rather than a thin band (#195); past the cap the Mac side gets its own bezel. */
function ExtendedStrip() {
  const { t } = useTranslation('landing')
  return (
    <figure
      role="img"
      aria-label={t('hero.stripAlt')}
      className="relative mx-auto mt-10 h-[220px] w-full max-w-[1680px] sm:h-[280px] lg:aspect-[1440/290] lg:h-auto"
    >
      <div className="absolute inset-0 flex">
        <div className="relative flex-[2.2] bg-[#172036] py-[10px] min-[1680px]:rounded-l-[28px] min-[1680px]:pl-[10px]">
          <div className="relative flex h-full w-full flex-col overflow-hidden min-[1680px]:rounded-l-[18px]">
            <div className="flex h-6 shrink-0 items-center justify-between border-b border-white/10 bg-[#162447] px-4 min-[1680px]:rounded-tl-[18px]">
              <div className="flex items-center gap-2">
                <span className="size-2 rounded-full bg-white/75" />
                <span className="h-1.5 w-10 rounded-full bg-white/45" />
                <span className="hidden h-1.5 w-7 rounded-full bg-white/25 sm:block" />
                <span className="hidden h-1.5 w-7 rounded-full bg-white/25 sm:block" />
              </div>
              <div className="flex items-center gap-2">
                <span className="h-1.5 w-5 rounded-full bg-[#4c8dff]/90" />
                <span className="h-1.5 w-8 rounded-full bg-white/35" />
              </div>
            </div>
            <div className="lg-scene relative flex-1 min-[1680px]:rounded-bl-[18px]">
              <div className="lg-backdrop absolute inset-0 bg-[radial-gradient(circle_at_85%_45%,rgba(124,192,255,0.42),transparent_55%),linear-gradient(180deg,#7cc0ff_0%,#3a7bff_60%,#1a3fa8_100%)] min-[1680px]:rounded-bl-[18px]">
                <div className="absolute inset-x-0 top-[58%] h-px bg-gradient-to-r from-[#bfe0ff]/35 via-[#bfe0ff]/80 to-[#bfe0ff]" />
              </div>
              <div
                className="glass absolute bottom-3 left-1/2 hidden h-6 w-32 -translate-x-1/2 items-center justify-center gap-2 rounded-full px-3 sm:flex"
                data-glass="clear"
                data-glass-dim
                data-glass-refraction="14"
                data-glass-bezel="10"
              >
                <span className="size-2 rounded-sm bg-white/75" />
                <span className="size-2 rounded-sm bg-white/75" />
                <span className="size-2 rounded-sm bg-[#4c8dff]" />
                <span className="size-2 rounded-sm bg-white/75" />
                <span className="size-2 rounded-sm bg-white/75" />
              </div>
              <span
                className="glass absolute bottom-3.5 left-4 inline-flex items-center gap-2 rounded-full px-3.5 py-1 text-[0.7rem] font-semibold tracking-[0.08em] text-white uppercase sm:left-6"
                data-glass="clear"
                data-glass-dim
                data-glass-refraction="12"
                data-glass-bezel="10"
              >
                <span>{t('hero.stripMac')}</span>
              </span>
            </div>
            <div className="pointer-events-none absolute inset-0 shadow-[inset_0_0_0_1px_rgba(255,255,255,0.14)] min-[1680px]:rounded-l-[18px]" />
          </div>
        </div>

        <div className="relative flex-1 rounded-r-[28px] bg-[#141c30] py-[10px] pr-[10px]">
          <div className="relative flex h-full w-full flex-col overflow-hidden rounded-r-[18px]">
            <div className="flex h-6 shrink-0 items-center justify-between rounded-tr-[18px] border-b border-white/10 bg-[#1a2c56] px-3.5">
              <span className="h-1.5 w-7 rounded-full bg-white/35" />
              <span className="size-1.5 rounded-full bg-[#0a101e] ring-1 ring-white/25" />
              <div className="flex items-center gap-1.5">
                <span className="size-1.5 rounded-full bg-[#4c8dff]" />
                <span className="h-1.5 w-4 rounded-full bg-white/40" />
              </div>
            </div>
            <div className="lg-scene relative flex-1 rounded-br-[18px]">
              <div className="lg-backdrop absolute inset-0 rounded-br-[18px] bg-[radial-gradient(circle_at_15%_45%,rgba(124,192,255,0.42),transparent_60%),linear-gradient(180deg,#7cc0ff_0%,#3a7bff_60%,#1a3fa8_100%)]">
                <div className="absolute inset-x-0 top-[58%] h-px bg-gradient-to-r from-[#bfe0ff] via-[#bfe0ff]/75 to-[#bfe0ff]/35" />
                <div className="absolute bottom-2 left-1/2 hidden h-1 w-14 -translate-x-1/2 rounded-full bg-white/40 sm:block" />
              </div>
              <span
                className="glass absolute right-3.5 bottom-3.5 inline-flex items-center gap-2 rounded-full px-3.5 py-1 text-right text-[0.7rem] font-semibold tracking-[0.08em] text-white uppercase"
                data-glass="clear"
                data-glass-dim
                data-glass-refraction="12"
                data-glass-bezel="10"
              >
                <span className="size-2 rounded-full bg-[#4c8dff] shadow-[0_0_8px_#4c8dff]" />
                <span>{t('hero.stripTablet')}</span>
              </span>
            </div>
            <div className="pointer-events-none absolute inset-0 rounded-r-[18px] shadow-[inset_0_0_0_1px_rgba(255,255,255,0.14)]" />
          </div>
        </div>
      </div>

      <div className="pointer-events-none absolute inset-y-[-14px] left-[68.75%] flex -translate-x-1/2 flex-col items-center justify-between">
        <span className="size-2.5 rounded-full bg-[var(--seam)] shadow-[0_0_12px_var(--seam)]" />
        <div className="h-full w-[3px] bg-[var(--seam)] shadow-[0_0_22px_var(--seam)]" />
        <span className="size-2.5 rounded-full bg-[var(--seam)] shadow-[0_0_12px_var(--seam)]" />
      </div>

      <img
        src="/hero-window.webp"
        alt=""
        width={1056}
        height={480}
        className="strip-window absolute top-[21%] left-[50%] w-[38%] rounded-xl ring-1 ring-black/25 shadow-[0_22px_48px_rgba(10,16,30,0.45)] lg:top-[14%] lg:w-[32%]"
      />
    </figure>
  )
}

/** One numbered step of the install bar.
 * @param n step number.
 * @param label where the step happens.
 * @param cta the download button's text.
 * @param href where it downloads from.
 * @param primary whether this is the filled (main) button. */
function InstallStep({ n, label, cta, href, primary }: { n: number; label: string; cta: string; href: string; primary?: boolean }) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 rounded-2xl bg-background/50 p-2.5 pl-3.5 lg:flex-nowrap lg:justify-start lg:bg-transparent lg:p-0">
      <div className="flex items-center gap-3">
        <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary font-heading text-sm font-semibold text-primary-foreground tabular-nums">
          {n}
        </span>
        <span className="text-sm font-medium whitespace-nowrap text-foreground">{label}</span>
      </div>
      <a
        href={href}
        data-glass-interactive
        data-glass-tint={primary ? '#1e5bd8' : undefined}
        className="glass lg-button px-5"
      >
        <Download className="size-4" aria-hidden="true" />
        <span className="tabular-nums">{cta}</span>
      </a>
    </div>
  )
}

export function Hero() {
  const { t } = useTranslation('landing')
  const specs = t('hero.specs', { returnObjects: true }) as string[]

  return (
    <section className="pt-10 pb-20 sm:pt-14">
      <div className="relative mx-auto max-w-[1200px] px-6">
        <div
          aria-hidden="true"
          className="absolute top-0 right-6 hidden h-full w-[38%] bg-[radial-gradient(var(--grille)_2px,transparent_2.5px)] bg-[length:16px_16px] lg:block"
        />
        <h1 className="relative max-w-[14ch] font-heading text-[3.25rem] leading-[0.95] font-light tracking-[-0.03em] text-foreground sm:text-[4.5rem] lg:text-[5.5rem]">
          <Trans i18nKey="hero.title" t={t} components={{ br: <br /> }} />
        </h1>
        <p className="relative mt-5 max-w-[60ch] text-base leading-relaxed text-muted-foreground sm:text-lg [&_a]:text-accent [&_a]:no-underline [&_a:hover]:underline">
          <Trans i18nKey="hero.lead" t={t} components={{ a: <a href="https://opendisplay.app/" /> }} />
        </p>
      </div>

      <ExtendedStrip />

      <div className="mx-auto max-w-[1200px] px-6">
        <div className="mt-7 flex flex-col gap-3 rounded-[28px] border border-border bg-card p-4 lg:flex-row lg:items-center lg:gap-5 lg:rounded-full lg:py-3 lg:pr-5 lg:pl-5">
          <InstallStep n={1} label={t('hero.stepMac')} cta={t('install.mac.cta')} href={MAC_DOWNLOAD} />
          <ArrowRight className="hidden size-4 shrink-0 text-muted-foreground lg:block" aria-hidden="true" />
          <InstallStep n={2} label={t('hero.stepAndroid')} cta={t('install.android.cta')} href={ANDROID_DOWNLOAD} primary />
          <p className="px-2 pt-1 text-xs leading-relaxed text-muted-foreground lg:ml-auto lg:max-w-[22rem] lg:p-0 lg:text-right">
            <span className="inline-flex items-center gap-2 font-medium text-foreground">
              <span className="size-2 rounded-full bg-accent" aria-hidden="true" />
              {t('hero.badge')}
            </span>
            <br />
            <span className="tabular-nums">{specs.join(' · ')}</span>
          </p>
        </div>
      </div>
    </section>
  )
}
