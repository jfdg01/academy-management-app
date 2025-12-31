package app.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for student enrollment request in a class
 * Used by professors to manage enrollments in their classes
 */
public record DTOPeticionEnrollment(
        @NotNull(message = "Student ID cannot be null")
        @Positive(message = "Student ID must be positive")
        Long studentId,
        
        @NotNull(message = "Class ID cannot be null")
        @Positive(message = "Class ID must be positive")
        Long classId
) {
    
    /**
     * Default constructor
     */
    public DTOPeticionEnrollment {
        // Additional validations if needed
    }
}
