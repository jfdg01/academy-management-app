package app.dtos;

import app.entidades.Material;
import app.entidades.Taller;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * DTO específico para la entidad Taller
 * Extiende la información de clase con datos específicos del taller
 */
public record DTOTaller(
        Long id,
        String titulo,
        String descripcion,
        BigDecimal precio,
        EPresencialidad presencialidad,
        String imagenPortada,
        EDificultad nivel,
        List<String> alumnosId,
        List<String> profesoresId,
        List<String> ejerciciosId,
        List<DTOMaterial> material,
        Integer duracionHoras,
        LocalDate fechaRealizacion,
        LocalTime horaComienzo
) {
    
    /**
     * Constructor that creates a DTO from a Taller entity
     */
    public DTOTaller(Taller taller) {
        this(
            taller.getId(),
            taller.getTitle(),
            taller.getDescription(),
            taller.getPrice(),
            taller.getFormat(),
            taller.getImage(),
            taller.getDifficulty(),
            taller.getStudents() != null ? taller.getStudents().stream()
                .map(alumno -> alumno.getId().toString())
                .collect(Collectors.toList()) : null,
            taller.getTeachers() != null ? taller.getTeachers().stream()
                .map(profesor -> profesor.getId().toString())
                .collect(Collectors.toList()) : null,
            taller.getExercises() != null ? taller.getExercises().stream()
                .map(ejercicio -> ejercicio.getId().toString())
                .collect(Collectors.toList()) : null,
            taller.getMaterial() != null ? taller.getMaterial().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList()) : new ArrayList<>(),
            taller.getDuracionHoras(),
            taller.getFechaRealizacion(),
            taller.getHoraComienzo()
        );
    }
    
    /**
     * metodo estático para crear desde entidad
     */
    public static DTOTaller from(Taller taller) {
        return new DTOTaller(taller);
    }
    
    /**
     * Calcula la hora de finalización del taller
     */
    public LocalTime getHoraFinalizacion() {
        return this.horaComienzo.plusHours(this.duracionHoras);
    }
    
    /**
     * Verifica si el taller está en curso en una fecha y hora dadas
     */
    public boolean estaEnCurso(LocalDate fecha, LocalTime hora) {
        if (!this.fechaRealizacion.equals(fecha)) {
            return false;
        }
        return hora.isAfter(this.horaComienzo) && hora.isBefore(getHoraFinalizacion());
    }
    
    /**
     * Verifica si el taller ya ha finalizado
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
    
    /**
     * Verifica si el taller es próximo (en los próximos 7 días)
     */
    public boolean esProximo() {
        LocalDate limite = LocalDate.now().plusDays(7);
        return this.fechaRealizacion.isAfter(LocalDate.now()) && 
               this.fechaRealizacion.isBefore(limite.plusDays(1));
    }
    
    /**
     * Obtiene el número de alumnos inscritos
     */
    public int getNumeroAlumnos() {
        return this.alumnosId != null ? this.alumnosId.size() : 0;
    }
    
    /**
     * Obtiene el número de profesores asignados
     */
    public int getNumeroProfesores() {
        return this.profesoresId != null ? this.profesoresId.size() : 0;
    }
}
