package app.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para operaciones con archivos en entregas
 * Define las diferentes operaciones que se pueden realizar con archivos
 */
public record DTOOperacionArchivo(
        @NotNull(message = "El tipo de operación es requerido")
        TipoOperacion tipo,
        
        @NotBlank(message = "La ruta del archivo es requerida")
        @Size(max = 500, message = "La ruta del archivo no puede exceder 500 caracteres")
        String rutaArchivo,
        
        // Para renombrar archivos
        @Size(max = 255, message = "El nuevo nombre no puede exceder 255 caracteres")
        String nuevoNombre
) {
    
    /**
     * Tipos de operaciones disponibles
     */
    public enum TipoOperacion {
        ELIMINAR,    // Eliminar un archivo
        RENOMBRAR    // Renombrar un archivo
    }
    
    /**
     * Verifica si la operación es de eliminación
     */
    public boolean esEliminacion() {
        return tipo == TipoOperacion.ELIMINAR;
    }
    
    /**
     * Verifica si la operación es de renombrado
     */
    public boolean esRenombrado() {
        return tipo == TipoOperacion.RENOMBRAR;
    }
    
    /**
     * Verifica si la operación es válida
     */
    public boolean esValida() {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            return false;
        }
        
        if (esRenombrado() && (nuevoNombre == null || nuevoNombre.trim().isEmpty())) {
            return false;
        }
        
        return true;
    }
}
