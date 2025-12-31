package app.dtos;

import app.validation.stripe.ValidCurrency;
import app.validation.stripe.ValidStripeAmount;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para la petición de creación de un pago
 * Contiene los datos necesarios para registrar un pago (EUR only)
 */
public record DTOPeticionCrearPago(
        @NotNull
        @ValidStripeAmount
        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal importe,
        
        @NotNull
        @Size(max = 255)
        String alumnoId,
        
        @NotBlank
        String description,
        
        @NotBlank
        @ValidCurrency
        String currency,
        
        Long classId // Optional class ID for enrollment payments
) {
    
    /**
     * Constructor por defecto con currency EUR
     */
    public DTOPeticionCrearPago(BigDecimal importe, String alumnoId, String description) {
        this(importe, alumnoId, description, "EUR", null);
    }
    
    /**
     * Constructor para pagos de inscripción a clases
     */
    public DTOPeticionCrearPago(BigDecimal importe, String alumnoId, String description, Long classId) {
        this(importe, alumnoId, description, "EUR", classId);
    }
}
