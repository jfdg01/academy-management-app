package app.excepciones;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends ApiException {
    public PaymentNotFoundException(String field, Object value) {
        super(String.format("Pago no encontrado con %s: %s", field, value), 
              HttpStatus.NOT_FOUND, 
              "PAYMENT_NOT_FOUND", 
              "El pago solicitado no existe");
    }

    public PaymentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "El pago solicitado no existe");
    }
}
