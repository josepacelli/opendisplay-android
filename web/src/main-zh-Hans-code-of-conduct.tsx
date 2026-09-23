import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@/hooks/useTheme'
import './index.css'
import { CodeOfConduct } from './pages/zh-Hans/CodeOfConduct.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <CodeOfConduct />
  </StrictMode>,
)
