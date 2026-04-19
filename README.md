# Profile Intelligence Service

A high-performance Spring Boot backend service that enriches user profiles using external intelligence APIs. This project demonstrates concurrent processing with Project Reactor (WebFlux), persistent storage with time-ordered UUIDs (v7), and a standardized RESTful API structure.

## Live Deployment

The service is live at: `https://api-data-persistence-production.up.railway.app`

## Features

- **Concurrent Data Fetching**: Uses `Mono.zip` to simultaneously query Genderize, Agify, and Nationalize APIs, minimizing latency.
- **UUID v7**: Implements time-sequential UUIDs for optimized database indexing.
- **Robust Error Handling**: Standardized JSON error responses for 400, 404, 422, and 502 status codes.
- **Optional Filtering**: Supports case-insensitive filtering by gender, country, and age group.

---

## API Documentation

### 1. Enrich & Create Profile
**POST** `/api/profiles`

**Request Body:**
```json
{
  "name": "Full Name"
}
```

**Validation:**  
Name must only contain letters and spaces (`422` error otherwise).

---

### 2. Get All Profiles (Filtered)
**GET** `/api/profiles`

**Optional Query Parameters:**
- `gender`
- `country_id`
- `age_group`

**Example:**
```text
https://api-data-persistence-production.up.railway.app/api/profiles?gender=female&country_id=NG
```

---

### 3. Get Single Profile
**GET** `/api/profiles/{id}`

---

### 4. Delete Profile
**DELETE** `/api/profiles/{id}`

---

## Error Handling

All errors follow the structure:

```json
{
  "status": "error",
  "message": "<message>"
}
```

| Status Code | Description |
|------------|-------------|
| 400 | Bad Request (missing name, malformed JSON, or invalid UUID format) |
| 404 | Not Found (profile ID does not exist) |
| 422 | Unprocessable Entity (validation failed: name contains numbers or symbols) |
| 502 | Bad Gateway (upstream API failure or timeout) |

---

## Tech Stack

- Java 21
- Spring Boot 3.4
- Spring Data JPA (PostgreSQL / H2)
- Spring WebFlux (WebClient)
- Lombok
- Jackson

---

## Installation & Setup

### Clone the Repository
```bash
git clone https://github.com/your-username/api-data-persistence.git
cd api-data-persistence
```

### Configure Database
Update `src/main/resources/application.properties` with your datasource credentials.

### Run the Application
```bash
./mvnw spring-boot:run
```

---

Developed by **Chinaza Esther Nwachukwu**
