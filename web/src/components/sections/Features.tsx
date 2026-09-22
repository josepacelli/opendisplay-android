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
import { RevealSection } from '@/components/RevealSection'
import { SectionHeading } from '@/components/SectionHeading'
import { Card, CardContent } from '@/components/ui/card'

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
    <RevealSection id="features">
      <SectionHeading eyebrow={t('features.eyebrow')} title={t('features.title')} sub={t('features.sub')} />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((item, i) => {
          const Icon = ICONS[i]
          return (
            <Card key={item.title}>
              <CardContent className="flex gap-3">
                <Icon className="size-5 shrink-0 text-primary" />
                <div>
                  <h4 className="text-sm font-medium text-foreground">{item.title}</h4>
                  <p className="mt-1 text-sm text-muted-foreground">
                    <Trans i18nKey={`features.items.${i}.desc`} t={t} components={{ code: <code /> }} />
                  </p>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>
    </RevealSection>
  )
}
