import { useCallback, useState } from 'react'

export type Theme = 'light' | 'dark' | 'auto'

const STORAGE_KEY = 'theme'
export const THEME_ORDER: Theme[] = ['light', 'dark', 'auto']

function applyTheme(theme: Theme) {
  if (theme === 'auto') {
    document.documentElement.removeAttribute('data-theme')
  } else {
    document.documentElement.setAttribute('data-theme', theme)
  }
}

function readStoredTheme(): Theme {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'light' || stored === 'dark' || stored === 'auto') return stored
  } catch {
    // localStorage indisponível (aba anônima, storage bloqueado) — cai pro padrão
  }
  return 'auto'
}

// Roda assim que o módulo é importado (todo entry point importa este arquivo),
// antes do primeiro render — evita flash do tema errado em qualquer página.
applyTheme(readStoredTheme())

export function useTheme() {
  const [theme, setThemeState] = useState<Theme>(readStoredTheme)

  const setTheme = useCallback((next: Theme) => {
    applyTheme(next)
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      // ignora — a preferência só não persiste nesta sessão
    }
    setThemeState(next)
  }, [])

  const cycle = useCallback(() => {
    setTheme(THEME_ORDER[(THEME_ORDER.indexOf(theme) + 1) % THEME_ORDER.length])
  }, [theme, setTheme])

  return { theme, setTheme, cycle }
}
