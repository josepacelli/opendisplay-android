import { Maximize2, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { PageSection } from '@/components/PageSection'
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
  const [activeIndex, setActiveIndex] = useState<number | null>(null)

  useEffect(() => {
    if (activeIndex === null) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setActiveIndex(null)
    }
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [activeIndex])

  const activeItem = activeIndex !== null ? items[activeIndex] : null
  const activeFile = activeIndex !== null ? FILES[activeIndex] : null

  return (
    <PageSection id="screenshots">
      <SectionHeading title={t('screenshots.title')} sub={t('screenshots.sub')} />
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((item, i) => {
          const isFeatured = i === 0
          return (
            <figure
              key={FILES[i]}
              className={
                'group flex flex-col overflow-hidden rounded-2xl border border-border bg-card transition-colors hover:border-foreground/30 ' +
                (isFeatured ? 'sm:col-span-2 lg:col-span-2' : '')
              }
            >
              <button
                type="button"
                onClick={() => setActiveIndex(i)}
                aria-label={item.alt}
                className="relative block w-full cursor-pointer overflow-hidden bg-background text-left focus-visible:outline-none"
              >
                <img
                  src={`/screenshots/${FILES[i]}`}
                  alt={item.alt}
                  loading="lazy"
                  className={
                    'w-full object-cover transition-transform duration-300 ease-out group-hover:scale-[1.02] motion-reduce:transition-none ' +
                    (isFeatured ? 'aspect-[16/9]' : 'aspect-[4/3]')
                  }
                />
                <span
                  aria-hidden="true"
                  data-glass="clear"
                  data-glass-dim
                  data-glass-refraction="10"
                  data-glass-bezel="8"
                  className="
                    glass absolute top-3 right-3 flex size-9 scale-0 items-center justify-center rounded-full
                    text-white transition-transform duration-200 group-hover:scale-100 group-focus-visible:scale-100
                  "
                >
                  <Maximize2 className="size-4" />
                </span>
              </button>
              <figcaption className="flex flex-1 items-center p-4 text-left text-sm leading-relaxed text-muted-foreground">
                {item.caption}
              </figcaption>
            </figure>
          )
        })}
      </div>

      {activeItem && activeFile && (
        <div
          role="dialog"
          aria-modal="true"
          aria-label={activeItem.alt}
          onClick={() => setActiveIndex(null)}
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#0a101e]/85 p-4 sm:p-8"
        >
          <div
            onClick={(e) => e.stopPropagation()}
            className="relative max-h-[90svh] w-full max-w-5xl overflow-hidden rounded-2xl border border-border bg-card"
          >
            <button
              type="button"
              aria-label={t('nav.closeMenu')}
              onClick={() => setActiveIndex(null)}
              data-glass="clear"
              data-glass-dim
              data-glass-interactive
              className="glass lg-button lg-icon absolute top-3 right-3 z-10 text-white"
            >
              <X className="size-5" aria-hidden="true" />
            </button>
            <img
              src={`/screenshots/${activeFile}`}
              alt={activeItem.alt}
              className="max-h-[76svh] w-full bg-[#0a101e] object-contain"
            />
            <p className="border-t border-border px-5 py-3.5 text-sm text-foreground">{activeItem.caption}</p>
          </div>
        </div>
      )}
    </PageSection>
  )
}
