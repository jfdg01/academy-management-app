package app.dtos;

public record DTOParametrosBusquedaAlumno(
    String q,              // General search term across multiple fields
    String firstName,
    String lastName,
    String dni,
    String email,
    Boolean enrolled
) {
    /**
     * Constructor with default values for backward compatibility
     */
    public DTOParametrosBusquedaAlumno(String firstName, String lastName, String dni, String email, Boolean enrolled) {
        this(null, firstName, lastName, dni, email, enrolled);
    }
    
    /**
     * Default constructor for backward compatibility
     */
    public DTOParametrosBusquedaAlumno() {
        this(null, null, null, null, null, null);
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
        return firstName != null || lastName != null || dni != null || 
               email != null || enrolled != null;
    }
    
    /**
     * Checks if all parameters are empty (for backward compatibility)
     * @return true if no search criteria are provided
     */
    public boolean isEmpty() {
        return !hasGeneralSearch() && !hasSpecificFilters();
    }
}
