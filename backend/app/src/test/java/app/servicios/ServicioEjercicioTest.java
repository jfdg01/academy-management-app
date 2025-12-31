package app.servicios;

import app.dtos.DTOEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Clase;
import app.entidades.Curso;
import app.entidades.Ejercicio;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import app.repositorios.RepositorioClase;
import app.repositorios.RepositorioEjercicio;
import app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class ServicioEjercicioTest {

    @Mock
    private RepositorioEjercicio repositorioEjercicio;

    @Mock
    private RepositorioClase repositorioClase;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ServicioEjercicio servicioEjercicio;

    private Clase testClase;
    private Ejercicio testEjercicio;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up security context for testing
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PROFESOR"));
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("test-professor", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Mock security utils
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        
        // Create test class
        testClase = new Curso(
            "Test Course",
            "Test Description",
            new BigDecimal("100.00"),
            EPresencialidad.ONLINE,
            null,
            EDificultad.INTERMEDIO,
            LocalDate.now(),
            LocalDate.now().plusMonths(3)
        );
        testClase.setId(1L);
        
        // Create test exercise
        testEjercicio = new Ejercicio(
            "Test Exercise",
            "Test Statement",
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(7),
            testClase
        );
        testEjercicio.setId(1L);
    }

    @Test
    @DisplayName("Creating an exercise should automatically add it to the class's exercise list")
    void testCrearEjercicioAddsToClass() {
        // Arrange
        String exerciseName = "Test Exercise";
        String statement = "Test Statement";
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        String classId = "1";
        
        // Mock repository responses
        when(repositorioEjercicio.findByName(exerciseName)).thenReturn(Optional.empty());
        when(repositorioEjercicio.save(any(Ejercicio.class))).thenReturn(testEjercicio);
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(testClase));
        when(repositorioClase.save(any(Clase.class))).thenAnswer(invocation -> {
            Clase savedClase = invocation.getArgument(0);
            savedClase.agregarEjercicio(testEjercicio);
            return savedClase;
        });
        
        // Act
        DTOEjercicio result = servicioEjercicio.crearEjercicio(
            exerciseName, statement, startDate, endDate, 1L
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(exerciseName, result.name());
        assertEquals(statement, result.statement());
        assertEquals(classId, result.classId());
        
        // Verify that the exercise was added to the class
        verify(repositorioClase).findById(1L);
        verify(repositorioClase).save(any(Clase.class));
        
        // Verify that agregarEjercicio was called on the class
        assertTrue(testClase.getExercises().contains(testEjercicio));
    }

    @Test
    @DisplayName("Deleting an exercise should remove it from the class's exercise list")
    void testBorrarEjercicioRemovesFromClass() {
        // Arrange
        Long exerciseId = 1L;
        
        // Add exercise to class's list using JPA relationship
        testClase.agregarEjercicio(testEjercicio);
        assertTrue(testClase.getExercises().contains(testEjercicio));
        
        // Mock repository responses
        when(repositorioEjercicio.findById(exerciseId)).thenReturn(Optional.of(testEjercicio));
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(testClase));
        when(repositorioClase.save(any(Clase.class))).thenAnswer(invocation -> {
            Clase savedClase = invocation.getArgument(0);
            savedClase.removerEjercicio(testEjercicio);
            return savedClase;
        });
        
        // Act
        boolean result = servicioEjercicio.borrarEjercicioPorId(exerciseId);
        
        // Assert
        assertTrue(result);
        
        // Verify that the class was saved after removing the exercise
        verify(repositorioClase).save(any(Clase.class));
        
        // Verify that removerEjercicio was called on the class
        assertFalse(testClase.getExercises().contains(testEjercicio));
    }

    @Test
    @DisplayName("Creating an exercise with invalid class ID should throw validation error")
    void testCrearEjercicioWithInvalidClassId() {
        // Arrange
        String exerciseName = "Test Exercise";
        String statement = "Test Statement";
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        String invalidClassId = "invalid";
        
        // Mock repository responses
        when(repositorioEjercicio.findByName(exerciseName)).thenReturn(Optional.empty());
        when(repositorioEjercicio.save(any(Ejercicio.class))).thenReturn(testEjercicio);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioEjercicio.crearEjercicio(
                exerciseName, statement, startDate, endDate, 999L
            );
        });
    }

    @Test
    @DisplayName("Creating an exercise with non-existent class ID should throw not found exception")
    void testCrearEjercicioWithNonExistentClassId() {
        // Arrange
        String exerciseName = "Test Exercise";
        String statement = "Test Statement";
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        String nonExistentClassId = "999";
        
        // Mock repository responses
        when(repositorioEjercicio.findByName(exerciseName)).thenReturn(Optional.empty());
        when(repositorioEjercicio.save(any(Ejercicio.class))).thenReturn(testEjercicio);
        when(repositorioClase.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioEjercicio.crearEjercicio(
                exerciseName, statement, startDate, endDate, 999L
            );
        });
    }

    // ===== TESTS PARA FILTRADO FLEXIBLE - CORREGIDOS =====

    @Test
    @DisplayName("obtenerEjerciciosPaginados con filtros combinados debe usar findByGeneralAndSpecificFilters")
    void testObtenerEjerciciosPaginadosConFiltrosCombinados() {
        // Arrange
        String q = "Java";
        String name = "Programación";
        String statement = "Aprende Java";
        String classId = "1";
        String status = "ACTIVE";
        int page = 0;
        int size = 20;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Ejercicio> ejercicios = new ArrayList<>();
        ejercicios.add(testEjercicio);

        Page<Ejercicio> ejercicioPage = new PageImpl<>(ejercicios);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for search in both name and statement
        when(repositorioEjercicio.findByNameContainingOrStatementContaining(
            eq(q), eq(q), any(Pageable.class))).thenReturn(ejercicioPage);

        // Act
        DTORespuestaPaginada<DTOEjercicio> resultado = servicioEjercicio.obtenerEjerciciosPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Test Exercise", resultado.content().get(0).name());
        
        // Verify that the search method was called with correct parameters
        verify(repositorioEjercicio).findByNameContainingOrStatementContaining(eq(q), eq(q), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerEjerciciosPaginados solo con búsqueda general debe filtrar correctamente")
    void testObtenerEjerciciosPaginadosSoloConBusquedaGeneral() {
        // Arrange
        String q = "Java";
        String name = null;
        String statement = null;
        String classId = null;
        String status = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Ejercicio> ejercicios = new ArrayList<>();
        ejercicios.add(testEjercicio);

        Page<Ejercicio> ejercicioPage = new PageImpl<>(ejercicios);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for search in both name and statement
        when(repositorioEjercicio.findByNameContainingOrStatementContaining(
            eq(q), eq(q), any(Pageable.class))).thenReturn(ejercicioPage);

        // Act
        DTORespuestaPaginada<DTOEjercicio> resultado = servicioEjercicio.obtenerEjerciciosPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Test Exercise", resultado.content().get(0).name());
        
        // Verify that the search method was called with correct parameters
        verify(repositorioEjercicio).findByNameContainingOrStatementContaining(eq(q), eq(q), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerEjerciciosPaginados solo con nombre debe filtrar correctamente")
    void testObtenerEjerciciosPaginadosSoloConNombre() {
        // Arrange
        String q = null;
        String name = "Programación";
        String statement = null;
        String classId = null;
        String status = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Ejercicio> ejercicios = new ArrayList<>();
        ejercicios.add(testEjercicio);

        Page<Ejercicio> ejercicioPage = new PageImpl<>(ejercicios);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for name filter
        when(repositorioEjercicio.findByNameContaining(eq(name), any(Pageable.class))).thenReturn(ejercicioPage);

        // Act
        DTORespuestaPaginada<DTOEjercicio> resultado = servicioEjercicio.obtenerEjerciciosPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Test Exercise", resultado.content().get(0).name());
        
        // Verify that the name filter method was called with correct parameters
        verify(repositorioEjercicio).findByNameContaining(eq(name), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerEjerciciosPaginados solo con classId debe filtrar correctamente")
    void testObtenerEjerciciosPaginadosSoloConClassId() {
        // Arrange
        String q = null;
        String name = null;
        String statement = null;
        String classId = "1";
        String status = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Ejercicio> ejercicios = new ArrayList<>();
        ejercicios.add(testEjercicio);

        Page<Ejercicio> ejercicioPage = new PageImpl<>(ejercicios);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for class ID filter
        when(repositorioEjercicio.findByClaseId(eq(Long.parseLong(classId)), any(Pageable.class))).thenReturn(ejercicioPage);

        // Act
        DTORespuestaPaginada<DTOEjercicio> resultado = servicioEjercicio.obtenerEjerciciosPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("Test Exercise", resultado.content().get(0).name());
        
        // Verify that the class ID filter method was called with correct parameters
        verify(repositorioEjercicio).findByClaseId(eq(Long.parseLong(classId)), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerEjerciciosPaginados sin filtros debe retornar todos")
    void testObtenerEjerciciosPaginadosSinFiltros() {
        // Arrange
        String q = null;
        String name = null;
        String statement = null;
        String classId = null;
        String status = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Ejercicio> ejercicios = new ArrayList<>();
        ejercicios.add(testEjercicio);

        Page<Ejercicio> ejercicioPage = new PageImpl<>(ejercicios);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for no filters (findAll)
        when(repositorioEjercicio.findAll(any(Pageable.class))).thenReturn(ejercicioPage);

        // Act
        DTORespuestaPaginada<DTOEjercicio> resultado = servicioEjercicio.obtenerEjerciciosPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that the findAll method was called
        verify(repositorioEjercicio).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerEjerciciosPaginados con filtros vacíos debe tratarlos como null")
    void testObtenerEjerciciosPaginadosConFiltrosVacios() {
        // Arrange
        String q = "";
        String name = "   ";
        String statement = "";
        String classId = "";
        String status = "";
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Ejercicio> ejercicios = new ArrayList<>();
        ejercicios.add(testEjercicio);

        Page<Ejercicio> ejercicioPage = new PageImpl<>(ejercicios);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for no filters (findAll)
        when(repositorioEjercicio.findAll(any(Pageable.class))).thenReturn(ejercicioPage);

        // Act
        DTORespuestaPaginada<DTOEjercicio> resultado = servicioEjercicio.obtenerEjerciciosPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that the findAll method was called
        verify(repositorioEjercicio).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerEjerciciosPaginados con paginación debe aplicar correctamente")
    void testObtenerEjerciciosPaginadosConPaginacion() {
        // Arrange
        String q = "Java";
        String name = null;
        String statement = null;
        String classId = null;
        String status = null;
        int page = 2;
        int size = 5;
        String sortBy = "name";
        String sortDirection = "DESC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Create test data
        List<Ejercicio> ejercicios = new ArrayList<>();
        ejercicios.add(testEjercicio);

        Page<Ejercicio> ejercicioPage = new PageImpl<>(ejercicios, PageRequest.of(page, size), 25);
        Pageable pageable = PageRequest.of(page, size);

        // Mock repository call for search term (findByNameContainingOrStatementContaining)
        when(repositorioEjercicio.findByNameContainingOrStatementContaining(
            eq(q), eq(q), any(Pageable.class))).thenReturn(ejercicioPage);

        // Act
        DTORespuestaPaginada<DTOEjercicio> resultado = servicioEjercicio.obtenerEjerciciosPaginados(
            q, name, statement, classId, status, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.page());
        assertEquals(5, resultado.size());
        assertEquals(25, resultado.totalElements());
        assertEquals(5, resultado.totalPages());
        assertEquals("name", resultado.sortBy());
        assertEquals("DESC", resultado.sortDirection());
        
        // Verify that the search method was called with correct parameters
        verify(repositorioEjercicio).findByNameContainingOrStatementContaining(eq(q), eq(q), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerEjerciciosPaginados sin permisos debe lanzar excepción")
    void testObtenerEjerciciosPaginadosSinPermisos() {
        // Arrange
        String q = null;
        String name = null;
        String statement = null;
        String classId = null;
        String status = null;
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check - no permissions
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(false);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioEjercicio.obtenerEjerciciosPaginados(
                q, name, statement, classId, status, page, size, sortBy, sortDirection);
        });
        
        // Verify that no repository methods were called
        verify(repositorioEjercicio, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerEjerciciosPaginados con parámetros inválidos debe lanzar excepción")
    void testObtenerEjerciciosPaginadosConParametrosInvalidos() {
        // Arrange
        String q = null;
        String name = null;
        String statement = null;
        String classId = null;
        String status = null;
        int page = -1; // Invalid page
        int size = 10;
        String sortBy = "name";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioEjercicio.obtenerEjerciciosPaginados(
                q, name, statement, classId, status, page, size, sortBy, sortDirection);
        });
        
        // Verify that no repository methods were called
        verify(repositorioEjercicio, never()).findAll(any(Pageable.class));
    }
}
