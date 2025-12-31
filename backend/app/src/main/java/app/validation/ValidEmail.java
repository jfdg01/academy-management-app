package app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación personalizada más estricta para email
 * Combina @Email con validaciones adicionales
 */
@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {
    String message() default "El email debe tener un formato válido y dominio reconocido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
