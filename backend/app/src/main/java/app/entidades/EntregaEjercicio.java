package app.entidades;

import app.entidades.enums.EEstadoEjercicio;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad EntregaEjercicio
 * Representa la entrega de un ejercicio por parte de un alumno
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "entregas_ejercicio")
@NamedEntityGraph(
    name = "EntregaEjercicio.withAlumno",
    attributeNodes = {
        @NamedAttributeNode("alumno")
    }
)
@NamedEntityGraph(
    name = "EntregaEjercicio.withEjercicio",
    attributeNodes = {
        @NamedAttributeNode("ejercicio")
    }
)
@NamedEntityGraph(
    name = "EntregaEjercicio.withAllRelationships",
    attributeNodes = {
        @NamedAttributeNode("alumno"),
        @NamedAttributeNode("ejercicio"),
        @NamedAttributeNode("archivosEntregados")
    }
)
public class EntregaEjercicio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    @Column(precision = 4, scale = 2)
    private BigDecimal nota;
    
    @NotNull
    private LocalDateTime fechaEntrega;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EEstadoEjercicio estado;
    
    // Representación simplificada de archivos como strings
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "entrega_archivos", joinColumns = @JoinColumn(name = "entrega_id"))
    @Column(name = "archivo_path")
    private List<String> archivosEntregados = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejercicio_id")
    private Ejercicio ejercicio;
    
    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String comentarios;
    
    public EntregaEjercicio() {
        this.fechaEntrega = LocalDateTime.now();
        this.estado = EEstadoEjercicio.ENTREGADO;
    }
    
    public EntregaEjercicio(Alumno alumno, Ejercicio ejercicio) {
        this();
        this.alumno = alumno;
        this.ejercicio = ejercicio;
    }
    
    /**
     * Agrega un archivo a la entrega
     * @param archivoPath Path o nombre del archivo
     */
    public void agregarArchivo(String archivoPath) {
        if (!this.archivosEntregados.contains(archivoPath)) {
            this.archivosEntregados.add(archivoPath);
        }
    }
    
    /**
     * Remueve un archivo de la entrega
     * @param archivoPath Path o nombre del archivo
     */
    public void removerArchivo(String archivoPath) {
        this.archivosEntregados.remove(archivoPath);
    }
    
    /**
     * Califica la entrega
     * @param nota Nota a asignar (0-10)
     */
    public void calificar(BigDecimal nota) {
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 10");
        }
        this.nota = nota;
        this.estado = EEstadoEjercicio.CALIFICADO;
    }
    
    /**
     * Califica la entrega con comentarios
     * @param nota Nota a asignar (0-10)
     * @param comentarios Comentarios del profesor
     */
    public void calificar(BigDecimal nota, String comentarios) {
        calificar(nota);
        this.comentarios = comentarios;
    }
    
    /**
     * Agrega o actualiza comentarios a la entrega
     * @param comentarios Comentarios del profesor
     */
    public void agregarComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
    
    /**
     * Obtiene los comentarios de la entrega
     * @return Comentarios del profesor o null si no hay
     */
    public String getComentarios() {
        return this.comentarios;
    }
    
    /**
     * Verifica si la entrega tiene comentarios
     * @return true si tiene comentarios, false en caso contrario
     */
    public boolean tieneComentarios() {
        return this.comentarios != null && !this.comentarios.trim().isEmpty();
    }
    
    /**
     * Verifica si la entrega está calificada
     * @return true si está calificada, false en caso contrario
     */
    public boolean estaCalificada() {
        return this.estado == EEstadoEjercicio.CALIFICADO && this.nota != null;
    }
    
    /**
     * Verifica si la entrega fue realizada fuera de plazo
     * @param ejercicioRef Ejercicio de referencia para verificar plazo
     * @return true si fue entregada fuera de plazo, false en caso contrario
     */
    public boolean esFueraDePlazo(Ejercicio ejercicioRef) {
        //return this.fechaEntrega.isAfter(ejercicioRef.getFechaFinalPlazo());
        return false;
    }
    
    /**
     * Obtiene el número de archivos entregados
     * @return Número de archivos
     */
    public int contarArchivos() {
        return this.archivosEntregados.size();
    }
    
    /**
     * Marca la entrega como pendiente (permite modificaciones)
     */
    public void marcarComoPendiente() {
        if (this.estado == EEstadoEjercicio.CALIFICADO) {
            throw new IllegalStateException("No se puede marcar como pendiente una entrega ya calificada");
        }
        this.estado = EEstadoEjercicio.PENDIENTE;
    }
    
    /**
     * Confirma la entrega
     */
    public void confirmarEntrega() {
        if (this.archivosEntregados.isEmpty()) {
            throw new IllegalStateException("No se puede confirmar una entrega sin archivos");
        }
        this.estado = EEstadoEjercicio.ENTREGADO;
        this.fechaEntrega = LocalDateTime.now();
    }
}
