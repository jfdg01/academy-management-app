package app.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entidad Administrador
 * Representa a un administrador en la plataforma
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {
    
    public Administrador() {
        super();
        this.setRole(Role.ADMIN);
    }
    
    public Administrador(String username, String password, String firstName, String lastName, 
                        String dni, String email, String phoneNumber) {
        super(username, password, firstName, lastName, dni, email, phoneNumber);
        this.setRole(Role.ADMIN);
    }
    
    /**
     * metodo para resetear password según UML
     * TODO: Implementar lógica de reseteo por email
     */
    public void resetearpassword() {
        // TODO: Implementar según especificaciones del proyecto
        throw new UnsupportedOperationException("metodo resetearpassword por implementar");
    }
}
