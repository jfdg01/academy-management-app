package app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementación del validador para strings que no deben estar vacíos si están presentes
 */
public class NotEmptyIfPresentValidator implements ConstraintValidator<NotEmptyIfPresent, String> {
    
    @Override
    public void initialize(NotEmptyIfPresent constraintAnnotation) {
        // Inicialización si es necesaria
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Si es null, es válido (campo opcional)
        if (value == null) {
            return true;
        }
        
        // Si no es null, no puede estar vacío o ser solo espacios
        return !value.trim().isEmpty();
    }
}
