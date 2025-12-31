package app.servicios;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import app.dtos.DTOActualizarAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOCrearAlumno;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Alumno;
import app.excepciones.ResourceNotFoundException;
import app.excepciones.ValidationException;
import app.repositorios.RepositorioAlumno;
import app.util.SecurityUtils;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ServicioAlumno")
class ServicioAlumnoTest {

    @Mock
    private RepositorioAlumno repositorioAlumno;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ServicioCachePassword servicioCachePassword;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ServicioAlumno servicioAlumno;

    private Alumno alumno1;
    private Alumno alumno2;
    private Alumno alumno3;
    private DTOAlumno dtoAlumno1;
    private DTOAlumno dtoAlumno2;
    private DTOAlumno dtoAlumno3;

    @BeforeEach
    void setUp() {
        // Create test data
        alumno1 = new Alumno("alumno1", "password123", "Juan", "Pérez", "12345678Z", "juan@ejemplo.com", "123456789");
        alumno1.setId(1L);
        alumno1.setEnabled(true);
        alumno1.setEnrolled(true);
        alumno1.setEnrollDate(LocalDateTime.now());

        alumno2 = new Alumno("alumno2", "password456", "María", "García", "87654321Y", "maria@ejemplo.com", "987654321");
        alumno2.setId(2L);
        alumno2.setEnabled(true);
        alumno2.setEnrolled(false);
        alumno2.setEnrollDate(LocalDateTime.now());

        alumno3 = new Alumno("alumno3", "password789", "Carlos", "López", "11223344W", "carlos@ejemplo.com", "555666777");
        alumno3.setId(3L);
        alumno3.setEnabled(false);
        alumno3.setEnrolled(true);
        alumno3.setEnrollDate(LocalDateTime.now());

        dtoAlumno1 = new DTOAlumno(alumno1);
        dtoAlumno2 = new DTOAlumno(alumno2);
        dtoAlumno3 = new DTOAlumno(alumno3);

        // Setup SecurityUtils mock behavior with lenient stubbings
        lenient().when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        lenient().when(securityUtils.hasRole("PROFESOR")).thenReturn(false);
        lenient().when(securityUtils.hasRole("ALUMNO")).thenReturn(false);
        lenient().when(securityUtils.getCurrentUserId()).thenReturn(1L);
    }

    @Test
    @DisplayName("obtenerAlumnoPorId debe retornar DTO cuando existe")
    void testObtenerAlumnoPorIdExiste() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.obtenerAlumnoPorId(1L);

        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findById(1L);
    }

    @Test
    @DisplayName("obtenerAlumnoPorId debe lanzar excepción cuando no existe")
    void testObtenerAlumnoPorIdNoExiste() {
        when(repositorioAlumno.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioAlumno.obtenerAlumnoPorId(999L);
        });
        verify(repositorioAlumno).findById(999L);
    }

    @Test
    @DisplayName("obtenerAlumnoPorEmail debe retornar DTO cuando existe")
    void testObtenerAlumnoPorEmailExiste() {
        when(repositorioAlumno.findByEmail("juan@ejemplo.com")).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.obtenerAlumnoPorEmail("juan@ejemplo.com");

        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findByEmail("juan@ejemplo.com");
    }

    @Test
    @DisplayName("obtenerAlumnoPorEmail debe lanzar excepción cuando no existe")
    void testObtenerAlumnoPorEmailNoExiste() {
        when(repositorioAlumno.findByEmail("inexistente@ejemplo.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioAlumno.obtenerAlumnoPorEmail("inexistente@ejemplo.com");
        });
        verify(repositorioAlumno).findByEmail("inexistente@ejemplo.com");
    }

    @Test
    @DisplayName("obtenerAlumnoPorUsuario debe retornar DTO cuando existe")
    void testObtenerAlumnoPorUsuarioExiste() {
        when(repositorioAlumno.findByUsername("alumno1")).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.obtenerAlumnoPorUsuario("alumno1");

        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findByUsername("alumno1");
    }

    @Test
    @DisplayName("obtenerAlumnoPorDni debe retornar DTO cuando existe")
    void testObtenerAlumnoPorDniExiste() {
        when(repositorioAlumno.findByDni("12345678Z")).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.obtenerAlumnoPorDni("12345678Z");

        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findByDni("12345678Z");
    }

    @Test
    @DisplayName("crearAlumno debe crear alumno correctamente")
    void testCrearAlumno() {
        DTOCrearAlumno peticion = new DTOCrearAlumno(
            "nuevo", "password123", "Nuevo", "Alumno", "99999999A", "nuevo@ejemplo.com", "111222333"
        );
        
        when(repositorioAlumno.existsByUsername("nuevo")).thenReturn(false);
        when(repositorioAlumno.existsByEmail("nuevo@ejemplo.com")).thenReturn(false);
        when(repositorioAlumno.existsByDni("99999999A")).thenReturn(false);
        when(servicioCachePassword.encodePassword("password123")).thenReturn("encodedPassword");
        when(repositorioAlumno.save(any(Alumno.class))).thenReturn(alumno1);

        DTOAlumno resultado = servicioAlumno.crearAlumno(peticion);

        assertNotNull(resultado);
        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).existsByUsername("nuevo");
        verify(repositorioAlumno).existsByEmail("nuevo@ejemplo.com");
        verify(repositorioAlumno).existsByDni("99999999A");
        verify(servicioCachePassword).encodePassword("password123");
        verify(repositorioAlumno).save(any(Alumno.class));
    }

    @Test
    @DisplayName("crearAlumno debe lanzar excepción si usuario ya existe")
    void testCrearAlumnoUsuarioExiste() {
        DTOCrearAlumno peticion = new DTOCrearAlumno(
            "existente", "password123", "Nuevo", "Alumno", "99999999A", "nuevo@ejemplo.com", "111222333"
        );
        
        when(repositorioAlumno.existsByUsername("existente")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            servicioAlumno.crearAlumno(peticion);
        });
        verify(repositorioAlumno).existsByUsername("existente");
        verify(repositorioAlumno, never()).save(any());
    }

    @Test
    @DisplayName("crearAlumno debe lanzar excepción si email ya existe")
    void testCrearAlumnoEmailExiste() {
        DTOCrearAlumno peticion = new DTOCrearAlumno(
            "nuevo", "password123", "Nuevo", "Alumno", "99999999A", "existente@ejemplo.com", "111222333"
        );
        
        when(repositorioAlumno.existsByUsername("nuevo")).thenReturn(false);
        when(repositorioAlumno.existsByEmail("existente@ejemplo.com")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            servicioAlumno.crearAlumno(peticion);
        });
        verify(repositorioAlumno).existsByUsername("nuevo");
        verify(repositorioAlumno).existsByEmail("existente@ejemplo.com");
        verify(repositorioAlumno, never()).save(any());
    }

    @Test
    @DisplayName("actualizarAlumno debe actualizar campos no nulos")
    void testActualizarAlumno() {
        DTOActualizarAlumno dtoParcial = new DTOActualizarAlumno("Nuevo Nombre", null, null, null, null, null, null);
        
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));
        when(repositorioAlumno.save(any(Alumno.class))).thenAnswer(invocation -> {
            Alumno savedAlumno = invocation.getArgument(0);
            return savedAlumno;
        });

        DTOAlumno resultado = servicioAlumno.actualizarAlumno(1L, dtoParcial);

        assertNotNull(resultado);
        assertEquals("Nuevo Nombre", resultado.firstName());
        assertEquals(alumno1.getLastName(), resultado.lastName());
        verify(repositorioAlumno).findById(1L);
        verify(repositorioAlumno).save(any(Alumno.class));
    }

    @Test
    @DisplayName("actualizarAlumno debe lanzar excepción si alumno no existe")
    void testActualizarAlumnoNoExiste() {
        DTOActualizarAlumno dtoParcial = new DTOActualizarAlumno("Nuevo Nombre", null, null, null, null, null, null);
        
        when(repositorioAlumno.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioAlumno.actualizarAlumno(999L, dtoParcial);
        });
        verify(repositorioAlumno).findById(999L);
        verify(repositorioAlumno, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEstadoMatricula debe cambiar estado correctamente")
    void testCambiarEstadoMatricula() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));
        when(repositorioAlumno.save(any(Alumno.class))).thenAnswer(invocation -> {
            Alumno savedAlumno = invocation.getArgument(0);
            return savedAlumno;
        });

        DTOAlumno resultado = servicioAlumno.cambiarEstadoMatricula(1L, false);

        assertNotNull(resultado);
        assertFalse(resultado.enrolled());
        verify(repositorioAlumno).findById(1L);
        verify(repositorioAlumno).save(any(Alumno.class));
    }

    @Test
    @DisplayName("habilitarDeshabilitarAlumno debe cambiar estado correctamente")
    void testHabilitarDeshabilitarAlumno() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));
        when(repositorioAlumno.save(any(Alumno.class))).thenAnswer(invocation -> {
            Alumno savedAlumno = invocation.getArgument(0);
            return savedAlumno;
        });

        DTOAlumno resultado = servicioAlumno.habilitarDeshabilitarAlumno(1L, false);

        assertNotNull(resultado);
        assertFalse(resultado.enabled());
        verify(repositorioAlumno).findById(1L);
        verify(repositorioAlumno).save(any(Alumno.class));
    }

    @Test
    @DisplayName("borrarAlumnoPorId debe borrar alumno correctamente")
    void testBorrarAlumnoPorId() {
        when(repositorioAlumno.findById(1L)).thenReturn(Optional.of(alumno1));

        DTOAlumno resultado = servicioAlumno.borrarAlumnoPorId(1L);

        assertNotNull(resultado);
        assertEquals(dtoAlumno1, resultado);
        verify(repositorioAlumno).findById(1L);
        verify(repositorioAlumno).deleteById(1L);
    }

    @Test
    @DisplayName("contarAlumnosMatriculados debe retornar conteo correcto")
    void testContarAlumnosMatriculados() {
        when(repositorioAlumno.countByMatriculado(true)).thenReturn(2L);

        long resultado = servicioAlumno.contarAlumnosMatriculados();

        assertEquals(2L, resultado);
        verify(repositorioAlumno).countByMatriculado(true);
    }

    @Test
    @DisplayName("contarAlumnosNoMatriculados debe retornar conteo correcto")
    void testContarAlumnosNoMatriculados() {
        when(repositorioAlumno.countByMatriculado(false)).thenReturn(1L);

        long resultado = servicioAlumno.contarAlumnosNoMatriculados();

        assertEquals(1L, resultado);
        verify(repositorioAlumno).countByMatriculado(false);
    }

    @Test
    @DisplayName("contarTotalAlumnos debe retornar conteo correcto")
    void testContarTotalAlumnos() {
        when(repositorioAlumno.count()).thenReturn(3L);

        long resultado = servicioAlumno.contarTotalAlumnos();

        assertEquals(3L, resultado);
        verify(repositorioAlumno).count();
    }

    @Test
    @DisplayName("obtenerAlumnosPaginados debe retornar respuesta paginada")
    void testObtenerAlumnosPaginados() {
        List<Alumno> alumnos = Arrays.asList(alumno1, alumno2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        Page<Alumno> page = new PageImpl<>(alumnos, pageable, 3);
        when(repositorioAlumno.findAllPaged(any(Pageable.class))).thenReturn(page);

        DTORespuestaPaginada<DTOAlumno> resultado = servicioAlumno.obtenerAlumnosPaginados(0, 2, "id", "ASC");

        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        assertEquals(3, resultado.totalElements());
        assertEquals(0, resultado.page());
        assertEquals(2, resultado.size());
        verify(repositorioAlumno).findAllPaged(any(Pageable.class));
    }

    @Test
    @DisplayName("buscarAlumnosPorParametrosPaginados debe retornar respuesta paginada")
    void testBuscarAlumnosPorParametrosPaginados() {
        DTOParametrosBusquedaAlumno parametros = new DTOParametrosBusquedaAlumno("Juan", null, null, null, null);
        List<Alumno> alumnosFiltrados = Arrays.asList(alumno1);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("id").ascending());
        Page<Alumno> page = new PageImpl<>(alumnosFiltrados, pageable, 1);
        when(repositorioAlumno.findByFiltrosPaged(anyString(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        DTORespuestaPaginada<DTOAlumno> resultado = servicioAlumno.buscarAlumnosPorParametrosPaginados(parametros, 0, 1, "id", "ASC");

        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals(1, resultado.totalElements());
        verify(repositorioAlumno).findByFiltrosPaged(anyString(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerAlumnosPorMatriculadoPaginados debe retornar respuesta paginada")
    void testObtenerAlumnosPorMatriculadoPaginados() {
        List<Alumno> alumnosMatriculados = Arrays.asList(alumno1, alumno3);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
        Page<Alumno> page = new PageImpl<>(alumnosMatriculados, pageable, 2);
        when(repositorioAlumno.findByMatriculadoPaged(any(Boolean.class), any(Pageable.class))).thenReturn(page);

        DTORespuestaPaginada<DTOAlumno> resultado = servicioAlumno.obtenerAlumnosPorMatriculadoPaginados(true, 0, 2, "id", "ASC");

        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        assertEquals(2, resultado.totalElements());
        verify(repositorioAlumno).findByMatriculadoPaged(any(Boolean.class), any(Pageable.class));
    }
}
