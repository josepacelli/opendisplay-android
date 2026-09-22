import { Trans, useTranslation } from 'react-i18next'
import { RevealSection } from '@/components/RevealSection'
import { SectionHeading } from '@/components/SectionHeading'

const CREDIT_HANDLES = ['gaeaearth', 'edoardomich', 'jpbhdrey', 'dyss1992']

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
}

export function Contribute() {
  const { t } = useTranslation('landing')
  const credits = t('contribute.credits', { returnObjects: true }) as { handle: string; text: string }[]

  return (
    <RevealSection id="contribute">
      <SectionHeading eyebrow={t('contribute.eyebrow')} title={t('contribute.title')} />
      <p className="mx-auto max-w-xl text-center text-sm text-muted-foreground [&_a]:text-primary [&_a]:no-underline [&_a:hover]:underline">
        <Trans
          i18nKey="contribute.desc"
          t={t}
          components={{ repo: <a href="https://github.com/josepacelli/opendisplay-android" /> }}
        />
      </p>
      <div className="mx-auto mt-8 grid max-w-2xl gap-5 sm:grid-cols-2">
        {credits.map((credit, i) => (
          <div key={credit.handle} className="flex gap-3 text-left">
            <img
              src={`https://github.com/${CREDIT_HANDLES[i]}.png`}
              alt=""
              loading="lazy"
              className="size-9 shrink-0 rounded-full"
            />
            <p className="text-sm text-muted-foreground [&_a]:text-primary [&_a]:no-underline [&_a:hover]:underline [&_code]:rounded [&_code]:border [&_code]:border-border [&_code]:bg-secondary [&_code]:px-1 [&_code]:font-mono [&_code]:text-[0.85em]">
              <strong className="text-foreground">{credit.handle}</strong> —{' '}
              <Trans i18nKey={`contribute.credits.${i}.text`} t={t} components={CREDIT_COMPONENTS[i]} />
            </p>
          </div>
        ))}
      </div>
    </RevealSection>
  )
}
