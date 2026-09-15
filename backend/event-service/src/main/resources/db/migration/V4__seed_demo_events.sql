-- Demo data so the platform is usable out of the box.
INSERT INTO events (name, description, venue, city, event_date_time, total_tickets, available_tickets, ticket_price, status)
VALUES
    ('Spring Boot Live 2026', 'A day of deep-dive talks on Spring Boot and cloud-native Java.',
     'ExCeL London', 'London', TIMESTAMP WITH TIME ZONE '2026-11-20 09:00:00+00', 500, 500, 99.00, 'SCHEDULED'),
    ('Indie Rock Night', 'Live music featuring up-and-coming indie bands.',
     'O2 Academy', 'Manchester', TIMESTAMP WITH TIME ZONE '2026-12-05 19:30:00+00', 800, 800, 45.50, 'SCHEDULED'),
    ('Premier League: Matchday', 'Top-flight football fixture.',
     'Emirates Stadium', 'London', TIMESTAMP WITH TIME ZONE '2027-01-10 15:00:00+00', 60000, 60000, 75.00, 'SCHEDULED');
