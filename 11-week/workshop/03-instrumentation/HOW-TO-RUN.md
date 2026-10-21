# How To Run Instrumentation and Verification Tests (Attendance API)

## Prerequisites
- Node.js (v20+ or v22+ LTS). Zero external dependencies.

## Commands
```bash
cd 11-week/workshop/03-instrumentation
node --test test.js
```

## Console Execution Output
```text
TAP version 13
# Subtest: Attendance Observability - trace context extraction and propagation
ok 1 - Attendance Observability - trace context extraction and propagation
# Subtest: Attendance Observability - structured logger output matches schema
ok 2 - Attendance Observability - structured logger output matches schema
# Subtest: Attendance Observability - metrics registry exposes Prometheus output
ok 3 - Attendance Observability - metrics registry exposes Prometheus output
1..3
# tests 3 | pass 3 | fail 0
```
