package app.entidades;

import app.validation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("USUARIO")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    @Column(unique = true)
    private String username;

    @NotNull
    @Size(min = 6)
    private String password;

    @NotNull
    @Size(max = 100)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String firstName;

    @NotNull
    @Size(max = 100)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "Los apellidos solo pueden contener letras y espacios")
    private String lastName;

    @NotNull
    @ValidDNI
    @Column(unique = true)
    private String dni;

    @NotNull
    @ValidEmail
    @Column(unique = true)
    private String email;

    @ValidPhone
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USUARIO;

    private boolean enabled = true;

    public enum Role {
        ADMIN, PROFESOR, ALUMNO, USUARIO
    }

    public Usuario() {
    }

    public Usuario(String username, String password, String firstName, String lastName, String dni, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
} 