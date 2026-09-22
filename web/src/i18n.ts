import i18next from 'i18next'
import { initReactI18next } from 'react-i18next'
import landingPtBR from './locales/pt-BR/landing.json'

i18next.use(initReactI18next).init({
  lng: 'pt-BR',
  fallbackLng: 'pt-BR',
  resources: {
    'pt-BR': { landing: landingPtBR },
  },
  interpolation: { escapeValue: false },
})

export default i18next
