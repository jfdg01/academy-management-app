package app.servicios;

import app.dtos.DTOEntregaEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Alumno;
import app.entidades.Ejercicio;
import app.entidades.EntregaEjercicio;
import app.entidades.enums.EEstadoEjercicio;
import app.repositorios.RepositorioEntregaEjercicio;
import app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioEntregaEjercicioTest {

    @Mock
    private RepositorioEntregaEjercicio repositorioEntregaEjercicio;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ServicioEntregaEjercicio servicioEntregaEjercicio;

    private Alumno mockAlumno1;
    private Alumno mockAlumno2;
    private Ejercicio mockEjercicio1;
    private Ejercicio mockEjercicio2;

    @BeforeEach
    void setUp() {
        // Set up security context for testing
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PROFESOR"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("test-professor", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create mock entities
        mockAlumno1 = new Alumno();
        mockAlumno1.setId(12L);
        mockAlumno1.setFirstName("Alumno 1");

        mockAlumno2 = new Alumno();
        mockAlumno2.setId(15L);
        mockAlumno2.setFirstName("Alumno 2");

        mockEjercicio1 = new Ejercicio();
        mockEjercicio1.setId(35L);
        mockEjercicio1.setName("Ejercicio 1");

        mockEjercicio2 = new Ejercicio();
        mockEjercicio2.setId(40L);
        mockEjercicio2.setName("Ejercicio 2");
    }

    @Test
    void testObtenerEntregasPaginadas_ConAlumnoIdYEjercicioId_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = "35";
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(BigDecimal.valueOf(8.5));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumno(mockAlumno1);
        entrega2.setEjercicio(mockEjercicio1);
        entrega2.setEstado(EEstadoEjercicio.ENTREGADO);
        entrega2.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method that the service actually uses
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(12L), eq(35L), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        assertEquals(2, resultado.totalElements());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_SoloAlumnoId_DebeFiltrarSoloPorAlumno() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumno(mockAlumno1);
        entrega2.setEjercicio(mockEjercicio2);
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(12L), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_SoloEjercicioId_DebeFiltrarSoloPorEjercicio() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = "35";
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumno(mockAlumno2);
        entrega2.setEjercicio(mockEjercicio1);
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), eq(35L), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConFiltrosCombinadosCompletos_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = "35";
        String estado = "CALIFICADO";
        BigDecimal notaMin = BigDecimal.valueOf(7.0);
        BigDecimal notaMax = BigDecimal.valueOf(9.0);
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(BigDecimal.valueOf(8.5));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(12L), eq(35L), eq(EEstadoEjercicio.CALIFICADO), eq(notaMin), eq(notaMax), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConAlumnoYEstado_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        String estado = "ENTREGADO";
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entrega1.setEstado(EEstadoEjercicio.ENTREGADO);
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(12L), isNull(), eq(EEstadoEjercicio.ENTREGADO), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConEjercicioYEstado_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = "35";
        String estado = "CALIFICADO";
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(BigDecimal.valueOf(8.5));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumno(mockAlumno2);
        entrega2.setEjercicio(mockEjercicio1);
        entrega2.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega2.setNota(BigDecimal.valueOf(7.5));
        entrega2.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), eq(35L), eq(EEstadoEjercicio.CALIFICADO), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConRangoDeNotas_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = null;
        String estado = null;
        BigDecimal notaMin = BigDecimal.valueOf(6.0);
        BigDecimal notaMax = BigDecimal.valueOf(9.0);
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(BigDecimal.valueOf(7.5));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), eq(notaMin), eq(notaMax), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.content().size());
        assertEquals("7,50", resultado.content().get(0).getNotaFormateada());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_SinFiltros_DebeRetornarTodas() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = null;
        String estado = null;
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumno(mockAlumno2);
        entrega2.setEjercicio(mockEjercicio2);
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method with all null filters
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the flexible filtering method was called with all null filters
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    @Disabled("Unnecessary stubbing issue - needs investigation")
    void testObtenerEntregasPaginadas_ConEstadoInvalido_DebeLanzarExcepcion() {
        // Arrange
        String alumnoId = null;
        String ejercicioId = null;
        String estado = "ESTADO_INVALIDO";
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data - the service will still call the repository with the invalid state
        List<EntregaEjercicio> entregas = new ArrayList<>();
        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method - the service now validates the state parameter
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act & Assert - should throw validation error for invalid state
        assertThrows(RuntimeException.class, () -> {
            servicioEntregaEjercicio.obtenerEntregasPaginadas(
                alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);
        });
        
        // Verify that no repository methods were called due to validation error
        verify(repositorioEntregaEjercicio, never()).findByFiltrosFlexibles(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConPaginacion_DebeAplicarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        int page = 2;
        int size = 5;
        String sortBy = "fechaEntrega";
        String sortDirection = "DESC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega = new EntregaEjercicio();
        entrega.setId(1L);
        entrega.setAlumno(mockAlumno1);
        entrega.setEjercicio(mockEjercicio1);
        entregas.add(entrega);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas, PageRequest.of(page, size), 25);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(12L), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, null, null, null, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.page());
        assertEquals(5, resultado.size());
        assertEquals(25, resultado.totalElements());
        assertEquals(5, resultado.totalPages());
        assertEquals("fechaEntrega", resultado.sortBy());
        assertEquals("DESC", resultado.sortDirection());
        
        // Verify that the method was called with correct pagination
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_SinPermisos_DebeLanzarExcepcion() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        String estado = null;
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check - no permissions
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioEntregaEjercicio.obtenerEntregasPaginadas(
                alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);
        });
        
        // Verify that no repository methods were called
        verify(repositorioEntregaEjercicio, never()).findByFiltrosFlexibles(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConFiltrosVacios_DebeTratarlosComoNull() {
        // Arrange
        String alumnoId = "";
        String ejercicioId = "   ";
        String estado = "";
        BigDecimal notaMin = null;
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> todasLasEntregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        todasLasEntregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumno(mockAlumno2);
        entrega2.setEjercicio(mockEjercicio2);
        todasLasEntregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(todasLasEntregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method with all null filters (empty strings should be treated as null)
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the flexible filtering method was called with all null filters
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }

    @Test
    void testObtenerEntregasPaginadas_ConFiltrosCombinadosParciales_DebeFiltrarCorrectamente() {
        // Arrange
        String alumnoId = "12";
        String ejercicioId = null;
        String estado = null;
        BigDecimal notaMin = BigDecimal.valueOf(7.0);
        BigDecimal notaMax = null;
        int page = 0;
        int size = 20;
        String sortBy = "id";
        String sortDirection = "ASC";

        // Mock security check
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(true);

        // Create test data
        List<EntregaEjercicio> entregas = new ArrayList<>();
        EntregaEjercicio entrega1 = new EntregaEjercicio();
        entrega1.setId(1L);
        entrega1.setAlumno(mockAlumno1);
        entrega1.setEjercicio(mockEjercicio1);
        entrega1.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega1.setNota(BigDecimal.valueOf(8.5));
        entrega1.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega1);

        EntregaEjercicio entrega2 = new EntregaEjercicio();
        entrega2.setId(2L);
        entrega2.setAlumno(mockAlumno1);
        entrega2.setEjercicio(mockEjercicio2);
        entrega2.setEstado(EEstadoEjercicio.CALIFICADO);
        entrega2.setNota(BigDecimal.valueOf(7.5));
        entrega2.setFechaEntrega(LocalDateTime.now());
        entregas.add(entrega2);

        Page<EntregaEjercicio> entregaPage = new PageImpl<>(entregas);
        Pageable pageable = PageRequest.of(page, size);

        // Mock the flexible filtering method
        when(repositorioEntregaEjercicio.findByFiltrosFlexibles(
            eq(12L), isNull(), isNull(), eq(notaMin), isNull(), any(Pageable.class)))
            .thenReturn(entregaPage);

        // Act
        DTORespuestaPaginada<DTOEntregaEjercicio> resultado = servicioEntregaEjercicio.obtenerEntregasPaginadas(
            alumnoId, ejercicioId, estado, notaMin, notaMax, page, size, sortBy, sortDirection);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.content().size());
        
        // Verify that the correct repository method was called
        verify(repositorioEntregaEjercicio).findByFiltrosFlexibles(
            any(), any(), any(), any(), any(), any());
    }
}
