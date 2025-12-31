package app.excepciones;

import org.springframework.http.HttpStatus;

public class PaymentProcessingException extends ApiException {
    public PaymentProcessingException(String message, String stripeError) {
        super(String.format("Error procesando pago: %s. Stripe: %s", message, stripeError), 
              HttpStatus.INTERNAL_SERVER_ERROR, 
              "PAYMENT_PROCESSING_ERROR", 
              "Error al procesar el pago. Por favor, inténtelo de nuevo más tarde.");
    }

    public PaymentProcessingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_PROCESSING_ERROR", 
              "Error al procesar el pago. Por favor, inténtelo de nuevo más tarde.");
    }
}
