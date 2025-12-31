package app.rest;

import app.dtos.DTOPeticionLogin;
import app.dtos.DTORespuestaLogin;
import app.dtos.DTOPeticionRegistro;
import app.servicios.ServicioAutenticacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para AutenticacionRest")
class AutenticacionRestTest {

    @Mock
    private ServicioAutenticacion servicioAutenticacion;

    @InjectMocks
    private AutenticacionRest autenticacionRest;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private DTORespuestaLogin respuestaLogin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(autenticacionRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        respuestaLogin = new DTORespuestaLogin(
                "token123",
                "Bearer",
                1L,
                "usuario1",
                "juan@ejemplo.com",
                "Juan",
                "Pérez",
                "ALUMNO"
        );
    }

    @Test
    @DisplayName("POST /api/auth/login debe autenticar usuario correctamente")
    void testLogin() throws Exception {
        DTOPeticionLogin peticionLogin = new DTOPeticionLogin("usuario1", "password123");
        when(servicioAutenticacion.login(any(DTOPeticionLogin.class))).thenReturn(respuestaLogin);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionLogin)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.username").value("usuario1"))
                .andExpect(jsonPath("$.role").value("ALUMNO"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("juan@ejemplo.com"));

        verify(servicioAutenticacion).login(any(DTOPeticionLogin.class));
    }

    @Test
    @DisplayName("POST /api/auth/login debe validar datos requeridos")
    void testLoginValidacion() throws Exception {
        DTOPeticionLogin peticionInvalida = new DTOPeticionLogin("", "");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionInvalida)))
                .andExpect(status().isBadRequest());

        verify(servicioAutenticacion, never()).login(any());
    }

    @Test
    @DisplayName("POST /api/auth/login con credenciales incorrectas debe retornar 401")
    void testLoginCredencialesIncorrectas() throws Exception {
        DTOPeticionLogin peticionLogin = new DTOPeticionLogin("usuario1", "wrongpassword");
        when(servicioAutenticacion.login(any(DTOPeticionLogin.class)))
                .thenThrow(new app.excepciones.ApiException("Credenciales inválidas", org.springframework.http.HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionLogin)))
                .andExpect(status().isUnauthorized());

        verify(servicioAutenticacion).login(any(DTOPeticionLogin.class));
    }

    @Test
    @DisplayName("POST /api/auth/registro debe registrar usuario correctamente")
    void testRegistro() throws Exception {
        DTOPeticionRegistro peticionRegistro = new DTOPeticionRegistro(
                "nuevoUsuario", "password123", "nuevo@ejemplo.com", "Nuevo", "Usuario"
        );
        when(servicioAutenticacion.registro(any(DTOPeticionRegistro.class))).thenReturn(respuestaLogin);

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionRegistro)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.username").value("usuario1"));

        verify(servicioAutenticacion).registro(any(DTOPeticionRegistro.class));
    }

    @Test
    @DisplayName("POST /api/auth/registro debe validar datos requeridos")
    void testRegistroValidacion() throws Exception {
        DTOPeticionRegistro peticionInvalida = new DTOPeticionRegistro("", "", "", "", "");

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionInvalida)))
                .andExpect(status().isBadRequest());

        verify(servicioAutenticacion, never()).registro(any());
    }

    @Test
    @DisplayName("POST /api/auth/registro con usuario existente debe retornar error")
    void testRegistroUsuarioExistente() throws Exception {
        DTOPeticionRegistro peticionRegistro = new DTOPeticionRegistro(
                "usuarioExistente", "password123", "existente@ejemplo.com", "Usuario", "Existente"
        );
        when(servicioAutenticacion.registro(any(DTOPeticionRegistro.class)))
                .thenThrow(new app.excepciones.ApiException("El username ya existe", org.springframework.http.HttpStatus.CONFLICT, "USERNAME_EXISTS"));

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionRegistro)))
                .andExpect(status().isConflict());

        verify(servicioAutenticacion).registro(any(DTOPeticionRegistro.class));
    }

    @Test
    @DisplayName("POST /api/auth/registro con email existente debe retornar error")
    void testRegistroEmailExistente() throws Exception {
        DTOPeticionRegistro peticionRegistro = new DTOPeticionRegistro(
                "nuevoUsuario", "password123", "existente@ejemplo.com", "Nuevo", "Usuario"
        );
        when(servicioAutenticacion.registro(any(DTOPeticionRegistro.class)))
                .thenThrow(new app.excepciones.ApiException("El email ya existe", org.springframework.http.HttpStatus.CONFLICT, "EMAIL_EXISTS"));

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionRegistro)))
                .andExpect(status().isConflict());

        verify(servicioAutenticacion).registro(any(DTOPeticionRegistro.class));
    }

    @Test
    @DisplayName("GET /api/auth/test debe retornar mensaje de prueba")
    void testTest() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Authentication working correctly"));

        verifyNoInteractions(servicioAutenticacion);
    }

    @Test
    @DisplayName("POST /api/auth/login con JSON malformado debe retornar 400")
    void testLoginJsonMalformado() throws Exception {
        String jsonMalformado = "{\"username\": \"test\", \"password\":}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMalformado))
                .andExpect(status().isBadRequest());

        verify(servicioAutenticacion, never()).login(any());
    }

    @Test
    @DisplayName("POST /api/auth/registro con JSON malformado debe retornar 400")
    void testRegistroJsonMalformado() throws Exception {
        String jsonMalformado = "{\"username\": \"test\", \"password\": \"pass\", \"nombre\":}";

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMalformado))
                .andExpect(status().isBadRequest());

        verify(servicioAutenticacion, never()).registro(any());
    }

    @Test
    @DisplayName("POST /api/auth/login sin Content-Type debe retornar 415")
    void testLoginSinContentType() throws Exception {
        DTOPeticionLogin peticionLogin = new DTOPeticionLogin("usuario1", "password123");

        mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(peticionLogin)))
                .andExpect(status().isUnsupportedMediaType());

        verify(servicioAutenticacion, never()).login(any());
    }

    @Test
    @DisplayName("POST /api/auth/registro sin Content-Type debe retornar 415")
    void testRegistroSinContentType() throws Exception {
        DTOPeticionRegistro peticionRegistro = new DTOPeticionRegistro(
                "nuevoUsuario", "password123", "nuevo@ejemplo.com", "Nuevo", "Usuario"
        );

        mockMvc.perform(post("/api/auth/registro")
                .content(objectMapper.writeValueAsString(peticionRegistro)))
                .andExpect(status().isUnsupportedMediaType());

        verify(servicioAutenticacion, never()).registro(any());
    }
}
