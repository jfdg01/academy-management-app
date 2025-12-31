package app.dtos;

import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la petición de creación de una clase
 * Contiene los datos básicos necesarios para crear una clase
 */
public record DTOPeticionCrearClase(
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
        
        List<Long> profesoresId,
        List<Material> material
) {
    
    /**
     * Constructor por defecto con listas vacías
     */
    public DTOPeticionCrearClase(String titulo, String descripcion, BigDecimal precio,
                                EPresencialidad presencialidad, String imagenPortada, EDificultad nivel) {
        this(titulo, descripcion, precio, presencialidad, imagenPortada, nivel, List.of(), List.of());
    }
}
