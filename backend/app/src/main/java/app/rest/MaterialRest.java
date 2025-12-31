package app.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOMaterial;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioMaterial;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for Material management
 * Follows Spring Boot best practices with clean REST API design
 */
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:5173" })
@Validated
@Tag(name = "Materials", description = "API for educational material management")
public class MaterialRest extends BaseRestController {

    private final ServicioMaterial servicioMaterial;

    // === CORE REST OPERATIONS ===

    /**
     * Gets paginated materials with optional filters
     * Follows REST conventions for collection endpoints
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(summary = "Get paginated materials", description = "Gets a paginated list of materials with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list of materials retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTORespuestaPaginada.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not authorized to view materials")
    })
    public ResponseEntity<DTORespuestaPaginada<DTOMaterial>> obtenerMateriales(
            @Parameter(description = "General search term (searches in name and URL)") @RequestParam(required = false) @Size(max = 100) String q,

            @Parameter(description = "Material name to filter by") @RequestParam(required = false) @Size(max = 200) String name,

            @Parameter(description = "Material URL to filter by") @RequestParam(required = false) @Size(max = 500) String url,

            @Parameter(description = "Material type to filter by (DOCUMENT, IMAGE, VIDEO)") @RequestParam(required = false) String type,

            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {

        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "name", "url", "type", "createdAt");
        sortDirection = validateSortDirection(sortDirection);

        DTORespuestaPaginada<DTOMaterial> respuesta = servicioMaterial.buscarPaginado(
                q, name, url, type, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Gets a specific material by ID
     * Follows REST conventions for individual resource endpoints
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(summary = "Get material by ID", description = "Gets a specific material by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOMaterial.class))),
            @ApiResponse(responseCode = "404", description = "Material not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not authorized to view this material")
    })
    public ResponseEntity<DTOMaterial> obtenerMaterial(
            @Parameter(description = "ID of the material", required = true) @PathVariable Long id) {

        DTOMaterial material = servicioMaterial.obtenerPorId(id);
        return ResponseEntity.ok(material);
    }

    /**
     * Creates a new material
     * Follows REST conventions for resource creation
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Create new material", description = "Creates a new material in the system (requires ADMIN or PROFESOR role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Material created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOMaterial.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Conflict - Material already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN or PROFESOR role is required")
    })
    public ResponseEntity<DTOMaterial> crearMaterial(
            @Parameter(description = "Material name", required = true) @RequestParam @NotBlank(message = "El nombre del material no puede estar vacío") @Size(max = 200) String name,
            @Parameter(description = "Material URL", required = true) @RequestParam @NotBlank(message = "La URL del material no puede estar vacía") @Size(max = 500) String url) {

        DTOMaterial materialCreado = servicioMaterial.crear(name, url);
        return ResponseEntity.status(HttpStatus.CREATED).body(materialCreado);
    }

    /**
     * Updates an existing material
     * Follows REST conventions for resource updates
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Update material", description = "Updates an existing material (requires ADMIN or PROFESOR role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOMaterial.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Material not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not authorized to modify this material")
    })
    public ResponseEntity<DTOMaterial> actualizarMaterial(
            @Parameter(description = "ID of the material", required = true) @PathVariable Long id,
            @Parameter(description = "Material name", required = true) @RequestParam @NotBlank(message = "El nombre del material no puede estar vacío") @Size(max = 200) String name,
            @Parameter(description = "Material URL", required = true) @RequestParam @NotBlank(message = "La URL del material no puede estar vacía") @Size(max = 500) String url) {

        DTOMaterial materialActualizado = servicioMaterial.actualizar(id, name, url);
        return ResponseEntity.ok(materialActualizado);
    }

    /**
     * Deletes a material
     * Follows REST conventions for resource deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(summary = "Delete material", description = "Deletes a material from the system (requires ADMIN or PROFESOR role)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOMaterial.class))),
            @ApiResponse(responseCode = "404", description = "Material not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN or PROFESOR role is required")
    })
    public ResponseEntity<DTOMaterial> eliminarMaterial(
            @Parameter(description = "ID of the material", required = true) @PathVariable Long id) {

        DTOMaterial materialEliminado = servicioMaterial.eliminar(id);
        return ResponseEntity.ok(materialEliminado);
    }

    // === SEARCH & FILTERING OPERATIONS ===

    /**
     * Searches materials by term with pagination
     * Provides flexible search functionality with pagination support
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(summary = "Search materials with pagination", description = "Searches materials by name with pagination support or returns all if no search term provided")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated search results retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTORespuestaPaginada.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not authorized to search materials")
    })
    public ResponseEntity<DTORespuestaPaginada<DTOMaterial>> buscarMateriales(
            @Parameter(description = "Search term (optional)") @RequestParam(required = false) @Size(max = 100) String q,

            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {

        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "name", "url", "type", "createdAt");
        sortDirection = validateSortDirection(sortDirection);

        DTORespuestaPaginada<DTOMaterial> respuesta = servicioMaterial.buscarPaginado(
                q, null, null, null, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Gets paginated materials by type
     * Provides type-specific filtering with pagination support
     */
    @GetMapping("/type/{tipo}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(summary = "Get paginated materials by type", description = "Gets paginated materials filtered by type (DOCUMENT, IMAGE, VIDEO, or file extension)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated materials by type retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTORespuestaPaginada.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not authorized to view materials")
    })
    public ResponseEntity<DTORespuestaPaginada<DTOMaterial>> obtenerMaterialesPorTipo(
            @Parameter(description = "Material type (DOCUMENT, IMAGE, VIDEO, or file extension)", required = true) @PathVariable String tipo,

            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,

            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {

        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "name", "url", "type", "createdAt");
        sortDirection = validateSortDirection(sortDirection);

        DTORespuestaPaginada<DTOMaterial> respuesta = servicioMaterial.obtenerPorTipoPaginado(
                tipo, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Counts materials by type
     * Provides statistics functionality
     */
    @GetMapping("/count/type/{tipo}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(summary = "Count materials by type", description = "Counts materials by type (DOCUMENT, IMAGE, VIDEO, or all if not specified)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Not authorized to view material statistics")
    })
    public ResponseEntity<Long> contarMaterialesPorTipo(
            @Parameter(description = "Material type (DOCUMENT, IMAGE, VIDEO, or empty for all)", required = true) @PathVariable String tipo) {

        long count = servicioMaterial.contarPorTipo(tipo);
        return ResponseEntity.ok(count);
    }
}