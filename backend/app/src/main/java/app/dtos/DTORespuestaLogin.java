package app.dtos;

import app.entidades.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response with JWT token")
public record DTORespuestaLogin(
    @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    String token,
    
    @Schema(description = "Token type", example = "Bearer", required = true)
    String type,
    
    @Schema(description = "Authenticated user ID", example = "1", required = true)
    Long id,
    
    @Schema(description = "Username", example = "user123", required = true)
    String username,
    
    @Schema(description = "User email", example = "user@example.com", required = true)
    String email,
    
    @Schema(description = "User first name", example = "Juan", required = true)
    String firstName,
    
    @Schema(description = "User last name", example = "Perez", required = true)
    String lastName,
    
    @Schema(description = "User role", example = "STUDENT", required = true)
    String role
) {
    public static DTORespuestaLogin from(Usuario usuario, String token) {
        return new DTORespuestaLogin(
            token,
            "Bearer",
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getFirstName(),
            usuario.getLastName(),
            usuario.getRole().name()
        );
    }
} 