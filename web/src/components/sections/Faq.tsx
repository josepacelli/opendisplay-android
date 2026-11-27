import { Trans, useTranslation } from 'react-i18next'
import { PageSection } from '@/components/PageSection'
import { SectionHeading } from '@/components/SectionHeading'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'

const RICH_COMPONENTS: Record<number, Record<string, React.ReactElement>> = {
  1: { play: <a href="https://play.google.com/store/apps/details?id=io.github.josepacelli.opendisplay" />, code: <code /> },
  2: { code: <code /> },
  4: { pr5: <a href="https://github.com/josepacelli/opendisplay-android/pull/5" /> },
}

export function Faq() {
  const { t } = useTranslation('landing')
  const items = t('faq.items', { returnObjects: true }) as { q: string; a: string }[]

  return (
    <PageSection id="faq">
      <SectionHeading title={t('faq.title')} />
      <div className="rounded-2xl border border-border bg-card px-6 py-2">
        <Accordion className="w-full">
          {items.map((item, i) => (
            <AccordionItem key={item.q} value={`item-${i}`} className="border-border">
              <AccordionTrigger className="py-4 text-left text-base font-medium hover:no-underline">
                {item.q}
              </AccordionTrigger>
              <AccordionContent className="max-w-[70ch] pb-4 text-muted-foreground [&_a]:text-accent [&_a]:no-underline [&_a:hover]:underline [&_code]:rounded [&_code]:border [&_code]:border-border [&_code]:bg-secondary [&_code]:px-1.5 [&_code]:py-0.5 [&_code]:font-mono [&_code]:text-[0.85em] [&_code]:text-foreground">
                {RICH_COMPONENTS[i] ? (
                  <Trans i18nKey={`faq.items.${i}.a`} t={t} components={RICH_COMPONENTS[i]} />
                ) : (
                  item.a
                )}
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </div>
    </PageSection>
  )
}
