# Structured Log Contract - EduTrack Platform (Attendance Domain)

## 1. Standard and Domain Schema Specification

| Field | Type | Requirement | Example | Description / Rationale |
|:---|:---|:---:|:---|:---|
| 	imestamp | string (ISO-8601 UTC) | Required | 2026-10-20T21:05:01.210Z | Standardized UTC timestamp for timeline alignment. |
| level | string (enum) | Required | INFO | Severity classification (DEBUG, INFO, WARN, ERROR). |
| service | string | Required | duck-attendance-api | Identifies attendance microservice source. |
| ersion | string | Required | 2.0.0 | Semantic version deployed to isolate release defects. |
| nvironment | string | Required | qa | Runtime stage (development, qa, production). |
| 	race_id | string (hex-32) | Required | 7c10b4288e224e73b2901a18bc0094ef | Shared W3C trace ID across all components of the call. |
| span_id | string (hex-16) | Required | 6d95b97c4e88a023 | Active execution segment identifier. |
| correlation_id | string | Required | corr-att-55102 | Gateway correlation token passed down the pipeline. |
| vent | string | Required | ttendance.record.initiated | Dot-notation machine indexable tag for ELK/Loki. |
| message | string | Required | Processing student attendance session submission | Diagnostic message for engineers. |
| duration_ms | number | Required | 38 | Segment execution duration in milliseconds. |
| student_id | string (UUID) | Required | s-8812-diego-rojas | EduTrack domain: student subject of attendance record. |
| session_id | string (UUID) | Required | ses-102-math-period1 | EduTrack domain: classroom period session identifier. |
| course_id | string (UUID) | Required | c-901-algebra-grade9 | EduTrack domain: course curricular offering identifier. |
| ttendance_status | string (enum) | Required | ABSENT | Attendance state (PRESENT, ABSENT, LATE, EXCUSED). |

---

## 2. Sensitive Data Policy (Never Log & Masking Rules)

| Data Category | Risk & Compliance | Masking / Redaction Policy | Example Before -> After |
|:---|:---|:---|:---|
| **Credentials & Hashes** | System Breach | Full suppression | secret123 -> [REDACTED] |
| **JWT Bearer Tokens** | Identity Theft | Prefix with token redaction | yJhbGciOi... -> yJh...[MASKED] |
| **Medical / Excuse Notes** | FERPA / Health Privacy | Omit diagnosis; log presence boolean | "Doctor note: asthma crisis" -> has_excuse: true |
| **Student National IDs** | Minor PII Protection | Masking to last 4 characters | 1082910481 -> ******0481 |

---

## 3. Seven Trace-Correlated JSON Log Samples (	race_id: 7c10b4288e224e73b2901a18bc0094ef)

`json
{"timestamp":"2026-10-20T21:05:01.010Z","level":"INFO","service":"educk-api-gateway","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"11a056bb0ca103c8","correlation_id":"corr-att-55102","event":"http.request.received","message":"Ingress attendance submission received","duration_ms":2,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-20T21:05:01.015Z","level":"INFO","service":"educk-api-gateway","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"11a056bb0ca103c8","correlation_id":"corr-att-55102","event":"http.request.routed","message":"Routing upstream to educk-attendance-api","duration_ms":5,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-20T21:05:01.042Z","level":"WARN","service":"educk-attendance-api","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"6d95b97c4e88a023","correlation_id":"corr-att-55102","event":"business.rule.rejection","message":"Rejection: Classroom session is already locked and archived","duration_ms":27,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-20T21:05:01.085Z","level":"INFO","service":"educk-attendance-api","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"6d95b97c4e88a023","correlation_id":"corr-att-55102","event":"domain.validation.success","message":"Active session confirmed; persisting attendance entry","duration_ms":35,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-20T21:05:04.095Z","level":"ERROR","service":"educk-attendance-api","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"82f4bc39e10d5401","correlation_id":"corr-att-55102","event":"downstream.database.timeout","message":"PostgreSQL educk-attendance-db timed out after 3000ms","duration_ms":3004,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-20T21:05:04.140Z","level":"INFO","service":"educk-attendance-api","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"82f4bc39e10d5401","correlation_id":"corr-att-55102","event":"database.insert.success","message":"Attendance record persisted after connection pool recovery","duration_ms":41,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
{"timestamp":"2026-10-20T21:05:04.170Z","level":"INFO","service":"educk-attendance-api","version":"2.0.0","environment":"qa","trace_id":"7c10b4288e224e73b2901a18bc0094ef","span_id":"4e91ca82bb4560fb","correlation_id":"corr-att-55102","event":"amqp.event.published","message":"StudentAbsentEvent published to edutrack.events","duration_ms":19,"student_id":"s-8812-diego-rojas","session_id":"ses-102-math-period1","course_id":"c-901-algebra-grade9","attendance_status":"ABSENT"}
`

---

## 4. Log Level Usage Criteria

- ERROR: System faults preventing state persistence (e.g. PostgreSQL pool exhaustion, broker channel error).
- WARN: Handled anomalies or domain rejections (e.g. attempting to submit attendance on an archived class session).
- INFO: Milestone operations (e.g. attendance saved, StudentAbsentEvent published to RabbitMQ).
- DEBUG: Low-level diagnostic details (e.g. vector clock updates, raw query plans). Disabled in QA/Prod.
