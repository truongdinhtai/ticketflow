import axios from 'axios';

// The services we surface on the health dashboard. Each is reached at
// /health/<key>, proxied to that service's /actuator/health (see vite.config.js
// and nginx.conf) so the browser makes only same-origin calls.
export const SERVICES = [
  { key: 'gateway', label: 'API Gateway' },
  { key: 'config', label: 'Config Server' },
  { key: 'discovery', label: 'Eureka Discovery' },
  { key: 'auth', label: 'Auth Service' },
  { key: 'event', label: 'Event Service' },
  { key: 'booking', label: 'Booking Service' },
  { key: 'notification', label: 'Notification Service' },
];

// Plain axios (no auth header needed) with a short timeout so a down service
// resolves quickly as DOWN rather than hanging the dashboard.
const probe = axios.create({ timeout: 4000 });

export async function fetchHealth(key) {
  try {
    const { data } = await probe.get(`/health/${key}`);
    return { key, status: data?.status || 'UNKNOWN' };
  } catch (e) {
    // Actuator returns 503 with a body when DOWN — still useful.
    const status = e?.response?.data?.status;
    return { key, status: status || 'DOWN' };
  }
}
