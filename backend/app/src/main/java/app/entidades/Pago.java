package app.entidades;

import app.entidades.enums.EMetodoPago;
import app.entidades.enums.EEstadoPago;
import app.validation.stripe.ValidStripePaymentIntentId;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
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
 * Entidad Pago
 * Representa un pago realizado por un alumno
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "pagos")
@NamedEntityGraph(
    name = "Pago.withItems",
    attributeNodes = @NamedAttributeNode("items")
)
@NamedEntityGraph(
    name = "Pago.withAlumno",
    attributeNodes = @NamedAttributeNode("alumno")
)
@NamedEntityGraph(
    name = "Pago.withClase",
    attributeNodes = @NamedAttributeNode("clase")
)
@NamedEntityGraph(
    name = "Pago.withAlumnoAndClase",
    attributeNodes = {
        @NamedAttributeNode("alumno"),
        @NamedAttributeNode("clase")
    }
)
@NamedEntityGraph(
    name = "Pago.withItemsAndAlumno",
    attributeNodes = {
        @NamedAttributeNode("items"),
        @NamedAttributeNode("alumno")
    }
)
@NamedEntityGraph(
    name = "Pago.withItemsAndClase",
    attributeNodes = {
        @NamedAttributeNode("items"),
        @NamedAttributeNode("clase")
    }
)
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull
    private LocalDateTime fechaPago;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(precision = 10, scale = 2)
    private BigDecimal importe;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EMetodoPago metodoPago;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private EEstadoPago estado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_id")
    private Clase clase;
    
    @NotNull
    private Boolean facturaCreada = false;
    
    // Items del pago embebidos
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pago_items", joinColumns = @JoinColumn(name = "pago_id"))
    private List<ItemPago> items = new ArrayList<>();
    
    // Stripe fields (add after existing fields)
    @ValidStripePaymentIntentId
    private String stripePaymentIntentId;
    
    private String stripeChargeId;
    
    @Size(max = 500, message = "La razón del fallo no puede exceder 500 caracteres")
    private String failureReason;
    
    private LocalDateTime fechaExpiracion;
    
    // Class enrollment field
    private Long classId; // Optional class ID for enrollment payments
    
    public Pago() {
        this.fechaPago = LocalDateTime.now();
        this.estado = EEstadoPago.PENDIENTE; // Default to pending for new payments
    }
    
    // New constructor with JPA relationships
    public Pago(BigDecimal importe, EMetodoPago metodoPago, Alumno alumno) {
        this();
        this.importe = importe;
        this.metodoPago = metodoPago;
        this.alumno = alumno;
    }
    
    public Pago(BigDecimal importe, EMetodoPago metodoPago, Alumno alumno, Clase clase) {
        this(importe, metodoPago, alumno);
        this.clase = clase;
        this.classId = clase != null ? clase.getId() : null;
    }
    
    /**
     * Agrega un ítem al pago
     * @param item ItemPago a agregar
     */
    public void agregarItem(ItemPago item) {
        this.items.add(item);
        recalcularImporte();
    }
    
    /**
     * Remueve un ítem del pago por ID
     * @param itemId ID del ítem a remover
     */
    public void removerItem(String itemId) {
        this.items.removeIf(item -> item.getId().equals(itemId));
        recalcularImporte();
    }
    
    /**
     * Recalcula el importe total basado en los ítems
     */
    private void recalcularImporte() {
        this.importe = this.items.stream()
                .map(ItemPago::getImporteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Verifica si se puede crear una factura para este pago
     * @return true si se puede crear factura, false en caso contrario
     */
    public boolean puedeCrearFactura() {
        return !this.facturaCreada && this.estado == EEstadoPago.EXITO;
    }
    
    /**
     * Marca el pago como facturado
     */
    public void marcarComoFacturado() {
        if (!puedeCrearFactura()) {
            throw new IllegalStateException("No se puede crear una factura para este pago");
        }
        this.facturaCreada = true;
    }
    
    /**
     * Verifica si el pago es un pago manual (efectivo)
     * @return true si es pago manual, false en caso contrario
     */
    public boolean esPagoManual() {
        return this.metodoPago == EMetodoPago.EFECTIVO;
    }
    
    /**
     * Procesa el reembolso del pago
     */
    public void procesarReembolso() {
        if (this.estado != EEstadoPago.EXITO) {
            throw new IllegalStateException("Solo se pueden reembolsar pagos exitosos");
        }
        this.estado = EEstadoPago.REEMBOLSADO;
    }
    
    /**
     * Verifica si el pago está expirado
     * @return true si está expirado, false en caso contrario
     */
    public boolean estaExpirado() {
        return this.fechaExpiracion != null && LocalDateTime.now().isAfter(this.fechaExpiracion);
    }
    
    /**
     * Obtiene el número de items en el pago
     * @return número de items
     */
    public int getNumeroItems() {
        return this.items != null ? this.items.size() : 0;
    }
    
    /**
     * Calcula el importe total de los items (para verificación)
     * @return importe total calculado
     */
    public BigDecimal calcularImporteTotalItems() {
        if (this.items == null) {
            return BigDecimal.ZERO;
        }
        
        return this.items.stream()
                .map(ItemPago::getImporteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
