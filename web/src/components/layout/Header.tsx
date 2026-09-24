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
    // 'mousedown', não 'click': o clique que abre o menu já troca o ícone
    // (Menu -> X) antes do 'click' borbulhar até o document, então o alvo
    // original deixa de existir na árvore e "contains" dá falso positivo de
    // clique-fora, fechando o menu na hora que ele abre.
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
    <header className="sticky top-0 z-30 border-b border-border/40 bg-background/60 backdrop-blur-xl backdrop-saturate-150">
      <div className="mx-auto flex max-w-7xl items-center gap-4 px-6 py-3.5">
        <div className="flex items-center gap-2 whitespace-nowrap font-medium text-foreground">
          <img src="/logo-mark.png" alt="" className="size-6 rounded-md" aria-hidden="true" />
          {t('brand.name')} <span className="text-muted-foreground">{t('brand.sub')}</span>
        </div>

        <nav className="hidden min-w-0 flex-1 items-center gap-5 xl:flex">
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
            className="ml-auto flex shrink-0 items-center gap-1.5 rounded-md border border-border/60 bg-background/40 px-2.5 py-1.5 text-sm text-foreground no-underline backdrop-blur-sm transition-colors hover:bg-accent/60"
          >
            <Star className="size-4" aria-hidden="true" />
            {t('nav.github')} <span className="text-muted-foreground">{stars ?? '···'}</span>
          </a>
        </nav>

        <div className="ml-auto flex items-center gap-1 xl:ml-0">
          <ThemeToggle />
          <LangSwitch />

          <div ref={menuRef} className="relative xl:hidden">
            <button
              type="button"
              aria-haspopup="true"
              aria-expanded={menuOpen}
              aria-label={menuOpen ? t('nav.closeMenu') : t('nav.openMenu')}
              onClick={() => setMenuOpen((v) => !v)}
              className="flex items-center justify-center rounded-md p-2 text-muted-foreground transition-colors hover:bg-accent/60 hover:text-foreground"
            >
              {menuOpen ? (
                <X className="size-5" aria-hidden="true" />
              ) : (
                <Menu className="size-5" aria-hidden="true" />
              )}
            </button>

            {menuOpen && (
              <div className="absolute right-0 z-20 mt-2 w-56 rounded-lg border border-border/60 bg-background/80 p-1.5 shadow-lg backdrop-blur-xl backdrop-saturate-150">
                {links.map((link) => (
                  <a
                    key={link.id}
                    href={`#${link.id}`}
                    onClick={() => setMenuOpen(false)}
                    className={cn(
                      'block rounded-md px-3 py-2 text-sm no-underline transition-colors hover:bg-accent/60',
                      active === link.id
                        ? 'font-medium text-foreground'
                        : 'text-muted-foreground hover:text-foreground',
                    )}
                  >
                    {link.label}
                  </a>
                ))}
                <a
                  href="https://github.com/josepacelli/opendisplay-android"
                  className="mt-1 flex items-center gap-1.5 border-t border-border/60 px-3 py-2 pt-2.5 text-sm text-foreground no-underline transition-colors hover:bg-accent/60"
                >
                  <Star className="size-4" aria-hidden="true" />
                  {t('nav.github')} <span className="text-muted-foreground">{stars ?? '···'}</span>
                </a>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}
