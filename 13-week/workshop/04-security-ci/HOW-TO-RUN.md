# HOW-TO-RUN: Security Pipeline & Negative Authorization Test

This guide documents the execution of the negative authorization security test for `educk-identity-api`, implementing the acceptance criteria of Security Backlog Story [#76](https://github.com/code-corhuila/educk-docs/issues/76) (STRIDE Threat I - BOLA / IDOR, Risk 9, P0).

## 1. Test Overview

- **Test Class:** `IdentityAuthorizationNegativeTest.java`
- **Component:** `educk-identity-api` (Port 8081)
- **Target Route:** `GET /api/v1/auth/users/{id}`
- **Security Rule Verified:**
  An authenticated student or parent caller attempting to query the user profile of another UUID without possessing `ROLE_ADMIN` must be rejected with HTTP `403 Forbidden` and an empty response body, preventing user enumeration and unauthorized data leakage.
- **Reference:** Tanenbaum & Van Steen (2008), Section 9.3: Protection Domains & Access Control Matrices [B].

## 2. Reproduction Steps

1. Copy `IdentityAuthorizationNegativeTest.java` into the test package:
   ```powershell
   Copy-Item "13-week/workshop/04-security-ci/IdentityAuthorizationNegativeTest.java" -Destination "educk-identity-api/src/test/java/com/corhuila/edutrack/identity/infrastructure/web/"
   ```

2. Run the test with Apache Maven and JDK 21:
   ```powershell
   cd educk-identity-api
   mvn test -Dtest=IdentityAuthorizationNegativeTest
   ```

## 3. Real Console Execution Output (recorded in CP8)

Executed on local environment: Windows 11, JDK 21 (Eclipse Adoptium 21.0.9), Apache Maven 3.9.9 against `educk-identity-api`:

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
[INFO] Recompiling the module because of added or removed source files.
[INFO] Compiling 26 source files with javac [debug parameters release 21] to target\classes
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ educk-identity-api ---
[INFO] skip non existing resourceDirectory C:\Users\Ximena Del Pilar\Music\educk-identity-api\src\test\resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ educk-identity-api ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 2 source files with javac [debug parameters release 21] to target\test-classes
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ educk-identity-api ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.corhuila.edutrack.identity.infrastructure.web.IdentityAuthorizationNegativeTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.066 s -- in com.corhuila.edutrack.identity.infrastructure.web.IdentityAuthorizationNegativeTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.947 s
[INFO] Finished at: 2026-10-30T14:44:54-05:00
[INFO] ------------------------------------------------------------------------
```

## 4. Pipeline Workflow Validation

The `security.yml` GitHub Actions pipeline was parsed and validated using standard YAML schema tools:
- Syntax: Valid YAML 1.2
- Trigger events: `push` and `pull_request` on `main` and `develop`
- Required jobs confirmed:
  1. `secret-scan` (Gitleaks action v2)
  2. `vulnerability-scan` (Trivy container & filesystem scanner)
  3. `negative-authorization-test` (Maven execution of `IdentityAuthorizationNegativeTest` on temurin-21)
