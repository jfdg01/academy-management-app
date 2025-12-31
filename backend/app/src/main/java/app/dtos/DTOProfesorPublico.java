// LLM_EDIT_TIMESTAMP: 25 ago. 14:00
package app.dtos;

import app.entidades.Profesor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * DTO for public Professor information
 * Contains only public professor information that students can access
 */
@Schema(description = "Public professor data")
public record DTOProfesorPublico(
    @Schema(description = "Unique professor ID", example = "1", required = true)
    @Positive(message = "ID must be positive")
    Long id,
    
    @Schema(description = "Professor's first name", example = "Maria", required = true)
    String firstName,
    
    @Schema(description = "Professor's last name", example = "Garcia Lopez", required = true)
    String lastName,
    
    @Schema(description = "Professor's email", example = "maria.garcia@email.com", required = true)
    String email,
    
    @Schema(description = "Phone number", example = "+34687654321", required = false)
    String phoneNumber,
    
    @Schema(description = "Indicates if the account is enabled", example = "true", required = true)
    boolean enabled,
    
    @Schema(description = "Number of assigned classes", example = "5", required = true)
    int classCount,
    
    @Schema(description = "Profile creation date", example = "2024-01-15T10:30:00", required = false)
    LocalDateTime createdAt
) {

    /**
     * Constructor that creates a DTO from a Professor entity
     */
    public DTOProfesorPublico(Profesor profesor) {
        this(
            profesor.getId(),
            profesor.getFirstName(),
            profesor.getLastName(),
            profesor.getEmail(),
            profesor.getPhoneNumber(),
            profesor.isEnabled(),
            profesor.getNumeroClases(),
            LocalDateTime.now() // placeholder, in a real implementation would be creation date from entity
        );
    }
    
    /**
     * Static method to create from entity
     */
    public static DTOProfesorPublico from(Profesor profesor) {
        return new DTOProfesorPublico(profesor);
    }
    
    /**
     * Gets the professor's full name
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
    
    /**
     * Checks if the professor has assigned classes
     */
    public boolean hasClasses() {
        return this.classCount > 0;
    }
    
    /**
     * Checks if the professor is enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }
    
    /**
     * Gets the enabled status as text
     */
    public String getEnabledStatus() {
        return this.enabled ? "Enabled" : "Disabled";
    }
}
