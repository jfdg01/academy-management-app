package app.excepciones;

import org.springframework.http.HttpStatus;

public class EntidadNoEncontradaException extends ApiException {
    public EntidadNoEncontradaException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }

    public EntidadNoEncontradaException(String resource, String field, Object value) {
        super(String.format("%s no encontrado con %s: %s", resource, field, value), 
              HttpStatus.NOT_FOUND, 
              "RESOURCE_NOT_FOUND", 
              String.format("El %s solicitado no existe", resource.toLowerCase()));
    }
}
