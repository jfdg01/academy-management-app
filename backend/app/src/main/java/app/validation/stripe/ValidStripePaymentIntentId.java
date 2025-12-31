package app.validation.stripe;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación personalizada para Stripe Payment Intent ID
 * Sigue estándares de Spring Boot para validaciones personalizadas
 */
@Documented
@Constraint(validatedBy = StripePaymentIntentIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStripePaymentIntentId {
    String message() default "El Payment Intent ID de Stripe no tiene un formato válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
