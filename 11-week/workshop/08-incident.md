# Incident Simulation and Blameless Postmortem - EduTrack (Attendance Domain)

## 1. Incident Scenario Description
During morning period 1 roll-call (07:15–07:55), `educk-attendance-db` suffered extreme disk write latency and lock contention on the `attendance_records` table. Consequently, downstream database insertions from `educk-attendance-api` responded with a **3.0-second delay for 25% of requests across a 40-minute duration**.

---

## 2. Distributed Trace of a Slow Request (`trace_id: 7c10b4288e224e73b2901a18bc0094ef`)

```text
[Root Span] educk-api-gateway: POST /api/v1/attendance/sessions/SES-102/records (3,048ms)
│
├── [Child Span 1] educk-attendance-api: Record Attendance Command (3,041ms)
│   │
│   ├── [Child Span 1.1] educk-attendance-db: INSERT INTO attendance_records (3,015ms) ⚠️ DB LATENCY
│   │
│   └── [Child Span 1.2] educk-attendance-api: AMQP publish StudentAbsentEvent (14ms)
│
└── [Child Span 2] educk-worker: AMQP consume StudentAbsentEvent (12ms)
```

---

## 3. Four Correlated Structured Logs (Format CP2)

```json
{"timestamp":"2026-10-21T07:22:01.100Z","level":"INFO","service":"educk-api-gateway","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"11a056bb0ca103c8","correlation_id":"corr-att-55102","event":"http.request.routed","message":"Ingress attendance submission routed to attendance api","duration_ms":3,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-21T07:22:01.105Z","level":"WARN","service":"educk-attendance-api","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"6d95b97c4e88a023","correlation_id":"corr-att-55102","event":"db.connection.slow","message":"Database response exceeded 1000ms latency threshold","duration_ms":1035,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-21T07:22:04.120Z","level":"INFO","service":"educk-attendance-api","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"82f4bc39e10d5401","correlation_id":"corr-att-55102","event":"db.insert.completed","message":"Attendance record persisted after connection pool delay","duration_ms":3015,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-21T07:22:04.135Z","level":"INFO","service":"educk-attendance-api","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"4e91ca82bb4560fb","correlation_id":"corr-att-55102","event":"amqp.event.published","message":"StudentAbsentEvent published to edutrack.events","duration_ms":14,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
```

---

## 4. SLI Impact and Alert Trigger Timeline

1. **SLI Impact:**
   - Total classroom roll-call submissions during the 40-minute window: 216 requests.
   - Affected requests (25% delayed by 3.0s > 500ms): **54 requests**.
   - Latency SLI (p95 <= 500ms) drops from **98.6% to 75.0%**, breaching the 95.0% SLO target.
2. **Alert Trigger:**
   - At **Minute 7**, `AttendanceApiAvailabilityFastBurn` triggered as sustained degradation passed the 5-minute confirmation window.
3. **Runbook Mitigation Executed:**
   - At **Minute 13**, on-call executed Runbook **Step 3 (Mitigation 1)**, enabling buffer queue mode in RabbitMQ and expanding connection pool size, restoring p95 latency to 160ms.

---

## 5. Blameless Postmortem Report

- **Date:** 2026-10-21
- **Severity:** P1 (Attendance Roll-Call Latency Degradation)
- **Impact:** 54 classroom teachers experienced 3-second UI latency; 0.50% of monthly error budget consumed; 0 attendance records lost.
- **Root Cause:** Row-level locks and table scans on `attendance_records` caused by missing foreign key composite indexing on `(session_id, recorded_at)`.
- **Contributing Factors:** Concurrent school-wide submission spike during the 07:15 AM period opening.

### Remediation Action Items (User Stories):
1. **HU-ATT-OPT1 (Composite Indexing on Attendance Records):**
   *As an* engineer, *I want* a Flyway migration adding composite index on `attendance_records (session_id, student_id)` *so that* batch roll-call validation performs index scans under 30ms.
   *Acceptance Criteria:* `EXPLAIN ANALYZE` confirms index scan; execution duration < 25ms.

2. **HU-ATT-RES2 (Non-Blocking Buffer Queue Fallback):**
   *As a* teacher, *I want* attendance batches queued in RabbitMQ if PostgreSQL exceeds 1,500ms *so that* roll-call never blocks classroom instruction.
   *Acceptance Criteria:* Under artificial 3s database lag, API returns HTTP 201 and delegates persistence asynchronously.

3. **HU-ATT-MON3 (PostgreSQL Lock Contention Metric):**
   *As an* on-call engineer, *I want* Prometheus metrics tracking active row locks *so that* lock contention is detected prior to user latency breach.
   *Acceptance Criteria:* `attendance_db_active_locks` exported and mapped to alerting dashboard.
