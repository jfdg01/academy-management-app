package app.util.datainit;

import app.entidades.Usuario;
import app.repositorios.RepositorioUsuario;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AdminDataInitializer extends BaseDataInitializer {
    
    @Override
    public void initialize() {
        RepositorioUsuario repositorioUsuario = context.getBean(RepositorioUsuario.class);

        if (repositorioUsuario.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario(
                "admin",
                encodePassword("admin"),
                "Administrador",
                "Sistema",
                "12345678Z",
                "admin@academia.com",
                "600000000"
            );
            admin.setRole(Usuario.Role.ADMIN);
            
            try {
                repositorioUsuario.save(admin);
                // System.out.println("✓ Admin user created successfully with encoded password");
            } catch (Exception e) {
                System.err.println("✗ Error creating admin user: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ Admin user already exists");
        }
    }
}
