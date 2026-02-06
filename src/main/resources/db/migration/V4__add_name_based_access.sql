-- ============================================================
-- TRAVEL PLANNER - ADD NAME FIELDS FOR NAME-BASED API ACCESS
-- Flyway Migration V4
-- ============================================================

-- ===========================
-- 1. ADD UNIQUE NAME TO TRIP
-- ===========================
-- Add constraint for unique trip name per user
ALTER TABLE trip ADD CONSTRAINT unique_trip_name_per_user UNIQUE (user_id, name);

-- ===========================
-- 2. ADD NAME TO TRIP_STOP
-- ===========================
-- Add a name field to trip_stop for identifying stops by name
-- This will be auto-generated from city name + stop_date to ensure uniqueness
ALTER TABLE trip_stop ADD COLUMN stop_name VARCHAR(200);

-- Update existing stop names with city name + stop date
UPDATE trip_stop ts
SET stop_name = LOWER(c.name) || '_' || TO_CHAR(ts.stop_date, 'YYYY-MM-DD')
FROM city c
WHERE ts.city_id = c.id;

-- Make stop_name required and unique within a trip
ALTER TABLE trip_stop ALTER COLUMN stop_name SET NOT NULL;
ALTER TABLE trip_stop ADD CONSTRAINT unique_stop_name_per_trip UNIQUE (trip_id, stop_name);

-- ===========================
-- 3. ADD UNIQUE CONSTRAINT TO POI
-- ===========================
-- Make POI external_id required and unique
UPDATE poi SET external_id = 'poi_' || id WHERE external_id IS NULL;
ALTER TABLE poi ALTER COLUMN external_id SET NOT NULL;
ALTER TABLE poi ADD CONSTRAINT unique_poi_external_id UNIQUE (external_id);

-- ===========================
-- 4. ADD INDEXES FOR NAME-BASED QUERIES
-- ===========================
CREATE INDEX idx_trip_name ON trip(LOWER(name));
CREATE INDEX idx_trip_stop_name ON trip_stop(LOWER(stop_name));
CREATE INDEX idx_poi_external_id ON poi(external_id);

-- ===========================
-- 5. MAKE COUNTRY/REGION/CITY NAMES UNIQUE
-- ===========================
-- Countries should have unique names
ALTER TABLE country ADD CONSTRAINT unique_country_name UNIQUE (name);

-- Regions should have unique names within a country
ALTER TABLE region ADD CONSTRAINT unique_region_name_per_country UNIQUE (country_id, name);

-- Cities should have unique names within a region
ALTER TABLE city ADD CONSTRAINT unique_city_name_per_region UNIQUE (region_id, name);

-- Add indexes for case-insensitive name searches
CREATE INDEX idx_country_name_lower ON country(LOWER(name));
CREATE INDEX idx_region_name_lower ON region(LOWER(name));
CREATE INDEX idx_city_name_lower ON city(LOWER(name));
