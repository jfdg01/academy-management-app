package app.servicios;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOActualizacionProfesor;
import app.dtos.DTOClase;
import app.dtos.DTOParametrosBusquedaProfesor;
import app.dtos.DTOPeticionRegistroProfesor;
import app.dtos.DTOProfesor;
import app.dtos.DTOProfesorPublico;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Clase;
import app.entidades.Profesor;
import app.excepciones.EntidadNoEncontradaException;
import app.repositorios.RepositorioClase;
import app.repositorios.RepositorioProfesor;
import app.util.ExceptionUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

/**
 * Servicio para la gestión de profesores
 * Implementa la lógica de negocio según el UML
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ServicioProfesor {

    private final RepositorioProfesor repositorioProfesor;
    private final RepositorioClase repositorioClase;
    private final PasswordEncoder passwordEncoder;
    private final ServicioCachePassword servicioCachePassword;
    private final SecurityUtils securityUtils;

    // ===== MÉTODOS DE CONSULTA BÁSICOS =====

    /**
     * Obtiene todos los profesores
     * @return Lista de DTOProfesor
     */
    @Transactional(readOnly = true)
    public List<DTOProfesor> obtenerProfesores() {
        // Security check: Only ADMIN can see all professors
        if (!securityUtils.hasRole("ADMIN")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para ver todos los profesores");
        }
        
        return repositorioProfesor.findAllOrderedById()
                .stream()
                .map(DTOProfesor::new)
                .toList();
    }

    /**
     * Obtiene un profesor por su ID
     * @param id ID del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorPorId(Long id) {
        // Use basic findById to avoid MultipleBagFetchException
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);
        
        // Security check: Only ADMIN, or the professor themselves can access professor data
        if (securityUtils.hasRole("ADMIN")) {
            // Admins can see any professor's data
            return new DTOProfesor(profesor);
        } else if (securityUtils.hasRole("PROFESOR")) {
            // Professors can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!id.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros profesores");
            }
            return new DTOProfesor(profesor);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de profesores");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene un profesor con sus clases cargadas usando Entity Graph
     * @param id ID del profesor
     * @return DTOProfesor con clases cargadas
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorConClases(Long id) {
        // Security check: Only ADMIN, or the professor themselves can access professor data
        if (securityUtils.hasRole("ADMIN")) {
            // Admins can see any professor's data
            Profesor profesor = repositorioProfesor.findByIdWithClasses(id).orElse(null);
            ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);
            return new DTOProfesor(profesor);
        } else if (securityUtils.hasRole("PROFESOR")) {
            // Professors can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!id.equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros profesores");
            }
            Profesor profesor = repositorioProfesor.findByIdWithClasses(id).orElse(null);
            ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);
            return new DTOProfesor(profesor);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de profesores");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene un profesor por su email
     * @param email Email del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorPorEmail(String email) {
        Profesor profesor = repositorioProfesor.findByEmail(email).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "email", email);
        
        // Security check: Only ADMIN, or the professor themselves can access professor data
        if (securityUtils.hasRole("ADMIN")) {
            // Admins can see any professor's data
            return new DTOProfesor(profesor);
        } else if (securityUtils.hasRole("PROFESOR")) {
            // Professors can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!profesor.getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros profesores");
            }
            return new DTOProfesor(profesor);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de profesores");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene un profesor por su nombre de usuario
     * @param usuario Nombre de usuario del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorPorUsuario(String usuario) {
        Profesor profesor = repositorioProfesor.findByUsername(usuario).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "usuario", usuario);
        
        // Security check: Only ADMIN, or the professor themselves can access professor data
        if (securityUtils.hasRole("ADMIN")) {
            // Admins can see any professor's data
            return new DTOProfesor(profesor);
        } else if (securityUtils.hasRole("PROFESOR")) {
            // Professors can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!profesor.getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros profesores");
            }
            return new DTOProfesor(profesor);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de profesores");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene un profesor por su DNI
     * @param dni DNI del profesor
     * @return DTOProfesor
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesor obtenerProfesorPorDni(String dni) {
        Profesor profesor = repositorioProfesor.findByDni(dni).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "DNI", dni);
        
        // Security check: Only ADMIN, or the professor themselves can access professor data
        if (securityUtils.hasRole("ADMIN")) {
            // Admins can see any professor's data
            return new DTOProfesor(profesor);
        } else if (securityUtils.hasRole("PROFESOR")) {
            // Professors can only see their own data
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!profesor.getId().equals(currentUserId)) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para ver los datos de otros profesores");
            }
            return new DTOProfesor(profesor);
        } else {
            // Any other role is not authorized
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de profesores");
            return null; // This line will never be reached due to the exception above
        }
    }

    /**
     * Obtiene información pública de un profesor por su ID
     * @param id ID del profesor
     * @return DTOProfesorPublico
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    @Transactional(readOnly = true)
    public DTOProfesorPublico obtenerProfesorPublicoPorId(Long id) {
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);
        
        // Security check: Only ADMIN, PROFESOR, or ALUMNO can access public professor data
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a datos de profesores");
        }
        
        return new DTOProfesorPublico(profesor);
    }

    // ===== MÉTODOS DE BÚSQUEDA =====

    /**
     * Busca profesores por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de profesores que contienen el nombre especificado
     */
    @Transactional(readOnly = true)
    public List<DTOProfesor> buscarProfesoresPorNombre(String nombre) {
        // Security check: Only ADMIN can search professors
        if (!securityUtils.hasRole("ADMIN")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para buscar profesores");
        }
        
        List<Profesor> profesores = repositorioProfesor.findByNombreContainingIgnoreCase(nombre);
        return profesores.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca profesores por apellidos (contiene, ignorando mayúsculas)
     * @param apellidos Apellidos a buscar
     * @return Lista de profesores que contienen los apellidos especificados
     */
    @Transactional(readOnly = true)
    public List<DTOProfesor> buscarProfesoresPorApellidos(String apellidos) {
        // Security check: Only ADMIN can search professors
        if (!securityUtils.hasRole("ADMIN")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para buscar profesores");
        }
        List<Profesor> profesores = repositorioProfesor.findByApellidosContainingIgnoreCase(apellidos);
        return profesores.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca profesores según diversos parámetros
     * @param parametros Parámetros de búsqueda
     * @return Lista de DTOProfesor
     */
    @Transactional(readOnly = true)
    public List<DTOProfesor> buscarProfesoresPorParametros(DTOParametrosBusquedaProfesor parametros) {
        // Enhanced search logic with "q" parameter support
        if (parametros.hasGeneralSearch()) {
            if (parametros.hasSpecificFilters()) {
                // Use combined search (general + specific filters) without pagination
                List<Profesor> profesores = repositorioProfesor.findAll().stream()
                    .filter(p -> {
                        String searchTerm = parametros.q().toLowerCase();
                        boolean generalMatch = p.getFirstName().toLowerCase().contains(searchTerm) ||
                                             p.getLastName().toLowerCase().contains(searchTerm) ||
                                             p.getEmail().toLowerCase().contains(searchTerm) ||
                                             p.getUsername().toLowerCase().contains(searchTerm) ||
                                             p.getDni().toLowerCase().contains(searchTerm);
                        
                        boolean specificMatch = (parametros.firstName() == null || 
                                               p.getFirstName().toLowerCase().contains(parametros.firstName().toLowerCase())) &&
                                              (parametros.lastName() == null || 
                                               p.getLastName().toLowerCase().contains(parametros.lastName().toLowerCase())) &&
                                              (parametros.dni() == null || 
                                               p.getDni().toLowerCase().contains(parametros.dni().toLowerCase())) &&
                                              (parametros.email() == null || 
                                               p.getEmail().toLowerCase().contains(parametros.email().toLowerCase()));
                        
                        return generalMatch && specificMatch;
                    })
                    .collect(Collectors.toList());
                
                return profesores.stream().map(DTOProfesor::new).toList();
            } else {
                // Use only general search without pagination
                List<Profesor> profesores = repositorioProfesor.findAll().stream()
                    .filter(p -> {
                        String searchTerm = parametros.q().toLowerCase();
                        return p.getFirstName().toLowerCase().contains(searchTerm) ||
                               p.getLastName().toLowerCase().contains(searchTerm) ||
                               p.getEmail().toLowerCase().contains(searchTerm) ||
                               p.getUsername().toLowerCase().contains(searchTerm) ||
                               p.getDni().toLowerCase().contains(searchTerm);
                    })
                    .collect(Collectors.toList());
                
                return profesores.stream().map(DTOProfesor::new).toList();
            }
        } else {
            // Use existing specific search logic
            if (parametros.hasSpecificFilters()) {
                // Buscar por filtros básicos
                List<Profesor> profesores = repositorioProfesor.findAll().stream()
                        .filter(p -> (parametros.firstName() == null || 
                                      p.getFirstName().toLowerCase().contains(parametros.firstName().toLowerCase())))
                        .filter(p -> (parametros.lastName() == null || 
                                      p.getLastName().toLowerCase().contains(parametros.lastName().toLowerCase())))
                        .filter(p -> (parametros.dni() == null || 
                                      p.getDni().toLowerCase().contains(parametros.dni().toLowerCase())))
                        .filter(p -> (parametros.email() == null || 
                                      p.getEmail().toLowerCase().contains(parametros.email().toLowerCase())))
                        .collect(Collectors.toList());
                
                return profesores.stream()
                        .map(DTOProfesor::new)
                        .toList();
            } else {
                return obtenerProfesores();
            }
        }
    }

    // ===== MÉTODOS DE CREACIÓN Y ACTUALIZACIÓN =====

    /**
     * Crea un nuevo profesor
     * @param peticion Datos del profesor a crear
     * @return DTOProfesor con los datos del profesor creado
     * @throws IllegalArgumentException si ya existe un profesor con el mismo usuario, email o DNI
     */
    public DTOProfesor crearProfesor(DTOPeticionRegistroProfesor peticion) {
        // Security check: Only ADMIN can create professors
        if (!securityUtils.hasRole("ADMIN")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para crear profesores");
        }
        
        // Validar que no existan duplicados
        if (repositorioProfesor.findByUsername(peticion.username()).isPresent()) {
            ExceptionUtils.throwValidationError("Ya existe un profesor con el usuario: " + peticion.username());
        }
        
        if (repositorioProfesor.findByEmail(peticion.email()).isPresent()) {
            ExceptionUtils.throwValidationError("Ya existe un profesor con el email: " + peticion.email());
        }
        
        if (repositorioProfesor.findByDni(peticion.dni()).isPresent()) {
            ExceptionUtils.throwValidationError("Ya existe un profesor con el DNI: " + peticion.dni());
        }

        // Crear el profesor
        Profesor profesor = new Profesor(
            peticion.username(),
            servicioCachePassword.encodePassword(peticion.password()),
            peticion.firstName(),
            peticion.lastName(),
            peticion.dni(),
            peticion.email(),
            peticion.phoneNumber()
        );

        Profesor profesorGuardado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorGuardado);
    }

    /**
     * Actualiza los datos de un profesor
     * @param id ID del profesor a actualizar
     * @param dtoParcial Datos a actualizar
     * @return DTOProfesor con los datos actualizados
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     * @throws IllegalArgumentException si se intenta actualizar a un valor duplicado
     */
    public DTOProfesor actualizarProfesor(Long id, DTOActualizacionProfesor dtoParcial) {
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);

        // Security check: Only ADMIN, or the professor themselves can update professor data
        if (!securityUtils.hasRole("ADMIN") && !profesor.getId().equals(securityUtils.getCurrentUserId())) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para actualizar los datos de otros profesores");
        }

        // Actualizar campos no nulos
        if (dtoParcial.firstName() != null) {
            profesor.setFirstName(dtoParcial.firstName());
        }
        
        if (dtoParcial.lastName() != null) {
            profesor.setLastName(dtoParcial.lastName());
        }
        
        if (dtoParcial.email() != null) {
            // Verificar que no exista otro profesor con ese email
            if (!profesor.getEmail().equals(dtoParcial.email()) && 
                repositorioProfesor.findByEmail(dtoParcial.email()).isPresent()) {
                ExceptionUtils.throwValidationError("Ya existe un profesor con el email: " + dtoParcial.email());
            }
            profesor.setEmail(dtoParcial.email());
        }
        
        if (dtoParcial.dni() != null) {
            // Verificar que no exista otro profesor con ese DNI
            if (!profesor.getDni().equals(dtoParcial.dni()) && 
                repositorioProfesor.findByDni(dtoParcial.dni()).isPresent()) {
                ExceptionUtils.throwValidationError("Ya existe un profesor con el DNI: " + dtoParcial.dni());
            }
            profesor.setDni(dtoParcial.dni());
        }
        
        if (dtoParcial.phoneNumber() != null) {
            profesor.setPhoneNumber(dtoParcial.phoneNumber());
        }

        // Handle enabled status update (only ADMIN can change enabled status)
        if (dtoParcial.enabled() != null) {
            if (!securityUtils.hasRole("ADMIN")) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para cambiar el estado de habilitación de profesores");
            }
            profesor.setEnabled(dtoParcial.enabled());
        }

        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    /**
     * Habilita o deshabilita un profesor
     * @param id ID del profesor
     * @param habilitar true para habilitar, false para deshabilitar
     * @return DTOProfesor actualizado
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public DTOProfesor habilitarDeshabilitarProfesor(Long id, boolean habilitar) {
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);
        
        // Security check: Only ADMIN can enable/disable professors
        if (!securityUtils.hasRole("ADMIN")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para habilitar/deshabilitar profesores");
        }
        
        profesor.setEnabled(habilitar);
        Profesor profesorActualizado = repositorioProfesor.save(profesor);
        return new DTOProfesor(profesorActualizado);
    }

    /**
     * Habilita o deshabilita un profesor (alias para compatibilidad con tests)
     * @param profesorId ID del profesor
     * @param habilitado true para habilitar, false para deshabilitar
     * @return DTOProfesor actualizado
     */
    public DTOProfesor cambiarEstadoProfesor(Long profesorId, boolean habilitado) {
        return habilitarDeshabilitarProfesor(profesorId, habilitado);
    }

    /**
     * Borra un profesor por su ID
     * @param id ID del profesor
     * @return true si el profesor fue borrado correctamente
     * @throws EntidadNoEncontradaException si no se encuentra el profesor
     */
    public boolean borrarProfesorPorId(Long id) {
        // Verificar que el profesor existe antes de intentar borrarlo
        Profesor profesor = repositorioProfesor.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", id);

        // Security check: Only ADMIN can delete professors
        if (!securityUtils.hasRole("ADMIN")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para borrar profesores");
        }

        repositorioProfesor.deleteById(id);
        return true;
    }

    // ===== MÉTODOS DE GESTIÓN DE CLASES =====

    /**
     * Asigna una clase a un profesor
     * @param profesorId ID del profesor
     * @param claseId ID de la clase (como String)
     * @return DTOProfesor actualizado
     */
    public DTOProfesor asignarClase(Long profesorId, String claseId) {
        try {
            Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
            ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
            
            // Security check: Only ADMIN, or the professor themselves can assign classes
            if (!securityUtils.hasRole("ADMIN") && !profesor.getId().equals(securityUtils.getCurrentUserId())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para asignar clases a otros profesores");
            }

            Long claseIdLong = Long.parseLong(claseId);
            Clase clase = repositorioClase.findById(claseIdLong).orElse(null);
            ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);

            // Verificar si el profesor ya tiene asignada la clase
            if (profesor.imparteClasePorId(claseIdLong)) {
                throw new IllegalArgumentException("El profesor ya tiene asignada esta clase.");
            }
            
            // Asignar la clase al profesor
            profesor.agregarClase(clase);
            
            // Asignar el profesor a la clase
            clase.agregarProfesor(profesor);
            
            // Guardar los cambios
            repositorioClase.save(clase);
            Profesor profesorActualizado = repositorioProfesor.save(profesor);
            
            return new DTOProfesor(profesorActualizado);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de clase debe ser un número válido: " + e.getMessage());
        }
    }

    /**
     * Remueve una clase de un profesor
     * @param profesorId ID del profesor
     * @param claseId ID de la clase (como String)
     * @return DTOProfesor actualizado
     */
    public DTOProfesor removerClase(Long profesorId, String claseId) {
        try {
            Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
            ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
            
            // Security check: Only ADMIN, or the professor themselves can remove classes
            if (!securityUtils.hasRole("ADMIN") && !profesor.getId().equals(securityUtils.getCurrentUserId())) {
                ExceptionUtils.throwAccessDenied("No tienes permisos para remover clases a otros profesores");
            }

            Long claseIdLong = Long.parseLong(claseId);
            Clase clase = repositorioClase.findById(claseIdLong).orElse(null);
            ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);

            // Verificar si el profesor tiene asignada la clase
            if (!profesor.imparteClasePorId(claseIdLong)) {
                throw new IllegalArgumentException("El profesor no tiene asignada esta clase.");
            }
            
            // Remover la clase del profesor
            profesor.removerClase(clase);
            
            // Remover el profesor de la clase
            clase.removerProfesor(profesor);
            
            // Guardar los cambios
            repositorioClase.save(clase);
            Profesor profesorActualizado = repositorioProfesor.save(profesor);
            
            return new DTOProfesor(profesorActualizado);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de clase debe ser un número válido: " + e.getMessage());
        }
    }

    /**
     * Cuenta el número de clases asignadas a un profesor
     * @param profesorId ID del profesor
     * @return Número de clases
     */
    public Integer contarClasesProfesor(Long profesorId) {
        Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
                
        return profesor.getNumeroClases();
    }
    
    /**
     * Obtiene profesores por clase
     * @param claseId ID de la clase
     * @return Lista de profesores que imparten la clase
     */
    public List<DTOProfesor> obtenerProfesoresPorClase(String claseId) {
        List<Profesor> profesoresDeClase = repositorioProfesor.findByClaseId(Long.parseLong(claseId));
        return profesoresDeClase.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene profesores sin clases asignadas
     * @return Lista de profesores sin clases
     */
    public List<DTOProfesor> obtenerProfesoresSinClases() {
        List<Profesor> profesoresSinClases = repositorioProfesor.findProfesoresSinClases();
        return profesoresSinClases.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las clases asignadas a un profesor
     * @param profesorId ID del profesor
     * @return Lista de clases
     */
    public List<DTOClase> obtenerClasesPorProfesor(Long profesorId) {
        // Verificar que el profesor existe
        Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
                
        // Obtener las clases del profesor
        return repositorioClase.findByProfesorId(profesorId)
                .stream()
                .map(DTOClase::new)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un profesor imparte una clase específica
     * @param profesorId ID del profesor
     * @param claseId ID de la clase
     * @return true si imparte la clase, false en caso contrario
     */
    public boolean imparteClase(Long profesorId, String claseId) {
        Profesor profesor = repositorioProfesor.findById(profesorId).orElse(null);
        ExceptionUtils.throwIfNotFound(profesor, "Profesor", "ID", profesorId);
                
        return profesor.imparteClasePorId(Long.parseLong(claseId));
    }

    // ===== MÉTODOS CON PAGINACIÓN =====

    /**
     * Obtiene todos los profesores con paginación
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar (por defecto: id)
     * @param sortDirection dirección del ordenamiento (por defecto: ASC)
     * @return DTORespuestaPaginada con los profesores y metadatos de paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOProfesor> obtenerProfesoresPaginados(
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
        
        // Implementar paginación manualmente
        List<Profesor> allProfesores = repositorioProfesor.findAll(Sort.by(direction, sortBy));
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allProfesores.size());
        
        if (start > end) {
            start = 0;
            end = 0;
        }
        
        List<Profesor> pageContent = allProfesores.subList(start, end);
        Page<Profesor> pageProfesores = new PageImpl<>(pageContent, pageable, allProfesores.size());
        
        // Convertir a DTOs
        Page<DTOProfesor> pageDTOs = pageProfesores.map(DTOProfesor::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Busca profesores por parámetros con paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOProfesor> buscarProfesoresPorParametrosPaginados(
            DTOParametrosBusquedaProfesor parametros, int page, int size, String sortBy, String sortDirection) {
        
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
        Page<Profesor> pageProfesores;
        
        // Enhanced search logic with "q" parameter support
        if (parametros.hasGeneralSearch()) {
            if (parametros.hasSpecificFilters()) {
                // Use combined search (general + specific filters)
                pageProfesores = repositorioProfesor.findByGeneralAndSpecificFilters(
                    parametros.q(),
                    parametros.firstName(),
                    parametros.lastName(),
                    parametros.email(),
                    parametros.username(),
                    parametros.dni(),
                    parametros.enabled(),
                    pageable
                );
            } else {
                // Use only general search
                pageProfesores = repositorioProfesor.findByGeneralSearch(parametros.q(), pageable);
            }
        } else {
            // Use existing specific search logic
            if (parametros.hasSpecificFilters()) {
                // Filtrar profesores según parámetros
                List<Profesor> profesoresFiltrados;
                
                // Si todos los parámetros son nulos, obtener todos
                if (parametros.firstName() == null && parametros.lastName() == null && 
                    parametros.dni() == null && parametros.email() == null) {
                    profesoresFiltrados = repositorioProfesor.findAll(Sort.by(direction, sortBy));
                } else {
                    // Filtrar según parámetros
                    profesoresFiltrados = repositorioProfesor.findAll(Sort.by(direction, sortBy))
                        .stream()
                        .filter(p -> (parametros.firstName() == null || 
                                    p.getFirstName().toLowerCase().contains(parametros.firstName().toLowerCase())))
                        .filter(p -> (parametros.lastName() == null || 
                                    p.getLastName().toLowerCase().contains(parametros.lastName().toLowerCase())))
                        .filter(p -> (parametros.dni() == null || 
                                    p.getDni().toLowerCase().contains(parametros.dni().toLowerCase())))
                        .filter(p -> (parametros.email() == null || 
                                    p.getEmail().toLowerCase().contains(parametros.email().toLowerCase())))
                        .collect(Collectors.toList());
                }
                
                // Aplicar paginación
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), profesoresFiltrados.size());
                
                if (start > end) {
                    start = 0;
                    end = 0;
                }
                
                List<Profesor> pageContent = profesoresFiltrados.subList(start, end);
                pageProfesores = new PageImpl<>(pageContent, pageable, profesoresFiltrados.size());
            } else {
                // Implementar paginación manualmente
                List<Profesor> allProfesores = repositorioProfesor.findAll(Sort.by(direction, sortBy));
                
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), allProfesores.size());
                
                if (start > end) {
                    start = 0;
                    end = 0;
                }
                
                List<Profesor> pageContent = allProfesores.subList(start, end);
                pageProfesores = new PageImpl<>(pageContent, pageable, allProfesores.size());
            }
        }
        
        // Convertir a DTOs
        Page<DTOProfesor> pageDTOs = pageProfesores.map(DTOProfesor::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }
    
    /**
     * Obtiene profesores que imparten una clase específica con paginación
     * @param claseId ID de la clase (como String)
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar
     * @param sortDirection dirección de ordenamiento (ASC/DESC)
     * @return DTORespuestaPaginada con los profesores que imparten la clase
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOProfesor> obtenerProfesoresPorClasePaginados(
            String claseId, int page, int size, String sortBy, String sortDirection) {
        
        try {
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
            
            // Verificar que la clase existe
            Long claseIdLong = Long.parseLong(claseId);
            Clase clase = repositorioClase.findById(claseIdLong).orElse(null);
            ExceptionUtils.throwIfNotFound(clase, "Clase", "ID", claseId);
            
            // Filtrar profesores por clase usando JPA relationships
            List<Profesor> profesoresDeClase = repositorioProfesor.findAll(Sort.by(direction, sortBy))
                .stream()
                .filter(p -> p.imparteClasePorId(claseIdLong))
                .collect(Collectors.toList());
            
            // Aplicar paginación
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), profesoresDeClase.size());
            
            if (start > end) {
                start = 0;
                end = 0;
            }
            
            List<Profesor> pageContent = profesoresDeClase.subList(start, end);
            Page<Profesor> pageProfesores = new PageImpl<>(pageContent, pageable, profesoresDeClase.size());
            
            // Convertir a DTOs
            Page<DTOProfesor> pageDTOs = pageProfesores.map(DTOProfesor::new);
            
            return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de clase debe ser un número válido: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene todos los profesores habilitados
     * @return Lista de profesores habilitados
     */
    public List<DTOProfesor> obtenerProfesoresHabilitados() {
        List<Profesor> profesoresHabilitados = repositorioProfesor.findByEnabledTrue();
        return profesoresHabilitados.stream()
                .map(DTOProfesor::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los profesores habilitados con paginación
     * @param page número de página (0-indexed)
     * @param size tamaño de página
     * @param sortBy campo por el que ordenar (por defecto: id)
     * @param sortDirection dirección del ordenamiento (por defecto: ASC)
     * @return DTORespuestaPaginada con los profesores habilitados y metadatos de paginación
     */
    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOProfesor> obtenerProfesoresHabilitadosPaginados(
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
        Page<Profesor> pageProfesores = repositorioProfesor.findByEnabledTrue(pageable);
        Page<DTOProfesor> pageDTOs = pageProfesores.map(DTOProfesor::new);
        
        return DTORespuestaPaginada.fromPage(pageDTOs, sortBy, sortDirection);
    }

    // ===== MÉTODOS DE ESTADÍSTICAS =====
    
    /**
     * Cuenta profesores habilitados
     * @return Número de profesores habilitados
     */
    public long contarProfesoresHabilitados() {
        return repositorioProfesor.countByEnabledTrue();
    }
    
    /**
     * Cuenta profesores deshabilitados
     * @return Número de profesores deshabilitados
     */
    public long contarProfesoresDeshabilitados() {
        return repositorioProfesor.countByEnabledFalse();
    }
    
    /**
     * Cuenta el número total de profesores
     */
    public long contarTotalProfesores() {
        return repositorioProfesor.count();
    }
}