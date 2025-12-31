package app.validation.stripe;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Simple validator for Stripe amounts
 * Follows Spring Boot standards for ConstraintValidator
 */
public class StripeAmountValidator implements ConstraintValidator<ValidStripeAmount, BigDecimal> {
    
    // Basic Stripe limits for demo
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.50");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999.99");
    
    @Override
    public void initialize(ValidStripeAmount constraintAnnotation) {
        // Initialization if needed
    }
    
    @Override
    public boolean isValid(BigDecimal amount, ConstraintValidatorContext context) {
        // If null, consider valid (use @NotNull separately if required)
        if (amount == null) {
            return true;
        }
        
        // Check for positive amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Check Stripe limits
        return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
    }
}
