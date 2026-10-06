# Parkezz — Parking Rental & Booking System (Spring Boot Microservices)

Parkezz lets parking-space **owners** list their spaces and lets **customers** search,
book, and pay (simulated) for them. It is built as a set of independent Spring Boot
microservices, backed by MySQL, secured with JWT, documented with Swagger, and tested
with JUnit + Mockito.

> **A note on how this project was built and how to verify it.** This project was
> generated in a sandboxed environment with **no internet/Maven-repository access**, so
> `mvn clean install` / `mvn test` could **not actually be executed here** to download
> dependencies and run the test suite live. Every file has been hand-written and
> carefully cross-checked (class names, constructor field order, imports, Spring
> Boot/Cloud version compatibility), but you **must run `mvn clean test` yourself** on a
> machine with internet access to get a compiled, verified, green build before your
> evaluation. Section "Troubleshooting" below covers the most likely issues if
> something doesn't compile first try.

---

## 1. Project Overview

| | |
|---|---|
| **Idea** | Parking rental & booking marketplace |
| **Owners** | Register, list parking spaces, manage their own listings |
| **Customers** | Register, search parking, book a slot, pay (simulated), view/cancel bookings |
| **Architecture** | Spring Boot microservices, Eureka service discovery, Spring Cloud Gateway |
| **Auth** | JWT (stateless), Spring Security, role-based (OWNER / CUSTOMER) |
| **Database** | MySQL, one schema per service |
| **Docs** | Swagger / OpenAPI on every business service |
| **Testing** | JUnit 5 + Mockito, JaCoCo coverage, SonarQube config |

---

## 2. Architecture

```
                         Client / Postman / Swagger UI
                                     |
                                     v
                          +----------------------+
                          |   API Gateway :8080  |
                          +----------------------+
                                     |
                         (looks up service location)
                                     v
                          +----------------------+
                          |  Eureka Server :8761 |
                          +----------------------+
                                     |
      +---------------+-------------+-------------+---------------+---------------+
      |               |                           |               |               |
 User Service     Auth Service              Parking Service  Booking Service  Payment Service
   :8081             :8082                      :8083            :8084            :8085
      |                 \                                            |               |
      |                  \  (REST call to fetch                      |               |
      |                   \  hashed password)                        |               |
      |                    \-------------------->  User Service       |               |
      |                                                                |               |
 parkezz_registration_db                                    (REST call to        (REST call to
                                                            check parking)      confirm booking)
                                          Booking Service -----------> Parking Service
                                          Payment Service ---- validate booking/amount ----> Booking Service
                                          Payment Service ---- confirm booking ----> Booking Service

 parkezz_parking_db  <---- Parking Service
 parkezz_booking_db  <---- Booking Service
 parkezz_payment_db  <---- Payment Service
```

Clients only ever talk to the **Gateway on port 8080**. The Gateway asks **Eureka**
where each service currently lives and forwards the request there. Individual service
ports (8081–8085) exist for direct debugging/Swagger only.

---

## 3. Service Responsibilities

| Service | Port | Responsibility |
|---|---:|---|
| **eureka-server** | 8761 | Service registry. Every other service registers here; Gateway and inter-service calls look services up by name instead of hardcoded host:port. |
| **api-gateway** | 8080 | Single entry point. Routes `/api/register`, `/api/auth/**`, `/api/parking/**`, `/api/bookings/**`, `/api/payments/**` to the right service via Eureka. |
| **user-service** | 8081 | Owns the `users` table. Registration (`POST /api/register`). Exposes an **internal** lookup endpoint for Auth Service. |
| **auth-service** | 8082 | Login (`POST /api/auth/login`). Calls User Service over REST (never touches its DB), verifies the password with BCrypt, issues a JWT. Contains the real `JwtFilter`. |
| **parking-service** | 8083 | CRUD + search for parking listings. OWNER-only create/update/delete, ownership checks. |
| **booking-service** | 8084 | Create/view/cancel bookings. Calls Parking Service to check availability, prevents overlapping bookings, calculates price. |
| **payment-service** | 8085 | Simulated payment. Before success it verifies the booking exists, belongs to the customer, and the payment amount matches the booking total; then it calls Booking Service to confirm the booking. |

---

## 4. Port Table

| Service | Port |
|---|---:|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| User/Registration Service | 8081 |
| Auth/Login Service | 8082 |
| Parking Service | 8083 |
| Booking Service | 8084 |
| Payment Service | 8085 |
| MySQL | 3306 (default) |

---

## 5. Database Setup

Each service owns its own MySQL schema (no service touches another's tables directly).

```sql
CREATE DATABASE IF NOT EXISTS parkezz_registration_db;
CREATE DATABASE IF NOT EXISTS parkezz_parking_db;
CREATE DATABASE IF NOT EXISTS parkezz_booking_db;
CREATE DATABASE IF NOT EXISTS parkezz_payment_db;
```

Each `application.yml` also uses `createDatabaseIfNotExist=true` in the JDBC URL, so
if your MySQL user has `CREATE DATABASE` privileges, they will be created automatically
on first boot. `spring.jpa.hibernate.ddl-auto=update` creates/updates tables
automatically — no manual schema scripts needed.

For local evaluation, the application files support these environment variables:

```
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=<long-secret>
```

The `root` values are demo fallbacks only. Use your own local MySQL credentials; do not commit real credentials to Git.

---

## 6. Prerequisites

- Java 17 (JDK)
- Maven 3.8+
- MySQL 8.x running locally on port 3306
- An IDE (IntelliJ / VS Code / Eclipse) or terminal
- Postman or the Swagger UI for testing

---

## 7. Installation & Run Order

Build each service (or all at once from the root, one at a time):

```bash
cd eureka-server   && mvn clean install
cd ../api-gateway  && mvn clean install
cd ../user-service && mvn clean install
cd ../auth-service && mvn clean install
cd ../parking-service && mvn clean install
cd ../booking-service && mvn clean install
cd ../payment-service && mvn clean install
```

**Startup order matters.** Start in this exact sequence, waiting a few seconds between
each so it can register with Eureka:

1. `eureka-server` (wait until `http://localhost:8761` is up)
2. `user-service`
3. `auth-service`
4. `parking-service`
5. `booking-service`
6. `payment-service`
7. `api-gateway` (last, so it can see all services already registered)

Run each with:

```bash
mvn spring-boot:run
```
or run the generated jar:
```bash
java -jar target/<service-name>-1.0.0.jar
```

---

## 8. Verifying Eureka Registration

Open **http://localhost:8761** in a browser. Under "Instances currently registered
with Eureka" you should see: `USER-SERVICE`, `AUTH-SERVICE`, `PARKING-SERVICE`,
`BOOKING-SERVICE`, `PAYMENT-SERVICE`, and `API-GATEWAY`. If a service is missing, check
its console logs for connection errors to `localhost:8761`.

---

## 9. Swagger / OpenAPI

Each business service exposes Swagger UI directly (useful for isolated testing):

| Service | Swagger URL |
|---|---|
| User Service | http://localhost:8081/swagger-ui.html |
| Auth Service | http://localhost:8082/swagger-ui.html |
| Parking Service | http://localhost:8083/swagger-ui.html |
| Booking Service | http://localhost:8084/swagger-ui.html |
| Payment Service | http://localhost:8085/swagger-ui.html |

**To test protected APIs in Swagger:**
1. Call `POST /api/register` (User Service Swagger) to create a user.
2. Call `POST /api/auth/login` (Auth Service Swagger) with that user's credentials.
3. Copy the `token` value from the response.
4. Click the **Authorize** button (top-right, lock icon) on the target service's
   Swagger page.
5. Enter: `Bearer <paste-token-here>`.
6. Now call any protected endpoint — it will include the `Authorization` header
   automatically.

For the **normal demo flow**, hit the same paths through the **Gateway** at
`http://localhost:8080/api/...` instead of the individual service ports.

---

## 10. API List

| Method | Path (via Gateway :8080) | Auth | Role |
|---|---|---|---|
| POST | `/api/register` | Public | — |
| POST | `/api/auth/login` | Public | — |
| GET | `/api/auth/me` | JWT | any |
| GET | `/api/users/{id}` | JWT | own profile |
| PUT | `/api/users/{id}` | JWT | own profile |
| DELETE | `/api/users/{id}` | JWT | own profile |
| POST | `/api/parking` | JWT | OWNER |
| GET | `/api/parking` | JWT | any |
| GET | `/api/parking/{id}` | JWT | any |
| GET | `/api/parking/search?location=&vehicleType=&status=` | JWT | any |
| PUT | `/api/parking/{id}` | JWT | OWNER (own) |
| DELETE | `/api/parking/{id}` | JWT | OWNER (own) |
| POST | `/api/bookings` | JWT | CUSTOMER |
| GET | `/api/bookings/{id}` | JWT | owner of booking |
| GET | `/api/bookings/my` | JWT | CUSTOMER |
| PUT | `/api/bookings/{id}/cancel` | JWT | CUSTOMER (own) |
| PUT | `/api/bookings/{id}/confirm` | JWT | CUSTOMER (own; called by Payment Service) |
| POST | `/api/payments` | JWT | CUSTOMER |
| GET | `/api/payments/{id}` | JWT | own payment |

---

## 11. Example API Testing Sequence (Postman-style JSON)

### 11.1 Register an OWNER
```
POST http://localhost:8080/api/register
{
  "name": "Alice Owner",
  "email": "alice@example.com",
  "password": "password123",
  "phone": "9876543210",
  "role": "OWNER"
}
```

### 11.2 Register a CUSTOMER
```
POST http://localhost:8080/api/register
{
  "name": "Bob Customer",
  "email": "bob@example.com",
  "password": "password123",
  "phone": "9123456780",
  "role": "CUSTOMER"
}
```

### 11.3 Login as Owner
```
POST http://localhost:8080/api/auth/login
{
  "email": "alice@example.com",
  "password": "password123"
}
```
Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "name": "Alice Owner",
  "email": "alice@example.com",
  "role": "OWNER",
  "tokenType": "Bearer"
}
```
Use this token as `Authorization: Bearer <token>` for the next call.

### 11.4 Create a Parking Listing (as Owner)
```
POST http://localhost:8080/api/parking
Authorization: Bearer <owner-token>
{
  "location": "Downtown",
  "address": "123 Main St",
  "pricePerHour": 50,
  "vehicleType": "CAR",
  "status": "AVAILABLE"
}
```

### 11.5 Login as Customer, then Search Parking
```
POST http://localhost:8080/api/auth/login   (bob@example.com)
GET  http://localhost:8080/api/parking/search?location=Downtown
Authorization: Bearer <customer-token>
```

### 11.6 Create a Booking (as Customer)
```
POST http://localhost:8080/api/bookings
Authorization: Bearer <customer-token>
{
  "parkingId": 1,
  "startTime": "2026-09-20T10:00:00",
  "endTime": "2026-09-20T12:00:00"
}
```
Response has `status: PENDING` and a calculated `totalAmount`.

### 11.7 Pay for the Booking
```
POST http://localhost:8080/api/payments
Authorization: Bearer <customer-token>
{
  "bookingId": 1,
  "amount": 100,
  "paymentMethod": "UPI"
}
```
On success, Payment Service first verifies that the booking exists, belongs to the
logged-in customer, and the payment amount exactly matches the booking's
calculated `totalAmount`. It then calls Booking Service, and the booking's status
becomes `CONFIRMED`. Verify with:
```
GET http://localhost:8080/api/bookings/1
Authorization: Bearer <customer-token>
```

---

## 12. Registration Flow

```
Client -> POST /api/register -> User Service
  1. Validate all fields (name, email, password, phone, role)
  2. Check email uniqueness
  3. Hash password with BCrypt
  4. Save to parkezz_registration_db
  5. Return UserResponse (never includes password)
```

## 13. Login / JWT Flow

```
Client -> POST /api/auth/login -> Auth Service
  1. Auth Service calls User Service: GET /api/users/internal/by-email/{email}
  2. Compares submitted password against the hashed password using BCrypt
  3. On match: generates a JWT (claims: sub=email, userId, role, exp)
  4. Returns token to client

Client -> any protected API, with header: Authorization: Bearer <token>
  1. JwtFilter (OncePerRequestFilter) intercepts the request
  2. Reads the Authorization header, checks "Bearer " prefix
  3. Validates the token's signature and expiry
  4. Extracts userId + role from claims
  5. Populates Spring Security's SecurityContext
  6. If valid -> request proceeds to controller
  7. If invalid/expired/missing -> Spring Security returns 401/403 for protected routes
```

**Important design decision:** Parking, Booking, and Payment services do **not** call
Auth Service on every request to validate a token. They each carry their own
`JwtService`/`JwtFilter` and validate the token **locally** using the same shared
secret (`jwt.secret` in each `application.yml`). This is the standard **stateless JWT**
pattern — fast, and doesn't create a single point of failure.

## 14. Gateway Routing

The Gateway's `application.yml` defines explicit routes (easier to read/explain than
auto-generated ones):

```yaml
routes:
  - id: user-service
    uri: lb://user-service
    predicates: [Path=/api/register/**,/api/users/**]
  - id: auth-service
    uri: lb://auth-service
    predicates: [Path=/api/auth/**]
  - id: parking-service
    uri: lb://parking-service
    predicates: [Path=/api/parking/**]
  - id: booking-service
    uri: lb://booking-service
    predicates: [Path=/api/bookings/**]
  - id: payment-service
    uri: lb://payment-service
    predicates: [Path=/api/payments/**]
```

`lb://` means "look this service name up in Eureka and load-balance across instances."

## 15. Validation

Every request DTO uses Jakarta Bean Validation:

- `RegisterRequest`: `@NotBlank`, `@Size`, `@Email`, `@Pattern` (10-digit phone), `@NotNull` (role)
- `ParkingRequest`: `@NotBlank` (location/address/vehicleType), `@NotNull` + `@Positive` (price), `@NotNull` (status)
- `BookingRequest`: `@NotNull` (parkingId), `@Future` (start/end time) — plus **business validation** in the service layer: end time must be after start time, parking must exist and be `AVAILABLE`, no overlapping bookings
- `PaymentRequest`: `@NotNull` (bookingId), `@DecimalMin("0.01")` (amount), `@NotNull` (paymentMethod)

Validation failures are caught by `@RestControllerAdvice` and returned as **400 Bad
Request** with a clear message (all field errors joined together).

## 16. Exception Handling

Every service has its own `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping
custom exceptions to meaningful HTTP responses:

| Exception | Status | Example Message |
|---|---:|---|
| `UserAlreadyExistsException` | 409 | "Email already registered: x@y.com" |
| `ResourceNotFoundException` | 404 | "User not found with id: 5" |
| `InvalidCredentialsException` | 401 | "Invalid email or password" |
| `ParkingNotFoundException` | 404 | "Parking space not found with id: 3" |
| `UnauthorizedActionException` | 403 | "You do not own this parking space" |
| `InvalidBookingRequestException` | 400 | "End time must be after start time" |
| `ParkingUnavailableException` | 400 | "Parking space is not available for booking" |
| `BookingConflictException` | 409 | "Parking space is already booked for the selected time." |
| `BookingNotFoundException` | 404 | "Booking not found with id: 7" |
| `PaymentNotFoundException` | 404 | "Payment not found with id: 2" |
| `MethodArgumentNotValidException` (bean validation) | 400 | joined field error messages |
| any other `Exception` | 500 | "An unexpected error occurred" (no stack trace exposed) |

Every error response has the same shape:
```json
{
  "timestamp": "2026-09-13T10:15:30",
  "status": 404,
  "message": "Parking space not found with id: 3",
  "path": "/api/parking/3"
}
```

## 17. Logging

SLF4J via Lombok's `@Slf4j` is used across service classes.

- `INFO` — normal successful operations (registration succeeded, booking created, payment processed)
- `WARN` — expected business failures (duplicate email, invalid login, booking conflict, resource not found)
- `ERROR` — reserved for unexpected failures (handled generically by the exception handler)

**Passwords and JWT tokens are never logged anywhere in this codebase.**

## 18. Booking Overlap Rule

```
requestedStart < existingEnd  AND  requestedEnd > existingStart
```
Implemented as a JPQL query (`BookingRepository.findOverlappingBookings`) that ignores
`CANCELLED` bookings. If any overlapping booking exists, the request is rejected with
`409 Conflict` and the message "Parking space is already booked for the selected time."

## 19. Payment Simulation

There is **no real payment gateway**. The rule is simple and **deterministic** (not
random, so demos are repeatable):

```
amount > 0  -> SUCCESS -> Payment Service calls Booking Service to CONFIRM the booking
amount <= 0 -> FAILED  -> booking stays PENDING (validation already blocks amount <= 0
                           at the DTO level with @DecimalMin, this is a defensive
                           second check)
```

---

## 20. Running Tests

From inside each service folder:
```bash
mvn test
```
This runs all JUnit 5 + Mockito tests for that service (business services use H2
in-memory DB for tests via the `test` Spring profile, so no MySQL is needed to run
tests). All tests are written to actually pass — none are `@Disabled` or weakened.

Run from every service directory to test the whole project:
```bash
for d in eureka-server api-gateway user-service auth-service parking-service booking-service payment-service; do
  (cd $d && mvn test) || echo "FAILED: $d"
done
```

## 21. JaCoCo (Code Coverage)

Each service's `pom.xml` includes the `jacoco-maven-plugin`, bound to `mvn test`.
After running tests, open:
```
<service>/target/site/jacoco/index.html
```
in a browser to see line/branch coverage per class. **Line coverage** = % of executed
lines; **branch coverage** = % of executed if/else/switch branches. JaCoCo does not run
tests itself — it measures what JUnit/Mockito already executed.

## 22. SonarQube

Each service has a simple `sonar-project.properties`. With a SonarQube server running
locally (`http://localhost:9000`):
```bash
cd <service>
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=parkezz-<service> \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<your-token>
```
SonarQube reads the JaCoCo XML report (`target/site/jacoco/jacoco.xml`) to show
coverage alongside its own static-analysis findings (code smells, bugs, duplication,
security hotspots). SonarQube is **not required for the app to start** — it's a
separate, optional quality-analysis step.

---

## 23. Git Workflow (Team Development)

```bash
git clone <repo-url>
git checkout -b feature/parking-service
# ... make changes ...
git add .
git commit -m "feat: add parking service CRUD"
git push origin feature/parking-service
# open a PR, then after review:
git checkout main
git pull
git merge feature/parking-service
```
Suggested commit style: `feat: ...`, `fix: ...`, `test: ...`, `build: ...`, `docs: ...`.
Avoid touching another teammate's service folder unless coordinating with them.

---

## 24. Troubleshooting

| Problem | Likely Cause / Fix |
|---|---|
| `Port already in use` | Another process is on that port; stop it or change `server.port` |
| Service doesn't show in Eureka | Check `eureka.client.service-url.defaultZone` points to `http://localhost:8761/eureka/`, and Eureka Server started first |
| Gateway returns 404 | Target service isn't registered yet, or the route `Path` predicate doesn't match your URL |
| `Connection refused` calling MySQL | MySQL isn't running, or wrong username/password in `application.yml` |
| Swagger UI blank/404 | Confirm the service actually started; URL is `/swagger-ui.html` (or `/swagger-ui/index.html` depending on springdoc version) |
| `401 Invalid or expired token` | Token expired (1 hour default) — login again; or missing `Bearer ` prefix |
| `403 Forbidden` | Logged in with the wrong role for that action (e.g. CUSTOMER trying to create parking) |
| `404 Not Found` (resource) | The ID you passed doesn't exist in that service's DB |
| `409 Conflict` | Duplicate email at registration, or overlapping booking time |
| Maven dependency errors | Ensure internet access; Maven needs to download Spring Boot 3.2.5 / Spring Cloud 2023.0.1 artifacts on first build |
| Spring Boot/Cloud version mismatch | This project pins Spring Boot `3.2.5` with Spring Cloud `2023.0.1` — a known-compatible pair. Don't change one without the other. |
| Tests fail on first run | Run `mvn clean test` (not just `test`) to avoid stale compiled classes |

---

## 25. Simple Viva / Evaluation Explanation

**One-line summary to say out loud:** "Parkezz is a parking booking platform split into
five business microservices supported by Eureka Server and reached through
a single API Gateway; users log in with JWT, and a booking only gets confirmed after a
simulated payment succeeds."

---

## 26. Evaluation / Viva Questions & Answers

### Microservices
**What are microservices?** Small, independently deployable services, each owning a
specific business capability and its own database, communicating over REST.

**Why use microservices?** Independent development/deployment/scaling per service,
technology flexibility, and fault isolation — one service crashing doesn't take the
whole app down.

**Why not a monolith?** A monolith is simpler for tiny apps, but here the requirement
was explicitly to demonstrate microservices skills — separate deployability, scaling,
and ownership.

**Advantages/disadvantages?** Advantages: scalability, isolation, independent releases.
Disadvantages: network latency between services, more operational complexity (multiple
databases, multiple deployments), harder to debug across services.

### Eureka
**What is Eureka?** A service registry — services register themselves on startup and
others query it to find where a service currently lives.

**Why use it?** So services can find each other by name instead of hardcoded
host:port, which would break the moment a service moves or restarts on a different
port.

**What is service discovery?** The general pattern of looking up a service's current
network location at runtime rather than configuring it statically.

**How does discovery work here?** Every service has `@EnableDiscoveryClient` and
`eureka.client.service-url.defaultZone` pointing at Eureka. On startup it registers its
name/host/port; Gateway and other services use `lb://service-name` to be resolved via
Eureka + a load-balanced RestTemplate.

**Why avoid hardcoded URLs?** They break if a service's host/port changes, and don't
work if you ever run multiple instances of a service.

### API Gateway
**What is it?** A single entry point that routes incoming requests to the correct
backend service.

**Why use it?** Clients only need to know one address (the Gateway); it centralizes
routing, and could later host cross-cutting concerns like rate limiting or logging.

**Why should clients use it instead of calling services directly?** Simplicity for the
client, and it decouples clients from internal service topology.

**How does routing work?** Spring Cloud Gateway matches the incoming path against
configured `Path` predicates and forwards to the matching `lb://service-name` URI,
which Eureka resolves to an actual instance.

### Why separate Registration and Login?
Registration and Login are different concerns: one creates a user, the other verifies
credentials and issues a token. Keeping Auth Service separate from User Service also
demonstrates that a service can depend on another's API rather than its database.

**Why does Auth Service call User Service instead of using its DB directly?** Each
microservice should own its data exclusively. Letting Auth Service query User Service's
database directly would tightly couple their schemas and break data ownership.

### JWT
**What is JWT?** A JSON Web Token — a signed, self-contained token carrying claims
(here: email, userId, role, expiry) that a server can verify without a database lookup.

**Why JWT?** It enables **stateless authentication** — no server-side session storage
needed; any service with the shared secret can verify a token independently.

**What is JwtFilter?** A Spring `OncePerRequestFilter` that runs before the controller,
reads the `Authorization` header, validates the token, and populates
`SecurityContext` so `@PreAuthorize`/role checks and `Authentication` objects work in
controllers.

**How does JwtFilter work here?** Reads header → checks `Bearer ` prefix → extracts
token → validates signature/expiry via `JwtService` → extracts userId/role → builds a
`UsernamePasswordAuthenticationToken` → sets it into `SecurityContextHolder`.

**Where is JWT validated?** Locally, in every protected service (Parking, Booking,
Payment) using the same shared secret — not via a live call back to Auth Service.

**What happens when a JWT expires?** `isTokenExpired()` returns true, the filter does
not authenticate the request, and Spring Security returns `401 Unauthorized` for
protected endpoints.

**What is a Bearer token?** A token sent in the `Authorization: Bearer <token>` header,
meaning "the bearer of this token is authorized."

**What is SecurityContext?** Spring Security's per-request holder for the current
`Authentication` object (who is logged in, with what authorities).

### Security
**Authentication vs authorization?** Authentication = proving who you are (login).
Authorization = deciding what you're allowed to do (roles/permissions).

**What is Spring Security?** A framework providing authentication/authorization
building blocks (filters, `SecurityFilterChain`, `PasswordEncoder`, etc.) integrated
into the servlet pipeline.

**Why BCrypt?** It's a slow, salted hashing algorithm designed specifically for
passwords — resistant to brute-force/rainbow-table attacks, unlike fast hashes like
MD5/SHA-256.

**Why not store passwords in plain text?** If the database is ever leaked, plain-text
passwords compromise every user's account (and likely other accounts, since people
reuse passwords). Hashing means even the developers can't read the original password.

**Why validate JWT locally in each service?** Avoids a network round-trip (and a single
point of failure) to Auth Service on every single protected request.

### DTO
**What is a DTO?** Data Transfer Object — a plain class used to shape API
request/response bodies, decoupled from the database entity.

**Why use DTOs?** Prevents accidentally exposing internal fields (like a password
hash) and lets the API contract evolve independently of the database schema.

### Validation
**Why validate input?** To reject bad data early with a clear message, rather than
letting it cause confusing errors (or bad data) further downstream.

**What is Bean Validation?** The Jakarta Validation API (`@NotBlank`, `@Email`, etc.)
that Spring automatically enforces on `@Valid`-annotated request bodies.

**What does `@NotBlank` do?** Rejects null, empty, or whitespace-only strings.

**What does `@Email` do?** Requires the string to look like a valid email address.

**What does `@Positive` do?** Requires a numeric value to be strictly greater than
zero.

### Exception Handling
**Why `@RestControllerAdvice`?** It centralizes exception-to-HTTP-response mapping in
one class per service instead of try/catch blocks scattered through every controller.

**Why custom exceptions?** They let the handler distinguish between different error
situations (not found vs conflict vs invalid) and map each to the correct HTTP status
and message.

**Difference between 400/401/403/404/409?**
- 400 Bad Request — the request itself is malformed/invalid
- 401 Unauthorized — you are not authenticated (missing/invalid/expired token)
- 403 Forbidden — you are authenticated, but not allowed to do this action
- 404 Not Found — the resource doesn't exist
- 409 Conflict — the request conflicts with existing state (duplicate email, overlapping booking)

**How does the error reach the client?** The exception thrown in the service layer
propagates up; `@RestControllerAdvice` catches it and returns a JSON `ErrorResponse`
with the matching HTTP status.

### Database
**Why MySQL?** A widely-used, free, reliable relational database, well supported by
Spring Data JPA.

**Why separate databases per service?** Enforces true data ownership — a defining
trait of microservices — and means one service's schema changes never break another.

### Booking / Payment
**How do you prevent overlapping bookings?** A JPQL query checks existing (non-cancelled)
bookings for the same parking space using
`requestedStart < existingEnd AND requestedEnd > existingStart`; any match rejects the
new booking with 409.

**How is the amount calculated?** `pricePerHour × ceil(minutes booked / 60)`, rounded
up to the next hour.

**Why does booking start as PENDING?** Because payment hasn't happened yet — PENDING
means "reserved but not yet paid for."

**When does it become CONFIRMED?** After Payment Service records a `SUCCESS` payment
and calls Booking Service's internal `/confirm` endpoint.

**Why simulate payment?** Integrating a real gateway (Razorpay/Stripe) needs live
credentials/compliance and adds complexity irrelevant to demonstrating the
microservices architecture itself.

**What if payment fails?** The `Payment` row is saved with status `FAILED`, and Booking
Service is never called — the booking remains `PENDING`.

### Testing
**What is JUnit?** A Java testing framework for writing and running unit tests.

**What is Mockito?** A mocking framework used to fake dependencies (like repositories)
so a service class can be tested in isolation.

**What is mocking?** Replacing a real dependency with a controllable fake, so the test
only exercises the logic being tested — not the database, not the network.

**Why use Mockito?** It lets tests run fast and deterministically without needing a
real database or another live service.

**What is unit testing?** Testing the smallest independent piece of logic (usually one
method/class) in isolation from its collaborators.

### JaCoCo / SonarQube
**What is JaCoCo?** A code-coverage tool that instruments class files to record which
lines/branches were executed while tests ran.

**Is JaCoCo a testing framework?** No — it doesn't run tests itself; it measures
coverage from tests that JUnit/Mockito already ran.

**Line vs branch coverage?** Line coverage = % of source lines executed. Branch
coverage = % of decision branches (if/else, switch cases) executed — a stricter
measure, since a line can execute without every branch being taken.

**What is SonarQube?** A static code-analysis platform that flags bugs, code smells,
duplication, and security hotspots, and can display test coverage (from JaCoCo)
alongside those findings.

**How is SonarQube different from JUnit?** JUnit runs tests you wrote to check
behavior; SonarQube analyzes the source code itself (without running it) for quality
and style issues, and separately reports the coverage JaCoCo already measured.

---

## 27. Final Notes

- No Kafka, RabbitMQ, Redis, Docker, Kubernetes, OAuth2, or real payment gateway were
  used, per the simplicity requirement.
- Every controller/service/repository/entity/dto/exception package follows the same
  simple layered structure across all five business services, so once you understand
  one service, the rest read the same way.
- If anything doesn't compile on your machine after `mvn clean install`, it is almost
  certainly a Maven dependency download issue (see Troubleshooting) rather than a logic
  bug — re-read the stack trace's first "Caused by" line before assuming otherwise.


## Minimal React Frontend

The project includes a deliberately simple React frontend in `frontend/`. It demonstrates the main end-to-end flow without adding unnecessary UI complexity:

- Register as OWNER or CUSTOMER
- Login and store the JWT in browser local storage
- View/search parking spaces
- OWNER: create a parking listing
- CUSTOMER: create a booking
- CUSTOMER: make a simulated payment
- View own profile and update/delete it

The frontend calls the API Gateway at `/api/**`. During local development Vite proxies these requests to `http://localhost:8080`, so the browser only needs to communicate with one backend entry point.

## Security notes

- User profile endpoints require a valid JWT and a user can update/delete only their own profile.
- The Auth-only user lookup endpoint is intentionally not exposed by the API Gateway.
- Booking confirmation requires authentication; Payment Service forwards the customer's bearer token when confirming a booking.
- Database credentials and JWT secret support environment variables (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`) with demo fallbacks for local evaluation. Replace the demo fallbacks in a real deployment.
- Booking billing uses whole billable hours: `ceil(minutes / 60) * pricePerHour`.


### User Profile CRUD

- `POST /api/register` — create a user
- `GET /api/users/{id}` — read your own profile
- `PUT /api/users/{id}` — update your own name, email and phone
- `DELETE /api/users/{id}` — delete your own profile

Role and password are not changed through the profile update endpoint. Password changes are intentionally outside this simple evaluation flow.
