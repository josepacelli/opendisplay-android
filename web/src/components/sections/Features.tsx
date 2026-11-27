import {
  Cable,
  Hand,
  MousePointer2,
  PictureInPicture2,
  RefreshCw,
  ScanEye,
  ShieldCheck,
  Sliders,
  Video,
  VenetianMask,
  ZoomIn,
} from 'lucide-react'
import type { ComponentType } from 'react'
import { Trans, useTranslation } from 'react-i18next'
import { PageSection } from '@/components/PageSection'
import { SectionHeading } from '@/components/SectionHeading'

const ICONS: ComponentType<{ className?: string }>[] = [
  Video,
  ScanEye,
  Hand,
  ZoomIn,
  MousePointer2,
  ShieldCheck,
  RefreshCw,
  Sliders,
  Cable,
  PictureInPicture2,
  VenetianMask,
]

export function Features() {
  const { t } = useTranslation('landing')
  const items = t('features.items', { returnObjects: true }) as { title: string; desc: string }[]

  return (
    <PageSection id="features">
      <SectionHeading title={t('features.title')} sub={t('features.sub')} />
      <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((item, i) => {
          const Icon = ICONS[i]
          const isWide = i === 3
          return (
            <li
              key={item.title}
              className={
                'flex flex-col rounded-2xl border border-border bg-card p-6 transition-colors hover:border-foreground/25 ' +
                (isWide ? 'lg:col-span-2' : '')
              }
            >
              <span className="flex size-10 items-center justify-center rounded-xl bg-secondary text-foreground">
                <Icon className="size-5" aria-hidden="true" />
              </span>
              <h3 className="mt-4 font-heading text-lg font-medium text-foreground">{item.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-muted-foreground [&_code]:rounded [&_code]:border [&_code]:border-border [&_code]:bg-secondary [&_code]:px-1.5 [&_code]:py-0.5 [&_code]:font-mono [&_code]:text-[0.85em] [&_code]:text-foreground">
                <Trans
                  i18nKey={`features.items.${i}.desc`}
                  t={t}
                  components={{ code: <code />, b: <b className="font-medium text-foreground" /> }}
                />
              </p>
            </li>
          )
        })}
      </ul>
    </PageSection>
  )
}
