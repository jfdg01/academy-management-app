package app.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@Tag(name = "Pruebas", description = "API de pruebas para verificar autenticación y autorización")
public class TestRest {

    @GetMapping("/public")
    @Operation(
        summary = "Endpoint público",
        description = "Endpoint de prueba que no requiere autenticación"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Acceso público exitoso",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string")
            )
        )
    })
    public ResponseEntity<String> publico() {
        return ResponseEntity.ok("Este endpoint es público - no requiere autenticación");
    }

    @GetMapping("/protected")
    @Operation(
        summary = "Endpoint protegido",
        description = "Endpoint de prueba que requiere autenticación de usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Acceso protegido exitoso",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado"
        )
    })
    public ResponseEntity<String> usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return ResponseEntity.ok("Endpoint protegido - Usuario autenticado: " + username);
    }

    @GetMapping("/admin")
    @Operation(
        summary = "Endpoint de administrador",
        description = "Endpoint de prueba que requiere rol de administrador"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Acceso de administrador exitoso",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Se requiere rol ADMIN"
        )
    })
    public ResponseEntity<String> admin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            return ResponseEntity.ok("Endpoint de admin - Usuario: " + username);
        } else {
            return ResponseEntity.status(403).body("Acceso denegado - Se requiere rol ADMIN");
        }
    }

    @GetMapping("/user-info")
    @Operation(
        summary = "Información del usuario",
        description = "Obtiene información detallada del usuario autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Información del usuario obtenida",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado"
        )
    })
    public ResponseEntity<String> obtenerInfoUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String authorities = auth.getAuthorities().toString();
        
        return ResponseEntity.ok(String.format(
                "Información del usuario:\nUsername: %s\nRoles: %s",
                username, authorities
        ));
    }
} 