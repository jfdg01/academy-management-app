package app.excepciones;

import org.springframework.http.HttpStatus;

public class StripeWebhookException extends ApiException {
    public StripeWebhookException(String message, String webhookError) {
        super(String.format("Error procesando webhook de Stripe: %s. Detalle: %s", message, webhookError), 
              HttpStatus.BAD_REQUEST, 
              "STRIPE_WEBHOOK_ERROR", 
              "Error al procesar la notificación de pago.");
    }

    public StripeWebhookException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "STRIPE_WEBHOOK_ERROR", 
              "Error al procesar la notificación de pago.");
    }
}
