# PrepTrack — Interview Preparation OS

A Spring Boot REST API that helps you track your interview preparation, calculate readiness scores, and manage job applications.

## Why this project exists

Most developers building for a Java backend role build generic E-Commerce or Todo apps. PrepTrack is different — it was built *while* preparing for Java backend interviews, using it to track the very preparation that led to building it.

## Features

| Feature | Java Concept Used |
|---|---|
| JWT Authentication | Spring Security 6, BCrypt |
| Topic & Study Logging | JPA relationships, @Transactional |
| Readiness Score Engine | Stream, Collectors, Optional, Predicate |
| Spaced Repetition Scheduler | java.time, ChronoUnit |
| Application State Machine | Map-based valid transition engine |
| Async Weekly Report | CompletableFuture, @Async |
| Daily Revision Digest | @Scheduled |
| Global Exception Handling | @ControllerAdvice |
| Swagger UI | SpringDoc OpenAPI |

## Tech Stack

- **Java 17** — Records, sealed types, text blocks
- **Spring Boot 3.2.5** — Auto-configuration, embedded Tomcat
- **Spring Security 6** — SecurityFilterChain, JWT stateless auth
- **Spring Data JPA** — Hibernate, PostgreSQL
- **jjwt 0.12.5** — JWT creation and validation
- **Lombok** — Compile-time boilerplate generation
- **JUnit 5 + Mockito** — Unit and integration tests

## Getting Started

### Prerequisites
- Java 17+
- PostgreSQL running locally
- Maven 3.8+

### Setup

```bash
# Create the database
psql -U postgres -c "CREATE DATABASE preptrack_db;"

# Clone and run
git clone https://github.com/<your-username>/preptrack.git
cd preptrack
./mvnw spring-boot:run
```

### API Documentation
Once running, visit: http://localhost:8080/swagger-ui.html

## Project Structure

```
com.preptrack/
├── config/          SecurityConfig, AsyncConfig, OpenApiConfig, JpaConfig
├── controller/      8 REST controllers
├── domain/          7 JPA entities + 4 enums
├── dto/             Request records (validated) + Response records
├── exception/       GlobalExceptionHandler + 3 custom exceptions
├── repository/      Spring Data JPA interfaces
├── scheduler/       Daily revision digest (@Scheduled)
├── security/        JWT provider, filter, UserDetailsService
├── service/         9 services — all business logic
└── util/            SecurityUtil (extracts current user from context)
```

## Key Design Decisions

**Readiness Score** — Weighted scoring: `Σ(confidence × importance) / Σ(5 × importance) × 100`. Higher-importance topics affect the score more than peripheral ones.

**Spaced Repetition** — Revision intervals are driven by confidence score: 1→1 day, 2→3 days, 3→7 days, 4→14 days, 5→30 days. Priority = `(6 - confidence) × overdue_ratio`.

**State Machine** — Application status transitions are defined in a `Map<Status, Set<Status>>`. Invalid transitions throw `InvalidStateTransitionException` with the allowed next states.

**Async Report** — Weekly report fetches study logs and applications concurrently via `CompletableFuture.allOf()`, then aggregates with stream collectors.
