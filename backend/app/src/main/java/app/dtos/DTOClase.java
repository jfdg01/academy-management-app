package app.dtos;

import app.entidades.Clase;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * DTO para la entidad Clase (abstracta)
 * Contiene información de la clase sin exposer detalles internos
 */
public record DTOClase(
        Long id,
        String titulo,
        String descripcion,
        BigDecimal precio,
        EPresencialidad presencialidad,
        String imagenPortada,
        EDificultad nivel,
        List<String> alumnosId,
        List<String> profesoresId,
        List<String> ejerciciosId,
        List<DTOMaterial> material,
        String tipoClase // "TALLER" o "CURSO"
) {
    
    /**
     * Constructor that creates a DTO from a Class entity
     */
    public DTOClase(Clase clase) {
        this(
            clase.getId(),
            clase.getTitle(),
            clase.getDescription(),
            clase.getPrice(),
            clase.getFormat(),
            clase.getImage(),
            clase.getDifficulty(),
            clase.getStudents().stream()
                .map(student -> student.getId().toString())
                .collect(Collectors.toList()),
            clase.getTeachers().stream()
                .map(teacher -> teacher.getId().toString())
                .collect(Collectors.toList()),
            clase.getExercises().stream()
                .map(exercise -> exercise.getId().toString())
                .collect(Collectors.toList()),
            clase.getMaterial().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList()),
            determinarTipoClase(clase)
        );
    }
    
    /**
     * Determina el tipo de clase de forma segura sin usar reflection
     */
    private static String determinarTipoClase(Clase clase) {
        if (clase instanceof app.entidades.Curso) {
            return "CURSO";
        } else if (clase instanceof app.entidades.Taller) {
            return "TALLER";
        } else {
            return "CLASE"; // Fallback para la clase abstracta
        }
    }
    
    /**
     * metodo estático para crear desde entidad
     */
    public static DTOClase from(Clase clase) {
        return new DTOClase(clase);
    }
    
    /**
     * Verifica si la clase es online
     */
    public boolean esOnline() {
        return this.presencialidad == EPresencialidad.ONLINE;
    }
    
    /**
     * Verifica si la clase es presencial
     */
    public boolean esPresencial() {
        return this.presencialidad == EPresencialidad.PRESENCIAL;
    }
    
    /**
     * Obtiene el número de alumnos inscritos
     */
    public int getNumeroAlumnos() {
        return this.alumnosId != null ? this.alumnosId.size() : 0;
    }
    
    /**
     * Obtiene el número de profesores asignados
     */
    public int getNumeroProfesores() {
        return this.profesoresId != null ? this.profesoresId.size() : 0;
    }
    
    /**
     * Obtiene el número de ejercicios asignados
     */
    public int getNumeroEjercicios() {
        return this.ejerciciosId != null ? this.ejerciciosId.size() : 0;
    }
    
    /**
     * Obtiene el número de materiales disponibles
     */
    public int getNumeroMateriales() {
        return this.material != null ? this.material.size() : 0;
    }
    
    /**
     * Verifica si es un taller
     */
    public boolean esTaller() {
        return "TALLER".equals(this.tipoClase);
    }
    
    /**
     * Verifica si es un curso
     */
    public boolean esCurso() {
        return "CURSO".equals(this.tipoClase);
    }
}
