import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { CodeOfConduct } from './pages/ko/CodeOfConduct.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <CodeOfConduct />
  </StrictMode>,
)
