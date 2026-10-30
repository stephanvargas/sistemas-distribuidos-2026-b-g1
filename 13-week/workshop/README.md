# Week 13 Workshop - Threat Modeling & Security Backlog

Individual workshop for Distributed Systems 2026-B (Corte 3).

## Canonical Configuration

<!-- CONFIG-START -->
- STUDENT: Stephan Vargas Quiroga
- GITHUB_USER: stephanvargas
- TEAM: EduTrack
- BOUNDARY: TB2a (educk-api-gateway -> educk-identity-api, inbound HTTP)
- CLAIM_ISSUE: https://github.com/code-corhuila/educk-docs/issues/74
- STRIDE_PR: https://github.com/code-corhuila/educk-docs/pull/75
- REFLECTION_DOC: https://docs.google.com/document/d/1jH4xmZqfupyPUwrXD4eS8OTzg7g1L59ZPtq7kRdD5yY/edit?usp=sharing
<!-- CONFIG-END -->

## Directory Structure

```text
13-week/workshop/
├── README.md                 <- Canonical configuration and tracking (CP0)
├── 01-attack-surface.md      <- Attack surface map and asset inventory (CP1)
└── 04-security-ci/           <- Security gates and negative test (CP4, CP8)
    ├── security.yml
    ├── IdentityAuthorizationNegativeTest.java
    └── HOW-TO-RUN.md
```
