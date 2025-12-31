package app.excepciones;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s no encontrado con %s: %s", resource, field, value), 
              HttpStatus.NOT_FOUND, 
              "RESOURCE_NOT_FOUND", 
              String.format("El %s solicitado no existe", resource.toLowerCase()));
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "El recurso solicitado no existe");
    }
}
