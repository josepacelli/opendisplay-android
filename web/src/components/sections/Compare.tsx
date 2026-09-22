import { Check, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { RevealSection } from '@/components/RevealSection'
import { SectionHeading } from '@/components/SectionHeading'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'

type Row = { label: string; us: string; duet: string; spacedesk: string }

function Cell({ value, positive }: { value: string; positive: boolean }) {
  return (
    <TableCell className={positive ? 'text-foreground' : 'text-muted-foreground'}>
      <span className="inline-flex items-center gap-1.5">
        {positive ? (
          <Check className="size-4 text-primary" aria-hidden="true" />
        ) : (
          <X className="size-4 text-muted-foreground/60" aria-hidden="true" />
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
    <RevealSection id="compare">
      <SectionHeading eyebrow={t('compare.eyebrow')} title={t('compare.title')} sub={t('compare.sub')} />
      <div className="overflow-x-auto rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              {headers.map((h) => (
                <TableHead key={h}>{h}</TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.label}>
                <TableCell className="font-medium text-foreground">{row.label}</TableCell>
                <Cell value={row.us} positive />
                <Cell value={row.duet} positive={false} />
                <Cell value={row.spacedesk} positive={false} />
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </RevealSection>
  )
}
