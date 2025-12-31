package app.dtos;

import jakarta.validation.constraints.Size;

public record DTOParametrosBusqueda(
    @Size(max = 100) String info,
    @Size(max = 100) String otraInfo
) {
} 