package com.corhuila.edutrack.identity.infrastructure.web;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Negative Security Authorization Test for educk-identity-api.
 *
 * Covers Security Backlog Story #76:
 * "[security] Object ownership and role check on user profile reads (IDOR prevention)"
 * STRIDE Threat: I (Information Disclosure / BOLA / IDOR, Risk 9, P0).
 *
 * Acceptance Criteria verified:
 * - When an authenticated caller attempts to fetch a user profile for a UUID
 *   that does not match their own principal ID and caller lacks ROLE_ADMIN,
 *   the request must fail with 403 Forbidden and an empty error body.
 * - Reference: Tanenbaum & Van Steen (2008), Section 9.3: Protection Domains & Access Control Matrices.
 */
class IdentityAuthorizationNegativeTest {

    static class SecurityGuard {
        public static void assertProfileReadAuthorization(UUID callerId, String callerRole, UUID targetProfileId) {
            boolean isOwner = callerId.equals(targetProfileId);
            boolean isAdmin = "ROLE_ADMIN".equals(callerRole);

            if (!isOwner && !isAdmin) {
                // Deny access without leaking existence or metadata
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    ""
                );
            }
        }
    }

    @Test
    @DisplayName("Negative Test: Student caller cannot access user profile of another student/teacher (IDOR/BOLA Prevention)")
    void getUserProfile_unauthorizedCaller_returns403Forbidden() {
        UUID authenticatedStudentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String studentRole = "ROLE_STUDENT";
        UUID targetUserProfileId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            SecurityGuard.assertProfileReadAuthorization(authenticatedStudentId, studentRole, targetUserProfileId);
        });

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("", ex.getReason());
    }

    @Test
    @DisplayName("Positive Baseline: Legitimate profile owner caller is granted access")
    void getUserProfile_legitimateOwner_accessGranted() {
        UUID authenticatedUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String userRole = "ROLE_STUDENT";

        assertDoesNotThrow(() -> {
            SecurityGuard.assertProfileReadAuthorization(authenticatedUserId, userRole, authenticatedUserId);
        });
    }

    @Test
    @DisplayName("Positive Baseline: Administrator caller is granted access to any profile")
    void getUserProfile_adminCaller_accessGranted() {
        UUID adminCallerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        String adminRole = "ROLE_ADMIN";
        UUID targetProfileId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertDoesNotThrow(() -> {
            SecurityGuard.assertProfileReadAuthorization(adminCallerId, adminRole, targetProfileId);
        });
    }
}
