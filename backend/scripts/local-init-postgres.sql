-- =====================================================================
-- Local (no-Docker) database bootstrap for TicketFlow.
-- Creates the shared demo role and one database per service.
--
-- HOW TO RUN
--   pgAdmin: open this in the Query Tool and run each statement ONE AT A TIME
--            (select the line, press F5). CREATE DATABASE cannot run together
--            with other statements in a single transaction.
--   psql:    "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -f scripts/local-init-postgres.sql
--
-- If the role already exists you'll get "role ... already exists" — ignore it.
-- =====================================================================

-- 1) Shared demo role used by all three services.
CREATE ROLE ticketflow WITH LOGIN PASSWORD 'ticketflow';

-- 2) One database per service (run each of these on its own).
CREATE DATABASE event_db OWNER ticketflow;
CREATE DATABASE booking_db OWNER ticketflow;
CREATE DATABASE notification_db OWNER ticketflow;
CREATE DATABASE auth_db OWNER ticketflow;
