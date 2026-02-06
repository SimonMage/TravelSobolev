-- ============================================================
-- TRAVEL PLANNER - INITIAL DATABASE SCHEMA (POSTGRESQL)
-- Flyway Migration V1
-- ============================================================

-- ===========================
-- 1. USERS & SECURITY
-- ===========================

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_profile (
    user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    preferred_units VARCHAR(10) CHECK (preferred_units IN ('metric', 'imperial'))
);


-- ===========================
-- 2. GEOGRAPHY TABLES
-- ===========================

CREATE TABLE country (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

CREATE TABLE region (
    id SERIAL PRIMARY KEY,
    country_id INTEGER NOT NULL REFERENCES country(id),
    name VARCHAR(150) NOT NULL
);

CREATE TABLE city (
    id SERIAL PRIMARY KEY,
    region_id INTEGER NOT NULL REFERENCES region(id),
    name VARCHAR(150) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL
);


-- ===========================
-- 3. TAGGING
-- ===========================

CREATE TABLE tag (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE city_tag (
    city_id INTEGER NOT NULL REFERENCES city(id) ON DELETE CASCADE,
    tag_id INTEGER NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (city_id, tag_id)
);


-- ===========================
-- 4. SEARCH HISTORY
-- ===========================

CREATE TABLE search_history (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    city_id INTEGER NOT NULL REFERENCES city(id),
    searched_at TIMESTAMP DEFAULT NOW()
);


-- ===========================
-- 5. TRIPS & STOPS
-- ===========================

CREATE TABLE trip (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

CREATE TABLE trip_stop (
    id SERIAL PRIMARY KEY,
    trip_id INTEGER NOT NULL REFERENCES trip(id) ON DELETE CASCADE,
    city_id INTEGER NOT NULL REFERENCES city(id),
    stop_date DATE NOT NULL,
    notes TEXT
);


-- ===========================
-- 6. POINTS OF INTEREST (POI)
-- ===========================

CREATE TABLE poi (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(100),
    name VARCHAR(255) NOT NULL,
    raw_json JSONB NOT NULL
);

CREATE TABLE trip_stop_poi (
    trip_stop_id INTEGER NOT NULL REFERENCES trip_stop(id) ON DELETE CASCADE,
    poi_id INTEGER NOT NULL REFERENCES poi(id) ON DELETE CASCADE,
    PRIMARY KEY (trip_stop_id, poi_id)
);


-- ===========================
-- 7. INDEXES (PERFORMANCE)
-- ===========================

-- Users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- City
CREATE INDEX idx_city_name ON city(name);
CREATE INDEX idx_city_coords ON city(latitude, longitude);

-- Tags
CREATE INDEX idx_tag_name ON tag(name);

-- Search history
CREATE INDEX idx_search_user ON search_history(user_id);
CREATE INDEX idx_search_city ON search_history(city_id);

-- POI JSONB search
CREATE INDEX idx_poi_jsonb ON poi USING GIN (raw_json);
