package app.dtos;

/**
 * DTO for professor search parameters
 * Allows searches with multiple criteria
 */
public record DTOParametrosBusquedaProfesor(
        String q,              // General search term across multiple fields
        String firstName,
        String lastName,
        String email,
        String username,
        String dni,
        Boolean enabled,
        String classId,
        Boolean hasNoClasses
) {
    
    /**
     * Constructor with default values for backward compatibility
     */
    public DTOParametrosBusquedaProfesor(String firstName, String lastName, String email, String username, String dni, Boolean enabled, String classId, Boolean hasNoClasses) {
        this(null, firstName, lastName, email, username, dni, enabled, classId, hasNoClasses);
    }
    
    /**
     * Default constructor with all parameters null
     */
    public DTOParametrosBusquedaProfesor() {
        this(null, null, null, null, null, null, null, null, null);
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
        return firstName != null || lastName != null || email != null || 
               username != null || dni != null || enabled != null || 
               classId != null || hasNoClasses != null;
    }
    
    /**
     * Checks if all parameters are empty
     * @return true if no search criteria are provided
     */
    public boolean isEmpty() {
        return !hasGeneralSearch() && !hasSpecificFilters();
    }
    
    /**
     * Checks if there are text criteria for search
     * @return true if there is at least one text criterion
     */
    public boolean hasTextCriteria() {
        return hasGeneralSearch() || firstName != null || lastName != null || email != null || 
               username != null || dni != null;
    }
}
