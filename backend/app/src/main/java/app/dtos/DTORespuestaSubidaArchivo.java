package app.dtos;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de subida de archivos
 */
public record DTORespuestaSubidaArchivo(
        String nombreOriginal,
        String nombreGuardado,
        String rutaRelativa,
        String tipoMime,
        Long tamanoBytes,
        String tamanoFormateado,
        LocalDateTime fechaSubida,
        String extension,
        boolean esImagen,
        boolean esPdf
) {
    
    /**
     * Constructor que calcula automáticamente las propiedades derivadas
     */
    public DTORespuestaSubidaArchivo {
        if (nombreOriginal == null) {
            throw new IllegalArgumentException("El nombre original no puede ser null");
        }
        if (rutaRelativa == null) {
            throw new IllegalArgumentException("La ruta relativa no puede ser null");
        }
        if (fechaSubida == null) {
            fechaSubida = LocalDateTime.now();
        }
        
        // Calcular extensión
        extension = calcularExtension(nombreOriginal);
        
        // Determinar tipo de archivo
        esImagen = "png".equalsIgnoreCase(extension);
        esPdf = "pdf".equalsIgnoreCase(extension);
        
        // Formatear tamaño si no se proporciona
        if (tamanoFormateado == null && tamanoBytes != null) {
            tamanoFormateado = formatearTamano(tamanoBytes);
        }
    }
    
    /**
     * Calcula la extensión del archivo
     */
    private static String calcularExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Formatea el tamaño del archivo para mostrar
     */
    private static String formatearTamano(Long bytes) {
        if (bytes == null) return "0 B";
        
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Verifica si el archivo es de un tipo válido para entregas
     */
    public boolean esTipoValido() {
        return esImagen || esPdf;
    }
    
    /**
     * Obtiene el tipo de archivo como string
     */
    public String getTipoArchivo() {
        if (esImagen) return "IMAGEN";
        if (esPdf) return "PDF";
        return "OTRO";
    }
}
