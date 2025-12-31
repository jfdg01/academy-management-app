package app.repositorios;

import app.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 * Proporciona operaciones de acceso a datos para usuarios
 */
@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca un usuario por su nombre de usuario
     * @param username Nombre de usuario
     * @return Optional<Usuario>
     */
    @Query("SELECT u FROM Usuario u WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);
    
    /**
     * Busca un usuario por su email
     * @param email Email del usuario
     * @return Optional<Usuario>
     */
    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Optional<Usuario> findByEmail(@Param("email") String email);
    
    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     * @param username Nombre de usuario
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Verifica si existe un usuario con el email dado
     * @param email Email
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
} 