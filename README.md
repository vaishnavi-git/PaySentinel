# Payroll Anomaly Detection & Audit API

> A production-grade Spring Boot REST API that automatically detects payroll anomalies — overtime violations, missing punches, excessive shift durations, and weekly hour breaches — and surfaces them through a secured, role-based audit dashboard.

Built to mirror real-world HR/payroll compliance requirements, the kind handled at scale by enterprise platforms like ADP, Workday, and SAP HCM.

---

## Why This Exists

Manual payroll audits are slow, error-prone, and expensive. A single missed overtime flag or undetected duplicate shift can create legal liability and erode employee trust. This system automates anomaly detection at the shift level, flags issues in real time, and gives payroll managers a structured resolution workflow — all secured behind JWT-based role access.

---

## Features

### Core Engine
- **Real-time anomaly detection** — every shift is analyzed on clock-out for overtime violations, excessive hours, and weekly limit breaches
- **Scheduled missing punch detection** — runs hourly to identify employees who clocked in but never clocked out (after a configurable threshold)
- **Overtime pay calculation** — automatically applies 1.5x rate for hours over 8, with gross pay computed and stored per shift
- **Weekly hour accumulation check** — aggregates all shifts in the current week and flags if total exceeds 40 hours

### Audit Workflow
- **Flag types:** `OVERTIME_VIOLATION`, `MISSING_PUNCH`, `EXCESSIVE_HOURS`, `UNAUTHORIZED_OVERTIME`, `RATE_MISMATCH`, `DUPLICATE_SHIFT`
- **Severity levels:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- **Resolution states:** `OPEN` → `IN_REVIEW` → `RESOLVED` / `DISMISSED`
- **Audit trail** — every resolution records who resolved it and when

### Security
- **JWT authentication** with configurable expiration
- **Role-based access control** — `ADMIN` and `PAYROLL_MANAGER` roles with method-level security (`@PreAuthorize`)
- **Stateless session** — no server-side session storage
- Spring Security filter chain with BCrypt password hashing

### Observability
- **Prometheus metrics** exposed at `/actuator/prometheus`
- **Spring Boot Actuator** health and info endpoints
- Structured for Grafana dashboard integration

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2, Spring MVC, Spring Security |
| API | RESTful API, JSON |
| Auth | OAuth2-ready, JWT (JJWT), BCrypt |
| ORM | Spring Data JPA, Hibernate |
| Database | H2 (dev) — swap to Oracle SQL / PostgreSQL for prod |
| Build | Maven |
| Testing | JUnit 5, Mockito, Spring MockMvc |
| CI/CD | GitHub Actions |
| Containerization | Docker |
| Monitoring | Prometheus, Spring Actuator |

---

## Architecture

```
src/
├── controller/        # REST endpoints (Auth, Employee, Shift, Audit)
├── service/           # Business logic (ShiftService, AuditService)
│   └── AuditEngineService.java   # Core anomaly detection engine
├── repository/        # Spring Data JPA repositories with custom JPQL
├── model/             # JPA entities (Employee, Shift, AuditFlag, User)
├── security/          # JWT filter, UserDetailsService, token utilities
├── config/            # SecurityConfig, DataSeeder
├── dto/               # Request/response transfer objects
└── exception/         # GlobalExceptionHandler, ResourceNotFoundException
```

---

## API Endpoints

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Returns JWT token |
| POST | `/api/auth/register` | Public | Register new user |

### Employees
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/employees` | Authenticated | List all employees |
| GET | `/api/employees/{id}` | Authenticated | Get employee by ID |
| GET | `/api/employees/department/{dept}` | Authenticated | Filter by department |
| POST | `/api/employees` | ADMIN only | Create employee |
| PUT | `/api/employees/{id}` | ADMIN only | Update employee |
| DELETE | `/api/employees/{id}` | ADMIN only | Delete employee |

### Shifts
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/shifts` | Authenticated | All shifts |
| GET | `/api/shifts/flagged` | Authenticated | Flagged shifts only |
| GET | `/api/shifts/employee/{id}` | Authenticated | Shifts by employee |
| POST | `/api/shifts/clock-in/{employeeId}` | Authenticated | Clock in |
| PUT | `/api/shifts/clock-out/{shiftId}` | Authenticated | Clock out + auto-analyze |
| POST | `/api/shifts` | Authenticated | Create shift with custom times |

### Audit
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/audit/summary` | PAYROLL_MANAGER | Aggregate audit metrics |
| GET | `/api/audit/flags` | PAYROLL_MANAGER | All open flags |
| GET | `/api/audit/flags/employee/{id}` | PAYROLL_MANAGER | Flags by employee |
| PUT | `/api/audit/flags/{id}/resolve` | PAYROLL_MANAGER | Mark flag resolved |
| PUT | `/api/audit/flags/{id}/dismiss` | PAYROLL_MANAGER | Dismiss flag |

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional)

### Run Locally

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/payroll-audit-api.git
cd payroll-audit-api

# Build and run
mvn spring-boot:run
```

The app starts at `http://localhost:8080`

**Seeded credentials:**
| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN + PAYROLL_MANAGER |
| `manager` | `manager123` | PAYROLL_MANAGER |

### Run with Docker

```bash
docker build -t payroll-audit-api .
docker run -p 8080:8080 payroll-audit-api
```

### Run Tests

```bash
# Unit tests only
mvn test

# Full suite including integration tests
mvn verify
```

---

## Example Usage

### 1. Login and get JWT

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "roles": [{"authority": "ROLE_ADMIN"}]
}
```

### 2. Clock in an employee

```bash
curl -X POST http://localhost:8080/api/shifts/clock-in/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Clock out (triggers anomaly engine)

```bash
curl -X PUT http://localhost:8080/api/shifts/clock-out/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Get audit summary

```bash
curl http://localhost:8080/api/audit/summary \
  -H "Authorization: Bearer YOUR_TOKEN"
```

```json
{
  "totalShifts": 12,
  "flaggedShifts": 3,
  "openFlags": 2,
  "criticalFlags": 1,
  "flagRate": 25.0,
  "flagsByType": {
    "OVERTIME_VIOLATION": 2,
    "MISSING_PUNCH": 1
  }
}
```

### 5. View open flags and resolve one

```bash
# Get open flags
curl http://localhost:8080/api/audit/flags \
  -H "Authorization: Bearer YOUR_TOKEN"

# Resolve flag #2
curl -X PUT http://localhost:8080/api/audit/flags/2/resolve \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Screenshots

> To add screenshots: run the app locally → use Postman or the H2 console at `http://localhost:8080/h2-console` → capture responses.

**Suggested screenshots to capture:**
1. `POST /api/auth/login` → JWT response in Postman
2. `GET /api/audit/summary` → metrics JSON response
3. `GET /api/audit/flags` → list of flagged anomalies with severity
4. `GET /api/shifts/flagged` → shifts with flag reasons
5. H2 console showing `AUDIT_FLAGS` table populated with real data

---

## Database Schema

```
employees          shifts                audit_flags
----------         ----------            ----------
id (PK)            id (PK)               id (PK)
first_name         employee_id (FK)      shift_id (FK)
last_name          clock_in              employee_id (FK)
email              clock_out             flag_type
department         hours_worked          severity
role               gross_pay             description
hourly_rate        status                resolution_status
hire_date          is_flagged            detected_at
status             flag_reason           resolved_by
                                         resolved_at
```

---

## Production Swap (Oracle SQL)

To connect to Oracle SQL instead of H2, update `application.properties`:

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.hibernate.ddl-auto=validate
```

---

## Resume Bullet Points

Use these directly in your resume under this project:

- Engineered a payroll anomaly detection API using **Java 17 and Spring Boot**, implementing a real-time audit engine that automatically flags overtime violations, missing punches, and weekly hour breaches — with severity classification and a full resolution workflow for payroll managers
- Secured all API endpoints using **JWT-based authentication and role-based access control** (ADMIN, PAYROLL_MANAGER) via Spring Security filter chain with BCrypt password hashing and stateless session management
- Designed a **custom JPQL query layer** using Spring Data JPA to aggregate weekly shift data per employee and detect threshold breaches, enabling per-employee audit trails with full flag history and resolution audit trail
- Implemented **scheduled anomaly detection** using `@Scheduled` to run hourly missing-punch scans across all active shifts, automatically flagging and updating shift status without manual intervention
- Built a **CI/CD pipeline using GitHub Actions** that runs JUnit unit tests and Spring MockMvc integration tests on every push to main, with Docker containerization for consistent deployment across environments
- Exposed **Prometheus-compatible metrics** via Spring Boot Actuator for real-time observability, structured for Grafana dashboard integration

---


