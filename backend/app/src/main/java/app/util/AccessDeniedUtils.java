package app.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.request.WebRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to extract relevant information from access denied scenarios
 */
public class AccessDeniedUtils {
    
    /**
     * Extracts the required role from a PreAuthorize annotation message
     */
    public static String extractRequiredRole(String message) {
        if (message == null) return null;
        
        // Common patterns for role requirements
        Pattern[] patterns = {
            Pattern.compile("hasRole\\('([^']+)'\\)"),
            Pattern.compile("hasAuthority\\('([^']+)'\\)"),
            Pattern.compile("ROLE_([A-Z_]+)"),
            Pattern.compile("requires role '([^']+)'"),
            Pattern.compile("needs role '([^']+)'")
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        return null;
    }
    
    /**
     * Extracts the current user's role from authentication
     */
    public static String extractCurrentUserRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return null;
        }
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Extracts resource type from the request path
     */
    public static String extractResourceType(String path) {
        if (path == null) return null;
        
        // Extract resource type from common API patterns
        String[] pathParts = path.split("/");
        for (int i = 0; i < pathParts.length; i++) {
            if (pathParts[i].equals("api") && i + 1 < pathParts.length) {
                return pathParts[i + 1];
            }
        }
        
        return null;
    }
    
    /**
     * Extracts resource ID from the request path
     */
    public static String extractResourceId(String path) {
        if (path == null) return null;
        
        // Look for numeric IDs in the path
        Pattern pattern = Pattern.compile("/api/[^/]+/([0-9]+)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * Extracts the HTTP method/action from the request
     */
    public static String extractAction(WebRequest request) {
        if (request == null) return null;
        
        // Try to get the HTTP method from request attributes
        Object method = request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler", 0);
        if (method != null) {
            return method.toString();
        }
        
        return null;
    }
    
    /**
     * Generates a helpful suggestion based on the access denied context
     */
    public static String generateSuggestion(String requiredRole, String currentUserRole, String resourceType, String action) {
        if (requiredRole != null && currentUserRole != null) {
            if (requiredRole.equals("ADMIN") && !currentUserRole.equals("ROLE_ADMIN")) {
                return "Esta acción requiere permisos de administrador. Contacta con un administrador del sistema.";
            } else if (requiredRole.equals("PROFESOR") && !currentUserRole.equals("ROLE_PROFESOR") && !currentUserRole.equals("ROLE_ADMIN")) {
                return "Esta acción requiere permisos de profesor. Solo los profesores y administradores pueden realizar esta operación.";
            } else if (requiredRole.equals("ALUMNO") && !currentUserRole.equals("ROLE_ALUMNO") && !currentUserRole.equals("ROLE_PROFESOR") && !currentUserRole.equals("ROLE_ADMIN")) {
                return "Esta acción requiere permisos de alumno.";
            }
        }
        
        if (resourceType != null) {
            switch (resourceType) {
                case "clases":
                    return "Para acceder a esta funcionalidad, asegúrate de tener los permisos adecuados para gestionar clases.";
                case "enrollments":
                    return "Para gestionar inscripciones, necesitas permisos de profesor o administrador.";
                case "alumnos":
                    return "Para acceder a información de alumnos, necesitas permisos de profesor o administrador.";
                case "profesores":
                    return "Para gestionar profesores, necesitas permisos de administrador.";
                default:
                    return "Verifica que tienes los permisos necesarios para realizar esta acción.";
            }
        }
        
        return "Contacta con el administrador del sistema si crees que deberías tener acceso a esta funcionalidad.";
    }
    
    /**
     * Determines if the access denied is due to missing authentication
     */
    public static boolean isAuthenticationRequired(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated();
    }
    
    /**
     * Determines if the access denied is due to insufficient privileges
     */
    public static boolean isInsufficientPrivileges(Authentication authentication, String requiredRole) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false; // This is an authentication issue, not privileges
        }
        
        if (requiredRole == null) {
            return true; // Can't determine, assume insufficient privileges
        }
        
        String currentRole = extractCurrentUserRole(authentication);
        if (currentRole == null) {
            return true;
        }
        
        // Check if current role has the required privileges
        switch (requiredRole) {
            case "ADMIN":
                return !currentRole.equals("ROLE_ADMIN");
            case "PROFESOR":
                return !currentRole.equals("ROLE_PROFESOR") && !currentRole.equals("ROLE_ADMIN");
            case "ALUMNO":
                return !currentRole.equals("ROLE_ALUMNO") && !currentRole.equals("ROLE_PROFESOR") && !currentRole.equals("ROLE_ADMIN");
            default:
                return !currentRole.equals("ROLE_" + requiredRole);
        }
    }
}
