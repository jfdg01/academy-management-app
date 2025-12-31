package app.util.datainit;

import app.dtos.DTOAlumno;
import app.dtos.DTOCrearAlumno;
import app.servicios.ServicioAlumno;
import app.repositorios.RepositorioAlumno;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
public class StudentDataInitializer extends BaseDataInitializer {
    private static final int NUM_STUDENTS = 50;
    private final List<DTOAlumno> createdStudents = new ArrayList<>();

    @Override
    public void initialize() {
        // Set up security context for student creation (as an admin)
        setupSecurityContext();
        
        ServicioAlumno servicioAlumno = context.getBean(ServicioAlumno.class);
        RepositorioAlumno repositorioAlumno = context.getBean(RepositorioAlumno.class);

        // Check if students already exist to avoid duplicates
        if (repositorioAlumno.count() > 0) {
            System.out.println("ℹ Students already exist, skipping student creation");
            return;
        }

        // Password service is now injected automatically

        // Create hardcoded student first
        DTOCrearAlumno hardcodedStudent = new DTOCrearAlumno(
                "estudiante", // username
                "password", // password
                "Estudiante",
                "Demo Apellido",
                generateUniqueDNI(NUM_STUDENTS),
                "estudiante@academia.com",
                "600000000"
        );

        try {
            DTOAlumno alumno = servicioAlumno.crearAlumno(hardcodedStudent);
            createdStudents.add(alumno);
        } catch (Exception e) {
            System.err.println("✗ Error creating hardcoded student: " + e.getMessage());
        }

        for (int i = 0; i < NUM_STUDENTS - 1; i++) {
            String[] nombreCompleto = generateRandomNames();
            String email = "user" + (i + 1) + "@academia.com";
            String dni = generateUniqueDNI(i);
            String telefono = generateRandomPhone();

            // Create student using the record constructor
            String username = removeAccents(nombreCompleto[0]) + i;

            // Note: The service will handle password encoding internally,
            // but we can also encode it here for consistency
            String rawPassword = "password";

            DTOCrearAlumno dto = new DTOCrearAlumno(
                    username, // username without accents
                    rawPassword, // The service will encode this password
                    nombreCompleto[0],
                    nombreCompleto[1] + " " + nombreCompleto[2], // apellidos
                    dni,
                    email,
                    telefono
            );

            try {
                DTOAlumno alumno = servicioAlumno.crearAlumno(dto);
                createdStudents.add(alumno);
            } catch (Exception e) {
                System.err.println("✗ Error creating student: " + e.getMessage());
            }
        }

        System.out.println("Students created: " + createdStudents.size());
    }
    
    private void setupSecurityContext() {
        // Create an admin authentication context for student creation
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("admin-init", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public List<DTOAlumno> getCreatedStudents() {
        return createdStudents;
    }
}
