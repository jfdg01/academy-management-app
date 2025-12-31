package app.dtos;

import app.entidades.Ejercicio;

import java.time.LocalDateTime;

/**
 * DTO para la entidad Ejercicio
 * Contiene información del ejercicio sin relaciones complejas
 */
public record DTOEjercicio(
        Long id,
        String name,
        String statement,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String classId,
        int numeroEntregas,
        long entregasCalificadas
) {
    
    /**
     * Constructor que crea un DTO desde una entidad Ejercicio
     */
    public DTOEjercicio(Ejercicio ejercicio) {
        this(
                ejercicio.getId(),
                ejercicio.getName(),
                ejercicio.getStatement(),
                ejercicio.getStartDate(),
                ejercicio.getEndDate(),
                ejercicio.getClase() != null ? ejercicio.getClase().getId().toString() : null,
                ejercicio.contarEntregas(),
                ejercicio.contarEntregasCalificadas()
        );
    }
    
    /**
     * metodo estático para crear desde entidad
     */
    public static DTOEjercicio from(Ejercicio ejercicio) {
        return new DTOEjercicio(ejercicio);
    }
    
    /**
     * Verifica si el ejercicio está actualmente disponible para entrega
     */
    public boolean estaEnPlazo() {
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(this.startDate) && !ahora.isAfter(this.endDate);
    }
    
    /**
     * Verifica si el plazo del ejercicio ya ha vencido
     */
    public boolean haVencido() {
        return LocalDateTime.now().isAfter(this.endDate);
    }
    
    /**
     * Verifica si el ejercicio aún no ha comenzado
     */
    public boolean noHaComenzado() {
        return LocalDateTime.now().isBefore(this.startDate);
    }
    
    /**
     * Calcula el porcentaje de entregas calificadas
     */
    public double getPorcentajeEntregasCalificadas() {
        if (this.numeroEntregas == 0) {
            return 0.0;
        }
        return (this.entregasCalificadas * 100.0) / this.numeroEntregas;
    }
    
    /**
     * Verifica si hay entregas pendientes de calificar
     */
    public boolean tieneEntregasPendientes() {
        return this.entregasCalificadas < this.numeroEntregas;
    }
    
    /**
     * Obtiene el estado del ejercicio
     */
    public String getEstado() {
        if (noHaComenzado()) {
            return "PENDIENTE";
        } else if (estaEnPlazo()) {
            return "ACTIVO";
        } else {
            return "VENCIDO";
        }
    }
    
    /**
     * Calcula las horas restantes para la entrega
     */
    public long getHorasRestantes() {
        if (haVencido()) {
            return 0;
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        return java.time.Duration.between(ahora, this.endDate).toHours();
    }
    
    /**
     * Verifica si el ejercicio está próximo a vencer (menos de 24 horas)
     */
    public boolean esUrgente() {
        return estaEnPlazo() && getHorasRestantes() <= 24;
    }
}
