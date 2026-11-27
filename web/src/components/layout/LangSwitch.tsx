import { Globe } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { LOCALES, localeHref, resolveLocale } from '@/lib/locales'

export function LangSwitch() {
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const current = resolveLocale(document.documentElement.lang)

  useEffect(() => {
    if (!open) return
    const onClick = (e: MouseEvent) => {
      if (!rootRef.current?.contains(e.target as Node)) setOpen(false)
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false)
    }
    document.addEventListener('mousedown', onClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('mousedown', onClick)
      document.removeEventListener('keydown', onKey)
    }
  }, [open])

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        aria-haspopup="true"
        aria-expanded={open}
        onClick={() => setOpen((v) => !v)}
        className="flex items-center gap-1.5 rounded-full px-3 py-1.5 text-sm whitespace-nowrap text-muted-foreground transition-colors hover:bg-[var(--lg-highlight)] hover:text-foreground"
      >
        <Globe className="size-4" aria-hidden="true" />
        <span>{current}</span>
      </button>
      {open && (
        <div className="absolute right-0 z-20 mt-2 min-w-44 rounded-2xl border border-border bg-popover/95 p-1.5 backdrop-blur-xl backdrop-saturate-150">
          {LOCALES.map((locale) => (
            <a
              key={locale.code}
              href={localeHref(locale.path, 'index')}
              aria-current={locale.code === current ? 'page' : undefined}
              className={
                'block rounded-xl px-3 py-1.5 text-sm no-underline ' +
                (locale.code === current
                  ? 'bg-secondary font-semibold text-foreground'
                  : 'text-muted-foreground hover:bg-secondary hover:text-foreground')
              }
            >
              {locale.label}
            </a>
          ))}
        </div>
      )}
    </div>
  )
}
