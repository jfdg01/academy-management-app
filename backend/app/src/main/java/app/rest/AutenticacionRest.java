package app.rest;

import app.dtos.DTOPeticionLogin;
import app.dtos.DTORespuestaLogin;
import app.dtos.DTOPeticionRegistro;
import app.servicios.ServicioAutenticacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "API for user authentication and registration")
public class AutenticacionRest {
    
    private final ServicioAutenticacion servicioAutenticacion;
    
    @PostMapping("/login")
    @Operation(
        summary = "Login",
        description = "Authenticates a user with their credentials and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaLogin.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
        )
    })
    public ResponseEntity<DTORespuestaLogin> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody DTOPeticionLogin request) {
        return ResponseEntity.ok(servicioAutenticacion.login(request));
    }
    
    @PostMapping("/registro")
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Registration successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DTORespuestaLogin.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or user already exists"
        )
    })
    public ResponseEntity<DTORespuestaLogin> registro(
            @Parameter(description = "Registration data", required = true)
            @Valid @RequestBody DTOPeticionRegistro request) {
        return ResponseEntity.ok(servicioAutenticacion.registro(request));
    }
    
    @GetMapping("/test")
    @Operation(
        summary = "Test authentication",
        description = "Test endpoint to verify that authentication is working correctly"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication working correctly",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string")
            )
        )
    })
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Authentication working correctly");
    }
} 