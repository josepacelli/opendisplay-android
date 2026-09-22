export function SectionHeading({
  eyebrow,
  title,
  sub,
}: {
  eyebrow: string
  title: string
  sub?: string
}) {
  return (
    <div className="mb-10 text-center">
      <p className="text-xs font-semibold uppercase tracking-wide text-primary">{eyebrow}</p>
      <h2 className="mt-2 text-2xl font-medium tracking-tight text-foreground sm:text-[1.75rem]">
        {title}
      </h2>
      {sub && <p className="mx-auto mt-2 max-w-lg text-[0.95rem] text-muted-foreground">{sub}</p>}
    </div>
  )
}
