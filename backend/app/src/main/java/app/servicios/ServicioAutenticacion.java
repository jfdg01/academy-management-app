package app.servicios;

import app.dtos.DTOPeticionLogin;
import app.dtos.DTORespuestaLogin;
import app.dtos.DTOPeticionRegistro;
import app.entidades.Usuario;
import app.excepciones.ApiException;
import app.repositorios.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioAutenticacion {
    
    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder pe;
    private final ServicioJwt servicioJwt;
    private final AuthenticationManager authenticationManager;
    
    public DTORespuestaLogin login(DTOPeticionLogin request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            
            var usuario = repositorioUsuario.findByUsername(request.username())
                    .orElseThrow(() -> new ApiException("Usuario no encontrado", HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));
            
            var jwtToken = servicioJwt.generateToken(usuario);
            return DTORespuestaLogin.from(usuario, jwtToken);
        } catch (BadCredentialsException e) {
            throw new ApiException("Credenciales incorrectas", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }
    }
    
    public DTORespuestaLogin registro(DTOPeticionRegistro request) {
        if (repositorioUsuario.existsByUsername(request.username())) {
            throw new ApiException("El username ya existe", HttpStatus.CONFLICT, "USERNAME_EXISTS");
        }
        
        if (repositorioUsuario.existsByEmail(request.email())) {
            throw new ApiException("El email ya existe", HttpStatus.CONFLICT, "EMAIL_EXISTS");
        }
        
        var usuario = new Usuario(
                request.username(),
                pe.encode(request.password()),
                request.firstName(),
                request.lastName(),
                "00000000X", // DNI temporal
                request.email(),
                null // numeroTelefono temporal
        );
        
        repositorioUsuario.save(usuario);
        
        var jwtToken = servicioJwt.generateToken(usuario);
        return DTORespuestaLogin.from(usuario, jwtToken);
    }
} 