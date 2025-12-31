package app.dtos;

import java.time.LocalDateTime;

/**
 * DTO para verificar el estado de inscripción de un estudiante en una clase
 */
public record DTOEstadoInscripcion(
        Long alumnoId,
        Long claseId,
        boolean isEnrolled,
        LocalDateTime fechaInscripcion
) {
    
    /**
     * Constructor para un estudiante inscrito
     */
    public static DTOEstadoInscripcion enrolled(Long alumnoId, Long claseId, LocalDateTime fechaInscripcion) {
        return new DTOEstadoInscripcion(alumnoId, claseId, true, fechaInscripcion);
    }
    
    /**
     * Constructor para un estudiante no inscrito
     */
    public static DTOEstadoInscripcion notEnrolled(Long alumnoId, Long claseId) {
        return new DTOEstadoInscripcion(alumnoId, claseId, false, null);
    }
}
