package app.entidades;

import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad Taller
 * Representa un taller (clase de duración específica en una fecha determinada)
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("TALLER")
public class Taller extends Clase {
    
    @NotNull
    @Min(value = 1, message = "La duración debe ser al menos 1 hora")
    private Integer duracionHoras;
    
    @NotNull
    private LocalDate fechaRealizacion;
    
    @NotNull
    private LocalTime horaComienzo;
    
    public Taller() {
        super();
    }
    
    public Taller(String title, String description, BigDecimal price, 
                 EPresencialidad format, String image, EDificultad difficulty,
                 Integer duracionHoras, LocalDate fechaRealizacion, LocalTime horaComienzo) {
        super(title, description, price, format, image, difficulty);
        this.duracionHoras = duracionHoras;
        this.fechaRealizacion = fechaRealizacion;
        this.horaComienzo = horaComienzo;
    }
    
    /**
     * Calcula la hora de finalización del taller
     * @return LocalTime con la hora de finalización
     */
    public LocalTime getHoraFinalizacion() {
        return this.horaComienzo.plusHours(this.duracionHoras);
    }
    
    /**
     * Verifica si el taller está en curso en una fecha y hora dadas
     * @param fecha Fecha a verificar
     * @param hora Hora a verificar
     * @return true si está en curso, false en caso contrario
     */
    public boolean estaEnCurso(LocalDate fecha, LocalTime hora) {
        if (!this.fechaRealizacion.equals(fecha)) {
            return false;
        }
        return hora.isAfter(this.horaComienzo) && hora.isBefore(getHoraFinalizacion());
    }
    
    /**
     * Verifica si el taller ya ha finalizado
     * @return true si ha finalizado, false en caso contrario
     */
    public boolean haFinalizado() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        
        if (this.fechaRealizacion.isBefore(hoy)) {
            return true;
        }
        
        if (this.fechaRealizacion.equals(hoy)) {
            return ahora.isAfter(getHoraFinalizacion());
        }
        
        return false;
    }
}
