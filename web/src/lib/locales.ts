export type PageId = 'index' | 'code-of-conduct' | 'contributing' | 'privacy'

export type Locale = {
  code: string
  label: string
  /** Empty string for pt-BR, which lives at the site root. */
  path: string
}

export const LOCALES: Locale[] = [
  { code: 'en', label: 'English', path: 'en' },
  { code: 'es', label: 'Español', path: 'es' },
  { code: 'pt-BR', label: 'Português (Brasil)', path: '' },
  { code: 'pt-PT', label: 'Português (Portugal)', path: 'pt-PT' },
  { code: 'zh-Hans', label: '中文', path: 'zh-Hans' },
  { code: 'ja', label: '日本語', path: 'ja' },
  { code: 'ko', label: '한국어', path: 'ko' },
]

export const DEFAULT_LOCALE = 'pt-BR'

export function resolveLocale(lang: string | undefined | null): string {
  return LOCALES.some((l) => l.code === lang) ? (lang as string) : DEFAULT_LOCALE
}

export function localeHref(localePath: string, page: PageId): string {
  const file = page === 'index' ? '' : `${page}.html`
  return localePath ? `/${localePath}/${file}` : `/${file}`
}
