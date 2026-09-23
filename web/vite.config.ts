import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const root = import.meta.dirname

const LOCALE_PATHS = ['en', 'es', 'pt-PT', 'zh-Hans', 'ja', 'ko']
const PAGES = ['index', 'code-of-conduct', 'contributing', 'privacy']

function entryKey(localePath: string, page: string) {
  const camelPage = page.replace(/-([a-z])/g, (_, c) => c.toUpperCase())
  const camelLocale = localePath.replace(/-/g, '')
  return `${camelLocale}${camelPage[0].toUpperCase()}${camelPage.slice(1)}`
}

const input: Record<string, string> = {
  index: path.resolve(root, 'index.html'),
  codeOfConduct: path.resolve(root, 'code-of-conduct.html'),
  contributing: path.resolve(root, 'contributing.html'),
  privacy: path.resolve(root, 'privacy.html'),
}

for (const locale of LOCALE_PATHS) {
  for (const page of PAGES) {
    input[entryKey(locale, page)] = path.resolve(root, locale, `${page}.html`)
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(root, './src'),
    },
  },
  build: {
    outDir: path.resolve(root, '../docs'),
    emptyOutDir: false,
    rollupOptions: {
      input,
    },
  },
})
