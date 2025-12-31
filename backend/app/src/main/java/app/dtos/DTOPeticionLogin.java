package app.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request data")
public record DTOPeticionLogin(
    @Schema(description = "Username", example = "user123", required = true)
    @NotBlank(message = "Username is required")
    String username,
    
    @Schema(description = "User password", example = "password123", required = true)
    @NotBlank(message = "Password is required")
    String password
) {} 