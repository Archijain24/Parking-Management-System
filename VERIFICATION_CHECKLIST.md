# Verification Checklist

Everything in this ZIP was written by static inspection of your source code in
an environment with no internet access and no Maven installed — **none of it
has been compiled or run**. Please run through this checklist locally; it
should take about 15-20 minutes.

## 1. Unit tests + JaCoCo coverage (run for each service)

```bash
cd user-service    && mvn clean test && open target/site/jacoco/index.html
cd auth-service     && mvn clean test && open target/site/jacoco/index.html
cd parking-service  && mvn clean test && open target/site/jacoco/index.html
cd booking-service  && mvn clean test && open target/site/jacoco/index.html
cd payment-service  && mvn clean test && open target/site/jacoco/index.html
cd api-gateway      && mvn clean test
cd eureka-server    && mvn clean test
```

(`open` is macOS; use `xdg-open` on Linux or just open the HTML file
manually.)

**What to expect / watch for:**
- If a test fails to *compile*, check `CHANGELOG.md` section 1 first — one
  pre-existing compile error (`PaymentServiceImplTest`) was already fixed,
  but if you spot another mismatch between a test and the actual method
  signature, that's a real bug to fix, not something wrong with your setup.
- I have not been able to confirm the actual coverage percentage. Please fill
  in the table below from your own `target/site/jacoco/index.html` reports
  and treat any number I might have guessed at as invalid — only your run
  counts.

| Service | Before | After (fill in from your JaCoCo report) |
|---|---|---|
| user-service | ~30-40% (your original estimate) | ? |
| auth-service | ~30-40% | ? |
| parking-service | ~30-40% | ? |
| booking-service | ~30-40% | ? |
| payment-service | ~30-40% | ? |
| api-gateway | ~30-40% | ? |

If any service lands below ~85%, the JaCoCo report's red/yellow lines will
point at exactly which branch is still uncovered — that's the fastest way to
close the gap with one or two more targeted tests rather than guessing.

## 2. Full build (parent-level, if you have one) or per-module

```bash
# from the Parkezz root, if a parent pom aggregates the modules:
mvn clean install
# otherwise run `mvn clean install` inside each service folder in this order:
# eureka-server -> user-service -> auth-service -> parking-service
# -> booking-service -> payment-service -> api-gateway
```

## 3. Log file check

After starting any one service (e.g. `cd user-service && mvn spring-boot:run`),
confirm its service-specific log file exists. For example:
```bash
ls user-service/logs/parkezz-user-service.log
tail -f user-service/logs/parkezz-user-service.log
```
For the other services, use the matching service name (`parkezz-auth-service.log`,
`parkezz-parking-service.log`, `parkezz-booking-service.log`,
`parkezz-payment-service.log`, `parkezz-api-gateway.log`, or
`parkezz-eureka-server.log`).
You should see `INFO` lines like `Entering UserServiceImpl.register(..)` and
`Exiting UserServiceImpl.register(..) successfully in Xms` from the new AOP
aspect, interleaved with Spring Boot's normal startup logs. Confirm no
password or JWT value ever appears in the file (search for `password` and
`Bearer` — you should only see the words themselves in log *messages*, never
an actual password or token value).

## 4. End-to-end run (in this order, matching the README)

1. `eureka-server` → confirm the dashboard at http://localhost:8761 loads
2. `user-service`, `auth-service`, `parking-service`, `booking-service`,
   `payment-service` → confirm all 5 register themselves in the Eureka
   dashboard within ~30 seconds
3. `api-gateway` → confirm it starts on :8080
4. `cd frontend && npm install && npm run dev` → open http://localhost:5173

Then walk through: register OWNER → login OWNER → create parking → register
CUSTOMER → login CUSTOMER → search parking → create booking → pay with the exact
booking `totalAmount` → confirm the booking status becomes `CONFIRMED`.

Also verify the payment guardrails:
- paying a non-existent booking is rejected
- paying another customer's booking is rejected
- paying an amount different from the booking `totalAmount` is rejected
- only a matching amount for the authenticated customer's booking proceeds to payment

**Specifically re-test the Gateway bug fix:** while logged in, open your
browser's network tab and confirm `GET /api/users/{id}` (loading "My
Profile"), the `PUT` (updating it), and the `DELETE` all return real
responses through port 8080 rather than a 404 — this was broken before this
round of changes.

## 5. Security / credential check

```bash
grep -rn "password\|secret" --include="*.yml" .
```
Confirm every match is a `${ENV_VAR:local-dev-default}` placeholder, not a
real value. If you use a real database password or a production JWT secret
anywhere, set it via an actual environment variable rather than editing the
`.yml` default, and never commit that value into the fallback string.

## 6. Frontend sanity check

```bash
cd frontend && npm install && npm run build
```
Should complete without errors. Then manually try registering with `email:
hello` and confirm you see an inline "Email must be a valid email address"
message without a network request firing (check the Network tab shows
nothing for that failed attempt).
