package app.repositorios;

import app.entidades.Profesor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Profesor
 * Proporciona operaciones de acceso a datos para profesores
 */
@Repository
public interface RepositorioProfesor extends JpaRepository<Profesor, Long> {
    
    /**
     * Busca un profesor por su nombre de usuario
     * @param usuario Nombre de usuario
     * @return Optional<Profesor>
     */
    @Query("SELECT p FROM Profesor p WHERE p.username = :username")
    Optional<Profesor> findByUsername(@Param("username") String username);
    
    /**
     * Busca un profesor por su email
     * @param email Email del profesor
     * @return Optional<Profesor>
     */
    @Query("SELECT p FROM Profesor p WHERE p.email = :email")
    Optional<Profesor> findByEmail(@Param("email") String email);
    
    /**
     * Busca un profesor por su DNI
     * @param dni DNI del profesor
     * @return Optional<Profesor>
     */
    @Query("SELECT p FROM Profesor p WHERE p.dni = :dni")
    Optional<Profesor> findByDni(@Param("dni") String dni);
    
    /**
     * Verifica si un profesor con el nombre de usuario dado existe
     * @param username Nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profesor p WHERE p.username = :username")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Verifica si un profesor con el email dado existe
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profesor p WHERE p.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Verifica si un profesor con el DNI dado existe
     * @param dni DNI a verificar
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profesor p WHERE p.dni = :dni")
    boolean existsByDni(@Param("dni") String dni);
    
    /**
     * Busca profesores por nombre (contiene, ignorando mayúsculas y acentos)
     * @param nombre Nombre a buscar
     * @return Lista de profesores
     */
    @Query("SELECT p FROM Profesor p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY p.id")
    List<Profesor> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    /**
     * Busca profesores por apellidos (contiene, ignorando mayúsculas y acentos)
     * @param apellidos Apellidos a buscar
     * @return Lista de profesores
     */
    @Query("SELECT p FROM Profesor p WHERE LOWER(p.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%')) ORDER BY p.id")
    List<Profesor> findByApellidosContainingIgnoreCase(@Param("apellidos") String apellidos);
    
    /**
     * Busca profesores que están habilitados
     * @return Lista de profesores habilitados
     */
    @Query("SELECT p FROM Profesor p WHERE p.enabled = true")
    List<Profesor> findByEnabledTrue();
    
    /**
     * Busca profesores habilitados con paginación
     * @param pageable Configuración de paginación
     * @return Página de profesores habilitados
     */
    @Query("SELECT p FROM Profesor p WHERE p.enabled = true")
    Page<Profesor> findByEnabledTrue(Pageable pageable);
    
    /**
     * Cuenta profesores habilitados
     * @return número de profesores habilitados
     */
    @Query("SELECT COUNT(p) FROM Profesor p WHERE p.enabled = true")
    long countByEnabledTrue();
    
    /**
     * Cuenta profesores deshabilitados
     * @return número de profesores deshabilitados
     */
    @Query("SELECT COUNT(p) FROM Profesor p WHERE p.enabled = false")
    long countByEnabledFalse();
    
    /**
     * Obtiene todos los profesores ordenados por ID
     * @return Lista de profesores ordenada
     */
    @Query("SELECT p FROM Profesor p ORDER BY p.id")
    List<Profesor> findAllOrderedById();
    
    /**
     * Obtiene todos los profesores con paginación
     * @param pageable Configuración de paginación
     * @return Página de profesores
     */
    @Query("SELECT p FROM Profesor p")
    Page<Profesor> findAllOrderedById(Pageable pageable);
    
    /**
     * General search across multiple fields using the "q" parameter
     * Searches in nombre, apellidos, email, usuario, and dni fields
     * @param searchTerm Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de profesores
     */
    @Query("SELECT p FROM Profesor p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.dni) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Profesor> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Combined search with general term and specific filters
     * @param searchTerm Término de búsqueda general
     * @param nombre Filtro por nombre
     * @param apellidos Filtro por apellidos
     * @param email Filtro por email
     * @param usuario Filtro por usuario
     * @param dni Filtro por DNI
     * @param habilitado Filtro por estado habilitado
     * @param pageable Configuración de paginación
     * @return Página de profesores
     */
    @Query("SELECT p FROM Profesor p WHERE " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.dni) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR :searchTerm IS NULL) AND " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :nombre, '%')) OR :nombre IS NULL) AND " +
           "(LOWER(p.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%')) OR :apellidos IS NULL) AND " +
           "(LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%')) OR :email IS NULL) AND " +
           "(LOWER(p.username) LIKE LOWER(CONCAT('%', :usuario, '%')) OR :usuario IS NULL) AND " +
           "(LOWER(p.dni) LIKE LOWER(CONCAT('%', :dni, '%')) OR :dni IS NULL) AND " +
           "(p.enabled = :habilitado OR :habilitado IS NULL)")
    Page<Profesor> findByGeneralAndSpecificFilters(
        @Param("searchTerm") String searchTerm,
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("email") String email,
        @Param("usuario") String usuario,
        @Param("dni") String dni,
        @Param("habilitado") Boolean habilitado,
        Pageable pageable
    );
    
    /**
     * Busca profesores que tienen una clase específica asignada
     * @param claseId ID de la clase
     * @return Lista de profesores
     */
    @Query("SELECT p FROM Profesor p JOIN p.classes c WHERE c.id = :claseId")
    List<Profesor> findByClaseId(@Param("claseId") Long claseId);
    
    /**
     * Cuenta el número de clases asignadas a un profesor
     * @param profesorId ID del profesor
     * @return Número de clases
     */
    @Query("SELECT COUNT(c) FROM Profesor p JOIN p.classes c WHERE p.id = :profesorId")
    Integer countClasesByProfesorId(@Param("profesorId") Long profesorId);
    
    /**
     * Busca profesores que no tienen clases asignadas
     * @return Lista de profesores sin clases
     */
    @Query("SELECT p FROM Profesor p WHERE SIZE(p.classes) = 0")
    List<Profesor> findProfesoresSinClases();
    
    /**
     * Busca profesores por múltiples filtros con soporte para texto insensible a acentos y mayúsculas
     * @param nombre Filtro por nombre
     * @param apellidos Filtro por apellidos
     * @param email Filtro por email
     * @param usuario Filtro por usuario
     * @param dni Filtro por DNI
     * @param habilitado Filtro por estado habilitado
     * @param claseId Filtro por clase asignada
     * @param sinClases Filtro para profesores sin clases
     * @return Lista de profesores que cumplen los criterios
     */
    @Query("SELECT DISTINCT p FROM Profesor p " +
           "WHERE " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :nombre, '%')) OR :nombre IS NULL) AND " +
           "(LOWER(p.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%')) OR :apellidos IS NULL) AND " +
           "(LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%')) OR :email IS NULL) AND " +
           "(LOWER(p.username) LIKE LOWER(CONCAT('%', :usuario, '%')) OR :usuario IS NULL) AND " +
           "(LOWER(p.dni) LIKE LOWER(CONCAT('%', :dni, '%')) OR :dni IS NULL) AND " +
           "(p.enabled = :habilitado OR :habilitado IS NULL) AND " +
           "(:claseId IS NULL OR EXISTS (SELECT 1 FROM p.classes c WHERE c.id = :claseId)) AND " +
           "(:sinClases IS NULL OR (:sinClases = true AND SIZE(p.classes) = 0) OR (:sinClases = false)) " +
           "ORDER BY p.id")
    List<Profesor> findByFiltros(
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("email") String email,
        @Param("usuario") String usuario,
        @Param("dni") String dni,
        @Param("habilitado") Boolean habilitado,
        @Param("claseId") Long claseId,
        @Param("sinClases") Boolean sinClases
    );
    
    /**
     * Busca profesores por múltiples filtros con soporte para paginación
     * @param nombre Filtro por nombre
     * @param apellidos Filtro por apellidos
     * @param email Filtro por email
     * @param usuario Filtro por usuario
     * @param dni Filtro por DNI
     * @param habilitado Filtro por estado habilitado
     * @param claseId Filtro por clase asignada
     * @param sinClases Filtro para profesores sin clases
     * @param pageable Configuración de paginación
     * @return Página de profesores que cumplen los criterios
     */
    @Query("SELECT DISTINCT p FROM Profesor p " +
           "WHERE " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :nombre, '%')) OR :nombre IS NULL) AND " +
           "(LOWER(p.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%')) OR :apellidos IS NULL) AND " +
           "(LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%')) OR :email IS NULL) AND " +
           "(LOWER(p.username) LIKE LOWER(CONCAT('%', :usuario, '%')) OR :usuario IS NULL) AND " +
           "(LOWER(p.dni) LIKE LOWER(CONCAT('%', :dni, '%')) OR :dni IS NULL) AND " +
           "(p.enabled = :habilitado OR :habilitado IS NULL) AND " +
           "(:claseId IS NULL OR EXISTS (SELECT 1 FROM p.classes c WHERE c.id = :claseId)) AND " +
           "(:sinClases IS NULL OR (:sinClases = true AND SIZE(p.classes) = 0) OR (:sinClases = false))")
    Page<Profesor> findByFiltrosPaginados(
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("email") String email,
        @Param("usuario") String usuario,
        @Param("dni") String dni,
        @Param("habilitado") Boolean habilitado,
        @Param("claseId") Long claseId,
        @Param("sinClases") Boolean sinClases,
        Pageable pageable
    );

    /**
     * Busca un profesor por ID con todas sus relaciones cargadas
     * @param profesorId ID del profesor
     * @return Optional<Profesor> con relaciones cargadas
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p WHERE p.id = :profesorId")
    Optional<Profesor> findByIdWithRelationships(@Param("profesorId") Long profesorId);

    /**
     * Busca profesores que tienen múltiples clases asignadas
     * @param minClases Número mínimo de clases
     * @return Lista de profesores con múltiples clases
     */
    @Query("SELECT p FROM Profesor p WHERE SIZE(p.classes) >= :minClases")
    List<Profesor> findProfesoresConMultipleClases(@Param("minClases") Integer minClases);

    // ========== ENTITY GRAPH METHODS ==========

    /**
     * Busca un profesor por ID con clases cargadas usando EntityGraph
     * @param profesorId ID del profesor
     * @return Optional<Profesor> con clases cargadas
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p WHERE p.id = :profesorId")
    Optional<Profesor> findByIdWithClasses(@Param("profesorId") Long profesorId);

    /**
     * Busca un profesor por ID con todas sus relaciones cargadas usando EntityGraph
     * @param profesorId ID del profesor
     * @return Optional<Profesor> con todas las relaciones cargadas
     */
    @EntityGraph(value = "Profesor.withAllRelationships")
    @Query("SELECT p FROM Profesor p WHERE p.id = :profesorId")
    Optional<Profesor> findByIdWithAllRelationships(@Param("profesorId") Long profesorId);

    /**
     * Busca profesores por clase con clases cargadas usando EntityGraph
     * @param claseId ID de la clase
     * @return Lista de profesores con clases cargadas
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p JOIN p.classes c WHERE c.id = :claseId")
    List<Profesor> findByClaseIdWithClasses(@Param("claseId") Long claseId);

    /**
     * Busca profesores habilitados con clases cargadas usando EntityGraph
     * @return Lista de profesores habilitados con clases cargadas
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p WHERE p.enabled = true")
    List<Profesor> findByEnabledTrueWithClasses();

    /**
     * Busca profesores por nombre con clases cargadas usando EntityGraph
     * @param nombre Nombre a buscar
     * @return Lista de profesores con clases cargadas
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Profesor> findByNombreContainingIgnoreCaseWithClasses(@Param("nombre") String nombre);

    /**
     * Busca profesores por apellidos con clases cargadas usando EntityGraph
     * @param apellidos Apellidos a buscar
     * @return Lista de profesores con clases cargadas
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p WHERE LOWER(p.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%'))")
    List<Profesor> findByApellidosContainingIgnoreCaseWithClasses(@Param("apellidos") String apellidos);

    /**
     * Busca profesores sin clases con clases cargadas usando EntityGraph
     * @return Lista de profesores sin clases con clases cargadas
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p WHERE SIZE(p.classes) = 0")
    List<Profesor> findProfesoresSinClasesWithClasses();

    /**
     * Busca profesores con múltiples clases con clases cargadas usando EntityGraph
     * @param minClases Número mínimo de clases
     * @return Lista de profesores con múltiples clases
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p WHERE SIZE(p.classes) >= :minClases")
    List<Profesor> findProfesoresConMultipleClasesWithClasses(@Param("minClases") Integer minClases);

    /**
     * Obtiene todos los profesores con clases cargadas usando EntityGraph
     * @return Lista de profesores con clases cargadas
     */
    @EntityGraph(value = "Profesor.withClasses")
    @Query("SELECT p FROM Profesor p")
    List<Profesor> findAllWithClasses();

    /**
     * Obtiene todos los profesores con todas sus relaciones cargadas usando EntityGraph
     * @return Lista de profesores con todas las relaciones cargadas
     */
    @EntityGraph(value = "Profesor.withAllRelationships")
    @Query("SELECT p FROM Profesor p")
    List<Profesor> findAllWithAllRelationships();
}
