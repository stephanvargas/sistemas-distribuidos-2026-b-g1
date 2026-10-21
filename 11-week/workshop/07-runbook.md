# Incident Runbook: Attendance API Fast-Burn Alert

## 1. Alert Overview and Symptoms
- **Triggering Alert:** `AttendanceApiAvailabilityFastBurn`
- **Condition:** Availability burn rate exceeds **14.4x** across 1-hour and 5-minute windows.
- **Service:** `educk-attendance-api` (:8083)
- **User Symptoms:** Teachers attempting roll-call in `educk-attendance-portal` encounter HTTP 500/504 errors or submission timeouts. Parents report not receiving absence notification alerts for period 1.

---

## 2. Immediate Diagnostic Triage (Minutes 0 - 5)

### Step 2.1: Confirm Real Impact via PromQL
Execute in Prometheus / Grafana explore console:
```promql
sum(rate(attendance_records_failed_total{status_code=~"5.."}[5m])) / sum(rate(attendance_records_total[5m]))
```
- If the error ratio exceeds 7.2% (14.4 * 0.005), the incident is confirmed active and critical.

### Step 2.2: Extract Causality via Distributed Trace Log Query
Filter logs in Grafana Loki or CLI:
```bash
logcli query '{service="educk-attendance-api", environment="qa"} |= "ERROR"' --limit=5
```
Extract the offending `trace_id` and query across all microservices:
```bash
logcli query '{environment="qa"} |= "7c10b4288e224e73b2901a18bc0094ef"'
```

---

## 3. Mitigations Ordered by Risk Level

### Mitigation 1 (Lowest Risk): Enable Offline Queue Fallback via Feature Flag
- **Action:** If `educk-attendance-db` is locked or congested, switch API to buffer attendance batches into RabbitMQ dead-letter queues for asynchronous persistence.
- **Command:**
  ```bash
  curl -X POST http://localhost:8083/api/v1/management/features/offline-buffer -d '{"enabled":true}'
  ```
- **Risk:** Minimal. Eliminates teacher submission failures while safely queueing records for database insertion.

### Mitigation 2 (Medium Risk): Scale Service Instances & Expand DB Pool
- **Action:** If traffic peaked during school morning entry, increase service replicas and temporarily scale PostgreSQL connection pool.
- **Command:**
  ```bash
  docker compose scale educk-attendance-api=3
  ```
- **Risk:** Moderate. Increases database connection load; monitor PostgreSQL `max_connections`.

### Mitigation 3 (High Risk): Immediate Deployment Rollback
- **Action:** If the incident began immediately following a release, revert to the last stable release tag (`v2.0.0-stable`).
- **Command:**
  ```bash
  git checkout tags/v2.0.0-stable && docker compose up -d --build educk-attendance-api
  ```
- **Risk:** High. Reverts any database schema changes introduced in the latest migration.

---

## 4. Escalation Matrix

| Window | Role | Assigned Engineer | Channel | Responsibility |
|:---|:---|:---|:---|:---|
| **0 - 10 min** | Primary On-Call | Stephan Vargas Quiroga | `#incident-attendance` | Initial triage, diagnosis, and mitigation execution. |
| **10 - 20 min** | Database / Infra Lead | Diego Rojas / Tech Staff | Phone / Direct Call | PostgreSQL locks, connection pool starvation, broker backlog. |
| **> 20 min** | Academic Incident Manager | Academic Coordinator | Institutional Call | Coordinate with school principals and classroom educators. |

---

## 5. Verification of Service Recovery
1. Error rate PromQL query drops below 0.5%:
   ```promql
   sum(rate(attendance_records_failed_total{status_code=~"5.."}[5m])) / sum(rate(attendance_records_total[5m])) < 0.005
   ```
2. RabbitMQ `educk.attendance.notifications` queue depth returns to 0.
3. Smoke test verification:
   ```bash
   curl -I -X POST http://localhost:8083/api/v1/attendance/sessions/SES-102/records -H "Content-Type: application/json" -d '{"test":true}'
   ```
   Must return `HTTP 201 Created` with valid `traceparent` header.
