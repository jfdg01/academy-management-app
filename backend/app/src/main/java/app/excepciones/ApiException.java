package app.excepciones;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final String userMessage;

    public ApiException(String message, HttpStatus status, String errorCode, String userMessage) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public ApiException(String message, HttpStatus status, String errorCode) {
        this(message, status, errorCode, message);
    }

    public ApiException(String message, HttpStatus status) {
        this(message, status, "API_ERROR", message);
    }
}
