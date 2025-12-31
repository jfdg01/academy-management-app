package app.other;

import app.dtos.ErrorResponse;
import app.util.AccessDeniedUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar las respuestas mejoradas de acceso denegado
 */
public class EnhancedAccessDeniedTest {

    @Test
    public void testExtractRequiredRole() {
        // Test extraction of required role from different message formats
        assertEquals("ADMIN", AccessDeniedUtils.extractRequiredRole("hasRole('ADMIN')"));
        assertEquals("PROFESOR", AccessDeniedUtils.extractRequiredRole("hasAuthority('PROFESOR')"));
        assertEquals("ALUMNO", AccessDeniedUtils.extractRequiredRole("ROLE_ALUMNO"));
        assertNull(AccessDeniedUtils.extractRequiredRole("Some other message"));
    }

    @Test
    public void testExtractCurrentUserRole() {
        // Test with ADMIN role
        TestingAuthenticationToken adminAuth = new TestingAuthenticationToken(
            "admin", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        assertEquals("ROLE_ADMIN", AccessDeniedUtils.extractCurrentUserRole(adminAuth));

        // Test with PROFESOR role
        TestingAuthenticationToken profesorAuth = new TestingAuthenticationToken(
            "profesor", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROFESOR"))
        );
        assertEquals("ROLE_PROFESOR", AccessDeniedUtils.extractCurrentUserRole(profesorAuth));

        // Test with no role
        TestingAuthenticationToken noRoleAuth = new TestingAuthenticationToken(
            "user", "password", 
            Collections.emptyList()
        );
        assertNull(AccessDeniedUtils.extractCurrentUserRole(noRoleAuth));
    }

    @Test
    public void testExtractResourceType() {
        assertEquals("clases", AccessDeniedUtils.extractResourceType("/api/clases/123"));
        assertEquals("enrollments", AccessDeniedUtils.extractResourceType("/api/enrollments"));
        assertEquals("alumnos", AccessDeniedUtils.extractResourceType("/api/alumnos/456"));
        assertNull(AccessDeniedUtils.extractResourceType("/some/other/path"));
    }

    @Test
    public void testExtractResourceId() {
        assertEquals("123", AccessDeniedUtils.extractResourceId("/api/clases/123"));
        assertEquals("456", AccessDeniedUtils.extractResourceId("/api/enrollments/456"));
        assertNull(AccessDeniedUtils.extractResourceId("/api/clases"));
        assertNull(AccessDeniedUtils.extractResourceId("/some/other/path"));
    }

    @Test
    public void testGenerateSuggestion() {
        // Test ADMIN requirement
        String suggestion = AccessDeniedUtils.generateSuggestion(
            "ADMIN", "ROLE_ALUMNO", "clases", "POST"
        );
        assertTrue(suggestion.contains("administrador"));

        // Test PROFESOR requirement
        suggestion = AccessDeniedUtils.generateSuggestion(
            "PROFESOR", "ROLE_ALUMNO", "enrollments", "DELETE"
        );
        assertTrue(suggestion.contains("profesor"));

        // Test resource-specific suggestion
        suggestion = AccessDeniedUtils.generateSuggestion(
            null, null, "clases", null
        );
        assertTrue(suggestion.contains("clases"));
    }

    @Test
    public void testIsAuthenticationRequired() {
        // Test with null authentication
        assertTrue(AccessDeniedUtils.isAuthenticationRequired(null));

        // Test with unauthenticated token
        TestingAuthenticationToken unauthenticatedAuth = new TestingAuthenticationToken(
            "user", "password", 
            Collections.emptyList()
        );
        unauthenticatedAuth.setAuthenticated(false);
        assertTrue(AccessDeniedUtils.isAuthenticationRequired(unauthenticatedAuth));

        // Test with authenticated token
        TestingAuthenticationToken authenticatedAuth = new TestingAuthenticationToken(
            "user", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authenticatedAuth.setAuthenticated(true);
        assertFalse(AccessDeniedUtils.isAuthenticationRequired(authenticatedAuth));
    }

    @Test
    public void testIsInsufficientPrivileges() {
        // Test ALUMNO trying to access ADMIN resource
        TestingAuthenticationToken alumnoAuth = new TestingAuthenticationToken(
            "alumno", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ALUMNO"))
        );
        assertTrue(AccessDeniedUtils.isInsufficientPrivileges(alumnoAuth, "ADMIN"));

        // Test PROFESOR accessing PROFESOR resource
        TestingAuthenticationToken profesorAuth = new TestingAuthenticationToken(
            "profesor", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROFESOR"))
        );
        assertFalse(AccessDeniedUtils.isInsufficientPrivileges(profesorAuth, "PROFESOR"));

        // Test ADMIN accessing any resource
        TestingAuthenticationToken adminAuth = new TestingAuthenticationToken(
            "admin", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        assertFalse(AccessDeniedUtils.isInsufficientPrivileges(adminAuth, "PROFESOR"));
        assertFalse(AccessDeniedUtils.isInsufficientPrivileges(adminAuth, "ALUMNO"));
    }
}
