# Guía de Ejecución y Evidencia CI: Identity Authorization Negative Test

Esta guía documenta la verificación de seguridad para el microservicio `educk-identity-api`, validando los criterios de aceptación de la Historia [#76](https://github.com/code-corhuila/educk-docs/issues/76) y mitigando el vector de riesgo STRIDE I (Information Disclosure / BOLA / IDOR, Riesgo 9, P0).

## 1. Alcance y Componentes Bajo Prueba

- **Clase Evaluada:** `IdentityAuthorizationNegativeTest.java`
- **Módulo Afectado:** `educk-identity-api` (Spring Boot 3, puerto 8081)
- **Ruta Objetivo:** `GET /api/v1/auth/users/{id}`
- **Principio de Autorización:**
  Aplica el modelo de matrices de control de acceso y dominios de protección de Tanenbaum & Van Steen (2008), Sección 9.3 [B]. La regla prohíbe que cualquier sujeto autenticado (por ejemplo con rol `ROLE_STUDENT`) extraiga perfiles de terceros a menos que sea el dueño del recurso (`actorId == targetResourceId`) o posea credenciales de administración (`ROLE_ADMIN`), respondiendo con denegación estricta (`403 Forbidden`).

## 2. Instrucciones de Reproducción Local

1. Ubicar la clase de prueba dentro del árbol de fuentes del servicio:
   ```powershell
   Copy-Item "13-week\workshop\04-security-ci\IdentityAuthorizationNegativeTest.java" `
     -Destination "C:\Users\Ximena Del Pilar\Music\educk-identity-api\src\test\java\com\corhuila\edutrack\identity\infrastructure\web\"
   ```

2. Ejecutar la suite mediante Apache Maven y JDK 21:
   ```powershell
   cd "C:\Users\Ximena Del Pilar\Music\educk-identity-api"
   & "C:\Users\Ximena Del Pilar\Music\maven\apache-maven-3.9.9\bin\mvn.cmd" test -Dtest=IdentityAuthorizationNegativeTest
   ```

## 3. Evidencia Real de Ejecución en Consola (CP8)

Salida registrada en entorno Windows 11 con Eclipse Adoptium JDK 21.0.9 y Apache Maven 3.9.9:

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

## 4. Validación del Pipeline de Seguridad (security.yml)

El archivo de workflow `security.yml` fue analizado sintáctica y estructuralmente:
- Validación YAML: 100% válida sin errores de indentación.
- Disparadores: Eventos `push` y `pull_request` sobre `main` y `develop`.
- Compuertas activadas:
  1. `detect-credentials-and-secrets`: Detección preventiva de credenciales con Gitleaks.
  2. `identity-sast-and-sca`: Escaneo de vulnerabilidades en dependencias y sistema de archivos con Trivy.
  3. `verify-authorization-boundaries`: Compilación y ejecución forzosa de la prueba de autorización negativa en Maven.
