import { Monitor, Moon, Sun } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import type { Theme } from '@/hooks/useTheme'
import { useTheme } from '@/hooks/useTheme'
import { cn } from '@/lib/utils'

const ICONS: Record<Theme, typeof Sun> = { light: Sun, dark: Moon, auto: Monitor }

export function ThemeToggle() {
  const { t } = useTranslation('landing')
  const { theme, cycle } = useTheme()

  return (
    <button
      type="button"
      onClick={cycle}
      aria-label={t(`theme.${theme}`)}
      title={t(`theme.${theme}`)}
      className="relative flex size-9 shrink-0 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-accent/60 hover:text-foreground"
    >
      {(Object.entries(ICONS) as [Theme, typeof Sun][]).map(([mode, Icon]) => (
        <Icon
          key={mode}
          aria-hidden="true"
          className={cn(
            'absolute size-[18px] transition-all duration-300 ease-out motion-reduce:transition-none',
            theme === mode ? 'scale-100 rotate-0 opacity-100' : 'scale-0 rotate-90 opacity-0',
          )}
        />
      ))}
    </button>
  )
}
