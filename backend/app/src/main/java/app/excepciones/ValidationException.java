package app.excepciones;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ValidationException extends ApiException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Los datos enviados contienen errores");
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
        this.fieldErrors = null;
    }
}
