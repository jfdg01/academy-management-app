package app.util.datainit;

import app.dtos.DTOProfesor;
import app.dtos.DTOPeticionRegistroProfesor;
import app.servicios.ServicioProfesor;
import app.repositorios.RepositorioProfesor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
public class ProfessorDataInitializer extends BaseDataInitializer {
    private static final int NUM_PROFESSORS = 9;
    private final List<DTOProfesor> createdProfessors = new ArrayList<>();

    @Override
    public void initialize() {
        // Set up security context for professor creation (as an admin)
        setupSecurityContext();
        
        ServicioProfesor servicioProfesor = context.getBean(ServicioProfesor.class);
        RepositorioProfesor repositorioProfesor = context.getBean(RepositorioProfesor.class);
        
        // Check if professors already exist to avoid duplicates
        if (repositorioProfesor.count() > 0) {
            System.out.println("ℹ Professors already exist, skipping professor creation");
            return;
        }
        
        // Password service is now injected automatically
        
        // System.out.println("Creating hardcoded professor and " + NUM_PROFESSORS + " additional professors...");
        
        // Create hardcoded professor first
        DTOPeticionRegistroProfesor hardcodedProfessor = new DTOPeticionRegistroProfesor(
            "profesor", // username
            "password", // password
            "Profesor",
            "Demo Apellido",
            generateUniqueDNI(NUM_PROFESSORS),
            "profesor@academia.com",
            "600000001",
            null // clasesId (null for now)
        );
        
        try {
            DTOProfesor profesor = servicioProfesor.crearProfesor(hardcodedProfessor);
            createdProfessors.add(profesor);
            // System.out.println("✓ Created hardcoded professor: profesor with password: password");
        } catch (Exception e) {
            System.err.println("✗ Error creating hardcoded professor: " + e.getMessage());
        }
        
        for (int i = 0; i < NUM_PROFESSORS; i++) {
            String[] nombreCompleto = generateRandomNames();
            String email = "prof" + (i + 1) + "@academia.com";
            String dni = generateUniqueDNI(i);
            String telefono = generateRandomPhone();
            
            // Create professor using the record constructor
            String username = "prof" + removeAccents(nombreCompleto[0]) + i;
            
            // Note: The service will handle password encoding internally,
            // but we can also encode it here for consistency
            String rawPassword = "password";
            String encodedPassword = encodePassword(rawPassword);
                
            DTOPeticionRegistroProfesor dto = new DTOPeticionRegistroProfesor(
                username, // username without accents
                rawPassword, // The service will encode this password
                nombreCompleto[0],
                nombreCompleto[1] + " " + nombreCompleto[2], // apellidos
                dni,
                email,
                telefono,
                null // clasesId (null for now)
            );
            
            try {
                DTOProfesor profesor = servicioProfesor.crearProfesor(dto);
                createdProfessors.add(profesor);
            } catch (Exception e) {
                System.err.println("✗ Error creating professor: " + e.getMessage());
            }
        }
        
        System.out.println("Professors created: " + createdProfessors.size());
    }
    
    private void setupSecurityContext() {
        // Create an admin authentication context for professor creation
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("admin-init", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    public List<DTOProfesor> getCreatedProfessors() {
        return createdProfessors;
    }
}
