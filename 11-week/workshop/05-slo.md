# Service Level Objectives (SLOs) and Error Budget - EduTrack Platform (Attendance Domain)

## 1. SLO Definitions for Critical Journey (`educk-attendance-api`)

| Objective Name | SLI Metric & Ratio | PromQL Query (30-day Rolling Window) | Target | Window |
|:---|:---|:---|:---:|:---:|
| **Availability SLO** | Proportion of successful attendance record requests (non-5xx) over valid requests. | `sum(rate(attendance_records_total{status_code!~"5.."}[30d])) / sum(rate(attendance_records_total[30d]))` | **99.5%** | 30 days (rolling) |
| **Latency SLO** | Proportion of attendance submissions completed in <= 500ms. | `sum(rate(attendance_record_duration_seconds_bucket{le="0.5"}[30d])) / sum(rate(attendance_records_total{status_code="201"}[30d]))` | **95.0%** | 30 days (rolling) |
| **Absence Event Delivery SLO** | Proportion of absent student records successfully published as `StudentAbsentEvent` to RabbitMQ within 1s. | `sum(rate(attendance_absent_events_published_total[30d])) / sum(rate(attendance_absent_records_total[30d]))` | **99.9%** | 30 days (rolling) |

---

## 2. Traffic Estimation and Error Budget Calculations

- **Population & User Base:** EduTrack attendance module serves 1,200 enrolled students distributed across 40 classroom sections and 60 faculty teachers.
- **Daily Traffic Profile:** Roll-call is concentrated into 6 distinct classroom period sessions per day (predominantly morning opening 07:15–08:00 and midday 11:30–12:15).
- **Average Daily Volume:** 240 classroom sessions submitting batches of ~30 student status records = ~7,200 individual attendance submissions/validations per day.
- **Monthly Volume (30 days):** 7,200 * 30 = 216,000 valid requests.

### Mathematical Budget Breakdown:
1. **Availability Budget (99.5% SLO):**
   - Allowed failure margin: 100% - 99.5% = 0.5%.
   - **Request Budget:** 216,000 * 0.005 = **1,080 allowed failed requests** per 30-day window.
   - **Downtime Minutes:** 30 days * 24 h * 60 min = 43,200 minutes.
   - 43,200 * 0.005 = **216 minutes (3 hours 36 minutes)** of allowable downtime.

2. **Latency Budget (95.0% <= 500ms SLO):**
   - Allowed degraded margin: 100% - 95.0% = 5.0%.
   - **Request Budget:** 216,000 * 0.05 = **10,800 slow requests** allowable before breaching SLO.

---

## 3. Team Error Budget Policy

- **50% Consumed (Warning Threshold):**
  - Notification sent to Slack `#edutrack-ops` and email alert to Attendance domain lead.
  - Triage slow queries in PostgreSQL `educk-attendance-db` and examine vector clock serialization.
  - Background roster synchronization scheduled exclusively outside classroom hours.

- **75% Consumed (Degradation / Freeze Alert):**
  - Feature release freeze on `educk-attendance-api`.
  - Sprint capacity redirected to database indexing on `attendance_records` and RabbitMQ publisher-confirms reliability.
  - New deployments require explicit sign-off from Technical Lead.

- **100% Consumed (Full Freeze & Incident Review):**
  - Complete production deployment freeze (except critical security CVE patches).
  - Mandatory blameless postmortem within 48 hours.
  - On-call rotation tightened until rolling 30-day availability recovers above 99.5%.
