package app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación personalizada para números de teléfono
 * Sigue estándares de Spring Boot para validaciones personalizadas
 */
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "El número de teléfono no tiene un formato válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
