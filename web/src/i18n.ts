import i18next from 'i18next'
import { initReactI18next } from 'react-i18next'
import { resolveLocale } from '@/lib/locales'
import landingEn from './locales/en/landing.json'
import landingEs from './locales/es/landing.json'
import landingJa from './locales/ja/landing.json'
import landingKo from './locales/ko/landing.json'
import landingPtBR from './locales/pt-BR/landing.json'
import landingPtPT from './locales/pt-PT/landing.json'
import landingZhHans from './locales/zh-Hans/landing.json'

i18next.use(initReactI18next).init({
  lng: resolveLocale(document.documentElement.lang),
  fallbackLng: 'pt-BR',
  resources: {
    'pt-BR': { landing: landingPtBR },
    en: { landing: landingEn },
    es: { landing: landingEs },
    'pt-PT': { landing: landingPtPT },
    'zh-Hans': { landing: landingZhHans },
    ja: { landing: landingJa },
    ko: { landing: landingKo },
  },
  interpolation: { escapeValue: false },
})

export default i18next
