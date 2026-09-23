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
    document.addEventListener('click', onClick)
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('click', onClick)
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
        className="flex items-center gap-1.5 rounded-md px-2 py-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
      >
        <Globe className="size-4" aria-hidden="true" />
        <span>{current}</span>
      </button>
      {open && (
        <div className="absolute right-0 z-20 mt-1 min-w-44 rounded-md border border-border bg-popover p-1 shadow-md">
          {LOCALES.map((locale) => (
            <a
              key={locale.code}
              href={localeHref(locale.path, 'index')}
              aria-current={locale.code === current ? 'page' : undefined}
              className={
                'block rounded-sm px-2.5 py-1.5 text-sm no-underline ' +
                (locale.code === current
                  ? 'font-semibold text-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-foreground')
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
