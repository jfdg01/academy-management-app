package app.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para la petición de crear una entrega de ejercicio con archivos previamente subidos
 */
public record DTOPeticionCrearEntregaConArchivos(
        @NotNull(message = "El ID del ejercicio es obligatorio")
        Long ejercicioId,
        
        @Size(max = 10, message = "No se pueden subir más de 10 archivos")
        List<String> archivosRutas
) {
    
    /**
     * Constructor simplificado para crear entrega sin archivos
     */
    public DTOPeticionCrearEntregaConArchivos(Long ejercicioId) {
        this(ejercicioId, List.of());
    }
    
    /**
     * Verifica si la entrega tiene archivos
     */
    public boolean tieneArchivos() {
        return archivosRutas != null && !archivosRutas.isEmpty();
    }
    
    /**
     * Obtiene el número de archivos
     */
    public int getNumeroArchivos() {
        return archivosRutas != null ? archivosRutas.size() : 0;
    }
}
