-- ============================================================
-- TRAVEL PLANNER - UPDATE POI TABLE FOR USER CUSTOM POIS
-- Flyway Migration V5
-- ============================================================

-- Rimuovi la tabella di join trip_stop_poi (non più necessaria per i POI personalizzati)
DROP TABLE IF EXISTS trip_stop_poi;

-- Modifica la tabella poi per salvare POI personalizzati dell'utente
-- Rimuovi l'indice GIN su raw_json (non più necessario)
DROP INDEX IF EXISTS idx_poi_jsonb;

-- Rimuovi il vincolo unique su external_id (non più necessario)
ALTER TABLE poi DROP CONSTRAINT IF EXISTS poi_external_id_key;

-- Modifica la struttura della tabella poi
ALTER TABLE poi
    DROP COLUMN IF EXISTS external_id,
    DROP COLUMN IF EXISTS raw_json,
    ADD COLUMN user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ADD COLUMN description TEXT,
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION,
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

-- Aggiungi indici per le query comuni
CREATE INDEX idx_poi_user ON poi(user_id);
CREATE INDEX idx_poi_name ON poi(name);

-- Aggiungi vincolo unique per evitare duplicati (stesso nome per lo stesso utente)
CREATE UNIQUE INDEX idx_poi_user_name ON poi(user_id, name);

