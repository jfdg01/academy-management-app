package app.dtos;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta de modificación de entrega
 * Incluye información sobre las operaciones realizadas
 */
public record DTORespuestaModificacionEntrega(
        Long entregaId,
        String comentarios,
        List<String> archivosEntregados,
        int numeroArchivos,
        LocalDateTime fechaModificacion,
        List<DTOOperacionArchivoResultado> operacionesRealizadas,
        List<String> errores
) {
    
    /**
     * Verifica si la modificación fue exitosa
     */
    public boolean fueExitosa() {
        return errores == null || errores.isEmpty();
    }
    
    /**
     * Verifica si se realizaron operaciones con archivos
     */
    public boolean tieneOperacionesArchivos() {
        return operacionesRealizadas != null && !operacionesRealizadas.isEmpty();
    }
    
    /**
     * Obtiene el número de operaciones exitosas
     */
    public long getOperacionesExitosas() {
        if (operacionesRealizadas == null) {
            return 0;
        }
        return operacionesRealizadas.stream()
                .filter(DTOOperacionArchivoResultado::fueExitosa)
                .count();
    }
    
    /**
     * Obtiene el número de operaciones fallidas
     */
    public long getOperacionesFallidas() {
        if (operacionesRealizadas == null) {
            return 0;
        }
        return operacionesRealizadas.stream()
                .filter(op -> !op.fueExitosa())
                .count();
    }
}
