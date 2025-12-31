package app.dtos;

import java.util.List;

import app.validation.ValidDNI;
import app.validation.ValidEmail;
import app.validation.ValidPhone;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for professor registration request
 * Contains all necessary data to create a professor
 */
public record DTOPeticionRegistroProfesor(
        @NotNull
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, hyphens and underscores")
        String username,
        
        @NotNull
        @Size(min = 6)
        String password,
        
        @NotNull
        @Size(max = 100)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "First name can only contain letters and spaces")
        String firstName,
        
        @NotNull
        @Size(max = 100)
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Last name can only contain letters and spaces")
        String lastName,
        
        @NotNull
        @ValidDNI
        String dni,
        
        @NotNull
        @ValidEmail
        String email,
        
        @ValidPhone
        String phoneNumber,
        
        List<Long> classIds
) {
    
    /**
     * Default constructor with empty class list
     */
    public DTOPeticionRegistroProfesor(String username, String password, String firstName, 
                                      String lastName, String dni, String email, 
                                      String phoneNumber) {
        this(username, password, firstName, lastName, dni, email, phoneNumber, List.of());
    }
}
