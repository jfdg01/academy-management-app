package app.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para la entidad Profesor")
class ProfesorTest {

    private Profesor profesor;
    private final String USUARIO = "profesor123";
    private final String PASSWORD = "password123";
    private final String NOMBRE = "María";
    private final String APELLIDOS = "García López";
    private final String DNI = "87654321A";
    private final String EMAIL = "maria.garcia@ejemplo.com";
    private final String TELEFONO = "987654321";

    @BeforeEach
    void setUp() {
        profesor = new Profesor(USUARIO, PASSWORD, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
    }

    @Test
    @DisplayName("Constructor con parámetros debe crear profesor correctamente")
    void testConstructorConParametros() {
        // Verificar que se crea correctamente
        assertNotNull(profesor);
        assertEquals(USUARIO, profesor.getUsername());
        assertEquals(PASSWORD, profesor.getPassword());
        assertEquals(NOMBRE, profesor.getFirstName());
        assertEquals(APELLIDOS, profesor.getLastName());
        assertEquals(DNI, profesor.getDni());
        assertEquals(EMAIL, profesor.getEmail());
        assertEquals(TELEFONO, profesor.getPhoneNumber());
        assertEquals(Usuario.Role.PROFESOR, profesor.getRole());
        assertTrue(profesor.isEnabled());
        assertNotNull(profesor.getClasses());
        assertTrue(profesor.getClasses().isEmpty());
        assertEquals(0, profesor.getNumeroClases());
    }

    @Test
    @DisplayName("Constructor vacío debe crear profesor con valores por defecto")
    void testConstructorVacio() {
        Profesor profesorVacio = new Profesor();
        
        assertNotNull(profesorVacio);
        assertEquals(Usuario.Role.PROFESOR, profesorVacio.getRole());
        assertTrue(profesorVacio.isEnabled());
        assertNotNull(profesorVacio.getClasses());
        assertTrue(profesorVacio.getClasses().isEmpty());
        assertEquals(0, profesorVacio.getNumeroClases());
    }

    @Test
    @DisplayName("Getters y setters deben funcionar correctamente")
    void testGettersYSetters() {
        // Test enabled (heredado de Usuario)
        profesor.setEnabled(false);
        assertFalse(profesor.isEnabled());
        
        profesor.setEnabled(true);
        assertTrue(profesor.isEnabled());
    }

    @Test
    @DisplayName("Método resetearpassword debe lanzar UnsupportedOperationException")
    void testResetearPassword() {
        assertThrows(UnsupportedOperationException.class, () -> {
            profesor.resetearpassword();
        });
    }

    @Test
    @DisplayName("Método agregarClase debe agregar clase correctamente")
    void testAgregarClase() {
        Clase clase = new Curso();
        clase.setId(1L);
        clase.setTitle("Matemáticas");
        
        // Verificar que la lista está vacía inicialmente
        assertTrue(profesor.getClasses().isEmpty());
        assertEquals(0, profesor.getNumeroClases());
        
        // Agregar clase
        profesor.agregarClase(clase);
        
        // Verificar que se agregó
        assertTrue(profesor.getClasses().contains(clase));
        assertEquals(1, profesor.getClasses().size());
        assertEquals(1, profesor.getNumeroClases());
        
        // Verificar que la clase también tiene al profesor
        assertTrue(clase.getTeachers().contains(profesor));
        
        // Agregar la misma clase no debe duplicar
        profesor.agregarClase(clase);
        assertEquals(1, profesor.getClasses().size());
        assertEquals(1, profesor.getNumeroClases());
    }

    @Test
    @DisplayName("Método removerClase debe remover clase correctamente")
    void testRemoverClase() {
        Clase clase1 = new Curso();
        clase1.setId(1L);
        clase1.setTitle("Matemáticas");
        
        Clase clase2 = new Curso();
        clase2.setId(2L);
        clase2.setTitle("Física");
        
        // Agregar dos clases
        profesor.agregarClase(clase1);
        profesor.agregarClase(clase2);
        assertEquals(2, profesor.getClasses().size());
        assertEquals(2, profesor.getNumeroClases());
        
        // Remover una clase
        profesor.removerClase(clase1);
        assertEquals(1, profesor.getClasses().size());
        assertEquals(1, profesor.getNumeroClases());
        assertFalse(profesor.getClasses().contains(clase1));
        assertTrue(profesor.getClasses().contains(clase2));
        
        // Verificar que la clase también removió al profesor
        assertFalse(clase1.getTeachers().contains(profesor));
        assertTrue(clase2.getTeachers().contains(profesor));
    }

    @Test
    @DisplayName("Método imparteClasePorId debe retornar true si imparte la clase")
    void testImparteClasePorId() {
        Clase clase = new Curso();
        clase.setId(1L);
        clase.setTitle("Matemáticas");
        
        // Inicialmente no imparte ninguna clase
        assertFalse(profesor.imparteClasePorId(1L));
        assertFalse(profesor.imparteClasePorId(2L));
        
        // Agregar clase
        profesor.agregarClase(clase);
        
        // Ahora debe impartir la clase
        assertTrue(profesor.imparteClasePorId(1L));
        assertFalse(profesor.imparteClasePorId(2L));
    }

    @Test
    @DisplayName("Método getNumeroClases debe retornar el número correcto de clases")
    void testGetNumeroClases() {
        // Inicialmente 0 clases
        assertEquals(0, profesor.getNumeroClases());
        
        // Agregar una clase
        Clase clase1 = new Curso();
        clase1.setId(1L);
        profesor.agregarClase(clase1);
        assertEquals(1, profesor.getNumeroClases());
        
        // Agregar otra clase
        Clase clase2 = new Curso();
        clase2.setId(2L);
        profesor.agregarClase(clase2);
        assertEquals(2, profesor.getNumeroClases());
        
        // Remover una clase
        profesor.removerClase(clase1);
        assertEquals(1, profesor.getNumeroClases());
    }

    @Test
    @DisplayName("Equals y hashCode deben funcionar correctamente")
    void testEqualsYHashCode() {
        Profesor profesor1 = new Profesor("user1", "pass1", "Juan", "Pérez", "12345678A", "juan@test.com", "123456789");
        Profesor profesor2 = new Profesor("user2", "pass2", "María", "García", "87654321B", "maria@test.com", "987654321");
        Profesor profesor3 = new Profesor("user1", "pass1", "Juan", "Pérez", "12345678A", "juan@test.com", "123456789");
        
        // Establecer IDs para comparación
        profesor1.setId(1L);
        profesor2.setId(2L);
        profesor3.setId(1L);
        
        // Test equals
        assertEquals(profesor1, profesor3);
        assertNotEquals(profesor1, profesor2);
        assertNotEquals(profesor2, profesor3);
        
        // Test hashCode
        assertEquals(profesor1.hashCode(), profesor3.hashCode());
        assertNotEquals(profesor1.hashCode(), profesor2.hashCode());
    }

    @Test
    @DisplayName("Rol por defecto debe ser PROFESOR")
    void testRolPorDefecto() {
        assertEquals(Usuario.Role.PROFESOR, profesor.getRole());
        
        Profesor nuevoProfesor = new Profesor();
        assertEquals(Usuario.Role.PROFESOR, nuevoProfesor.getRole());
    }

    @Test
    @DisplayName("Enabled por defecto debe ser true")
    void testEnabledPorDefecto() {
        assertTrue(profesor.isEnabled());
        
        Profesor nuevoProfesor = new Profesor();
        assertTrue(nuevoProfesor.isEnabled());
    }

    @Test
    @DisplayName("Lista de clases por defecto debe estar vacía")
    void testClassesPorDefecto() {
        assertNotNull(profesor.getClasses());
        assertTrue(profesor.getClasses().isEmpty());
        assertEquals(0, profesor.getNumeroClases());
        
        Profesor nuevoProfesor = new Profesor();
        assertNotNull(nuevoProfesor.getClasses());
        assertTrue(nuevoProfesor.getClasses().isEmpty());
        assertEquals(0, nuevoProfesor.getNumeroClases());
    }
}
