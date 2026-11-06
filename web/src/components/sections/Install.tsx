import { Download } from 'lucide-react'
import type { ReactNode } from 'react'
import { Trans, useTranslation } from 'react-i18next'
import { PageSection } from '@/components/PageSection'
import { SectionHeading } from '@/components/SectionHeading'
import { Button } from '@/components/ui/button'

/** One side of the install pair, numbered like the hero's install bar. */
function InstallStep({
  n,
  title,
  desc,
  cta,
  href,
  note,
}: {
  n: number
  title: string
  desc: string
  cta: string
  href: string
  note: ReactNode
}) {
  return (
    <li className="border-t border-border pt-6">
      <div className="flex items-center gap-3">
        <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary font-heading text-sm font-semibold text-primary-foreground tabular-nums">
          {n}
        </span>
        <h3 className="font-heading text-2xl font-normal text-foreground">{title}</h3>
      </div>
      <p className="mt-3 max-w-[48ch] text-sm leading-relaxed text-muted-foreground">{desc}</p>
      <Button render={<a href={href} />} nativeButton={false} className="mt-5 h-11 rounded-full px-5">
        <Download className="size-4" aria-hidden="true" />
        <span className="tabular-nums">{cta}</span>
      </Button>
      <p className="mt-4 max-w-[52ch] text-xs leading-relaxed text-muted-foreground [&_a]:text-accent [&_a]:no-underline [&_a:hover]:underline">
        {note}
      </p>
    </li>
  )
}

export function Install() {
  const { t } = useTranslation('landing')

  return (
    <PageSection id="install">
      <SectionHeading title={t('install.title')} sub={t('install.sub')} />
      <ol className="grid gap-x-10 gap-y-10 sm:grid-cols-2">
        <InstallStep
          n={1}
          title={t('install.mac.title')}
          desc={t('install.mac.desc')}
          cta={t('install.mac.cta')}
          href="https://github.com/peetzweg/opendisplay/releases/latest"
          note={
            <Trans
              i18nKey="install.mac.note"
              t={t}
              components={{ compile: <a href="https://github.com/peetzweg/opendisplay" /> }}
            />
          }
        />
        <InstallStep
          n={2}
          title={t('install.android.title')}
          desc={t('install.android.desc')}
          cta={t('install.android.cta')}
          href="https://play.google.com/store/apps/details?id=io.github.josepacelli.opendisplay"
          note={
            <Trans
              i18nKey="install.android.note"
              t={t}
              components={{
                compile: <a href="https://github.com/josepacelli/opendisplay-android#build" />,
              }}
            />
          }
        />
      </ol>
    </PageSection>
  )
}
