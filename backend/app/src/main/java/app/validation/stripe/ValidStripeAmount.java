package app.validation.stripe;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación personalizada para montos EUR de Stripe
 * Sigue estándares de Spring Boot para validaciones personalizadas
 */
@Documented
@Constraint(validatedBy = StripeAmountValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStripeAmount {
    String message() default "El monto debe estar entre 0.50€ y 999,999.99€";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
