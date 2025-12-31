package app.dtos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para DTOParametrosBusquedaAlumno")
class DTOParametrosBusquedaAlumnoTest {

    @Test
    @DisplayName("Constructor debe crear DTO correctamente con todos los parámetros")
    void testConstructorCompleto() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            true
        );

        assertEquals("Juan", dto.firstName());
        assertEquals("Pérez García", dto.lastName());
        assertEquals("12345678Z", dto.dni());
        assertEquals("juan@ejemplo.com", dto.email());
        assertTrue(dto.enrolled());
    }

    @Test
    @DisplayName("Constructor debe crear DTO con todos los parámetros null")
    void testConstructorConParametrosNull() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            null,
            null,
            null,
            null,
            null
        );

        assertNull(dto.firstName());
        assertNull(dto.lastName());
        assertNull(dto.dni());
        assertNull(dto.email());
        assertNull(dto.enrolled());
    }

    @Test
    @DisplayName("Constructor debe crear DTO con algunos parámetros null")
    void testConstructorConAlgunosParametrosNull() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            "Juan",
            null,
            "12345678Z",
            null,
            false
        );

        assertEquals("Juan", dto.firstName());
        assertNull(dto.lastName());
        assertEquals("12345678Z", dto.dni());
        assertNull(dto.email());
        assertFalse(dto.enrolled());
    }

    @Test
    @DisplayName("Record debe ser inmutable")
    void testInmutabilidad() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            true
        );

        // Los records son inmutables por defecto, no se pueden modificar
        // Solo podemos verificar que los valores se mantienen
        assertEquals("Juan", dto.firstName());
        assertEquals("Pérez García", dto.lastName());
        assertEquals("12345678Z", dto.dni());
    }

    @Test
    @DisplayName("Equals debe funcionar correctamente")
    void testEquals() {
        DTOParametrosBusquedaAlumno dto1 = new DTOParametrosBusquedaAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            true
        );

        DTOParametrosBusquedaAlumno dto2 = new DTOParametrosBusquedaAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            true
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Equals debe funcionar con parámetros null")
    void testEqualsConParametrosNull() {
        DTOParametrosBusquedaAlumno dto1 = new DTOParametrosBusquedaAlumno(
            null,
            null,
            null,
            null,
            null
        );

        DTOParametrosBusquedaAlumno dto2 = new DTOParametrosBusquedaAlumno(
            null,
            null,
            null,
            null,
            null
        );

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("ToString debe contener información relevante")
    void testToString() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            "Juan",
            "Pérez García",
            "12345678Z",
            "juan@ejemplo.com",
            true
        );

        String toString = dto.toString();

        assertTrue(toString.contains("Juan"));
        assertTrue(toString.contains("Pérez García"));
        assertTrue(toString.contains("juan@ejemplo.com"));
        assertTrue(toString.contains("true"));
    }

    @Test
    @DisplayName("ToString debe manejar parámetros null")
    void testToStringConParametrosNull() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            null,
            "Pérez García",
            null,
            "juan@ejemplo.com",
            null
        );

        String toString = dto.toString();

        assertTrue(toString.contains("Pérez García"));
        assertTrue(toString.contains("juan@ejemplo.com"));
    }

    @Test
    @DisplayName("Constructor debe manejar diferentes valores de matrícula")
    void testDiferentesValoresMatricula() {
        // Alumno matriculado
        DTOParametrosBusquedaAlumno dto1 = new DTOParametrosBusquedaAlumno(
            "Juan", "Pérez", "12345678Z", "juan@ejemplo.com", true
        );
        assertTrue(dto1.enrolled());

        // Alumno no matriculado
        DTOParametrosBusquedaAlumno dto2 = new DTOParametrosBusquedaAlumno(
            "María", "García", "87654321Y", "maria@ejemplo.com", false
        );
        assertFalse(dto2.enrolled());

        // Sin especificar matrícula
        DTOParametrosBusquedaAlumno dto3 = new DTOParametrosBusquedaAlumno(
            "Carlos", "López", "11223344X", "carlos@ejemplo.com", null
        );
        assertNull(dto3.enrolled());
    }

    @Test
    @DisplayName("Constructor debe manejar nombres con acentos")
    void testNombresConAcentos() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            "José María",
            "García López",
            "12345678Z",
            "jose@ejemplo.com",
            true
        );

        assertEquals("José María", dto.firstName());
        assertEquals("García López", dto.lastName());
    }

    @Test
    @DisplayName("Constructor debe manejar DNI con letra")
    void testDNIConLetra() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            "Juan",
            "Pérez",
            "12345678Z",
            "juan@ejemplo.com",
            true
        );

        assertEquals("12345678Z", dto.dni());
    }

    @Test
    @DisplayName("Constructor debe manejar email con dominio")
    void testEmailConDominio() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            "Juan",
            "Pérez",
            "12345678Z",
            "juan.perez@empresa.com",
            true
        );

        assertEquals("juan.perez@empresa.com", dto.email());
    }

    @Test
    @DisplayName("Constructor debe manejar búsqueda por nombre parcial")
    void testBusquedaPorNombreParcial() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            "Juan",
            null,
            null,
            null,
            null
        );

        assertEquals("Juan", dto.firstName());
        assertNull(dto.lastName());
        assertNull(dto.dni());
        assertNull(dto.email());
        assertNull(dto.enrolled());
    }

    @Test
    @DisplayName("Constructor debe manejar búsqueda por email parcial")
    void testBusquedaPorEmailParcial() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            null,
            null,
            null,
            "juan@ejemplo.com",
            null
        );

        assertNull(dto.firstName());
        assertNull(dto.lastName());
        assertNull(dto.dni());
        assertEquals("juan@ejemplo.com", dto.email());
        assertNull(dto.enrolled());
    }

    @Test
    @DisplayName("Constructor debe manejar búsqueda solo por estado de matrícula")
    void testBusquedaSoloPorMatricula() {
        DTOParametrosBusquedaAlumno dto = new DTOParametrosBusquedaAlumno(
            null,
            null,
            null,
            null,
            true
        );

        assertNull(dto.firstName());
        assertNull(dto.lastName());
        assertNull(dto.dni());
        assertNull(dto.email());
        assertTrue(dto.enrolled());
    }
}
