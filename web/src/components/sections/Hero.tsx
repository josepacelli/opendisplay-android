import { ArrowRight, Download } from 'lucide-react'
import { Trans, useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'

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
      className="relative mx-auto mt-8 h-[200px] w-full max-w-[1680px] sm:h-[260px] lg:aspect-[1440/280] lg:h-auto"
    >
      <div className="absolute inset-0 flex">
        <div className="relative flex-[2.2] border-[#1b2440] bg-[linear-gradient(180deg,#7cc0ff,#3a7bff_60%,#1a3fa8)] min-[1680px]:rounded-l-[28px] min-[1680px]:border-[10px] min-[1680px]:border-r-0">
          <div className="absolute inset-x-0 top-0 h-5 bg-[#0e1526]/55" />
          <div className="absolute inset-x-0 top-[62%] h-0.5 bg-[#bfe0ff]/70" />
          <span className="absolute bottom-4 left-4 text-xs font-medium tracking-[0.08em] text-white uppercase sm:left-6">
            {t('hero.stripMac')}
          </span>
        </div>
        <div className="relative flex-1 rounded-r-[28px] border-[10px] border-l-0 border-[#1b2440] bg-[linear-gradient(180deg,#7cc0ff,#3a7bff_60%,#1a3fa8)]">
          <div className="absolute inset-x-0 top-[62%] h-0.5 bg-[#bfe0ff]/70" />
          <span className="absolute right-4 bottom-4 text-right text-xs font-medium tracking-[0.08em] text-white uppercase">
            {t('hero.stripTablet')}
          </span>
        </div>
      </div>
      <div className="absolute inset-y-[-12px] left-[68.75%] w-[3px] bg-[var(--seam)] shadow-[0_0_18px_var(--seam)]" />
      <img
        src="/hero-window.webp"
        alt=""
        width={1056}
        height={480}
        className="strip-window absolute top-[20%] left-[50%] w-[38%] rounded-lg shadow-[0_18px_40px_rgba(0,0,0,0.35)] lg:top-[12%] lg:w-[32%]"
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
    <div className="flex items-center gap-3">
      <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary font-heading text-sm font-semibold text-primary-foreground tabular-nums">
        {n}
      </span>
      <span className="text-sm whitespace-nowrap text-foreground">{label}</span>
      <Button
        render={<a href={href} />}
        nativeButton={false}
        variant={primary ? 'default' : 'secondary'}
        className="h-11 rounded-full px-5"
      >
        <Download className="size-4" aria-hidden="true" />
        <span className="tabular-nums">{cta}</span>
      </Button>
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
        <div className="mt-6 flex flex-col gap-4 rounded-[28px] border border-border bg-card p-5 lg:flex-row lg:items-center lg:gap-5 lg:rounded-full lg:py-3 lg:pr-3 lg:pl-5">
          <InstallStep n={1} label={t('hero.stepMac')} cta={t('install.mac.cta')} href={MAC_DOWNLOAD} />
          <ArrowRight className="hidden size-4 text-muted-foreground lg:block" aria-hidden="true" />
          <InstallStep n={2} label={t('hero.stepAndroid')} cta={t('install.android.cta')} href={ANDROID_DOWNLOAD} primary />
          <p className="text-xs text-muted-foreground lg:ml-auto lg:max-w-[22rem] lg:pr-4 lg:text-right">
            <span className="text-foreground">{t('hero.badge')}</span>
            <br />
            {specs.join(' · ')}
          </p>
        </div>
      </div>
    </section>
  )
}
