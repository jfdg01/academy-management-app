package app.util;

import app.excepciones.AccessDeniedException;
import app.excepciones.ResourceNotFoundException;
import app.excepciones.ValidationException;

import java.util.Map;

public class ExceptionUtils {
    
    /**
     * Throws a ResourceNotFoundException if the condition is true
     */
    public static void throwIfNotFound(boolean condition, String resource, String field, Object value) {
        if (condition) {
            throw new ResourceNotFoundException(resource, field, value);
        }
    }
    
    /**
     * Throws a ResourceNotFoundException if the object is null
     */
    public static void throwIfNotFound(Object object, String resource, String field, Object value) {
        if (object == null) {
            throw new ResourceNotFoundException(resource, field, value);
        }
    }
    
    /**
     * Throws a ValidationException with field errors
     */
    public static void throwValidationError(String message, Map<String, String> fieldErrors) {
        throw new ValidationException(message, fieldErrors);
    }
    
    /**
     * Throws a ValidationException with a simple message
     */
    public static void throwValidationError(String message) {
        throw new ValidationException(message);
    }
    
    /**
     * Throws an AccessDeniedException
     */
    public static void throwAccessDenied(String message) {
        throw new AccessDeniedException(message);
    }
    
    /**
     * Throws an AccessDeniedException with default message
     */
    public static void throwAccessDenied() {
        throw new AccessDeniedException();
    }
}
