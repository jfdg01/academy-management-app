package app.entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;

/**
 * Student Entity
 * Represents a student in the platform
 * Based on UML specification
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ALUMNO")
@NamedEntityGraph(
    name = "Alumno.withClasses",
    attributeNodes = {
        @NamedAttributeNode("classes")
    }
)
@NamedEntityGraph(
    name = "Alumno.withPayments",
    attributeNodes = {
        @NamedAttributeNode("payments")
    }
)
@NamedEntityGraph(
    name = "Alumno.withSubmissions",
    attributeNodes = {
        @NamedAttributeNode("submissions")
    }
)
@NamedEntityGraph(
    name = "Alumno.withClassesAndPayments",
    attributeNodes = {
        @NamedAttributeNode("classes"),
        @NamedAttributeNode("payments")
    }
)
@NamedEntityGraph(
    name = "Alumno.withClassesAndSubmissions",
    attributeNodes = {
        @NamedAttributeNode("classes"),
        @NamedAttributeNode("submissions")
    }
)
@NamedEntityGraph(
    name = "Alumno.withPaymentsAndSubmissions",
    attributeNodes = {
        @NamedAttributeNode("payments"),
        @NamedAttributeNode("submissions")
    }
)
public class Alumno extends Usuario {
    
    @NotNull
    @Column(name = "enrollment_date")
    private LocalDateTime enrollDate = LocalDateTime.now();
    
    @NotNull
    private boolean enrolled = false;
    
    // JPA Relationships - using List for consistency
    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    private List<Clase> classes = new ArrayList<>();
    
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pago> payments = new ArrayList<>();
    
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EntregaEjercicio> submissions = new ArrayList<>();
    
    public Alumno() {
        super();
        this.setRole(Role.ALUMNO);
    }
    
    public Alumno(String username, String password, String firstName, String lastName,
                  String dni, String email, String phoneNumber) {
        super(username, password, firstName, lastName, dni, email, phoneNumber);
        this.setRole(Role.ALUMNO);
    }
    
    /**
     * Method to reset password according to UML
     * TODO: Implement email-based reset logic
     */
    public void resetPassword() {
        // TODO: Implement according to project specifications
        throw new UnsupportedOperationException("resetPassword method not implemented");
    }
    
    /**
     * Adds a class to the student using JPA relationship
     * @param clase Clase entity
     */
    public void agregarClase(Clase clase) {
        if (!this.classes.contains(clase)) {
            this.classes.add(clase);
            clase.getStudents().add(this); // Maintain bidirectional relationship
        }
    }
    
    /**
     * Removes a class from the student using JPA relationship
     * @param clase Clase entity
     */
    public void removerClase(Clase clase) {
        this.classes.remove(clase);
        clase.getStudents().remove(this);
    }
    
    /**
     * Checks if the student is enrolled in a specific class using JPA relationship
     * @param clase Clase entity
     * @return true if enrolled, false otherwise
     */
    public boolean estaInscritoEnClase(Clase clase) {
        return this.classes.contains(clase);
    }
    
    /**
     * Checks if the student is enrolled in a specific class by ID
     * @param claseId ID of the class
     * @return true if enrolled, false otherwise
     */
    public boolean estaInscritoEnClasePorId(Long claseId) {
        return this.classes.stream().anyMatch(c -> c.getId().equals(claseId));
    }
    

    
}