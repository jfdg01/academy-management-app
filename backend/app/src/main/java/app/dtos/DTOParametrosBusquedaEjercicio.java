// LLM_EDIT_TIMESTAMP: 25 ago. 14:08
package app.dtos;

import jakarta.validation.constraints.Size;

/**
 * DTO para los parámetros de búsqueda de ejercicios
 * Contiene los filtros disponibles para buscar ejercicios
 */
public record DTOParametrosBusquedaEjercicio(
        @Size(max = 100) String q,                    // Búsqueda general (nombre y enunciado)
        @Size(max = 200) String name,                 // Filtro por nombre
        @Size(max = 2000) String statement,           // Filtro por enunciado
        @Size(max = 255) String classId,              // Filtro por ID de clase
        String status                                  // Filtro por estado (ACTIVE, EXPIRED, FUTURE, WITH_DELIVERIES, WITHOUT_DELIVERIES)
) {
    
    /**
     * Verifica si hay algún filtro activo
     */
    public boolean tieneFiltros() {
        return (q != null && !q.trim().isEmpty()) ||
               (name != null && !name.trim().isEmpty()) ||
               (statement != null && !statement.trim().isEmpty()) ||
               (classId != null && !classId.trim().isEmpty()) ||
               (status != null && !status.trim().isEmpty());
    }
    
    /**
     * Verifica si el filtro de estado es válido
     */
    public boolean estadoValido() {
        if (status == null || status.trim().isEmpty()) {
            return true;
        }
        
        String statusUpper = status.toUpperCase();
        return statusUpper.equals("ACTIVE") ||
               statusUpper.equals("EXPIRED") ||
               statusUpper.equals("FUTURE") ||
               statusUpper.equals("WITH_DELIVERIES") ||
               statusUpper.equals("WITHOUT_DELIVERIES");
    }
    
    /**
     * Obtiene el estado normalizado (en mayúsculas)
     */
    public String getStatusNormalizado() {
        return status != null ? status.toUpperCase() : null;
    }
}
