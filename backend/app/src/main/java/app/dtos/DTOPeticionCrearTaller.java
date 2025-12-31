package app.dtos;

import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO para la petición de creación de un taller
 * Contiene los datos básicos necesarios para crear un taller
 */
public record DTOPeticionCrearTaller(
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
        @Min(value = 1, message = "La duración debe ser al menos 1 hora")
        Integer duracionHoras,
        
        @NotNull
        LocalDate fechaRealizacion,
        
        @NotNull
        LocalTime horaComienzo,
        
        List<Long> profesoresId,
        List<Material> material
) {
    
    /**
     * Constructor simplificado sin listas
     */
    public DTOPeticionCrearTaller(String titulo, String descripcion, BigDecimal precio,
                                 EPresencialidad presencialidad, String imagenPortada, EDificultad nivel,
                                 Integer duracionHoras, LocalDate fechaRealizacion, LocalTime horaComienzo) {
        this(titulo, descripcion, precio, presencialidad, imagenPortada, nivel, 
             duracionHoras, fechaRealizacion, horaComienzo, List.of(), List.of());
    }
    
    /**
     * Verifica que la petición sea válida para crear un taller
     * @return true si la petición es válida, false en caso contrario
     */
    public boolean esValido() {
        // Verificaciones básicas de nulidad
        if (titulo == null || titulo.isBlank() || 
            precio == null || precio.compareTo(BigDecimal.ZERO) <= 0 ||
            presencialidad == null || nivel == null ||
            duracionHoras == null || duracionHoras < 1 ||
            fechaRealizacion == null || horaComienzo == null) {
            return false;
        }
        
        // Verificar que la fecha de realización no sea en el pasado
        if (fechaRealizacion.isBefore(LocalDate.now())) {
            return false;
        }
        
        // Verificar que si la fecha es hoy, la hora no sea en el pasado
        if (fechaRealizacion.equals(LocalDate.now()) && horaComienzo.isBefore(LocalTime.now())) {
            return false;
        }
        
        return true;
    }
}