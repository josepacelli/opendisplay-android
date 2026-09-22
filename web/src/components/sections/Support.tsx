import { Coffee } from 'lucide-react'
import { Trans, useTranslation } from 'react-i18next'
import { RevealSection } from '@/components/RevealSection'
import { Button } from '@/components/ui/button'

export function Support() {
  const { t } = useTranslation('landing')

  return (
    <RevealSection id="support" className="text-center">
      <p className="text-xs font-semibold uppercase tracking-wide text-primary">{t('support.eyebrow')}</p>
      <div className="mt-3 flex items-center justify-center gap-2 text-foreground">
        <Coffee className="size-5 text-primary" aria-hidden="true" />
        <strong>{t('support.logo')}</strong>
      </div>
      <h2 className="mx-auto mt-4 max-w-xl text-2xl font-medium tracking-tight text-foreground">
        {t('support.title')}
      </h2>
      <div className="mx-auto mt-6 max-w-lg space-y-3 text-sm text-muted-foreground [&_a]:text-primary [&_a]:no-underline [&_a:hover]:underline">
        <p>
          <Trans
            i18nKey="support.p1"
            t={t}
            components={{ profile: <a href="https://github.com/josepacelli" /> }}
          />
        </p>
        <p>{t('support.p2')}</p>
        <p>{t('support.p3')}</p>
      </div>
      <Button
        render={<a href="https://ko-fi.com/Q3D5259MWT" target="_blank" rel="noopener" />}
        nativeButton={false}
        className="mt-6 bg-[#72a4f2] text-white hover:bg-[#72a4f2]/85"
      >
        <Coffee className="size-4" aria-hidden="true" />
        {t('hero.kofiSupport').replace(' ↓', '')}
      </Button>
    </RevealSection>
  )
}
