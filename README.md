# Profile Intelligence Service — Queryable Intelligence Engine

A production-grade demographic intelligence API built for **Insighta Labs**. This system upgrades a basic profile storage service into a fully queryable intelligence engine — supporting advanced filtering, multi-condition queries, sorting, pagination, and a rule-based natural language query system.

---

## Live API

**Base URL:** `profile-intelligence-service-production-7cd5.up.railway.app`

---

## System Overview

```
Client Request
      ↓
Spring Boot Controller
      ↓
┌─────────────────────────────────────┐
│         /api/profiles               │
│  filters + sort + pagination        │
│  → JPQL query with optional params  │
│  → Page<Profiles> from PostgreSQL   │
└─────────────────────────────────────┘
      ↓
┌─────────────────────────────────────┐
│       /api/profiles/search          │
│  plain English query string         │
│  → Rule-based parser (no AI/LLMs)   │
│  → ProfileFilterDto                 │
│  → same filter pipeline above       │
└─────────────────────────────────────┘
      ↓
{ status, page, limit, total, data }
```

---

## Tech Stack

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

## Database Schema

| Field | Type | Notes |
|---|---|---|
| `id` | UUID v7 | Primary key, time-ordered |
| `name` | VARCHAR UNIQUE | Person's full name |
| `gender` | VARCHAR | `male` or `female` |
| `gender_probability` | FLOAT | Confidence score |
| `age` | INT | Exact age |
| `age_group` | VARCHAR | `child`, `teenager`, `adult`, `senior` |
| `country_id` | VARCHAR(2) | ISO 3166-1 alpha-2 code e.g. `NG`, `KE` |
| `country_name` | VARCHAR | Full country name e.g. `Nigeria` |
| `country_probability` | FLOAT | Confidence score |
| `created_at` | TIMESTAMP | UTC ISO 8601 |

---

## Data Seeding

The database is pre-seeded with **2026 profiles**. Re-running the seed is safe — `ON CONFLICT (name) DO NOTHING` prevents duplicates.

---

## Age Group Classification

| Age Range | Group |
|---|---|
| 0 – 12 | `child` |
| 13 – 19 | `teenager` |
| 20 – 59 | `adult` |
| 60+ | `senior` |

---

## API Reference

### 1. Get All Profiles

```
GET /api/profiles
```

All parameters are optional and fully combinable.

**Filter Parameters**

| Parameter | Type | Description |
|---|---|---|
| `gender` | string | `male` or `female` |
| `age_group` | string | `child`, `teenager`, `adult`, `senior` |
| `country_id` | string | ISO 2-letter code |
| `min_age` | integer | Minimum age inclusive |
| `max_age` | integer | Maximum age inclusive |
| `min_gender_probability` | float | Minimum gender confidence score |
| `min_country_probability` | float | Minimum country confidence score |

**Sort Parameters**

| Parameter | Accepted Values | Default |
|---|---|---|
| `sort_by` | `age`, `created_at`, `gender_probability` | `created_at` |
| `order` | `asc`, `desc` | `desc` |

**Pagination Parameters**

| Parameter | Default | Max |
|---|---|---|
| `page` | `1` | — |
| `limit` | `10` | `50` |

**Example Requests**

```
GET /api/profiles?gender=male&country_id=NG&min_age=25
GET /api/profiles?age_group=senior&sort_by=age&order=asc
GET /api/profiles?gender=female&min_gender_probability=0.8&sort_by=gender_probability&order=desc&page=2&limit=20
GET /api/profiles?min_age=18&max_age=35&country_id=KE&sort_by=age&order=asc&page=1&limit=10
```

**Success Response `200`**

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

### 2. Natural Language Search

```
GET /api/profiles/search?q={query}
```

Interprets plain English queries and converts them into structured filters using a **rule-based parser only — no AI, no LLMs**.

**How the Parser Works**

```
"adult females from kenya above 25"
      ↓
"females"  → gender = female
"adult"    → ageGroup = adult
"above 25" → minAge = 25
"kenya"    → countryId = KE
      ↓
ProfileFilterDto → same pipeline as /api/profiles
```

**Supported Patterns**

| Query Example | Resolves To |
|---|---|
| `young males` | `gender=male` + `minAge=16` + `maxAge=24` |
| `females above 30` | `gender=female` + `minAge=30` |
| `people from angola` | `countryId=AO` |
| `adult males from kenya` | `gender=male` + `ageGroup=adult` + `countryId=KE` |
| `male and female teenagers above 17` | `ageGroup=teenager` + `minAge=17` |
| `senior women from ethiopia` | `gender=female` + `ageGroup=senior` + `countryId=ET` |
| `children below 10` | `ageGroup=child` + `maxAge=10` |
| `men between 25 and 40` | `gender=male` + `minAge=25` + `maxAge=40` |

**Keyword Reference**

| Keyword or Pattern | Maps To |
|---|---|
| `male`, `man`, `men`, `boys` | `gender = male` |
| `female`, `woman`, `women`, `girls` | `gender = female` |
| `child`, `children` | `ageGroup = child` |
| `teen`, `teenager`, `teenagers` | `ageGroup = teenager` |
| `adult`, `adults` | `ageGroup = adult` |
| `senior`, `elderly`, `old` | `ageGroup = senior` |
| `young` | `minAge = 16`, `maxAge = 24` — not a stored age group |
| `above X`, `over X`, `older than X` | `minAge = X` |
| `below X`, `under X`, `younger than X` | `maxAge = X` |
| `between X and Y` | `minAge = X`, `maxAge = Y` |
| `from {country}`, `{country name}` | `countryId = ISO code` |

Pagination (`page`, `limit`) applies here too.

**Success Response `200`**

```json
{
  "status": "success",
  "page": 1,
  "limit": 10,
  "total": 34,
  "data": [ ... ]
}
```

**Uninterpretable Query `422`**

```json
{ "status": "error", "message": "Unable to interpret query" }
```

**Missing or Empty `q` `400`**

```json
{ "status": "error", "message": "Invalid query parameters" }
```

---

## Error Handling

All errors follow this structure:

```json
{ "status": "error", "message": "<detailed error message>" }
```

| Status Code | Trigger |
|---|---|
| `400` | Missing or empty parameter, malformed UUID, empty `q` |
| `404` | Profile ID does not exist |
| `422` | Invalid parameter type, query cannot be interpreted |
| `500/502` | Database failure, upstream API timeout |

---

## External APIs

| API | Endpoint | Data Extracted |
|---|---|---|
| Genderize | `https://api.genderize.io?name={name}` | `gender`, `probability`, `count` → `sample_size` |
| Agify | `https://api.agify.io?name={name}` | `age` |
| Nationalize | `https://api.nationalize.io?name={name}` | Highest probability country → `country_id`, `country_probability` |

All three are called **concurrently** via `Mono.zip`, so total latency equals the slowest single call.

---

## Installation & Setup

**1. Clone the repository**

```bash
git clone https://github.com/your-username/profile-intelligence-api.git
cd profile-intelligence-api
```

**2. Set environment variables**

```env
SPRING_DATA_URL=jdbc:postgresql://<host>:<port>/<database>
SPRING_DATA_USERNAME=your_db_username
SPRING_DATA_PASSWORD=your_db_password
PORT=8080
```

**3. Run**

```bash
./mvnw spring-boot:run
```

---

## Testing with Postman

```
# Fetch all
GET /api/profiles

# Filter
GET /api/profiles?gender=male&age_group=adult&country_id=NG

# Sort + paginate
GET /api/profiles?sort_by=age&order=desc&page=2&limit=10

# Combined
GET /api/profiles?gender=female&min_age=25&max_age=45&country_id=KE&sort_by=age&order=asc

# NLQ
GET /api/profiles/search?q=young males from nigeria
GET /api/profiles/search?q=senior women from ethiopia
GET /api/profiles/search?q=adult males above 30 from kenya
GET /api/profiles/search?q=females between 20 and 35&page=1&limit=5

# Errors
GET  /api/profiles?sort_by=invalid_field           → 422
GET  /api/profiles/search?q=                       → 400
GET  /api/profiles/search?q=xyzabc123              → 422
GET  /api/profiles/not-a-uuid                      → 400
POST /api/profiles  Body: { "name": "" }           → 400
POST /api/profiles  Body: { "name": "John123" }    → 422
```

---

## Project Structure

```
src/
├── controller/
│   └── ProfileController.java
├── service/
│   ├── contracts/
│   └── serviceImpl/
│       ├── AllProfileServiceImpl.java
│       ├── CreateProfileServiceImpl.java
│       ├── GetProfileByIdServiceImpl.java
│       └── DeleteProfileServiceImpl.java
├── parser/
│   └── NaturalLanguageQueryParser.java
├── repository/
│   └── ProfileRepository.java
├── model/
│   └── Profiles.java
├── dtos/
│   ├── AllProfileResponseDto.java
│   ├── ProfileResponseDto.java
│   ├── ProfileFilterDto.java
│   └── CreateProfileRequestDto.java
├── exception/
│   └── GlobalExceptionHandler.java
└── helper/
    └── UUIDHelper.java
```

---

## Key Implementation Notes

**Optional filter pattern** — The repository uses `:param IS NULL OR field = :param` in JPQL so one query handles every combination of optional filters without branching.

**Pagination** — `PageRequest.of(page - 1, limit, sort)` converts the client's 1-indexed page to Spring's 0-indexed page. Total count comes from `result.getTotalElements()`.

**Sorting** — Client-facing names like `gender_probability` and `created_at` are mapped to their Java field equivalents `genderProbability` and `createdAt` before being passed to `Sort.by()`.

**Rule-based NLQ** — The parser uses regex patterns and a country name-to-ISO-code map. No external services are called. Output is a `ProfileFilterDto` that feeds directly into the same pipeline as `/api/profiles`.

**`young` keyword** — Maps to `minAge=16, maxAge=24` for parsing only. It is not a stored age group.

**Seed safety** — `ON CONFLICT (name) DO NOTHING` means the seed can be re-run at any time without creating duplicates.

**CORS** — `Access-Control-Allow-Origin: *` is set globally so the grading script and any client can reach the API without preflight issues.

**UUID v7** — Time-ordered primary keys improve index performance on large datasets compared to random UUID v4.
