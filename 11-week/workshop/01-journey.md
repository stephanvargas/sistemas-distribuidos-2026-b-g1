# Critical Journey and Distributed Trace Map - EduTrack (Attendance Domain)

## 1. Critical User Journey Overview
- **Journey Name:** Student Attendance and Daily Absence Notification Journey
- **Description:** A teacher records classroom attendance via the attendance portal. The request traverses the ingress gateway with W3C trace context, executes causal consistency validation in the attendance microservice, persists session records in PostgreSQL, and dispatches an asynchronous StudentAbsentEvent through RabbitMQ to notify guardians.
- **Participating Components:**
  1. duck-attendance-portal (Frontend Client React SPA :3003)
  2. duck-api-gateway (Spring Cloud Gateway :8080)
  3. duck-attendance-api (Hexagonal Spring Boot 3 Core Service :8083)
  4. duck-attendance-db (PostgreSQL 16 isolated database :5433)
  5. RabbitMQ (Exchange topic dutrack.events :5672)
  6. duck-worker (Async event consumer and deduplicator)

---

## 2. Distributed Hop Table

| Hop | Caller | Receiver | Protocol | Operation / Endpoint | Sync/Async | Data Exchanged |
|:---:|:---|:---|:---:|:---|:---:|:---|
| 1 | duck-attendance-portal | duck-api-gateway | HTTP / REST | POST /api/v1/attendance/sessions/SES-102/records | Sync | JSON attendance batch: studentId, status, timestamp |
| 2 | duck-api-gateway | duck-attendance-api | HTTP / REST | POST /api/v1/attendance/sessions/SES-102/records | Sync | Request payload + injected W3C 	raceparent header |
| 3 | duck-attendance-api | duck-attendance-db | TCP / Postgres | INSERT INTO attendance_records | Sync | SQL insert with causal sequence and student attendance state |
| 4 | duck-attendance-api | RabbitMQ | AMQP 0-9-1 | Publish dutrack.events | Async | StudentAbsentEvent payload + 	raceparent in message headers |
| 5 | duck-worker | RabbitMQ | AMQP 0-9-1 | Consume duck.attendance.notifications | Async | Consumes event, extracts traceparent, deduplicates by eventId |

---

## 3. Sequence Diagram with Trace Context Propagation

`mermaid
sequenceDiagram
    autonumber
    participant Portal as educk-attendance-portal
    participant GW as educk-api-gateway (:8080)
    participant Att as educk-attendance-api (:8083)
    participant DB as educk-attendance-db (:5433)
    participant MQ as RabbitMQ (edutrack.events)
    participant Worker as educk-worker

    Portal->>GW: POST /api/v1/attendance/sessions/SES-102/records
    Note over GW: Generates W3C traceparent header<br/>trace_id: 7c10b4288e224e73b2901a18bc0094ef<br/>span_id: 11a056bb0ca103c8
    GW->>Att: POST /api/v1/attendance/... (traceparent: 00-7c10b4288e224e73b2901a18bc0094ef-11a056bb0ca103c8-01)
    Note over Att: Creates span 6d95b97c4e88a023<br/>Validates session status and student roster
    Att->>DB: INSERT INTO attendance_records (DB span 82f4bc39e10d5401)
    DB-->>Att: 201 Created (records inserted)
    Att-)MQ: StudentAbsentEvent (traceparent injected into AMQP message headers)
    Att-->>GW: 201 Created (HTTP response with traceparent header)
    GW-->>Portal: 201 Created (HTTP response with traceparent header)
    MQ-)Worker: Deliver StudentAbsentEvent (reads traceparent from headers)
    Note over Worker: Spawns async consumer span f1207ab48d0112cd<br/>Shares exact trace_id 7c10b4288e224e73b2901a18bc0094ef
`

---

## 4. Distributed Span Tree Hierarchy

- **Root Span:** duck-api-gateway: HTTP POST /api/v1/attendance/sessions/SES-102/records
  - 	race_id: 7c10b4288e224e73b2901a18bc0094ef
  - span_id: 11a056bb0ca103c8
  - parent_span_id: 
ull (ingress root)
  - **Child Span 1:** duck-attendance-api: HTTP POST /api/v1/attendance/sessions/SES-102/records
    - 	race_id: 7c10b4288e224e73b2901a18bc0094ef
    - span_id: 6d95b97c4e88a023
    - parent_span_id: 11a056bb0ca103c8
    - **Child Span 1.1:** duck-attendance-api: DB INSERT attendance_records
      - 	race_id: 7c10b4288e224e73b2901a18bc0094ef
      - span_id: 82f4bc39e10d5401
      - parent_span_id: 6d95b97c4e88a023
    - **Child Span 1.2:** duck-attendance-api: AMQP publish StudentAbsentEvent
      - 	race_id: 7c10b4288e224e73b2901a18bc0094ef
      - span_id: 4e91ca82bb4560fb
      - parent_span_id: 6d95b97c4e88a023
  - **Child Span 2 (Asynchronous Boundary Crossing):** duck-worker: AMQP consume StudentAbsentEvent
    - 	race_id: 7c10b4288e224e73b2901a18bc0094ef
    - span_id: 1207ab48d0112cd
    - parent_span_id: 4e91ca82bb4560fb (propagated via AMQP header)
