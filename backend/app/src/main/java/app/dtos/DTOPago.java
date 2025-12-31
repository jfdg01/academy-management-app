package app.dtos;

import app.entidades.ItemPago;
import app.entidades.Pago;
import app.entidades.enums.EMetodoPago;
import app.entidades.enums.EEstadoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la entidad Pago
 * Contiene información del pago sin exposer detalles internos
 */
public record DTOPago(
        Long id,
        LocalDateTime fechaPago,
        BigDecimal importe,
        EMetodoPago metodoPago,
        EEstadoPago estado,
        String alumnoId,
        Boolean facturaCreada,
        List<ItemPago> items,
        String stripePaymentIntentId,
        String stripeChargeId,
        String failureReason,
        String clientSecret, // Only present in create response
        Long classId // Optional class ID for enrollment payments
) {
    
    /**
     * Constructor que crea un DTO desde una entidad Pago
     */
    public DTOPago(Pago pago) {
        this(
                pago.getId(),
                pago.getFechaPago(),
                pago.getImporte(),
                pago.getMetodoPago(),
                pago.getEstado(),
                pago.getAlumno() != null ? pago.getAlumno().getId().toString() : null,
                pago.getFacturaCreada(),
                pago.getItems(),
                pago.getStripePaymentIntentId(),
                pago.getStripeChargeId(),
                pago.getFailureReason(),
                null, // Never include client_secret when loading from DB
                pago.getClase() != null ? pago.getClase().getId() : null
        );
    }
    
    /**
     * metodo estático para crear desde entidad
     */
    public static DTOPago from(Pago pago) {
        return new DTOPago(pago);
    }
    
    /**
     * Verifica si se puede crear una factura para este pago
     */
    public boolean puedeCrearFactura() {
        return !this.facturaCreada && this.estado == EEstadoPago.EXITO;
    }
    
    /**
     * Verifica si el pago es un pago manual (efectivo)
     */
    public boolean esPagoManual() {
        return this.metodoPago == EMetodoPago.EFECTIVO;
    }
    
    /**
     * Verifica si el pago fue exitoso
     */
    public boolean esExitoso() {
        return this.estado == EEstadoPago.EXITO;
    }
    
    /**
     * Verifica si el pago fue reembolsado
     */
    public boolean fueReembolsado() {
        return this.estado == EEstadoPago.REEMBOLSADO;
    }
    
    /**
     * Obtiene el número de items en el pago
     */
    public int getNumeroItems() {
        return this.items != null ? this.items.size() : 0;
    }
    
    /**
     * Calcula el importe total de los items (para verificación)
     */
    public BigDecimal calcularImporteTotalItems() {
        if (this.items == null) {
            return BigDecimal.ZERO;
        }
        
        return this.items.stream()
                .map(ItemPago::getImporteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Obtiene una descripción del metodo de pago
     */
    public String getDescripcionMetodoPago() {
        return switch (this.metodoPago) {
            case EFECTIVO -> "Pago en efectivo";
            case DEBITO -> "Tarjeta de débito";
            case CREDITO -> "Tarjeta de crédito";
            case TRANSFERENCIA -> "Transferencia bancaria";
            case STRIPE -> "Pago con Stripe";
        };
    }
    
    /**
     * Obtiene una descripción del estado del pago
     */
    public String getDescripcionEstado() {
        return switch (this.estado) {
            case PENDIENTE -> "Pago pendiente";
            case PROCESANDO -> "Pago en procesamiento";
            case EXITO -> "Pago procesado exitosamente";
            case ERROR -> "Error en el procesamiento";
            case REEMBOLSADO -> "Pago reembolsado";
        };
    }
}
