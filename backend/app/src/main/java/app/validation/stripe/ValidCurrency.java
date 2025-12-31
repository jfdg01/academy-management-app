package app.validation.stripe;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación personalizada para EUR (demo)
 * Sigue estándares de Spring Boot para validaciones personalizadas
 */
@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "Solo se acepta EUR para este demo";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
