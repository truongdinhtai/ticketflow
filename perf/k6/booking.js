import http from 'k6/http';
import { check } from 'k6';

// Load test for the booking WRITE path (Feign reserve -> persist -> Kafka).
// Demonstrates throughput / error rate under concurrency (and the effect of
// HikariCP tuning). Target a high-capacity event so it does not sell out.
//
//   docker run --rm -i --network ticketflow_default \
//     -e VUS=500 -e BASE_URL=http://booking-service:8082 -e EVENT_ID=<bigEventId> \
//     grafana/k6 run - < perf/k6/booking.js

const VUS = __ENV.VUS ? parseInt(__ENV.VUS) : 500;
const BASE_URL = __ENV.BASE_URL || 'http://booking-service:8082';
const EVENT_ID = __ENV.EVENT_ID ? parseInt(__ENV.EVENT_ID) : 1;

export const options = {
  scenarios: {
    write_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: VUS },
        { duration: '30s', target: VUS },
        { duration: '5s', target: 0 },
      ],
      gracefulStop: '5s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.10'],
  },
};

export default function () {
  const payload = JSON.stringify({
    eventId: EVENT_ID,
    customerName: `LoadTest ${__VU}`,
    customerEmail: `vu${__VU}-${__ITER}@loadtest.dev`,
    quantity: 1,
  });
  const res = http.post(`${BASE_URL}/api/bookings`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, { 'status is 201': (r) => r.status === 201 });
}
