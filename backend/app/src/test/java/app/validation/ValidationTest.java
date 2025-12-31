package app.validation;

import app.dtos.DTOActualizarAlumno;
import app.dtos.DTOCrearAlumno;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para validar las validaciones personalizadas
 */
public class ValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidDNI() {
        // DNI válido
        DTOActualizarAlumno validDto = new DTOActualizarAlumno(null, null, "12345678Z", null, null, null, null);
        Set<ConstraintViolation<DTOActualizarAlumno>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // DNI inválido
        DTOActualizarAlumno invalidDto = new DTOActualizarAlumno(null, null, "12345678A", null, null, null, null);
        violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testValidEmail() {
        // Email válido
        DTOActualizarAlumno validDto = new DTOActualizarAlumno(null, null, null, "test@ejemplo.com", null, null, null);
        Set<ConstraintViolation<DTOActualizarAlumno>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // Email inválido
        DTOActualizarAlumno invalidDto = new DTOActualizarAlumno(null, null, null, "email-invalido", null, null, null);
        violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testValidPhone() {
        // Teléfono válido
        DTOActualizarAlumno validDto = new DTOActualizarAlumno(null, null, null, null, "123456789", null, null);
        Set<ConstraintViolation<DTOActualizarAlumno>> violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // Teléfono con prefijo válido
        validDto = new DTOActualizarAlumno(null, null, null, null, "+34 123 456 789", null, null);
        violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // Teléfono inválido (muy corto)
        DTOActualizarAlumno invalidDto = new DTOActualizarAlumno(null, null, null, null, "123", null, null);
        violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testNotEmptyIfPresent() {
        // Campo vacío (inválido)
        DTOActualizarAlumno invalidDto = new DTOActualizarAlumno("", null, null, null, null, null, null);
        Set<ConstraintViolation<DTOActualizarAlumno>> violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty());
        
        // Campo null (válido para actualización parcial)
        DTOActualizarAlumno validDto = new DTOActualizarAlumno(null, null, null, null, null, null, null);
        violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
        
        // Campo con contenido (válido)
        validDto = new DTOActualizarAlumno("Juan", null, null, null, null, null, null);
        violations = validator.validate(validDto);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testCompleteRegistration() {
        // Registro válido completo
        DTOCrearAlumno validRegistro = new DTOCrearAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            "123456789"
        );
        
        Set<ConstraintViolation<DTOCrearAlumno>> violations = validator.validate(validRegistro);
        assertTrue(violations.isEmpty());
        
        // Registro con email inválido
        DTOCrearAlumno invalidRegistro = new DTOCrearAlumno(
            "usuario123",
            "password123",
            "Juan",
            "Pérez García",
            "12345678Z",
            "email-invalido",
            "123456789"
        );
        
        violations = validator.validate(invalidRegistro);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
}
