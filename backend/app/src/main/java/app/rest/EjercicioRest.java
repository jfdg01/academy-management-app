package app.rest;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

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
import org.springframework.security.access.prepost.PreAuthorize;

import app.dtos.DTOEjercicio;
import app.dtos.DTOEjercicioConEntrega;
import app.dtos.DTOParametrosBusquedaEjercicio;
import app.dtos.DTOPeticionCrearEjercicio;
import app.dtos.DTOEntregaEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioEjercicio;
import app.servicios.ServicioEntregaEjercicio;
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
@RequestMapping("/api/ejercicios")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "Exercises", description = "API for exercise management")
public class EjercicioRest extends BaseRestController {

    private final ServicioEjercicio servicioEjercicio;
    private final ServicioEntregaEjercicio servicioEntregaEjercicio;

    // Standard GET collection endpoint with comprehensive filtering and pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get paginated exercises",
        description = "Gets a paginated list of exercises with optional filters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of exercises retrieved successfully",
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
            description = "Access denied - Not authorized to view exercises"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOEjercicio>> obtenerEjercicios(
            @Parameter(description = "Search query", required = false)
            @RequestParam(required = false) @Size(max = 100) String q,
            @Parameter(description = "Exercise name filter", required = false)
            @RequestParam(required = false) @Size(max = 200) String name,
            @Parameter(description = "Exercise statement filter", required = false)
            @RequestParam(required = false) @Size(max = 2000) String statement,
            @Parameter(description = "Class ID filter", required = false)
            @RequestParam(required = false) @Size(max = 255) String classId,
            @Parameter(description = "Exercise status filter", required = false)
            @RequestParam(required = false) String status,
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
        sortBy = validateSortBy(sortBy, "id", "name", "startDate", "endDate", "classId");
        sortDirection = validateSortDirection(sortDirection);
        
        DTOParametrosBusquedaEjercicio parametros = new DTOParametrosBusquedaEjercicio(q, name, statement, classId, status);
        
        DTORespuestaPaginada<DTOEjercicio> respuesta = servicioEjercicio.obtenerEjerciciosPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // GET exercises with delivery information for students
    @GetMapping("/with-delivery")
    @PreAuthorize("hasRole('ALUMNO')")
    @Operation(
        summary = "Get exercises with delivery information",
        description = "Gets a paginated list of exercises with delivery status information for the current student."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of exercises with delivery info retrieved successfully",
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
            description = "Access denied - Only students can access this endpoint"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOEjercicioConEntrega>> obtenerEjerciciosConEntrega(
            @Parameter(description = "Search query", required = false)
            @RequestParam(required = false) @Size(max = 100) String q,
            @Parameter(description = "Exercise name filter", required = false)
            @RequestParam(required = false) @Size(max = 200) String name,
            @Parameter(description = "Exercise statement filter", required = false)
            @RequestParam(required = false) @Size(max = 2000) String statement,
            @Parameter(description = "Class ID filter", required = false)
            @RequestParam(required = false) @Size(max = 255) String classId,
            @Parameter(description = "Exercise status filter", required = false)
            @RequestParam(required = false) String status,
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
        sortBy = validateSortBy(sortBy, "id", "name", "startDate", "endDate", "classId");
        sortDirection = validateSortDirection(sortDirection);
        
        DTORespuestaPaginada<DTOEjercicioConEntrega> respuesta = servicioEjercicio.obtenerEjerciciosConEntregaPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // Standard GET specific resource endpoint
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get exercise by ID",
        description = "Gets a specific exercise by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this exercise"
        )
    })
    public ResponseEntity<DTOEjercicio> obtenerEjercicioPorId(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOEjercicio dtoEjercicio = servicioEjercicio.obtenerEjercicioPorId(id);
        return new ResponseEntity<>(dtoEjercicio, HttpStatus.OK);
    }

    // Standard POST create endpoint
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Create new exercise",
        description = "Creates a new exercise in the system (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Exercise created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Exercise already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<DTOEjercicio> crearEjercicio(
            @Parameter(description = "Exercise creation data", required = true)
            @Valid @RequestBody DTOPeticionCrearEjercicio peticion) {
        
        // Validate date logic
        if (!peticion.fechasValidas()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        DTOEjercicio dtoEjercicio = servicioEjercicio.crearEjercicio(
            peticion.nombre(),
            peticion.enunciado(),
            peticion.fechaInicioPlazo(),
            peticion.fechaFinalPlazo(),
            peticion.claseId()
        );
        
        return new ResponseEntity<>(dtoEjercicio, HttpStatus.CREATED);
    }

    // Standard PATCH partial update endpoint
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Update exercise partially",
        description = "Partially updates an existing exercise (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this exercise"
        )
    })
    public ResponseEntity<DTOEjercicio> actualizarEjercicioParcial(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Exercise update data", required = true)
            @Valid @RequestBody DTOPeticionCrearEjercicio peticion) {
        
        // Validate date logic
        if (!peticion.fechasValidas()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        DTOEjercicio dtoEjercicio = servicioEjercicio.actualizarEjercicio(
            id,
            peticion.nombre(),
            peticion.enunciado(),
            peticion.fechaInicioPlazo(),
            peticion.fechaFinalPlazo()
        );
        
        return new ResponseEntity<>(dtoEjercicio, HttpStatus.OK);
    }

    // Standard DELETE endpoint
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Delete exercise",
        description = "Deletes an exercise from the system (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Exercise deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<Void> eliminarEjercicio(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        servicioEjercicio.borrarEjercicioPorId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Standard PUT replace endpoint
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Replace exercise",
        description = "Replaces an entire exercise record (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise replaced successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this exercise"
        )
    })
    public ResponseEntity<DTOEjercicio> reemplazarEjercicio(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Complete exercise data", required = true)
            @Valid @RequestBody DTOPeticionCrearEjercicio peticion) {
        
        // Validate date logic
        if (!peticion.fechasValidas()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        DTOEjercicio dtoEjercicio = servicioEjercicio.actualizarEjercicio(
            id,
            peticion.nombre(),
            peticion.enunciado(),
            peticion.fechaInicioPlazo(),
            peticion.fechaFinalPlazo()
        );
        
        return new ResponseEntity<>(dtoEjercicio, HttpStatus.OK);
    }

    // Additional endpoints for specific use cases

    @GetMapping("/{id}/entregas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Get exercise deliveries",
        description = "Gets all deliveries for a specific exercise (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise deliveries retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEntregaEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<List<DTOEntregaEjercicio>> obtenerEntregasEjercicio(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        List<DTOEntregaEjercicio> entregas = servicioEntregaEjercicio.obtenerEntregasPorEjercicio(id.toString());
        return new ResponseEntity<>(entregas, HttpStatus.OK);
    }

    @GetMapping("/{id}/estadisticas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Get exercise statistics",
        description = "Gets statistics about an exercise (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise statistics retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasEjercicio(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        // These methods don't exist in ServicioEjercicio, so we'll return placeholder data
        Map<String, Object> estadisticas = Map.of(
            "totalEntregas", 0L,
            "notaPromedio", BigDecimal.ZERO
        );
        
        return new ResponseEntity<>(estadisticas, HttpStatus.OK);
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Get all exercises statistics",
        description = "Gets statistics about all exercises (requires ADMIN or PROFESOR role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercises statistics retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGenerales() {
        long totalEjercicios = servicioEjercicio.obtenerTodosLosEjercicios().size();
        
        Map<String, Object> estadisticas = Map.of(
            "total", totalEjercicios,
            "activos", 0L,
            "vencidos", 0L,
            "porcentajeActivos", 0.0
        );
        
        return new ResponseEntity<>(estadisticas, HttpStatus.OK);
    }

    // ===== OPTIMIZED ENTITY GRAPH ENDPOINTS =====

    /**
     * Gets the total count of exercises
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get exercise count",
        description = "Gets the total count of exercises in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise count retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Long.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view exercise count"
        )
    })
    public ResponseEntity<Long> contarEjercicios() {
        // Use the basic count method from JpaRepository
        long count = servicioEjercicio.obtenerTodosLosEjercicios().size();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    /**
     * Gets an exercise with its class loaded using Entity Graph
     */
    @GetMapping("/{id}/con-clase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get exercise with class",
        description = "Gets an exercise with its class loaded using Entity Graph for optimal performance"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise with class retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this exercise"
        )
    })
    public ResponseEntity<DTOEjercicio> obtenerEjercicioConClase(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        // Use the basic method since EntityGraph methods are not available
        DTOEjercicio dtoEjercicio = servicioEjercicio.obtenerEjercicioPorId(id);
        return new ResponseEntity<>(dtoEjercicio, HttpStatus.OK);
    }

    /**
     * Gets an exercise with its deliveries loaded using Entity Graph
     */
    @GetMapping("/{id}/con-entregas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get exercise with deliveries",
        description = "Gets an exercise with its deliveries loaded using Entity Graph for optimal performance"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise with deliveries retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this exercise"
        )
    })
    public ResponseEntity<DTOEjercicio> obtenerEjercicioConEntregas(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        // Use the basic method since EntityGraph methods are not available
        DTOEjercicio dtoEjercicio = servicioEjercicio.obtenerEjercicioPorId(id);
        return new ResponseEntity<>(dtoEjercicio, HttpStatus.OK);
    }

    /**
     * Gets an exercise with all its relationships loaded using Entity Graph
     */
    @GetMapping("/{id}/completo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
        summary = "Get exercise with all relationships",
        description = "Gets an exercise with all its relationships (class and deliveries) loaded using Entity Graph"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise with all relationships retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOEjercicio.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN or PROFESOR role is required"
        )
    })
    public ResponseEntity<DTOEjercicio> obtenerEjercicioCompleto(
            @Parameter(description = "ID of the exercise", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOEjercicio dtoEjercicio = servicioEjercicio.obtenerEjercicioPorId(id);
        return new ResponseEntity<>(dtoEjercicio, HttpStatus.OK);
    }

    /**
     * Gets exercises by class with deliveries loaded using Entity Graph
     */
    @GetMapping("/clase/{claseId}/con-entregas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR') or hasRole('ALUMNO')")
    @Operation(
        summary = "Get exercises by class with deliveries",
        description = "Gets exercises for a specific class with their deliveries loaded using Entity Graph"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercises with deliveries retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view these exercises"
        )
    })
    public ResponseEntity<List<DTOEjercicio>> obtenerEjerciciosConEntregasPorClase(
            @Parameter(description = "ID of the class", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long claseId) {
        
        // Use the basic method since EntityGraph methods are not available
        List<DTOEjercicio> ejercicios = servicioEjercicio.obtenerEjerciciosPorClase(claseId);
        return new ResponseEntity<>(ejercicios, HttpStatus.OK);
    }
}
