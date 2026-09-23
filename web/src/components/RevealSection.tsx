import type { ReactNode } from 'react'
import { useScrollReveal } from '@/hooks/useScrollReveal'
import { cn } from '@/lib/utils'

export function RevealSection({
  id,
  className,
  children,
}: {
  id: string
  className?: string
  children: ReactNode
}) {
  const { ref, inView } = useScrollReveal<HTMLElement>()

  return (
    <section
      id={id}
      ref={ref}
      className={cn(
        'py-16 transition-all duration-500 ease-out motion-reduce:transition-none sm:py-24',
        inView ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0',
        className,
      )}
    >
      {children}
    </section>
  )
}
