CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS stylists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    duration_minutes INT NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS working_hours (
    id BIGSERIAL PRIMARY KEY,
    stylist_id BIGINT NOT NULL REFERENCES stylists(id),
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- NFR-03: no two ACTIVE appointments for the same stylist may have overlapping
-- [start_time, end_time) ranges, enforced by the database itself (not just app code)
-- so the guarantee holds even under concurrent/simultaneous booking attempts.
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    stylist_id BIGINT NOT NULL REFERENCES stylists(id),
    service_id BIGINT NOT NULL REFERENCES services(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT no_overlapping_active_appointments
        EXCLUDE USING gist (
            stylist_id WITH =,
            tsrange(start_time, end_time) WITH &&
        ) WHERE (status = 'ACTIVE')
);

-- Georgian display names (existing databases get the column added on startup)
ALTER TABLE stylists ADD COLUMN IF NOT EXISTS name_ka VARCHAR(255);
ALTER TABLE services ADD COLUMN IF NOT EXISTS name_ka VARCHAR(255);
