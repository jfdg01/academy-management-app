package app.dtos;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO para modificar una entrega de ejercicio
 * Permite actualizar comentarios y realizar operaciones con archivos
 */
public record DTOPeticionModificarEntrega(
        @Size(max = 2000, message = "Los comentarios no pueden exceder 2000 caracteres")
        String comentarios,
        
        // Operaciones con archivos
        List<DTOOperacionArchivo> operacionesArchivos
) {
    
    /**
     * Verifica si la petición tiene operaciones de archivos
     */
    public boolean tieneOperacionesArchivos() {
        return operacionesArchivos != null && !operacionesArchivos.isEmpty();
    }
    
    /**
     * Verifica si la petición tiene comentarios para actualizar
     */
    public boolean tieneComentarios() {
        return comentarios != null && !comentarios.trim().isEmpty();
    }
    
    /**
     * Verifica si la petición es válida (tiene al menos una operación)
     */
    public boolean esValida() {
        return tieneComentarios() || tieneOperacionesArchivos();
    }
}
