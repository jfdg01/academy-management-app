package app.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO para la petición de crear una entrega de ejercicio
 */
public record DTOPeticionCrearEntregaEjercicio(
        @NotNull(message = "El ID del alumno es obligatorio")
        Long alumnoId,
        @NotNull(message = "El ID del ejercicio es obligatorio")
        Long ejercicioId,
        List<String> archivosEntregados
) {
    
    /**
     * Constructor simplificado para crear entrega sin archivos
     */
    public DTOPeticionCrearEntregaEjercicio(Long alumnoId, Long ejercicioId) {
        this(alumnoId, ejercicioId, List.of());
    }
    
    /**
     * Verifica si la entrega tiene archivos
     */
    public boolean tieneArchivos() {
        return archivosEntregados != null && !archivosEntregados.isEmpty();
    }
    
    /**
     * Obtiene el número de archivos
     */
    public int getNumeroArchivos() {
        return archivosEntregados != null ? archivosEntregados.size() : 0;
    }
}
