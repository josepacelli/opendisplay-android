import { Globe } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'

const LANGS = [
  { href: '/en/', label: 'English' },
  { href: '/es/', label: 'Español' },
  { href: '/', label: 'Português (Brasil)', current: true },
  { href: '/pt-PT/', label: 'Português (Portugal)' },
  { href: '/zh-Hans/', label: '中文' },
  { href: '/ja/', label: '日本語' },
  { href: '/ko/', label: '한국어' },
]

export function LangSwitch() {
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)

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
        <span>PT-BR</span>
      </button>
      {open && (
        <div className="absolute right-0 z-20 mt-1 min-w-44 rounded-md border border-border bg-popover p-1 shadow-md">
          {LANGS.map((lang) => (
            <a
              key={lang.href}
              href={lang.href}
              aria-current={lang.current ? 'page' : undefined}
              className={
                'block rounded-sm px-2.5 py-1.5 text-sm no-underline ' +
                (lang.current
                  ? 'font-semibold text-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-foreground')
              }
            >
              {lang.label}
            </a>
          ))}
        </div>
      )}
    </div>
  )
}
