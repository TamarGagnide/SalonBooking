-- Demo seed data, applied on every startup (spring.sql.init.mode=always).
-- Stylists and services are upserted by id, so editing a name here and redeploying
-- renames the existing row. Working hours are only added for stylists that have none.

INSERT INTO stylists (id, name, name_ka) VALUES
    (1, 'Marita', 'მარიტა'),
    (2, 'Mancho', 'მანჩო'),
    (3, 'Nina', 'ნინა')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, name_ka = EXCLUDED.name_ka;

INSERT INTO services (id, name, name_ka, duration_minutes, price) VALUES
    (1, 'Haircut', 'თმის შეჭრა', 60, 30.00),
    (2, 'Hair coloring', 'თმის შეღებვა', 120, 80.00),
    (3, 'Manicure', 'მანიკური', 45, 25.00)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, name_ka = EXCLUDED.name_ka,
    duration_minutes = EXCLUDED.duration_minutes, price = EXCLUDED.price;

-- keep the id sequences ahead of the explicit ids above
SELECT setval(pg_get_serial_sequence('stylists', 'id'), (SELECT MAX(id) FROM stylists));
SELECT setval(pg_get_serial_sequence('services', 'id'), (SELECT MAX(id) FROM services));

-- Monday to Saturday, 09:00-18:00
INSERT INTO working_hours (stylist_id, day_of_week, start_time, end_time)
SELECT s.id, d.day_of_week, TIME '09:00', TIME '18:00'
FROM stylists s
CROSS JOIN (VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY'), ('SATURDAY'))
     AS d(day_of_week)
WHERE NOT EXISTS (SELECT 1 FROM working_hours w WHERE w.stylist_id = s.id);
