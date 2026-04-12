# TourPlaner

Semesterprojekt **Tour Planner** in der Lehrveranstaltung **Software Engineering 2** (FH Technikum Wien): Web-Anwendung zur Planung von Touren (Bike, Hike, Run, Vacation) und Verwaltung von **Tour-Logs** (Angular-Frontend, Spring-Boot-Backend).

## Git-Repository

**URL:** `https://github.com/Abdullah10-swen/tour-planner-team09`  

## Projektstruktur

| Ordner      | Inhalt |
|------------|--------|
| `backend/` | Spring Boot (REST-API, In-Memory-Services für Zwischenabgabe) |
| `frontend/`| Angular 21 (Dashboard, Tour- und Log-Formulare, Anbindung an API) |

## Voraussetzungen

- **Backend:** JDK (Projekt: Java 25, siehe `pom.xml`), Maven oder Wrapper `mvnw` / `mvnw.cmd`
- **Frontend:** Node.js + npm (siehe `frontend/package.json` für Versionen)

## Backend starten

```bash
cd backend
./mvnw.cmd spring-boot:run   # Windows
# ./mvnw spring-boot:run     # Linux/macOS
```

API-Standard: `http://localhost:8080`  
Tours: `GET/POST/PUT/DELETE` unter `http://localhost:8080/api/tours` (siehe Controller).

## Frontend starten

```bash
cd frontend
npm install
npm start
```

App: `http://localhost:4200`  
Die API-Basis-URL ist in `frontend/src/app/core/api/api-base.ts` konfiguriert (`http://localhost:8080/api`).

## Zwischenabgabe (Stand)

- Angular-UI mit Tour-Liste, Details, Karten-Platzhalter, CRUD für Touren und Logs.
- Spring-Boot-REST, DTOs, Schichten (Controller → Service → Domain), In-Memory-Persistenz (keine PostgreSQL in dieser Phase).

## Dokumentation

- **Protokoll / UX & Wireframes (Zwischenabgabe):** siehe `docs/PROTOKOLL-Zwischenabgabe.md`.

## Kurs / Kontext

**Lehrveranstaltung:** Software Engineering 2  
**Semesterprojekt:** Tour Planner  
**Hochschule:** FH Technikum Wien
