import { useTranslation } from 'react-i18next'
import { LOCALES, localeHref, resolveLocale } from '@/lib/locales'

const SHORT_LABELS: Record<string, string> = {
  en: 'EN',
  es: 'ES',
  'pt-BR': 'PT-BR',
  'pt-PT': 'PT-PT',
  'zh-Hans': '中文',
  ja: '日本語',
  ko: '한국어',
}

export function Footer() {
  const { t } = useTranslation('landing')
  const current = resolveLocale(document.documentElement.lang)

  return (
    <footer className="border-t border-border">
      <div className="mx-auto max-w-[920px] px-6 py-10 text-center">
        <div className="flex flex-wrap justify-center gap-x-5 gap-y-2 text-sm">
          <a className="text-muted-foreground no-underline hover:text-foreground" href="https://github.com/josepacelli/opendisplay-android">
            {t('footer.links.github')}
          </a>
          <a className="text-muted-foreground no-underline hover:text-foreground" href="https://github.com/josepacelli/opendisplay-android/releases">
            {t('footer.links.releases')}
          </a>
          <a className="text-muted-foreground no-underline hover:text-foreground" href="https://github.com/josepacelli/opendisplay-android/issues">
            {t('footer.links.issues')}
          </a>
          <a className="text-muted-foreground no-underline hover:text-foreground" href="https://github.com/josepacelli/opendisplay-android/blob/main/LICENSE">
            {t('footer.links.license')}
          </a>
          <a className="text-muted-foreground no-underline hover:text-foreground" href="code-of-conduct.html">
            {t('footer.links.codeOfConduct')}
          </a>
          <a className="text-muted-foreground no-underline hover:text-foreground" href="contributing.html">
            {t('footer.links.contributing')}
          </a>
          <a className="text-muted-foreground no-underline hover:text-foreground" href="privacy.html">
            {t('footer.links.privacy')}
          </a>
          <a className="text-muted-foreground no-underline hover:text-foreground" href="https://opendisplay.app/">
            {t('footer.links.original')}
          </a>
        </div>

        <div className="mt-4 flex flex-wrap justify-center gap-x-4 gap-y-2 text-xs">
          {LOCALES.map((locale) =>
            locale.code === current ? (
              <span key={locale.code} aria-current="page" className="font-semibold text-foreground">
                {SHORT_LABELS[locale.code]}
              </span>
            ) : (
              <a
                key={locale.code}
                href={localeHref(locale.path, 'index')}
                className="text-muted-foreground no-underline hover:text-foreground"
              >
                {SHORT_LABELS[locale.code]}
              </a>
            ),
          )}
        </div>

        <p className="mt-5 text-xs text-muted-foreground">{t('footer.disclaimer')}</p>
      </div>
    </footer>
  )
}
