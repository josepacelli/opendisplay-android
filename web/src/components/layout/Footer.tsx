import { useTranslation } from 'react-i18next'

const LANGS = [
  { href: '/en/', label: 'EN' },
  { href: '/es/', label: 'ES' },
  { href: '/pt-PT/', label: 'PT-PT' },
  { href: '/zh-Hans/', label: '中文' },
  { href: '/ja/', label: '日本語' },
  { href: '/ko/', label: '한국어' },
]

export function Footer() {
  const { t } = useTranslation('landing')

  return (
    <footer className="border-t border-border">
      <div className="mx-auto max-w-[1126px] px-6 py-10 text-center">
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
          <span aria-current="page" className="font-semibold text-foreground">
            PT-BR
          </span>
          {LANGS.map((lang) => (
            <a key={lang.href} href={lang.href} className="text-muted-foreground no-underline hover:text-foreground">
              {lang.label}
            </a>
          ))}
        </div>

        <p className="mt-5 text-xs text-muted-foreground">{t('footer.disclaimer')}</p>
      </div>
    </footer>
  )
}
