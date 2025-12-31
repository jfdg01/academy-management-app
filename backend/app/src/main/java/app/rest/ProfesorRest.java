package app.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOActualizacionProfesor;
import app.dtos.DTOParametrosBusquedaProfesor;
import app.dtos.DTOPeticionRegistroProfesor;
import app.dtos.DTOProfesor;
import app.dtos.DTOProfesorPublico;
import app.dtos.DTORespuestaPaginada;
import app.dtos.DTOClase;
import app.servicios.ServicioClase;
import app.servicios.ServicioProfesor;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profesores")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "Professors", description = "API for professor management")
public class ProfesorRest extends BaseRestController {

    private final ServicioProfesor servicioProfesor;
    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // Standard GET collection endpoint with comprehensive filtering and pagination
    @GetMapping
    @Operation(
        summary = "Get paginated professors",
        description = "Gets a paginated list of professors with optional filters. Professors can only see their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of professors retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaPaginada.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view these professors"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOProfesor>> obtenerProfesores(
            @Parameter(description = "General search term (searches in firstName, lastName, email, username, dni)", required = false)
            @RequestParam(required = false) @Size(max = 100) String q,
            @Parameter(description = "Professor's first name to filter by", required = false)
            @RequestParam(required = false) @Size(max = 100) String firstName,
            @Parameter(description = "Professor's last name to filter by", required = false)
            @RequestParam(required = false) @Size(max = 100) String lastName,
            @Parameter(description = "Professor's email to filter by", required = false)
            @RequestParam(required = false) String email,
            @Parameter(description = "Professor's username to filter by", required = false)
            @RequestParam(required = false) @Size(max = 50) String username,
            @Parameter(description = "Professor's DNI to filter by", required = false)
            @RequestParam(required = false) @Size(max = 20) String dni,
            @Parameter(description = "Account enabled status to filter by", required = false)
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Class ID to filter by", required = false)
            @RequestParam(required = false) String claseId,
            @Parameter(description = "If true, searches for professors without assigned classes", required = false)
            @RequestParam(required = false) Boolean sinClases,
            @Parameter(description = "Page number (0-indexed)", required = false)
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by", required = false)
            @RequestParam(defaultValue = "id") @Size(max = 50) String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)", required = false)
            @RequestParam(defaultValue = "ASC") @Pattern(regexp = "(?i)^(ASC|DESC)$") String sortDirection) {
        
        // Validate and standardize parameters using BaseRestController
        page = validatePageNumber(page);
        size = validatePageSize(size);
        sortBy = validateSortBy(sortBy, "id", "firstName", "lastName", "email", "username", "dni", "enabled");
        sortDirection = validateSortDirection(sortDirection);
        
        DTOParametrosBusquedaProfesor parametros = new DTOParametrosBusquedaProfesor(
            q, firstName, lastName, email, username, dni, enabled, claseId, sinClases);
        
        DTORespuestaPaginada<DTOProfesor> respuesta = servicioProfesor.buscarProfesoresPorParametrosPaginados(
            parametros, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // Standard GET specific resource endpoint
    @GetMapping("/{id}")
    @Operation(
        summary = "Get professor by ID",
        description = "Gets a specific professor by their ID. Professors can only see their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this professor"
        )
    })
    public ResponseEntity<DTOProfesor> obtenerProfesorPorId(
            @Parameter(description = "ID of the professor", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOProfesor dtoProfesor = servicioProfesor.obtenerProfesorPorId(id);
        return new ResponseEntity<>(dtoProfesor, HttpStatus.OK);
    }

    // Standard POST create endpoint
    @PostMapping
    @Operation(
        summary = "Create new professor",
        description = "Creates a new professor in the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Professor created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Professor already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role is required"
        )
    })
    public ResponseEntity<DTOProfesor> crearProfesor(
            @Valid @RequestBody DTOPeticionRegistroProfesor peticion) {
        
        DTOProfesor dtoProfesorNuevo = servicioProfesor.crearProfesor(peticion);
        return new ResponseEntity<>(dtoProfesorNuevo, HttpStatus.CREATED);
    }

    // Standard PATCH partial update endpoint
    @PatchMapping("/{id}")
    @Operation(
        summary = "Update professor partially",
        description = "Partially updates an existing professor. Professors can only update their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this professor"
        )
    })
    public ResponseEntity<DTOProfesor> actualizarProfesorParcial(
            @Parameter(description = "ID of the professor", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Professor update data", required = true)
            @Valid @RequestBody DTOActualizacionProfesor peticion) {
        
        DTOProfesor dtoProfesorActualizado = servicioProfesor.actualizarProfesor(id, peticion);
        return new ResponseEntity<>(dtoProfesorActualizado, HttpStatus.OK);
    }

    // Standard DELETE endpoint
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete professor",
        description = "Deletes a professor from the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Professor deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role is required"
        )
    })
    public ResponseEntity<Void> eliminarProfesor(
            @Parameter(description = "ID of the professor", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        servicioProfesor.borrarProfesorPorId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Standard PUT replace endpoint
    @PutMapping("/{id}")
    @Operation(
        summary = "Replace professor",
        description = "Replaces an entire professor record. Professors can only update their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor replaced successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this professor"
        )
    })
    public ResponseEntity<DTOProfesor> reemplazarProfesor(
            @Parameter(description = "ID of the professor", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Complete professor data", required = true)
            @Valid @RequestBody DTOActualizacionProfesor peticion) {
        
        DTOProfesor dtoProfesorReemplazado = servicioProfesor.actualizarProfesor(id, peticion);
        return new ResponseEntity<>(dtoProfesorReemplazado, HttpStatus.OK);
    }

    // Additional endpoints for specific use cases

    @GetMapping("/{id}/perfil")
    @Operation(
        summary = "Get professor profile",
        description = "Gets a professor's public profile information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor profile retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesorPublico.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        )
    })
    public ResponseEntity<DTOProfesorPublico> obtenerPerfilProfesor(
            @Parameter(description = "ID of the professor", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        // Get the professor first to get their public profile
        DTOProfesor profesor = servicioProfesor.obtenerProfesorPorId(id);
        // We need to get the actual entity to create the public DTO
        // For now, return the regular professor DTO as public profile
        DTOProfesorPublico perfil = new DTOProfesorPublico(
            profesor.id(), profesor.firstName(), profesor.lastName(), 
            profesor.email(), profesor.phoneNumber(), profesor.enabled(), 
            profesor.classIds() != null ? profesor.classIds().size() : 0, profesor.createdAt());
        return new ResponseEntity<>(perfil, HttpStatus.OK);
    }

    @GetMapping("/{id}/clases")
    @Operation(
        summary = "Get professor's classes",
        description = "Gets all classes that a professor teaches"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor's classes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClase.class, type = "array")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this professor's classes"
        )
    })
    public ResponseEntity<List<DTOClase>> obtenerClasesProfesor(
            @Parameter(description = "ID of the professor", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        List<DTOClase> clases = servicioProfesor.obtenerClasesPorProfesor(id);
        return new ResponseEntity<>(clases, HttpStatus.OK);
    }

    @GetMapping("/estadisticas")
    @Operation(
        summary = "Get professor statistics",
        description = "Gets statistics about professors (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        long totalProfesores = servicioProfesor.contarTotalProfesores();
        long profesoresActivos = servicioProfesor.contarProfesoresHabilitados();
        long profesoresDeshabilitados = servicioProfesor.contarProfesoresDeshabilitados();
        
        Map<String, Object> estadisticas = Map.of(
            "total", totalProfesores,
            "activos", profesoresActivos,
            "deshabilitados", profesoresDeshabilitados,
            "porcentajeActivos", totalProfesores > 0 ? (profesoresActivos * 100.0) / totalProfesores : 0.0
        );
        
        return new ResponseEntity<>(estadisticas, HttpStatus.OK);
    }

    // ===== OPTIMIZED ENTITY GRAPH ENDPOINTS =====

    /**
     * Gets a professor with their classes loaded using Entity Graph
     */
    @GetMapping("/{id}/con-clases")
    @Operation(
        summary = "Get professor with classes",
        description = "Gets a professor with their classes loaded using Entity Graph for optimal performance"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Professor with classes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOProfesor.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Professor not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this professor"
        )
    })
    public ResponseEntity<DTOProfesor> obtenerProfesorConClases(
            @Parameter(description = "ID of the professor", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOProfesor dtoProfesor = servicioProfesor.obtenerProfesorConClases(id);
        return new ResponseEntity<>(dtoProfesor, HttpStatus.OK);
    }
}
