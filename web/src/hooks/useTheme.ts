import { useCallback, useState } from 'react'
import '@/lib/liquid-glass.js'

export type Theme = 'light' | 'dark' | 'auto'

const STORAGE_KEY = 'theme'
export const THEME_ORDER: Theme[] = ['light', 'dark', 'auto']

function applyTheme(theme: Theme) {
  if (theme === 'auto') {
    document.documentElement.removeAttribute('data-theme')
  } else {
    document.documentElement.setAttribute('data-theme', theme)
  }
  document.documentElement.setAttribute('data-glass-appearance', theme)
}

function readStoredTheme(): Theme {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'light' || stored === 'dark' || stored === 'auto') return stored
  } catch {
    return 'auto'
  }
  return 'auto'
}

/**
 * Apply the stored theme as soon as this module is imported by any entry point,
 * before the first React render, preventing a wrong-theme flash on load.
 */
applyTheme(readStoredTheme())

export function useTheme() {
  const [theme, setThemeState] = useState<Theme>(readStoredTheme)

  const setTheme = useCallback((next: Theme) => {
    applyTheme(next)
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      /* Preference stays in memory for the current session when storage is blocked. */
    }
    setThemeState(next)
  }, [])

  const cycle = useCallback(() => {
    setTheme(THEME_ORDER[(THEME_ORDER.indexOf(theme) + 1) % THEME_ORDER.length])
  }, [theme, setTheme])

  return { theme, setTheme, cycle }
}
