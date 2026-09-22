import { ArrowUp } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useBackToTop } from '@/hooks/useBackToTop'
import { cn } from '@/lib/utils'

export function BackToTopButton() {
  const { t } = useTranslation('landing')
  const { visible, scrollToTop } = useBackToTop()

  return (
    <button
      type="button"
      aria-label={t('backToTop')}
      onClick={scrollToTop}
      className={cn(
        'fixed bottom-6 right-6 z-30 flex size-10 items-center justify-center rounded-full border border-border bg-background text-foreground shadow-md transition-all duration-200 hover:bg-accent',
        visible ? 'translate-y-0 opacity-100' : 'pointer-events-none translate-y-2 opacity-0',
      )}
    >
      <ArrowUp className="size-5" aria-hidden="true" />
    </button>
  )
}
