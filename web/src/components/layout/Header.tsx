import { Menu, Star, X } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useActiveSection } from '@/hooks/useActiveSection'
import { useGithubStars } from '@/hooks/useGithubStars'
import { cn } from '@/lib/utils'
import { LangSwitch } from './LangSwitch'
import { ThemeToggle } from './ThemeToggle'

const SECTION_IDS = ['features', 'how-it-works', 'screenshots', 'install', 'compare', 'faq']

export function Header() {
  const { t } = useTranslation('landing')
  const active = useActiveSection(SECTION_IDS)
  const stars = useGithubStars('josepacelli/opendisplay-android')
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!menuOpen) return
    const onClick = (e: MouseEvent) => {
      if (!menuRef.current?.contains(e.target as Node)) setMenuOpen(false)
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setMenuOpen(false)
    }
    /**
     * Listen on mousedown rather than click: toggling the button swaps the
     * icon (Menu -> X) before click bubbles to document, which would detach
     * the original event target and trigger a false outside-click close.
     */
    document.addEventListener('mousedown', onClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onClick)
      document.removeEventListener('keydown', onKey)
    }
  }, [menuOpen])

  const links: { id: string; label: string }[] = [
    { id: 'features', label: t('nav.features') },
    { id: 'how-it-works', label: t('nav.howItWorks') },
    { id: 'screenshots', label: t('nav.screenshots') },
    { id: 'install', label: t('nav.install') },
    { id: 'compare', label: t('nav.compare') },
    { id: 'faq', label: t('nav.faq') },
  ]

  return (
    <>
      <div
        className="lg-progressive-blur"
        data-direction="top"
        data-blur="16"
        data-layers="5"
        style={{ position: 'fixed', height: '84px', zIndex: 25 }}
      />
      <header ref={menuRef} className="sticky top-3 z-30 mx-auto max-w-[1200px] px-4 sm:px-6">
        <div
          className="glass flex items-center justify-between gap-3 rounded-full px-4 py-2 sm:px-5"
          data-glass-refraction="16"
          data-glass-bezel="18"
        >
          <a href="#" className="flex shrink-0 items-center gap-2 whitespace-nowrap font-heading text-base font-medium text-foreground no-underline">
            <img src="/logo-mark.png" alt="" className="size-7 rounded-lg ring-1 ring-border/60" aria-hidden="true" />
            <span>{t('brand.name')}</span>
            <span className="hidden text-muted-foreground min-[400px]:inline">{t('brand.sub')}</span>
          </a>

          <nav className="hidden items-center gap-0.5 xl:flex">
            {links.map((link) => {
              const isActive = active === link.id
              return (
                <a
                  key={link.id}
                  href={`#${link.id}`}
                  className={cn(
                    'shrink-0 rounded-full px-2.5 py-1.5 text-[13.5px] whitespace-nowrap no-underline transition-colors',
                    isActive
                      ? 'bg-[var(--lg-highlight)] font-medium text-foreground'
                      : 'text-muted-foreground hover:bg-[var(--lg-highlight)] hover:text-foreground',
                  )}
                >
                  {link.label}
                </a>
              )
            })}
          </nav>

          <div className="flex shrink-0 items-center gap-1">
            <a
              href="https://github.com/josepacelli/opendisplay-android"
              className="hidden shrink-0 items-center gap-1.5 rounded-full bg-[var(--lg-highlight)] px-3 py-1.5 text-[13px] font-medium whitespace-nowrap text-foreground no-underline transition-colors hover:bg-secondary xl:inline-flex"
            >
              <Star className="size-3.5 text-accent" aria-hidden="true" />
              <span>{t('nav.github')}</span>
              <span className="text-muted-foreground tabular-nums">{stars ?? '···'}</span>
            </a>
            <ThemeToggle />
            <LangSwitch />

            <div className="relative xl:hidden">
              <button
                type="button"
                aria-haspopup="true"
                aria-expanded={menuOpen}
                aria-label={menuOpen ? t('nav.closeMenu') : t('nav.openMenu')}
                onClick={() => setMenuOpen((v) => !v)}
                className="flex size-9 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-[var(--lg-highlight)] hover:text-foreground"
              >
                {menuOpen ? (
                  <X className="size-5" aria-hidden="true" />
                ) : (
                  <Menu className="size-5" aria-hidden="true" />
                )}
              </button>
            </div>
          </div>
        </div>

        {menuOpen && (
          <div
            className="glass absolute right-4 z-30 mt-2 w-60 rounded-3xl p-2 sm:right-6 xl:hidden"
            data-glass-refraction="18"
            data-glass-bezel="20"
          >
            {links.map((link) => (
              <a
                key={link.id}
                href={`#${link.id}`}
                onClick={() => setMenuOpen(false)}
                className={cn(
                  'flex items-center justify-between rounded-2xl px-3.5 py-2.5 text-sm no-underline transition-colors hover:bg-[var(--lg-highlight)]',
                  active === link.id
                    ? 'bg-[var(--lg-highlight)] font-medium text-foreground'
                    : 'text-muted-foreground hover:text-foreground',
                )}
              >
                <span>{link.label}</span>
                {active === link.id && <span className="size-1.5 rounded-full bg-accent" aria-hidden="true" />}
              </a>
            ))}
            <a
              href="https://github.com/josepacelli/opendisplay-android"
              className="mt-1.5 flex items-center justify-between rounded-2xl border-t border-border/40 px-3.5 py-2.5 pt-3 text-sm font-medium text-foreground no-underline transition-colors hover:bg-[var(--lg-highlight)]"
            >
              <span className="flex items-center gap-2">
                <Star className="size-4 text-accent" aria-hidden="true" />
                {t('nav.github')}
              </span>
              <span className="text-muted-foreground tabular-nums">{stars ?? '···'}</span>
            </a>
          </div>
        )}
      </header>
    </>
  )
}
