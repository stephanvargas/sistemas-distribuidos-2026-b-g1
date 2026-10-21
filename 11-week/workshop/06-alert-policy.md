# Multi-Window Multi-Burn-Rate Alerting Policy - EduTrack Platform (Attendance Domain)

## 1. Quantitative Budget Consumption Breakdown

Multi-window multi-burn-rate alerting eliminates static threshold noise by requiring both a long window (ensuring significant error budget consumption) and a short window (ensuring the incident is actively occurring).

With a 30-day (720 hours) availability SLO of **99.5%**, the error budget rate is 1 - 0.995 = 0.005.

| Alert Type | Burn Rate Multiplier | Long Window | Short Window | Budget Consumed Before Alert | Incident Time to Exhaust Budget | Severity / Channel |
|:---|:---:|:---:|:---:|:---:|:---:|:---|
| **Fast Burn (Critical)** | **14.4x** | 1 hour | 5 minutes | **2.0%** (21.6 failed requests) | 50.0 hours (~2.1 days) | PagerDuty / P0 Escalation |
| **Slow Burn (Warning)** | **6.0x** | 6 hours | 30 minutes | **5.0%** (54.0 failed requests) | 120.0 hours (5.0 days) | Slack Ticket / Daily Standup |

---

## 2. Anti-Pattern Guardrails: Cause-Based Alerts We DO NOT Create

To protect on-call attention and prevent alarm fatigue, EduTrack enforces symptom-based SLO alerting and forbids the following cause-based anti-patterns:

1. **Host/Container CPU Utilization Alert (`container_cpu_usage > 85%`):**
   - *Why Prohibited:* High CPU spikes during batch attendance roster parsing or bcrypt password hashing in the gateway do not indicate user failure. If roll-call submissions complete within 500ms, paging engineers for CPU causes alert fatigue.
   - *Proper Replacement:* Track user-facing latency p95 and HTTP 5xx error rate; CPU is reviewed as diagnostic telemetry during triage.

2. **JVM Heap Utilization Threshold Alert (`jvm_memory_used_bytes > 90%`):**
   - *Why Prohibited:* Spring Boot JVM runtimes deliberately defer major garbage collection until heap boundaries are reached. Alerting on raw memory generates constant false alarms during peak morning attendance hours without real OOM faults.
   - *Proper Replacement:* Track saturation using PostgreSQL connection pool wait queues (`attendance_connection_pool_active_ratio`) and GC pause duration (`jvm_gc_pause_seconds`).
