package app.dtos;

import java.util.Set;

import app.validation.NotEmptyIfPresent;
import app.validation.ValidDNI;
import app.validation.ValidEmail;
import app.validation.ValidPhone;
import jakarta.validation.constraints.Size;

/**
 * DTO for partial professor data update
 * All fields are optional, only non-null fields are updated
 */
public record DTOActualizacionProfesor(
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @NotEmptyIfPresent
    String firstName,

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @NotEmptyIfPresent
    String lastName,

    @ValidDNI
    @NotEmptyIfPresent
    String dni,

    @ValidEmail
    @NotEmptyIfPresent
    String email,

    @ValidPhone
    @NotEmptyIfPresent
    String phoneNumber,

    Set<String> classIds,

    Boolean enabled
) {
    /**
     * Checks if all fields are empty/null
     * @return true if all fields are null
     */
    public boolean isEmpty() {
        return firstName == null && 
               lastName == null && 
               dni == null && 
               email == null && 
               phoneNumber == null && 
               (classIds == null || classIds.isEmpty()) &&
               enabled == null;
    }
}
