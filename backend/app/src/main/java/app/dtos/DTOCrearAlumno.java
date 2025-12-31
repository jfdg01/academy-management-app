package app.dtos;

import app.validation.ValidDNI;
import app.validation.ValidEmail;
import app.validation.ValidPhone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DTOCrearAlumno(
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, hyphens and underscores")
    String username,
    
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must have at least 6 characters")
    String password,
    
    @NotBlank(message = "First name cannot be empty")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "First name can only contain letters and spaces")
    String firstName,
    
    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Last name can only contain letters and spaces")
    String lastName,
    
    @NotBlank(message = "DNI cannot be empty")
    @ValidDNI
    String dni,
    
    @NotBlank(message = "Email cannot be empty")
    @ValidEmail
    String email,
    
    @ValidPhone
    String phoneNumber
) {}
