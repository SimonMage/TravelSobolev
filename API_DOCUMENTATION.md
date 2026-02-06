# Travel Planner - Documentazione API Completa

## Panoramica

Questa documentazione descrive tutti gli endpoint REST API del progetto Travel Planner. Tutte le API utilizzano **nomi leggibili** invece di ID numerici per facilitare l'integrazione da programmi esterni.

## Base URL
```
http://localhost:8080/api
```

## Autenticazione

Le API protette richiedono un token JWT Bearer nell'header Authorization:
```
Authorization: Bearer <token>
```

Per ottenere un token, utilizza gli endpoint di autenticazione.

---

## 📍 AUTENTICAZIONE

### 1. Registrazione Utente
**Endpoint:** `POST /auth/register`
**Autenticazione:** Non richiesta
**Descrizione:** Registra un nuovo utente

**Request Body:**
```json
{
  "username": "mario.rossi",
  "email": "mario.rossi@example.com",
  "password": "SecurePassword123!"
}
```

**Response:** `201 Created`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "mario.rossi"
}
```

**Esempio cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mario.rossi",
    "email": "mario.rossi@example.com",
    "password": "SecurePassword123!"
  }'
```

**Esempio da codice esterno (Python):**
```python
import requests

url = "http://localhost:8080/api/auth/register"
data = {
    "username": "mario.rossi",
    "email": "mario.rossi@example.com",
    "password": "SecurePassword123!"
}

response = requests.post(url, json=data)
token = response.json()["token"]
print(f"Token ricevuto: {token}")
```

**Esempio da codice esterno (JavaScript):**
```javascript
const response = await fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'mario.rossi',
    email: 'mario.rossi@example.com',
    password: 'SecurePassword123!'
  })
});

const data = await response.json();
const token = data.token;
console.log('Token ricevuto:', token);
```

### 2. Login
**Endpoint:** `POST /auth/login`
**Autenticazione:** Non richiesta
**Descrizione:** Effettua il login e ottiene un token JWT

**Request Body:**
```json
{
  "username": "mario.rossi",
  "password": "SecurePassword123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "mario.rossi"
}
```

**Esempio cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mario.rossi",
    "password": "SecurePassword123!"
  }'
```

---

## 🌍 GEOGRAFIA (Countries, Regions, Cities)

### 3. Ottieni tutti i paesi
**Endpoint:** `GET /countries`
**Autenticazione:** Non richiesta
**Descrizione:** Restituisce l'elenco di tutti i paesi

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Italy"
  },
  {
    "id": 2,
    "name": "France"
  }
]
```

**Esempio cURL:**
```bash
curl http://localhost:8080/api/countries
```

**Esempio Python:**
```python
import requests

response = requests.get("http://localhost:8080/api/countries")
countries = response.json()
for country in countries:
    print(f"Paese: {country['name']}")
```

### 4. Ottieni paese per nome
**Endpoint:** `GET /countries/{name}`
**Autenticazione:** Non richiesta
**Descrizione:** Restituisce i dettagli di un paese specifico per nome

**Path Parameters:**
- `name` (string): Nome del paese (case-insensitive)

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Italy"
}
```

**Esempio cURL:**
```bash
curl http://localhost:8080/api/countries/Italy
curl http://localhost:8080/api/countries/italy  # case-insensitive
```

### 5. Ottieni tutte le regioni
**Endpoint:** `GET /regions`
**Autenticazione:** Non richiesta
**Descrizione:** Restituisce tutte le regioni, opzionalmente filtrate per paese

**Query Parameters:**
- `countryName` (optional, string): Nome del paese per filtrare le regioni

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Lazio",
    "countryId": 1,
    "countryName": "Italy"
  },
  {
    "id": 2,
    "name": "Lombardia",
    "countryId": 1,
    "countryName": "Italy"
  }
]
```

**Esempi cURL:**
```bash
# Tutte le regioni
curl http://localhost:8080/api/regions

# Regioni filtrate per paese
curl "http://localhost:8080/api/regions?countryName=Italy"
```

**Esempio JavaScript:**
```javascript
// Ottieni regioni dell'Italia
const response = await fetch('http://localhost:8080/api/regions?countryName=Italy');
const regions = await response.json();
regions.forEach(region => {
  console.log(`Regione: ${region.name}`);
});
```

### 6. Ottieni regione per nome
**Endpoint:** `GET /regions/{regionName}`
**Autenticazione:** Non richiesta
**Descrizione:** Restituisce una regione specifica per nome

**Path Parameters:**
- `regionName` (string): Nome della regione (case-insensitive)

**Query Parameters:**
- `country` (optional, string): Nome del paese per disambiguare (se ci sono regioni con lo stesso nome)

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Lazio",
  "countryId": 1,
  "countryName": "Italy"
}
```

**Esempi cURL:**
```bash
curl http://localhost:8080/api/regions/Lazio
curl "http://localhost:8080/api/regions/Lazio?country=Italy"
```

### 7. Ottieni tutte le città
**Endpoint:** `GET /cities`
**Autenticazione:** Non richiesta
**Descrizione:** Restituisce tutte le città, opzionalmente filtrate per regione o tag

**Query Parameters:**
- `regionName` (optional, string): Nome della regione per filtrare le città
- `tags` (optional, array of strings): Tag per filtrare le città (es: mare, montagna)

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Rome",
    "latitude": 41.9028,
    "longitude": 12.4964,
    "regionId": 1,
    "regionName": "Lazio",
    "countryName": "Italy",
    "tags": ["arte", "cultura", "storia"]
  }
]
```

**Esempi cURL:**
```bash
# Tutte le città
curl http://localhost:8080/api/cities

# Città filtrate per regione
curl "http://localhost:8080/api/cities?regionName=Lazio"

# Città filtrate per tag
curl "http://localhost:8080/api/cities?tags=mare&tags=cultura"
```

**Esempio Python:**
```python
import requests

# Ottieni città per regione
response = requests.get("http://localhost:8080/api/cities", params={"regionName": "Lazio"})
cities = response.json()
for city in cities:
    print(f"Città: {city['name']} - Lat: {city['latitude']}, Lon: {city['longitude']}")
```

### 8. Cerca città per nome
**Endpoint:** `GET /cities/search`
**Autenticazione:** Opzionale (se autenticato, salva la ricerca nella cronologia)
**Descrizione:** Cerca città per nome nel database locale e, se non trovate, su GeoAPIfy

**Query Parameters:**
- `query` (required, string): Testo di ricerca

**Response:** `200 OK`
```json
[
  {
    "id": "local_1",
    "name": "Rome",
    "region": "Lazio",
    "country": "Italy",
    "latitude": 41.9028,
    "longitude": 12.4964,
    "cityId": 1,
    "tags": ["arte", "cultura"],
    "source": "local"
  }
]
```

**Esempio cURL:**
```bash
curl "http://localhost:8080/api/cities/search?query=Rome"

# Con autenticazione
curl "http://localhost:8080/api/cities/search?query=Rome" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 9. Ottieni città per nome
**Endpoint:** `GET /cities/{cityName}`
**Autenticazione:** Opzionale (se autenticato, salva nella cronologia)
**Descrizione:** Restituisce i dettagli di una città specifica per nome

**Path Parameters:**
- `cityName` (string): Nome della città (case-insensitive)

**Query Parameters:**
- `region` (optional, string): Nome della regione per disambiguare

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Rome",
  "latitude": 41.9028,
  "longitude": 12.4964,
  "regionId": 1,
  "regionName": "Lazio",
  "countryName": "Italy",
  "tags": ["arte", "cultura", "storia"]
}
```

**Esempi cURL:**
```bash
curl http://localhost:8080/api/cities/Rome
curl "http://localhost:8080/api/cities/Rome?region=Lazio"
```

**Esempio JavaScript:**
```javascript
// Ottieni informazioni su Roma
const response = await fetch('http://localhost:8080/api/cities/Rome?region=Lazio');
const city = await response.json();
console.log(`Città: ${city.name}`);
console.log(`Coordinate: ${city.latitude}, ${city.longitude}`);
console.log(`Tags: ${city.tags.join(', ')}`);
```

### 10. Ottieni meteo per città
**Endpoint:** `GET /cities/{cityName}/weather`
**Autenticazione:** Non richiesta
**Descrizione:** Restituisce i dati meteo attuali per una città (da OpenWeatherMap)

**Path Parameters:**
- `cityName` (string): Nome della città

**Query Parameters:**
- `region` (optional, string): Nome della regione per disambiguare

**Response:** `200 OK`
```json
{
  "city": "Rome",
  "temperature": 22.5,
  "description": "Clear sky",
  "humidity": 60,
  "windSpeed": 3.5,
  "icon": "01d"
}
```

**Esempi cURL:**
```bash
curl http://localhost:8080/api/cities/Rome/weather
curl "http://localhost:8080/api/cities/Milan/weather?region=Lombardia"
```

**Esempio Python:**
```python
import requests

response = requests.get("http://localhost:8080/api/cities/Rome/weather")
weather = response.json()
print(f"Temperatura a {weather['city']}: {weather['temperature']}°C")
print(f"Condizioni: {weather['description']}")
```

### 11. Ottieni POI per città
**Endpoint:** `GET /cities/{cityName}/pois`
**Autenticazione:** Non richiesta
**Descrizione:** Restituisce i punti di interesse (POI) per una città (da GeoAPIfy)

**Path Parameters:**
- `cityName` (string): Nome della città

**Query Parameters:**
- `region` (optional, string): Nome della regione per disambiguare

**Response:** `200 OK`
```json
[
  {
    "id": "poi_123",
    "name": "Colosseum",
    "category": "tourism.attraction",
    "address": "Piazza del Colosseo, 1",
    "latitude": 41.8902,
    "longitude": 12.4922
  },
  {
    "id": "poi_124",
    "name": "Trevi Fountain",
    "category": "tourism.attraction",
    "address": "Piazza di Trevi",
    "latitude": 41.9009,
    "longitude": 12.4833
  }
]
```

**Esempi cURL:**
```bash
curl http://localhost:8080/api/cities/Rome/pois
curl "http://localhost:8080/api/cities/Florence/pois?region=Toscana"
```

### 12. Filtra città per tag
**Endpoint:** `GET /cities/filter`
**Autenticazione:** Non richiesta
**Descrizione:** Filtra città per uno o più tag

**Query Parameters:**
- `tags` (required, array of strings): Lista di tag

**Response:** `200 OK` - Stessa struttura di GET /cities

**Esempio cURL:**
```bash
curl "http://localhost:8080/api/cities/filter?tags=mare&tags=cultura"
```

---

## 🗺️ TRIPS (Viaggi e Tappe)

**⚠️ Tutte le API dei trip richiedono autenticazione JWT**

### 13. Ottieni tutti i viaggi dell'utente
**Endpoint:** `GET /trips`
**Autenticazione:** Richiesta
**Descrizione:** Restituisce tutti i viaggi dell'utente autenticato

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Summer Vacation 2026",
    "startDate": "2026-07-01",
    "endDate": "2026-07-15",
    "stops": []
  }
]
```

**Esempio cURL:**
```bash
curl http://localhost:8080/api/trips \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Esempio Python:**
```python
import requests

headers = {"Authorization": f"Bearer {token}"}
response = requests.get("http://localhost:8080/api/trips", headers=headers)
trips = response.json()
for trip in trips:
    print(f"Viaggio: {trip['name']} ({trip['startDate']} - {trip['endDate']})")
```

### 14. Ottieni viaggio per nome
**Endpoint:** `GET /trips/{tripName}`
**Autenticazione:** Richiesta
**Descrizione:** Restituisce i dettagli di un viaggio specifico per nome

**Path Parameters:**
- `tripName` (string): Nome del viaggio (case-insensitive, URL-encoded se contiene spazi)

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Summer Vacation 2026",
  "startDate": "2026-07-01",
  "endDate": "2026-07-15",
  "stops": [
    {
      "stopName": "rome_2026-07-03",
      "cityName": "Rome",
      "regionName": "Lazio",
      "stopDate": "2026-07-03",
      "notes": "Visit Colosseum and Roman Forum",
      "pois": []
    }
  ]
}
```

**Esempi cURL:**
```bash
# Nome senza spazi
curl http://localhost:8080/api/trips/SummerTrip \
  -H "Authorization: Bearer YOUR_TOKEN"

# Nome con spazi (URL-encoded)
curl "http://localhost:8080/api/trips/Summer%20Vacation%202026" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Esempio JavaScript:**
```javascript
const tripName = "Summer Vacation 2026";
const encodedName = encodeURIComponent(tripName);

const response = await fetch(`http://localhost:8080/api/trips/${encodedName}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});

const trip = await response.json();
console.log(`Viaggio: ${trip.name}`);
console.log(`Tappe: ${trip.stops.length}`);
```

### 15. Crea nuovo viaggio
**Endpoint:** `POST /trips`
**Autenticazione:** Richiesta
**Descrizione:** Crea un nuovo viaggio

**Request Body:**
```json
{
  "name": "Summer Vacation 2026",
  "startDate": "2026-07-01",
  "endDate": "2026-07-15"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Summer Vacation 2026",
  "startDate": "2026-07-01",
  "endDate": "2026-07-15",
  "stops": []
}
```

**Esempio cURL:**
```bash
curl -X POST http://localhost:8080/api/trips \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Summer Vacation 2026",
    "startDate": "2026-07-01",
    "endDate": "2026-07-15"
  }'
```

**Esempio Python:**
```python
import requests

headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}

data = {
    "name": "Summer Vacation 2026",
    "startDate": "2026-07-01",
    "endDate": "2026-07-15"
}

response = requests.post("http://localhost:8080/api/trips", headers=headers, json=data)
trip = response.json()
print(f"Viaggio creato: {trip['name']}")
```

### 16. Aggiorna viaggio
**Endpoint:** `PUT /trips/{tripName}`
**Autenticazione:** Richiesta
**Descrizione:** Aggiorna i dettagli di un viaggio esistente

**Path Parameters:**
- `tripName` (string): Nome del viaggio da aggiornare

**Request Body:** (tutti i campi sono opzionali)
```json
{
  "name": "Summer Vacation 2026 - Updated",
  "startDate": "2026-07-01",
  "endDate": "2026-07-20"
}
```

**Response:** `200 OK` - Viaggio aggiornato

**Esempio cURL:**
```bash
curl -X PUT "http://localhost:8080/api/trips/Summer%20Vacation%202026" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "endDate": "2026-07-20"
  }'
```

### 17. Elimina viaggio
**Endpoint:** `DELETE /trips/{tripName}`
**Autenticazione:** Richiesta
**Descrizione:** Elimina un viaggio e tutte le sue tappe

**Path Parameters:**
- `tripName` (string): Nome del viaggio da eliminare

**Response:** `204 No Content`

**Esempio cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/trips/Summer%20Vacation%202026" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 18. Esporta viaggio in CSV
**Endpoint:** `GET /trips/{tripName}/export`
**Autenticazione:** Richiesta
**Descrizione:** Esporta un viaggio in formato CSV

**Path Parameters:**
- `tripName` (string): Nome del viaggio da esportare

**Response:** `200 OK` (Content-Type: text/csv)
```csv
Trip Name,Start Date,End Date,Stop Name,Stop Date,City,Region,Notes,POI External ID,POI Name
"Summer Vacation 2026","2026-07-01","2026-07-15","rome_2026-07-03","2026-07-03","Rome","Lazio","Visit Colosseum","poi_123","Colosseum"
```

**Esempio cURL:**
```bash
curl "http://localhost:8080/api/trips/Summer%20Vacation%202026/export" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o trip_export.csv
```

---

## 🚩 STOP (Tappe del Viaggio)

### 19. Aggiungi tappa a viaggio
**Endpoint:** `POST /trips/{tripName}/stops`
**Autenticazione:** Richiesta
**Descrizione:** Aggiunge una nuova tappa a un viaggio. Il nome della tappa viene generato automaticamente come `cittàminuscola_data`

**Path Parameters:**
- `tripName` (string): Nome del viaggio

**Request Body:**
```json
{
  "cityName": "Rome",
  "regionName": "Lazio",
  "stopDate": "2026-07-03",
  "notes": "Visit Colosseum and Roman Forum"
}
```

**Campi:**
- `cityName` (required): Nome della città (case-insensitive)
- `regionName` (optional): Nome della regione per disambiguare città con nomi simili
- `stopDate` (required): Data della tappa (deve essere compresa tra startDate e endDate del trip)
- `notes` (optional): Note sulla tappa

**Response:** `201 Created`
```json
{
  "stopName": "rome_2026-07-03",
  "cityName": "Rome",
  "regionName": "Lazio",
  "stopDate": "2026-07-03",
  "notes": "Visit Colosseum and Roman Forum",
  "pois": []
}
```

**Esempio cURL:**
```bash
curl -X POST "http://localhost:8080/api/trips/Summer%20Vacation%202026/stops" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cityName": "Rome",
    "regionName": "Lazio",
    "stopDate": "2026-07-03",
    "notes": "Visit Colosseum and Roman Forum"
  }'
```

**Esempio Python:**
```python
import requests

headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}

data = {
    "cityName": "Rome",
    "regionName": "Lazio",
    "stopDate": "2026-07-03",
    "notes": "Visit Colosseum"
}

trip_name = "Summer Vacation 2026"
response = requests.post(
    f"http://localhost:8080/api/trips/{trip_name}/stops",
    headers=headers,
    json=data
)

stop = response.json()
print(f"Tappa creata: {stop['stopName']} a {stop['cityName']}")
```

**Esempio JavaScript:**
```javascript
const tripName = encodeURIComponent("Summer Vacation 2026");
const stopData = {
  cityName: "Rome",
  regionName: "Lazio",
  stopDate: "2026-07-03",
  notes: "Visit Colosseum"
};

const response = await fetch(`http://localhost:8080/api/trips/${tripName}/stops`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(stopData)
});

const stop = await response.json();
console.log(`Tappa creata: ${stop.stopName}`);
```

### 20. Aggiorna tappa
**Endpoint:** `PUT /trips/{tripName}/stops/{stopName}`
**Autenticazione:** Richiesta
**Descrizione:** Aggiorna i dettagli di una tappa esistente

**Path Parameters:**
- `tripName` (string): Nome del viaggio
- `stopName` (string): Nome della tappa (es: "rome_2026-07-03")

**Request Body:** (tutti i campi sono opzionali)
```json
{
  "stopDate": "2026-07-04",
  "notes": "Visit Colosseum, Roman Forum and Pantheon"
}
```

**⚠️ Nota:** Se cambi la `stopDate`, il nome della tappa verrà automaticamente aggiornato

**Response:** `200 OK` - Tappa aggiornata

**Esempio cURL:**
```bash
curl -X PUT "http://localhost:8080/api/trips/Summer%20Vacation%202026/stops/rome_2026-07-03" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Visit Colosseum, Roman Forum and Pantheon"
  }'
```

### 21. Elimina tappa
**Endpoint:** `DELETE /trips/{tripName}/stops/{stopName}`
**Autenticazione:** Richiesta
**Descrizione:** Elimina una tappa da un viaggio

**Path Parameters:**
- `tripName` (string): Nome del viaggio
- `stopName` (string): Nome della tappa

**Response:** `204 No Content`

**Esempio cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/trips/Summer%20Vacation%202026/stops/rome_2026-07-03" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📍 POI (Punti di Interesse nelle Tappe)

### 22. Aggiungi POI a tappa
**Endpoint:** `POST /trips/{tripName}/stops/{stopName}/pois`
**Autenticazione:** Richiesta
**Descrizione:** Aggiunge un punto di interesse a una tappa

**Path Parameters:**
- `tripName` (string): Nome del viaggio
- `stopName` (string): Nome della tappa

**Request Body:**
```json
{
  "externalId": "poi_colosseum_123",
  "name": "Colosseum",
  "rawJson": {
    "address": "Piazza del Colosseo, 1",
    "category": "tourism.attraction",
    "latitude": 41.8902,
    "longitude": 12.4922
  }
}
```

**Campi:**
- `externalId` (required): ID univoco del POI (es: da GeoAPIfy)
- `name` (required): Nome del POI
- `rawJson` (required): Dati completi del POI in formato JSON

**Response:** `201 Created`
```json
{
  "externalId": "poi_colosseum_123",
  "name": "Colosseum",
  "rawJson": {
    "address": "Piazza del Colosseo, 1",
    "category": "tourism.attraction",
    "latitude": 41.8902,
    "longitude": 12.4922
  }
}
```

**Esempio cURL:**
```bash
curl -X POST "http://localhost:8080/api/trips/Summer%20Vacation%202026/stops/rome_2026-07-03/pois" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "poi_colosseum_123",
    "name": "Colosseum",
    "rawJson": {
      "address": "Piazza del Colosseo, 1",
      "category": "tourism.attraction"
    }
  }'
```

**Esempio Python:**
```python
import requests

headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}

poi_data = {
    "externalId": "poi_colosseum_123",
    "name": "Colosseum",
    "rawJson": {
        "address": "Piazza del Colosseo, 1",
        "category": "tourism.attraction",
        "latitude": 41.8902,
        "longitude": 12.4922
    }
}

trip_name = "Summer Vacation 2026"
stop_name = "rome_2026-07-03"

response = requests.post(
    f"http://localhost:8080/api/trips/{trip_name}/stops/{stop_name}/pois",
    headers=headers,
    json=poi_data
)

poi = response.json()
print(f"POI aggiunto: {poi['name']}")
```

### 23. Rimuovi POI da tappa
**Endpoint:** `DELETE /trips/{tripName}/stops/{stopName}/pois/{poiExternalId}`
**Autenticazione:** Richiesta
**Descrizione:** Rimuove un POI da una tappa

**Path Parameters:**
- `tripName` (string): Nome del viaggio
- `stopName` (string): Nome della tappa
- `poiExternalId` (string): External ID del POI da rimuovere

**Response:** `204 No Content`

**Esempio cURL:**
```bash
curl -X DELETE "http://localhost:8080/api/trips/Summer%20Vacation%202026/stops/rome_2026-07-03/pois/poi_colosseum_123" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 👤 UTENTE

### 24. Ottieni profilo utente corrente
**Endpoint:** `GET /users/me`
**Autenticazione:** Richiesta
**Descrizione:** Restituisce il profilo dell'utente autenticato

**Response:** `200 OK`
```json
{
  "id": 1,
  "createdAt": "2026-01-15T10:30:00",
  "profile": {
    "username": "mario.rossi",
    "email": "mario.rossi@example.com",
    "firstName": "Mario",
    "lastName": "Rossi",
    "preferredUnits": "metric"
  }
}
```

**Esempio cURL:**
```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 25. Aggiorna profilo utente
**Endpoint:** `PUT /users/me/profile`
**Autenticazione:** Richiesta
**Descrizione:** Aggiorna il profilo dell'utente autenticato

**Request Body:**
```json
{
  "username": "mario.rossi",
  "email": "mario.rossi@example.com",
  "firstName": "Mario",
  "lastName": "Rossi",
  "preferredUnits": "metric"
}
```

**Response:** `200 OK` - Profilo aggiornato

**Esempio cURL:**
```bash
curl -X PUT http://localhost:8080/api/users/me/profile \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Mario",
    "lastName": "Rossi",
    "preferredUnits": "metric"
  }'
```

---

## 🔍 CRONOLOGIA RICERCHE

### 26. Ottieni cronologia ricerche
**Endpoint:** `GET /search-history`
**Autenticazione:** Richiesta
**Descrizione:** Restituisce la cronologia delle ricerche dell'utente

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "query": "Rome",
    "cityId": 1,
    "cityName": "Rome",
    "searchedAt": "2026-02-05T10:15:30"
  }
]
```

**Esempio cURL:**
```bash
curl http://localhost:8080/api/search-history \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 27. Cancella cronologia ricerche
**Endpoint:** `DELETE /search-history`
**Autenticazione:** Richiesta
**Descrizione:** Cancella tutta la cronologia delle ricerche dell'utente

**Response:** `204 No Content`

**Esempio cURL:**
```bash
curl -X DELETE http://localhost:8080/api/search-history \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📍 POI PERSONALIZZATI

### 28. Crea un POI personalizzato
**Endpoint:** `POST /pois`
**Autenticazione:** Richiesta
**Descrizione:** Crea un nuovo punto di interesse personalizzato per l'utente

**Request Body:**
```json
{
  "name": "Ristorante preferito",
  "description": "Il miglior ristorante di Roma",
  "latitude": 41.9028,
  "longitude": 12.4964
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Ristorante preferito",
  "description": "Il miglior ristorante di Roma",
  "latitude": 41.9028,
  "longitude": 12.4964,
  "createdAt": "2026-02-05T10:15:30"
}
```

**Esempio cURL:**
```bash
curl -X POST http://localhost:8080/api/pois \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Ristorante preferito",
    "description": "Il miglior ristorante di Roma",
    "latitude": 41.9028,
    "longitude": 12.4964
  }'
```

**Note:**
- Il campo `name` è obbligatorio e deve essere univoco per l'utente
- I campi `description`, `latitude` e `longitude` sono opzionali
- Se esiste già un POI con lo stesso nome per l'utente, viene restituito errore 400

### 29. Ottieni tutti i POI personalizzati
**Endpoint:** `GET /pois`
**Autenticazione:** Richiesta
**Descrizione:** Restituisce tutti i POI personalizzati dell'utente

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Ristorante preferito",
    "description": "Il miglior ristorante di Roma",
    "latitude": 41.9028,
    "longitude": 12.4964,
    "createdAt": "2026-02-05T10:15:30"
  },
  {
    "id": 2,
    "name": "Museo da visitare",
    "description": "Museo interessante a Milano",
    "latitude": 45.4642,
    "longitude": 9.1900,
    "createdAt": "2026-02-04T14:20:15"
  }
]
```

**Esempio cURL:**
```bash
curl http://localhost:8080/api/pois \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 30. Ottieni un POI specifico
**Endpoint:** `GET /pois/{poiId}`
**Autenticazione:** Richiesta
**Descrizione:** Restituisce un POI personalizzato specifico

**Path Parameters:**
- `poiId` (integer): ID del POI

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Ristorante preferito",
  "description": "Il miglior ristorante di Roma",
  "latitude": 41.9028,
  "longitude": 12.4964,
  "createdAt": "2026-02-05T10:15:30"
}
```

**Esempio cURL:**
```bash
curl http://localhost:8080/api/pois/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 31. Elimina un POI specifico
**Endpoint:** `DELETE /pois/{poiId}`
**Autenticazione:** Richiesta
**Descrizione:** Elimina un POI personalizzato specifico

**Path Parameters:**
- `poiId` (integer): ID del POI da eliminare

**Response:** `204 No Content`

**Esempio cURL:**
```bash
curl -X DELETE http://localhost:8080/api/pois/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 32. Elimina tutti i POI personalizzati
**Endpoint:** `DELETE /pois`
**Autenticazione:** Richiesta
**Descrizione:** Elimina tutti i POI personalizzati dell'utente

**Response:** `204 No Content`

**Esempio cURL:**
```bash
curl -X DELETE http://localhost:8080/api/pois \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🏷️ TAG

### 33. Ottieni tutti i tag
**Endpoint:** `GET /tags`
**Autenticazione:** Non richiesta
**Descrizione:** Restituisce tutti i tag disponibili per le città

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "mare"
  },
  {
    "id": 2,
    "name": "montagna"
  },
  {
    "id": 3,
    "name": "cultura"
  }
]
```

**Esempio cURL:**
```bash
curl http://localhost:8080/api/tags
```

---

## ⚠️ GESTIONE ERRORI

Tutte le API restituiscono errori in formato standard:

**400 Bad Request** - Richiesta non valida
```json
{
  "timestamp": "2026-02-05T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Start date must be before or equal to end date",
  "path": "/api/trips"
}
```

**401 Unauthorized** - Token mancante o non valido
```json
{
  "timestamp": "2026-02-05T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/trips"
}
```

**404 Not Found** - Risorsa non trovata
```json
{
  "timestamp": "2026-02-05T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Trip 'Summer Vacation' not found for user",
  "path": "/api/trips/Summer%20Vacation"
}
```

**409 Conflict** - Conflitto (es: username già esistente)
```json
{
  "timestamp": "2026-02-05T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Username already exists",
  "path": "/api/auth/register"
}
```

---

## 💡 NOTE IMPORTANTI

### URL Encoding
I nomi nei path devono essere URL-encoded se contengono spazi o caratteri speciali:
- Spazio diventa `%20`
- Esempio: "Summer Vacation 2026" → "Summer%20Vacation%202026"

### Case Insensitive
Tutte le ricerche per nome sono case-insensitive:
- "Rome", "rome", "ROME" trovano tutti la stessa città

### Nomi delle Tappe (Stop Names)
Le tappe vengono identificate dal nome generato automaticamente:
- Formato: `cittàminuscola_data`
- Esempio: `rome_2026-07-03`, `milan_2026-07-05`
- Gli spazi nella città vengono sostituiti con underscore

### POI External ID
I POI sono identificati dal loro `externalId` univoco:
- Deve essere univoco nel sistema
- Suggerito formato: `poi_nome_identificativo`
- Esempio: `poi_colosseum_123`, `poi_trevifountain_456`

### Autenticazione
- Le API pubbliche (geografia) non richiedono autenticazione
- Le API dei trip, utente e cronologia richiedono token JWT
- Il token si ottiene tramite login/registrazione
- Il token va inserito nell'header: `Authorization: Bearer YOUR_TOKEN`

### Date Format
Tutte le date sono in formato ISO 8601: `YYYY-MM-DD`
- Esempio: "2026-07-15"

---

## 🚀 WORKFLOW COMPLETO - ESEMPIO PRATICO

### Scenario: Creare un viaggio in Italia con tappe e POI

```python
import requests

BASE_URL = "http://localhost:8080/api"

# 1. REGISTRAZIONE/LOGIN
register_data = {
    "username": "traveler",
    "email": "traveler@example.com",
    "password": "SecurePass123!"
}
response = requests.post(f"{BASE_URL}/auth/register", json=register_data)
token = response.json()["token"]

headers = {"Authorization": f"Bearer {token}"}

# 2. ESPLORA DESTINAZIONI
# Ottieni tutte le città italiane
italy_cities = requests.get(f"{BASE_URL}/cities?regionName=Lazio").json()
print(f"Trovate {len(italy_cities)} città nel Lazio")

# Ottieni meteo per Roma
weather = requests.get(f"{BASE_URL}/cities/Rome/weather").json()
print(f"Meteo a Roma: {weather['temperature']}°C - {weather['description']}")

# Ottieni POI di Roma
pois = requests.get(f"{BASE_URL}/cities/Rome/pois").json()
print(f"POI trovati a Roma: {len(pois)}")

# 3. CREA VIAGGIO
trip_data = {
    "name": "Italian Summer 2026",
    "startDate": "2026-07-01",
    "endDate": "2026-07-10"
}
trip = requests.post(f"{BASE_URL}/trips", headers=headers, json=trip_data).json()
print(f"Viaggio creato: {trip['name']}")

# 4. AGGIUNGI TAPPE
# Tappa a Roma
stop1_data = {
    "cityName": "Rome",
    "regionName": "Lazio",
    "stopDate": "2026-07-02",
    "notes": "Visit ancient Rome"
}
stop1 = requests.post(
    f"{BASE_URL}/trips/Italian%20Summer%202026/stops",
    headers=headers,
    json=stop1_data
).json()
print(f"Tappa creata: {stop1['stopName']}")

# Tappa a Florence
stop2_data = {
    "cityName": "Florence",
    "regionName": "Toscana",
    "stopDate": "2026-07-05",
    "notes": "Renaissance art"
}
stop2 = requests.post(
    f"{BASE_URL}/trips/Italian%20Summer%202026/stops",
    headers=headers,
    json=stop2_data
).json()

# 5. AGGIUNGI POI ALLA TAPPA
poi_data = {
    "externalId": "poi_colosseum_geo123",
    "name": "Colosseum",
    "rawJson": {
        "address": "Piazza del Colosseo, 1",
        "category": "tourism.attraction",
        "latitude": 41.8902,
        "longitude": 12.4922,
        "opening_hours": "9:00-19:00"
    }
}
poi = requests.post(
    f"{BASE_URL}/trips/Italian%20Summer%202026/stops/{stop1['stopName']}/pois",
    headers=headers,
    json=poi_data
).json()
print(f"POI aggiunto: {poi['name']}")

# 6. OTTIENI VIAGGIO COMPLETO
final_trip = requests.get(
    f"{BASE_URL}/trips/Italian%20Summer%202026",
    headers=headers
).json()
print(f"\n=== VIAGGIO COMPLETO ===")
print(f"Nome: {final_trip['name']}")
print(f"Date: {final_trip['startDate']} - {final_trip['endDate']}")
print(f"Tappe: {len(final_trip['stops'])}")
for stop in final_trip['stops']:
    print(f"  - {stop['cityName']} il {stop['stopDate']}")
    print(f"    POI: {len(stop['pois'])}")

# 7. ESPORTA IN CSV
csv_response = requests.get(
    f"{BASE_URL}/trips/Italian%20Summer%202026/export",
    headers=headers
)
with open("trip_export.csv", "w") as f:
    f.write(csv_response.text)
print("\nViaggio esportato in trip_export.csv")
```

---

## 📝 RIEPILOGO MODIFICHE RISPETTO ALLA VERSIONE CON ID

### Cosa è cambiato:

1. **Endpoint Geografia**: Ora usano nomi invece di ID
   - `/cities/{id}` → `/cities/{cityName}`
   - `/countries/{id}` → `/countries/{name}`
   - `/regions/{id}` → `/regions/{regionName}`

2. **Endpoint Trips**: Identificati per nome
   - `/trips/{id}` → `/trips/{tripName}`
   - Esempio: `/trips/1` → `/trips/Summer%20Vacation%202026`

3. **Stop (Tappe)**: Identificati per nome auto-generato
   - Nome formato: `cittàminuscola_data`
   - Esempio: `rome_2026-07-03`
   - Request richiede `cityName` invece di `cityId`

4. **POI**: Identificati per `externalId`
   - `/pois/{id}` → `/pois/{externalId}`
   - Esempio: `/pois/1` → `/pois/poi_colosseum_123`

### Cosa NON è cambiato:

- Gli ID interni sono ancora usati nel database per le relazioni
- La struttura dati interna rimane la stessa
- Le performance non sono impattate
- La logica di business è invariata

---

## 🔧 TROUBLESHOOTING

### Errore "Multiple cities found"
**Problema:** Ci sono più città con lo stesso nome
**Soluzione:** Specifica il parametro `region` nella query
```bash
curl "http://localhost:8080/api/cities/Springfield?region=Illinois"
```

### Errore "Stop date must be within trip date range"
**Problema:** La data della tappa è fuori dal range del viaggio
**Soluzione:** Assicurati che stopDate sia tra startDate e endDate del trip

### Errore "A stop for [city] on [date] already exists"
**Problema:** Stai cercando di aggiungere una tappa per la stessa città nella stessa data
**Soluzione:** Cambia la data o la città

### Token Expired
**Problema:** Il token JWT è scaduto
**Soluzione:** Fai nuovamente login per ottenere un nuovo token

---

## 📚 RISORSE AGGIUNTIVE

### Swagger UI
Documentazione interattiva disponibile a:
```
http://localhost:8080/swagger-ui.html
```

### Avvio Backend
```bash
start-backend.bat
```

### Database
- PostgreSQL su localhost:5432
- Database: sobolev
- User: postgres
- Password: postgres

---

**Data Documento:** 5 Febbraio 2026
**Versione API:** 1.0.0
**Progetto:** Travel Planner Sobolev
