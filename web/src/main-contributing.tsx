import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { Contributing } from './pages/Contributing.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Contributing />
  </StrictMode>,
)
