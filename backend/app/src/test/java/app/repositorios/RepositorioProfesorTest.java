package app.repositorios;

import app.entidades.Profesor;
import app.entidades.Clase;
import app.entidades.Curso;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests para RepositorioProfesor")
class RepositorioProfesorTest {

    @Autowired
    private RepositorioProfesor repositorioProfesor;

    @Autowired
    private RepositorioClase repositorioClase;

    @Autowired
    private TestEntityManager entityManager;

    private Profesor profesor1;
    private Profesor profesor2;
    private Profesor profesor3;
    private Clase clase1;
    private Clase clase2;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        repositorioProfesor.deleteAll();
        repositorioClase.deleteAll();

        // Crear profesores de prueba con DNIs válidos
        // 12345678 % 23 = 14, DNI_LETTERS[14] = 'Z' ✓
        // 87654321 % 23 = 10, DNI_LETTERS[10] = 'X' 
        // 11223344 % 23 = 11, DNI_LETTERS[11] = 'B'
        profesor1 = new Profesor("profesor1", "password1", "María", "García", "12345678Z", "maria@ejemplo.com", "123456789");
        profesor1.setEnabled(true);

        profesor2 = new Profesor("profesor2", "password2", "Juan", "Pérez", "87654321X", "juan@ejemplo.com", "987654321");
        profesor2.setEnabled(true);

        profesor3 = new Profesor("profesor3", "password3", "Ana", "López", "11223344B", "ana@ejemplo.com", "555666777");
        profesor3.setEnabled(false);

        // Crear clases de prueba
        clase1 = new Curso(
                "Clase 1", "Descripción de la clase 1", new BigDecimal("99.99"),
                EPresencialidad.ONLINE, "imagen1.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(30)
        );

        clase2 = new Curso(
                "Clase 2", "Descripción de la clase 2", new BigDecimal("149.99"),
                EPresencialidad.PRESENCIAL, "imagen2.jpg", EDificultad.INTERMEDIO,
                LocalDate.now().plusDays(14), LocalDate.now().plusDays(45)
        );

        // Guardar clases primero
        clase1 = repositorioClase.save(clase1);
        clase2 = repositorioClase.save(clase2);

        // Asignar clases a profesores usando JPA relationships
        profesor1.agregarClase(clase1);
        profesor1.agregarClase(clase2);
        profesor3.agregarClase(clase1);

        // Guardar profesores
        repositorioProfesor.save(profesor1);
        repositorioProfesor.save(profesor2);
        repositorioProfesor.save(profesor3);

        // Flush para asegurar que las relaciones se persistan
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByUsername debe encontrar profesor por usuario")
    void testFindByUsername() {
        Optional<Profesor> resultado = repositorioProfesor.findByUsername("profesor1");

        assertTrue(resultado.isPresent());
        assertEquals("profesor1", resultado.get().getUsername());
        assertEquals("María", resultado.get().getFirstName());
    }

    @Test
    @DisplayName("findByUsername debe retornar empty cuando no existe")
    void testFindByUsernameNoExiste() {
        Optional<Profesor> resultado = repositorioProfesor.findByUsername("inexistente");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByEmail debe encontrar profesor por email")
    void testFindByEmail() {
        Optional<Profesor> resultado = repositorioProfesor.findByEmail("maria@ejemplo.com");

        assertTrue(resultado.isPresent());
        assertEquals("maria@ejemplo.com", resultado.get().getEmail());
        assertEquals("María", resultado.get().getFirstName());
    }

    @Test
    @DisplayName("findByEmail debe retornar empty cuando no existe")
    void testFindByEmailNoExiste() {
        Optional<Profesor> resultado = repositorioProfesor.findByEmail("inexistente@ejemplo.com");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByDni debe encontrar profesor por DNI")
    void testFindByDni() {
        Optional<Profesor> resultado = repositorioProfesor.findByDni("12345678Z");

        assertTrue(resultado.isPresent());
        assertEquals("12345678Z", resultado.get().getDni());
        assertEquals("María", resultado.get().getFirstName());
    }

    @Test
    @DisplayName("findByDni debe retornar empty cuando no existe")
    void testFindByDniNoExiste() {
        // Usar un DNI válido que no existe en la base de datos
        Optional<Profesor> resultado = repositorioProfesor.findByDni("99999999R");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe encontrar profesores por nombre")
    void testFindByNombreContainingIgnoreCase() {
        List<Profesor> resultado = repositorioProfesor.findByNombreContainingIgnoreCase("María");

        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getFirstName());
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe ser case insensitive")
    void testFindByNombreContainingIgnoreCaseCaseInsensitive() {
        List<Profesor> resultado = repositorioProfesor.findByNombreContainingIgnoreCase("maría");

        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getFirstName());
    }

    @Test
    @DisplayName("findByNombreContainingIgnoreCase debe retornar lista vacía cuando no encuentra")
    void testFindByNombreContainingIgnoreCaseNoEncuentra() {
        List<Profesor> resultado = repositorioProfesor.findByNombreContainingIgnoreCase("Inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("findByApellidosContainingIgnoreCase debe encontrar profesores por apellidos")
    void testFindByApellidosContainingIgnoreCase() {
        List<Profesor> resultado = repositorioProfesor.findByApellidosContainingIgnoreCase("García");

        assertEquals(1, resultado.size());
        assertEquals("García", resultado.get(0).getLastName());
    }

    @Test
    @DisplayName("findByApellidosContainingIgnoreCase debe ser case insensitive")
    void testFindByApellidosContainingIgnoreCaseCaseInsensitive() {
        List<Profesor> resultado = repositorioProfesor.findByApellidosContainingIgnoreCase("garcía");

        assertEquals(1, resultado.size());
        assertEquals("García", resultado.get(0).getLastName());
    }

    @Test
    @DisplayName("findByEnabledTrue debe encontrar profesores habilitados")
    void testFindByEnabledTrue() {
        List<Profesor> resultado = repositorioProfesor.findByEnabledTrue();

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Profesor::isEnabled));
    }

    @Test
    @DisplayName("findAllOrderedById debe retornar todos los profesores ordenados")
    void testFindAllOrderedById() {
        List<Profesor> resultado = repositorioProfesor.findAllOrderedById();

        assertEquals(3, resultado.size());
        // Verificar que están ordenados por ID
        assertTrue(resultado.get(0).getId() < resultado.get(1).getId());
        assertTrue(resultado.get(1).getId() < resultado.get(2).getId());
    }

    @Test
    @DisplayName("findByClaseId debe encontrar profesores por clase")
    void testFindByClaseId() {
        List<Profesor> resultado = repositorioProfesor.findByClaseId(clase1.getId());

        assertEquals(2, resultado.size()); // profesor1 y profesor3 tienen clase1
        assertTrue(resultado.stream().anyMatch(p -> p.getUsername().equals("profesor1")));
        assertTrue(resultado.stream().anyMatch(p -> p.getUsername().equals("profesor3")));
    }

    @Test
    @DisplayName("findByClaseId debe retornar lista vacía cuando no encuentra")
    void testFindByClaseIdNoEncuentra() {
        List<Profesor> resultado = repositorioProfesor.findByClaseId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("countClasesByProfesorId debe contar clases correctamente")
    void testCountClasesByProfesorId() {
        Integer resultado = repositorioProfesor.countClasesByProfesorId(profesor1.getId());

        assertEquals(2, resultado);
    }

    @Test
    @DisplayName("countClasesByProfesorId debe retornar 0 para profesor sin clases")
    void testCountClasesByProfesorIdSinClases() {
        Integer resultado = repositorioProfesor.countClasesByProfesorId(profesor2.getId());

        assertEquals(0, resultado);
    }

    @Test
    @DisplayName("findProfesoresSinClases debe encontrar profesores sin clases")
    void testFindProfesoresSinClases() {
        List<Profesor> resultado = repositorioProfesor.findProfesoresSinClases();

        assertEquals(1, resultado.size());
        assertEquals("profesor2", resultado.get(0).getUsername());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por nombre")
    void testFindByFiltrosPorNombre() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                "María", null, null, null, null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getFirstName());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por apellidos")
    void testFindByFiltrosPorApellidos() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, "García", null, null, null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("García", resultado.get(0).getLastName());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por email")
    void testFindByFiltrosPorEmail() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, "maria@ejemplo.com", null, null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("maria@ejemplo.com", resultado.get(0).getEmail());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por usuario")
    void testFindByFiltrosPorUsuario() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, "profesor1", null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("profesor1", resultado.get(0).getUsername());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por DNI")
    void testFindByFiltrosPorDni() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, "12345678Z", null, null, null);

        assertEquals(1, resultado.size());
        assertEquals("12345678Z", resultado.get(0).getDni());
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por habilitado")
    void testFindByFiltrosPorHabilitado() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, null, true, null, null);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Profesor::isEnabled));
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por clase")
    void testFindByFiltrosPorClase() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, null, null, clase1.getId(), null);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().anyMatch(p -> p.getUsername().equals("profesor1")));
        assertTrue(resultado.stream().anyMatch(p -> p.getUsername().equals("profesor3")));
    }

    @Test
    @DisplayName("findByFiltros debe filtrar por sin clases")
    void testFindByFiltrosPorSinClases() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, null, null, null, true);

        assertEquals(1, resultado.size());
        assertEquals("profesor2", resultado.get(0).getUsername());
    }

    @Test
    @DisplayName("findByFiltros debe combinar múltiples filtros")
    void testFindByFiltrosMultiples() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                "María", "García", null, null, null, true, null, null);

        assertEquals(1, resultado.size());
        assertEquals("María", resultado.get(0).getFirstName());
        assertEquals("García", resultado.get(0).getLastName());
        assertTrue(resultado.get(0).isEnabled());
    }

    @Test
    @DisplayName("findByFiltros debe retornar todos cuando no hay filtros")
    void testFindByFiltrosSinFiltros() {
        List<Profesor> resultado = repositorioProfesor.findByFiltros(
                null, null, null, null, null, null, null, null);

        assertEquals(3, resultado.size());
    }

    @Test
    @DisplayName("save debe guardar profesor correctamente")
    void testSave() {
        Profesor nuevoProfesor = new Profesor("nuevo", "password", "Nuevo", "Profesor", "99999999A", "nuevo@ejemplo.com", "111222333");
        nuevoProfesor.agregarClase(clase1);

        Profesor resultado = repositorioProfesor.save(nuevoProfesor);

        assertNotNull(resultado.getId());
        assertEquals("nuevo", resultado.getUsername());
        assertEquals("Nuevo", resultado.getFirstName());
        assertTrue(resultado.getClasses().contains(clase1));
    }

    @Test
    @DisplayName("findById debe encontrar profesor por ID")
    void testFindById() {
        Optional<Profesor> resultado = repositorioProfesor.findById(profesor1.getId());

        assertTrue(resultado.isPresent());
        assertEquals(profesor1.getId(), resultado.get().getId());
        assertEquals("profesor1", resultado.get().getUsername());
    }

    @Test
    @DisplayName("findById debe retornar empty cuando no existe")
    void testFindByIdNoExiste() {
        Optional<Profesor> resultado = repositorioProfesor.findById(999L);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("existsById debe retornar true cuando existe")
    void testExistsById() {
        boolean resultado = repositorioProfesor.existsById(profesor1.getId());

        assertTrue(resultado);
    }

    @Test
    @DisplayName("existsById debe retornar false cuando no existe")
    void testExistsByIdNoExiste() {
        boolean resultado = repositorioProfesor.existsById(999L);

        assertFalse(resultado);
    }

    @Test
    @DisplayName("deleteById debe eliminar profesor correctamente")
    void testDeleteById() {
        Long idAEliminar = profesor1.getId();
        
        // Remove relationships first to avoid foreign key constraint violations
        // Clear the bidirectional relationship properly
        List<Clase> clasesToRemove = new ArrayList<>(profesor1.getClasses());
        for (Clase clase : clasesToRemove) {
            clase.getTeachers().remove(profesor1);
            entityManager.merge(clase);
        }
        profesor1.getClasses().clear();
        entityManager.merge(profesor1);
        entityManager.flush();
        
        repositorioProfesor.deleteById(idAEliminar);
        
        assertFalse(repositorioProfesor.existsById(idAEliminar));
        assertEquals(2, repositorioProfesor.count());
    }

    @Test
    @DisplayName("count debe retornar número correcto de profesores")
    void testCount() {
        long resultado = repositorioProfesor.count();

        assertEquals(3, resultado);
    }

    @Test
    @DisplayName("deleteAll debe eliminar todos los profesores")
    void testDeleteAll() {
        // Remove all relationships first to avoid foreign key constraint violations
        repositorioProfesor.findAll().forEach(profesor -> {
            // Clear the bidirectional relationship properly
            List<Clase> clasesToRemove = new ArrayList<>(profesor.getClasses());
            for (Clase clase : clasesToRemove) {
                clase.getTeachers().remove(profesor);
                entityManager.merge(clase);
            }
            profesor.getClasses().clear();
            entityManager.merge(profesor);
        });
        entityManager.flush();
        
        repositorioProfesor.deleteAll();

        assertEquals(0, repositorioProfesor.count());
    }
}
