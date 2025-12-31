package app.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import app.dtos.DTOEntregaEjercicio;
import app.dtos.DTOOperacionArchivo;
import app.dtos.DTOOperacionArchivoResultado;
import app.dtos.DTOPeticionModificarEntrega;
import app.dtos.DTORespuestaModificacionEntrega;
import app.dtos.DTORespuestaPaginada;
import app.dtos.DTORespuestaSubidaArchivo;
import app.entidades.Alumno;
import app.entidades.Ejercicio;
import app.entidades.EntregaEjercicio;
import app.entidades.enums.EEstadoEjercicio;
import app.repositorios.RepositorioAlumno;
import app.repositorios.RepositorioEjercicio;
import app.repositorios.RepositorioEntregaEjercicio;
import app.util.ExceptionUtils;
import app.util.FileUploadUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioEntregaEjercicio {

    private final RepositorioEntregaEjercicio repositorioEntregaEjercicio;
    private final RepositorioEjercicio repositorioEjercicio;
    private final RepositorioAlumno repositorioAlumno;
    private final SecurityUtils securityUtils;
    private final FileUploadUtils fileUploadUtils;

    @Transactional(readOnly = true)
    public DTOEntregaEjercicio obtenerEntregaPorId(Long id) {
        // Use basic findById to avoid MultipleBagFetchException
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or the student who owns the delivery can see it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see all deliveries
            return new DTOEntregaEjercicio(entrega);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver esta entrega");
            }
            return new DTOEntregaEjercicio(entrega);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerTodasLasEntregas() {
        // Security check: Only ADMIN and PROFESOR can see all deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver todas las entregas");
        }
        
        return repositorioEntregaEjercicio.findAll().stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorAlumno(String alumnoId) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can see student's deliveries
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's deliveries
            return repositorioEntregaEjercicio.findByAlumnoEntreganteId(Long.parseLong(alumnoId)).stream()
                    .map(DTOEntregaEjercicio::new)
                    .collect(Collectors.toList());
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver las entregas de otros alumnos");
            }
            return repositorioEntregaEjercicio.findByAlumnoEntreganteId(Long.parseLong(alumnoId)).stream()
                    .map(DTOEntregaEjercicio::new)
                    .collect(Collectors.toList());
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorEjercicio(String ejercicioId) {
        // Security check: Only ADMIN and PROFESOR can see all deliveries for an exercise
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver todas las entregas de un ejercicio");
        }
        
        return repositorioEntregaEjercicio.findByEjercicioId(Long.parseLong(ejercicioId)).stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DTOEntregaEjercicio obtenerEntregaPorAlumnoYEjercicio(String alumnoId, String ejercicioId) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio
                .findByAlumnoEntreganteIdAndEjercicioId(Long.parseLong(alumnoId), Long.parseLong(ejercicioId)).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "alumno y ejercicio", alumnoId + " - " + ejercicioId);
        
        // Security check: Only ADMIN, PROFESOR, or the student themselves can see the delivery
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any delivery
            return new DTOEntregaEjercicio(entrega);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver las entregas de otros alumnos");
            }
            return new DTOEntregaEjercicio(entrega);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
            return null; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorEstado(EEstadoEjercicio estado) {
        // Security check: Only ADMIN and PROFESOR can see deliveries by status
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas por estado");
        }
        
        return repositorioEntregaEjercicio.findByEstado(estado).stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Security check: Only ADMIN and PROFESOR can see deliveries by date range
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas por rango de fechas");
        }
        
        return repositorioEntregaEjercicio.findByFechaEntregaBetween(fechaInicio, fechaFin).stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPorRangoNotas(BigDecimal notaMin, BigDecimal notaMax) {
        // Security check: Only ADMIN and PROFESOR can see deliveries by grade range
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas por rango de notas");
        }
        
        return repositorioEntregaEjercicio.findByNotaBetween(notaMin, notaMax).stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasCalificadas() {
        // Security check: Only ADMIN and PROFESOR can see graded deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas calificadas");
        }
        
        return obtenerEntregasPorEstado(EEstadoEjercicio.CALIFICADO);
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasPendientes() {
        // Security check: Only ADMIN and PROFESOR can see pending deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas pendientes");
        }
        
        return obtenerEntregasPorEstado(EEstadoEjercicio.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<DTOEntregaEjercicio> obtenerEntregasEntregadas() {
        // Security check: Only ADMIN and PROFESOR can see submitted deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver entregas entregadas");
        }
        
        return obtenerEntregasPorEstado(EEstadoEjercicio.ENTREGADO);
    }

    public DTOEntregaEjercicio crearEntrega(Long alumnoId, Long ejercicioId, List<String> archivos) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can create deliveries
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can create deliveries for any student
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only create deliveries for themselves
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No puedes crear entregas para otros alumnos");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear entregas");
        }
        
        // Validate input
        if (alumnoId == null) {
            ExceptionUtils.throwValidationError("El ID del alumno no puede estar vacío");
        }
        
        if (ejercicioId == null) {
            ExceptionUtils.throwValidationError("El ID del ejercicio no puede estar vacío");
        }

        // Check if exercise exists
        Ejercicio ejercicio = repositorioEjercicio.findById(ejercicioId).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", ejercicioId);

        // Check if student exists
        Alumno alumno = repositorioAlumno.findById(alumnoId).orElse(null);
        ExceptionUtils.throwIfNotFound(alumno, "Alumno", "ID", alumnoId);

        // Check if exercise is still active
        if (ejercicio.haVencido()) {
            ExceptionUtils.throwValidationError("No se puede entregar un ejercicio que ya ha vencido");
        }

        // Check if student already has a delivery for this exercise
        repositorioEntregaEjercicio.findByAlumnoEntreganteIdAndEjercicioId(alumnoId, ejercicioId)
                .ifPresent(existing -> {
                    ExceptionUtils.throwValidationError("El alumno ya tiene una entrega para este ejercicio");
                });

        // Create delivery with JPA relationships
        EntregaEjercicio entrega = new EntregaEjercicio(alumno, ejercicio);
        
        // Add files if provided
        if (archivos != null && !archivos.isEmpty()) {
            archivos.forEach(entrega::agregarArchivo);
        }

        // Establish bidirectional relationship
        ejercicio.agregarEntrega(entrega);

        EntregaEjercicio entregaGuardada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaGuardada);
    }

    public DTOEntregaEjercicio calificarEntrega(Long entregaId, BigDecimal nota) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);

        // Security check: Only teachers and admins can grade deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden calificar entregas");
        }

        // Validate grade
        if (nota == null) {
            ExceptionUtils.throwValidationError("La nota no puede estar vacía");
        }
        
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            ExceptionUtils.throwValidationError("La nota debe estar entre 0 y 10");
        }

        // Grade the delivery
        entrega.calificar(nota);

        EntregaEjercicio entregaCalificada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaCalificada);
    }
    
    public DTOEntregaEjercicio calificarEntrega(Long entregaId, BigDecimal nota, String comentarios) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);

        // Security check: Only teachers and admins can grade deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden calificar entregas");
        }

        // Validate grade
        if (nota == null) {
            ExceptionUtils.throwValidationError("La nota no puede estar vacía");
        }
        
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            ExceptionUtils.throwValidationError("La nota debe estar entre 0 y 10");
        }

        // Grade the delivery with comments
        entrega.calificar(nota, comentarios);

        EntregaEjercicio entregaCalificada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaCalificada);
    }

    public DTOEntregaEjercicio actualizarEntrega(Long entregaId, List<String> archivos, BigDecimal nota) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);

        // Security check: Only ADMIN, PROFESOR, or the student who owns the delivery can update it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can update any delivery
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only update their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta entrega");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar entregas");
        }

        // Update files if provided
        if (archivos != null) {
            // Check if delivery can be modified (only pending deliveries can have files updated)
            if (entrega.getEstado() != EEstadoEjercicio.PENDIENTE) {
                ExceptionUtils.throwValidationError("Solo se pueden modificar archivos de entregas pendientes");
            }
            entrega.getArchivosEntregados().clear();
            archivos.forEach(entrega::agregarArchivo);
        }

        // Update grade if provided
        if (nota != null) {
            // Security check: Only teachers and admins can grade deliveries
            if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
                ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden calificar entregas");
            }
            
            // Validate grade
            if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
                ExceptionUtils.throwValidationError("La nota debe estar entre 0 y 10");
            }
            
            // Grade the delivery
            entrega.calificar(nota);
        }

        EntregaEjercicio entregaActualizada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaActualizada);
    }
    
    public DTOEntregaEjercicio actualizarEntrega(Long entregaId, List<String> archivos, BigDecimal nota, String comentarios) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);

        // Security check: Only ADMIN, PROFESOR, or the student who owns the delivery can update it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can update any delivery
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only update their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta entrega");
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar entregas");
        }

        // Update files if provided
        if (archivos != null) {
            // Check if delivery can be modified (only pending deliveries can have files updated)
            if (entrega.getEstado() != EEstadoEjercicio.PENDIENTE) {
                ExceptionUtils.throwValidationError("Solo se pueden modificar archivos de entregas pendientes");
            }
            entrega.getArchivosEntregados().clear();
            archivos.forEach(entrega::agregarArchivo);
        }

        // Update grade and comments if provided
        if (nota != null) {
            // Security check: Only teachers and admins can grade deliveries
            if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
                ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden calificar entregas");
            }
            
            // Validate grade
            if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
                ExceptionUtils.throwValidationError("La nota debe estar entre 0 y 10");
            }
            
            // Grade the delivery with comments
            entrega.calificar(nota, comentarios);
        } else if (comentarios != null) {
            // Security check: Only teachers and admins can add comments
            if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
                ExceptionUtils.throwValidationError("Solo los profesores y administradores pueden agregar comentarios");
            }
            
            // Add comments only
            entrega.agregarComentarios(comentarios);
        }

        EntregaEjercicio entregaActualizada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaActualizada);
    }

    public boolean borrarEntregaPorId(Long id) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", id);

        // Security check: Only ADMIN and PROFESOR can delete deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar entregas");
        }

        repositorioEntregaEjercicio.delete(entrega);
        return true;
    }

    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOEntregaEjercicio> obtenerEntregasPaginadas(
            String alumnoId,
            String ejercicioId,
            String estado,
            BigDecimal notaMin,
            BigDecimal notaMax,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        // Security check: Only ADMIN, PROFESOR, or students viewing their own deliveries
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see all deliveries
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (alumnoId == null || !alumnoId.equals(currentUserId.toString())) {
                // Force filter to current student's deliveries only
                alumnoId = currentUserId.toString();
            }
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
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

        // Build query based on filters using flexible approach
        Page<EntregaEjercicio> entregaPage;
        
        // Prepare filter parameters
        Long alumnoIdFilter = (alumnoId != null && !alumnoId.trim().isEmpty()) ? Long.parseLong(alumnoId.trim()) : null;
        Long ejercicioIdFilter = (ejercicioId != null && !ejercicioId.trim().isEmpty()) ? Long.parseLong(ejercicioId.trim()) : null;
        app.entidades.enums.EEstadoEjercicio estadoFilter = null;
        if (estado != null && !estado.trim().isEmpty()) {
            try {
                estadoFilter = app.entidades.enums.EEstadoEjercicio.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                ExceptionUtils.throwValidationError("Estado inválido. Valores permitidos: PENDIENTE, ENTREGADO, CALIFICADO");
            }
        }
        BigDecimal notaMinFilter = notaMin;
        BigDecimal notaMaxFilter = notaMax;
        
        // Use flexible query that handles all combinations
        entregaPage = repositorioEntregaEjercicio.findByFiltrosFlexibles(
            alumnoIdFilter, ejercicioIdFilter, estadoFilter, notaMinFilter, notaMaxFilter, pageable);

        // Convert to DTOs
        List<DTOEntregaEjercicio> entregas = entregaPage.getContent().stream()
                .map(DTOEntregaEjercicio::new)
                .collect(Collectors.toList());

        // Build response
        return new DTORespuestaPaginada<>(
            entregas,
            entregaPage.getNumber(),
            entregaPage.getSize(),
            entregaPage.getTotalElements(),
            entregaPage.getTotalPages(),
            entregaPage.isFirst(),
            entregaPage.isLast(),
            entregaPage.hasContent(),
            sortBy,
            sortDirection
        );
    }

    @Transactional(readOnly = true)
    public long contarEntregas() {
        // Security check: Only ADMIN and PROFESOR can count all deliveries
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para contar todas las entregas");
        }
        return repositorioEntregaEjercicio.count();
    }

    @Transactional(readOnly = true)
    public long contarEntregasPorAlumno(String alumnoId) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can count student's deliveries
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can count any student's deliveries
            return repositorioEntregaEjercicio.countByAlumnoEntreganteId(Long.parseLong(alumnoId));
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only count their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para contar las entregas de otros alumnos");
            }
            return repositorioEntregaEjercicio.countByAlumnoEntreganteId(Long.parseLong(alumnoId));
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para contar entregas");
            return 0; // This line will never be reached due to the exception above
        }
    }

    @Transactional(readOnly = true)
    public long contarEntregasPorEjercicio(String ejercicioId) {
        // Security check: Only ADMIN and PROFESOR can count deliveries by exercise
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para contar entregas por ejercicio");
        }
        return repositorioEntregaEjercicio.countByEjercicioId(Long.parseLong(ejercicioId));
    }

    @Transactional(readOnly = true)
    public long contarEntregasPorEstado(EEstadoEjercicio estado) {
        // Security check: Only ADMIN and PROFESOR can count deliveries by status
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para contar entregas por estado");
        }
        return repositorioEntregaEjercicio.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerNotaPromedioPorEjercicio(String ejercicioId) {
        // Security check: Only ADMIN and PROFESOR can see average grades by exercise
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver promedios por ejercicio");
        }
        return repositorioEntregaEjercicio.findAverageGradeByEjercicioId(Long.parseLong(ejercicioId));
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerNotaPromedioPorAlumno(String alumnoId) {
        // Security check: Only ADMIN, PROFESOR, or the student themselves can see student's average grade
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see any student's average grade
            return repositorioEntregaEjercicio.findAverageGradeByAlumnoId(Long.parseLong(alumnoId));
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own average grade
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!alumnoId.equals(currentUserId.toString())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver el promedio de otros alumnos");
            }
            return repositorioEntregaEjercicio.findAverageGradeByAlumnoId(Long.parseLong(alumnoId));
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver promedios");
            return BigDecimal.ZERO; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene una entrega con su alumno cargado usando Entity Graph
     * @param id ID de la entrega
     * @return DTOEntregaEjercicio con alumno cargado
     * @throws EntidadNoEncontradaException si no se encuentra la entrega
     */
    @Transactional(readOnly = true)
    public DTOEntregaEjercicio obtenerEntregaConAlumno(Long id) {
        // Security check: Only ADMIN, PROFESOR, or the student who owns the delivery can see it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see all deliveries
            EntregaEjercicio entrega = repositorioEntregaEjercicio.findByIdWithAlumno(id).orElse(null);
            ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", id);
            return new DTOEntregaEjercicio(entrega);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            EntregaEjercicio entrega = repositorioEntregaEjercicio.findByIdWithAlumno(id).orElse(null);
            ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", id);
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver esta entrega");
            }
            return new DTOEntregaEjercicio(entrega);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene una entrega con su ejercicio cargado usando Entity Graph
     * @param id ID de la entrega
     * @return DTOEntregaEjercicio con ejercicio cargado
     * @throws EntidadNoEncontradaException si no se encuentra la entrega
     */
    @Transactional(readOnly = true)
    public DTOEntregaEjercicio obtenerEntregaConEjercicio(Long id) {
        // Security check: Only ADMIN, PROFESOR, or the student who owns the delivery can see it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can see all deliveries
            EntregaEjercicio entrega = repositorioEntregaEjercicio.findByIdWithEjercicio(id).orElse(null);
            ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", id);
            return new DTOEntregaEjercicio(entrega);
        } else if (securityUtils.hasRole("ALUMNO")) {
            // Students can only see their own deliveries
            Long currentUserId = securityUtils.getCurrentUserId();
            EntregaEjercicio entrega = repositorioEntregaEjercicio.findByIdWithEjercicio(id).orElse(null);
            ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", id);
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver esta entrega");
            }
            return new DTOEntregaEjercicio(entrega);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a entregas de ejercicios");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Sube un archivo para una entrega de ejercicio
     * @param file El archivo a subir
     * @param ejercicioId ID del ejercicio
     * @return DTORespuestaSubidaArchivo con información del archivo subido
     */
    public DTORespuestaSubidaArchivo subirArchivoEntrega(MultipartFile file, Long ejercicioId) {
        // Security check: Only students can upload files for their own deliveries
        if (!securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("Solo los alumnos pueden subir archivos para entregas");
        }
        
        Long currentUserId = securityUtils.getCurrentUserId();
        
        // Validate exercise exists
        Ejercicio ejercicio = repositorioEjercicio.findById(ejercicioId).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", ejercicioId);
        
        // Check if exercise is still active
        if (ejercicio.haVencido()) {
            ExceptionUtils.throwValidationError("No se puede subir archivos para un ejercicio que ya ha vencido");
        }
        
        // Save the file
        FileUploadUtils.FileUploadResult uploadResult = fileUploadUtils.saveExerciseDeliveryFile(file, currentUserId, ejercicioId);
        
        // Create response DTO
        return new DTORespuestaSubidaArchivo(
            uploadResult.getOriginalFilename(),
            uploadResult.getUniqueFilename(),
            uploadResult.getRelativePath(),
            file.getContentType(),
            file.getSize(),
            null, // Will be calculated by the DTO
            null, // Will be set to now by the DTO
            null, // Will be calculated by the DTO
            false, // Will be calculated by the DTO
            false  // Will be calculated by the DTO
        );
    }
    
    /**
     * Sube múltiples archivos para una entrega de ejercicio
     * @param files Lista de archivos a subir
     * @param ejercicioId ID del ejercicio
     * @return Lista de DTORespuestaSubidaArchivo con información de los archivos subidos
     */
    public List<DTORespuestaSubidaArchivo> subirArchivosEntrega(List<MultipartFile> files, Long ejercicioId) {
        // Security check: Only students can upload files for their own deliveries
        if (!securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("Solo los alumnos pueden subir archivos para entregas");
        }
        
        Long currentUserId = securityUtils.getCurrentUserId();
        
        // Validate exercise exists
        Ejercicio ejercicio = repositorioEjercicio.findById(ejercicioId).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", ejercicioId);
        
        // Check if exercise is still active
        if (ejercicio.haVencido()) {
            ExceptionUtils.throwValidationError("No se puede subir archivos para un ejercicio que ya ha vencido");
        }
        
        // Validate files list
        if (files == null || files.isEmpty()) {
            ExceptionUtils.throwValidationError("Debe proporcionar al menos un archivo");
        }
        
        // Save all files
        return files.stream()
            .map(file -> {
                FileUploadUtils.FileUploadResult uploadResult = fileUploadUtils.saveExerciseDeliveryFile(file, currentUserId, ejercicioId);
                return new DTORespuestaSubidaArchivo(
                    uploadResult.getOriginalFilename(),
                    uploadResult.getUniqueFilename(),
                    uploadResult.getRelativePath(),
                    file.getContentType(),
                    file.getSize(),
                    null, // Will be calculated by the DTO
                    null, // Will be set to now by the DTO
                    null, // Will be calculated by the DTO
                    false, // Will be calculated by the DTO
                    false  // Will be calculated by the DTO
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Crea una entrega con archivos subidos previamente
     * @param ejercicioId ID del ejercicio
     * @param archivosRutas Lista de rutas de archivos ya subidos
     * @return DTOEntregaEjercicio de la entrega creada
     */
    public DTOEntregaEjercicio crearEntregaConArchivos(Long ejercicioId, List<String> archivosRutas) {
        // Security check: Only students can create deliveries for themselves
        if (!securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("Solo los alumnos pueden crear entregas");
        }
        
        Long currentUserId = securityUtils.getCurrentUserId();
        
        // Validate exercise exists
        Ejercicio ejercicio = repositorioEjercicio.findById(ejercicioId).orElse(null);
        ExceptionUtils.throwIfNotFound(ejercicio, "Ejercicio", "ID", ejercicioId);
        
        // Check if exercise is still active
        if (ejercicio.haVencido()) {
            ExceptionUtils.throwValidationError("No se puede entregar un ejercicio que ya ha vencido");
        }
        
        // Check if student already has a delivery for this exercise
        repositorioEntregaEjercicio.findByAlumnoEntreganteIdAndEjercicioId(currentUserId, ejercicioId)
                .ifPresent(existing -> {
                    ExceptionUtils.throwValidationError("Ya tienes una entrega para este ejercicio");
                });
        
        // Validate that all files exist
        if (archivosRutas != null && !archivosRutas.isEmpty()) {
            for (String ruta : archivosRutas) {
                if (!fileUploadUtils.fileExists(ruta)) {
                    ExceptionUtils.throwValidationError("El archivo " + ruta + " no existe");
                }
            }
        }
        
        // Create delivery
        EntregaEjercicio entrega = new EntregaEjercicio(
            repositorioAlumno.findById(currentUserId).orElse(null), 
            ejercicio
        );
        
        // Add files if provided
        if (archivosRutas != null && !archivosRutas.isEmpty()) {
            archivosRutas.forEach(entrega::agregarArchivo);
        }
        
        // Establish bidirectional relationship
        ejercicio.agregarEntrega(entrega);
        
        EntregaEjercicio entregaGuardada = repositorioEntregaEjercicio.save(entrega);
        return new DTOEntregaEjercicio(entregaGuardada);
    }
    
    /**
     * Elimina un archivo de una entrega
     * @param entregaId ID de la entrega
     * @param rutaArchivo Ruta del archivo a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarArchivoEntrega(Long entregaId, String rutaArchivo) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);
        
        // Security check: Only the student who owns the delivery can delete files
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can delete files from any delivery
        } else if (securityUtils.hasRole("ALUMNO")) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar archivos de esta entrega");
            }
        } else {
            ExceptionUtils.throwAccessDenied("No tienes permisos para eliminar archivos");
        }
        
        // Check if delivery can be modified (only pending deliveries can have files deleted)
        if (entrega.getEstado() != EEstadoEjercicio.PENDIENTE && entrega.getEstado() != EEstadoEjercicio.ENTREGADO) {
            ExceptionUtils.throwValidationError("Solo se pueden eliminar archivos de entregas pendientes o entregadas");
        }
        
        // Remove file from delivery
        entrega.removerArchivo(rutaArchivo);
        repositorioEntregaEjercicio.save(entrega);
        
        // Delete physical file
        return fileUploadUtils.deleteFile(rutaArchivo);
    }
    
    /**
     * Modifica una entrega existente
     * Permite actualizar comentarios y realizar operaciones con archivos
     * @param entregaId ID de la entrega a modificar
     * @param peticion Petición de modificación
     * @return Respuesta con los resultados de las operaciones
     */
    public DTORespuestaModificacionEntrega modificarEntrega(Long entregaId, DTOPeticionModificarEntrega peticion) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);
        
        // Security check: Only the student who owns the delivery can modify it
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can modify any delivery
        } else if (securityUtils.hasRole("ALUMNO")) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para modificar esta entrega");
            }
        } else {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar entregas");
        }
        
        // Check if delivery can be modified (only pending or delivered deliveries can be modified)
        if (entrega.getEstado() != EEstadoEjercicio.PENDIENTE && entrega.getEstado() != EEstadoEjercicio.ENTREGADO) {
            ExceptionUtils.throwValidationError("Solo se pueden modificar entregas pendientes o entregadas");
        }
        
        // Validate request
        if (!peticion.esValida()) {
            ExceptionUtils.throwValidationError("La petición debe incluir comentarios o operaciones con archivos");
        }
        
        List<DTOOperacionArchivoResultado> operacionesRealizadas = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        
        // Update comments if provided
        if (peticion.tieneComentarios()) {
            entrega.setComentarios(peticion.comentarios());
        }
        
        // Process file operations if provided
        if (peticion.tieneOperacionesArchivos()) {
            for (DTOOperacionArchivo operacion : peticion.operacionesArchivos()) {
                if (!operacion.esValida()) {
                    operacionesRealizadas.add(new DTOOperacionArchivoResultado(
                        operacion.tipo(),
                        operacion.rutaArchivo(),
                        operacion.nuevoNombre(),
                        false,
                        "Operación inválida",
                        "Los parámetros de la operación no son válidos"
                    ));
                    continue;
                }
                
                try {
                    DTOOperacionArchivoResultado resultado = procesarOperacionArchivo(entrega, operacion);
                    operacionesRealizadas.add(resultado);
                    
                    if (!resultado.fueExitosa()) {
                        errores.add(resultado.error());
                    }
                } catch (Exception e) {
                    operacionesRealizadas.add(new DTOOperacionArchivoResultado(
                        operacion.tipo(),
                        operacion.rutaArchivo(),
                        operacion.nuevoNombre(),
                        false,
                        "Error procesando operación",
                        e.getMessage()
                    ));
                    errores.add("Error procesando operación " + operacion.tipo() + ": " + e.getMessage());
                }
            }
        }
        
        // Save the modified delivery
        EntregaEjercicio entregaModificada = repositorioEntregaEjercicio.save(entrega);
        
        return new DTORespuestaModificacionEntrega(
            entregaModificada.getId(),
            entregaModificada.getComentarios(),
            entregaModificada.getArchivosEntregados(),
            entregaModificada.contarArchivos(),
            LocalDateTime.now(),
            operacionesRealizadas,
            errores
        );
    }
    
    /**
     * Procesa una operación específica con archivos
     * @param entrega Entrega a modificar
     * @param operacion Operación a realizar
     * @return Resultado de la operación
     */
    private DTOOperacionArchivoResultado procesarOperacionArchivo(EntregaEjercicio entrega, DTOOperacionArchivo operacion) {
        // Verify file exists in delivery
        if (!entrega.getArchivosEntregados().contains(operacion.rutaArchivo())) {
            return new DTOOperacionArchivoResultado(
                operacion.tipo(),
                operacion.rutaArchivo(),
                operacion.nuevoNombre(),
                false,
                "Archivo no encontrado",
                "El archivo " + operacion.rutaArchivo() + " no existe en esta entrega"
            );
        }
        
        if (operacion.esEliminacion()) {
            return procesarEliminacionArchivo(entrega, operacion);
        } else if (operacion.esRenombrado()) {
            return procesarRenombradoArchivo(entrega, operacion);
        } else {
            return new DTOOperacionArchivoResultado(
                operacion.tipo(),
                operacion.rutaArchivo(),
                operacion.nuevoNombre(),
                false,
                "Tipo de operación no soportado",
                "El tipo de operación " + operacion.tipo() + " no está soportado"
            );
        }
    }
    
    /**
     * Procesa la eliminación de un archivo
     * @param entrega Entrega a modificar
     * @param operacion Operación de eliminación
     * @return Resultado de la operación
     */
    private DTOOperacionArchivoResultado procesarEliminacionArchivo(EntregaEjercicio entrega, DTOOperacionArchivo operacion) {
        try {
            // Remove file from delivery
            entrega.removerArchivo(operacion.rutaArchivo());
            
            // Delete physical file
            boolean archivoEliminado = fileUploadUtils.deleteFile(operacion.rutaArchivo());
            
            if (archivoEliminado) {
                return new DTOOperacionArchivoResultado(
                    operacion.tipo(),
                    operacion.rutaArchivo(),
                    null,
                    true,
                    "Archivo eliminado exitosamente",
                    null
                );
            } else {
                return new DTOOperacionArchivoResultado(
                    operacion.tipo(),
                    operacion.rutaArchivo(),
                    null,
                    false,
                    "Error eliminando archivo físico",
                    "No se pudo eliminar el archivo físico del sistema"
                );
            }
        } catch (Exception e) {
            return new DTOOperacionArchivoResultado(
                operacion.tipo(),
                operacion.rutaArchivo(),
                null,
                false,
                "Error en eliminación",
                e.getMessage()
            );
        }
    }
    
    /**
     * Procesa el renombrado de un archivo
     * @param entrega Entrega a modificar
     * @param operacion Operación de renombrado
     * @return Resultado de la operación
     */
    private DTOOperacionArchivoResultado procesarRenombradoArchivo(EntregaEjercicio entrega, DTOOperacionArchivo operacion) {
        try {
            // Generate new file path with new name
            String nuevaRuta = fileUploadUtils.renombrarArchivo(operacion.rutaArchivo(), operacion.nuevoNombre());
            
            if (nuevaRuta != null) {
                // Update file path in delivery
                entrega.removerArchivo(operacion.rutaArchivo());
                entrega.agregarArchivo(nuevaRuta);
                
                return new DTOOperacionArchivoResultado(
                    operacion.tipo(),
                    operacion.rutaArchivo(),
                    operacion.nuevoNombre(),
                    true,
                    "Archivo renombrado exitosamente",
                    null
                );
            } else {
                return new DTOOperacionArchivoResultado(
                    operacion.tipo(),
                    operacion.rutaArchivo(),
                    operacion.nuevoNombre(),
                    false,
                    "Error renombrando archivo",
                    "No se pudo renombrar el archivo físico"
                );
            }
        } catch (Exception e) {
            return new DTOOperacionArchivoResultado(
                operacion.tipo(),
                operacion.rutaArchivo(),
                operacion.nuevoNombre(),
                false,
                "Error en renombrado",
                e.getMessage()
            );
        }
    }
    
    /**
     * Agrega archivos adicionales a una entrega existente
     * @param entregaId ID de la entrega
     * @param archivos Lista de archivos a agregar
     * @return Respuesta con información de los archivos agregados
     */
    public List<DTORespuestaSubidaArchivo> agregarArchivosEntrega(Long entregaId, List<MultipartFile> archivos) {
        EntregaEjercicio entrega = repositorioEntregaEjercicio.findById(entregaId).orElse(null);
        ExceptionUtils.throwIfNotFound(entrega, "Entrega", "ID", entregaId);
        
        // Security check: Only the student who owns the delivery can add files
        if (securityUtils.hasRole("ADMIN") || securityUtils.hasRole("PROFESOR")) {
            // Admins and professors can add files to any delivery
        } else if (securityUtils.hasRole("ALUMNO")) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!entrega.getAlumno().getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para agregar archivos a esta entrega");
            }
        } else {
            ExceptionUtils.throwAccessDenied("No tienes permisos para agregar archivos");
        }
        
        // Check if delivery can be modified
        if (entrega.getEstado() != EEstadoEjercicio.PENDIENTE && entrega.getEstado() != EEstadoEjercicio.ENTREGADO) {
            ExceptionUtils.throwValidationError("Solo se pueden agregar archivos a entregas pendientes o entregadas");
        }
        
        // Validate files
        if (archivos == null || archivos.isEmpty()) {
            ExceptionUtils.throwValidationError("Debe proporcionar al menos un archivo");
        }
        
        List<DTORespuestaSubidaArchivo> archivosAgregados = new ArrayList<>();
        
        for (MultipartFile archivo : archivos) {
            try {
                // Upload file
                FileUploadUtils.FileUploadResult uploadResult = fileUploadUtils.guardarArchivoEntrega(
                    entrega.getAlumno().getId(),
                    entrega.getEjercicio().getId(),
                    archivo
                );
                
                // Add to delivery
                entrega.agregarArchivo(uploadResult.getRelativePath());
                
                // Create response
                archivosAgregados.add(new DTORespuestaSubidaArchivo(
                    uploadResult.getOriginalFilename(),
                    uploadResult.getUniqueFilename(),
                    uploadResult.getRelativePath(),
                    archivo.getContentType(),
                    archivo.getSize(),
                    fileUploadUtils.formatFileSize(archivo.getSize()),
                    LocalDateTime.now(),
                    fileUploadUtils.getFileExtension(archivo.getOriginalFilename()),
                    fileUploadUtils.isImageFile(archivo.getOriginalFilename()),
                    fileUploadUtils.isPdfFile(archivo.getOriginalFilename())
                ));
                
            } catch (Exception e) {
                throw new RuntimeException("Error procesando archivo " + archivo.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        // Save the modified delivery
        repositorioEntregaEjercicio.save(entrega);
        
        return archivosAgregados;
    }
}
