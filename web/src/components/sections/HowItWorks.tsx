import { Laptop, TabletSmartphone } from 'lucide-react'
import type { ComponentType } from 'react'
import { useTranslation } from 'react-i18next'
import { PageSection } from '@/components/PageSection'
import { SectionHeading } from '@/components/SectionHeading'

/** One side of the connection, as the protocol sees it: a hardware bento card with that end's pipeline steps. */
function DiagramColumn({
  icon: Icon,
  heading,
  headingSub,
  steps,
  liveIndicator,
}: {
  icon: ComponentType<{ className?: string }>
  heading: string
  headingSub: string
  steps: string[]
  liveIndicator?: boolean
}) {
  return (
    <div className="flex flex-col rounded-2xl border border-border bg-card p-6 sm:p-7">
      <div className="flex items-center gap-3.5">
        <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-secondary text-foreground">
          <Icon className="size-5" aria-hidden="true" />
        </span>
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <p className="font-heading text-lg font-medium text-foreground">{heading}</p>
            {liveIndicator && <span className="size-2 rounded-full bg-accent" aria-hidden="true" />}
          </div>
          <p className="font-sans text-xs text-muted-foreground">{headingSub}</p>
        </div>
      </div>
      <ol className="mt-5 space-y-2.5 font-mono text-[0.8rem] leading-relaxed">
        {steps.map((step, i) => (
          <li
            key={i}
            className="flex items-center gap-3 rounded-xl border border-border/70 bg-background/60 px-3.5 py-2.5 text-foreground"
          >
            <span className="font-sans text-xs font-medium text-muted-foreground tabular-nums">
              {String(i + 1).padStart(2, '0')}
            </span>
            <span className="min-w-0 flex-1 break-words">{step}</span>
          </li>
        ))}
      </ol>
    </div>
  )
}

export function HowItWorks() {
  const { t } = useTranslation('landing')
  const macSteps = t('howItWorks.mac.steps', { returnObjects: true }) as string[]
  const androidSteps = t('howItWorks.android.steps', { returnObjects: true }) as string[]

  return (
    <PageSection id="how-it-works">
      <SectionHeading title={t('howItWorks.title')} sub={t('howItWorks.sub')} />
      <div className="grid gap-5 sm:grid-cols-2">
        <DiagramColumn
          icon={Laptop}
          heading={t('howItWorks.mac.heading')}
          headingSub={t('howItWorks.mac.headingSub')}
          steps={macSteps}
        />
        <DiagramColumn
          icon={TabletSmartphone}
          heading={t('howItWorks.android.heading')}
          headingSub={t('howItWorks.android.headingSub')}
          steps={androidSteps}
          liveIndicator
        />
      </div>
    </PageSection>
  )
}
