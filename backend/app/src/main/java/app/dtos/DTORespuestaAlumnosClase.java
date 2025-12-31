package app.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Specific DTO for class student responses
 * Can contain complete information (DTOAlumno) or public information (DTOAlumnoPublico)
 * depending on the user's access level
 */
@Schema(description = "Paginated response of class students with different information levels according to user role")
public record DTORespuestaAlumnosClase(
    @Schema(description = "List of students (complete or public information according to role)")
    List<Object> content,
    
    @Schema(description = "Pagination metadata")
    DTOMetadatosPaginacion page,
    
    @Schema(description = "Type of information returned: 'COMPLETE' for admin/class professor, 'PUBLIC' for others")
    String informationType
) {
    
    /**
     * Constructor that converts a Spring Data Page to DTORespuestaAlumnosClase
     * @param page Page with students (DTOAlumno or DTOAlumnoPublico)
     * @param informationType Type of information returned
     */
    public DTORespuestaAlumnosClase(Page<?> page, String informationType) {
        this(
            (List<Object>) page.getContent(),
            new DTOMetadatosPaginacion(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
            ),
            informationType
        );
    }
    
    /**
     * Record for pagination metadata
     */
    public record DTOMetadatosPaginacion(
        @Schema(description = "Current page number (0-indexed)")
        int number,
        
        @Schema(description = "Page size")
        int size,
        
        @Schema(description = "Total elements across all pages")
        long totalElements,
        
        @Schema(description = "Total pages")
        int totalPages,
        
        @Schema(description = "Indicates if this is the first page")
        boolean first,
        
        @Schema(description = "Indicates if this is the last page")
        boolean last,
        
        @Schema(description = "Indicates if there is a next page")
        boolean hasNext,
        
        @Schema(description = "Indicates if there is a previous page")
        boolean hasPrevious
    ) {}
    
    /**
     * Constants for information type
     */
    public static final String TIPO_COMPLETA = "COMPLETE";
    public static final String TIPO_PUBLICA = "PUBLIC";
}
