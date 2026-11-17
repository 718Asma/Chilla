import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// Tout appel vers /api/... est transmis à http://localhost:8081/... (backend Chilla).
// Le proxy supporte le streaming : le flux temps réel (SSE) passe aussi par là.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
