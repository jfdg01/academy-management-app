package app.dtos;

import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para la petición de creación de un curso
 * Contiene los datos básicos necesarios para crear un curso
 */
public record DTOPeticionCrearCurso(
        @NotNull
        @Size(max = 200)
        String titulo,
        
        @Size(max = 1000)
        String descripcion,
        
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal precio,
        
        @NotNull
        EPresencialidad presencialidad,
        
        @Size(max = 500)
        String imagenPortada,
        
        @NotNull
        EDificultad nivel,
        
        @NotNull
        @FutureOrPresent(message = "La fecha de inicio debe ser hoy o en el futuro")
        LocalDate fechaInicio,
        
        @NotNull
        @Future(message = "La fecha de fin debe ser en el futuro")
        LocalDate fechaFin,
        
        List<Long> profesoresId,
        List<Material> material
) {
    
    /**
     * Constructor simplificado sin listas
     */
    public DTOPeticionCrearCurso(String titulo, String descripcion, BigDecimal precio,
                                EPresencialidad presencialidad, String imagenPortada, EDificultad nivel,
                                LocalDate fechaInicio, LocalDate fechaFin) {
        this(titulo, descripcion, precio, presencialidad, imagenPortada, nivel, 
             fechaInicio, fechaFin, List.of(), List.of());
    }
    
    /**
     * Verifica que la petición sea válida para crear un curso
     * @return true si la petición es válida, false en caso contrario
     */
    public boolean esValido() {
        // Verificaciones básicas de nulidad
        if (titulo == null || titulo.isBlank() || 
            precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 ||
            presencialidad == null || nivel == null ||
            fechaInicio == null || fechaFin == null) {
            return false;
        }
        
        // Verificar fechas de inicio y fin
        if (fechaInicio.isAfter(fechaFin) || fechaInicio.isBefore(LocalDate.now())) {
            return false;
        }
        
        return true;
    }
}