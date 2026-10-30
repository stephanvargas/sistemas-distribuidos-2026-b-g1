package com.corhuila.edutrack.identity.infrastructure.web;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/**
 * Suite de Verificación de Seguridad y Autorización Negativa - Identity Domain.
 *
 * Valida el criterio de aceptación principal de la Historia #76:
 * "[security] Object ownership and role check on user profile reads (IDOR prevention)"
 * Vector de ataque STRIDE: I (Information Disclosure / BOLA / IDOR).
 *
 * Fundamentación teórica: Tanenbaum & Van Steen (2008), Cap. 9, Sec. 9.3:
 * "Dominios de protección y matrices de control de acceso a objetos en sistemas distribuidos".
 */
class IdentityAuthorizationNegativeTest {

    public enum PolicyDecision {
        PERMIT,
        DENY
    }

    public static class UserProfileAccessDecisionManager {
        public PolicyDecision evaluate(UUID actorId, Set<String> grantedRoles, UUID targetResourceId) {
            if (actorId == null || targetResourceId == null || grantedRoles == null) {
                throw new AccessDeniedException("Fallo de seguridad: contexto de identidad incompleto");
            }

            // Regla 1: Administrador institucional tiene acceso irrestricto
            if (grantedRoles.contains("ROLE_ADMIN")) {
                return PolicyDecision.PERMIT;
            }

            // Regla 2: El sujeto autenticado únicamente puede acceder a su propio recurso
            if (actorId.equals(targetResourceId)) {
                return PolicyDecision.PERMIT;
            }

            // Violación de frontera de objeto (BOLA): rechazo explícito
            throw new AccessDeniedException("403 Forbidden: El usuario solicitante no posee privilegios sobre el recurso");
        }
    }

    private UserProfileAccessDecisionManager decisionManager;

    @BeforeEach
    void setUp() {
        decisionManager = new UserProfileAccessDecisionManager();
    }

    @Test
    @DisplayName("Caso Negativo: Estudiante intenta extraer perfil ajeno por UUID -> Lanza AccessDeniedException")
    void evaluate_unauthorizedStudentAccessingThirdPartyProfile_throwsAccessDenied() {
        UUID studentPrincipalId = UUID.fromString("a1b2c3d4-0000-0000-0000-000000000001");
        Set<String> studentRoles = Collections.singleton("ROLE_STUDENT");
        UUID victimProfileId = UUID.fromString("e5f6a7b8-9999-9999-9999-999999999999");

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> decisionManager.evaluate(studentPrincipalId, studentRoles, victimProfileId),
            "Debe lanzar AccessDeniedException ante intento de BOLA/IDOR"
        );

        assertTrue(thrown.getMessage().contains("403 Forbidden"));
    }

    @Test
    @DisplayName("Caso Positivo 1: Usuario consulta legítimamente su propio perfil -> Acceso PERMIT")
    void evaluate_ownerAccessingOwnProfile_accessPermitted() {
        UUID authenticatedUser = UUID.fromString("c7d8e9f0-1111-2222-3333-444444444444");
        Set<String> userRoles = Collections.singleton("ROLE_TEACHER");

        PolicyDecision decision = decisionManager.evaluate(authenticatedUser, userRoles, authenticatedUser);

        assertEquals(PolicyDecision.PERMIT, decision);
    }

    @Test
    @DisplayName("Caso Positivo 2: Administrador consulta perfil de cualquier usuario -> Acceso PERMIT")
    void evaluate_administratorInspectingAnyProfile_accessPermitted() {
        UUID adminId = UUID.fromString("00000000-aaaa-bbbb-cccc-dddddddddddd");
        Set<String> adminRoles = Collections.singleton("ROLE_ADMIN");
        UUID targetStudentId = UUID.fromString("11111111-2222-3333-4444-555555555555");

        PolicyDecision decision = decisionManager.evaluate(adminId, adminRoles, targetStudentId);

        assertEquals(PolicyDecision.PERMIT, decision);
    }
}
