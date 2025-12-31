package app.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.dtos.DTOClase;
import app.dtos.DTOPeticionEnrollment;
import app.dtos.DTORespuestaEnrollment;
import app.entidades.Material;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for class element management and enrollment operations
 * Handles management operations like adding/removing professors, exercises, materials, and student enrollments
 */
@RestController
@RequestMapping("/api/clases/{claseId}")
@RequiredArgsConstructor
@Validated
@Tag(name = "Class Management", description = "API for class element management and enrollment operations")
public class ClaseManagementRest {

    private final ServicioClase servicioClase;
    private final SecurityUtils securityUtils;

    // ===== STUDENT ENROLLMENT MANAGEMENT =====

    /**
     * Enrolls a student in a class (for professors and administrators)
     */
    @PostMapping("/students/{studentId}")
    @Operation(summary = "Enroll student in class", description = "Enrolls a student in a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student enrolled successfully"),
            @ApiResponse(responseCode = "400", description = "Enrollment error - Invalid data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    public ResponseEntity<DTORespuestaEnrollment> inscribirAlumnoEnClase(
            @PathVariable Long claseId,
            @PathVariable Long studentId) {

        DTOPeticionEnrollment peticion = new DTOPeticionEnrollment(studentId, claseId);
        DTORespuestaEnrollment respuesta = servicioClase.inscribirAlumnoEnClase(peticion);

        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    /**
     * Unenrolls a student from a class (ADMIN ONLY)
     * Students and teachers cannot unenroll due to payment requirements
     */
    @DeleteMapping("/students/{studentId}")
    @Operation(summary = "Unenroll student from class (Admin only)", description = "Unenrolls a student from a specific class. Only administrators can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student unenrolled successfully"),
            @ApiResponse(responseCode = "400", description = "Unenrollment error - Invalid data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN permissions"),
            @ApiResponse(responseCode = "404", description = "Class or student not found")
    })
    public ResponseEntity<DTORespuestaEnrollment> darDeBajaAlumnoDeClase(
            @PathVariable Long claseId,
            @PathVariable Long studentId) {

        DTOPeticionEnrollment peticion = new DTOPeticionEnrollment(studentId, claseId);
        DTORespuestaEnrollment respuesta = servicioClase.darDeBajaAlumnoDeClase(peticion);

        if (respuesta.success()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    // ===== PROFESSOR MANAGEMENT =====

    /**
     * Adds a professor to a class
     */
    @PostMapping("/professors/{professorId}")
    @Operation(summary = "Add professor to class", description = "Adds a professor to a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Professor added successfully"),
            @ApiResponse(responseCode = "400", description = "Error adding professor - Invalid data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
            @ApiResponse(responseCode = "404", description = "Class or professor not found")
    })
    public ResponseEntity<String> agregarProfesorAClase(
            @PathVariable Long claseId,
            @PathVariable Long professorId) {

        DTOClase resultado = servicioClase.agregarProfesor(claseId, professorId.toString());
        if (resultado != null) {
            return ResponseEntity.ok("Professor added successfully to class");
        } else {
            return ResponseEntity.badRequest().body("Error adding professor to class");
        }
    }

    /**
     * Removes a professor from a class
     */
    @DeleteMapping("/professors/{professorId}")
    @Operation(summary = "Remove professor from class", description = "Removes a professor from a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Professor removed successfully"),
            @ApiResponse(responseCode = "400", description = "Error removing professor - Invalid data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
            @ApiResponse(responseCode = "404", description = "Class or professor not found")
    })
    public ResponseEntity<String> quitarProfesorDeClase(
            @PathVariable Long claseId,
            @PathVariable Long professorId) {

        DTOClase resultado = servicioClase.removerProfesor(claseId, professorId.toString());
        if (resultado != null) {
            return ResponseEntity.ok("Professor removed successfully from class");
        } else {
            return ResponseEntity.badRequest().body("Error removing professor from class");
        }
    }

    // ===== MATERIAL MANAGEMENT =====

    /**
     * Adds material to a class
     */
    @PostMapping("/materials")
    @Operation(summary = "Add material to class", description = "Adds educational material to a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material added successfully"),
            @ApiResponse(responseCode = "400", description = "Error adding material - Invalid data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<String> agregarMaterialAClase(
            @PathVariable Long claseId,
            @Valid @RequestBody Material material) {

        DTOClase resultado = servicioClase.agregarMaterial(claseId, material);
        if (resultado != null) {
            return ResponseEntity.ok("Material added successfully to class");
        } else {
            return ResponseEntity.badRequest().body("Error adding material to class");
        }
    }

    /**
     * Removes material from a class
     */
    @DeleteMapping("/materials/{materialId}")
    @Operation(summary = "Remove material from class", description = "Removes educational material from a specific class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material removed successfully"),
            @ApiResponse(responseCode = "400", description = "Error removing material - Invalid data or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN or PROFESOR permissions"),
            @ApiResponse(responseCode = "404", description = "Class or material not found")
    })
    public ResponseEntity<String> quitarMaterialDeClase(
            @PathVariable Long claseId,
            @PathVariable Long materialId) {

        DTOClase resultado = servicioClase.removerMaterial(claseId, materialId);
        if (resultado != null) {
            return ResponseEntity.ok("Material removed successfully from class");
        } else {
            return ResponseEntity.badRequest().body("Error removing material from class");
        }
    }
}
