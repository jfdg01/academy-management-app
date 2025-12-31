package app.dtos;

/**
 * DTO para el resultado de una operación con archivos
 * Indica si la operación fue exitosa y proporciona información adicional
 */
public record DTOOperacionArchivoResultado(
        DTOOperacionArchivo.TipoOperacion tipo,
        String rutaArchivo,
        String nuevoNombre,
        boolean exitosa,
        String mensaje,
        String error
) {
    
    /**
     * Verifica si la operación fue exitosa
     */
    public boolean fueExitosa() {
        return exitosa;
    }
    
    /**
     * Verifica si la operación falló
     */
    public boolean fallo() {
        return !exitosa;
    }
    
    /**
     * Obtiene el mensaje descriptivo de la operación
     */
    public String getMensajeDescriptivo() {
        if (exitosa) {
            return switch (tipo) {
                case ELIMINAR -> "Archivo eliminado exitosamente: " + rutaArchivo;
                case RENOMBRAR -> "Archivo renombrado exitosamente: " + rutaArchivo + " → " + nuevoNombre;
            };
        } else {
            return "Error en operación " + tipo + ": " + error;
        }
    }
}
