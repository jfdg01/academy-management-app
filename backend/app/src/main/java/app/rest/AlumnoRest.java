package app.rest;

import java.util.List;

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

import app.dtos.DTOActualizarAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOClaseInscrita;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOPerfilAlumno;
import app.dtos.DTOCrearAlumno;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioAlumno;
import app.servicios.ServicioClase;
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
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
@Validated
@Tag(name = "Students", description = "API for student management")
public class AlumnoRest extends BaseRestController {

    private final ServicioAlumno servicioAlumno;
    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // Standard GET collection endpoint with comprehensive filtering and pagination
    @GetMapping
    @Operation(
        summary = "Get paginated students",
        description = "Gets a paginated list of students with optional filters. Students can only see their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of students retrieved successfully",
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
            description = "Access denied - Not authorized to view students"
        )
    })
    public ResponseEntity<DTORespuestaPaginada<DTOAlumno>> obtenerAlumnos(
            @Parameter(description = "Search query", required = false)
            @RequestParam(required = false) @Size(max = 255) String q,
            @Parameter(description = "First name filter", required = false)
            @RequestParam(required = false) @Size(max = 255) String firstName,
            @Parameter(description = "Last name filter", required = false)
            @RequestParam(required = false) @Size(max = 255) String lastName,
            @Parameter(description = "DNI filter", required = false)
            @RequestParam(required = false) @Size(max = 255) String dni,
            @Parameter(description = "Email filter", required = false)
            @RequestParam(required = false) @Size(max = 255) String email,
            @Parameter(description = "Enrollment status filter", required = false)
            @RequestParam(required = false) Boolean enrolled,
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
        sortBy = validateSortBy(sortBy, "id", "firstName", "lastName", "dni", "email", "enrolled", "enabled");
        sortDirection = validateSortDirection(sortDirection);
        
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno(
            q, firstName, lastName, dni, email, enrolled);
        
        DTORespuestaPaginada<DTOAlumno> respuesta = servicioAlumno.buscarAlumnosPorParametrosPaginados(
            parametros, page, size, sortBy, sortDirection);
        
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    // Standard GET specific resource endpoint
    @GetMapping("/{id}")
    @Operation(
        summary = "Get student by ID",
        description = "Gets a specific student by their ID. Students can only see their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this student"
        )
    })
    public ResponseEntity<DTOAlumno> obtenerAlumnoPorId(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoPorId(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    // Standard POST create endpoint
    @PostMapping
    @Operation(
        summary = "Create new student",
        description = "Creates a new student in the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Student created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Student already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role is required"
        )
    })
    public ResponseEntity<DTOAlumno> crearAlumno(
            @Valid @RequestBody DTOCrearAlumno peticion) {
        
        DTOAlumno dtoAlumnoNuevo = servicioAlumno.crearAlumno(peticion);
        return new ResponseEntity<>(dtoAlumnoNuevo, HttpStatus.CREATED);
    }

    // Standard PATCH partial update endpoint with status management
    @PatchMapping("/{id}")
    @Operation(
        summary = "Update student partially",
        description = "Partially updates an existing student. Students can only update their own profile. Administrators can change enrollment and enabled status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this student"
        )
    })
    public ResponseEntity<DTOAlumno> actualizarAlumnoParcial(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Student update data", required = true)
            @Valid @RequestBody DTOActualizarAlumno peticion) {
        
        DTOAlumno dtoAlumnoActualizado = servicioAlumno.actualizarAlumno(id, peticion);
        return new ResponseEntity<>(dtoAlumnoActualizado, HttpStatus.OK);
    }

    // Standard DELETE endpoint
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete student",
        description = "Deletes a student from the system (requires ADMIN role)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Student deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - ADMIN role is required"
        )
    })
    public ResponseEntity<Void> eliminarAlumno(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        servicioAlumno.borrarAlumnoPorId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Standard PUT replace endpoint
    @PutMapping("/{id}")
    @Operation(
        summary = "Replace student",
        description = "Replaces an entire student record. Students can only update their own profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student replaced successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to modify this student"
        )
    })
    public ResponseEntity<DTOAlumno> reemplazarAlumno(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id,
            @Parameter(description = "Complete student data", required = true)
            @Valid @RequestBody DTOActualizarAlumno peticion) {
        
        DTOAlumno dtoAlumnoReemplazado = servicioAlumno.actualizarAlumno(id, peticion);
        return new ResponseEntity<>(dtoAlumnoReemplazado, HttpStatus.OK);
    }

    // Additional endpoints for specific use cases

    @GetMapping("/{id}/perfil")
    @Operation(
        summary = "Get student profile",
        description = "Gets a student's public profile information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student profile retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOPerfilAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        )
    })
    public ResponseEntity<DTOPerfilAlumno> obtenerPerfilAlumno(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        // Get the student first to get their username
        DTOAlumno alumno = servicioAlumno.obtenerAlumnoPorId(id);
        DTOPerfilAlumno perfil = servicioAlumno.obtenerPerfilAlumnoPorUsuario(alumno.username());
        return new ResponseEntity<>(perfil, HttpStatus.OK);
    }

    @GetMapping("/{id}/clases")
    @Operation(
        summary = "Get student's classes",
        description = "Gets all classes that a student is enrolled in"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student's classes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOClaseInscrita.class, type = "array")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this student's classes"
        )
    })
    public ResponseEntity<List<DTOClaseInscrita>> obtenerClasesAlumno(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        List<DTOClaseInscrita> clases = servicioClase.obtenerClasesInscritasConDetalles(id);
        return new ResponseEntity<>(clases, HttpStatus.OK);
    }

    // ===== OPTIMIZED ENTITY GRAPH ENDPOINTS =====

    /**
     * Gets a student with their classes loaded using Entity Graph
     */
    @GetMapping("/{id}/con-clases")
    @Operation(
        summary = "Get student with classes",
        description = "Gets a student with their classes loaded using Entity Graph for optimal performance"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student with classes retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this student"
        )
    })
    public ResponseEntity<DTOAlumno> obtenerAlumnoConClases(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoConClases(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }

    /**
     * Gets a student with all their relationships loaded using Entity Graph
     */
    @GetMapping("/{id}/completo")
    @Operation(
        summary = "Get student with all relationships",
        description = "Gets a student with all their relationships (classes, payments, submissions) loaded using Entity Graph"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Student with all relationships retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTOAlumno.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - Not authorized to view this student"
        )
    })
    public ResponseEntity<DTOAlumno> obtenerAlumnoCompleto(
            @Parameter(description = "ID of the student", required = true)
            @PathVariable @Min(value = 1, message = "The ID must be greater than 0") Long id) {
        
        DTOAlumno dtoAlumno = servicioAlumno.obtenerAlumnoConTodo(id);
        return new ResponseEntity<>(dtoAlumno, HttpStatus.OK);
    }
}
