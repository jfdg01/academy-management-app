package app.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Entidad ItemPago (Agregado)
 * Representa un ítem dentro de un pago
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Embeddable
public class ItemPago {
    
    @NotNull
    @Size(max = 255)
    private String id;
    
    @NotNull
    @Size(max = 300)
    private String concepto;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    
    @NotNull
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
    
    public ItemPago() {}
    
    public ItemPago(String id, String concepto, BigDecimal precioUnitario, 
                   Integer cantidad) {
        this.id = id;
        this.concepto = concepto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }
    
    /**
     * Calcula el importe total del ítem (precio unitario * cantidad)
     * @return BigDecimal con el importe total
     */
    public BigDecimal getImporteTotal() {
        return this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
    }
}
