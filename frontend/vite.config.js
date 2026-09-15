import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev-server proxy so the browser only calls same-origin paths (no CORS) during
// local development. Both /api and /health go to the API Gateway, which routes
// them (the same as nginx does in the Docker/K8s image).
const gateway = 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: gateway, changeOrigin: true },
      '/health': { target: gateway, changeOrigin: true },
    },
  },
});
