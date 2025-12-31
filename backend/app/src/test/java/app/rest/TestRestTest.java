package app.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para TestRest")
class TestRestTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TestRest testRest;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testRest)
                .setControllerAdvice(new app.excepciones.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/test/public debe retornar mensaje público")
    void testPublico() throws Exception {
        mockMvc.perform(get("/api/test/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("Este endpoint es público - no requiere autenticación"));
    }

    @Test
    @DisplayName("GET /api/test/protected debe retornar información del usuario autenticado")
    void testUsuario() throws Exception {
        // Configurar mock de autenticación
        when(authentication.getName()).thenReturn("usuario_test");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isOk())
                .andExpect(content().string("Endpoint protegido - Usuario autenticado: usuario_test"));

        // Limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/test/admin debe retornar acceso exitoso para administrador")
    void testAdminConRolAdmin() throws Exception {
        // Configurar mock de autenticación con rol ADMIN
        Collection<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
        );
        
        when(authentication.getName()).thenReturn("admin_test");
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Endpoint de admin - Usuario: admin_test"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/test/admin debe retornar acceso denegado para usuario sin rol admin")
    void testAdminSinRolAdmin() throws Exception {
        // Configurar mock de autenticación sin rol ADMIN
        Collection<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ALUMNO")
        );
        
        when(authentication.getName()).thenReturn("usuario_test");
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acceso denegado - Se requiere rol ADMIN"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/test/user-info debe retornar información detallada del usuario")
    void testObtenerInfoUsuario() throws Exception {
        // Configurar mock de autenticación con múltiples roles
        Collection<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_PROFESOR")
        );
        
        when(authentication.getName()).thenReturn("profesor_test");
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/test/user-info"))
                .andExpect(status().isOk())
                .andExpect(content().string("Información del usuario:\nUsername: profesor_test\nRoles: [ROLE_USER, ROLE_PROFESOR]"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/test/user-info debe manejar usuario sin roles")
    void testObtenerInfoUsuarioSinRoles() throws Exception {
        // Configurar mock de autenticación sin roles
        Collection<SimpleGrantedAuthority> authorities = Arrays.asList();
        
        when(authentication.getName()).thenReturn("usuario_sin_roles");
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/test/user-info"))
                .andExpect(status().isOk())
                .andExpect(content().string("Información del usuario:\nUsername: usuario_sin_roles\nRoles: []"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/test/protected debe manejar usuario con nombre nulo")
    void testUsuarioConNombreNulo() throws Exception {
        // Configurar mock de autenticación con nombre nulo
        when(authentication.getName()).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isOk())
                .andExpect(content().string("Endpoint protegido - Usuario autenticado: null"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/test/admin debe manejar verificación de rol con usuario anónimo")
    void testAdminConUsuarioAnonimo() throws Exception {
        // Configurar mock de autenticación con nombre anonymous
        Collection<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ANONYMOUS")
        );
        
        when(authentication.getName()).thenReturn("anonymousUser");
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acceso denegado - Se requiere rol ADMIN"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/test/admin debe verificar exactamente el rol ROLE_ADMIN")
    void testAdminVerificaRolExacto() throws Exception {
        // Configurar mock de autenticación con rol similar pero no exacto
        Collection<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"), // Similar pero no exacto
                new SimpleGrantedAuthority("ADMIN"), // Sin prefijo ROLE_
                new SimpleGrantedAuthority("ROLE_SUPER_ADMIN") // Contiene ADMIN pero no es exacto
        );
        
        when(authentication.getName()).thenReturn("usuario_test");
        doReturn(authorities).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Acceso denegado - Se requiere rol ADMIN"));

        SecurityContextHolder.clearContext();
    }
}
