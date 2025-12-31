package app.util.datainit;

import app.dtos.DTOEntregaEjercicio;
import app.servicios.ServicioEntregaEjercicio;
import app.repositorios.RepositorioEntregaEjercicio;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("!test")
public class EntregaEjercicioDataInitializer extends BaseDataInitializer {
    private static final double DELIVERY_RATE = 0.4; // 50% of students will deliver exercises
    private static final double GRADING_RATE = 0.2; // 80% of deliveries will be graded
    private final List<DTOEntregaEjercicio> createdDeliveries = new ArrayList<>();
    private final Random random = new Random();
    
    // Sample comments for different grade ranges
    private static final String[] EXCELLENT_COMMENTS = {
        "Excelente trabajo. Has demostrado un dominio completo del tema.",
        "Muy bien hecho. Tu solución es elegante y eficiente.",
        "Trabajo sobresaliente. Has superado las expectativas.",
        "Perfecto. Tu implementación es impecable.",
        "Excelente comprensión del problema y solución muy bien estructurada."
    };
    
    private static final String[] GOOD_COMMENTS = {
        "Buen trabajo. La solución es correcta con algunos detalles menores.",
        "Bien hecho. Has comprendido los conceptos principales.",
        "Trabajo satisfactorio. Algunas mejoras menores serían beneficiosas.",
        "Correcto en general. Buena comprensión de los fundamentos.",
        "Aprobado. La solución funciona aunque podría optimizarse."
    };
    
    private static final String[] AVERAGE_COMMENTS = {
        "Trabajo aceptable. Necesitas revisar algunos conceptos.",
        "Aprobado con reservas. Hay aspectos que mejorar.",
        "Solución funcional pero con errores menores.",
        "Básicamente correcto. Más práctica ayudaría.",
        "Aprobado. Considera revisar la documentación."
    };
    
    private static final String[] POOR_COMMENTS = {
        "Necesitas revisar los conceptos fundamentales.",
        "La solución no cumple con los requisitos básicos.",
        "Hay errores importantes que deben corregirse.",
        "Se requiere más estudio del tema.",
        "La implementación tiene problemas significativos."
    };
    
    @Override
    public void initialize() {
        // Set up security context for delivery creation (as an admin)
        setupSecurityContext();
        
        ServicioEntregaEjercicio servicioEntregaEjercicio = context.getBean(ServicioEntregaEjercicio.class);
        RepositorioEntregaEjercicio repositorioEntregaEjercicio = context.getBean(RepositorioEntregaEjercicio.class);
        EjercicioDataInitializer ejercicioInit = context.getBean(EjercicioDataInitializer.class);
        
        // Check if exercise deliveries already exist to avoid duplicates
        if (repositorioEntregaEjercicio.count() > 0) {
            System.out.println("ℹ Exercise deliveries already exist, skipping delivery creation");
            return;
        }
        
        // Get all created exercises
        List<String> exerciseIds = ejercicioInit.getCreatedExercises().stream()
            .map(exercise -> exercise.id().toString())
            .toList();
        
        if (exerciseIds.isEmpty()) {
            System.err.println("Warning: No exercises available for delivery assignment");
            return;
        }
        
        // Get all students from the student initializer
        StudentDataInitializer studentInit = context.getBean(StudentDataInitializer.class);
        List<String> studentIds = studentInit.getCreatedStudents().stream()
            .map(student -> student.id().toString())
            .toList();
        
        if (studentIds.isEmpty()) {
            System.err.println("Warning: No students available for delivery assignment");
            return;
        }
        

        
        int totalDeliveries = 0;
        int totalGraded = 0;
        
        // Create 5 deliveries per exercise from students in that class
        for (String exerciseId : exerciseIds) {
            // Get students enrolled in this exercise's course
            List<String> studentsInExerciseClass = getStudentsInExerciseClass(exerciseId);
            
            if (studentsInExerciseClass.isEmpty()) {
                System.err.println("Warning: No students found for exercise " + exerciseId);
                continue;
            }
            
            // Create 5 deliveries for this exercise
            int deliveriesForExercise = Math.min(5, studentsInExerciseClass.size());
            List<String> availableStudents = new ArrayList<>(studentsInExerciseClass);
            
            for (int i = 0; i < deliveriesForExercise; i++) {
                if (availableStudents.isEmpty()) break;
                
                // Pick a random student from this class
                int studentIndex = random.nextInt(availableStudents.size());
                String studentId = availableStudents.remove(studentIndex);
                
                try {
                    // Generate random files for the delivery
                    List<String> files = generateRandomFiles();
                    
                    DTOEntregaEjercicio dtoEntrega = servicioEntregaEjercicio.crearEntrega(
                        Long.parseLong(studentId),
                        Long.parseLong(exerciseId),
                        files
                    );
                    
                    if (dtoEntrega != null) {
                        createdDeliveries.add(dtoEntrega);
                        totalDeliveries++;
                        
                        // 80% chance of being graded
                        if (random.nextDouble() < GRADING_RATE) {
                            // Grade the delivery with a random grade
                            BigDecimal grade = generateRandomGrade();
                            String comments = generateRandomComments(grade);
                            
                            try {
                                servicioEntregaEjercicio.calificarEntrega(
                                    dtoEntrega.id(),
                                    grade,
                                    comments
                                );
                                totalGraded++;
                            } catch (Exception e) {
                                System.err.println("Error grading delivery: " + e.getMessage());
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error creating delivery: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Exercise deliveries created: " + totalDeliveries + 
            " (graded: " + totalGraded + ") - 5 deliveries per exercise from students in that class");
    }
    
    private void setupSecurityContext() {
        // Create an admin authentication context for delivery creation
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("admin-init", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    private List<String> generateRandomFiles() {
        String[] fileTypes = {
            "documento.pdf", "presentacion.pptx", "codigo.java", "codigo.py", 
            "codigo.cpp", "codigo.js", "archivo.docx", "imagen.png", "datos.csv",
            "memoria.pdf", "diagrama.drawio", "script.sql", "readme.md"
        };
        
        String[] prefixes = {
            "tarea_", "ejercicio_", "proyecto_", "practica_", "examen_",
            "presentacion_", "ensayo_", "investigacion_", "memoria_", "codigo_"
        };
        
        List<String> files = new ArrayList<>();
        int numFiles = random.nextInt(3) + 1; // 1 to 3 files
        
        for (int i = 0; i < numFiles; i++) {
            String prefix = prefixes[random.nextInt(prefixes.length)];
            String fileType = fileTypes[random.nextInt(fileTypes.length)];
            String fileName = prefix + "alumno_" + (random.nextInt(1000) + 1) + "_" + fileType;
            files.add(fileName);
        }
        
        return files;
    }
    
    private BigDecimal generateRandomGrade() {
        // Generate grades with a normal-like distribution (more grades in the middle range)
        double grade;
        double rand = random.nextDouble();
        
        if (rand < 0.1) {
            // 10% chance of low grades (0-5)
            grade = random.nextDouble() * 5.0;
        } else if (rand < 0.3) {
            // 20% chance of medium-low grades (5-7)
            grade = 5.0 + random.nextDouble() * 2.0;
        } else if (rand < 0.7) {
            // 40% chance of medium-high grades (7-9)
            grade = 7.0 + random.nextDouble() * 2.0;
        } else {
            // 30% chance of high grades (9-10)
            grade = 9.0 + random.nextDouble() * 1.0;
        }
        
        // Round to 2 decimal places
        return BigDecimal.valueOf(Math.round(grade * 100.0) / 100.0);
    }
    
    private String generateRandomComments(BigDecimal grade) {
        if (grade.compareTo(BigDecimal.valueOf(9.0)) >= 0) {
            return EXCELLENT_COMMENTS[random.nextInt(EXCELLENT_COMMENTS.length)];
        } else if (grade.compareTo(BigDecimal.valueOf(7.0)) >= 0) {
            return GOOD_COMMENTS[random.nextInt(GOOD_COMMENTS.length)];
        } else if (grade.compareTo(BigDecimal.valueOf(5.0)) >= 0) {
            return AVERAGE_COMMENTS[random.nextInt(AVERAGE_COMMENTS.length)];
        } else {
            return POOR_COMMENTS[random.nextInt(POOR_COMMENTS.length)];
        }
    }
    
    public List<DTOEntregaEjercicio> getCreatedDeliveries() {
        return createdDeliveries;
    }
    
    /**
     * Gets students enrolled in the course that contains the given exercise
     */
    private List<String> getStudentsInExerciseClass(String exerciseId) {
        try {
            // Get the exercise to find its course
            app.servicios.ServicioEjercicio servicioEjercicio = context.getBean(app.servicios.ServicioEjercicio.class);
            app.dtos.DTOEjercicio ejercicio = servicioEjercicio.obtenerEjercicioPorId(Long.parseLong(exerciseId));
            
            if (ejercicio == null || ejercicio.classId() == null) {
                return new ArrayList<>();
            }
            
            // Get students enrolled in this course
            app.repositorios.RepositorioAlumno repositorioAlumno = context.getBean(app.repositorios.RepositorioAlumno.class);
            List<app.entidades.Alumno> studentsInClass = repositorioAlumno.findByClaseId(Long.parseLong(ejercicio.classId()));
            
            return studentsInClass.stream()
                .map(student -> student.getId().toString())
                .toList();
                
        } catch (Exception e) {
            System.err.println("Error getting students for exercise " + exerciseId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
