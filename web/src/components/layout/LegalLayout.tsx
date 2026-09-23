import type { ReactNode } from 'react'
import { LOCALES, type PageId, localeHref, resolveLocale } from '@/lib/locales'

const SHORT_LABELS: Record<string, string> = {
  en: 'EN',
  es: 'ES',
  'pt-BR': 'PT-BR',
  'pt-PT': 'PT-PT',
  'zh-Hans': '中文',
  ja: '日本語',
  ko: '한국어',
}

export function LegalLayout({
  title,
  subtitle,
  page,
  backLabel,
  children,
}: {
  title: string
  subtitle: string
  page: PageId
  backLabel: string
  children: ReactNode
}) {
  const current = resolveLocale(document.documentElement.lang)

  return (
    <div className="mx-auto max-w-2xl px-6 py-14 sm:py-20">
      <h1 className="text-3xl font-semibold tracking-tight text-foreground">{title}</h1>
      <p className="mt-1.5 mb-10 text-sm text-muted-foreground">{subtitle}</p>

      <div className="space-y-9 text-[0.95rem] leading-relaxed text-muted-foreground [&_a]:text-primary [&_a]:no-underline [&_a:hover]:underline [&_code]:rounded [&_code]:border [&_code]:border-border [&_code]:bg-secondary [&_code]:px-1.5 [&_code]:py-0.5 [&_code]:font-mono [&_code]:text-[0.85em] [&_code]:text-foreground [&_pre]:overflow-x-auto [&_pre]:rounded-md [&_pre]:border [&_pre]:border-border [&_pre]:bg-secondary [&_pre]:p-3.5 [&_pre_code]:border-0 [&_pre_code]:bg-transparent [&_pre_code]:p-0 [&_h2]:mb-2.5 [&_h2]:mt-0 [&_h2]:text-lg [&_h2]:font-semibold [&_h2]:text-foreground [&_ul]:list-disc [&_ul]:space-y-1.5 [&_ul]:pl-5 [&_ol]:list-decimal [&_ol]:space-y-1.5 [&_ol]:pl-5 [&_table]:w-full [&_table]:border-collapse [&_th]:border-b [&_th]:border-border [&_th]:py-2 [&_th]:pr-3 [&_th]:text-left [&_th]:text-xs [&_th]:font-semibold [&_th]:uppercase [&_th]:tracking-wide [&_th]:text-muted-foreground [&_td]:border-b [&_td]:border-border [&_td]:py-2 [&_td]:pr-3 [&_td]:align-top">
        {children}
      </div>

      <div className="mt-14 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs">
        {LOCALES.map((locale) =>
          locale.code === current ? (
            <span key={locale.code} aria-current="page" className="font-semibold text-foreground">
              {SHORT_LABELS[locale.code]}
            </span>
          ) : (
            <a
              key={locale.code}
              href={localeHref(locale.path, page)}
              className="text-muted-foreground no-underline hover:text-foreground"
            >
              {SHORT_LABELS[locale.code]}
            </a>
          ),
        )}
      </div>

      <a href={localeHref(LOCALES.find((l) => l.code === current)!.path, 'index')} className="mt-4 inline-block text-sm text-primary no-underline hover:underline">
        ← {backLabel}
      </a>
    </div>
  )
}
