package app.dtos;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para la petición de subir archivos para una entrega de ejercicio
 */
public record DTOPeticionSubirArchivoEntrega(
        @NotNull(message = "El ID del ejercicio es obligatorio")
        Long ejercicioId
) {
    
    /**
     * Constructor por defecto
     */
    public DTOPeticionSubirArchivoEntrega {
        if (ejercicioId == null) {
            throw new IllegalArgumentException("El ID del ejercicio no puede ser null");
        }
    }
}
