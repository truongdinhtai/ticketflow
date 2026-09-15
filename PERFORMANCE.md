# TicketFlow — Performance Optimisation Report

This document records **measured** performance work on the Booking Service: Redis
caching, PostgreSQL connection-pool tuning, and query indexing. Every number
below comes from a real run on the setup described under *Test environment* —
nothing is estimated. Reproduction commands are included so the results can be
re-verified.

> **Test environment (be honest about this in interviews).** All numbers were
> produced on a **single developer machine** (Windows 11 + Docker Desktop / WSL2):
> every service, PostgreSQL 16, Redis 7 and Kafka run in containers on one host,
> and the k6 load generator runs in a container on the same Docker network hitting
> `booking-service:8082` directly. Absolute figures are therefore **relative**
> (they show the effect of each change on identical hardware), not production
> throughput on dedicated infrastructure. Stack: Java 21, Spring Boot 3.3.

---

## Summary (headline numbers)

| Optimisation | Metric | Before | After | Improvement |
|---|---|---|---|---|
| **Redis cache** (available-seats read) | p95 latency | 621 ms | **60 ms** | **~10× lower (90 %↓)** |
| **Redis cache** | throughput | 1,577 req/s | **13,746 req/s** | **~8.7×** |
| **HikariCP tuning** (booking write) | throughput | 147 req/s | **281 req/s** | ~1.9× |
| **HikariCP tuning** | error rate @500 VUs | 0.07 % | **0 %** | timeouts eliminated |
| **Composite index** (event search) | query exec time | 6.9 ms | **2.8 ms** | ~2.4× |

---

## 1. Redis caching (cache-aside) for available-seats lookup

**What:** `GET /api/bookings/availability/{eventId}` returns an event's remaining
seats. Implemented with Spring Cache over Redis (`@Cacheable`); a new booking
calls `@CacheEvict` so the cached count is invalidated immediately. Caching is
toggled with `CACHE_TYPE=redis|none`, which is exactly how the two runs below were
produced (no code change between them).

**Load profile:** k6, ramp to **500 virtual users**, hold 30 s (45 s total).

| Metric | Before (no cache) | After (Redis) | Improvement |
|---|---|---|---|
| Requests served | 70,971 | 618,553 | 8.7× |
| Throughput | 1,577 req/s | **13,746 req/s** | **8.7×** |
| Latency avg | 264 ms | 30 ms | 8.9× |
| Latency p90 | 492 ms | 49 ms | 10× |
| Latency **p95** | **621 ms** | **60 ms** | **10.3× (90 %↓)** |
| Latency max | 2.13 s | 234 ms | 9× |
| Error rate | 0 % | 0 % | — |

**Why it helps:** without the cache, every read makes a Feign call to Event
Service, which queries PostgreSQL — the DB round-trip dominates latency and caps
throughput. With cache-aside, all but the first read (and the first after each
booking) are served from Redis in-memory, removing the network + DB hop.

**Correctness:** the `@CacheEvict` on booking keeps the cached value from going
stale — the seat count reflects a new booking on the very next read.

---

## 2. HikariCP connection-pool tuning

**What:** tuned the pool for the write path (`POST /api/bookings`, which does a
Feign reservation + a DB write + a Kafka publish). Config in
`backend/config-repo/booking-service.yml`.

**Load profile:** k6, 500 VUs, 45 s, against a high-capacity event (so it never
sells out — we measure the pipeline, not `409 Sold out`).

| Metric | Pool = 5 (undersized) | Pool = 30 (tuned) | Improvement |
|---|---|---|---|
| Throughput | 147 req/s | **281 req/s** | ~1.9× |
| Latency avg | 2.87 s | 1.49 s | ~1.9× |
| Latency p95 | 4.03 s | **2.15 s** | ~1.9× |
| Error rate | 0.07 % (connection timeouts) | **0 %** | eliminated |

**Parameters and rationale:**

| Setting | Value | Why |
|---|---|---|
| `maximum-pool-size` | 30 | Ceiling of concurrent DB connections. The default (10) becomes the bottleneck under 500 concurrent writers — requests queue for a connection and eventually hit `connection-timeout`. Each service has its **own** PostgreSQL (database-per-service), so 30 is safely under Postgres' default `max_connections` (100) per instance. |
| `minimum-idle` | 10 | Keep warm connections ready so a traffic burst doesn't pay connection-establishment latency. |
| `connection-timeout` | 3000 ms | **Fail fast** — under overload, surface backpressure as a clear error instead of threads blocking unboundedly. |
| `max-lifetime` | 1,200,000 ms (20 min) | Recycle connections below typical DB / load-balancer idle cut-offs, avoiding use of a silently-dead connection. |
| `idle-timeout` | 300,000 ms (5 min) | Release connections above `minimum-idle` when traffic subsides. |
| `keepalive-time` | 120,000 ms | Probe idle connections so they don't go stale between bursts. |

**Takeaway:** the pool size is a real throughput lever — an undersized pool both
lowers throughput and produces timeout errors under load. Sizing it to the DB's
capacity removed the errors and ~doubled write throughput.

---

## 3. Query indexing (EXPLAIN ANALYZE before/after)

**What:** the high-frequency "find events by **city** within a **date range**"
query. Baseline schema had a date-only index (`idx_events_event_date_time`); added
a composite `idx_events_city_datetime (city, event_date_time)`
(migration `V5__add_event_search_index.sql`).

**Dataset:** 100,000 events (`ANALYZE`d), warm cache.

Query:
```sql
SELECT id, name FROM events
WHERE city = 'Glasgow'
  AND event_date_time BETWEEN now() AND now() + interval '30 days'
ORDER BY event_date_time;
```

| Metric | Before (date index only) | After (composite index) |
|---|---|---|
| **Execution time** | **6.910 ms** | **2.815 ms** (~2.4×) |
| Index rows scanned | 8,220 | 1,644 |
| **Rows removed by filter** | **6,576** | **0** |
| Access path | Bitmap scan on date index, then filter city | Bitmap scan on (city, event_date_time) |

**Before:**
```
Bitmap Heap Scan on events (actual time=1.626..4.913 rows=1644)
  Filter: ((city)::text = 'Glasgow'::text)
  Rows Removed by Filter: 6576
  ->  Bitmap Index Scan on idx_events_event_date_time (rows=8220)
Execution Time: 6.910 ms
```

**After:**
```
Bitmap Heap Scan on events (actual time=0.143..2.169 rows=1644)
  ->  Bitmap Index Scan on idx_events_city_datetime (rows=1644)
Execution Time: 2.815 ms
```

**Why it helps:** the date-only index returns every event in the window (8,220
rows) and then discards 80 % that aren't Glasgow. The composite index matches
`city` **and** the date range together, so it reads only the 1,644 rows the query
actually needs — no wasted heap fetches, no filter step.

---

## How to reproduce

```bash
# 1. Bring the stack up
docker compose up -d --build

# 2. Cache OFF vs ON (availability read load test)
CACHE_TYPE=none  docker compose up -d booking-service   # wait until healthy
docker run --rm -i --network ticketflow_default \
  -e VUS=500 -e BASE_URL=http://booking-service:8082 -e EVENT_ID=1 \
  grafana/k6 run - < perf/k6/availability.js            # BEFORE

CACHE_TYPE=redis docker compose up -d booking-service
docker run --rm -i --network ticketflow_default \
  -e VUS=500 -e BASE_URL=http://booking-service:8082 -e EVENT_ID=1 \
  grafana/k6 run - < perf/k6/availability.js            # AFTER

# 3. HikariCP pool size (write load test) — swap DB_POOL_MAX between runs
DB_POOL_MAX=5  docker compose up -d booking-service
DB_POOL_MAX=30 docker compose up -d booking-service
docker run --rm -i --network ticketflow_default \
  -e VUS=500 -e BASE_URL=http://booking-service:8082 -e EVENT_ID=<big-capacity-id> \
  grafana/k6 run - < perf/k6/booking.js

# 4. Index EXPLAIN ANALYZE
docker compose exec event-db psql -U ticketflow -d event_db \
  -c "EXPLAIN (ANALYZE, BUFFERS) SELECT id,name FROM events
      WHERE city='Glasgow' AND event_date_time BETWEEN now() AND now()+interval '30 days'
      ORDER BY event_date_time;"
```

k6 scripts: [`perf/k6/availability.js`](perf/k6/availability.js), [`perf/k6/booking.js`](perf/k6/booking.js).
