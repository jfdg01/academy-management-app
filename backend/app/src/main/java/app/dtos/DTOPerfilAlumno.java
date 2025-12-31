package app.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import app.entidades.Alumno;
import app.entidades.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for student profile (view for the student themselves)
 * Hides sensitive information like ID and enabled status
 */
public record DTOPerfilAlumno(
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @NotBlank(message = "First name cannot be empty")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    String firstName,
    
    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    String lastName,
    
    @NotBlank(message = "DNI cannot be empty")
    @Size(max = 20, message = "DNI cannot exceed 20 characters")
    String dni,
    
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must have a valid format")
    String email,
    
    @Size(max = 15, message = "Phone number cannot exceed 15 characters")
    String phoneNumber,
    
    LocalDateTime enrollmentDate,
    
    boolean enrolled,
    
    List<Long> classIds,
    
    List<Long> paymentIds,
    
    List<Long> submissionIds,
    
    Usuario.Role role
) {

    /**
     * Constructor that creates a DTO from a Student entity
     * Hides sensitive information like ID and enabled status
     */
    public DTOPerfilAlumno(Alumno alumno) {
        this(
            alumno.getUsername(),
            alumno.getFirstName(),
            alumno.getLastName(),
            alumno.getDni(),
            alumno.getEmail(),
            alumno.getPhoneNumber(),
            alumno.getEnrollDate(),
            alumno.isEnrolled(),
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
    public static DTOPerfilAlumno from(Alumno alumno) {
        return new DTOPerfilAlumno(alumno);
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
