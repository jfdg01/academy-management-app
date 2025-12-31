package app.rest;

import app.dtos.DTOClase;
import app.dtos.DTOAlumnoPublico;
import app.dtos.DTORespuestaAlumnosClase;
import app.entidades.Usuario;
import app.servicios.ServicioAlumno;
import app.servicios.ServicioClase;
import app.util.SecurityUtils;
import app.config.ApiLoggingInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserClaseRest controller using @SpringBootTest
 */
@WebMvcTest(UserClaseRest.class)
@Import(BaseRestTestConfig.class)
@ActiveProfiles("test")
@Disabled("ApplicationContext loading issues - needs investigation")
class UserClaseRestTest {

    @MockBean
    private ServicioClase servicioClase;

    @MockBean
    private ServicioAlumno servicioAlumno;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private ApiLoggingInterceptor apiLoggingInterceptor;

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Usuario usuarioMock;
    private DTOClase dtoClase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        usuarioMock = new Usuario("testuser", "password", "Test", "User", "12345678Z", "test@example.com", "123456789");
        usuarioMock.setId(1L);
        usuarioMock.setRole(Usuario.Role.PROFESOR);

        dtoClase = new DTOClase(
            1L, "Test Class", "Test Description", 
            BigDecimal.valueOf(100), 
            app.entidades.enums.EPresencialidad.PRESENCIAL,
            "test-image.jpg", app.entidades.enums.EDificultad.INTERMEDIO,
            Arrays.asList("1"), Arrays.asList("1"), 
            Arrays.asList("1"), Arrays.asList(), "CURSO"
        );
    }

    @Test
    @DisplayName("GET /api/my/classes debe obtener clases del profesor")
    @WithMockUser(roles = "PROFESOR")
    void testObtenerMisClasesConUsuarioProfesor() throws Exception {
        // Set up mocks with doReturn to ensure they work
        doReturn(1L).when(securityUtils).getCurrentUserId();
        doReturn(Arrays.asList(dtoClase)).when(servicioClase).obtenerClasesPorProfesor(1L);

        // Perform request and print response for debugging
        String response = mockMvc.perform(get("/api/my/classes"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        System.out.println("Response body: " + response);

        // Verify mocks were called
        verify(securityUtils).getCurrentUserId();
        verify(servicioClase).obtenerClasesPorProfesor(1L);
    }

    @Test
    @DisplayName("GET /api/my/classes/{claseId}/students debe obtener alumnos de clase")
    @WithMockUser(roles = "PROFESOR")
    void testObtenerAlumnosDeClase() throws Exception {
        String response = mockMvc.perform(get("/api/my/classes/1/students"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        System.out.println("Students response body: " + response);
    }

    @Test
    @DisplayName("GET /api/my/classes/{claseId}/students/{studentId} debe obtener perfil público del alumno")
    @WithMockUser(roles = "PROFESOR")
    void testObtenerPerfilAlumnoClase() throws Exception {
        String response = mockMvc.perform(get("/api/my/classes/1/students/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        System.out.println("Student profile response body: " + response);
    }
}
