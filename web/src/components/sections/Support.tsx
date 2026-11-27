import { Coffee } from 'lucide-react'
import { Trans, useTranslation } from 'react-i18next'
import { PageSection } from '@/components/PageSection'

export function Support() {
  const { t } = useTranslation('landing')

  return (
    <PageSection id="support">
      <div className="relative overflow-hidden rounded-[28px] border border-border bg-card p-7 sm:p-10">
        <div
          aria-hidden="true"
          className="absolute inset-y-6 right-6 hidden w-[28%] bg-[radial-gradient(var(--grille)_2px,transparent_2.5px)] bg-[length:16px_16px] lg:block"
        />
        <h2 className="relative max-w-[40rem] text-balance font-heading text-[1.75rem] leading-[1.1] font-light tracking-[-0.02em] text-foreground sm:text-[2.5rem]">
          {t('support.title')}
        </h2>
        <div className="relative mt-5 max-w-[60ch] space-y-3 text-sm leading-relaxed text-muted-foreground [&_a]:text-accent [&_a]:no-underline [&_a:hover]:underline">
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
        <a
          href="https://ko-fi.com/Q3D5259MWT"
          target="_blank"
          rel="noopener"
          data-glass-interactive
          className="glass lg-button relative mt-6 px-6"
        >
          <Coffee className="size-4" aria-hidden="true" />
          <span>{t('support.cta')}</span>
        </a>
      </div>
    </PageSection>
  )
}
