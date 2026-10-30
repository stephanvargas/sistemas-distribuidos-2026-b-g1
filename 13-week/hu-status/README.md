<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       13-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 13

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Stephan Vargas Quiroga
- GITHUB_USER: stephanvargas
- TEAM: EduTrack
- SPRINT_GOAL: Threat Modeling, Trust Boundary STRIDE (TB2a: Gateway to Identity API), Security Backlog Stories, Security CI Gates, Negative Authorization Testing, and Implementation Plan
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| CP0 | Workshop Setup and Boundary Claim | done | https://github.com/code-corhuila/educk-docs/issues/74 |
| CP1 | Attack Surface Map and Asset Inventory | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/blob/main/13-week/workshop/01-attack-surface.md |
| CP2 | STRIDE Analysis Pull Request (educk-docs) | done | https://github.com/code-corhuila/educk-docs/pull/75 |
| CP3 | Security Backlog Stories (educk-docs) | done | https://github.com/code-corhuila/educk-docs/issues/76 |
| CP4 | Security Pipeline Gates and Negative Test | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/tree/main/13-week/workshop/04-security-ci |
| CP5 | Delivery 1 Closure and Peer Review | done | https://github.com/code-corhuila/educk-docs/pull/65#issuecomment-6532010857 |
| CP6 | Address Peer Review and Finalize STRIDE | done | https://github.com/code-corhuila/educk-docs/pull/75#issuecomment-6532022185 |
| CP7 | Refine Security Backlog Stories | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/commit/ed1c7fb |
| CP8 | Execute Negative Authorization Test and Validate CI | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/blob/main/13-week/workshop/04-security-ci/HOW-TO-RUN.md |
| CP9 | Implementation Plan and Workshop Closure | done | https://docs.google.com/document/d/1jH4xmZqfupyPUwrXD4eS8OTzg7g1L59ZPtq7kRdD5yY/edit?usp=sharing |
| CP10 | Delivery 2 Final Submission and State Closure | done | https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/tree/main/13-week/hu-status |

## 2. My individual contribution
- Claimed and documented Trust Boundary TB2a (`educk-api-gateway` -> `educk-identity-api`, inbound HTTP port 8081) on `code-corhuila/educk-docs` (Issue #74).
- Modeled the system attack surface in Mermaid, classifying 7 primary assets across the Identity & Access domain and cataloging entry points.
- Developed an in-depth STRIDE matrix for TB2a covering spoofing, role tampering, repudiation, BOLA/IDOR, BCrypt CPU exhaustion, and privilege escalation, grounded in Tanenbaum & Van Steen (2008), Chapter 9 [B].
- Authored three prioritized security user stories with automated acceptance criteria: #76 (P0 - IDOR prevention), #77 (P1 - Gateway isolation & mutual auth), and #78 (P1 - Strict RBAC).
- Designed GitHub Actions security workflow (`security.yml`) incorporating Gitleaks, Trivy SCA, and negative authorization tests.
- Implemented and validated negative authorization unit test `IdentityAuthorizationNegativeTest` asserting 403 Forbidden with empty body on unauthorized user profile reads.
- Executed technical peer review on Attendance API PR #65 and addressed incoming peer review feedback from @XimenaChala on PR #75.
- Executed negative authorization test against `educk-identity-api` using Apache Maven 3.9.9 and Eclipse Adoptium JDK 21, recording real console execution output (3 tests run, 0 failures).
- Authored the comprehensive final implementation plan and reflective learning synthesis covering CP0 through CP9.

## 3. Blockers and risks
- Microservices on the internal docker network implicitly trust caller headers under ADR-006; rogue internal components could bypass edge token checks. Mitigated by designing network boundary isolation and service-to-service HMAC validation.
- BCrypt password hashing (work factor 12) introduces CPU exhaustion vulnerability under concurrent brute-force floods; mitigated via gateway token bucket throttling.

## 4. Plan for next week
- Integrate the object ownership security guard into the production controller of `educk-identity-api`, provision Docker bridge network isolation for port 8081, and configure secret rotation.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links
- Fork repository: https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1
- Workshop directory: https://github.com/stephanvargas/sistemas-distribuidos-2026-b-g1/tree/main/13-week/workshop
- Claim issue: https://github.com/code-corhuila/educk-docs/issues/74
- STRIDE Pull Request: https://github.com/code-corhuila/educk-docs/pull/75
- Security Stories: https://github.com/code-corhuila/educk-docs/issues/76, https://github.com/code-corhuila/educk-docs/issues/77, https://github.com/code-corhuila/educk-docs/issues/78
- Peer review given: https://github.com/code-corhuila/educk-docs/pull/65#issuecomment-6532010857
- Peer review received & addressed: https://github.com/code-corhuila/educk-docs/pull/75#issuecomment-6532022185
- Reflection document: https://docs.google.com/document/d/1jH4xmZqfupyPUwrXD4eS8OTzg7g1L59ZPtq7kRdD5yY/edit?usp=sharing
