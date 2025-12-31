package app.dtos;

import app.validation.NotEmptyIfPresent;
import app.validation.ValidDNI;
import app.validation.ValidEmail;
import app.validation.ValidPhone;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for partial student updates (PATCH)
 * All fields are optional to allow partial updates
 */
public record DTOActualizarAlumno(
    @NotEmptyIfPresent(message = "First name cannot be empty if provided")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "First name can only contain letters and spaces")
    String firstName,
    
    @NotEmptyIfPresent(message = "Last name cannot be empty if provided")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Last name can only contain letters and spaces")
    String lastName,
    
    @NotEmptyIfPresent(message = "DNI cannot be empty if provided")
    @ValidDNI
    String dni,
    
    @NotEmptyIfPresent(message = "Email cannot be empty if provided")
    @ValidEmail
    String email,
    
    @ValidPhone
    String phoneNumber,
    
    Boolean enrolled,
    
    Boolean enabled
) {
}
