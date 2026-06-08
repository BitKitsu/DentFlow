# DentFlow Backend

Backend system for the DentFlow dental clinic management application.
Microservices architecture with two Spring Boot services and PostgreSQL database.

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
|       |-- file/              # Files (S3/Supabase Storage)
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
| Files | AWS S3 (Supabase Storage) |
| PDF | iText 8 (AGPL) |
| Testing | JUnit 5, Mockito, AssertJ, H2 |

## Quick Start

### Requirements
- Java 21
- Maven 3.9+
- Docker + Docker Compose

### Running

```bash
# 1. Start database
docker-compose up -d postgres

# 2. Install modules
mvn install -DskipTests

# 3. Start services (separate terminals)
mvn spring-boot:run -pl identity-service -Dspring-boot.run.profiles=dev
mvn spring-boot:run -pl core-service/core-app -Dspring-boot.run.profiles=dev
```

### Configuration

```bash
cp .env.example .env
# Fill in environment variables in .env
```

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

```bash
# Run all tests
mvn test

# Run all tests except integration tests
mvn test -DskipTests=false -Dtest='!*IntegrationTest'

# Run integration tests only
mvn test -Dtest='*IntegrationTest'
```

## Test Structure

```
src/test/java/
|-- auth/
|   |-- api/
|   |   |-- AuthControllerTest.java              # Controller tests (WebMvcTest)
|   |   +-- RegisterRequestValidationTest.java   # DTO validation
|   +-- application/
|       |-- AuthServiceTest.java                 # Unit tests
|       +-- AuthFlowIntegrationTest.java         # Integration test (full flow)
|-- reservation/
|   +-- application/
|       |-- AppointmentServiceTest.java          # Unit tests
|       |-- ReservationFlowIntegrationTest.java  # Integration test (full flow)
|       +-- AppointmentNotificationIntegrationTest.java
|   +-- infrastructure/
|       +-- AppointmentRepositoryTest.java       # Repository test (DataJpaTest)
|-- patient/
|   +-- application/
|       +-- PatientServiceTest.java
|-- scheduling/
|   +-- application/
|       +-- SchedulingServiceTest.java
|-- clinic/
|   +-- application/
|       +-- StaffMemberServiceTest.java
+-- notification/
    +-- application/
        +-- NotificationServiceTest.java
```

## Docker

```bash
# Start all services
docker-compose up -d

# Database only
docker-compose up -d postgres

# Logs
docker-compose logs -f identity-service
docker-compose logs -f core-service
```

## Coding Conventions

- DDD-style packages: `api/`, `application/`, `domain/`, `infrastructure/`
- DTOs as records (Java 16+)
- Validation via Jakarta Validation
- Errors as ResponseStatusException
- Logging via SLF4J
- Javadoc for key classes and methods
