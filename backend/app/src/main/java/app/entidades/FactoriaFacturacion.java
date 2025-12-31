package app.entidades;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FactoriaFacturacion
 * Factoría para crear facturas desde pagos
 * Basado en el UML de especificación
 */
@Component
public class FactoriaFacturacion {
    
    /**
     * Crea una factura desde un pago
     * @param pago Pago del cual crear la factura
     * @return String con el contenido de la factura (placeholder)
     * @throws IllegalStateException si el pago no puede ser facturado
     */
    public String crearFacturaDesdePago(Pago pago) {
        if (!pago.puedeCrearFactura()) {
            throw new IllegalStateException(
                "No se puede crear factura para el pago con ID: " + pago.getId() + 
                ". Motivo: " + (pago.getFacturaCreada() ? "Ya tiene factura" : "Estado de pago no válido")
            );
        }
        
        StringBuilder factura = new StringBuilder();
        factura.append("=== FACTURA ===\n");
        factura.append("Fecha: ").append(pago.getFechaPago()).append("\n");
        factura.append("Pago ID: ").append(pago.getId()).append("\n");
        //factura.append("Alumno ID: ").append(pago.g()).append("\n");
        factura.append("metodo de pago: ").append(pago.getMetodoPago()).append("\n");
        factura.append("Estado: ").append(pago.getEstado()).append("\n");
        factura.append("\n--- DETALLE ---\n");
        
        for (ItemPago item : pago.getItems()) {
            factura.append(String.format("- %s | Cant: %d | Precio unit: %.2f | Total: %.2f\n",
                item.getConcepto(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getImporteTotal()
            ));
        }
        
        factura.append("\nIMPORTE TOTAL: ").append(pago.getImporte()).append("€\n");
        factura.append("===============\n");
        
        // Marcar como facturado
        pago.marcarComoFacturado();
        
        return factura.toString();
    }
    
    /**
     * Crea facturas desde una lista de pagos
     * @param pagos Lista de pagos
     * @return Lista de facturas generadas
     */
    public List<String> crearFacturasDesdeListaPago(List<Pago> pagos) {
        return pagos.stream()
                .filter(Pago::puedeCrearFactura)
                .map(this::crearFacturaDesdePago)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea todas las facturas pendientes de una lista de pagos
     * @param todosPagos Lista completa de pagos
     * @return Lista de facturas generadas
     */
    public List<String> crearTodasLasFacturas(List<Pago> todosPagos) {
        List<Pago> pagosPendientesFacturacion = todosPagos.stream()
                .filter(Pago::puedeCrearFactura)
                .collect(Collectors.toList());
        
        return crearFacturasDesdeListaPago(pagosPendientesFacturacion);
    }
    
    /**
     * Cuenta cuántos pagos están pendientes de facturación
     * @param pagos Lista de pagos
     * @return Número de pagos pendientes de facturación
     */
    public long contarPagosPendientesFacturacion(List<Pago> pagos) {
        return pagos.stream()
                .filter(Pago::puedeCrearFactura)
                .count();
    }
}
