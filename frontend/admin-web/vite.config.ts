import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/admin': {
        target: 'http://localhost:8102',
        changeOrigin: true
      },
      '/files': {
        target: 'http://localhost:8102',
        changeOrigin: true
      }
    }
  }
})
