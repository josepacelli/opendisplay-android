import { Trans, useTranslation } from 'react-i18next'
import { PageSection } from '@/components/PageSection'
import { SectionHeading } from '@/components/SectionHeading'


const CREDIT_COMPONENTS: Record<number, Record<string, React.ReactElement>> = {
  0: {
    issue4: <a href="https://github.com/josepacelli/opendisplay-android/issues/4" />,
    issue10: <a href="https://github.com/josepacelli/opendisplay-android/issues/10" />,
    issue28: <a href="https://github.com/josepacelli/opendisplay-android/issues/28" />,
    issue18: <a href="https://github.com/josepacelli/opendisplay-android/issues/18" />,
  },
  1: { pr5: <a href="https://github.com/josepacelli/opendisplay-android/pull/5" /> },
  2: {
    pr33: <a href="https://github.com/josepacelli/opendisplay-android/pull/33" />,
    code: <code />,
  },
  3: {
    issue44: <a href="https://github.com/josepacelli/opendisplay-android/issues/44" />,
    issue45: <a href="https://github.com/josepacelli/opendisplay-android/issues/45" />,
  },
  4: { issue113: <a href="https://github.com/josepacelli/opendisplay-android/issues/113" /> },
  5: {
    pr153: <a href="https://github.com/josepacelli/opendisplay-android/pull/153" />,
    pr162: <a href="https://github.com/josepacelli/opendisplay-android/pull/162" />,
    pr165: <a href="https://github.com/josepacelli/opendisplay-android/pull/165" />,
  },
}

export function Contribute() {
  const { t } = useTranslation('landing')
  const credits = t('contribute.credits', { returnObjects: true }) as { handle: string; text: string }[]

  return (
    <PageSection id="contribute">
      <SectionHeading title={t('contribute.title')} />
      <p className="max-w-[60ch] text-base text-muted-foreground [&_a]:text-accent [&_a]:no-underline [&_a:hover]:underline">
        <Trans
          i18nKey="contribute.desc"
          t={t}
          components={{ repo: <a href="https://github.com/josepacelli/opendisplay-android" /> }}
        />
      </p>
      <div className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {credits.map((credit, i) => (
          <div key={credit.handle} className="rounded-2xl border border-border bg-card p-5">
            <p className="font-heading text-base font-medium text-foreground">{credit.handle}</p>
            <p className="mt-2 text-sm leading-relaxed text-muted-foreground [&_a]:text-accent [&_a]:no-underline [&_a:hover]:underline [&_code]:rounded [&_code]:border [&_code]:border-border [&_code]:bg-secondary [&_code]:px-1 [&_code]:font-mono [&_code]:text-[0.85em] [&_code]:text-foreground">
              <Trans i18nKey={`contribute.credits.${i}.text`} t={t} components={CREDIT_COMPONENTS[i]} />
            </p>
          </div>
        ))}
      </div>
    </PageSection>
  )
}
