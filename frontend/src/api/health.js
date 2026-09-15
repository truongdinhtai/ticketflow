import client from './client';

// Services shown on the health dashboard. Each is reached at /health/<key>,
// which the API Gateway routes to that service's /actuator/health. Going through
// the gateway means the dashboard works both same-origin (Docker/K8s, proxied by
// nginx) and cross-origin (frontend on Cloudflare Pages -> gateway with CORS).
export const SERVICES = [
  { key: 'gateway', label: 'API Gateway' },
  { key: 'config', label: 'Config Server' },
  { key: 'discovery', label: 'Eureka Discovery' },
  { key: 'auth', label: 'Auth Service' },
  { key: 'event', label: 'Event Service' },
  { key: 'booking', label: 'Booking Service' },
  { key: 'notification', label: 'Notification Service' },
];

export async function fetchHealth(key) {
  try {
    const { data } = await client.get(`/health/${key}`, { timeout: 4000 });
    return { key, status: data?.status || 'UNKNOWN' };
  } catch (e) {
    // A down service surfaces as 502/503 (with an actuator body when available).
    const status = e?.response?.data?.status;
    return { key, status: status || 'DOWN' };
  }
}
