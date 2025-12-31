package app.dtos;

import app.entidades.EntregaEjercicio;
import app.entidades.enums.EEstadoEjercicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO para la entidad EntregaEjercicio
 * Contiene información de la entrega sin relaciones complejas
 */
public record DTOEntregaEjercicio(
        Long id,
        BigDecimal nota,
        LocalDateTime fechaEntrega,
        EEstadoEjercicio estado,
        List<String> archivosEntregados,
        Long alumnoId,
        Long ejercicioId,
        int numeroArchivos,
        String comentarios
) {
    
    /**
     * Constructor that creates a DTO from an EntregaEjercicio entity
     */
    public DTOEntregaEjercicio(EntregaEjercicio entrega) {
        this(
            entrega.getId(),
            entrega.getNota(),
            entrega.getFechaEntrega(),
            entrega.getEstado(),
            entrega.getArchivosEntregados(),
            entrega.getAlumno() != null ? entrega.getAlumno().getId() : null,
            entrega.getEjercicio() != null ? entrega.getEjercicio().getId() : null,
            entrega.contarArchivos(),
            entrega.getComentarios()
        );
    }
    
    /**
     * metodo estático para crear desde entidad
     */
    public static DTOEntregaEjercicio from(EntregaEjercicio entrega) {
        return new DTOEntregaEjercicio(entrega);
    }
    
    /**
     * Verifica si la entrega está calificada
     */
    public boolean estaCalificada() {
        return this.estado == EEstadoEjercicio.CALIFICADO && this.nota != null;
    }
    
    /**
     * Verifica si la entrega está pendiente
     */
    public boolean estaPendiente() {
        return this.estado == EEstadoEjercicio.PENDIENTE;
    }
    
    /**
     * Verifica si la entrega fue entregada pero no calificada
     */
    public boolean estaEntregada() {
        return this.estado == EEstadoEjercicio.ENTREGADO;
    }
    
    /**
     * Obtiene la nota como string formateado
     */
    public String getNotaFormateada() {
        if (this.nota == null) {
            return "Sin calificar";
        }
        return String.format("%.2f", this.nota);
    }
    
    /**
     * Verifica si la nota es aprobatoria (>= 5.0)
     */
    public boolean esAprobatoria() {
        if (this.nota == null) {
            return false;
        }
        return this.nota.compareTo(BigDecimal.valueOf(5.0)) >= 0;
    }
    
    /**
     * Obtiene el estado como string descriptivo
     */
    public String getEstadoDescriptivo() {
        return switch (this.estado) {
            case PENDIENTE -> "Pendiente de entrega";
            case ENTREGADO -> "Entregado - Pendiente de calificación";
            case CALIFICADO -> "Calificado";
        };
    }
    
    /**
     * Verifica si la entrega tiene archivos
     */
    public boolean tieneArchivos() {
        return this.numeroArchivos > 0;
    }
    
    /**
     * Obtiene una calificación cualitativa basada en la nota
     */
    public String getCalificacionCualitativa() {
        if (this.nota == null) {
            return "Sin calificar";
        }
        
        double notaDouble = this.nota.doubleValue();
        
        if (notaDouble >= 9.0) {
            return "Excelente";
        } else if (notaDouble >= 7.0) {
            return "Notable";
        } else if (notaDouble >= 5.0) {
            return "Aprobado";
        } else {
            return "Suspenso";
        }
    }
    
    /**
     * Verifica si la entrega puede ser modificada
     */
    public boolean puedeSerModificada() {
        return this.estado == EEstadoEjercicio.PENDIENTE;
    }
    
    /**
     * Verifica si la entrega tiene comentarios del profesor
     */
    public boolean tieneComentarios() {
        return this.comentarios != null && !this.comentarios.trim().isEmpty();
    }
    
    /**
     * Obtiene los comentarios del profesor o un mensaje por defecto
     */
    public String getComentariosFormateados() {
        if (this.comentarios == null || this.comentarios.trim().isEmpty()) {
            return "Sin comentarios";
        }
        return this.comentarios.trim();
    }
    
    /**
     * Obtiene un resumen de los comentarios (primeros 100 caracteres)
     */
    public String getResumenComentarios() {
        if (!tieneComentarios()) {
            return "Sin comentarios";
        }
        
        String comentarios = this.comentarios.trim();
        if (comentarios.length() <= 100) {
            return comentarios;
        }
        return comentarios.substring(0, 97) + "...";
    }
}
