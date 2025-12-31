package app.entidades;

import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entidad Curso
 * Representa un curso (clase con fecha de inicio y fin)
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("CURSO")
public class Curso extends Clase {
    
    @NotNull
    private LocalDate fechaInicio;
    
    @NotNull
    private LocalDate fechaFin;
    
    public Curso() {
        super();
    }
    
    public Curso(String title, String description, BigDecimal price, 
                EPresencialidad format, String image, EDificultad difficulty,
                LocalDate fechaInicio, LocalDate fechaFin) {
        super(title, description, price, format, image, difficulty);
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        
        // Validación: la fecha de fin debe ser posterior a la de inicio
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }
    
    /**
     * Calcula la duración del curso en días
     * @return Número de días del curso
     */
    public long getDuracionEnDias() {
        return ChronoUnit.DAYS.between(this.fechaInicio, this.fechaFin.plusDays(1));
    }
    
    /**
     * Calcula la duración del curso en semanas
     * @return Número de semanas del curso (redondeado hacia arriba)
     */
    public long getDuracionEnSemanas() {
        long dias = getDuracionEnDias();
        return (dias + 6) / 7; // Redondeo hacia arriba
    }
    
    /**
     * Verifica si el curso está actualmente en progreso
     * @return true si está en progreso, false en caso contrario
     */
    public boolean estaEnProgreso() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }
    
    /**
     * Verifica si el curso ya ha finalizado
     * @return true si ha finalizado, false en caso contrario
     */
    public boolean haFinalizado() {
        return LocalDate.now().isAfter(this.fechaFin);
    }
    
    /**
     * Verifica si el curso aún no ha comenzado
     * @return true si no ha comenzado, false en caso contrario
     */
    public boolean noHaComenzado() {
        return LocalDate.now().isBefore(this.fechaInicio);
    }
    
    /**
     * Calcula el porcentaje de progreso del curso
     * @return Porcentaje de progreso (0-100)
     */
    public double getPorcentajeProgreso() {
        LocalDate hoy = LocalDate.now();
        
        if (noHaComenzado()) {
            return 0.0;
        }
        
        if (haFinalizado()) {
            return 100.0;
        }
        
        long diasTranscurridos = ChronoUnit.DAYS.between(this.fechaInicio, hoy);
        long duracionTotal = getDuracionEnDias();
        
        return (diasTranscurridos * 100.0) / duracionTotal;
    }
    
    /**
     * Setter personalizado para fechaFin que valida que sea posterior a fechaInicio
     */
    public void setFechaFin(LocalDate fechaFin) {
        if (this.fechaInicio != null && fechaFin.isBefore(this.fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        this.fechaFin = fechaFin;
    }
}
