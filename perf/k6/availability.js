import http from 'k6/http';
import { check } from 'k6';

// Load test for the "available seats" read endpoint (Redis cache-aside).
// Run against booking-service directly (bypasses the gateway/JWT so we measure
// the service + cache, not the edge).
//
//   docker run --rm -i --network ticketflow_default \
//     -e VUS=500 -e BASE_URL=http://booking-service:8082 -e EVENT_ID=1 \
//     grafana/k6 run - < perf/k6/availability.js

const VUS = __ENV.VUS ? parseInt(__ENV.VUS) : 500;
const BASE_URL = __ENV.BASE_URL || 'http://booking-service:8082';
const EVENT_ID = __ENV.EVENT_ID || '1';

export const options = {
  scenarios: {
    read_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: VUS }, // ramp up
        { duration: '30s', target: VUS }, // hold at target
        { duration: '5s', target: 0 },    // ramp down
      ],
      gracefulStop: '5s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/api/bookings/availability/${EVENT_ID}`);
  check(res, { 'status is 200': (r) => r.status === 200 });
}
