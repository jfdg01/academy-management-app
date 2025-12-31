package app.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "User registration request data")
public record DTOPeticionRegistro(
    @Schema(description = "Unique username", example = "user123", required = true)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @Schema(description = "User password", example = "password123", required = true)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least 6 characters")
    String password,
    
    @Schema(description = "User email", example = "user@email.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must have a valid format")
    String email,
    
    @Schema(description = "User first name", example = "Juan", required = true)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    String firstName,
    
    @Schema(description = "User last name", example = "Perez Garcia", required = true)
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    String lastName
) {} 