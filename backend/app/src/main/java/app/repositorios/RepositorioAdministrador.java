package app.repositorios;

import app.entidades.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repositorio para la entidad Administrador
 * Proporciona operaciones de acceso a datos para administradores
 */
@Repository
public interface RepositorioAdministrador extends JpaRepository<Administrador, Long> {
    
    /**
     * Busca un administrador por su nombre de usuario
     * @param username Nombre de usuario
     * @return Optional<Administrador>
     */
    @Query("SELECT a FROM Administrador a WHERE a.username = :username")
    Optional<Administrador> findByUsername(@Param("username") String username);
    
    /**
     * Busca un administrador por su email
     * @param email Email del administrador
     * @return Optional<Administrador>
     */
    @Query("SELECT a FROM Administrador a WHERE a.email = :email")
    Optional<Administrador> findByEmail(@Param("email") String email);
    
    /**
     * Busca un administrador por su DNI
     * @param dni DNI del administrador
     * @return Optional<Administrador>
     */
    @Query("SELECT a FROM Administrador a WHERE a.dni = :dni")
    Optional<Administrador> findByDni(@Param("dni") String dni);
    
    /**
     * Busca administradores por nombre (contiene, ignorando mayúsculas)
     * @param nombre Nombre a buscar
     * @return Lista de administradores
     */
    @Query("SELECT a FROM Administrador a WHERE LOWER(a.firstName) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Administrador> findByFirstNameContainingIgnoreCase(@Param("nombre") String nombre);
    
    /**
     * Busca administradores por apellidos (contiene, ignorando mayúsculas)
     * @param apellidos Apellidos a buscar
     * @return Lista de administradores
     */
    @Query("SELECT a FROM Administrador a WHERE LOWER(a.lastName) LIKE LOWER(CONCAT('%', :apellidos, '%'))")
    List<Administrador> findByLastNameContainingIgnoreCase(@Param("apellidos") String apellidos);
    
    /**
     * Busca administradores que están habilitados
     * @return Lista de administradores habilitados
     */
    @Query("SELECT a FROM Administrador a WHERE a.enabled = true")
    List<Administrador> findByEnabledTrue();
    
    /**
     * Obtiene todos los administradores ordenados por ID
     * @return Lista de administradores ordenada
     */
    @Query("SELECT a FROM Administrador a ORDER BY a.id")
    List<Administrador> findAllOrderedById();
    
    /**
     * Obtiene todos los administradores ordenados por nombre
     * @return Lista de administradores ordenada por nombre
     */
    @Query("SELECT a FROM Administrador a ORDER BY a.firstName ASC, a.lastName ASC")
    List<Administrador> findAllOrderedByNombre();
    
    /**
     * Verifica si existe un administrador con el usuario dado
     * @param username Nombre de usuario
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Administrador a WHERE a.username = :username")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Verifica si existe un administrador con el email dado
     * @param email Email
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Administrador a WHERE a.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Verifica si existe un administrador con el DNI dado
     * @param dni DNI
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Administrador a WHERE a.dni = :dni")
    boolean existsByDni(@Param("dni") String dni);
}
