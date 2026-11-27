declare module '@/lib/liquid-glass.js' {
  const LiquidGlass: {
    init: (defaults?: Record<string, unknown>) => void
    refresh: (el?: Element | null) => void
  }
  export default LiquidGlass
}
