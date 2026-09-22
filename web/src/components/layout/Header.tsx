import { MonitorSmartphone, Star } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useActiveSection } from '@/hooks/useActiveSection'
import { useGithubStars } from '@/hooks/useGithubStars'
import { cn } from '@/lib/utils'
import { LangSwitch } from './LangSwitch'

const SECTION_IDS = ['features', 'how-it-works', 'screenshots', 'install', 'compare', 'faq']

export function Header() {
  const { t } = useTranslation('landing')
  const active = useActiveSection(SECTION_IDS)
  const stars = useGithubStars('josepacelli/opendisplay-android')

  const links: { id: string; label: string }[] = [
    { id: 'features', label: t('nav.features') },
    { id: 'how-it-works', label: t('nav.howItWorks') },
    { id: 'screenshots', label: t('nav.screenshots') },
    { id: 'install', label: t('nav.install') },
    { id: 'compare', label: t('nav.compare') },
    { id: 'faq', label: t('nav.faq') },
  ]

  return (
    <header className="sticky top-0 z-30 border-b border-border bg-background/90 backdrop-blur-sm">
      <div className="mx-auto flex max-w-[1126px] items-center gap-4 px-6 py-3.5">
        <div className="flex items-center gap-2 whitespace-nowrap font-medium text-foreground">
          <MonitorSmartphone className="size-5 text-primary" aria-hidden="true" />
          {t('brand.name')} <span className="text-muted-foreground">{t('brand.sub')}</span>
        </div>

        <nav className="flex min-w-0 flex-1 items-center gap-5 overflow-x-auto [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
          {links.map((link) => (
            <a
              key={link.id}
              href={`#${link.id}`}
              className={cn(
                'shrink-0 text-sm text-muted-foreground no-underline transition-colors hover:text-foreground',
                active === link.id && 'font-medium text-foreground',
              )}
            >
              {link.label}
            </a>
          ))}
          <a
            href="https://github.com/josepacelli/opendisplay-android"
            className="ml-auto flex shrink-0 items-center gap-1.5 rounded-md border border-border px-2.5 py-1.5 text-sm text-foreground no-underline transition-colors hover:bg-accent"
          >
            <Star className="size-4" aria-hidden="true" />
            {t('nav.github')} <span className="text-muted-foreground">{stars ?? '···'}</span>
          </a>
        </nav>

        <LangSwitch />
      </div>
    </header>
  )
}
