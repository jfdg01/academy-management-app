package app.servicios;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOActualizarAlumno;
import app.dtos.DTOAlumno;
import app.dtos.DTOAlumnoPublico;
import app.dtos.DTOClase;
import app.dtos.DTOParametrosBusquedaAlumno;
import app.dtos.DTOPerfilAlumno;
import app.dtos.DTOCrearAlumno;
import app.dtos.DTORespuestaAlumnosClase;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Alumno;
import app.entidades.Clase;
import app.repositorios.RepositorioAlumno;
import app.repositorios.RepositorioClase;
import app.util.ExceptionUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioAlumno {

    private final RepositorioAlumno repositorioAlumno;
    private final RepositorioClase repositorioClase;
    private final PasswordEncoder passwordEncoder;
    private final ServicioCachePassword servicioCachePassword;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoPorId(Long id) {
        // Use basic findById to avoid MultipleBagFetchException
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can access student data
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's data
            return new DTOAlumno(alumno);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!id.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros alumnos");
            }
            return new DTOAlumno(alumno);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de alumnos");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoPorEmail(String email) {
        Alumno alumno = repositorioAlumno.findByEmail(email).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "email", email);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can access student data
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's data
            return new DTOAlumno(alumno);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumno.getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros alumnos");
            }
            return new DTOAlumno(alumno);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de alumnos");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoPorUsuario(String usuario) {
        Alumno alumno = repositorioAlumno.findByUsername(usuario).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "usuario", usuario);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can access student data
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's data
            return new DTOAlumno(alumno);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumno.getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros alumnos");
            }
            return new DTOAlumno(alumno);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de alumnos");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene el perfil del alumno (sin información sensible)
     * @param usuario Usuario del alumno
     * @return DTOPerfilAlumno
     */
    @Transactional(readOnly = true)
    public DTOPerfilAlumno obtenerPerfilAlumnoPorUsuario(String usuario) {
        Alumno alumno = repositorioAlumno.findByUsername(usuario).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "usuario", usuario);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can access student data
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's data
            return new DTOPerfilAlumno(alumno);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumno.getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros alumnos");
            }
            return new DTOPerfilAlumno(alumno);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de alumnos");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoPorDni(String dni) {
        Alumno alumno = repositorioAlumno.findByDni(dni).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "DNI", dni);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can access student data
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's data
            return new DTOAlumno(alumno);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumno.getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros alumnos");
            }
            return new DTOAlumno(alumno);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de alumnos");
            return null; // This line will never be reached due to the exception above
        }
    }

    public DTOAlumno crearAlumno(DTOCrearAlumno peticion) {
        // Security check: Only ADMIN can create students
        if (!securityUtils.hasRole("ADMIN")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear alumnos");
        }
        
        // Validar que no existan duplicados
        if (repositorioAlumno.existsByUsername(peticion.username())) {
            ExceptionUtils.throwValidationError("Ya existe un alumno con el usuario: " + peticion.username());
        }
        
        if (repositorioAlumno.existsByEmail(peticion.email())) {
            ExceptionUtils.throwValidationError("Ya existe un alumno con el email: " + peticion.email());
        }
        
        if (repositorioAlumno.existsByDni(peticion.dni())) {
            ExceptionUtils.throwValidationError("Ya existe un alumno con el DNI: " + peticion.dni());
        }

        // Crear el alumno
        Alumno alumno = new Alumno(
            peticion.username(),
            servicioCachePassword.encodePassword(peticion.password()),
            peticion.firstName(),
            peticion.lastName(),
            peticion.dni(),
            peticion.email(),
            peticion.phoneNumber()
        );

        Alumno alumnoGuardado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoGuardado);
    }

    public DTOAlumno actualizarAlumno(Long id, DTOActualizarAlumno dtoParcial) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);

        // Security check: Only ADMIN, PROFESOR, or the student themselves can update student data
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can update any student's data
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only update their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!id.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para modificar los datos de otros alumnos");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar datos de alumnos");
        }

        // Update non-null fields
        if (dtoParcial.firstName() != null) {
            alumno.setFirstName(dtoParcial.firstName());
        }
        
        if (dtoParcial.lastName() != null) {
            alumno.setLastName(dtoParcial.lastName());
        }
        
        if (dtoParcial.email() != null) {
            // Check that no other student exists with that email
            if (!alumno.getEmail().equals(dtoParcial.email()) && 
                repositorioAlumno.existsByEmail(dtoParcial.email())) {
                ExceptionUtils.throwValidationError("A student with this email already exists: " + dtoParcial.email());
            }
            alumno.setEmail(dtoParcial.email());
        }
        
        if (dtoParcial.dni() != null) {
            // Check that no other student exists with that DNI
            if (!alumno.getDni().equals(dtoParcial.dni()) && 
                repositorioAlumno.existsByDni(dtoParcial.dni())) {
                ExceptionUtils.throwValidationError("A student with this DNI already exists: " + dtoParcial.dni());
            }
            alumno.setDni(dtoParcial.dni());
        }
        
        if (dtoParcial.phoneNumber() != null) {
            alumno.setPhoneNumber(dtoParcial.phoneNumber());
        }
        
        // Handle enrollment status update
        if (dtoParcial.enrolled() != null) {
            alumno.setEnrolled(dtoParcial.enrolled());
        }
        
        // Handle enabled status update
        if (dtoParcial.enabled() != null) {
            alumno.setEnabled(dtoParcial.enabled());
        }

        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoActualizado);
    }

    public DTOAlumno cambiarEstadoMatricula(Long id, boolean matriculado) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can update enrollment status
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can update any student's enrollment status
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only update their own enrollment status
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!id.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para modificar el estado de matriculación de otros alumnos");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar el estado de matriculación de alumnos");
        }
        
        alumno.setEnrolled(matriculado);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoActualizado);
    }

    public DTOAlumno habilitarDeshabilitarAlumno(Long id, boolean habilitar) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can update enabled status
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can update any student's enabled status
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only update their own enabled status
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!id.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para modificar el estado de habilitación de otros alumnos");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar el estado de habilitación de alumnos");
        }
        
        alumno.setEnabled(habilitar);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        return new DTOAlumno(alumnoActualizado);
    }

    public DTOAlumno borrarAlumnoPorId(Long id) {
        Alumno alumno = repositorioAlumno.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
        
        // Security check: Only ADMIN can delete students
        if (!securityUtils.hasRole("ADMIN")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar alumnos");
        }
        
        repositorioAlumno.deleteById(id);
        return new DTOAlumno(alumno);
    }

    // metodos de estadísticas útiles para administradores
    public long contarAlumnosMatriculados() {
        return repositorioAlumno.countByMatriculado(true);
    }

    public long contarAlumnosNoMatriculados() {
        return repositorioAlumno.countByMatriculado(false);
    }

    public long contarTotalAlumnos() {
        return repositorioAlumno.count();
    }

    // métodos para gestionar relaciones con clases
    
    /**
     * Inscribir un alumno en una clase
     * @param alumnoId ID del alumno
     * @param claseId ID de la clase
     * @return DTOAlumno actualizado
     */
    public DTOAlumno inscribirEnClase(Long alumnoId, Long claseId) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can enroll in classes
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can enroll any student
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only enroll themselves
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No puedes inscribir a otros alumnos en clases");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para inscribir alumnos en clases");
        }
        
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
                
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar que el alumno no esté ya inscrito en la clase
        if (alumno.getClasses().stream().anyMatch(c -> c.getId().equals(claseId))) {
            throw new IllegalArgumentException("El alumno ya está inscrito en esta clase.");
        }
        
        // Add the class to the student using JPA relationship
        alumno.agregarClase(clase);
        
        // Add the student to the class using JPA relationship
        clase.agregarAlumno(alumno);
        
        // Guardar cambios
        repositorioClase.save(clase);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        
        return new DTOAlumno(alumnoActualizado);
    }
    
    /**
     * Dar de baja a un alumno de una clase
     * @param alumnoId ID del alumno
     * @param claseId ID de la clase
     * @return DTOAlumno actualizado
     */
    public DTOAlumno darDeBajaDeClase(Long alumnoId, Long claseId) {
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
                
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar que el alumno esté inscrito en la clase
        if (!alumno.getClasses().stream().anyMatch(c -> c.getId().equals(claseId))) {
            throw new IllegalArgumentException("El alumno no está inscrito en esta clase.");
        }
        
        // Remove the class from the student using JPA relationship
        alumno.removerClase(clase);
        
        // Remove the student from the class using JPA relationship
        clase.removerAlumno(alumno);
        
        // Guardar cambios
        repositorioClase.save(clase);
        Alumno alumnoActualizado = repositorioAlumno.save(alumno);
        
        return new DTOAlumno(alumnoActualizado);
    }
    
    /**
     * Obtener todas las clases en las que está inscrito un alumno
     * @param alumnoId ID del alumno
     * @return Lista de clases
     */
    public List<DTOClase> obtenerClasesPorAlumno(Long alumnoId) {
        // Primero verificamos que el alumno existe
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
        
        // Obtenemos las clases del alumno usando el repositorio de clase
        return repositorioClase.findByAlumnoId(alumnoId)
                .stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Verificar si un alumno está inscrito en una clase
     * @param alumnoId ID del alumno
     * @param claseId ID de la clase
     * @return true si está inscrito, false en caso contrario
     */
    public boolean estaInscritoEnClase(Long alumnoId, Long claseId) {
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
                
        return alumno.estaInscritoEnClasePorId(claseId);
    }
    
    /**
     * Contar el número de clases en las que está inscrito un alumno
     * @param alumnoId ID del alumno
     * @return Número de clases
     */
    public int contarClasesPorAlumno(Long alumnoId) {
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);
                
        return alumno.getClasses().size();
    }

    // metodos con paginación
    
    /**
     * Obtiene todos los alumnos con paginación
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar (por defecto: id)
     * @param sortDirection dirección del ordenamiento (por defecto: ASC)
     * @return DTORespuestaPaginada con los alumnos y metadatos de paginación
     */
    @Transactional
    public DTORespuestaPaginada<DTOAlumno> obtenerAlumnosPaginados(
            int page, int size, String sortBy, String sortDirection) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20; // Máximo 100 elementos por página
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Alumno> pageAlumnos = repositorioAlumno.findAllPaged(pageable);
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Busca alumnos por parámetros con paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOAlumno> buscarAlumnosPorParametrosPaginados(
            DTOParametrosBusquedaAlumno parametros, int page, int size, String sortBy, String sortDirection) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Alumno> pageAlumnos;
        
        // Enhanced search logic with "q" parameter support
        if (parametros.hasGeneralSearch()) {
            if (parametros.hasSpecificFilters()) {
                // Use combined search (general + specific filters)
                pageAlumnos = repositorioAlumno.findByGeneralAndSpecificFilters(
                    parametros.q(),
                    parametros.firstName(),
                    parametros.lastName(),
                    parametros.dni(),
                    parametros.email(),
                    parametros.enrolled(),
                    pageable
                );
            } else {
                // Use only general search
                pageAlumnos = repositorioAlumno.findByGeneralSearch(parametros.q(), pageable);
            }
        } else {
            // Use existing specific search logic
            if (parametros.hasSpecificFilters()) {
                pageAlumnos = repositorioAlumno.findByFiltrosPaged(
                    parametros.firstName(),
                    parametros.lastName(),
                    parametros.dni(),
                    parametros.email(),
                    parametros.enrolled(),
                    pageable
                );
            } else {
                pageAlumnos = repositorioAlumno.findAllPaged(pageable);
            }
        }
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Obtiene alumnos matriculados/no matriculados con paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOAlumno> obtenerAlumnosPorMatriculadoPaginados(
            boolean matriculado, int page, int size, String sortBy, String sortDirection) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Alumno> pageAlumnos = repositorioAlumno.findByMatriculadoPaged(matriculado, pageable);
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Obtiene alumnos inscritos en una clase específica con paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOAlumno> obtenerAlumnosPorClasePaginados(
            Long claseId, int page, int size, String sortBy, String sortDirection) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Primero verificamos que la clase existe
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Obtenemos los alumnos de la clase usando la relación JPA
        List<Alumno> alumnosDeClase = new ArrayList<>(clase.getStudents());
        
        // Aplicamos paginación manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alumnosDeClase.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Alumno> pageContent = alumnosDeClase.subList(start, end);
        Page<Alumno> pageAlumnos = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, alumnosDeClase.size());
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }

    /**
     * Obtiene alumnos disponibles (habilitados y matriculados) con paginación
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @return DTORespuestaPaginada con los alumnos disponibles
     */
    public DTORespuestaPaginada<DTOAlumno> obtenerAlumnosDisponiblesPaginados(
            int page, int size, String sortBy, String sortDirection) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Obtener solo alumnos habilitados y matriculados
        // Nota: En un escenario real se implementaría una consulta específica en el repositorio
        List<Alumno> alumnosDisponibles = repositorioAlumno.findAll().stream()
                .filter(alumno -> alumno.isEnabled() && alumno.isEnrolled())
                .collect(Collectors.toList());
        
        // Aplicar paginación manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alumnosDisponibles.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Alumno> pageContent = alumnosDisponibles.subList(start, end);
        Page<Alumno> pageAlumnos = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, alumnosDisponibles.size());
        
        // Convertir a DTOs
        Page<DTOAlumno> pageDTOs = pageAlumnos.map(DTOAlumno::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }

    /**
     * Obtiene alumnos inscritos en una clase específica con diferentes niveles de acceso
     * según el rol del usuario autenticado
     * @param claseId ID de la clase
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página
     * @param sortBy Campo por el que ordenar
     * @param sortDirection Dirección de ordenación (ASC/DESC)
     * @param userRole Rol del usuario autenticado
     * @param currentUserId ID del usuario autenticado (para verificar si es profesor de la clase)
     * @return DTORespuestaAlumnosClase con los alumnos de la clase según el nivel de acceso
     */
    public DTORespuestaAlumnosClase obtenerAlumnosPorClaseConNivelAcceso(
            Long claseId, int page, int size, String sortBy, String sortDirection, 
            String userRole, Long currentUserId) {
        
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "id";
        
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception e) {
            direction = Sort.Direction.ASC;
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Primero verificamos que la clase existe
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Verificar acceso según el rol
        if ("ALUMNO".equals(userRole)) {
            // Para estudiantes, verificar que estén inscritos en la clase
            Alumno alumno = repositorioAlumno.findById(currentUserId).orElse(null);
            ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", currentUserId);
            
            if (!alumno.estaInscritoEnClasePorId(claseId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a esta clase. Debes estar inscrito.");
            }
        }
        
        // Obtenemos los alumnos de la clase usando la relación JPA
        List<Alumno> alumnosDeClase = new ArrayList<>(clase.getStudents());
        
        // Aplicamos paginación manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alumnosDeClase.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Alumno> pageContent = alumnosDeClase.subList(start, end);
        Page<Alumno> pageAlumnos = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, alumnosDeClase.size());
        
        // Convertir a DTOs según el nivel de acceso
        Page<?> pageDTOs;
        String tipoInformacion;
        
        if ("ADMIN".equals(userRole)) {
            // Admin: ve toda la información
            pageDTOs = pageAlumnos.map(DTOAlumno::new);
            tipoInformacion = DTORespuestaAlumnosClase.TIPO_COMPLETA;
        } else if ("PROFESOR".equals(userRole)) {
            // Profesor: verificar si es profesor de esta clase específica
            // La clase ya fue verificada arriba, podemos usarla directamente
            
            if (clase.getTeachers().stream().anyMatch(p -> p.getId().equals(currentUserId))) {
                // Es profesor de esta clase: ve toda la información
                pageDTOs = pageAlumnos.map(DTOAlumno::new);
                tipoInformacion = DTORespuestaAlumnosClase.TIPO_COMPLETA;
            } else {
                // No es profesor de esta clase: solo información pública
                pageDTOs = pageAlumnos.map(DTOAlumnoPublico::new);
                tipoInformacion = DTORespuestaAlumnosClase.TIPO_PUBLICA;
            }
        } else {
            // Alumno: solo información pública
            pageDTOs = pageAlumnos.map(DTOAlumnoPublico::new);
            tipoInformacion = DTORespuestaAlumnosClase.TIPO_PUBLICA;
        }
        
        return new DTORespuestaAlumnosClase(pageDTOs, tipoInformacion);
    }

    // ===== OPTIMIZED ENTITY GRAPH METHODS =====

    /**
     * Obtiene un alumno con sus clases cargadas usando Entity Graph
     * @param id ID del alumno
     * @return DTOAlumno con clases cargadas
     * @throws EntidadNoEncontradaException si no se encuentra el alumno
     */
    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoConClases(Long id) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can access student data
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's data
            Alumno alumno = repositorioAlumno.findByIdWithClasses(id).orElse(null);
            ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
            return new DTOAlumno(alumno);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!id.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros alumnos");
            }
            Alumno alumno = repositorioAlumno.findByIdWithClasses(id).orElse(null);
            ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
            return new DTOAlumno(alumno);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de alumnos");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene un alumno con todas sus relaciones cargadas usando Entity Graph
     * @param id ID del alumno
     * @return DTOAlumno con todas las relaciones cargadas
     * @throws EntidadNoEncontradaException si no se encuentra el alumno
     */
    @Transactional(readOnly = true)
    public DTOAlumno obtenerAlumnoConTodo(Long id) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can access student data
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's data
            Alumno alumno = repositorioAlumno.findByIdWithClasses(id).orElse(null);
            ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
            return new DTOAlumno(alumno);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!id.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros alumnos");
            }
            Alumno alumno = repositorioAlumno.findByIdWithClasses(id).orElse(null);
            ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", id);
            return new DTOAlumno(alumno);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de alumnos");
            return null; // This line will never be reached due to the exception above
        }
    }
}