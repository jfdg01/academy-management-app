package app.dtos;

import app.entidades.Clase;
import app.entidades.Curso;
import app.entidades.Profesor;
import app.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests para DTOProfesor")
class DTOProfesorTest {

    private DTOProfesor dtoProfesor;
    private Profesor profesor;
    
    private final Long ID = 1L;
    private final String USUARIO = "profesor123";
    private final String NOMBRE = "María";
    private final String APELLIDOS = "García López";
    private final String DNI = "87654321A";
    private final String EMAIL = "maria.garcia@ejemplo.com";
    private final String TELEFONO = "987654321";
    private final Usuario.Role Role = Usuario.Role.PROFESOR;
    private final boolean ENABLED = true;
    private final LocalDateTime FECHA_CREACION = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        dtoProfesor = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        
        profesor = new Profesor(USUARIO, "password123", NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO);
        profesor.setId(ID);
        profesor.setEnabled(ENABLED);
    }

    @Test
    @DisplayName("Constructor con parámetros debe crear DTO correctamente")
    void testConstructorConParametros() {
        assertNotNull(dtoProfesor);
        assertEquals(ID, dtoProfesor.id());
        assertEquals(USUARIO, dtoProfesor.username());
        assertEquals(NOMBRE, dtoProfesor.firstName());
        assertEquals(APELLIDOS, dtoProfesor.lastName());
        assertEquals(DNI, dtoProfesor.dni());
        assertEquals(EMAIL, dtoProfesor.email());
        assertEquals(TELEFONO, dtoProfesor.phoneNumber());
        assertEquals(Role, dtoProfesor.role());
        assertEquals(ENABLED, dtoProfesor.enabled());
        assertNotNull(dtoProfesor.classIds());
        assertEquals(FECHA_CREACION, dtoProfesor.createdAt());
    }

    @Test
    @DisplayName("Constructor desde entidad Profesor debe crear DTO correctamente")
    void testConstructorDesdeEntidad() {
        // Agregar algunas clases al profesor
        Clase clase1 = new Curso();
        clase1.setId(1L);
        clase1.setTitle("Matemáticas");
        
        Clase clase2 = new Curso();
        clase2.setId(2L);
        clase2.setTitle("Física");
        
        profesor.agregarClase(clase1);
        profesor.agregarClase(clase2);
        
        DTOProfesor dtoDesdeEntidad = new DTOProfesor(profesor);
        
        assertNotNull(dtoDesdeEntidad);
        assertEquals(profesor.getId(), dtoDesdeEntidad.id());
        assertEquals(profesor.getUsername(), dtoDesdeEntidad.username());
        assertEquals(profesor.getFirstName(), dtoDesdeEntidad.firstName());
        assertEquals(profesor.getLastName(), dtoDesdeEntidad.lastName());
        assertEquals(profesor.getDni(), dtoDesdeEntidad.dni());
        assertEquals(profesor.getEmail(), dtoDesdeEntidad.email());
        assertEquals(profesor.getPhoneNumber(), dtoDesdeEntidad.phoneNumber());
        assertEquals(profesor.getRole(), dtoDesdeEntidad.role());
        assertEquals(profesor.isEnabled(), dtoDesdeEntidad.enabled());
        assertEquals(2, dtoDesdeEntidad.classIds().size());
        assertTrue(dtoDesdeEntidad.classIds().contains(1L));
        assertTrue(dtoDesdeEntidad.classIds().contains(2L));
        assertNotNull(dtoDesdeEntidad.createdAt());
    }

    @Test
    @DisplayName("Método from debe crear DTO desde entidad")
    void testMetodoFrom() {
        DTOProfesor dtoFrom = DTOProfesor.from(profesor);
        
        assertNotNull(dtoFrom);
        assertEquals(profesor.getId(), dtoFrom.id());
        assertEquals(profesor.getUsername(), dtoFrom.username());
        assertEquals(profesor.getFirstName(), dtoFrom.firstName());
        assertEquals(profesor.getLastName(), dtoFrom.lastName());
        assertEquals(profesor.getDni(), dtoFrom.dni());
        assertEquals(profesor.getEmail(), dtoFrom.email());
        assertEquals(profesor.getPhoneNumber(), dtoFrom.phoneNumber());
        assertEquals(profesor.getRole(), dtoFrom.role());
        assertEquals(profesor.isEnabled(), dtoFrom.enabled());
        assertEquals(0, dtoFrom.classIds().size()); // No classes assigned yet
    }

    @Test
    @DisplayName("Método getNombreCompleto debe retornar nombre completo")
    void testGetNombreCompleto() {
        String nombreCompleto = dtoProfesor.getFullName();
        assertEquals(NOMBRE + " " + APELLIDOS, nombreCompleto);
    }

    @Test
    @DisplayName("Método getNombreCompleto con nombres con espacios")
    void testGetNombreCompletoConEspacios() {
        DTOProfesor dtoConEspacios = new DTOProfesor(ID, USUARIO, "María José", "García López", DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        String nombreCompleto = dtoConEspacios.getFullName();
        assertEquals("María José García López", nombreCompleto);
    }

    @Test
    @DisplayName("Método tieneClases debe retornar false cuando no hay clases")
    void testTieneClasesSinClases() {
        assertFalse(dtoProfesor.hasClasses());
    }

    @Test
    @DisplayName("Método tieneClases debe retornar true cuando hay clases")
    void testTieneClasesConClases() {
        List<Long> clases = new ArrayList<>();
        clases.add(1L);
        clases.add(2L);
        
        DTOProfesor dtoConClases = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, clases, FECHA_CREACION);
        assertTrue(dtoConClases.hasClasses());
    }

    @Test
    @DisplayName("Método tieneClases debe retornar false cuando clasesId es null")
    void testTieneClasesConClasesIdNull() {
        DTOProfesor dtoConClasesNull = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, null, FECHA_CREACION);
        assertFalse(dtoConClasesNull.hasClasses());
    }

    @Test
    @DisplayName("Método getNumeroClases debe retornar 0 cuando no hay clases")
    void testGetNumeroClasesSinClases() {
        assertEquals(0, dtoProfesor.getClassCount());
    }

    @Test
    @DisplayName("Método getNumeroClases debe retornar número correcto de clases")
    void testGetNumeroClasesConClases() {
        List<Long> clases = new ArrayList<>();
        clases.add(1L);
        clases.add(2L);
        clases.add(3L);
        
        DTOProfesor dtoConClases = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, clases, FECHA_CREACION);
        assertEquals(3, dtoConClases.getClassCount());
    }

    @Test
    @DisplayName("Método getNumeroClases debe retornar 0 cuando clasesId es null")
    void testGetNumeroClasesConClasesIdNull() {
        DTOProfesor dtoConClasesNull = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, null, FECHA_CREACION);
        assertEquals(0, dtoConClasesNull.getClassCount());
    }

    @Test
    @DisplayName("Método getEnabledStatus debe retornar texto correcto cuando está habilitado")
    void testGetEnabledStatusHabilitado() {
        assertEquals("Enabled", dtoProfesor.getEnabledStatus());
    }

    @Test
    @DisplayName("Método getEnabledStatus debe retornar texto correcto cuando está deshabilitado")
    void testGetEnabledStatusDeshabilitado() {
        DTOProfesor dtoDeshabilitado = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, false, new ArrayList<>(), FECHA_CREACION);
        assertEquals("Disabled", dtoDeshabilitado.getEnabledStatus());
    }

    @Test
    @DisplayName("Método isEnabled debe retornar true cuando está habilitado")
    void testIsEnabledHabilitado() {
        assertTrue(dtoProfesor.isEnabled());
    }

    @Test
    @DisplayName("Método isEnabled debe retornar false cuando está deshabilitado")
    void testIsEnabledDeshabilitado() {
        DTOProfesor dtoDeshabilitado = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, false, new ArrayList<>(), FECHA_CREACION);
        assertFalse(dtoDeshabilitado.isEnabled());
    }

    @Test
    @DisplayName("Constructor desde entidad con clases debe crear DTO correctamente")
    void testConstructorDesdeEntidadConClases() {
        // Crear clases y agregarlas al profesor
        Clase clase1 = new Curso();
        clase1.setId(1L);
        clase1.setTitle("Matemáticas");
        
        Clase clase2 = new Curso();
        clase2.setId(2L);
        clase2.setTitle("Física");
        
        profesor.agregarClase(clase1);
        profesor.agregarClase(clase2);
        
        DTOProfesor dtoDesdeEntidad = new DTOProfesor(profesor);
        
        assertNotNull(dtoDesdeEntidad);
        assertEquals(2, dtoDesdeEntidad.classIds().size());
        assertTrue(dtoDesdeEntidad.classIds().contains(1L));
        assertTrue(dtoDesdeEntidad.classIds().contains(2L));
        assertTrue(dtoDesdeEntidad.hasClasses());
        assertEquals(2, dtoDesdeEntidad.getClassCount());
    }

    @Test
    @DisplayName("Equals y hashCode deben funcionar correctamente")
    void testEqualsYHashCode() {
        DTOProfesor dto1 = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        DTOProfesor dto2 = new DTOProfesor(ID, USUARIO, NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        DTOProfesor dto3 = new DTOProfesor(2L, "otro", NOMBRE, APELLIDOS, DNI, EMAIL, TELEFONO, Role, ENABLED, new ArrayList<>(), FECHA_CREACION);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
}
