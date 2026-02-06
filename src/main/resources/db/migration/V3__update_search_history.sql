-- ============================================================
-- TRAVEL PLANNER - UPDATE SEARCH_HISTORY TABLE
-- Flyway Migration V3
-- Add query column and make city_id optional
-- ============================================================

-- Make city_id nullable (for searches via GeoAPIfy that may not match a local city)
ALTER TABLE search_history ALTER COLUMN city_id DROP NOT NULL;

-- Add query column to store the original search text
ALTER TABLE search_history ADD COLUMN query VARCHAR(255);

-- Add index for query search
CREATE INDEX idx_search_history_query ON search_history(query);
