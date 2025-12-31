package app.repositorios;

import app.entidades.Ejercicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Ejercicio
 * Proporciona operaciones de acceso a datos para ejercicios
 * Migrado completamente a Spring Data JPA para evitar problemas de bytea
 */
@Repository
public interface RepositorioEjercicio extends JpaRepository<Ejercicio, Long> {
    
    /**
     * Busca un ejercicio por su nombre (case-sensitive)
     * @param nombre Nombre del ejercicio
     * @return Optional<Ejercicio>
     */
    Optional<Ejercicio> findByName(String nombre);
    
    /**
     * Busca ejercicios por nombre (contiene, case-sensitive)
     * @param nombre Nombre a buscar
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByNameContaining(String nombre);
    
    /**
     * Busca ejercicios por nombre (contiene, case-sensitive) con paginación
     * @param nombre Nombre a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    Page<Ejercicio> findByNameContaining(String nombre, Pageable pageable);
    
    /**
     * Busca ejercicios por enunciado (contiene, case-sensitive)
     * @param enunciado Enunciado a buscar
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByStatementContaining(String enunciado);
    
    /**
     * Busca ejercicios por enunciado (contiene, case-sensitive) con paginación
     * @param enunciado Enunciado a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    Page<Ejercicio> findByStatementContaining(String enunciado, Pageable pageable);
    
    /**
     * Busca ejercicios por nombre o enunciado (contiene, case-sensitive) con paginación
     * @param nombre Nombre a buscar
     * @param enunciado Enunciado a buscar
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    Page<Ejercicio> findByNameContainingOrStatementContaining(String nombre, String enunciado, Pageable pageable);
    
    /**
     * Busca ejercicios por ID de clase
     * @param claseId ID de la clase
     * @return Lista de ejercicios de la clase
     */
    List<Ejercicio> findByClaseId(Long claseId);
    
    /**
     * Busca ejercicios por ID de clase con paginación
     * @param claseId ID de la clase
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios
     */
    Page<Ejercicio> findByClaseId(Long claseId, Pageable pageable);
    
    /**
     * Busca ejercicios por rango de fechas de inicio de plazo
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByStartDateBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca ejercicios por rango de fechas de fin de plazo
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de ejercicios
     */
    List<Ejercicio> findByEndDateBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Busca ejercicios que están actualmente en plazo
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios en plazo
     */
    List<Ejercicio> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime ahora, LocalDateTime ahora2);
    
    /**
     * Busca ejercicios que están actualmente en plazo con paginación
     * @param ahora Fecha y hora actual
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios en plazo
     */
    Page<Ejercicio> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime ahora, LocalDateTime ahora2, Pageable pageable);
    
    /**
     * Busca ejercicios que han vencido
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios vencidos
     */
    List<Ejercicio> findByEndDateBefore(LocalDateTime ahora);
    
    /**
     * Busca ejercicios que han vencido con paginación
     * @param ahora Fecha y hora actual
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios vencidos
     */
    Page<Ejercicio> findByEndDateBefore(LocalDateTime ahora, Pageable pageable);
    
    /**
     * Busca ejercicios que aún no han comenzado
     * @param ahora Fecha y hora actual
     * @return Lista de ejercicios futuros
     */
    List<Ejercicio> findByStartDateAfter(LocalDateTime ahora);
    
    /**
     * Busca ejercicios que aún no han comenzado con paginación
     * @param ahora Fecha y hora actual
     * @param pageable Parámetros de paginación
     * @return Página de ejercicios futuros
     */
    Page<Ejercicio> findByStartDateAfter(LocalDateTime ahora, Pageable pageable);
    
    /**
     * Busca ejercicios por nombre con clase cargada usando EntityGraph
     * @param nombre Nombre a buscar
     * @return Lista de ejercicios con clase cargada
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.name LIKE %:nombre%")
    @EntityGraph(value = "Ejercicio.withClase")
    List<Ejercicio> findByNameContainingWithClase(@Param("nombre") String nombre);
    
    /**
     * Busca ejercicios por enunciado con clase cargada usando EntityGraph
     * @param enunciado Enunciado a buscar
     * @return Lista de ejercicios con clase cargada
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.statement LIKE %:enunciado%")
    @EntityGraph(value = "Ejercicio.withClase")
    List<Ejercicio> findByStatementContainingWithClase(@Param("enunciado") String enunciado);
    
    /**
     * Obtiene todos los ejercicios con clase cargada usando EntityGraph
     * @return Lista de ejercicios con clase cargada
     */
    @Query("SELECT e FROM Ejercicio e")
    @EntityGraph(value = "Ejercicio.withClase")
    List<Ejercicio> findAllWithClase();
    
    /**
     * Obtiene todos los ejercicios con entregas cargadas usando EntityGraph
     * @return Lista de ejercicios con entregas cargadas
     */
    @Query("SELECT e FROM Ejercicio e")
    @EntityGraph(value = "Ejercicio.withEntregas")
    List<Ejercicio> findAllWithEntregas();
    
    /**
     * Obtiene todos los ejercicios con todas las relaciones cargadas usando EntityGraph
     * @return Lista de ejercicios con todas las relaciones cargadas
     */
    @Query("SELECT e FROM Ejercicio e")
    @EntityGraph(value = "Ejercicio.withAllRelationships")
    List<Ejercicio> findAllWithAllRelationships();
    
    /**
     * Busca ejercicios por nombre con entregas cargadas usando EntityGraph
     * @param nombre Nombre a buscar
     * @return Lista de ejercicios con entregas cargadas
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.name LIKE %:nombre%")
    @EntityGraph(value = "Ejercicio.withEntregas")
    List<Ejercicio> findByNameContainingWithEntregas(@Param("nombre") String nombre);
    
    /**
     * Busca ejercicios por enunciado con entregas cargadas usando EntityGraph
     * @param enunciado Enunciado a buscar
     * @return Lista de ejercicios con entregas cargadas
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.statement LIKE %:enunciado%")
    @EntityGraph(value = "Ejercicio.withEntregas")
    List<Ejercicio> findByStatementContainingWithEntregas(@Param("enunciado") String enunciado);
    
    /**
     * Busca ejercicios por ID de clase con entregas cargadas usando EntityGraph
     * @param claseId ID de la clase
     * @return Lista de ejercicios con entregas cargadas
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    @EntityGraph(value = "Ejercicio.withEntregas")
    List<Ejercicio> findByClaseIdWithEntregas(@Param("claseId") Long claseId);
    
    /**
     * Busca ejercicios por ID de clase con todas las relaciones cargadas usando EntityGraph
     * @param claseId ID de la clase
     * @return Lista de ejercicios con todas las relaciones cargadas
     */
    @Query("SELECT e FROM Ejercicio e WHERE e.clase.id = :claseId")
    @EntityGraph(value = "Ejercicio.withAllRelationships")
    List<Ejercicio> findByClaseIdWithAllRelationships(@Param("claseId") Long claseId);
    
    /**
     * Cuenta ejercicios por ID de clase
     * @param claseId ID de la clase
     * @return Número de ejercicios en la clase
     */
    long countByClaseId(Long claseId);
    
    /**
     * Cuenta ejercicios que están en plazo
     * @param ahora Fecha y hora actual
     * @return Número de ejercicios en plazo
     */
    long countByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime ahora, LocalDateTime ahora2);
    
    /**
     * Cuenta ejercicios que han vencido
     * @param ahora Fecha y hora actual
     * @return Número de ejercicios vencidos
     */
    long countByEndDateBefore(LocalDateTime ahora);
    
    /**
     * Cuenta ejercicios que aún no han comenzado
     * @param ahora Fecha y hora actual
     * @return Número de ejercicios futuros
     */
    long countByStartDateAfter(LocalDateTime ahora);
    
    /**
     * Verifica si existe un ejercicio con el nombre especificado
     * @param nombre Nombre del ejercicio
     * @return true si existe, false en caso contrario
     */
    boolean existsByName(String nombre);
    
    /**
     * Verifica si existe un ejercicio con el nombre especificado excluyendo el ID dado
     * @param nombre Nombre del ejercicio
     * @param id ID del ejercicio a excluir
     * @return true si existe, false en caso contrario
     */
    boolean existsByNameAndIdNot(String nombre, Long id);
    
    /**
     * Busca ejercicios ordenados por fecha de inicio de plazo
     * @return Lista de ejercicios ordenada por fecha de inicio
     */
    List<Ejercicio> findAllByOrderByStartDateAsc();
    
    /**
     * Busca ejercicios ordenados por fecha de fin de plazo
     * @return Lista de ejercicios ordenada por fecha de fin
     */
    List<Ejercicio> findAllByOrderByEndDateAsc();
    
    /**
     * Busca ejercicios ordenados por ID
     * @return Lista de ejercicios ordenada por ID
     */
    List<Ejercicio> findAllByOrderByIdAsc();
    
    /**
     * Busca ejercicios por ID de clase ordenados por fecha de inicio
     * @param claseId ID de la clase
     * @return Lista de ejercicios ordenada por fecha de inicio
     */
    List<Ejercicio> findByClaseIdOrderByStartDateAsc(Long claseId);
    
    /**
     * Busca ejercicios por ID de clase ordenados por fecha de fin
     * @param claseId ID de la clase
     * @return Lista de ejercicios ordenada por fecha de fin
     */
    List<Ejercicio> findByClaseIdOrderByEndDateAsc(Long claseId);
    
    /**
     * Busca ejercicios por ID de clase ordenados por ID
     * @param claseId ID de la clase
     * @return Lista de ejercicios ordenada por ID
     */
    List<Ejercicio> findByClaseIdOrderByIdAsc(Long claseId);
    
    /**
     * Busca ejercicios con filtros generales y específicos combinados
     * @param searchTerm Término de búsqueda general
     * @param name Filtro por nombre
     * @param statement Filtro por enunciado
     * @param classId Filtro por ID de clase
     * @param status Filtro por estado (en plazo, vencido, futuro)
     * @param currentTime Tiempo actual para filtros de estado
     * @param pageable Configuración de paginación
     * @return Página de ejercicios
     */
    @Query("SELECT e FROM Ejercicio e WHERE " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.statement) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR :searchTerm IS NULL) AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL) AND " +
           "(LOWER(e.statement) LIKE LOWER(CONCAT('%', :statement, '%')) OR :statement IS NULL) AND " +
           "(:classId IS NULL OR e.clase.id = :classId) AND " +
           "(:status IS NULL OR " +
           "(:status = 'en_plazo' AND e.startDate <= :currentTime AND e.endDate >= :currentTime) OR " +
           "(:status = 'vencido' AND e.endDate < :currentTime) OR " +
           "(:status = 'futuro' AND e.startDate > :currentTime))")
    Page<Ejercicio> findByGeneralAndSpecificFilters(
        @Param("searchTerm") String searchTerm,
        @Param("name") String name,
        @Param("statement") String statement,
        @Param("classId") String classId,
        @Param("status") String status,
        @Param("currentTime") LocalDateTime currentTime,
        Pageable pageable
    );
}
