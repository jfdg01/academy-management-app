package app.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import app.entidades.Alumno;

/**
 * Repositorio para la entidad Alumno
 * Proporciona operaciones de acceso a datos para alumnos
 */
@Repository
public interface RepositorioAlumno extends JpaRepository<Alumno, Long> {
    
    /**
     * Busca un alumno por su nombre de usuario
     * @param username Nombre de usuario
     * @return Optional<Alumno>
     */
    @Query("SELECT a FROM Alumno a WHERE a.username = :username")
    Optional<Alumno> findByUsername(@Param("username") String username);
    
    /**
     * Busca un alumno por su email
     * @param email Email del alumno
     * @return Optional<Alumno>
     */
    @Query("SELECT a FROM Alumno a WHERE a.email = :email")
    Optional<Alumno> findByEmail(@Param("email") String email);
    
    /**
     * Busca un alumno por su DNI
     * @param dni DNI del alumno
     * @return Optional<Alumno>
     */
    @Query("SELECT a FROM Alumno a WHERE a.dni = :dni")
    Optional<Alumno> findByDni(@Param("dni") String dni);
    
    /**
     * Verifica si existe un alumno con el nombre de usuario dado
     * @param username Nombre de usuario
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Alumno a WHERE a.username = :username")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Verifica si existe un alumno con el email dado
     * @param email Email
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Alumno a WHERE a.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Verifica si existe un alumno con el DNI dado
     * @param dni DNI
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Alumno a WHERE a.dni = :dni")
    boolean existsByDni(@Param("dni") String dni);
    
    /**
     * Busca alumnos por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de alumnos
     */
    @Query("SELECT a FROM Alumno a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY a.id")
    List<Alumno> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    /**
     * Busca alumnos por apellidos (contiene, ignorando mayúsculas)
     * @param apellidos Apellidos a buscar
     * @return Lista de alumnos
     */
    @Query("SELECT a FROM Alumno a WHERE LOWER(a.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%')) ORDER BY a.id")
    List<Alumno> findByApellidosContainingIgnoreCase(@Param("apellidos") String apellidos);
    
    /**
     * Cuenta alumnos por estado de matriculación
     * @param matriculado Estado de matriculación
     * @return Número de alumnos
     */
    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.enrolled = :matriculado")
    long countByMatriculado(@Param("matriculado") boolean matriculado);
    
    /**
     * Obtiene todos los alumnos con paginación
     * @param pageable Configuración de paginación
     * @return Página de alumnos
     */
    @Query("SELECT a FROM Alumno a")
    Page<Alumno> findAllPaged(Pageable pageable);
    
    /**
     * Busca alumnos por múltiples filtros con paginación
     * @param nombre Filtro por nombre
     * @param apellidos Filtro por apellidos
     * @param dni Filtro por DNI
     * @param email Filtro por email
     * @param matriculado Filtro por estado de matriculación
     * @param pageable Configuración de paginación
     * @return Página de alumnos
     */
    @Query("SELECT a FROM Alumno a WHERE " +
           "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :nombre, '%')) OR :nombre IS NULL) AND " +
           "(LOWER(a.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%')) OR :apellidos IS NULL) AND " +
           "(LOWER(a.dni) LIKE LOWER(CONCAT('%', :dni, '%')) OR :dni IS NULL) AND " +
           "(LOWER(a.email) LIKE LOWER(CONCAT('%', :email, '%')) OR :email IS NULL) AND " +
           "(a.enrolled = :matriculado OR :matriculado IS NULL)")
    Page<Alumno> findByFiltrosPaged(
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("dni") String dni,
        @Param("email") String email,
        @Param("matriculado") Boolean matriculado,
        Pageable pageable
    );
    
    /**
     * Busca alumnos por estado de matriculación con paginación
     * @param matriculado Estado de matriculación
     * @param pageable Configuración de paginación
     * @return Página de alumnos
     */
    @Query("SELECT a FROM Alumno a WHERE a.enrolled = :matriculado")
    Page<Alumno> findByMatriculadoPaged(@Param("matriculado") boolean matriculado, Pageable pageable);
    
    /**
     * Búsqueda general en múltiples campos usando el parámetro "q"
     * Busca en nombre, apellidos, DNI y email
     * @param searchTerm Término de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de alumnos
     */
    @Query("SELECT a FROM Alumno a WHERE " +
           "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.dni) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Alumno> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Búsqueda combinada con término general y filtros específicos
     * @param searchTerm Término de búsqueda general
     * @param nombre Filtro por nombre
     * @param apellidos Filtro por apellidos
     * @param dni Filtro por DNI
     * @param email Filtro por email
     * @param matriculado Filtro por estado de matriculación
     * @param pageable Configuración de paginación
     * @return Página de alumnos
     */
    @Query("SELECT a FROM Alumno a WHERE " +
           "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.dni) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR :searchTerm IS NULL) AND " +
           "(LOWER(a.firstName) LIKE LOWER(CONCAT('%', :nombre, '%')) OR :nombre IS NULL) AND " +
           "(LOWER(a.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%')) OR :apellidos IS NULL) AND " +
           "(LOWER(a.dni) LIKE LOWER(CONCAT('%', :dni, '%')) OR :dni IS NULL) AND " +
           "(LOWER(a.email) LIKE LOWER(CONCAT('%', :email, '%')) OR :email IS NULL) AND " +
           "(a.enrolled = :matriculado OR :matriculado IS NULL)")
    Page<Alumno> findByGeneralAndSpecificFilters(
        @Param("searchTerm") String searchTerm,
        @Param("nombre") String nombre,
        @Param("apellidos") String apellidos,
        @Param("dni") String dni,
        @Param("email") String email,
        @Param("matriculado") Boolean matriculado,
        Pageable pageable
    );

    /**
     * Busca alumnos que están inscritos en una clase específica
     * @param claseId ID de la clase
     * @return Lista de alumnos inscritos en la clase
     */
    @Query("SELECT a FROM Alumno a JOIN a.classes c WHERE c.id = :claseId")
    List<Alumno> findByClaseId(@Param("claseId") Long claseId);

    /**
     * Busca alumnos que no están inscritos en ninguna clase
     * @return Lista de alumnos sin clases
     */
    @Query("SELECT a FROM Alumno a WHERE SIZE(a.classes) = 0")
    List<Alumno> findAlumnosSinClases();

    /**
     * Busca un alumno por ID con todas sus relaciones cargadas
     * @param alumnoId ID del alumno
     * @return Optional<Alumno> con relaciones cargadas
     */
    @EntityGraph(value = "Alumno.withClassesAndPayments")
    @Query("SELECT a FROM Alumno a WHERE a.id = :alumnoId")
    Optional<Alumno> findByIdWithRelationships(@Param("alumnoId") Long alumnoId);

    /**
     * Cuenta el número de clases en las que está inscrito un alumno
     * @param alumnoId ID del alumno
     * @return Número de clases
     */
    @Query("SELECT COUNT(c) FROM Alumno a JOIN a.classes c WHERE a.id = :alumnoId")
    Integer countClasesByAlumnoId(@Param("alumnoId") Long alumnoId);

    /**
     * Busca alumnos que están inscritos en múltiples clases
     * @param minClases Número mínimo de clases
     * @return Lista de alumnos con múltiples clases
     */
    @Query("SELECT a FROM Alumno a WHERE SIZE(a.classes) >= :minClases")
    List<Alumno> findAlumnosConMultipleClases(@Param("minClases") Integer minClases);

    // ========== ENTITY GRAPH METHODS ==========

    /**
     * Busca un alumno por ID con clases cargadas usando EntityGraph
     * @param alumnoId ID del alumno
     * @return Optional<Alumno> con clases cargadas
     */
    @EntityGraph(value = "Alumno.withClasses")
    @Query("SELECT a FROM Alumno a WHERE a.id = :alumnoId")
    Optional<Alumno> findByIdWithClasses(@Param("alumnoId") Long alumnoId);

    /**
     * Busca un alumno por ID con todas sus relaciones cargadas usando EntityGraph
     * @param alumnoId ID del alumno
     * @return Optional<Alumno> con todas las relaciones cargadas
     */
    @EntityGraph(value = "Alumno.withClassesAndPayments")
    @Query("SELECT a FROM Alumno a WHERE a.id = :alumnoId")
    Optional<Alumno> findByIdWithAllRelationships(@Param("alumnoId") Long alumnoId);

    /**
     * Busca alumnos por clase con relaciones cargadas usando EntityGraph
     * @param claseId ID de la clase
     * @return Lista de alumnos con relaciones cargadas
     */
    @EntityGraph(value = "Alumno.withClasses")
    @Query("SELECT a FROM Alumno a JOIN a.classes c WHERE c.id = :claseId")
    List<Alumno> findByClaseIdWithClasses(@Param("claseId") Long claseId);

    /**
     * Busca alumnos matriculados con clases cargadas usando EntityGraph
     * @param matriculado Estado de matriculación
     * @return Lista de alumnos matriculados con clases cargadas
     */
    @EntityGraph(value = "Alumno.withClasses")
    @Query("SELECT a FROM Alumno a WHERE a.enrolled = :matriculado")
    List<Alumno> findByMatriculadoWithClasses(@Param("matriculado") boolean matriculado);

    /**
     * Busca alumnos por nombre con clases cargadas usando EntityGraph
     * @param nombre Nombre a buscar
     * @return Lista de alumnos con clases cargadas
     */
    @EntityGraph(value = "Alumno.withClasses")
    @Query("SELECT a FROM Alumno a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Alumno> findByNombreContainingIgnoreCaseWithClasses(@Param("nombre") String nombre);

    /**
     * Busca alumnos por apellidos con clases cargadas usando EntityGraph
     * @param apellidos Apellidos a buscar
     * @return Lista de alumnos con clases cargadas
     */
    @EntityGraph(value = "Alumno.withClasses")
    @Query("SELECT a FROM Alumno a WHERE LOWER(a.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%'))")
    List<Alumno> findByApellidosContainingIgnoreCaseWithClasses(@Param("apellidos") String apellidos);

    /**
     * Obtiene todos los alumnos con clases cargadas usando EntityGraph
     * @return Lista de alumnos con clases cargadas
     */
    @EntityGraph(value = "Alumno.withClasses")
    @Query("SELECT a FROM Alumno a")
    List<Alumno> findAllWithClasses();

    /**
     * Obtiene todos los alumnos con todas sus relaciones cargadas usando EntityGraph
     * @return Lista de alumnos con todas las relaciones cargadas
     */
    @EntityGraph(value = "Alumno.withClassesAndPayments")
    @Query("SELECT a FROM Alumno a")
    List<Alumno> findAllWithAllRelationships();
}
