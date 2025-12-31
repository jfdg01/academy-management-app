package app.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import app.entidades.Alumno;
import app.entidades.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for Student entity
 * Contains student information without sensitive data
 */
@Schema(description = "Student data")
public record DTOAlumno(
    @Schema(description = "Unique student ID", example = "1", required = true)
    @Positive(message = "ID must be positive")
    Long id,
    
    @Schema(description = "Unique username", example = "student123", required = true)
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @Schema(description = "Student's first name", example = "Juan", required = true)
    @NotBlank(message = "First name cannot be empty")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    String firstName,
    
    @Schema(description = "Student's last name", example = "Perez Garcia", required = true)
    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    String lastName,
    
    @Schema(description = "Student's DNI", example = "12345678A", required = true)
    @NotBlank(message = "DNI cannot be empty")
    @Size(max = 20, message = "DNI cannot exceed 20 characters")
    String dni,
    
    @Schema(description = "Student's email", example = "juan.perez@email.com", required = true)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must have a valid format")
    String email,
    
    @Schema(description = "Phone number", example = "+34612345678", required = false)
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    String phoneNumber,
    
    @Schema(description = "Enrollment date", example = "2024-01-15T10:30:00", required = false)
    LocalDateTime enrollmentDate,
    
    @Schema(description = "Indicates if the student is enrolled", example = "true", required = true)
    boolean enrolled,
    
    @Schema(description = "Indicates if the account is enabled", example = "true", required = true)
    boolean enabled,
    
    @Schema(description = "List of enrolled class IDs", example = "[1, 2]", required = false)
    List<Long> classIds,
    
    @Schema(description = "List of payment IDs", example = "[1, 2]", required = false)
    List<Long> paymentIds,
    
    @Schema(description = "List of exercise submission IDs", example = "[1, 2]", required = false)
    List<Long> submissionIds,
    
    @Schema(description = "User role", example = "STUDENT", required = true)
    Usuario.Role role
) {

    /**
     * Constructor that creates a DTO from a Student entity
     */
    public DTOAlumno(Alumno alumno) {
        this(
            alumno.getId(),
            alumno.getUsername(),
            alumno.getFirstName(),
            alumno.getLastName(),
            alumno.getDni(),
            alumno.getEmail(),
            alumno.getPhoneNumber(),
            alumno.getEnrollDate(),
            alumno.isEnrolled(),
            alumno.isEnabled(),
            alumno.getClasses() != null ? alumno.getClasses().stream()
                .map(clase -> clase.getId())
                .collect(Collectors.toList()) : null,
            alumno.getPayments() != null ? alumno.getPayments().stream()
                .map(pago -> pago.getId())
                .collect(Collectors.toList()) : null,
            alumno.getSubmissions() != null ? alumno.getSubmissions().stream()
                .map(entrega -> entrega.getId())
                .collect(Collectors.toList()) : null,
            alumno.getRole()
        );
    }
    
    /**
     * Static method to create from entity
     */
    public static DTOAlumno from(Alumno alumno) {
        return new DTOAlumno(alumno);
    }
    
    /**
     * Gets the student's full name
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
    
    /**
     * Checks if the student is enrolled in any class
     */
    public boolean hasClasses() {
        return this.classIds != null && !this.classIds.isEmpty();
    }
    
    /**
     * Counts the number of enrolled classes
     */
    public int getClassCount() {
        return this.classIds != null ? this.classIds.size() : 0;
    }
    
    /**
     * Checks if the student has payments
     */
    public boolean hasPayments() {
        return this.paymentIds != null && !this.paymentIds.isEmpty();
    }
    
    /**
     * Counts the number of payments
     */
    public int getPaymentCount() {
        return this.paymentIds != null ? this.paymentIds.size() : 0;
    }
    
    /**
     * Checks if the student has exercise submissions
     */
    public boolean hasSubmissions() {
        return this.submissionIds != null && !this.submissionIds.isEmpty();
    }
    
    /**
     * Counts the number of submissions
     */
    public int getSubmissionCount() {
        return this.submissionIds != null ? this.submissionIds.size() : 0;
    }
}