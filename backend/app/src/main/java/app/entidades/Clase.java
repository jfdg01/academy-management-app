package app.entidades;

import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Clase (abstracta)
 * Representa una clase en la plataforma (puede ser Taller o Curso)
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "clases")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_clase", discriminatorType = DiscriminatorType.STRING)
public abstract class Clase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull
    @Size(max = 200)
    private String title;
    
    @Size(max = 1000)
    private String description;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EPresencialidad format;
    
    @Size(max = 500)
    private String image;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EDificultad difficulty;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "clase_alumno",
        joinColumns = @JoinColumn(name = "clase_id"),
        inverseJoinColumns = @JoinColumn(name = "alumno_id")
    )
    private List<Alumno> students = new ArrayList<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "clase_profesor",
        joinColumns = @JoinColumn(name = "clase_id"),
        inverseJoinColumns = @JoinColumn(name = "profesor_id")
    )
    private List<Profesor> teachers = new ArrayList<>();
    
    @OneToMany(mappedBy = "clase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ejercicio> exercises = new ArrayList<>();
    
    // Relación Many-to-Many con Material (already JPA-based)
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "clase_material",
        joinColumns = @JoinColumn(name = "clase_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> material = new ArrayList<>();
    
    public Clase() {}
    
    public Clase(String title, String description, BigDecimal price,
                 EPresencialidad format, String image, EDificultad difficulty) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.format = format;
        this.image = image;
        this.difficulty = difficulty;
    }
    
    /**
     * Agrega un alumno a la clase usando relación JPA
     * @param alumno Entidad Alumno
     */
    public void agregarAlumno(Alumno alumno) {
        if (!this.students.contains(alumno)) {
            this.students.add(alumno);
            alumno.getClasses().add(this); 
        }
    }
    
    /**
     * Remueve un alumno de la clase usando relación JPA
     * @param alumno Entidad Alumno
     */
    public void removerAlumno(Alumno alumno) {
        this.students.remove(alumno);
        alumno.getClasses().remove(this);
    }
    
    /**
     * Agrega un profesor a la clase usando relación JPA
     * @param profesor Entidad Profesor
     */
    public void agregarProfesor(Profesor profesor) {
        if (!this.teachers.contains(profesor)) {
            this.teachers.add(profesor);
            profesor.getClasses().add(this); 
        }
    }
    
    /**
     * Remueve un profesor de la clase usando relación JPA
     * @param profesor Entidad Profesor
     */
    public void removerProfesor(Profesor profesor) {
        this.teachers.remove(profesor);
        profesor.getClasses().remove(this);
    }
    
    /**
     * Agrega un ejercicio a la clase usando relación JPA
     * @param ejercicio Entidad Ejercicio
     */
    public void agregarEjercicio(Ejercicio ejercicio) {
        if (!this.exercises.contains(ejercicio)) {
            this.exercises.add(ejercicio);
            ejercicio.setClase(this);
        }
    }
    
    /**
     * Remueve un ejercicio de la clase usando relación JPA
     * @param ejercicio Entidad Ejercicio
     */
    public void removerEjercicio(Ejercicio ejercicio) {
        this.exercises.remove(ejercicio);
        ejercicio.setClase(null);
    }
    
    /**
     * Verifica si un profesor está asignado a la clase
     * @param profesorId ID del profesor
     * @return true si está asignado, false en caso contrario
     */
    public boolean tieneProfesor(Long profesorId) {
        return this.teachers.stream().anyMatch(p -> p.getId().equals(profesorId));
    }
    
    /**
     * Agrega material a la clase
     * @param nuevoMaterial Material a agregar
     */
    public void agregarMaterial(Material nuevoMaterial) {
        this.material.add(nuevoMaterial);
    }
    
    /**
     * Remueve material de la clase por ID
     * @param materialId ID del material
     */
    public void removerMaterial(Long materialId) {
        this.material.removeIf(m -> m.getId().equals(materialId));
    }
}
