# Parkezz Enhancement Changelog

This document lists every change made to the original project. Nothing in the
architecture, ports, database-per-service design, JWT design, or Gateway
routing approach was redesigned — only the items below were added or fixed.

**Important honesty note:** the sandbox this work was produced in has no
internet access and no Maven installed, so none of the code below has been
compiled or executed. Everything is written by hand, carefully cross-checked
against your actual source files (exact method signatures, exact validation
messages, exact exception types), but it is **unverified** until you run it
locally. See `VERIFICATION_CHECKLIST.md` for exactly what to run.

## 1. Genuine bugs found and fixed

1. **API Gateway was missing the user-profile route.** Only
   `/api/register/**` was routed to `user-service` — `GET/PUT/DELETE
   /api/users/{id}` (view/update/delete profile) had no route at all, so
   those calls 404'd through the Gateway even though User Service handled
   them fine directly. Fixed by adding a `user-service-profile` route scoped
   to `/api/users/{id}` (a single path segment) so the internal
   `/api/users/internal/by-email/**` endpoint — meant only for direct
   Auth-Service-to-User-Service calls via Eureka — stays unreachable from the
   public Gateway. The pre-existing `GatewayApplicationTests` asserted exactly
   5 routes named `user-service`; updated it to expect the corrected 6 routes
   (`user-service-register`, `user-service-profile`, plus the other 4).
2. **`PaymentServiceImplTest` did not compile.** Its
   `getById_paymentNotFound_throwsPaymentNotFoundException` test called
   `paymentService.getById(99L)` (one argument), but `PaymentService.getById`
   takes `(Long id, Long userId)`. This means `mvn clean test` on
   payment-service was already failing before any of these changes. Fixed
   the call and added the missing `userId` argument.
3. **Malformed JSON bodies and missing headers returned 500, not 400.**
   Every service's `GlobalExceptionHandler` had no handler for
   `HttpMessageNotReadableException` (bad/invalid JSON, invalid enum values
   like `"role":"ADMIN"`) or `MissingRequestHeaderException` (booking/payment
   controllers that `@RequestHeader("Authorization")`), so both fell through
   to the generic 500 handler. Added both handlers, returning 400, to all 5
   services' handlers — the rest of the existing error-mapping design
   (409/403/404/401/500) was left exactly as it was.

## 2. AOP logging (new)

- Added `spring-boot-starter-aop` to user, auth, parking, booking and
  payment services (NOT to api-gateway or eureka-server, which have no
  `..service..` package to intercept — adding an unused dependency there
  would be needless).
- Added one `LoggingAspect` class per service, at
  `com.parkezz.<service>.aop.LoggingAspect`, with an `@Around` advice on
  `execution(* com.parkezz.<service>.service..*(..))`. It logs method entry
  (argument **class names only**, never the argument values — see the
  "credential safety" note below), successful exit with elapsed time, and any
  exception with its message.
- **Credential safety:** the aspect logs `RegisterRequest`, `LoginRequest`,
  etc. only as their simple class name (`"[RegisterRequest]"`), never their
  field values, so a password can never reach the log through this aspect.
  This mirrors the project's existing rule that passwords and JWTs are never
  logged.

## 3. Physical log file (new)

- Added `logback-spring.xml` to all 7 services (including api-gateway and
  eureka-server, for consistency), each writing to `logs/parkezz.log`
  relative to that service's own working directory, with a
  size-and-time-based rolling policy (10MB / 14 days) plus the existing
  console output preserved.
- Because each service is its own Spring Boot process (by design — this is
  the same reasoning as the database-per-service split), each one produces
  its **own** `logs/parkezz.log` under wherever it's launched from (typically
  each service's own directory). This is not a single shared file across all
  7 JVMs — a shared central log would need a log-shipping stack (ELK, etc.),
  which the README explicitly says was intentionally excluded for
  simplicity.

## 4. Exception handling

- Added `HttpMessageNotReadableException` → 400 and
  `MissingRequestHeaderException` → 400 handlers to all 5 core services (see
  bug #3 above). Every other status-code mapping (400/401/403/404/409/500)
  was already correct and was left untouched.

## 5. Validation

- Inspected `RegisterRequest`, `UserUpdateRequest`, `LoginRequest`,
  `ParkingRequest`, `BookingRequest`, `PaymentRequest` — all already used
  correct Jakarta Bean Validation annotations with clear messages. No
  annotations were added or changed; they were already complete.
- Added client-side registration validation in the React frontend (see
  section 7) that mirrors these exact messages, so obviously invalid input
  (e.g. `hello` as an email) is caught before a network call, while the
  backend `@Valid` remains the actual source of truth.

## 6. Tests added (by service)

All new tests use JUnit 5 + Mockito + MockMvc/Spring Boot Test, matching the
stack already in place. Nothing was rewritten to use a different testing
approach.

**user-service**
- Expanded `UserServiceImplTest`: `getById` (own profile / not found /
  unauthorized), `update` (success / duplicate email / not found /
  unauthorized), `delete` (success / not found / unauthorized).
- New `RegistrationControllerTest` (`@WebMvcTest`): valid registration,
  invalid email, blank name, invalid phone, missing role, invalid role enum
  value, short password, duplicate email (409), malformed JSON (400).
- New `UserControllerTest` (`@WebMvcTest` with the real `SecurityConfig` +
  `JwtFilter` + `JwtService` beans, identity injected via
  `SecurityMockMvcRequestPostProcessors.authentication(...)`): own-profile
  get/update/delete, no-authentication, not-found, another-user's-profile
  (403), invalid email on update.
- New `GlobalExceptionHandlerTest`: every handler method called directly
  (409/403/404/400×3/500), including that the generic handler never leaks
  the original exception message.
- New `JwtFilterTest`: valid token, no header, non-Bearer header, invalid
  token, malformed-token exception swallowed.
- New `JwtServiceTest`: extract userId/role, valid token, expired token,
  malformed token, token signed with a different key.
- New `RegisterRequestValidationTest` / `UserUpdateRequestValidationTest`:
  every field's valid/blank/null/boundary/invalid-format case, asserting the
  exact message text from the DTO.

**auth-service**
- New `AuthControllerTest`: valid login, invalid credentials (401), blank
  email/password (400), invalid email format (400), `/me` with a real
  authentication, `/me` with no header, `/me` with an invalid bearer token.
- New `GlobalExceptionHandlerTest`: invalid credentials, "user not found"
  mapped to a generic 401 (never leaking whether the email or password was
  wrong), validation, malformed body, missing header, generic 500.
- New `JwtFilterTest`: valid token → email + role authorities, expired token,
  malformed token, no header.
- New `LoginRequestValidationTest`: blank/invalid email, blank/null password.
- Expanded `AuthServiceImplTest`: user-not-found from User Service, User
  Service returning `null`, OWNER-role login response.

**parking-service**
- Expanded `ParkingServiceImplTest`: `getById` found, `getAll`, `search`
  (with results and empty), `update`/`delete` not-found branches.
- New `ParkingControllerTest`: OWNER-only create/update/delete (403 for
  CUSTOMER), blank location / negative price (400), public-to-authenticated
  GET (401/403 when logged out), not-found (404), ownership check (403),
  search with query params.
- New `GlobalExceptionHandlerTest` and `ParkingRequestValidationTest`
  (blank location/address, null/zero/negative price, blank vehicle type).

**booking-service** (already had the best existing coverage; filled the
remaining gaps)
- Expanded `BookingServiceImplTest`: non-overlapping booking succeeds,
  `getById` own booking / not-owner, `getMyBookings` (with and without
  results), `confirm` not-owner / already-confirmed / cancelled / not-found,
  `cancel` not-found.
- New `BookingControllerTest`: CUSTOMER-only create (403 for OWNER),
  start-time-in-the-past (400), missing parkingId (400), parking-unavailable
  (400), overlapping booking (409), `/my`, cancel, not-found (404).
- New `GlobalExceptionHandlerTest` and `BookingRequestValidationTest`.

**payment-service**
- Fixed the pre-existing compile error (see bug #2) and expanded
  `PaymentServiceImplTest`: unauthorized access to another user's payment,
  negative amount → FAILED + booking never confirmed, payment correctly
  saved with the caller's userId and the request's bookingId.
- New `PaymentControllerTest`: CUSTOMER-only pay (403 for OWNER), zero /
  negative amount (400), missing bookingId (400), missing Authorization
  header (400), own-payment get, not-found (404), another-user's-payment
  (403).
- New `GlobalExceptionHandlerTest` and `PaymentRequestValidationTest`
  (null/zero/negative amount, boundary 0.01, null payment method).

**api-gateway**
- Updated `GatewayApplicationTests` for the corrected 6-route configuration
  (see bug #1).

**eureka-server** — left as-is (a single `@SpringBootApplication` with no
custom logic beyond the dashboard; the existing `EurekaServerApplicationTests`
context-load smoke test is the appropriate level of testing here, so nothing
was added to avoid meaningless tests).

## 7. Frontend

- Confirmed the existing Vite/React frontend already routes every call
  through the Gateway (`vite.config.js` proxies `/api` → `http://localhost:8080`,
  and `App.jsx` only ever calls relative `/api/...` paths — never
  `localhost:8081-8085` directly). No change needed here.
- Confirmed JWT storage (`localStorage`) and the request helper that attaches
  `Authorization: Bearer <token>` were already correct.
- **Added:** a `validateRegister()` function in `App.jsx` that mirrors the
  backend's exact validation rules and messages (name length, email format,
  password length, 10-digit phone, role required) and blocks the network
  call with an inline message if any fail — matching section 18's example of
  catching `hello` as an invalid email before it reaches the server. This was
  the one genuinely missing piece; login/parking/booking/payment forms
  already had adequate `required`/`type="number"` HTML validation for a
  demo-sized project and were left alone to avoid overengineering.

## 8. What was deliberately NOT changed

- No Kafka, RabbitMQ, Redis, Docker, Kubernetes, OAuth2, or real payment
  gateway was introduced.
- Eureka, Spring Cloud Gateway, and the JWT design were not replaced.
- The booking overlap rule (`requestedStart < existingEnd AND requestedEnd >
  existingStart`, cancelled bookings ignored) and the price formula
  (`pricePerHour × ceil(minutes / 60)`) were verified against the source and
  left untouched — they already matched the README.
- Auth Service's User-Service-mediated login flow (never touching the User
  database directly) was preserved.
- Refresh tokens and logout were not added: the existing design is a
  stateless JWT with no session, and neither feature exists in the source —
  adding them would be a new feature, not a fix, and the prompt asks not to
  add functionality "for the sake of it."
- `jwt.secret` and `DB_PASSWORD` in `application.yml` use Spring's
  `${VAR:default}` placeholder syntax with local-dev fallback values — this
  was already the case in the original project and is standard practice for
  a demo/viva project. These are not real production secrets, so they were
  left as-is rather than stripped (stripping the fallback would break local
  `mvn spring-boot:run` for anyone without the env vars set).

## 9. Payment validation hardening (latest change)

- Payment Service now fetches the booking from Booking Service before saving a payment.
- It verifies the booking exists.
- It verifies the booking belongs to the authenticated customer.
- It verifies the submitted payment amount exactly matches the booking's calculated `totalAmount`.
- Only after those checks does the existing simulated payment flow save the payment and confirm the booking.
- Added focused unit tests for valid payment, amount mismatch, wrong customer, missing booking, null booking response, and the existing defensive failed-payment branch.
- No new external payment provider or complex transaction mechanism was introduced.

## 10. Log file configuration fix (latest change)

- Fixed the previously incomplete Logback file appender configuration.
- Every backend process now writes to a real rolling file under `logs/`.
- Each service has its own file to avoid multiple Spring Boot processes competing for the same file:
  - `logs/parkezz-user-service.log`
  - `logs/parkezz-auth-service.log`
  - `logs/parkezz-parking-service.log`
  - `logs/parkezz-booking-service.log`
  - `logs/parkezz-payment-service.log`
  - `logs/parkezz-api-gateway.log`
  - `logs/parkezz-eureka-server.log`
- Console logging remains enabled. Rolling remains size/time based (10MB / 14 days).
