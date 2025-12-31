package app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación que asegura que si un string no es null, tampoco puede estar vacío
 * Útil para campos opcionales que si se proporcionan deben tener contenido
 */
@Documented
@Constraint(validatedBy = NotEmptyIfPresentValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyIfPresent {
    String message() default "El campo no puede estar vacío si se proporciona";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
