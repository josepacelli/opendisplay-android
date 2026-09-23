import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@/hooks/useTheme'
import './index.css'
import { Contributing } from './pages/zh-Hans/Contributing.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Contributing />
  </StrictMode>,
)
