# TourPlaner

Web-Anwendung zur Planung von Touren (Bike, Hike, Run, Vacation) und Verwaltung von **Tour-Logs** (Angular-Frontend, Spring-Boot-Backend).

## Git-Repository

**URL:** `https://github.com/Abdullah10-swen/tour-planner-team09`  

## Projektstruktur

| Ordner      | Inhalt |
|------------|--------|
| `backend/` | Spring Boot (REST-API, JPA + PostgreSQL, OpenRouteService-Integration) |
| `frontend/`| Angular 21 (Dashboard, Tour- und Log-Formulare, Karte, Anbindung an API) |
| `docs/`    | Protokoll / UX & Wireframes (Zwischenabgabe) |

## Voraussetzungen

- **Backend:** JDK 25 (siehe `pom.xml`), Maven oder Wrapper `mvnw` / `mvnw.cmd`
- **Frontend:** Node.js + npm (siehe `frontend/package.json` für Versionen)
- **Docker:** für die PostgreSQL-Datenbank

## Setup: `.env`-Datei anlegen

Vor dem ersten Start muss im Projektroot eine `.env`-Datei erstellt werden (wird nicht ins Git eingecheckt):

```bash
# .env (im Projektroot)
POSTGRES_DB=tourplanner
POSTGRES_USER=tourplanner
POSTGRES_PASSWORD=dein_passwort
# POSTGRES_PORT=5432   # optional, Standard: 5432
```

Die Datei `.env` enthält bereits ein Beispiel mit allen Variablen.

## Backend-Secrets: `application-local.properties`

Das Backend erwartet das Datenbankpasswort und den OpenRouteService-API-Key in einer lokalen Properties-Datei (wird nicht ins Git eingecheckt):

```properties
# backend/src/main/resources/application-local.properties
spring.datasource.password=dein_passwort
openrouteservice.api.key=dein_ors_api_key
```

Einen kostenlosen API-Key gibt es unter [openrouteservice.org](https://openrouteservice.org/dev/#/login).

## Docker und Backend starten

```bash
docker compose up -d
cd backend
./mvnw.cmd spring-boot:run   # Windows
# ./mvnw spring-boot:run     # Linux/macOS
```

API-Standard: `http://localhost:8080`  
Tours: `GET/POST/PUT/DELETE` unter `http://localhost:8080/api/tours` (siehe Controller).

## Frontend starten

```bash
cd frontend
ng serve
```

App: `http://localhost:4200`  
Die API-Basis-URL ist in `frontend/src/app/core/api/api-base.ts` konfiguriert (`http://localhost:8080/api`).

## Features (aktueller Stand)

- Angular-UI mit Tour-Liste, Details, Kartenansicht (OpenRouteService), CRUD für Touren und Logs
- Spring-Boot-REST, DTOs, Schichten (Controller → Service → Repository → PostgreSQL via JPA)
- Routenberechnung und Geocoding über OpenRouteService
- JWT-basierte Authentifizierung
- Tour-Export / -Import (JSON)
- Berechnung von Popularität und Kinderfreundlichkeit aus Tour-Logs
- Datei-Upload für Tour-Bilder
- Strukturiertes Logging (Logback, rollierende Log-Dateien unter `backend/logs/`)

## Dokumentation

- **Protokoll:** siehe `docs/Protokoll_Tourplanner.pdf`

## Kurs / Kontext

**Lehrveranstaltung:** Software Engineering 2  
**Semesterprojekt:** Tour Planner  
**Hochschule:** FH Technikum Wien
