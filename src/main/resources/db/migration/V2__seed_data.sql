-- ============================================================
-- TRAVEL PLANNER - SEED DATA
-- Flyway Migration V2
-- ============================================================

-- ===========================
-- 1. COUNTRIES
-- ===========================
INSERT INTO country (id, name) VALUES
(1, 'Italia'),
(2, 'Francia'),
(3, 'Spagna'),
(4, 'Germania'),
(5, 'Regno Unito');

-- ===========================
-- 2. REGIONS
-- ===========================
INSERT INTO region (id, country_id, name) VALUES
-- Italia
(1, 1, 'Lombardia'),
(2, 1, 'Lazio'),
(3, 1, 'Toscana'),
(4, 1, 'Veneto'),
(5, 1, 'Campania'),
-- Francia
(6, 2, 'Île-de-France'),
(7, 2, 'Provence-Alpes-Côte d''Azur'),
-- Spagna
(8, 3, 'Comunidad de Madrid'),
(9, 3, 'Cataluña'),
-- Germania
(10, 4, 'Baviera'),
(11, 4, 'Berlino'),
-- Regno Unito
(12, 5, 'Greater London'),
(13, 5, 'Scotland');

-- ===========================
-- 3. CITIES
-- ===========================
INSERT INTO city (id, region_id, name, latitude, longitude) VALUES
-- Italia
(1, 1, 'Milano', 45.4642, 9.1900),
(2, 2, 'Roma', 41.9028, 12.4964),
(3, 3, 'Firenze', 43.7696, 11.2558),
(4, 4, 'Venezia', 45.4408, 12.3155),
(5, 5, 'Napoli', 40.8518, 14.2681),
-- Francia
(6, 6, 'Parigi', 48.8566, 2.3522),
(7, 7, 'Marsiglia', 43.2965, 5.3698),
(8, 7, 'Nizza', 43.7102, 7.2620),
-- Spagna
(9, 8, 'Madrid', 40.4168, -3.7038),
(10, 9, 'Barcellona', 41.3851, 2.1734),
-- Germania
(11, 10, 'Monaco di Baviera', 48.1351, 11.5820),
(12, 11, 'Berlino', 52.5200, 13.4050),
-- Regno Unito
(13, 12, 'Londra', 51.5074, -0.1278),
(14, 13, 'Edimburgo', 55.9533, -3.1883);

-- ===========================
-- 4. TAGS
-- ===========================
INSERT INTO tag (id, name) VALUES
(1, 'arte'),
(2, 'storia'),
(3, 'gastronomia'),
(4, 'mare'),
(5, 'montagna'),
(6, 'nightlife'),
(7, 'shopping'),
(8, 'romantico'),
(9, 'famiglia'),
(10, 'avventura');

-- ===========================
-- 5. CITY-TAG ASSOCIATIONS
-- ===========================
INSERT INTO city_tag (city_id, tag_id) VALUES
-- Milano
(1, 1), (1, 7), (1, 3),
-- Roma
(2, 1), (2, 2), (2, 3), (2, 8),
-- Firenze
(3, 1), (3, 2), (3, 3), (3, 8),
-- Venezia
(4, 1), (4, 8), (4, 2),
-- Napoli
(5, 3), (5, 2), (5, 4),
-- Parigi
(6, 1), (6, 8), (6, 7), (6, 3),
-- Marsiglia
(7, 4), (7, 3),
-- Nizza
(8, 4), (8, 8),
-- Madrid
(9, 1), (9, 6), (9, 3),
-- Barcellona
(10, 1), (10, 4), (10, 6), (10, 3),
-- Monaco di Baviera
(11, 2), (11, 3), (11, 9),
-- Berlino
(12, 2), (12, 6), (12, 1),
-- Londra
(13, 2), (13, 7), (13, 1), (13, 6),
-- Edimburgo
(14, 2), (14, 10);

-- Reset sequences after explicit ID inserts
SELECT setval('country_id_seq', (SELECT MAX(id) FROM country));
SELECT setval('region_id_seq', (SELECT MAX(id) FROM region));
SELECT setval('city_id_seq', (SELECT MAX(id) FROM city));
SELECT setval('tag_id_seq', (SELECT MAX(id) FROM tag));
