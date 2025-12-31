package app.dtos;

import app.entidades.Ejercicio;
import app.entidades.enums.EEstadoEjercicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la entidad Ejercicio con información de entrega del usuario actual
 * Extiende DTOEjercicio para incluir el estado de entrega del estudiante
 */
public record DTOEjercicioConEntrega(
        Long id,
        String name,
        String statement,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String classId,
        int numeroEntregas,
        long entregasCalificadas,
        
        // Información de entrega del usuario actual
        boolean tieneEntrega,
        Long entregaId,
        EEstadoEjercicio estadoEntrega,
        BigDecimal notaEntrega,
        LocalDateTime fechaEntrega,
        String notaFormateada,
        String estadoEntregaDescriptivo
) {
    
    /**
     * Constructor que crea un DTO desde una entidad Ejercicio sin información de entrega
     */
    public DTOEjercicioConEntrega(Ejercicio ejercicio) {
        this(
                ejercicio.getId(),
                ejercicio.getName(),
                ejercicio.getStatement(),
                ejercicio.getStartDate(),
                ejercicio.getEndDate(),
                ejercicio.getClase() != null ? ejercicio.getClase().getId().toString() : null,
                ejercicio.contarEntregas(),
                ejercicio.contarEntregasCalificadas(),
                
                // Sin información de entrega
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
    
    /**
     * Constructor que crea un DTO desde una entidad Ejercicio con información de entrega
     */
    public DTOEjercicioConEntrega(Ejercicio ejercicio, DTOEntregaEjercicio entrega) {
        this(
                ejercicio.getId(),
                ejercicio.getName(),
                ejercicio.getStatement(),
                ejercicio.getStartDate(),
                ejercicio.getEndDate(),
                ejercicio.getClase() != null ? ejercicio.getClase().getId().toString() : null,
                ejercicio.contarEntregas(),
                ejercicio.contarEntregasCalificadas(),
                
                // Con información de entrega
                true,
                entrega.id(),
                entrega.estado(),
                entrega.nota(),
                entrega.fechaEntrega(),
                entrega.getNotaFormateada(),
                entrega.getEstadoDescriptivo()
        );
    }
    
    /**
     * metodo estático para crear desde entidad sin entrega
     */
    public static DTOEjercicioConEntrega from(Ejercicio ejercicio) {
        return new DTOEjercicioConEntrega(ejercicio);
    }
    
    /**
     * metodo estático para crear desde entidad con entrega
     */
    public static DTOEjercicioConEntrega from(Ejercicio ejercicio, DTOEntregaEjercicio entrega) {
        return new DTOEjercicioConEntrega(ejercicio, entrega);
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
    
    /**
     * Obtiene el estado de entrega del usuario para mostrar en el frontend
     */
    public String getEstadoEntregaUsuario() {
        if (!tieneEntrega) {
            if (noHaComenzado()) {
                return "PENDIENTE";
            } else if (haVencido()) {
                return "VENCIDO_SIN_ENTREGA";
            } else {
                return "DISPONIBLE";
            }
        } else {
            return switch (estadoEntrega) {
                case PENDIENTE -> "ENTREGADO_PENDIENTE";
                case ENTREGADO -> "ENTREGADO_PENDIENTE";
                case CALIFICADO -> "CALIFICADO";
                default -> "ENTREGADO_PENDIENTE";
            };
        }
    }
    
    /**
     * Obtiene el color o icono para mostrar el estado de entrega
     */
    public String getEstadoEntregaIcono() {
        return switch (getEstadoEntregaUsuario()) {
            case "PENDIENTE" -> "⏳";
            case "DISPONIBLE" -> "📝";
            case "VENCIDO_SIN_ENTREGA" -> "❌";
            case "ENTREGADO_PENDIENTE" -> "📤";
            case "CALIFICADO" -> "✅";
            default -> "❓";
        };
    }
    
    /**
     * Obtiene el texto descriptivo del estado de entrega
     */
    public String getEstadoEntregaTexto() {
        return switch (getEstadoEntregaUsuario()) {
            case "PENDIENTE" -> "Pendiente de inicio";
            case "DISPONIBLE" -> "Disponible para entrega";
            case "VENCIDO_SIN_ENTREGA" -> "Vencido sin entrega";
            case "ENTREGADO_PENDIENTE" -> "Entregado - Pendiente de calificación";
            case "CALIFICADO" -> "Calificado";
            default -> "Estado desconocido";
        };
    }
    
    /**
     * Verifica si el usuario puede entregar este ejercicio
     */
    public boolean puedeEntregar() {
        return !tieneEntrega && estaEnPlazo();
    }
    
    /**
     * Verifica si el usuario puede ver su entrega
     */
    public boolean puedeVerEntrega() {
        return tieneEntrega;
    }
    
    /**
     * Verifica si el ejercicio está calificado
     */
    public boolean estaCalificado() {
        return tieneEntrega && estadoEntrega == EEstadoEjercicio.CALIFICADO;
    }
}
