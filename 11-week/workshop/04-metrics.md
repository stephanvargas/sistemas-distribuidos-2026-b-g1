# Golden Signals and Metric Catalog - EduTrack Platform (Attendance Domain)

## 1. Golden Signals Specification (`educk-attendance-api`)

| Signal | Metric Name | Type | Labels | PromQL Query (Dashboard / Alerting) |
|:---|:---|:---:|:---|:---|
| **Latency** | `attendance_record_duration_seconds` | Histogram | `route`, `method`, `status_code` | `histogram_quantile(0.95, sum by (le) (rate(attendance_record_duration_seconds_bucket[5m])))` |
| **Traffic** | `attendance_records_total` | Counter | `route`, `method` | `sum(rate(attendance_records_total[5m]))` |
| **Errors** | `attendance_records_failed_total` | Counter | `route`, `error_type`, `status_code` | `sum(rate(attendance_records_failed_total{status_code=~"5.."}[5m])) / sum(rate(attendance_records_total[5m]))` |
| **Saturation** | `attendance_connection_pool_active_ratio` | Gauge | `pool_id`, `state` | `sum(attendance_connection_pool_active) / sum(attendance_connection_pool_max)` |

---

## 2. Cardinality Analysis and Anti-Pattern Guardrails

| Unbounded Label (Forbidden) | Cardinality Risk | Bounded Replacement Label | Max Expected Cardinality |
|:---|:---|:---|:---:|
| `student_id` | Thousands of students -> high memory explosion | `attendance_status` (`PRESENT`, `ABSENT`, `LATE`) | 4 |
| Raw URL (`/api/v1/attendance/sessions/SES-102`) | Unbounded path parameters | Normalized `route` (`/api/v1/attendance/sessions/{id}`) | 6 distinct routes |
| Student National ID | Critical PII violation & high cardinality | Replaced by `channel_type` (`WEB_PORTAL`, `BATCH_IMPORT`) | 2 |
| Exception Stack Message | Arbitrary strings unbounded cardinality | Categorized `error_type` (`SESSION_CLOSED`, `DB_TIMEOUT`, `AMQP_UNAVAILABLE`) | 5 |

---

## 3. Latency Histogram Buckets and Justification

- **Chosen Buckets (seconds):** `[0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0]`
- **User Experience Justification:**
  - `0.05s - 0.25s (50ms - 250ms)`: Real-time UI responsiveness expected by classroom teachers submitting roll-call during attendance periods.
  - `0.5s (500ms)`: Target p95 objective for validating session roster and recording batch status in PostgreSQL.
  - `1.0s (1000ms)`: Warning threshold for noticeable teacher wait time in classroom environments.
  - `2.5s - 5.0s`: Saturation indicator; signals connection pool contention or broker backpressure prior to client-side timeout at 5000ms.
