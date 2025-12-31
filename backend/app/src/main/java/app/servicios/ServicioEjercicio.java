package app.servicios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOEjercicio;
import app.dtos.DTOEjercicioConEntrega;
import app.dtos.DTOEntregaEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Ejercicio;
import app.entidades.Clase;
import app.repositorios.RepositorioEjercicio;
import app.repositorios.RepositorioClase;
import app.repositorios.RepositorioEntregaEjercicio;
import app.util.ExceptionUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioEjercicio {

    private final RepositorioEjercicio repositorioEjercicio;
    private final RepositorioClase repositorioClase;
    private final RepositorioEntregaEjercicio repositorioEntregaEjercicio;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public DTOEjercicio obtenerEjercicioPorId(Long id) {
        Ejercicio ejercicio = repositorioEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return new DTOEjercicio(ejercicio);
    }

    @Transactional(readOnly = true)
    public DTOEjercicio obtenerEjercicioPorNombre(String nombre) {
        Ejercicio ejercicio = repositorioEjercicio.findByName(nombre).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "nombre", nombre);
        
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return new DTOEjercicio(ejercicio);
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerTodosLosEjercicios() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findAll().stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorNombre(String nombre) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findByNameContaining(nombre).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorEnunciado(String enunciado) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findByStatementContaining(enunciado).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosPorClase(Long claseId) {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        return repositorioEjercicio.findByClaseId(claseId).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosEnPlazo() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        return repositorioEjercicio.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(ahora, ahora).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosVencidos() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        return repositorioEjercicio.findByEndDateBefore(ahora).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEjercicio> obtenerEjerciciosFuturos() {
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        return repositorioEjercicio.findByStartDateAfter(ahora).stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());
    }

    public DTOEjercicio crearEjercicio(String nombre, String enunciado, LocalDateTime fechaInicioPlazo, 
                                      LocalDateTime fechaFinalPlazo, Long claseId) {
        // Security check: Only ADMIN and PROFESOR can create exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear ejercicios");
        }
        
        // Validate input
        if (nombre == null || nombre.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El nombre del ejercicio no puede estar vacío");
        }
        
        if (enunciado == null || enunciado.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El enunciado del ejercicio no puede estar vacío");
        }
        
        if (fechaInicioPlazo == null) {
            ExceptionUtils.throwValidationError("La fecha de inicio del plazo no puede estar vacía");
        }
        
        if (fechaFinalPlazo == null) {
            ExceptionUtils.throwValidationError("La fecha final del plazo no puede estar vacía");
        }
        
        if (claseId == null) {
            ExceptionUtils.throwValidationError("El ID de la clase no puede estar vacío");
        }

        // Validate date logic
        if (fechaFinalPlazo.isBefore(fechaInicioPlazo)) {
            ExceptionUtils.throwValidationError("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }

        // Check for duplicates
        if (repositorioEjercicio.existsByName(nombre)) {
            ExceptionUtils.throwValidationError("Ya existe un ejercicio con el nombre: " + nombre);
        }

        // Get the class first
        Clase clase = repositorioClase.findById(claseId).orElse(null);
        ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
        
        // Create exercise with the class
        Ejercicio ejercicio = new Ejercicio(
            nombre.trim(),
            enunciado.trim(),
            fechaInicioPlazo,
            fechaFinalPlazo,
            clase
        );

        Ejercicio ejercicioGuardado = repositorioEjercicio.save(ejercicio);
        
        // Automatically add the exercise to the class's exercise list
        clase.agregarEjercicio(ejercicioGuardado);
        repositorioClase.save(clase);
        
        return new DTOEjercicio(ejercicioGuardado);
    }

    public DTOEjercicio actualizarEjercicio(Long id, String nombre, String enunciado, 
                                           LocalDateTime fechaInicioPlazo, LocalDateTime fechaFinalPlazo) {
        Ejercicio ejercicio = repositorioEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", id);

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can update exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para actualizar ejercicios");
        }

        // Validate input
        if (nombre == null || nombre.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El nombre del ejercicio no puede estar vacío");
        }
        
        if (enunciado == null || enunciado.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El enunciado del ejercicio no puede estar vacío");
        }
        
        if (fechaInicioPlazo == null) {
            ExceptionUtils.throwValidationError("La fecha de inicio del plazo no puede estar vacía");
        }
        
        if (fechaFinalPlazo == null) {
            ExceptionUtils.throwValidationError("La fecha final del plazo no puede estar vacía");
        }

        // Validate date logic
        if (fechaFinalPlazo.isBefore(fechaInicioPlazo)) {
            ExceptionUtils.throwValidationError("La fecha final del plazo no puede ser anterior a la fecha inicial");
        }

        // Check for duplicates (excluding current exercise)
        if (repositorioEjercicio.existsByNameAndIdNot(nombre, id)) {
            ExceptionUtils.throwValidationError("Ya existe un ejercicio con el nombre: " + nombre);
        }

        // Update exercise
        ejercicio.setName(nombre.trim());
        ejercicio.setStatement(enunciado.trim());
        ejercicio.setStartDate(fechaInicioPlazo);
        ejercicio.setEndDate(fechaFinalPlazo);

        Ejercicio ejercicioActualizado = repositorioEjercicio.save(ejercicio);
        return new DTOEjercicio(ejercicioActualizado);
    }

    public boolean borrarEjercicioPorId(Long id) {
        Ejercicio ejercicio = repositorioEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", id);

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can delete exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar ejercicios");
        }

        // Remove the exercise from the class's exercise list before deleting
        Clase clase = ejercicio.getClase();
        if (clase == null) {
            ExceptionUtils.throwValidationError("Exercise " + ejercicio.getId() + " is not associated with any class");
        }
        
        clase.removerEjercicio(ejercicio);
        repositorioClase.save(clase);

        repositorioEjercicio.delete(ejercicio);
        return true;
    }

    public boolean borrarEjercicioPorNombre(String nombre) {
        Ejercicio ejercicio = repositorioEjercicio.findByName(nombre).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "nombre", nombre);

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can delete exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar ejercicios");
        }

        repositorioEjercicio.delete(ejercicio);
        return true;
    }

    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOEjercicio> obtenerEjerciciosPaginados(
            String q,
            String name,
            String statement,
            String classId,
            String status,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access exercises
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a ejercicios");
        }

        // Validate pagination parameters
        if (page < 0) {
            ExceptionUtils.throwValidationError("El número de página no puede ser negativo");
        }
        if (size < 1 || size > 100) {
            ExceptionUtils.throwValidationError("El tamaño de página debe estar entre 1 y 100");
        }

        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build query based on filters using simple approach
        Page<Ejercicio> ejercicioPage;
        
        // Prepare filter parameters
        String searchTerm = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String nombreFilter = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String enunciadoFilter = (statement != null && !statement.trim().isEmpty()) ? statement.trim() : null;
        String classIdFilter = (classId != null && !classId.trim().isEmpty()) ? classId.trim() : null;
        LocalDateTime now = LocalDateTime.now();
        
        // Simple filtering approach - use basic Spring Data JPA methods
        if (searchTerm != null) {
            // Search in both name and statement
            ejercicioPage = repositorioEjercicio.findByNameContainingOrStatementContaining(
                searchTerm, searchTerm, pageable);
        } else if (nombreFilter != null && enunciadoFilter != null) {
            // Both name and statement filters
            ejercicioPage = repositorioEjercicio.findByNameContainingOrStatementContaining(
                nombreFilter, enunciadoFilter, pageable);
        } else if (nombreFilter != null) {
            // Only name filter
            ejercicioPage = repositorioEjercicio.findByNameContaining(nombreFilter, pageable);
        } else if (enunciadoFilter != null) {
            // Only statement filter
            ejercicioPage = repositorioEjercicio.findByStatementContaining(enunciadoFilter, pageable);
        } else if (classIdFilter != null) {
            // Only class ID filter
            ejercicioPage = repositorioEjercicio.findByClaseId(Long.parseLong(classIdFilter), pageable);
        } else {
            // No filters, get all
            ejercicioPage = repositorioEjercicio.findAll(pageable);
        }

        // Apply status filter in memory if needed
        List<Ejercicio> filteredContent = ejercicioPage.getContent();
        if (status != null && !status.trim().isEmpty()) {
            String statusFilter = status.toUpperCase();
            filteredContent = filteredContent.stream()
                .filter(ejercicio -> {
                    switch (statusFilter) {
                        case "ACTIVE":
                            return ejercicio.estaEnPlazo();
                        case "EXPIRED":
                            return ejercicio.haVencido();
                        case "FUTURE":
                            return ejercicio.noHaComenzado();
                        case "WITH_DELIVERIES":
                            return ejercicio.contarEntregas() > 0;
                        case "WITHOUT_DELIVERIES":
                            return ejercicio.contarEntregas() == 0;
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
        }

        // Convert to DTOs
        List<DTOEjercicio> ejercicios = filteredContent.stream()
                .map(DTOEjercicio::new)
                .collect(Collectors.toList());

        // Build response
        return new DTORespuestaPaginada<DTOEjercicio>(
            ejercicios,
            ejercicioPage.getNumber(),
            ejercicioPage.getSize(),
            ejercicioPage.getTotalElements(),
            ejercicioPage.getTotalPages(),
            ejercicioPage.isFirst(),
            ejercicioPage.isLast(),
            ejercicioPage.hasContent(),
            sortBy,
            sortDirection
        );
    }

    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOEjercicioConEntrega> obtenerEjerciciosConEntregaPaginados(
            String q,
            String name,
            String statement,
            String classId,
            String status,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // Security check: Only ALUMNO can access exercises with delivery info
        if (!securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("Solo los estudiantes pueden acceder a ejercicios con información de entrega");
        }

        // Get current user ID
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId == null) {
            ExceptionUtils.throwAccessDenied("Usuario no autenticado");
        }

        // Validate pagination parameters
        if (page < 0) {
            ExceptionUtils.throwValidationError("El número de página no puede ser negativo");
        }
        if (size < 1 || size > 100) {
            ExceptionUtils.throwValidationError("El tamaño de página debe estar entre 1 y 100");
        }

        // Create pageable
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build query based on filters using simple approach
        Page<Ejercicio> ejercicioPage;
        
        // Prepare filter parameters
        String searchTerm = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String nombreFilter = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String enunciadoFilter = (statement != null && !statement.trim().isEmpty()) ? statement.trim() : null;
        String classIdFilter = (classId != null && !classId.trim().isEmpty()) ? classId.trim() : null;
        
        // Simple filtering approach - use basic Spring Data JPA methods
        if (searchTerm != null) {
            // Search in both name and statement
            ejercicioPage = repositorioEjercicio.findByNameContainingOrStatementContaining(
                searchTerm, searchTerm, pageable);
        } else if (nombreFilter != null && enunciadoFilter != null) {
            // Both name and statement filters
            ejercicioPage = repositorioEjercicio.findByNameContainingOrStatementContaining(
                nombreFilter, enunciadoFilter, pageable);
        } else if (nombreFilter != null) {
            // Only name filter
            ejercicioPage = repositorioEjercicio.findByNameContaining(nombreFilter, pageable);
        } else if (enunciadoFilter != null) {
            // Only statement filter
            ejercicioPage = repositorioEjercicio.findByStatementContaining(enunciadoFilter, pageable);
        } else if (classIdFilter != null) {
            // Only class ID filter
            ejercicioPage = repositorioEjercicio.findByClaseId(Long.parseLong(classIdFilter), pageable);
        } else {
            // No filters, get all
            ejercicioPage = repositorioEjercicio.findAll(pageable);
        }

        // Apply status filter in memory if needed
        List<Ejercicio> filteredContent = ejercicioPage.getContent();
        if (status != null && !status.trim().isEmpty()) {
            String statusFilter = status.toUpperCase();
            filteredContent = filteredContent.stream()
                .filter(ejercicio -> {
                    switch (statusFilter) {
                        case "ACTIVE":
                            return ejercicio.estaEnPlazo();
                        case "EXPIRED":
                            return ejercicio.haVencido();
                        case "FUTURE":
                            return ejercicio.noHaComenzado();
                        case "WITH_DELIVERIES":
                            return ejercicio.contarEntregas() > 0;
                        case "WITHOUT_DELIVERIES":
                            return ejercicio.contarEntregas() == 0;
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
        }

        // Convert to DTOs with delivery information
        List<DTOEjercicioConEntrega> ejercicios = filteredContent.stream()
                .map(ejercicio -> {
                    // Check if current user has a delivery for this exercise
                    var entrega = repositorioEntregaEjercicio.findByAlumnoEntreganteIdAndEjercicioId(currentUserId, ejercicio.getId());
                    if (entrega.isPresent()) {
                        return DTOEjercicioConEntrega.from(ejercicio, new DTOEntregaEjercicio(entrega.get()));
                    } else {
                        return DTOEjercicioConEntrega.from(ejercicio);
                    }
                })
                .collect(Collectors.toList());

        // Build response
        return new DTORespuestaPaginada<DTOEjercicioConEntrega>(
            ejercicios,
            ejercicioPage.getNumber(),
            ejercicioPage.getSize(),
            ejercicioPage.getTotalElements(),
            ejercicioPage.getTotalPages(),
            ejercicioPage.isFirst(),
            ejercicioPage.isLast(),
            ejercicioPage.hasContent(),
            sortBy,
            sortDirection
        );
    }
}
