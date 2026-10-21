<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       11-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 11

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Stephan Vargas Quiroga
- GITHUB_USER: stephanvargas
- TEAM: EduTrack
- SPRINT_GOAL: Observability, Distributed Tracing, Structured Logging, and Golden Signals Instrumentation (Week 11 - Delivery 1)
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| CP0 | Workshop Setup and Canonical Configuration | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/commit/57c389d |
| CP1 | Critical Journey and Distributed Trace Map | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/commit/c275bc2 |
| CP2 | Structured Log Contract and Masking Rules | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/commit/05ea974 |
| CP3 | Instrumentation Middleware, Downstream Call and Metrics | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/commit/45d6ce0 |
| CP4 | Golden Signals and Prometheus Metric Catalog | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/commit/0f7ba04 |
| CP5 | Delivery 1 Closure and Weekly Status Update | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/tree/main/11-week/hu-status |

## 2. My individual contribution
- Mapped end-to-end critical journey for daily attendance recording and absence notifications across 5 EduTrack components.
- Established W3C trace context propagation across HTTP gateway and asynchronous RabbitMQ StudentAbsentEvent publishing.
- Defined uniform structured JSON log contract with 11 standard fields, 4 attendance domain attributes, and sensitive data redaction.
- Implemented and unit-tested trace middleware, structured logger, and downstream context forwarder for educk-attendance-api.
- Cataloged the 4 golden signals with Prometheus queries and cardinality controls.

## 3. Blockers and risks
- Avoided high cardinality memory leaks in Prometheus by removing raw student IDs and path parameters.

## 4. Plan for next week
- Complete Delivery 2 (CP6 to CP10): SLOs, error budgets, burn rate alerts, runbook, and drill postmortem.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Fork repository: https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1
- Workshop directory: https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/tree/main/11-week/workshop
- Reflection document: https://docs.google.com/document/d/1CNezoc0lzOeu6_LEtQOQCLD8kh6mqn-N_UvBabDvU4I/edit

