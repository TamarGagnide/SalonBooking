# Testing

Scenario D: this project's own module — the Booking/Availability logic that backs NFR-03 (no overlapping appointments) and NFR-04 (access control). All numbers and log excerpts below are from real runs: the test suite was executed in a `maven:3.9-eclipse-temurin-21` container against a real PostgreSQL instance (the `backend/docker-compose.yml` `postgres` service), not simulated.

## 8+ test scenarios, with boundary cases

| # | Scenario | Boundary? | Covered by |
|---|---|---|---|
| 1 | Book a free slot within working hours | happy path | `AppointmentServiceTest.bookAppointment_whenSlotIsFree_savesAndReturnsAppointment` |
| 2 | Two ranges overlap | — | `TimeRangeOverlapTest.returnsTrueWhenRangesOverlap` |
| 3 | One range ends exactly when the next starts | boundary — must be *allowed*, not an overlap | `TimeRangeOverlapTest.returnsFalseWhenSecondRangeStartsExactlyWhenFirstEnds` |
| 4 | Ranges far apart, no contact | — | `TimeRangeOverlapTest.returnsFalseWhenRangesAreFarApart` |
| 5 | One range fully contains the other | boundary — containment, not just partial overlap | `TimeRangeOverlapTest.returnsTrueWhenOneRangeFullyContainsTheOther` |
| 6 | Booking rejected when it overlaps an existing active appointment | boundary — the exact conflict NFR-03 exists to prevent | `AppointmentServiceTest.bookAppointment_whenSlotOverlapsExistingAppointment_throwsSlotUnavailable` |
| 7 | Booking rejected when outside the stylist's working hours (or the stylist doesn't work that day at all) | boundary — zero working-hours rows for that day | `AppointmentServiceTest.bookAppointment_whenOutsideStylistWorkingHours_throwsSlotUnavailable` |
| 8 | Simultaneous/concurrent double-booking race, caught at the DB layer after the app-level check passes | boundary — the race condition itself | `AppointmentServiceTest.bookAppointment_whenDatabaseRejectsConcurrentDoubleBooking_throwsSlotUnavailable` (unit, mocked) **and** `AppointmentRepositoryIntegrationTest.databaseRejectsSecondActiveAppointmentOverlappingTheFirstForSameStylist` (integration, real Postgres) |
| 9 | Cancelling your own appointment succeeds | happy path / NFR-04 | `AppointmentServiceTest.cancelAppointment_whenRequestedByOwningCustomer_marksAppointmentCancelled` |
| 10 | Cancelling someone else's appointment is rejected | boundary — access control (NFR-04) | `AppointmentServiceTest.cancelAppointment_whenRequestedByDifferentCustomer_throwsUnauthorized` |
| 11 | Back-to-back appointments for the same stylist are both allowed to persist | boundary, proven at the real DB constraint | `AppointmentRepositoryIntegrationTest.databaseAllowsBackToBackAppointmentsForSameStylist` |

11 scenarios, exceeding the 8+ requirement, each boundary case named explicitly rather than only testing the happy path.

## 5+ unit tests, AAA structure

All test methods above follow Arrange/Act/Assert with explicit `// Arrange`, `// Act`, `// Assert` comments (see `TimeRangeOverlapTest.java` and `AppointmentServiceTest.java`). 9 unit tests total:
- 4 in `TimeRangeOverlapTest` (pure function, no mocks needed)
- 5 in `AppointmentServiceTest` (Mockito mocks for `AppointmentRepository`/`WorkingHoursRepository`)

Run (verified, container output):
```
docker run --rm -v "<repo>:/app" -v maven_repo:/root/.m2 -w /app maven:3.9-eclipse-temurin-21 \
    mvn -q -Dtest=AppointmentServiceTest,TimeRangeOverlapTest test
EXIT=0
```

## One full TDD cycle

Target: `TimeRangeOverlap.isOverlapping` — the function the overlap scenarios above depend on.

**Red.** The test file was written first, against a method that threw `UnsupportedOperationException`:
```java
public static boolean isOverlapping(LocalDateTime firstStart, LocalDateTime firstEnd,
                                     LocalDateTime secondStart, LocalDateTime secondEnd) {
    throw new UnsupportedOperationException("not implemented yet");
}
```
Actual failing run:
```
[ERROR] Tests run: 4, Failures: 0, Errors: 4, Skipped: 0, Time elapsed: 0.354 s <<< FAILURE! -- in com.salonbooking.service.TimeRangeOverlapTest
[ERROR] com.salonbooking.service.TimeRangeOverlapTest.returnsTrueWhenOneRangeFullyContainsTheOther -- Time elapsed: 0.137 s <<< ERROR!
java.lang.UnsupportedOperationException: not implemented yet
	at com.salonbooking.service.TimeRangeOverlap.isOverlapping(TimeRangeOverlap.java:12)
...
[ERROR] Tests run: 4, Failures: 0, Errors: 4, Skipped: 0
```

**Green.** Implemented the half-open-interval overlap check:
```java
return firstStart.isBefore(secondEnd) && secondStart.isBefore(firstEnd);
```
Re-run: exit code 0, all 4 tests pass.

**Refactor.** Added a class-level Javadoc comment documenting the half-open `[start, end)` convention (why back-to-back slots aren't an overlap) without changing behavior. Re-ran the same 4 tests: still exit code 0.

## One integration test, with a real database

`AppointmentRepositoryIntegrationTest` boots the full Spring context against the real `docker-compose` Postgres service (not a mock, not an in-memory substitute) and proves the `EXCLUDE` constraint in `schema.sql` — not just `AppointmentService`'s application-level check — rejects a double-booking `INSERT`. Actual captured output from the failing insert:
```
WARN  o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: 23P01
ERROR o.h.engine.jdbc.spi.SqlExceptionHelper   : ERROR: conflicting key value violates exclusion constraint "no_overlapping_active_appointments"
  Detail: Key (stylist_id, tsrange(start_time, end_time))=(4, ["2026-11-02 10:30:00","2026-11-02 11:30:00")) conflicts with existing key (stylist_id, tsrange(start_time, end_time))=(4, ["2026-11-02 10:00:00","2026-11-02 11:00:00")).
```
A second test in the same class (`databaseAllowsBackToBackAppointmentsForSameStylist`) proves the same constraint does *not* reject adjoining, non-overlapping slots. Both tests: exit code 0.

Run it yourself: `docker-compose up -d postgres` (from `backend/`), then run Maven against it, joined to the same Docker network, with `-DintegrationTest.dbHost=postgres` (see `.github/workflows/ci.yml` for the equivalent against `localhost` on a GitHub Actions runner).

## Coverage report

Generated by the Jacoco Maven plugin (bound to the `test` phase in `backend/pom.xml`) from the real run of all 11 tests (9 unit + 2 integration). Report: `backend/target/site/jacoco/index.html` and `backend/target/site/jacoco/jacoco.csv`.

**Overall: 99 / 166 lines covered (~60%).**

By package, line coverage is concentrated exactly where the business-critical logic lives:
- `com.salonbooking.service` (the booking/availability rules): `AppointmentService` 28/30 lines (93%), including 10/14 branches (71%) — the mocked unit tests exercise all four decision branches (overlap exists, outside working hours, DB race, ownership check). `TimeRangeOverlap` 12/12 lines (100%).
- `com.salonbooking.domain` (entities): well covered as a side effect of being exercised through the service and repository tests.
- `com.salonbooking.api` (controllers) and `com.salonbooking.api.dto`: **low to zero coverage** — see the gap below.

**One explicitly untested place:** the entire HTTP/controller layer (`AuthController`, `CustomerController`, `AppointmentController`) has no dedicated tests — no `MockMvc`/`WebTestClient` test exists. The 5 endpoints were verified manually (see `ARCHITECTURE.md`'s API table) but not under automated test. Rationale: this milestone prioritized proving the two hardest NFRs (NFR-03 overlap prevention, NFR-04 access control) genuinely, end-to-end through the database, over breadth across every layer. Controller tests (e.g. a `@WebMvcTest` per controller, asserting status codes and JSON shape) are the natural next addition before this module is considered done.

## Code review of another team

**Not produced here** — this requires an actual second team's repository or pull request, which doesn't exist for this solo project. This is flagged as a manual action item: once a peer team's code is available (in class or via a shared repo), review it for 3+ substantive comments following the same standard applied here (correctness, boundary cases, and whether their NFRs are actually enforced in code, not just asserted in a README).
