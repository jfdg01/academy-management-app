package app.dtos;

import app.entidades.Alumno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for public student information (view for other students)
 * Only shows first name and last name, hiding sensitive information like email, ID, etc.
 */
@Schema(description = "Public student information (first name and last name only)")
public record DTOAlumnoPublico(
    @Schema(description = "Student's first name", example = "Juan")
    @NotBlank(message = "First name cannot be empty")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    String firstName,
    
    @Schema(description = "Student's last name", example = "Garcia Lopez")
    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    String lastName
) {

    /**
     * Constructor that creates a DTO from a Student entity
     * Only includes public information (first name and last name)
     */
    public DTOAlumnoPublico(Alumno alumno) {
        this(
            alumno.getFirstName(),
            alumno.getLastName()
        );
    }
    
    /**
     * Static method to create from entity
     */
    public static DTOAlumnoPublico from(Alumno alumno) {
        return new DTOAlumnoPublico(alumno);
    }
    
    /**
     * Gets the student's full name
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}
