# Grimni Internal Control Platform

Full-stack project for IDATT2105. The system is built as a multi-tenant internal control platform for restaurants and serving establishments, with support for both IK-Mat and IK-Alkohol workflows.

The application includes:

- a Vue 3 frontend
- a Spring Boot backend
- a MySQL database
- SeaweedFS for file storage
- Docker-based local startup

## Main functionality

The current project includes both mobile and desktop workflows backed by the real backend and database.

Implemented areas include:

- shared authentication and registration flow
- JWT-based authentication and role-based access control
- organization-aware sessions and organization switching
- mobile routines, CCP logging, mapping view, and deviation registration
- desktop team management and role changes
- desktop learning and course administration
- desktop prerequisites management
- desktop CCP management
- organization creation and membership handling

## Tech stack

- Frontend: Vue 3, TypeScript, Vue Router, Vite
- Backend: Java 21, Spring Boot 3, Spring Security, JPA
- Database: MySQL 8
- File storage: SeaweedFS
- API docs: Swagger / springdoc-openapi
- CI: GitHub Actions

## Repository structure

```text
.
├── backend/                      Spring Boot backend
├── frontend/                     Vue frontend
├── seaweedfs/                    SeaweedFS startup config
├── compose.yaml                  Docker Compose setup
├── schema.sql                    Database schema
├── seed.sql                      Base demo seed loaded automatically in Docker
├── seed_mobile_frontend_test_data-1-1.sql
└── docs/                         Project documentation
```

## Requirements

### Recommended local setup

- Docker Desktop
- Node.js 22 or newer
- npm
- Java 21
- Maven wrapper is included in `backend/`

### Environment variables

Copy `.env.template` to `.env` before running the project:

```bash
cp .env.template .env
```

The template already contains development-safe placeholder values, but you should still review and replace them as needed:

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `S3_ACCESS_KEY`
- `S3_SECRET_KEY`
- `JWT_SECRET_KEY`

## Running the project with Docker

This is the easiest and recommended way to run the full system locally.

### Start everything

From the repository root:

```bash
docker compose up -d --build
```

This starts:

- `db` on MySQL 8.4
- `seaweedfs`
- `backend_app`
- `db_seed`
- `frontend_app`

### What Docker does automatically

When the database starts for the first time, Docker mounts and applies:

- [schema.sql](/Users/benjaminkvello-hansen/Projects/IDATT2105_Fullstack_Project/schema.sql)
- [seed.sql](/Users/benjaminkvello-hansen/Projects/IDATT2105_Fullstack_Project/seed.sql)

After backend startup, the `db_seed` service waits for the schema/backend to be ready and then applies the demo seed again to ensure the expected demo data exists.

### Main local URLs

- Frontend app: [http://localhost:8080](http://localhost:8080)
- Health check: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Stop everything

```bash
docker compose down
```

### Stop and remove volumes

Use this if you want a completely fresh database:

```bash
docker compose down -v
```

## Resetting the database

If the database gets into a bad state, use this reset flow:

```bash
docker compose down -v
docker compose up -d --build
```

Because `schema.sql` and `seed.sql` are mounted into MySQL init, a fresh database volume will recreate the schema and base seed automatically.

## Demo and test users

The base Docker seed in [seed.sql](/Users/benjaminkvello-hansen/Projects/IDATT2105_Fullstack_Project/seed.sql) already creates several demo accounts for teachers, evaluators, and local testing.

All seeded demo users share the same password:

```text
teacherdemo123
```

Demo logins:

- `teacher.owner@demo.local`
  - `OWNER` in Fjord Bistro and Bar
  - can also switch organization to North Taproom and Harbor Bakery
- `anna.manager@demo.local`
  - `MANAGER` in Fjord Bistro and Bar
- `mia.worker@demo.local`
  - `WORKER` in Fjord Bistro and Bar
  - useful for testing mobile routines and CCP logging
- `ole.tapowner@demo.local`
  - `OWNER` in North Taproom
- `lars.bartender@demo.local`
  - `WORKER` in North Taproom
- `ida.bakeryowner@demo.local`
  - `OWNER` in Harbor Bakery
- `eva.baker@demo.local`
  - `WORKER` in Harbor Bakery

Useful test scenarios:

- use `teacher.owner@demo.local` to test desktop administration and organization switching
- use `anna.manager@demo.local` to test manager-level desktop access
- use `mia.worker@demo.local` or `eva.baker@demo.local` to test worker/mobile flows
- register a brand new user to test the no-organization flow

## Running services individually

### Start only one service

Example:

```bash
docker compose up -d seaweedfs
```

### Restart only the database

```bash
docker compose up -d --force-recreate db
```

## Frontend development

Run frontend locally from `frontend/`:

```bash
cd frontend
npm install
npm run dev
```

Useful frontend commands:

```bash
npm run build
npm run test:unit -- --run
npm run lint
```

Frontend notes:

- The frontend uses `sessionStorage` for short-lived auth token handling.
- The main production app in Docker is served through nginx on port `8080`.

## Backend development

Run backend locally from `backend/`:

```bash
cd backend
./mvnw spring-boot:run
```

Useful backend commands:

```bash
./mvnw test
./mvnw verify
./mvnw -DskipTests package
```

Backend notes:

- Java 21 is the intended local runtime.
- JaCoCo is configured in Maven with a 50% line coverage requirement on verify.
- Swagger annotations are used on the REST controllers.

## Testing

### Frontend

```bash
cd frontend
npm run test:unit -- --run
```

Optional e2e commands:

```bash
npm run test:e2e
npm run test:e2e:dev
```

### Backend

```bash
cd backend
./mvnw test
```

For coverage and verification:

```bash
./mvnw verify
```

## Authentication and roles

The current auth flow supports:

- login
- registration
- logout
- refresh token rotation
- organization switching

Role-based behavior currently distinguishes between:

- `OWNER`
- `MANAGER`
- `WORKER`

Typical landing behavior:

- owners and managers are routed to desktop views
- workers are routed to mobile views
- users without an organization are routed to the no-organization flow

## Common troubleshooting

### The app starts, but I cannot log in

Check:

- `.env` exists and has values
- the database is healthy
- backend and frontend containers are running

Useful commands:

```bash
docker compose ps
docker compose logs backend_app
docker compose logs frontend_app
docker compose logs db
```

### The schema looks wrong or old data keeps showing up

Use a full reset:

```bash
docker compose down -v
docker compose up -d --build
```

### I changed code but Docker still shows old behavior

Rebuild the stack:

```bash
docker compose up -d --build
```

### I want to verify the backend is alive

Open:

- [http://localhost:8080/api/health](http://localhost:8080/api/health)

## Delivery-oriented notes

This repository includes:

- runnable source code for frontend and backend
- Docker configuration for easy startup
- schema and seed files
- API documentation support through Swagger
- test setup for both frontend and backend

For project delivery documentation, see:

- [delivery-priorities.md](/Users/benjaminkvello-hansen/Projects/IDATT2105_Fullstack_Project/docs/delivery-priorities.md)
