INSERT INTO stylists (name)
SELECT v.name FROM (VALUES ('Nino'), ('Mariam')) AS v(name)
WHERE NOT EXISTS (SELECT 1 FROM stylists);

INSERT INTO services (name, duration_minutes, price)
SELECT v.name, v.duration_minutes, v.price
FROM (VALUES ('Haircut', 60, 30.00), ('Hair coloring', 120, 80.00), ('Manicure', 45, 25.00))
     AS v(name, duration_minutes, price)
WHERE NOT EXISTS (SELECT 1 FROM services);

-- Monday to Saturday, 09:00-18:00, for every stylist
INSERT INTO working_hours (stylist_id, day_of_week, start_time, end_time)
SELECT s.id, d.day_of_week, TIME '09:00', TIME '18:00'
FROM stylists s
CROSS JOIN (VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY'), ('SATURDAY'))
     AS d(day_of_week)
WHERE NOT EXISTS (SELECT 1 FROM working_hours);
