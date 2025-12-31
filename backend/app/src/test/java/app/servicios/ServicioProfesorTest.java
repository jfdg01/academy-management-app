package app.servicios;

import app.dtos.DTOProfesor;
import app.dtos.DTOPeticionRegistroProfesor;
import app.dtos.DTOParametrosBusquedaProfesor;
import app.entidades.Profesor;
import app.entidades.Curso;
import app.excepciones.ResourceNotFoundException;
import app.repositorios.RepositorioProfesor;
import app.repositorios.RepositorioClase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import app.entidades.Usuario;
import app.excepciones.ValidationException;
import app.repositorios.RepositorioUsuario;
import app.util.SecurityUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ServicioProfesor")
class ServicioProfesorTest {

    @Mock
    private RepositorioProfesor repositorioProfesor;

    @Mock
    private RepositorioClase repositorioClase;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ServicioCachePassword servicioCachePassword;

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ServicioProfesor servicioProfesor;

    private Profesor profesor1;
    private Profesor profesor2;
    private Profesor profesor3;
    private DTOProfesor dtoProfesor1;
    private DTOProfesor dtoProfesor2;
    private DTOProfesor dtoProfesor3;

    @BeforeEach
    void setUp() {
        // Mock security utils to allow all operations for testing
        lenient().when(securityUtils.hasRole(anyString())).thenReturn(true);
        lenient().when(securityUtils.getCurrentUserId()).thenReturn(1L);
        
        profesor1 = new Profesor("profesor1", "password1", "María", "García", "12345678Z", "maria@ejemplo.com", "123456789");
        profesor1.setId(1L);
        profesor1.setEnabled(true);

        profesor2 = new Profesor("profesor2", "password2", "Juan", "Pérez", "87654321Y", "juan@ejemplo.com", "987654321");
        profesor2.setId(2L);
        profesor2.setEnabled(true);

        profesor3 = new Profesor("profesor3", "password3", "Ana", "López", "11223344X", "ana@ejemplo.com", "555666777");
        profesor3.setId(3L);
        profesor3.setEnabled(false);

        // Create DTOs with fixed timestamp to avoid comparison issues
        dtoProfesor1 = new DTOProfesor(profesor1.getId(), profesor1.getUsername(), profesor1.getFirstName(),
                                      profesor1.getLastName(), profesor1.getDni(), profesor1.getEmail(),
                                      profesor1.getPhoneNumber(), profesor1.getRole(), profesor1.isEnabled(),
                                      List.of(), LocalDateTime.of(2025, 1, 1, 12, 0, 0));
        dtoProfesor2 = new DTOProfesor(profesor2.getId(), profesor2.getUsername(), profesor2.getFirstName(),
                                      profesor2.getLastName(), profesor2.getDni(), profesor2.getEmail(),
                                      profesor2.getPhoneNumber(), profesor2.getRole(), profesor2.isEnabled(),
                                      List.of(), LocalDateTime.of(2025, 1, 1, 12, 0, 0));
        dtoProfesor3 = new DTOProfesor(profesor3.getId(), profesor3.getUsername(), profesor3.getFirstName(),
                                      profesor3.getLastName(), profesor3.getDni(), profesor3.getEmail(),
                                      profesor3.getPhoneNumber(), profesor3.getRole(), profesor3.isEnabled(),
                                      List.of(), LocalDateTime.of(2025, 1, 1, 12, 0, 0));
    }

    @Test
    @DisplayName("obtenerProfesores debe retornar lista de DTOs")
    void testObtenerProfesores() {
        List<Profesor> profesores = Arrays.asList(profesor1, profesor2, profesor3);
        when(repositorioProfesor.findAllOrderedById()).thenReturn(profesores);

        List<DTOProfesor> resultado = servicioProfesor.obtenerProfesores();

        assertEquals(3, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultado1 = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultado1.id());
        assertEquals(dtoProfesor1.username(), resultado1.username());
        assertEquals(dtoProfesor1.firstName(), resultado1.firstName());
        assertEquals(dtoProfesor1.lastName(), resultado1.lastName());
        assertEquals(dtoProfesor1.dni(), resultado1.dni());
        assertEquals(dtoProfesor1.email(), resultado1.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultado1.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultado1.role());
        assertEquals(dtoProfesor1.enabled(), resultado1.enabled());
        assertEquals(dtoProfesor1.classIds(), resultado1.classIds());
        
        DTOProfesor resultado2 = resultado.get(1);
        assertEquals(dtoProfesor2.id(), resultado2.id());
        assertEquals(dtoProfesor2.username(), resultado2.username());
        assertEquals(dtoProfesor2.firstName(), resultado2.firstName());
        assertEquals(dtoProfesor2.lastName(), resultado2.lastName());
        assertEquals(dtoProfesor2.dni(), resultado2.dni());
        assertEquals(dtoProfesor2.email(), resultado2.email());
        assertEquals(dtoProfesor2.phoneNumber(), resultado2.phoneNumber());
        assertEquals(dtoProfesor2.role(), resultado2.role());
        assertEquals(dtoProfesor2.enabled(), resultado2.enabled());
        assertEquals(dtoProfesor2.classIds(), resultado2.classIds());
        
        DTOProfesor resultado3 = resultado.get(2);
        assertEquals(dtoProfesor3.id(), resultado3.id());
        assertEquals(dtoProfesor3.username(), resultado3.username());
        assertEquals(dtoProfesor3.firstName(), resultado3.firstName());
        assertEquals(dtoProfesor3.lastName(), resultado3.lastName());
        assertEquals(dtoProfesor3.dni(), resultado3.dni());
        assertEquals(dtoProfesor3.email(), resultado3.email());
        assertEquals(dtoProfesor3.phoneNumber(), resultado3.phoneNumber());
        assertEquals(dtoProfesor3.role(), resultado3.role());
        assertEquals(dtoProfesor3.enabled(), resultado3.enabled());
        assertEquals(dtoProfesor3.classIds(), resultado3.classIds());
        
        verify(repositorioProfesor).findAllOrderedById();
    }

    @Test
    @DisplayName("obtenerProfesorPorId debe retornar DTO cuando existe")
    void testObtenerProfesorPorIdExiste() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));

        DTOProfesor resultado = servicioProfesor.obtenerProfesorPorId(1L);

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(dtoProfesor1.id(), resultado.id());
        assertEquals(dtoProfesor1.username(), resultado.username());
        assertEquals(dtoProfesor1.firstName(), resultado.firstName());
        assertEquals(dtoProfesor1.lastName(), resultado.lastName());
        assertEquals(dtoProfesor1.dni(), resultado.dni());
        assertEquals(dtoProfesor1.email(), resultado.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultado.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultado.role());
        assertEquals(dtoProfesor1.enabled(), resultado.enabled());
        assertEquals(dtoProfesor1.classIds(), resultado.classIds());
        verify(repositorioProfesor).findById(1L);
    }

    @Test
    @DisplayName("obtenerProfesorPorId debe lanzar excepción cuando no existe")
    void testObtenerProfesorPorIdNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioProfesor.obtenerProfesorPorId(999L);
        });
        verify(repositorioProfesor).findById(999L);
    }

    @Test
    @DisplayName("obtenerProfesorPorEmail debe retornar DTO cuando existe")
    void testObtenerProfesorPorEmailExiste() {
        when(repositorioProfesor.findByEmail("maria@ejemplo.com")).thenReturn(Optional.of(profesor1));

        DTOProfesor resultado = servicioProfesor.obtenerProfesorPorEmail("maria@ejemplo.com");

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(dtoProfesor1.id(), resultado.id());
        assertEquals(dtoProfesor1.username(), resultado.username());
        assertEquals(dtoProfesor1.firstName(), resultado.firstName());
        assertEquals(dtoProfesor1.lastName(), resultado.lastName());
        assertEquals(dtoProfesor1.dni(), resultado.dni());
        assertEquals(dtoProfesor1.email(), resultado.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultado.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultado.role());
        assertEquals(dtoProfesor1.enabled(), resultado.enabled());
        assertEquals(dtoProfesor1.classIds(), resultado.classIds());
        verify(repositorioProfesor).findByEmail("maria@ejemplo.com");
    }

    @Test
    @DisplayName("obtenerProfesorPorEmail debe lanzar excepción cuando no existe")
    void testObtenerProfesorPorEmailNoExiste() {
        when(repositorioProfesor.findByEmail("inexistente@ejemplo.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioProfesor.obtenerProfesorPorEmail("inexistente@ejemplo.com");
        });
        verify(repositorioProfesor).findByEmail("inexistente@ejemplo.com");
    }

    @Test
    @DisplayName("obtenerProfesorPorUsuario debe retornar DTO cuando existe")
    void testObtenerProfesorPorUsuarioExiste() {
        when(repositorioProfesor.findByUsername("profesor1")).thenReturn(Optional.of(profesor1));

        DTOProfesor resultado = servicioProfesor.obtenerProfesorPorUsuario("profesor1");

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(dtoProfesor1.id(), resultado.id());
        assertEquals(dtoProfesor1.username(), resultado.username());
        assertEquals(dtoProfesor1.firstName(), resultado.firstName());
        assertEquals(dtoProfesor1.lastName(), resultado.lastName());
        assertEquals(dtoProfesor1.dni(), resultado.dni());
        assertEquals(dtoProfesor1.email(), resultado.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultado.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultado.role());
        assertEquals(dtoProfesor1.enabled(), resultado.enabled());
        assertEquals(dtoProfesor1.classIds(), resultado.classIds());
        verify(repositorioProfesor).findByUsername("profesor1");
    }

    @Test
    @DisplayName("obtenerProfesorPorUsuario debe lanzar excepción cuando no existe")
    void testObtenerProfesorPorUsuarioNoExiste() {
        when(repositorioProfesor.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioProfesor.obtenerProfesorPorUsuario("inexistente");
        });
        verify(repositorioProfesor).findByUsername("inexistente");
    }

    @Test
    @DisplayName("obtenerProfesorPorDni debe retornar DTO cuando existe")
    void testObtenerProfesorPorDniExiste() {
        when(repositorioProfesor.findByDni("12345678Z")).thenReturn(Optional.of(profesor1));

        DTOProfesor resultado = servicioProfesor.obtenerProfesorPorDni("12345678Z");

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(dtoProfesor1.id(), resultado.id());
        assertEquals(dtoProfesor1.username(), resultado.username());
        assertEquals(dtoProfesor1.firstName(), resultado.firstName());
        assertEquals(dtoProfesor1.lastName(), resultado.lastName());
        assertEquals(dtoProfesor1.dni(), resultado.dni());
        assertEquals(dtoProfesor1.email(), resultado.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultado.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultado.role());
        assertEquals(dtoProfesor1.enabled(), resultado.enabled());
        assertEquals(dtoProfesor1.classIds(), resultado.classIds());
        verify(repositorioProfesor).findByDni("12345678Z");
    }

    @Test
    @DisplayName("obtenerProfesorPorDni debe lanzar excepción cuando no existe")
    void testObtenerProfesorPorDniNoExiste() {
        when(repositorioProfesor.findByDni("99999999A")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioProfesor.obtenerProfesorPorDni("99999999A");
        });
        verify(repositorioProfesor).findByDni("99999999A");
    }

    @Test
    @DisplayName("buscarProfesoresPorNombre debe retornar profesores filtrados")
    void testBuscarProfesoresPorNombre() {
        List<Profesor> profesoresFiltrados = Arrays.asList(profesor1);
        when(repositorioProfesor.findByNombreContainingIgnoreCase("María")).thenReturn(profesoresFiltrados);

        List<DTOProfesor> resultado = servicioProfesor.buscarProfesoresPorNombre("María");

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor1.username(), resultadoProfesor.username());
        assertEquals(dtoProfesor1.firstName(), resultadoProfesor.firstName());
        assertEquals(dtoProfesor1.lastName(), resultadoProfesor.lastName());
        assertEquals(dtoProfesor1.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor1.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultadoProfesor.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultadoProfesor.role());
        assertEquals(dtoProfesor1.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor1.classIds(), resultadoProfesor.classIds());
        verify(repositorioProfesor).findByNombreContainingIgnoreCase("María");
    }

    @Test
    @DisplayName("buscarProfesoresPorApellidos debe retornar profesores filtrados")
    void testBuscarProfesoresPorApellidos() {
        List<Profesor> profesoresFiltrados = Arrays.asList(profesor1);
        when(repositorioProfesor.findByApellidosContainingIgnoreCase("García")).thenReturn(profesoresFiltrados);

        List<DTOProfesor> resultado = servicioProfesor.buscarProfesoresPorApellidos("García");

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor1.username(), resultadoProfesor.username());
        assertEquals(dtoProfesor1.firstName(), resultadoProfesor.firstName());
        assertEquals(dtoProfesor1.lastName(), resultadoProfesor.lastName());
        assertEquals(dtoProfesor1.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor1.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultadoProfesor.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultadoProfesor.role());
        assertEquals(dtoProfesor1.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor1.classIds(), resultadoProfesor.classIds());
        verify(repositorioProfesor).findByApellidosContainingIgnoreCase("García");
    }

    @Test
    @DisplayName("buscarProfesoresPorParametros debe retornar todos cuando no hay filtros")
    void testBuscarProfesoresPorParametrosSinFiltros() {
        DTOParametrosBusquedaProfesor parametros = new DTOParametrosBusquedaProfesor();
        List<Profesor> todosLosProfesores = Arrays.asList(profesor1, profesor2, profesor3);
        when(repositorioProfesor.findAllOrderedById()).thenReturn(todosLosProfesores);

        List<DTOProfesor> resultado = servicioProfesor.buscarProfesoresPorParametros(parametros);

        assertEquals(3, resultado.size());
        verify(repositorioProfesor).findAllOrderedById();
        verify(repositorioProfesor, never()).findByFiltros(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("buscarProfesoresPorParametros debe usar filtros cuando se proporcionan")
    void testBuscarProfesoresPorParametrosConFiltros() {
        DTOParametrosBusquedaProfesor parametros = new DTOParametrosBusquedaProfesor(
                "María", null, null, null, null, null, null, null);
        List<Profesor> todosLosProfesores = Arrays.asList(profesor1, profesor2, profesor3);
        when(repositorioProfesor.findAll()).thenReturn(todosLosProfesores);

        List<DTOProfesor> resultado = servicioProfesor.buscarProfesoresPorParametros(parametros);

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor1.username(), resultadoProfesor.username());
        assertEquals(dtoProfesor1.firstName(), resultadoProfesor.firstName());
        assertEquals(dtoProfesor1.lastName(), resultadoProfesor.lastName());
        assertEquals(dtoProfesor1.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor1.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultadoProfesor.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultadoProfesor.role());
        assertEquals(dtoProfesor1.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor1.classIds(), resultadoProfesor.classIds());
        verify(repositorioProfesor).findAll();
    }

    @Test
    @DisplayName("obtenerProfesoresHabilitados debe retornar profesores habilitados")
    void testObtenerProfesoresHabilitados() {
        List<Profesor> profesoresHabilitados = Arrays.asList(profesor1, profesor2);
        when(repositorioProfesor.findByEnabledTrue()).thenReturn(profesoresHabilitados);

        List<DTOProfesor> resultado = servicioProfesor.obtenerProfesoresHabilitados();

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(DTOProfesor::enabled));
        verify(repositorioProfesor).findByEnabledTrue();
    }

    @Test
    @DisplayName("obtenerProfesoresPorClase debe retornar profesores de la clase")
    void testObtenerProfesoresPorClase() {
        List<Profesor> profesoresDeClase = Arrays.asList(profesor1);
        when(repositorioProfesor.findByClaseId(1L)).thenReturn(profesoresDeClase);

        List<DTOProfesor> resultado = servicioProfesor.obtenerProfesoresPorClase("1");

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor1.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor1.username(), resultadoProfesor.username());
        assertEquals(dtoProfesor1.firstName(), resultadoProfesor.firstName());
        assertEquals(dtoProfesor1.lastName(), resultadoProfesor.lastName());
        assertEquals(dtoProfesor1.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor1.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor1.phoneNumber(), resultadoProfesor.phoneNumber());
        assertEquals(dtoProfesor1.role(), resultadoProfesor.role());
        assertEquals(dtoProfesor1.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor1.classIds(), resultadoProfesor.classIds());
        verify(repositorioProfesor).findByClaseId(1L);
    }

    @Test
    @DisplayName("obtenerProfesoresSinClases debe retornar profesores sin clases")
    void testObtenerProfesoresSinClases() {
        List<Profesor> profesoresSinClases = Arrays.asList(profesor2);
        when(repositorioProfesor.findProfesoresSinClases()).thenReturn(profesoresSinClases);

        List<DTOProfesor> resultado = servicioProfesor.obtenerProfesoresSinClases();

        assertEquals(1, resultado.size());
        // Compare individual fields instead of the whole DTO due to timestamp differences
        DTOProfesor resultadoProfesor = resultado.get(0);
        assertEquals(dtoProfesor2.id(), resultadoProfesor.id());
        assertEquals(dtoProfesor2.username(), resultadoProfesor.username());
        assertEquals(dtoProfesor2.firstName(), resultadoProfesor.firstName());
        assertEquals(dtoProfesor2.lastName(), resultadoProfesor.lastName());
        assertEquals(dtoProfesor2.dni(), resultadoProfesor.dni());
        assertEquals(dtoProfesor2.email(), resultadoProfesor.email());
        assertEquals(dtoProfesor2.phoneNumber(), resultadoProfesor.phoneNumber());
        assertEquals(dtoProfesor2.role(), resultadoProfesor.role());
        assertEquals(dtoProfesor2.enabled(), resultadoProfesor.enabled());
        assertEquals(dtoProfesor2.classIds(), resultadoProfesor.classIds());
        verify(repositorioProfesor).findProfesoresSinClases();
    }

    @Test
    @DisplayName("crearProfesor debe crear profesor correctamente")
    void testCrearProfesor() {
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "nuevo", "password123", "Nuevo", "Profesor", "98765432X", "nuevo@ejemplo.com", "555123456");
        
        when(servicioCachePassword.encodePassword("password123")).thenReturn("encodedPassword");
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor profesorGuardado = invocation.getArgument(0);
            profesorGuardado.setId(4L);
            return profesorGuardado;
        });

        DTOProfesor resultado = servicioProfesor.crearProfesor(peticion);

        // Compare individual fields instead of the whole DTO due to timestamp differences
        assertEquals(4L, resultado.id());
        assertEquals("nuevo", resultado.username());
        assertEquals("Nuevo", resultado.firstName());
        assertEquals("Profesor", resultado.lastName());
        assertEquals("98765432X", resultado.dni());
        assertEquals("nuevo@ejemplo.com", resultado.email());
        assertEquals("555123456", resultado.phoneNumber());
        assertEquals(Usuario.Role.PROFESOR, resultado.role());
        assertTrue(resultado.enabled());
        assertNotNull(resultado.classIds());
        assertTrue(resultado.classIds().isEmpty());
        
        verify(servicioCachePassword).encodePassword("password123");
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("crearProfesor debe lanzar excepción cuando el usuario ya existe")
    void testCrearProfesorUsuarioExiste() {
        // Arrange
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "existente", "password123", "Existente", "Profesor", "12345678Z", "existente@ejemplo.com", "555123456");
        when(repositorioProfesor.findByUsername("existente")).thenReturn(Optional.of(profesor1));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            servicioProfesor.crearProfesor(peticion);
        });

        assertEquals("Ya existe un profesor con el usuario: existente", exception.getMessage());
        verify(repositorioProfesor).findByUsername("existente");
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("crearProfesor debe lanzar excepción cuando el email ya existe")
    void testCrearProfesorEmailExiste() {
        // Arrange
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "existente", "password123", "Existente", "Profesor", "12345678Z", "existente@ejemplo.com", "555123456");
        when(repositorioProfesor.findByUsername("existente")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByEmail("existente@ejemplo.com")).thenReturn(Optional.of(profesor1));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            servicioProfesor.crearProfesor(peticion);
        });

        assertEquals("Ya existe un profesor con el email: existente@ejemplo.com", exception.getMessage());
        verify(repositorioProfesor).findByUsername("existente");
        verify(repositorioProfesor).findByEmail("existente@ejemplo.com");
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("crearProfesor debe lanzar excepción cuando el DNI ya existe")
    void testCrearProfesorDniExiste() {
        // Arrange
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "existente", "password123", "Existente", "Profesor", "12345678Z", "existente@ejemplo.com", "555123456");
        when(repositorioProfesor.findByUsername("existente")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByEmail("existente@ejemplo.com")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByDni("12345678Z")).thenReturn(Optional.of(profesor1));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            servicioProfesor.crearProfesor(peticion);
        });

        assertEquals("Ya existe un profesor con el DNI: 12345678Z", exception.getMessage());
        verify(repositorioProfesor).findByUsername("existente");
        verify(repositorioProfesor).findByEmail("existente@ejemplo.com");
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("crearProfesor con clases debe asignar clases correctamente")
    void testCrearProfesorConClases() {
        DTOPeticionRegistroProfesor peticion = new DTOPeticionRegistroProfesor(
                "nuevo", "password123", "Nuevo", "Profesor", "99999999A", "nuevo@ejemplo.com", "111222333",
                Arrays.asList(1L, 2L)
        );
        
        when(repositorioProfesor.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByEmail("nuevo@ejemplo.com")).thenReturn(Optional.empty());
        when(repositorioProfesor.findByDni("99999999A")).thenReturn(Optional.empty());
        when(servicioCachePassword.encodePassword("password123")).thenReturn("encodedPassword");
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor savedProfesor = invocation.getArgument(0);
            return savedProfesor;
        });

        DTOProfesor resultado = servicioProfesor.crearProfesor(peticion);

        assertNotNull(resultado);
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("borrarProfesorPorId debe borrar profesor correctamente")
    void testBorrarProfesorPorId() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));

        boolean resultado = servicioProfesor.borrarProfesorPorId(1L);

        assertTrue(resultado);
        verify(repositorioProfesor).findById(1L);
        verify(repositorioProfesor).deleteById(1L);
    }

    @Test
    @DisplayName("borrarProfesorPorId debe lanzar excepción si profesor no existe")
    void testBorrarProfesorPorIdNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioProfesor.borrarProfesorPorId(999L);
        });
        verify(repositorioProfesor).findById(999L);
        verify(repositorioProfesor, never()).deleteById(any());
    }

    @Test
    @DisplayName("asignarClase debe asignar clase correctamente")
    void testAsignarClase() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor savedProfesor = invocation.getArgument(0);
            return savedProfesor;
        });
        when(repositorioClase.findById(3L)).thenReturn(Optional.of(new Curso()));
        when(repositorioClase.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DTOProfesor resultado = servicioProfesor.asignarClase(1L, "3");

        assertNotNull(resultado);
        verify(repositorioProfesor).findById(1L);
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("asignarClase debe lanzar excepción si profesor no existe")
    void testAsignarClaseProfesorNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioProfesor.asignarClase(999L, "3");
        });
        verify(repositorioProfesor).findById(999L);
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("removerClase debe remover clase correctamente")
    void testRemoverClase() {
        // Create a professor with a class already assigned
        Profesor profesorConClase = new Profesor("profesor1", "password1", "María", "García", "12345678Z", "maria@ejemplo.com", "123456789");
        profesorConClase.setId(1L);
        profesorConClase.setEnabled(true);
        
        // Create a class and assign it to the professor
        Curso clase = new Curso();
        clase.setId(1L);
        profesorConClase.agregarClase(clase);
        clase.agregarProfesor(profesorConClase);
        
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesorConClase));
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor savedProfesor = invocation.getArgument(0);
            return savedProfesor;
        });
        when(repositorioClase.findById(1L)).thenReturn(Optional.of(clase));
        when(repositorioClase.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DTOProfesor resultado = servicioProfesor.removerClase(1L, "1");

        assertNotNull(resultado);
        verify(repositorioProfesor).findById(1L);
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("removerClase debe lanzar excepción si profesor no existe")
    void testRemoverClaseProfesorNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioProfesor.removerClase(999L, "1");
        });
        verify(repositorioProfesor).findById(999L);
        verify(repositorioProfesor, never()).save(any());
    }

    @Test
    @DisplayName("contarClasesProfesor debe retornar número correcto de clases")
    void testContarClasesProfesor() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));

        Integer resultado = servicioProfesor.contarClasesProfesor(1L);

        assertEquals(0, resultado); // Professor starts with 0 classes after JPA migration
        verify(repositorioProfesor).findById(1L);
    }

    @Test
    @DisplayName("cambiarEstadoProfesor debe cambiar estado correctamente")
    void testCambiarEstadoProfesor() {
        when(repositorioProfesor.findById(1L)).thenReturn(Optional.of(profesor1));
        when(repositorioProfesor.save(any(Profesor.class))).thenAnswer(invocation -> {
            Profesor savedProfesor = invocation.getArgument(0);
            return savedProfesor;
        });

        DTOProfesor resultado = servicioProfesor.cambiarEstadoProfesor(1L, false);

        assertNotNull(resultado);
        verify(repositorioProfesor).findById(1L);
        verify(repositorioProfesor).save(any(Profesor.class));
    }

    @Test
    @DisplayName("cambiarEstadoProfesor debe lanzar excepción si profesor no existe")
    void testCambiarEstadoProfesorNoExiste() {
        when(repositorioProfesor.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicioProfesor.cambiarEstadoProfesor(999L, false);
        });
        verify(repositorioProfesor).findById(999L);
        verify(repositorioProfesor, never()).save(any());
    }
}
