package app.validation.stripe;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Simple validator for EUR currency (demo only)
 * Follows Spring Boot standards for ConstraintValidator
 */
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {
    
    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // Initialization if needed
    }
    
    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        // If null, consider valid (use @NotNull separately if required)
        if (currency == null) {
            return true;
        }
        
        // Only accept EUR for demo
        return "EUR".equals(currency.trim().toUpperCase());
    }
}
