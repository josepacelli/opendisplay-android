import { Check, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { PageSection } from '@/components/PageSection'
import { SectionHeading } from '@/components/SectionHeading'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'

type Row = { label: string; us: string; duet: string; spacedesk: string }

/** Whether each cell is a point in that product's favour, per row of `compare.rows` (price,
 * open source, account required, same protocol, central server) and column (us, Duet,
 * Spacedesk). A fact about the products, not the wording, so it lives here instead of in the
 * seven locale files. */
const FAVOURABLE: [boolean, boolean, boolean][] = [
  [true, false, true],
  [true, false, false],
  [true, false, true],
  [true, false, false],
  [true, false, true],
]

function Cell({ value, positive, highlighted }: { value: string; positive: boolean; highlighted?: boolean }) {
  return (
    <TableCell
      className={
        'px-4 py-3.5 ' +
        (positive ? 'text-foreground' : 'text-muted-foreground') +
        (highlighted ? ' bg-secondary/45 font-medium' : '')
      }
    >
      <span className="inline-flex items-center gap-2">
        {positive ? (
          <span className="flex size-5 shrink-0 items-center justify-center rounded-full bg-accent/15 text-accent">
            <Check className="size-3.5" aria-hidden="true" />
          </span>
        ) : (
          <span className="flex size-5 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground/70">
            <X className="size-3.5" aria-hidden="true" />
          </span>
        )}
        {value}
      </span>
    </TableCell>
  )
}

export function Compare() {
  const { t } = useTranslation('landing')
  const headers = t('compare.headers', { returnObjects: true }) as string[]
  const rows = t('compare.rows', { returnObjects: true }) as Row[]

  return (
    <PageSection id="compare">
      <SectionHeading title={t('compare.title')} sub={t('compare.sub')} />
      <div className="overflow-x-auto rounded-2xl border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow className="border-border hover:bg-transparent">
              {headers.map((h, idx) => (
                <TableHead
                  key={h}
                  className={
                    'h-12 px-4 font-heading text-base ' +
                    (idx === 1 ? 'bg-secondary/65 font-semibold text-foreground' : 'font-medium text-muted-foreground')
                  }
                >
                  {idx === 1 ? (
                    <span className="inline-flex items-center gap-2">
                      <span className="size-2 rounded-full bg-accent" aria-hidden="true" />
                      {h}
                    </span>
                  ) : (
                    h
                  )}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.map((row, i) => (
              <TableRow key={row.label} className="border-border">
                <TableCell className="px-4 py-3.5 font-medium text-foreground">{row.label}</TableCell>
                <Cell value={row.us} positive={FAVOURABLE[i][0]} highlighted />
                <Cell value={row.duet} positive={FAVOURABLE[i][1]} />
                <Cell value={row.spacedesk} positive={FAVOURABLE[i][2]} />
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </PageSection>
  )
}
