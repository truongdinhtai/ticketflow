import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev-server proxy so the browser only ever calls same-origin paths (no CORS).
// - /api/*        -> API Gateway
// - /health/<svc> -> that service's /actuator/health
// In production the same paths are proxied by Nginx (see nginx.conf).
const gateway = 'http://localhost:8080';
const healthTargets = {
  gateway: 'http://localhost:8080',
  config: 'http://localhost:8888',
  discovery: 'http://localhost:8761',
  auth: 'http://localhost:8084',
  event: 'http://localhost:8081',
  booking: 'http://localhost:8082',
  notification: 'http://localhost:8083',
};

const healthProxies = Object.fromEntries(
  Object.entries(healthTargets).map(([key, target]) => [
    `/health/${key}`,
    { target, changeOrigin: true, rewrite: () => '/actuator/health' },
  ]),
);

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: gateway, changeOrigin: true },
      ...healthProxies,
    },
  },
});
