package app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Implementación del validador para DNI español
 * Sigue estándares de Spring Boot para ConstraintValidator
 */
public class DNIValidator implements ConstraintValidator<ValidDNI, String> {
    
    // Patrón para DNI español: 8 dígitos seguidos de una letra
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}[A-Z]$");
    
    // Letras válidas para el cálculo del DNI
    private static final String DNI_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    
    @Override
    public void initialize(ValidDNI constraintAnnotation) {
        // Inicialización si es necesaria
    }
    
    @Override
    public boolean isValid(String dni, ConstraintValidatorContext context) {
        // Si es null, se considera válido (usar @NotNull por separado si se requiere)
        if (dni == null) {
            return true;
        }
        
        // Si es string vacío, es inválido
        if (dni.trim().isEmpty()) {
            return false;
        }
        
        // Convertir a mayúsculas para la validación
        dni = dni.toUpperCase().trim();
        
        // Verificar formato básico
        if (!DNI_PATTERN.matcher(dni).matches()) {
            return false;
        }
        
        // Verificar que la letra sea correcta
        try {
            String numbers = dni.substring(0, 8);
            char letter = dni.charAt(8);
            int remainder = Integer.parseInt(numbers) % 23;
            char expectedLetter = DNI_LETTERS.charAt(remainder);
            
            return letter == expectedLetter;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return false;
        }
    }
}
