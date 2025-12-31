package app.dtos;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic paginated response")
public record DTORespuestaPaginada<T>(
    @Schema(description = "List of elements for the current page", required = true)
    List<T> content,
    
    @Schema(description = "Current page number (0-indexed)", example = "0", required = true)
    int page,
    
    @Schema(description = "Page size", example = "20", required = true)
    int size,
    
    @Schema(description = "Total number of elements", example = "150", required = true)
    long totalElements,
    
    @Schema(description = "Total number of pages", example = "8", required = true)
    int totalPages,
    
    @Schema(description = "Indicates if this is the first page", example = "true", required = true)
    boolean isFirst,
    
    @Schema(description = "Indicates if this is the last page", example = "false", required = true)
    boolean isLast,
    
    @Schema(description = "Indicates if the page has content", example = "true", required = true)
    boolean hasContent,
    
    @Schema(description = "Field by which results are sorted", example = "id", required = true)
    String sortBy,
    
    @Schema(description = "Sort direction", example = "ASC", required = true)
    String sortDirection
) {
    public static <T> DTORespuestaPaginada<T> of(
            List<T> content,
            int page,
            int size,
            long totalElements,
            String sortBy,
            String sortDirection) {
        
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isFirst = page == 0;
        boolean isLast = page >= totalPages - 1;
        boolean hasContent = !content.isEmpty();
        
        return new DTORespuestaPaginada<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                isFirst,
                isLast,
                hasContent,
                sortBy,
                sortDirection
        );
    }
    
    public static <T> DTORespuestaPaginada<T> fromPage(org.springframework.data.domain.Page<T> page, String sortBy, String sortDirection) {
        return new DTORespuestaPaginada<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasContent(),
                sortBy,
                sortDirection
        );
    }
}
