package app.dtos;

import java.math.BigDecimal;

import app.entidades.enums.EDificultad;
import app.entidades.enums.EPresencialidad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para parámetros de búsqueda de clases
 * Contiene filtros y criterios de ordenación
 */
public record DTOParametrosBusquedaClase(
        // General search term
        String q,                    // General search term across title and description
        
        // Filtros de búsqueda
        String titulo,
        String descripcion,
        EPresencialidad presencialidad,
        EDificultad nivel,
        BigDecimal precioMinimo,
        BigDecimal precioMaximo,
        Boolean soloConPlazasDisponibles,
        Boolean soloProximas,
        
        // Parámetros de paginación
        @NotNull
        @Min(value = 0, message = "La página debe ser >= 0")
        Integer page,
        
        @NotNull
        @Min(value = 1, message = "El tamaño de página debe ser >= 1")
        Integer size,
        
        // Parámetros de ordenación
        @NotNull
        @Size(min = 2, message = "El campo de ordenación no puede estar vacío")
        String sortBy,
        
        @NotNull
        String sortDirection
) {
    /**
     * Constructor with default values for backward compatibility
     */
    public DTOParametrosBusquedaClase(String titulo, String descripcion, EPresencialidad presencialidad,
                                      EDificultad nivel, BigDecimal precioMinimo, BigDecimal precioMaximo,
                                      Boolean soloConPlazasDisponibles, Boolean soloProximas) {
        this(null, titulo, descripcion, presencialidad, nivel, precioMinimo, precioMaximo,
             soloConPlazasDisponibles, soloProximas, 0, 10, "id", "ASC");
    }
    
    /**
     * Constructor with values for pagination and ordering
     */
    public DTOParametrosBusquedaClase(String titulo, String descripcion, EPresencialidad presencialidad,
                                      EDificultad nivel, BigDecimal precioMinimo, BigDecimal precioMaximo,
                                      Boolean soloConPlazasDisponibles, Boolean soloProximas,
                                      Integer page, Integer size, String sortBy, String sortDirection) {
        this(null, titulo, descripcion, presencialidad, nivel, precioMinimo, precioMaximo,
             soloConPlazasDisponibles, soloProximas, page, size, sortBy, sortDirection);
    }
    
    /**
     * Constructor con valores mínimos requeridos para paginación
     */
    public DTOParametrosBusquedaClase(Integer page, Integer size) {
        this(null, null, null, null, null, null, null, null,
             page, size, "id", "ASC");
    }
    
    /**
     * Constructor con valores mínimos requeridos para paginación y ordenación
     */
    public DTOParametrosBusquedaClase(Integer page, Integer size, String sortBy, String sortDirection) {
        this(null, null, null, null, null, null, null, null,
             page, size, sortBy, sortDirection);
    }
    
    /**
     * Constructor por defecto para casos donde no se necesitan parámetros específicos
     */
    public DTOParametrosBusquedaClase() {
        this(null, null, null, null, null, null, null, null,
             0, 10, "id", "ASC");
    }
    
    /**
     * Checks if general search should be used
     * @return true if general search term is provided
     */
    public boolean hasGeneralSearch() {
        return q != null && !q.trim().isEmpty();
    }
    
    /**
     * Checks if specific filters are provided
     * @return true if any specific filter is provided
     */
    public boolean hasSpecificFilters() {
        return titulo != null || descripcion != null || presencialidad != null || 
               nivel != null || precioMinimo != null || precioMaximo != null || 
               soloConPlazasDisponibles != null || soloProximas != null;
    }
    
    /**
     * Checks if all search parameters are empty
     * @return true if no search criteria are provided
     */
    public boolean estaVacio() {
        return !hasGeneralSearch() && !hasSpecificFilters();
    }
}
