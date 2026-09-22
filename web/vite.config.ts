import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const root = import.meta.dirname

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
      input: {
        index: path.resolve(root, 'index.html'),
        codeOfConduct: path.resolve(root, 'code-of-conduct.html'),
        contributing: path.resolve(root, 'contributing.html'),
        privacy: path.resolve(root, 'privacy.html'),
      },
    },
  },
})
