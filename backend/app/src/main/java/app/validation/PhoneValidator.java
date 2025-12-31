package app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Implementación del validador para números de teléfono
 * Sigue estándares de Spring Boot para ConstraintValidator
 */
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    
    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        // Inicialización si es necesaria
    }
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // Si es null, se considera válido (usar @NotNull por separado si se requiere)
        if (phone == null) {
            return true;
        }
        
        // Si es string vacío, es inválido
        if (phone.trim().isEmpty()) {
            return false;
        }
        
        // Limpiar espacios, guiones y otros caracteres para validación
        String cleanPhone = phone.replaceAll("[\\s.\\-()]", "");
        
        // Validar que solo contenga dígitos y posiblemente un prefijo +
        if (!cleanPhone.matches("^(\\+\\d+)?\\d{6,14}$")) {
            return false;
        }
        
        // Verificar longitud sin el prefijo +
        String numberOnly = cleanPhone.replaceFirst("^\\+\\d{1,4}", "");
        return numberOnly.length() >= 6 && numberOnly.length() <= 14;
    }
}
