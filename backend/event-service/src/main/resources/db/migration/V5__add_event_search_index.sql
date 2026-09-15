-- Composite index for the high-frequency "find events by city within a date
-- range" query. Without it, Postgres uses the date-only index and then filters
-- out other cities (Rows Removed by Filter); this lets it match city + date
-- directly. See PERFORMANCE.md for EXPLAIN ANALYZE before/after.
CREATE INDEX IF NOT EXISTS idx_events_city_datetime ON events (city, event_date_time);
