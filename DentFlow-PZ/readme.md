# DentFlow Backend

Backend system for the DentFlow dental clinic management application.
Microservices architecture with two Spring Boot services, PostgreSQL database, and MinIO (S3-compatible) object storage.

## Structure

```
DentFlow-PZ/
|-- identity-service/          # Identity service (port 8081)
|   +-- src/main/java/pl/edu/ur/dentflow/identity/
|       |-- auth/              # Registration, login, JWT
|       |   |-- api/           # REST controllers + DTOs
|       |   +-- application/   # Business logic (AuthService)
|       |-- security/          # JWT, filtering, rate limiting
|       +-- user/              # User entities
|           |-- domain/        # User, UserRole, Role
|           +-- infrastructure/# UserRepository
|-- core-service/              # Main service (port 8080)
|   +-- src/main/java/pl/edu/ur/dentflow/core/
|       |-- clinic/            # Clinics, locations, staff
|       |-- patient/           # Patients
|       |-- scheduling/        # Work schedules, blockers
|       |-- reservation/       # Appointments (reservations)
|       |-- catalog/           # Service catalog
|       |-- notification/      # In-app notifications + email
|       |-- file/              # Files (S3)
|       +-- core-app/          # Main app, security, configuration
|-- pdf-generator/             # PDF report generation library
|-- docker-compose.yml         # PostgreSQL + both services
+-- .env.example               # Environment variables
```

## Technologies

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Database | PostgreSQL 15 (Flyway migrations) |
| Authorization | JWT (jjwt 0.12.5) |
| Build | Maven 3.9+ |
| Containerization | Docker + Docker Compose |
| API Documentation | Springdoc OpenAPI (Swagger UI) |
| Email | SendGrid SMTP |
| Files | S3 (MinIO locally, AWS S3 / Railway in production) |
| PDF | iText 8 (AGPL) |
| Testing | JUnit 5, Mockito, AssertJ, H2 |

## Quick Start

### Requirements
- Java 21
- Maven 3.9+
- Docker + Docker Compose

### Running (Docker Compose -- all services)

```bash
# 1. Configure environment
cp .env.example .env
# Defaults work for local dev -- edit .env for production

# 2. Start everything (PostgreSQL + MinIO + both services)
docker-compose up -d --build

# 3. Check logs
docker-compose logs -f
```

MinIO console is available at http://localhost:9001 (login: `minioadmin` / `minioadmin`).

### Running (local development, without Docker)

```bash
# 1. Start database
docker-compose up -d postgres

# 2. Configure environment
cp .env.example .env
# Edit .env -- SPRING_DATASOURCE_URL must point to localhost

# 3. Install parent POM + modules
mvn install -DskipTests

# 4. Start services (separate terminals)
mvn spring-boot:run -pl identity-service -Dspring-boot.run.profiles=dev
mvn spring-boot:run -pl core-service/core-app -Dspring-boot.run.profiles=dev
```

### Configuration

```bash
cp .env.example .env
# Defaults work out of the box for local dev (dentflow123)
# For production: change POSTGRES_PASSWORD, JWT_SECRET, SENDGRID_API_KEY
```

## Android App Connection

The Android app (`DentFlowAndroid/`) reads server URLs from `local.properties`:

```properties
# Android emulator (maps 10.0.2.2 -> host localhost)
API_AUTH_URL="http://10.0.2.2:8081/"
API_CORE_URL="http://10.0.2.2:8080/"

# Physical device on same network (replace with your machine's IP)
# Find it with: hostname -I
API_AUTH_URL="http://192.168.x.x:8081/"
API_CORE_URL="http://192.168.x.x:8080/"

# Production (Railway)
API_AUTH_URL="https://identity-service-production-6149.up.railway.app/"
API_CORE_URL="https://core-service-production-9ce3.up.railway.app/"
```

Set these in `DentFlowAndroid/local.properties` (not committed to git).

## API

### Identity Service (port 8081)
- Swagger UI: http://localhost:8081/swagger-ui.html
- Registration: `POST /auth/register`
- Login: `POST /auth/login`
- Logout: `POST /auth/logout`
- Change password: `PUT /auth/change-password`
- Profile: `PUT /auth/profile`
- Assign role: `POST /auth/assign-role`

### Core Service (port 8080)
- Swagger UI: http://localhost:8080/swagger-ui.html
- Clinics: `GET/POST/PUT/DELETE /tenants`
- Patients: `GET/POST/PUT/DELETE /tenants/{id}/patients`
- Appointments: `GET/POST/PUT /tenants/{id}/appointments`, `GET /tenants/{id}/appointments/my`
- Schedules: `GET/POST/PUT/DELETE /tenants/{id}/scheduling/slots`
- Catalog: `GET/POST/PUT/DELETE /tenants/{id}/catalog`
- Notifications: `GET/POST /tenants/{id}/users/{userId}/notifications`
- Files: `GET/POST/DELETE /tenants/{id}/files`
- PDF Reports: `GET /tenants/{id}/reports/*`

## Testing

### Test Types

| Type | Framework | Description |
|------|-----------|-------------|
| **Unit** | JUnit 5 + Mockito | Service logic with mocked repositories (no Spring context) |
| **WebMvc** | Spring WebMvcTest | Controller layer: HTTP status, request validation, JSON mapping |
| **Validation** | Jakarta Validation | DTO field constraints (email, size, regex) |
| **Repository** | Spring DataJpaTest | JPA query correctness with H2 in-memory database |
| **Integration** | SpringBootTest + H2 | Full application flow across multiple services |
| **Context Load** | SpringBootTest | Verifies Spring context boots without errors |

### Running Tests

```bash
# Run all tests
./test.sh

# Run all tests
mvn test

# Run all tests except integration tests
mvn test -DskipTests=false -Dtest='!*IntegrationTest'

# Run integration tests only
mvn test -Dtest='*IntegrationTest'
```

## Test Structure

14 test classes across 8 Maven modules. Run with `./test.sh`.

```
identity-service/
+-- src/test/java/.../identity/
    |-- IdentityServiceApplicationTests.java           # Context load (placeholder)
    |-- auth/
    |   |-- api/
    |   |   |-- AuthControllerTest.java                # WebMvcTest: HTTP status, validation
    |   |   +-- RegisterRequestValidationTest.java     # DTO validation (Jakarta)
    |   +-- application/
    |       |-- AuthServiceTest.java                   # Unit tests (Mockito)
    |       +-- AuthFlowIntegrationTest.java           # Integration: register -> login -> change password
core-service/
|-- core-app/
|   +-- src/test/java/.../core/
|       +-- CoreServiceApplicationTests.java           # Context load (H2, Flyway off)
|-- clinic/
|   +-- src/test/java/.../clinic/application/
|       +-- StaffMemberServiceTest.java               # Unit tests (Mockito)
|-- patient/
|   +-- src/test/java/.../patient/application/
|       +-- PatientServiceTest.java                   # Unit tests (Mockito)
|-- scheduling/
|   +-- src/test/java/.../scheduling/application/
|       +-- SchedulingServiceTest.java                # Unit tests (Mockito)
|-- reservation/
|   +-- src/test/java/.../reservation/
|       |-- application/
|       |   |-- AppointmentServiceTest.java           # Unit tests (Mockito)
|       |   |-- ReservationFlowIntegrationTest.java   # Integration: create -> update -> cancel
|       |   +-- AppointmentNotificationIntegrationTest.java  # (@Disabled) event -> notification
|       +-- infrastructure/
|           +-- AppointmentRepositoryTest.java        # DataJpaTest: JPA queries, sorting
+-- notification/
    +-- src/test/java/.../notification/application/
        +-- NotificationServiceTest.java              # Unit tests (Mockito)

pdf-generator/
+-- src/test/java/.../pdf/
    +-- DentFlowPdfGeneratorTest.java                 # PDF generation
```

## Docker

```bash
# Build and start all services
docker-compose up -d --build

# Rebuild a single service
docker-compose up -d --build identity-service
docker-compose up -d --build core-service

# Database + MinIO only
docker-compose up -d postgres minio

# Logs
docker-compose logs -f identity-service
docker-compose logs -f core-service

# Stop everything
docker-compose down

# Stop and remove volumes (fresh start)
docker-compose down -v
```

## Coding Conventions

- DDD-style packages: `api/`, `application/`, `domain/`, `infrastructure/`
- DTOs as records (Java 16+)
- Validation via Jakarta Validation
- Errors as ResponseStatusException
- Logging via SLF4J
- Javadoc for key classes and methods
