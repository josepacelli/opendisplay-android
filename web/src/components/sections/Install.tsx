import { Download } from 'lucide-react'
import type { ReactNode } from 'react'
import { Trans, useTranslation } from 'react-i18next'
import { RevealSection } from '@/components/RevealSection'
import { SectionHeading } from '@/components/SectionHeading'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'

function InstallCard({
  tag,
  title,
  desc,
  cta,
  href,
  note,
}: {
  tag: string
  title: string
  desc: string
  cta: string
  href: string
  note: ReactNode
}) {
  return (
    <Card>
      <CardContent>
        <p className="text-xs font-semibold tracking-wide text-primary">{tag}</p>
        <h3 className="mt-2 text-lg font-medium text-foreground">{title}</h3>
        <p className="mt-1.5 text-sm text-muted-foreground">{desc}</p>
        <Button render={<a href={href} />} nativeButton={false} className="mt-4">
          <Download className="size-4" aria-hidden="true" />
          {cta}
        </Button>
        <p className="mt-3 text-xs text-muted-foreground [&_a]:text-primary [&_a]:no-underline [&_a:hover]:underline">
          {note}
        </p>
      </CardContent>
    </Card>
  )
}

export function Install() {
  const { t } = useTranslation('landing')

  return (
    <RevealSection id="install">
      <SectionHeading eyebrow={t('install.eyebrow')} title={t('install.title')} sub={t('install.sub')} />
      <div className="grid gap-4 sm:grid-cols-2">
        <InstallCard
          tag={t('install.mac.tag')}
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
        <InstallCard
          tag={t('install.android.tag')}
          title={t('install.android.title')}
          desc={t('install.android.desc')}
          cta={t('install.android.cta')}
          href="https://github.com/josepacelli/opendisplay-android/releases/latest"
          note={
            <Trans
              i18nKey="install.android.note"
              t={t}
              components={{
                mail: <a href="mailto:josepacelli@gmail.com" />,
                compile: <a href="https://github.com/josepacelli/opendisplay-android#build" />,
                releases: <a href="https://github.com/josepacelli/opendisplay-android/releases" />,
              }}
            />
          }
        />
      </div>
    </RevealSection>
  )
}
