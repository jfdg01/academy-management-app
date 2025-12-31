package app.validation.stripe;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Simple validator for Stripe Payment Intent ID
 * Follows Spring Boot standards for ConstraintValidator
 */
public class StripePaymentIntentIdValidator implements ConstraintValidator<ValidStripePaymentIntentId, String> {
    
    // Pattern for Stripe Payment Intent ID: pi_ followed by alphanumeric characters
    private static final Pattern PAYMENT_INTENT_PATTERN = Pattern.compile("^pi_[a-zA-Z0-9]{24}$");
    
    @Override
    public void initialize(ValidStripePaymentIntentId constraintAnnotation) {
        // Initialization if needed
    }
    
    @Override
    public boolean isValid(String paymentIntentId, ConstraintValidatorContext context) {
        // If null, consider valid (use @NotNull separately if required)
        if (paymentIntentId == null) {
            return true;
        }
        
        // If empty string, invalid
        if (paymentIntentId.trim().isEmpty()) {
            return false;
        }
        
        // Verify Stripe Payment Intent ID format
        return PAYMENT_INTENT_PATTERN.matcher(paymentIntentId.trim()).matches();
    }
}
