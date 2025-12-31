package app.util.datainit;

import app.dtos.DTOAlumno;
import app.dtos.DTOCurso;
import app.servicios.ServicioAlumno;
import app.repositorios.RepositorioClase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@Profile("!test")
public class StudentEnrollmentInitializer extends BaseDataInitializer {
    private static final double ENROLLMENT_PROBABILITY = 0.7; // 70% chance a student enrolls in a course
    private static final int MAX_COURSES_PER_STUDENT = 5; // Maximum courses a student can enroll in
    private final Random random = new Random();

    @Override
    public void initialize() {
        // Set up security context for enrollment (as an admin)
        setupSecurityContext();
        
        ServicioAlumno servicioAlumno = context.getBean(ServicioAlumno.class);
        RepositorioClase repositorioClase = context.getBean(RepositorioClase.class);
        StudentDataInitializer studentInit = context.getBean(StudentDataInitializer.class);
        CourseDataInitializer courseInit = context.getBean(CourseDataInitializer.class);
        
        List<DTOAlumno> students = studentInit.getCreatedStudents();
        List<DTOCurso> courses = courseInit.getCreatedCourses();
        
        if (students.isEmpty() || courses.isEmpty()) {
            System.out.println("No students or courses available for enrollment");
            return;
        }
        
        System.out.println("Students: " + students.size() + ", Courses: " + courses.size());
        
        // Verify courses exist in database
        List<Long> verifiedCourseIds = new ArrayList<>();
        for (DTOCurso course : courses) {
            var savedCourse = repositorioClase.findById(course.id());
            if (savedCourse.isPresent()) {
                verifiedCourseIds.add(course.id());
            } else {
                System.err.println("✗ Course " + course.titulo() + " (ID: " + course.id() + ") NOT found in database - skipping enrollment");
            }
        }
        
        if (verifiedCourseIds.isEmpty()) {
            System.err.println("No courses found in database for enrollment!");
            return;
        }
        
        int totalEnrollments = 0;
        
        // For each student, randomly decide which courses to enroll in
        for (DTOAlumno student : students) {
            // Shuffle courses to randomize selection
            List<DTOCurso> shuffledCourses = new ArrayList<>(courses);
            Collections.shuffle(shuffledCourses, random);
            
            int enrolledInThisStudent = 0;
            
            for (DTOCurso course : shuffledCourses) {
                // Only try to enroll in courses that we verified exist in the database
                if (!verifiedCourseIds.contains(course.id())) {
                    continue;
                }
                
                // Stop if student has reached max courses
                if (enrolledInThisStudent >= MAX_COURSES_PER_STUDENT) {
                    break;
                }
                
                // Randomly decide if student enrolls in this course
                if (random.nextDouble() < ENROLLMENT_PROBABILITY) {
                    try {
                        servicioAlumno.inscribirEnClase(student.id(), course.id());
                        totalEnrollments++;
                        enrolledInThisStudent++;
                    } catch (Exception e) {
                        if (!e.getMessage().contains("ya está inscrito")) {
                            System.err.println("✗ Error enrolling student " + student.firstName() + " in course " + course.titulo() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        System.out.println("Enrollments completed: " + totalEnrollments);
        
        // Print enrollment statistics
        printEnrollmentStatistics(students, courses, servicioAlumno);
    }
    
    private void setupSecurityContext() {
        // Create an admin authentication context for enrollment operations
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("admin-init", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    private void printEnrollmentStatistics(List<DTOAlumno> students, List<DTOCurso> courses, ServicioAlumno servicioAlumno) {
        // Count students per course
        int totalStudentsInCourses = 0;
        for (DTOCurso course : courses) {
            try {
                int studentCount = servicioAlumno.obtenerAlumnosPorClasePaginados(course.id(), 0, 1000, "id", "ASC").content().size();
                totalStudentsInCourses += studentCount;
            } catch (Exception e) {
                System.err.println("Error getting students for course " + course.titulo() + ": " + e.getMessage());
            }
        }
        
        // Count courses per student
        int studentsWithCourses = 0;
        int totalStudentCourses = 0;
        
        for (DTOAlumno student : students) {
            try {
                int courseCount = servicioAlumno.obtenerClasesPorAlumno(student.id()).size();
                if (courseCount > 0) {
                    studentsWithCourses++;
                    totalStudentCourses += courseCount;
                }
            } catch (Exception e) {
                System.err.println("Error getting courses for student " + student.firstName() + ": " + e.getMessage());
            }
        }
        
        System.out.println("Students enrolled: " + studentsWithCourses + "/" + students.size() + 
            " (avg " + String.format("%.1f", (double) totalStudentCourses / studentsWithCourses) + " courses each)");
    }
}
