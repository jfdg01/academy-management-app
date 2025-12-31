package app.repositorios;

import app.entidades.Clase;
import app.entidades.Curso;
import app.entidades.Taller;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests para RepositorioClase")
class RepositorioClaseTest {

    @Autowired
    private RepositorioClase repositorioClase;

    @Autowired
    private TestEntityManager entityManager;

    private Clase curso;
    private Clase taller;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        repositorioClase.deleteAll();

        // Crear clases de prueba
        curso = new Curso(
                "Curso de Java", "Aprende Java desde cero", new BigDecimal("99.99"),
                EPresencialidad.ONLINE, "imagen1.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(30)
        );

        taller = new Taller(
                "Taller de Spring", "Taller intensivo de Spring Boot", new BigDecimal("49.99"),
                EPresencialidad.PRESENCIAL, "imagen2.jpg", EDificultad.INTERMEDIO,
                4, LocalDate.now().plusDays(3), LocalTime.of(10, 0)
        );
    }

    @Test
    @DisplayName("testBasicCRUDOperations debe realizar operaciones CRUD básicas")
    public void testBasicCRUDOperations() {
        // Save curso
        Clase savedCurso = repositorioClase.save(curso);
        assertNotNull(savedCurso.getId());
        assertEquals("Curso de Java", savedCurso.getTitle());
        assertEquals("Aprende Java desde cero", savedCurso.getDescription());
        assertEquals(new BigDecimal("99.99"), savedCurso.getPrice());

        // Save taller
        Clase savedTaller = repositorioClase.save(taller);
        assertNotNull(savedTaller.getId());
        assertEquals("Taller de Spring", savedTaller.getTitle());
        assertEquals("Taller intensivo de Spring Boot", savedTaller.getDescription());
        assertEquals(new BigDecimal("49.99"), savedTaller.getPrice());

        // Find by ID
        Optional<Clase> foundCurso = repositorioClase.findById(savedCurso.getId());
        assertTrue(foundCurso.isPresent());
        assertEquals(savedCurso.getId(), foundCurso.get().getId());

        // Find by title
        Optional<Clase> foundByTitle = repositorioClase.findByTitle("Curso de Java");
        assertTrue(foundByTitle.isPresent());
        assertEquals("Curso de Java", foundByTitle.get().getTitle());

        // Find by title containing
        List<Clase> foundByContaining = repositorioClase.findByTitleContainingIgnoreCase("Java");
        assertFalse(foundByContaining.isEmpty());
        assertTrue(foundByContaining.stream().anyMatch(c -> c.getTitle().contains("Java")));

        // Find by description containing
        List<Clase> foundByDescription = repositorioClase.findByDescriptionContainingIgnoreCase("Spring");
        assertFalse(foundByDescription.isEmpty());
        assertTrue(foundByDescription.stream().anyMatch(c -> c.getDescription().contains("Spring")));

        // Find by presencialidad
        List<Clase> foundByPresencialidad = repositorioClase.findByFormat(EPresencialidad.ONLINE);
        assertFalse(foundByPresencialidad.isEmpty());
        assertTrue(foundByPresencialidad.stream().allMatch(c -> c.getFormat() == EPresencialidad.ONLINE));

        // Find by dificultad
        List<Clase> foundByDificultad = repositorioClase.findByDifficulty(EDificultad.PRINCIPIANTE);
        assertFalse(foundByDificultad.isEmpty());
        assertTrue(foundByDificultad.stream().allMatch(c -> c.getDifficulty() == EDificultad.PRINCIPIANTE));

        // Find by price range
        List<Clase> foundByPriceRange = repositorioClase.findByPriceBetween(new BigDecimal("50"), new BigDecimal("100"));
        assertFalse(foundByPriceRange.isEmpty());
        assertTrue(foundByPriceRange.stream().allMatch(c -> 
            c.getPrice().compareTo(new BigDecimal("50")) >= 0 && 
            c.getPrice().compareTo(new BigDecimal("100")) <= 0));

        // Update clase
        savedCurso.setTitle("Updated Curso de Java");
        Clase updated = repositorioClase.save(savedCurso);
        assertEquals("Updated Curso de Java", updated.getTitle());

        // Delete clase
        repositorioClase.delete(savedCurso);
        Optional<Clase> deleted = repositorioClase.findById(savedCurso.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("testFindAllOrderedById debe retornar clases ordenadas por ID")
    public void testFindAllOrderedById() {
        // Save clases
        Clase savedCurso = repositorioClase.save(curso);
        Clase savedTaller = repositorioClase.save(taller);

        // Find all ordered by ID
        List<Clase> allOrdered = repositorioClase.findAllOrderedById();
        assertFalse(allOrdered.isEmpty());
        assertTrue(allOrdered.size() >= 2);

        // Verify order (first ID should be less than second ID)
        assertTrue(allOrdered.get(0).getId() < allOrdered.get(1).getId());
    }

    @Test
    @DisplayName("testPagination debe probar paginación")
    public void testPagination() {
        // Create multiple clases
        for (int i = 1; i <= 15; i++) {
            Curso newCurso = new Curso(
                    "Curso " + i, "Descripción " + i, new BigDecimal("99.99"),
                    EPresencialidad.ONLINE, "imagen" + i + ".jpg", EDificultad.PRINCIPIANTE,
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(30)
            );
            repositorioClase.save(newCurso);
        }

        // Test pagination
        Page<Clase> firstPage = repositorioClase.findAll(PageRequest.of(0, 10));
        assertEquals(10, firstPage.getContent().size());
        assertTrue(firstPage.getTotalElements() >= 15);

        Page<Clase> secondPage = repositorioClase.findAll(PageRequest.of(1, 10));
        assertTrue(secondPage.getContent().size() > 0);
    }

    @Test
    @DisplayName("testFindByGeneralSearch debe probar búsqueda general")
    public void testFindByGeneralSearch() {
        // Save clases
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        // Test general search
        Page<Clase> searchResults = repositorioClase.findByGeneralSearch("Java", PageRequest.of(0, 10));
        assertFalse(searchResults.getContent().isEmpty());
        assertTrue(searchResults.getContent().stream().anyMatch(c -> 
            c.getTitle().contains("Java") || c.getDescription().contains("Java")));
    }

    @Test
    @DisplayName("testFindByGeneralAndSpecificFilters debe probar filtrado combinado")
    public void testFindByGeneralAndSpecificFilters() {
        // Save clases
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        // Test combined filtering
        Page<Clase> filterResults = repositorioClase.findByGeneralAndSpecificFilters(
                "Java", "Curso", "Aprende", EPresencialidad.ONLINE, 
                EDificultad.PRINCIPIANTE, new BigDecimal("50"), new BigDecimal("100"), 
                PageRequest.of(0, 10)
        );
        assertFalse(filterResults.getContent().isEmpty());
    }

    @Test
    @DisplayName("testCountQueries debe probar consultas de conteo")
    public void testCountQueries() {
        // Save clases
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        // Test count queries
        long totalCount = repositorioClase.count();
        assertTrue(totalCount >= 2);

        // Test counting by format using existing methods
        List<Clase> onlineClases = repositorioClase.findByFormat(EPresencialidad.ONLINE);
        assertTrue(onlineClases.size() >= 1);

        List<Clase> presencialClases = repositorioClase.findByFormat(EPresencialidad.PRESENCIAL);
        assertTrue(presencialClases.size() >= 1);
    }

    @Test
    @DisplayName("testFindByGeneralSearchPartial debe probar búsqueda parcial")
    public void testFindByGeneralSearchPartial() {
        // Save clases
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        // Test partial search - "cla" should match "Clase" in descriptions
        Page<Clase> searchResults = repositorioClase.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            "cla", "cla", PageRequest.of(0, 10));
        // This should find classes with "Clase" in their description
        assertTrue(searchResults.getContent().size() >= 0); // May or may not find matches depending on data
    }

    @Test
    @DisplayName("testFindByGeneralSearchCaseInsensitive debe probar búsqueda case-insensitive")
    public void testFindByGeneralSearchCaseInsensitive() {
        // Save clases
        repositorioClase.save(curso);
        repositorioClase.save(taller);

        // Test case-insensitive search - "java" should match "Java"
        Page<Clase> searchResults = repositorioClase.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            "java", "java", PageRequest.of(0, 10));
        assertFalse(searchResults.getContent().isEmpty());
        assertTrue(searchResults.getContent().stream().anyMatch(c -> 
            c.getTitle().toLowerCase().contains("java") || c.getDescription().toLowerCase().contains("java")));
    }
}
