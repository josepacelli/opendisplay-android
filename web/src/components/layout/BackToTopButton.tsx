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
      data-glass-interactive
      className={cn(
        'glass lg-button lg-icon fixed right-6 bottom-6 z-30 transition-transform duration-200',
        visible ? 'translate-y-0 scale-100' : 'pointer-events-none translate-y-2 scale-0',
      )}
    >
      <ArrowUp className="size-5" aria-hidden="true" />
    </button>
  )
}
