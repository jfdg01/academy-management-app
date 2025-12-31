package app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Implementación del validador más estricto para email
 * Sigue estándares de Spring Boot para ConstraintValidator
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    // Patrón más estricto para email que el @Email por defecto
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        // Inicialización si es necesaria
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // Si es null, se considera válido (usar @NotNull por separado si se requiere)
        if (email == null) {
            return true;
        }
        
        // Si es string vacío, es inválido
        if (email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim().toLowerCase();
        
        // Verificar formato básico
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        
        // Verificaciones adicionales de formato
        if (email.length() > 254) { // RFC 5321 límite
            return false;
        }
        
        // No puede empezar o terminar con punto
        if (email.startsWith(".") || email.endsWith(".")) {
            return false;
        }
        
        // No puede tener puntos consecutivos
        if (email.contains("..")) {
            return false;
        }
        
        // Verificar parte local (antes de @)
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        // Parte local no puede ser más larga de 64 caracteres
        if (localPart.length() > 64) {
            return false;
        }
        
        // Dominio debe tener al menos un punto
        if (!domainPart.contains(".")) {
            return false;
        }
        
        return true;
    }
}
