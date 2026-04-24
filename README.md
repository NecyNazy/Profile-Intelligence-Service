# Profile Intelligence Service — Insighta Labs

A production-grade demographic intelligence API built for **Insighta Labs**. This service collects, enriches, stores, and queries profile data using external intelligence APIs. It supports advanced filtering, sorting, pagination, and a rule-based natural language query engine — enabling marketing teams, product analysts, and growth teams to segment and explore demographic data efficiently.

---

## 🚀 Live API

**Base URL:** `https://your-deployment.up.railway.app`

> All endpoints are live and accept requests. See testing examples below.

---

## 🧠 How It Works

```
Client Request
     ↓
Spring Boot Controller
     ↓
Service Layer (filtering, sorting, pagination logic)
     ↓
JPA Repository (@Query with optional filters)
     ↓
PostgreSQL Database (2026 seeded profiles)
     ↓
Structured JSON Response
```

For natural language queries, an additional parsing step sits between the controller and service:

```
?q="young males from nigeria"
     ↓
NaturalLanguageQueryParser (rule-based string analysis)
     ↓
ProfileFilterDto (gender=male, minAge=16, maxAge=24, countryId=NG)
     ↓
Same filter pipeline as /api/profiles
```

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Persistence | Spring Data JPA |
| Reactive Client | Spring WebFlux (WebClient) |
| Database | PostgreSQL (Railway) |
| Boilerplate | Lombok |
| ID Generation | UUID v7 |
| Deployment | Railway |

---

## 🗄 Database Schema

```sql
CREATE TABLE profiles (
    id                  UUID PRIMARY KEY,
    name                VARCHAR UNIQUE NOT NULL,
    gender              VARCHAR,
    gender_probability  FLOAT,
    age                 INT,
    age_group           VARCHAR,
    country_id          VARCHAR(2),
    country_name        VARCHAR,
    country_probability FLOAT,
    created_at          TIMESTAMP
);
```

| Field | Type | Notes |
|---|---|---|
| `id` | UUID v7 | Primary key, time-ordered |
| `name` | VARCHAR UNIQUE | Person's full name |
| `gender` | VARCHAR | `male` or `female` |
| `gender_probability` | FLOAT | Confidence score from Genderize API |
| `age` | INT | Exact age from Agify API |
| `age_group` | VARCHAR | `child`, `teenager`, `adult`, `senior` |
| `country_id` | VARCHAR(2) | ISO 3166-1 alpha-2 code (e.g. `NG`, `KE`) |
| `country_name` | VARCHAR | Full country name |
| `country_probability` | FLOAT | Confidence score from Nationalize API |
| `created_at` | TIMESTAMP | Auto-set in UTC ISO 8601 |

---

## 📋 API Reference

### 1. Create & Enrich Profile

Submits a name, calls Genderize, Agify, and Nationalize APIs concurrently, then persists the enriched profile.

```
POST /api/profiles
Content-Type: application/json
```

**Request Body:**
```json
{ "name": "Chinaza" }
```

**Validation Rules:**
- Name must not be empty → `400`
- Name must contain only letters and spaces → `422`
- Name must be unique → `409` (if already exists)

**Success Response `201`:**
```json
{
  "status": "success",
  "data": {
    "id": "01926b3e-7c2a-7000-8f3d-1a2b3c4d5e6f",
    "name": "Chinaza",
    "gender": "female",
    "gender_probability": 0.89,
    "age": 28,
    "age_group": "adult",
    "country_id": "NG",
    "country_name": "Nigeria",
    "country_probability": 0.74,
    "created_at": "2026-04-18T14:22:00Z"
  }
}
```

---

### 2. Get All Profiles (with Filtering, Sorting & Pagination)

```
GET /api/profiles
```

#### Filter Parameters

| Parameter | Type | Description | Example |
|---|---|---|---|
| `gender` | string | `male` or `female` | `?gender=male` |
| `age_group` | string | `child`, `teenager`, `adult`, `senior` | `?age_group=adult` |
| `country_id` | string | ISO 2-letter code | `?country_id=NG` |
| `min_age` | integer | Minimum age (inclusive) | `?min_age=25` |
| `max_age` | integer | Maximum age (inclusive) | `?max_age=40` |
| `min_gender_probability` | float | Minimum gender confidence | `?min_gender_probability=0.8` |
| `min_country_probability` | float | Minimum country confidence | `?min_country_probability=0.5` |

#### Sort Parameters

| Parameter | Values | Default |
|---|---|---|
| `sort_by` | `age`, `created_at`, `gender_probability` | `created_at` |
| `order` | `asc`, `desc` | `desc` |

#### Pagination Parameters

| Parameter | Default | Max |
|---|---|---|
| `page` | `1` | — |
| `limit` | `10` | `50` |

All filters are combinable. Results strictly match all provided conditions.

**Example Requests:**
```
GET /api/profiles?gender=male&country_id=NG&min_age=25
GET /api/profiles?age_group=senior&sort_by=age&order=asc&page=2&limit=20
GET /api/profiles?gender=female&min_gender_probability=0.8&sort_by=gender_probability&order=desc
GET /api/profiles?min_age=18&max_age=35&country_id=KE&sort_by=age&order=asc&page=1&limit=10
```

**Success Response `200`:**
```json
{
  "status": "success",
  "page": 1,
  "limit": 10,
  "total": 2026,
  "data": [
    {
      "id": "01926b3e-7c2a-7000-8f3d-1a2b3c4d5e6f",
      "name": "John Adams",
      "gender": "male",
      "gender_probability": 0.98,
      "age": 37,
      "age_group": "adult",
      "country_id": "GB",
      "country_name": "United Kingdom",
      "country_probability": 0.52,
      "created_at": "2026-04-18T14:22:00Z"
    }
  ]
}
```

---

### 3. Get Profile by ID

```
GET /api/profiles/{id}
```

| Scenario | Status |
|---|---|
| Valid UUID, profile found | `200` |
| Valid UUID, not found | `404` |
| Malformed UUID | `400` |

**Success Response `200`:**
```json
{
  "status": "success",
  "data": {
    "id": "01926b3e-7c2a-7000-8f3d-1a2b3c4d5e6f",
    "name": "John Adams",
    "gender": "male",
    "gender_probability": 0.98,
    "age": 37,
    "age_group": "adult",
    "country_id": "GB",
    "country_name": "United Kingdom",
    "country_probability": 0.52,
    "created_at": "2026-04-18T14:22:00Z"
  }
}
```

---

### 4. Natural Language Search

```
GET /api/profiles/search?q={query}
```

Interprets plain English queries and converts them into structured filters using a **rule-based parser** (no AI or LLMs).

**Pagination applies here too:**
```
GET /api/profiles/search?q=young males from nigeria&page=1&limit=10
```

#### Supported Query Patterns

| Natural Language Query | Resolves To |
|---|---|
| `young males` | `gender=male` + `minAge=16` + `maxAge=24` |
| `females above 30` | `gender=female` + `minAge=30` |
| `people from angola` | `countryId=AO` |
| `adult males from kenya` | `gender=male` + `ageGroup=adult` + `countryId=KE` |
| `male and female teenagers above 17` | `ageGroup=teenager` + `minAge=17` |
| `senior women from ethiopia` | `gender=female` + `ageGroup=senior` + `countryId=ET` |
| `children below 10` | `ageGroup=child` + `maxAge=10` |
| `men between 25 and 40` | `gender=male` + `minAge=25` + `maxAge=40` |

#### Parsing Rules

- `young` → `minAge=16`, `maxAge=24` (not a stored age group — used for parsing only)
- `above / over / older than X` → `minAge=X`
- `below / under / younger than X` → `maxAge=X`
- `between X and Y` → `minAge=X`, `maxAge=Y`
- Gender: `male`, `man`, `men`, `boys` → `male`; `female`, `woman`, `women`, `girls` → `female`
- Age groups: `child/children`, `teen/teenager`, `adult/adults`, `senior/elderly/old`
- Countries: matched by full name (e.g. `nigeria` → `NG`, `south africa` → `ZA`)
- Queries are case-insensitive
- All parseable conditions are combinable in one query

**Success Response `200`:**
```json
{
  "status": "success",
  "page": 1,
  "limit": 10,
  "total": 34,
  "data": [ ... ]
}
```

**Uninterpretable Query `422`:**
```json
{
  "status": "error",
  "message": "Unable to interpret query"
}
```

**Missing Query Parameter `400`:**
```json
{
  "status": "error",
  "message": "Invalid query parameters"
}
```

---

## ⚠️ Error Handling

All errors follow this structure:

```json
{
  "status": "error",
  "message": "<detailed error message>"
}
```

| Status Code | Meaning | Example Trigger |
|---|---|---|
| `400` | Bad Request | Empty name, missing `q` param, malformed UUID |
| `404` | Not Found | Profile ID does not exist |
| `422` | Unprocessable Entity | Name contains numbers, query cannot be interpreted |
| `500/502` | Server / Gateway Error | DB connection failure, upstream API timeout |

---

## 🌐 External APIs Used

| API | Purpose | URL |
|---|---|---|
| Genderize.io | Predicts gender from name | `https://api.genderize.io` |
| Agify.io | Predicts age from name | `https://api.agify.io` |
| Nationalize.io | Predicts nationality from name | `https://api.nationalize.io` |

All three are called **concurrently** using `Mono.zip` (Spring WebFlux), minimizing response latency. Each call has a 10-second timeout with a safe fallback value if any API fails.

---

## ⚙️ Installation & Setup

### Prerequisites
- Java 21
- Maven
- PostgreSQL (local or Railway)

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/profile-intelligence-api.git
cd profile-intelligence-api
```

### 2. Configure Environment Variables

Set these in your environment or Railway Variables:

```env
SPRING_DATA_URL=jdbc:postgresql://<host>:<port>/<database>
SPRING_DATA_USERNAME=your_db_username
SPRING_DATA_PASSWORD=your_db_password
PORT=8080
```

For local development, fallback values in `application.properties` are used automatically.

### 3. Run the Application
```bash
./mvnw spring-boot:run
```

### 4. Seed the Database

The seed data (2026 profiles) is loaded via SQL. Re-running the seed is safe — duplicate names are ignored due to the `UNIQUE` constraint on the `name` column.

```sql
-- Safe upsert pattern used in seed script
INSERT INTO profiles (...) VALUES (...)
ON CONFLICT (name) DO NOTHING;
```

---

## 🧪 Testing with Postman

### Create Profile
```
POST https://your-deployment.up.railway.app/api/profiles
Body: { "name": "Amina" }
```

### Filter: Male adults from Nigeria
```
GET https://your-deployment.up.railway.app/api/profiles?gender=male&age_group=adult&country_id=NG
```

### Sort by age descending, page 2
```
GET https://your-deployment.up.railway.app/api/profiles?sort_by=age&order=desc&page=2&limit=10
```

### Combined filters
```
GET https://your-deployment.up.railway.app/api/profiles?gender=female&min_age=25&max_age=45&country_id=KE&sort_by=age&order=asc
```

### Natural language search
```
GET https://your-deployment.up.railway.app/api/profiles/search?q=young males from nigeria
GET https://your-deployment.up.railway.app/api/profiles/search?q=senior women from ethiopia
GET https://your-deployment.up.railway.app/api/profiles/search?q=adult males above 30 from kenya
```

### Get by ID
```
GET https://your-deployment.up.railway.app/api/profiles/01926b3e-7c2a-7000-8f3d-1a2b3c4d5e6f
```

### Error cases
```
POST /api/profiles  Body: { "name": "John123" }     → 422
POST /api/profiles  Body: { "name": "" }             → 400
GET  /api/profiles/not-a-uuid                        → 400
GET  /api/profiles/search?q=                         → 400
GET  /api/profiles/search?q=xyzabc123               → 422
```

---

## 🏗 Architecture Overview

```
src/
├── controller/
│   └── ProfileController.java        # All endpoints
├── service/
│   ├── contracts/                    # Interfaces
│   └── serviceImpl/
│       ├── AllProfileServiceImpl.java      # Filtering, sorting, pagination
│       ├── CreateProfileServiceImpl.java   # Enrichment + persistence
│       └── GetProfileByIdServiceImpl.java  # Single profile lookup
├── parser/
│   └── NaturalLanguageQueryParser.java    # Rule-based NLQ engine
├── repository/
│   └── ProfileRepository.java        # JPA queries
├── model/
│   └── Profiles.java                 # Entity
├── dtos/
│   ├── AllProfileResponseDto.java
│   ├── ProfileResponseDto.java
│   ├── ProfileFilterDto.java
│   └── CreateProfileRequestDto.java
├── exception/
│   └── GlobalExceptionHandler.java   # Centralized error handling
└── helper/
    └── UUIDHelper.java               # UUID v7 generation
```

---

## 📌 Key Implementation Decisions

**UUID v7** — Used instead of UUID v4 for time-ordered primary keys, which improves database index performance on large datasets.

**Mono.zip for concurrent API calls** — All three external APIs (Genderize, Agify, Nationalize) are called simultaneously rather than sequentially, reducing create latency from ~3x to ~1x the slowest API response.

**Optional filter pattern** — The repository uses `:param IS NULL OR field = :param` in JPQL so a single query handles all combinations of optional filters without code branching.

**Rule-based NLQ** — The natural language parser uses regex patterns and keyword maps only. No external AI services are called, keeping the feature fast, predictable, and free.

**CORS** — `Access-Control-Allow-Origin: *` is set globally, allowing any frontend or API client to consume this API without preflight issues.

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
