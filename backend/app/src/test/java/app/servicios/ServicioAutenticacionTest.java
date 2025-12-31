package app.servicios;

import app.dtos.DTOPeticionLogin;
import app.dtos.DTOPeticionRegistro;
import app.dtos.DTORespuestaLogin;
import app.entidades.Usuario;
import app.repositorios.RepositorioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import app.excepciones.ApiException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ServicioAutenticacion")
class ServicioAutenticacionTest {

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ServicioJwt servicioJwt;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private ServicioAutenticacion servicioAutenticacion;

    private Usuario usuario;
    private DTOPeticionLogin peticionLogin;
    private DTOPeticionRegistro peticionRegistro;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("testuser", "encoded_password", "Juan", "Pérez", "12345678Z", "juan@ejemplo.com", "123456789");
        usuario.setId(1L);
        usuario.setRole(Usuario.Role.ALUMNO);

        peticionLogin = new DTOPeticionLogin("testuser", "password123");
        peticionRegistro = new DTOPeticionRegistro("newuser", "password123", "nuevo@ejemplo.com", "Nuevo", "Usuario");
    }

    @Test
    @DisplayName("login debe autenticar usuario correctamente")
    void testLoginExitoso() {
        // Arrange
        String jwtToken = "jwt.token.example";
        when(repositorioUsuario.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(servicioJwt.generateToken(usuario)).thenReturn(jwtToken);

        // Act
        DTORespuestaLogin respuesta = servicioAutenticacion.login(peticionLogin);

        // Assert
        assertNotNull(respuesta);
        assertEquals(jwtToken, respuesta.token());
        assertEquals("testuser", respuesta.username());
        assertEquals("ALUMNO", respuesta.role());
        assertEquals(1L, respuesta.id());
        assertEquals("Juan", respuesta.firstName());
        assertEquals("Pérez", respuesta.lastName());
        assertEquals("juan@ejemplo.com", respuesta.email());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repositorioUsuario).findByUsername("testuser");
        verify(servicioJwt).generateToken(usuario);
    }

    @Test
    @DisplayName("login debe lanzar excepción cuando las credenciales son incorrectas")
    void testLoginCredencialesIncorrectas() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        assertThrows(ApiException.class, () -> {
            servicioAutenticacion.login(peticionLogin);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repositorioUsuario, never()).findByUsername(anyString());
        verify(servicioJwt, never()).generateToken(any());
    }

    @Test
    @DisplayName("login debe lanzar excepción cuando el usuario no existe")
    void testLoginUsuarioNoExiste() {
        // Arrange
        when(repositorioUsuario.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioAutenticacion.login(peticionLogin);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repositorioUsuario).findByUsername("testuser");
        verify(servicioJwt, never()).generateToken(any());
    }

    @Test
    @DisplayName("registro debe crear usuario correctamente")
    void testRegistroExitoso() {
        // Arrange
        String jwtToken = "jwt.token.example";
        Usuario usuarioGuardado = new Usuario("newuser", "encoded_password", "Nuevo", "Usuario", "00000000X", "nuevo@ejemplo.com", null);
        usuarioGuardado.setId(2L);

        when(repositorioUsuario.existsByUsername("newuser")).thenReturn(false);
        when(repositorioUsuario.existsByEmail("nuevo@ejemplo.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(repositorioUsuario.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(servicioJwt.generateToken(any(Usuario.class))).thenReturn(jwtToken);

        // Act
        DTORespuestaLogin respuesta = servicioAutenticacion.registro(peticionRegistro);

        // Assert
        assertNotNull(respuesta);
        assertEquals(jwtToken, respuesta.token());
        assertEquals("newuser", respuesta.username());

        verify(repositorioUsuario).existsByUsername("newuser");
        verify(repositorioUsuario).existsByEmail("nuevo@ejemplo.com");
        verify(passwordEncoder).encode("password123");
        verify(repositorioUsuario).save(any(Usuario.class));
        verify(servicioJwt).generateToken(any(Usuario.class));
    }

    @Test
    @DisplayName("registro debe lanzar excepción cuando el username ya existe")
    void testRegistroUsernameExiste() {
        // Arrange
        when(repositorioUsuario.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioAutenticacion.registro(peticionRegistro);
        });

        assertEquals("El username ya existe", exception.getMessage());

        verify(repositorioUsuario).existsByUsername("newuser");
        verify(repositorioUsuario, never()).existsByEmail(anyString());
        verify(repositorioUsuario, never()).save(any());
        verify(servicioJwt, never()).generateToken(any());
    }

    @Test
    @DisplayName("registro debe lanzar excepción cuando el email ya existe")
    void testRegistroEmailExiste() {
        // Arrange
        when(repositorioUsuario.existsByUsername("newuser")).thenReturn(false);
        when(repositorioUsuario.existsByEmail("nuevo@ejemplo.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioAutenticacion.registro(peticionRegistro);
        });

        assertEquals("El email ya existe", exception.getMessage());

        verify(repositorioUsuario).existsByUsername("newuser");
        verify(repositorioUsuario).existsByEmail("nuevo@ejemplo.com");
        verify(repositorioUsuario, never()).save(any());
        verify(servicioJwt, never()).generateToken(any());
    }

    @Test
    @DisplayName("registro debe crear usuario con datos por defecto")
    void testRegistroConDatosPorDefecto() {
        // Arrange
        String jwtToken = "jwt.token.example";
        when(repositorioUsuario.existsByUsername("newuser")).thenReturn(false);
        when(repositorioUsuario.existsByEmail("nuevo@ejemplo.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(servicioJwt.generateToken(any(Usuario.class))).thenReturn(jwtToken);

        // Act
        servicioAutenticacion.registro(peticionRegistro);

        // Assert - Verificar que se llama save con un usuario que tiene valores por defecto
        verify(repositorioUsuario).save(argThat(usuario -> 
            "newuser".equals(usuario.getUsername()) &&
            "encoded_password".equals(usuario.getPassword()) &&
            "Nuevo".equals(usuario.getFirstName()) &&
            "Usuario".equals(usuario.getLastName()) &&
            "00000000X".equals(usuario.getDni()) &&
            "nuevo@ejemplo.com".equals(usuario.getEmail()) &&
            usuario.getPhoneNumber() == null
        ));
    }

    @Test
    @DisplayName("login debe manejar token JWT correctamente")
    void testLoginGeneracionToken() {
        // Arrange
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        when(repositorioUsuario.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(servicioJwt.generateToken(usuario)).thenReturn(jwtToken);

        // Act
        DTORespuestaLogin respuesta = servicioAutenticacion.login(peticionLogin);

        // Assert
        assertEquals(jwtToken, respuesta.token());
        verify(servicioJwt).generateToken(usuario);
    }

    @Test
    @DisplayName("registro debe manejar errores de la base de datos")
    void testRegistroErrorBaseDatos() {
        // Arrange
        when(repositorioUsuario.existsByUsername("newuser")).thenReturn(false);
        when(repositorioUsuario.existsByEmail("nuevo@ejemplo.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(repositorioUsuario.save(any(Usuario.class))).thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioAutenticacion.registro(peticionRegistro);
        });

        assertEquals("Error de base de datos", exception.getMessage());
        verify(repositorioUsuario).save(any(Usuario.class));
    }

    @Test
    @DisplayName("login debe verificar autenticación antes de buscar usuario")
    void testLoginOrdenOperaciones() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        assertThrows(ApiException.class, () -> {
            servicioAutenticacion.login(peticionLogin);
        });

        // Verificar que no se busca el usuario si la autenticación falla
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repositorioUsuario, never()).findByUsername(anyString());
    }
}
