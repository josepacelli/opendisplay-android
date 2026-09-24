import { ArrowRight, ArrowUpRight, Coffee } from 'lucide-react'
import { Trans, useTranslation } from 'react-i18next'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'

export function Hero() {
  const { t } = useTranslation('landing')
  const specs = t('hero.specs', { returnObjects: true }) as string[]

  return (
    <section className="py-16 text-center sm:py-24">
      <img
        src="/banner.jpg"
        alt="OpenDisplay"
        className="mx-auto mb-8 w-full max-w-2xl rounded-2xl"
      />

      <Badge variant="outline" className="gap-2 border-primary/40 bg-primary/10 text-primary">
        <span className="size-1.5 rounded-full bg-primary" />
        {t('hero.badge')}
      </Badge>

      <h1 className="mx-auto mt-6 max-w-2xl text-3xl font-medium tracking-tight text-foreground sm:text-5xl">
        <Trans
          i18nKey="hero.title"
          t={t}
          components={{ em: <em className="text-primary not-italic" /> }}
        />
      </h1>

      <p className="mx-auto mt-5 max-w-xl text-[0.95rem] leading-relaxed text-muted-foreground">
        <Trans
          i18nKey="hero.lead"
          t={t}
          components={{ a: <a href="https://opendisplay.app/" className="text-primary no-underline hover:underline" /> }}
        />
      </p>

      <div className="mt-5 flex flex-wrap justify-center gap-2 text-xs text-muted-foreground">
        {specs.map((spec) => (
          <span key={spec} className="rounded-full border border-border px-3 py-1">
            {spec}
          </span>
        ))}
      </div>

      <div className="mt-7 flex flex-wrap justify-center gap-3">
        <Button render={<a href="#install" />} nativeButton={false} size="lg">
          {t('hero.ctaStart')}
          <ArrowRight className="size-4" aria-hidden="true" />
        </Button>
        <Button
          render={<a href="https://github.com/josepacelli/opendisplay-android" />}
          nativeButton={false}
          size="lg"
          variant="outline"
        >
          {t('hero.ctaGithub')}
          <ArrowUpRight className="size-4" aria-hidden="true" />
        </Button>
      </div>

      <div className="mt-8 flex justify-center">
        <div className="flex flex-wrap items-center gap-2.5 rounded-full border border-border bg-secondary/60 px-4 py-2 text-sm">
          <Coffee className="size-4 text-primary" aria-hidden="true" />
          <span className="text-muted-foreground">{t('hero.kofiText')}</span>
          <a href="#support" className="font-semibold text-primary no-underline hover:underline">
            {t('hero.kofiSupport')}
          </a>
        </div>
      </div>
    </section>
  )
}
