import { useTranslation } from 'react-i18next'
import { RevealSection } from '@/components/RevealSection'
import { SectionHeading } from '@/components/SectionHeading'
import { Card, CardContent } from '@/components/ui/card'

function DiagramColumn({ heading, headingSub, steps }: { heading: string; headingSub: string; steps: string[] }) {
  return (
    <Card className="bg-secondary/40">
      <CardContent>
        <p className="font-mono text-xs font-semibold tracking-wide text-foreground">
          {heading} <span className="font-sans font-normal text-muted-foreground">{headingSub}</span>
        </p>
        <ol className="mt-3 space-y-2 font-mono text-[0.8rem] leading-relaxed text-muted-foreground">
          {steps.map((step, i) => (
            <li key={i}>{step}</li>
          ))}
        </ol>
      </CardContent>
    </Card>
  )
}

export function HowItWorks() {
  const { t } = useTranslation('landing')
  const macSteps = t('howItWorks.mac.steps', { returnObjects: true }) as string[]
  const androidSteps = t('howItWorks.android.steps', { returnObjects: true }) as string[]

  return (
    <RevealSection id="how-it-works">
      <SectionHeading eyebrow={t('howItWorks.eyebrow')} title={t('howItWorks.title')} sub={t('howItWorks.sub')} />
      <div className="grid gap-4 sm:grid-cols-2">
        <DiagramColumn heading={t('howItWorks.mac.heading')} headingSub={t('howItWorks.mac.headingSub')} steps={macSteps} />
        <DiagramColumn heading={t('howItWorks.android.heading')} headingSub={t('howItWorks.android.headingSub')} steps={androidSteps} />
      </div>
    </RevealSection>
  )
}
