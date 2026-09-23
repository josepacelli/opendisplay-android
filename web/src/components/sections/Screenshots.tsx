import { useTranslation } from 'react-i18next'
import { RevealSection } from '@/components/RevealSection'
import { SectionHeading } from '@/components/SectionHeading'

const FILES = [
  'tablet-mirroring-site.jpeg',
  'mac-arrange-displays.jpeg',
  'app-home.jpeg',
  'app-settings.jpeg',
  'app-mirroring.jpeg',
]

export function Screenshots() {
  const { t } = useTranslation('landing')
  const items = t('screenshots.items', { returnObjects: true }) as { alt: string; caption: string }[]

  return (
    <RevealSection id="screenshots">
      <SectionHeading eyebrow={t('screenshots.eyebrow')} title={t('screenshots.title')} sub={t('screenshots.sub')} />
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((item, i) => (
          <figure key={FILES[i]} className="overflow-hidden rounded-lg border border-border">
            <img src={`/screenshots/${FILES[i]}`} alt={item.alt} loading="lazy" className="aspect-[4/3] w-full object-cover" />
            <figcaption className="p-3 text-left text-sm text-muted-foreground">{item.caption}</figcaption>
          </figure>
        ))}
      </div>
    </RevealSection>
  )
}
