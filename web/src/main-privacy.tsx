import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@/hooks/useTheme'
import './index.css'
import { Privacy } from './pages/pt-BR/Privacy.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Privacy />
  </StrictMode>,
)
