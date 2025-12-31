// LLM_EDIT_TIMESTAMP: 25 ago. 14:00
package app.dtos;

import app.entidades.Clase;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * DTO para mostrar información detallada de una clase con información pública del profesor
 * y estado de inscripción para estudiantes
 */
public record DTOClaseConDetallesPublico(
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
        String tipoClase, // "TALLER" o "CURSO"
        
        // Información pública del profesor principal
        DTOProfesorPublico profesor,
        
        // Estado de inscripción para el estudiante actual
        boolean isEnrolled,
        LocalDateTime fechaInscripcion,
        
        // Información adicional
        int alumnosCount,
        int profesoresCount
) {
    
    /**
     * Constructor that creates a DTO from a Class entity with public details
     */
    public DTOClaseConDetallesPublico(Clase clase, DTOProfesorPublico profesor, boolean inscrito, 
                                       LocalDateTime fechaInscripcion, int numeroAlumnos, int numeroProfesores) {
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
                clase.getMaterial() != null ? clase.getMaterial().stream()
                    .map(DTOMaterial::new)
                    .collect(Collectors.toList()) : new ArrayList<>(),
                determinarTipoClase(clase),
                profesor,
                inscrito,
                fechaInscripcion,
                numeroAlumnos,
                numeroProfesores
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
