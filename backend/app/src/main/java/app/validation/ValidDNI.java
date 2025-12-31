package app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación personalizada para DNI español
 * Sigue estándares de Spring Boot para validaciones personalizadas
 */
@Documented
@Constraint(validatedBy = DNIValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDNI {
    String message() default "El DNI no tiene un formato válido (ej: 12345678Z)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
