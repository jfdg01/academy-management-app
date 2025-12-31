package app.dtos;

import app.entidades.Administrador;
import app.entidades.Usuario;

import java.time.LocalDateTime;

/**
 * DTO for Administrator entity
 * Contains administrator information without sensitive data
 */
public record DTOAdministrador(
        Long id,
        String username,
        String firstName,
        String lastName,
        String dni,
        String email,
        String phoneNumber,
        Usuario.Role role,
        boolean enabled,
        LocalDateTime createdAt
) {
    
    /**
     * Constructor that creates a DTO from an Administrator entity
     */
    public DTOAdministrador(Administrador administrador) {
        this(
                administrador.getId(),
                administrador.getUsername(),
                administrador.getFirstName(),
                administrador.getLastName(),
                administrador.getDni(),
                administrador.getEmail(),
                administrador.getPhoneNumber(),
                administrador.getRole(),
                administrador.isEnabled(),
                LocalDateTime.now() // placeholder, in a real implementation would be creation date from entity
        );
    }
    
    /**
     * Static method to create from entity
     */
    public static DTOAdministrador from(Administrador administrador) {
        return new DTOAdministrador(administrador);
    }
    
    /**
     * Gets the administrator's full name
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}
