package app.repositorios;

import app.entidades.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the Material entity
 * Provides data access operations for materials
 */
@Repository
public interface RepositorioMaterial extends JpaRepository<Material, Long> {

       /**
        * Finds a material by its name
        * 
        * @param name Material name
        * @return Optional<Material>
        */
       @Query("SELECT m FROM Material m WHERE m.name = :name")
       Optional<Material> findByName(@Param("name") String name);

       /**
        * Finds a material by ID with classes loaded
        * 
        * @param id Material ID
        * @return Optional<Material> with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE m.id = :id")
       Optional<Material> findByIdWithClasses(@Param("id") Long id);

       /**
        * Finds a material by ID with all relationships loaded
        * 
        * @param id Material ID
        * @return Optional<Material> with all relationships
        */
       @EntityGraph(value = "Material.withAllRelationships")
       @Query("SELECT m FROM Material m WHERE m.id = :id")
       Optional<Material> findByIdWithAllRelationships(@Param("id") Long id);

       /**
        * Finds materials by name with classes loaded
        * 
        * @param name Material name
        * @return List of materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE m.name = :name")
       List<Material> findByNameWithClasses(@Param("name") String name);

       /**
        * Finds all materials with classes loaded
        * 
        * @return List of all materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m")
       List<Material> findAllWithClasses();

       /**
        * Finds all materials with all relationships loaded
        * 
        * @return List of all materials with all relationships
        */
       @EntityGraph(value = "Material.withAllRelationships")
       @Query("SELECT m FROM Material m")
       List<Material> findAllWithAllRelationships();

       /**
        * Finds materials by name (contains, case insensitive)
        * 
        * @param name Name to search
        * @return List of materials
        */
       @Query("SELECT m FROM Material m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
       List<Material> findByNameContainingIgnoreCase(@Param("name") String name);

       /**
        * Finds materials by name (contains, case insensitive) with classes loaded
        * 
        * @param name Name to search
        * @return List of materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
       List<Material> findByNameContainingIgnoreCaseWithClasses(@Param("name") String name);

       /**
        * Finds materials by name (contains, case insensitive) with pagination
        * 
        * @param name     Name to search
        * @param pageable Pagination parameters
        * @return Page of materials
        */
       @Query("SELECT m FROM Material m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
       Page<Material> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

       /**
        * Finds materials by name (contains, case insensitive) with pagination and
        * classes loaded
        * 
        * @param name     Name to search
        * @param pageable Pagination parameters
        * @return Page of materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
       Page<Material> findByNameContainingIgnoreCaseWithClasses(@Param("name") String name, Pageable pageable);

       /**
        * Finds materials by URL (contains, case insensitive)
        * 
        * @param url URL to search
        * @return List of materials
        */
       @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE LOWER(CONCAT('%', :url, '%'))")
       List<Material> findByUrlContainingIgnoreCase(@Param("url") String url);

       /**
        * Finds materials by URL (contains, case insensitive) with classes loaded
        * 
        * @param url URL to search
        * @return List of materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE LOWER(CONCAT('%', :url, '%'))")
       List<Material> findByUrlContainingIgnoreCaseWithClasses(@Param("url") String url);

       /**
        * Finds materials by URL (contains, case insensitive) with pagination
        * 
        * @param url      URL to search
        * @param pageable Pagination parameters
        * @return Page of materials
        */
       @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE LOWER(CONCAT('%', :url, '%'))")
       Page<Material> findByUrlContainingIgnoreCase(@Param("url") String url, Pageable pageable);

       /**
        * Finds materials by URL (contains, case insensitive) with pagination and
        * classes loaded
        * 
        * @param url      URL to search
        * @param pageable Pagination parameters
        * @return Page of materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE LOWER(CONCAT('%', :url, '%'))")
       Page<Material> findByUrlContainingIgnoreCaseWithClasses(@Param("url") String url, Pageable pageable);

       /**
        * Finds materials by name or URL (contains, case insensitive) with pagination
        * 
        * @param name     Name to search
        * @param url      URL to search
        * @param pageable Pagination parameters
        * @return Page of materials
        */
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
                     "LOWER(m.url) LIKE LOWER(CONCAT('%', :url, '%'))")
       Page<Material> findByNameContainingIgnoreCaseOrUrlContainingIgnoreCase(
                     @Param("name") String name,
                     @Param("url") String url,
                     Pageable pageable);

       /**
        * Finds materials by name or URL (contains, case insensitive) with pagination
        * and classes loaded
        * 
        * @param name     Name to search
        * @param url      URL to search
        * @param pageable Pagination parameters
        * @return Page of materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
                     "LOWER(m.url) LIKE LOWER(CONCAT('%', :url, '%'))")
       Page<Material> findByNameContainingIgnoreCaseOrUrlContainingIgnoreCaseWithClasses(
                     @Param("name") String name,
                     @Param("url") String url,
                     Pageable pageable);

       /**
        * Finds materials by file extension
        * 
        * @param extension File extension to search
        * @return List of materials
        */
       @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE LOWER(CONCAT('%.', :extension))")
       List<Material> findByFileExtension(@Param("extension") String extension);

       /**
        * Finds materials by file extension with classes loaded
        * 
        * @param extension File extension to search
        * @return List of materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE LOWER(CONCAT('%.', :extension))")
       List<Material> findByFileExtensionWithClasses(@Param("extension") String extension);

       /**
        * Finds materials by file extension with pagination
        * 
        * @param extension File extension to search
        * @param pageable  Pagination parameters
        * @return Page of materials
        */
       @Query("SELECT m FROM Material m WHERE LOWER(m.url) LIKE LOWER(CONCAT('%.', :extension))")
       Page<Material> findByFileExtension(@Param("extension") String extension, Pageable pageable);

       /**
        * Finds materials that are documents (PDF, DOC, etc.)
        * 
        * @return List of document materials
        */
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.pdf' OR " +
                     "LOWER(m.url) LIKE '%.doc' OR " +
                     "LOWER(m.url) LIKE '%.docx' OR " +
                     "LOWER(m.url) LIKE '%.txt' OR " +
                     "LOWER(m.url) LIKE '%.rtf' OR " +
                     "LOWER(m.url) LIKE '%.md'")
       List<Material> findDocuments();

       /**
        * Finds materials that are documents (PDF, DOC, etc.) with classes loaded
        * 
        * @return List of document materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.pdf' OR " +
                     "LOWER(m.url) LIKE '%.doc' OR " +
                     "LOWER(m.url) LIKE '%.docx' OR " +
                     "LOWER(m.url) LIKE '%.txt' OR " +
                     "LOWER(m.url) LIKE '%.rtf' OR " +
                     "LOWER(m.url) LIKE '%.md'")
       List<Material> findDocumentsWithClasses();

       /**
        * Finds materials that are documents (PDF, DOC, etc.) with pagination
        * 
        * @param pageable Pagination parameters
        * @return Page of document materials
        */
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.pdf' OR " +
                     "LOWER(m.url) LIKE '%.doc' OR " +
                     "LOWER(m.url) LIKE '%.docx' OR " +
                     "LOWER(m.url) LIKE '%.txt' OR " +
                     "LOWER(m.url) LIKE '%.rtf' OR " +
                     "LOWER(m.url) LIKE '%.md'")
       Page<Material> findDocuments(Pageable pageable);

       /**
        * Finds materials that are documents (PDF, DOC, etc.) with pagination and
        * classes loaded
        * 
        * @param pageable Pagination parameters
        * @return Page of document materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.pdf' OR " +
                     "LOWER(m.url) LIKE '%.doc' OR " +
                     "LOWER(m.url) LIKE '%.docx' OR " +
                     "LOWER(m.url) LIKE '%.txt' OR " +
                     "LOWER(m.url) LIKE '%.rtf' OR " +
                     "LOWER(m.url) LIKE '%.md'")
       Page<Material> findDocumentsWithClasses(Pageable pageable);

       /**
        * Finds materials that are images
        * 
        * @return List of image materials
        */
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.jpg' OR " +
                     "LOWER(m.url) LIKE '%.jpeg' OR " +
                     "LOWER(m.url) LIKE '%.png' OR " +
                     "LOWER(m.url) LIKE '%.gif' OR " +
                     "LOWER(m.url) LIKE '%.bmp' OR " +
                     "LOWER(m.url) LIKE '%.svg'")
       List<Material> findImages();

       /**
        * Finds materials that are images with classes loaded
        * 
        * @return List of image materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.jpg' OR " +
                     "LOWER(m.url) LIKE '%.jpeg' OR " +
                     "LOWER(m.url) LIKE '%.png' OR " +
                     "LOWER(m.url) LIKE '%.gif' OR " +
                     "LOWER(m.url) LIKE '%.bmp' OR " +
                     "LOWER(m.url) LIKE '%.svg'")
       List<Material> findImagesWithClasses();

       /**
        * Finds materials that are images with pagination
        * 
        * @param pageable Pagination parameters
        * @return Page of image materials
        */
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.jpg' OR " +
                     "LOWER(m.url) LIKE '%.jpeg' OR " +
                     "LOWER(m.url) LIKE '%.png' OR " +
                     "LOWER(m.url) LIKE '%.gif' OR " +
                     "LOWER(m.url) LIKE '%.bmp' OR " +
                     "LOWER(m.url) LIKE '%.svg'")
       Page<Material> findImages(Pageable pageable);

       /**
        * Finds materials that are images with pagination and classes loaded
        * 
        * @param pageable Pagination parameters
        * @return Page of image materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.jpg' OR " +
                     "LOWER(m.url) LIKE '%.jpeg' OR " +
                     "LOWER(m.url) LIKE '%.png' OR " +
                     "LOWER(m.url) LIKE '%.gif' OR " +
                     "LOWER(m.url) LIKE '%.bmp' OR " +
                     "LOWER(m.url) LIKE '%.svg'")
       Page<Material> findImagesWithClasses(Pageable pageable);

       /**
        * Finds materials that are videos
        * 
        * @return List of video materials
        */
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.mp4' OR " +
                     "LOWER(m.url) LIKE '%.avi' OR " +
                     "LOWER(m.url) LIKE '%.mov' OR " +
                     "LOWER(m.url) LIKE '%.wmv' OR " +
                     "LOWER(m.url) LIKE '%.flv' OR " +
                     "LOWER(m.url) LIKE '%.webm'")
       List<Material> findVideos();

       /**
        * Finds materials that are videos with classes loaded
        * 
        * @return List of video materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.mp4' OR " +
                     "LOWER(m.url) LIKE '%.avi' OR " +
                     "LOWER(m.url) LIKE '%.mov' OR " +
                     "LOWER(m.url) LIKE '%.wmv' OR " +
                     "LOWER(m.url) LIKE '%.flv' OR " +
                     "LOWER(m.url) LIKE '%.webm'")
       List<Material> findVideosWithClasses();

       /**
        * Finds materials that are videos with pagination
        * 
        * @param pageable Pagination parameters
        * @return Page of video materials
        */
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.mp4' OR " +
                     "LOWER(m.url) LIKE '%.avi' OR " +
                     "LOWER(m.url) LIKE '%.mov' OR " +
                     "LOWER(m.url) LIKE '%.wmv' OR " +
                     "LOWER(m.url) LIKE '%.flv' OR " +
                     "LOWER(m.url) LIKE '%.webm'")
       Page<Material> findVideos(Pageable pageable);

       /**
        * Finds materials that are videos with pagination and classes loaded
        * 
        * @param pageable Pagination parameters
        * @return Page of video materials with classes
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.mp4' OR " +
                     "LOWER(m.url) LIKE '%.avi' OR " +
                     "LOWER(m.url) LIKE '%.mov' OR " +
                     "LOWER(m.url) LIKE '%.wmv' OR " +
                     "LOWER(m.url) LIKE '%.flv' OR " +
                     "LOWER(m.url) LIKE '%.webm'")
       Page<Material> findVideosWithClasses(Pageable pageable);

       /**
        * Counts materials by type
        * 
        * @return Number of materials
        */
       @Query("SELECT COUNT(m) FROM Material m")
       Long countAllMaterials();

       /**
        * Counts document materials
        * 
        * @return Number of document materials
        */
       @Query("SELECT COUNT(m) FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.pdf' OR " +
                     "LOWER(m.url) LIKE '%.doc' OR " +
                     "LOWER(m.url) LIKE '%.docx' OR " +
                     "LOWER(m.url) LIKE '%.txt' OR " +
                     "LOWER(m.url) LIKE '%.rtf' OR " +
                     "LOWER(m.url) LIKE '%.md'")
       Long countDocuments();

       /**
        * Counts image materials
        * 
        * @return Number of image materials
        */
       @Query("SELECT COUNT(m) FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.jpg' OR " +
                     "LOWER(m.url) LIKE '%.jpeg' OR " +
                     "LOWER(m.url) LIKE '%.png' OR " +
                     "LOWER(m.url) LIKE '%.gif' OR " +
                     "LOWER(m.url) LIKE '%.bmp' OR " +
                     "LOWER(m.url) LIKE '%.svg'")
       Long countImages();

       /**
        * Counts video materials
        * 
        * @return Number of video materials
        */
       @Query("SELECT COUNT(m) FROM Material m WHERE " +
                     "LOWER(m.url) LIKE '%.mp4' OR " +
                     "LOWER(m.url) LIKE '%.avi' OR " +
                     "LOWER(m.url) LIKE '%.mov' OR " +
                     "LOWER(m.url) LIKE '%.wmv' OR " +
                     "LOWER(m.url) LIKE '%.flv' OR " +
                     "LOWER(m.url) LIKE '%.webm'")
       Long countVideos();

       /**
        * Búsqueda flexible de materiales con múltiples filtros opcionales
        * Permite combinar filtros de nombre, URL y tipo
        * 
        * @param searchTerm Término de búsqueda general
        * @param nombre     Filtro por nombre
        * @param url        Filtro por URL
        * @param tipo       Filtro por tipo de material
        * @param pageable   Configuración de paginación
        * @return Página de materiales
        */
       @Query("SELECT m FROM Material m WHERE " +
                     "(LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(m.url) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR :searchTerm IS NULL) AND " +
                     "(LOWER(m.name) LIKE LOWER(CONCAT('%', :nombre, '%')) OR :nombre IS NULL) AND " +
                     "(LOWER(m.url) LIKE LOWER(CONCAT('%', :url, '%')) OR :url IS NULL) AND " +
                     "(:tipo IS NULL OR (" +
                     "  CASE WHEN :tipo = 'DOCUMENT' THEN (" +
                     "    LOWER(m.url) LIKE '%.pdf' OR LOWER(m.url) LIKE '%.doc' OR " +
                     "    LOWER(m.url) LIKE '%.docx' OR LOWER(m.url) LIKE '%.txt' OR " +
                     "    LOWER(m.url) LIKE '%.rtf' OR LOWER(m.url) LIKE '%.md') " +
                     "       WHEN :tipo = 'IMAGE' THEN (" +
                     "    LOWER(m.url) LIKE '%.jpg' OR LOWER(m.url) LIKE '%.jpeg' OR " +
                     "    LOWER(m.url) LIKE '%.png' OR LOWER(m.url) LIKE '%.gif' OR " +
                     "    LOWER(m.url) LIKE '%.bmp' OR LOWER(m.url) LIKE '%.svg') " +
                     "       WHEN :tipo = 'VIDEO' THEN (" +
                     "    LOWER(m.url) LIKE '%.mp4' OR LOWER(m.url) LIKE '%.avi' OR " +
                     "    LOWER(m.url) LIKE '%.mov' OR LOWER(m.url) LIKE '%.wmv' OR " +
                     "    LOWER(m.url) LIKE '%.flv' OR LOWER(m.url) LIKE '%.webm') " +
                     "       ELSE TRUE END))")
       Page<Material> findByFiltrosFlexibles(
                     @Param("searchTerm") String searchTerm,
                     @Param("nombre") String nombre,
                     @Param("url") String url,
                     @Param("tipo") String tipo,
                     Pageable pageable);

       /**
        * Búsqueda flexible de materiales con múltiples filtros opcionales y clases
        * cargadas
        * Permite combinar filtros de nombre, URL y tipo con relaciones cargadas
        * 
        * @param searchTerm Término de búsqueda general
        * @param nombre     Filtro por nombre
        * @param url        Filtro por URL
        * @param tipo       Filtro por tipo de material
        * @param pageable   Configuración de paginación
        * @return Página de materiales con clases
        */
       @EntityGraph(value = "Material.withClasses")
       @Query("SELECT m FROM Material m WHERE " +
                     "(LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(m.url) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR :searchTerm IS NULL) AND " +
                     "(LOWER(m.name) LIKE LOWER(CONCAT('%', :nombre, '%')) OR :nombre IS NULL) AND " +
                     "(LOWER(m.url) LIKE LOWER(CONCAT('%', :url, '%')) OR :url IS NULL) AND " +
                     "(:tipo IS NULL OR (" +
                     "  CASE WHEN :tipo = 'DOCUMENT' THEN (" +
                     "    LOWER(m.url) LIKE '%.pdf' OR LOWER(m.url) LIKE '%.doc' OR " +
                     "    LOWER(m.url) LIKE '%.docx' OR LOWER(m.url) LIKE '%.txt' OR " +
                     "    LOWER(m.url) LIKE '%.rtf' OR LOWER(m.url) LIKE '%.md') " +
                     "       WHEN :tipo = 'IMAGE' THEN (" +
                     "    LOWER(m.url) LIKE '%.jpg' OR LOWER(m.url) LIKE '%.jpeg' OR " +
                     "    LOWER(m.url) LIKE '%.png' OR LOWER(m.url) LIKE '%.gif' OR " +
                     "    LOWER(m.url) LIKE '%.bmp' OR LOWER(m.url) LIKE '%.svg') " +
                     "       WHEN :tipo = 'VIDEO' THEN (" +
                     "    LOWER(m.url) LIKE '%.mp4' OR LOWER(m.url) LIKE '%.avi' OR " +
                     "    LOWER(m.url) LIKE '%.mov' OR LOWER(m.url) LIKE '%.wmv' OR " +
                     "    LOWER(m.url) LIKE '%.flv' OR LOWER(m.url) LIKE '%.webm') " +
                     "       ELSE TRUE END))")
       Page<Material> findByFiltrosFlexiblesWithClasses(
                     @Param("searchTerm") String searchTerm,
                     @Param("nombre") String nombre,
                     @Param("url") String url,
                     @Param("tipo") String tipo,
                     Pageable pageable);
}
