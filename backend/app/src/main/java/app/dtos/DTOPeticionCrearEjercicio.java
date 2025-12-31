package app.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para la petición de creación de un ejercicio
 * Contiene los datos necesarios para crear un ejercicio
 */
public record DTOPeticionCrearEjercicio(
        @NotNull
        @Size(max = 200)
        String nombre,
        
        @NotNull
        @Size(max = 2000)
        String enunciado,
        
        @NotNull
        LocalDateTime fechaInicioPlazo,
        
        @NotNull
        LocalDateTime fechaFinalPlazo,
        
        @NotNull
        Long claseId
) {
    
    /**
     * Valida que la fecha final sea posterior a la inicial
     */
    public boolean fechasValidas() {
        return fechaFinalPlazo.isAfter(fechaInicioPlazo);
    }
    
    /**
     * Verifica si el ejercicio comenzará en el futuro
     */
    public boolean esFuturo() {
        return fechaInicioPlazo.isAfter(LocalDateTime.now());
    }
    
    /**
     * Calcula la duración del plazo en horas
     */
    public long getDuracionEnHoras() {
        return java.time.Duration.between(fechaInicioPlazo, fechaFinalPlazo).toHours();
    }
}
