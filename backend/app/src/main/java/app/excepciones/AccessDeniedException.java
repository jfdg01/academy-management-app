package app.excepciones;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ApiException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "No tienes permisos para realizar esta acción");
    }

    public AccessDeniedException() {
        this("Acceso denegado");
    }
}
