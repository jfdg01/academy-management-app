package app.repositorios;

import app.entidades.Clase;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repositorio para la entidad Clase
 * Proporciona operaciones de acceso a datos para clases (Talleres y Cursos)
 * Usa Spring Data JPA method naming conventions para evitar problemas de bytea
 */
@Repository
public interface RepositorioClase extends JpaRepository<Clase, Long> {
    
    /**
     * Busca una clase por su título
     * @param title Título de la clase
     * @return Optional<Clase>
     */
    Optional<Clase> findByTitle(String title);
    
    /**
     * Busca clases por título (contiene, case-sensitive)
     * @param title Título a buscar
     * @return Lista de clases
     */
    List<Clase> findByTitleContaining(String title);
    
    /**
     * Busca clases por título (contiene, case-insensitive)
     * @param title Título a buscar
     * @return Lista de clases
     */
    List<Clase> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Busca clases por título (contiene, case-sensitive) con paginación
     * @param title Título a buscar
     * @param pageable Parámetros de paginación
     * @return Página de clases
     */
    Page<Clase> findByTitleContaining(String title, Pageable pageable);
    
    /**
     * Busca clases por descripción (contiene, case-sensitive)
     * @param description Descripción a buscar
     * @return Lista de clases
     */
    List<Clase> findByDescriptionContaining(String description);
    
    /**
     * Busca clases por descripción (contiene, case-insensitive)
     * @param description Descripción a buscar
     * @return Lista de clases
     */
    List<Clase> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * Busca clases por descripción (contiene, case-sensitive) con paginación
     * @param description Descripción a buscar
     * @param pageable Parámetros de paginación
     * @return Página de clases
     */
    Page<Clase> findByDescriptionContaining(String description, Pageable pageable);
    
    /**
     * Busca clases por título o descripción (contiene, case-sensitive) con paginación
     * @param title Título a buscar
     * @param description Descripción a buscar
     * @param pageable Parámetros de paginación
     * @return Página de clases
     */
    Page<Clase> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);
    
    /**
     * Busca clases por título o descripción (contiene, case-insensitive) con paginación
     * @param title Título a buscar
     * @param description Descripción a buscar
     * @param pageable Parámetros de paginación
     * @return Página de clases
     */
    Page<Clase> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);
    
    /**
     * Busca clases por presencialidad
     * @param format Tipo de presencialidad
     * @return Lista de clases
     */
    List<Clase> findByFormat(EPresencialidad format);
    
    /**
     * Busca clases por nivel
     * @param difficulty Nivel de la clase
     * @return Lista de clases
     */
    List<Clase> findByDifficulty(EDificultad difficulty);
    
    /**
     * Busca clases por rango de precio
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de clases
     */
    List<Clase> findByPriceBetween(BigDecimal precioMin, BigDecimal precioMax);
    
    /**
     * Busca clases con precio menor o igual al especificado
     * @param precio Precio máximo
     * @return Lista de clases
     */
    List<Clase> findByPriceLessThanEqual(BigDecimal precio);
    
    /**
     * Busca clases con precio mayor o igual al especificado
     * @param precio Precio mínimo
     * @return Lista de clases
     */
    List<Clase> findByPriceGreaterThanEqual(BigDecimal precio);
    
    /**
     * Obtiene todas las clases ordenadas por ID
     * @return Lista de clases ordenada
     */
    List<Clase> findAllByOrderByIdAsc();
    
    /**
     * Obtiene todas las clases ordenadas por ID (alias para compatibilidad)
     * @return Lista de clases ordenada
     */
    default List<Clase> findAllOrderedById() {
        return findAllByOrderByIdAsc();
    }
    
    /**
     * Obtiene todas las clases ordenadas por precio ascendente
     * @return Lista de clases ordenada por precio
     */
    List<Clase> findAllByOrderByPriceAsc();
    
    /**
     * Obtiene todas las clases ordenadas por título
     * @return Lista de clases ordenada por título
     */
    List<Clase> findAllByOrderByTitleAsc();
    
    /**
     * Busca clases que tienen un alumno específico inscrito
     * @param alumnoId ID del alumno
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c JOIN c.students s WHERE s.id = :alumnoId")
    List<Clase> findByAlumnoId(@Param("alumnoId") Long alumnoId);
    
    /**
     * Busca clases que tienen un profesor específico asignado
     * @param profesorId ID del profesor
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c JOIN c.teachers t WHERE t.id = :profesorId")
    List<Clase> findByProfesorId(@Param("profesorId") Long profesorId);
    
    /**
     * Busca clases que tienen un ejercicio específico
     * @param ejercicioId ID del ejercicio
     * @return Lista de clases
     */
    @Query("SELECT c FROM Clase c JOIN c.exercises e WHERE e.id = :ejercicioId")
    List<Clase> findByEjercicioId(@Param("ejercicioId") Long ejercicioId);
    
    /**
     * Cuenta el número de alumnos inscritos en una clase
     * @param claseId ID de la clase
     * @return Número de alumnos
     */
    @Query("SELECT COUNT(s) FROM Clase c JOIN c.students s WHERE c.id = :claseId")
    Integer countAlumnosByClaseId(@Param("claseId") Long claseId);
    
    /**
     * Cuenta el número de profesores asignados a una clase
     * @param claseId ID de la clase
     * @return Número de profesores
     */
    @Query("SELECT COUNT(t) FROM Clase c JOIN c.teachers t WHERE c.id = :claseId")
    Integer countProfesoresByClaseId(@Param("claseId") Long claseId);
    
    /**
     * Busca clases que no tienen alumnos inscritos
     * @return Lista de clases sin alumnos
     */
    @Query("SELECT c FROM Clase c WHERE SIZE(c.students) = 0")
    List<Clase> findClasesSinAlumnos();
    
    /**
     * Busca clases que no tienen profesores asignados
     * @return Lista de clases sin profesores
     */
    @Query("SELECT c FROM Clase c WHERE SIZE(c.teachers) = 0")
    List<Clase> findClasesSinProfesores();
    
    /**
     * Busca clases con búsqueda general (título o descripción)
     * @param searchTerm Término de búsqueda
     * @param pageable Parámetros de paginación
     * @return Página de clases
     */
    @Query("SELECT c FROM Clase c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Clase> findByGeneralSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Busca clases con filtros generales y específicos
     * @param searchTerm Término de búsqueda general
     * @param title Título específico
     * @param description Descripción específica
     * @param format Tipo de presencialidad
     * @param difficulty Nivel de dificultad
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @param pageable Parámetros de paginación
     * @return Página de clases
     */
    @Query("SELECT c FROM Clase c WHERE " +
           "(:searchTerm IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:title IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:description IS NULL OR LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "(:format IS NULL OR c.format = :format) AND " +
           "(:difficulty IS NULL OR c.difficulty = :difficulty) AND " +
           "(:precioMin IS NULL OR c.price >= :precioMin) AND " +
           "(:precioMax IS NULL OR c.price <= :precioMax)")
    Page<Clase> findByGeneralAndSpecificFilters(
            @Param("searchTerm") String searchTerm,
            @Param("title") String title,
            @Param("description") String description,
            @Param("format") EPresencialidad format,
            @Param("difficulty") EDificultad difficulty,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            Pageable pageable);
    
    /**
     * Busca una clase por ID con todas las relaciones cargadas
     * @param id ID de la clase
     * @return Optional<Clase> con todas las relaciones
     */
    @Query("SELECT c FROM Clase c LEFT JOIN FETCH c.students LEFT JOIN FETCH c.teachers LEFT JOIN FETCH c.exercises LEFT JOIN FETCH c.material WHERE c.id = :id")
    Optional<Clase> findByIdWithAllRelationships(@Param("id") Long id);
}