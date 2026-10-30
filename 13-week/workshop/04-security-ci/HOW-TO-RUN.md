# HOW-TO-RUN: Identity Authorization Negative Security Test & CI Gates

This guide documents the execution and verification of the negative security test suite for `educk-identity-api`, satisfying the automated acceptance criteria of Security Backlog Story [#76](https://github.com/code-corhuila/educk-docs/issues/76) and mitigating STRIDE Threat I (Information Disclosure / BOLA / IDOR, Risk Score 9, P0).

## 1. Scope & System Under Test

- **Test Suite Class:** `IdentityAuthorizationNegativeTest.java`
- **Target Microservice:** `educk-identity-api` (Spring Boot 3, Port 8081)
- **Target Endpoint:** `GET /api/v1/auth/users/{id}`
- **Security Rule Enforced:**
  Implements the protection domain and access control matrix model from Tanenbaum & Van Steen (2008), Section 9.3 [B]. The authorization rule strictly forbids any authenticated caller (such as `ROLE_STUDENT`) from inspecting user profiles belonging to other individuals unless the caller is the resource owner (`actorId == targetResourceId`) or holds administrative privileges (`ROLE_ADMIN`), rejecting violations with HTTP `403 Forbidden` (`AccessDeniedException`).

## 2. Local Reproduction Steps

1. Place the test class inside the test source directory:
   ```powershell
   Copy-Item "13-week\workshop\04-security-ci\IdentityAuthorizationNegativeTest.java" `
     -Destination "C:\Users\Ximena Del Pilar\Music\educk-identity-api\src\test\java\com\corhuila\edutrack\identity\infrastructure\web\"
   ```

2. Execute the test using Apache Maven with JDK 21:
   ```powershell
   cd "C:\Users\Ximena Del Pilar\Music\educk-identity-api"
   & "C:\Users\Ximena Del Pilar\Music\maven\apache-maven-3.9.9\bin\mvn.cmd" test -Dtest=IdentityAuthorizationNegativeTest
   ```

## 3. Real Console Execution Output (CP8 Evidence)

Recorded on Windows 11 host using Eclipse Adoptium JDK 21.0.9 and Apache Maven 3.9.9:

```text
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------< com.corhuila.edutrack:educk-identity-api >--------------
[INFO] Building educk-identity-api 1.0.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ educk-identity-api ---
[INFO] Copying 1 resource from src\main\resources to target\classes
[INFO] Copying 0 resource from src\main\resources to target\classes
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ educk-identity-api ---
[INFO] Nothing to compile - all classes are up to date.
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ educk-identity-api ---
[INFO] skip non existing resourceDirectory C:\Users\Ximena Del Pilar\Music\educk-identity-api\src\test\resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ educk-identity-api ---
[INFO] Nothing to compile - all classes are up to date.
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ educk-identity-api ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.corhuila.edutrack.identity.infrastructure.web.IdentityAuthorizationNegativeTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.062 s -- in com.corhuila.edutrack.identity.infrastructure.web.IdentityAuthorizationNegativeTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.688 s
[INFO] Finished at: 2026-10-30T15:01:24-05:00
[INFO] ------------------------------------------------------------------------
```

## 4. Pipeline Workflow Validation (security.yml)

The GitHub Actions workflow definition `security.yml` was parsed and verified:
- YAML Syntax: Fully conforming to YAML 1.2 specifications.
- Trigger Events: Automated execution on `push` and `pull_request` targeting `main` and `develop`.
- Security Gates:
  1. `detect-credentials-and-secrets`: Scans complete git commit history using Gitleaks action v2 to intercept hardcoded tokens and private keys.
  2. `identity-sast-and-sca`: Evaluates project filesystem and dependencies against CVE databases using Trivy with severity threshold `HIGH,CRITICAL`.
  3. `verify-authorization-boundaries`: Runs the automated negative authorization regression test in JDK 21 to prevent BOLA/IDOR vulnerabilities from entering staging or production.
