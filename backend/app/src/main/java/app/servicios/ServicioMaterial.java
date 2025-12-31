package app.servicios;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.dtos.DTOMaterial;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Material;
import app.entidades.Clase;
import app.repositorios.RepositorioMaterial;
import app.util.ExceptionUtils;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

/**
 * Service for Material entity management
 * Follows Spring Boot best practices with consolidated methods
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ServicioMaterial {

    private final RepositorioMaterial repositorioMaterial;
    private final SecurityUtils securityUtils;

    // === CORE CRUD OPERATIONS ===

    @Transactional(readOnly = true)
    public DTOMaterial obtenerPorId(Long id) {
        validateReadAccess();
        Material material = repositorioMaterial.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(material, "Material", "ID", id);
        return new DTOMaterial(material);
    }

    public DTOMaterial crear(String name, String url) {
        validateWriteAccess();
        validateMaterialData(name, url);

        Material material = new Material(name.trim(), url.trim());

        Material materialGuardado = repositorioMaterial.save(material);
        return new DTOMaterial(materialGuardado);
    }

    public DTOMaterial actualizar(Long id, String name, String url) {
        validateWriteAccess();
        validateMaterialData(name, url);

        Material material = repositorioMaterial.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(material, "Material", "ID", id);

        material.setName(name.trim());
        material.setUrl(url.trim());

        Material materialActualizado = repositorioMaterial.save(material);
        return new DTOMaterial(materialActualizado);
    }

    public DTOMaterial eliminar(Long id) {
        validateWriteAccess();
        Material material = repositorioMaterial.findById(id).orElse(null);
        ExceptionUtils.throwIfNotFound(material, "Material", "ID", id);
        
        // Create DTO before deletion to return the deleted material
        DTOMaterial materialEliminado = new DTOMaterial(material);
        
        // Remove material from all associated classes before deletion
        // This prevents foreign key constraint violations
        List<Clase> associatedClasses = new ArrayList<>(material.getClasses());
        for (Clase clase : associatedClasses) {
            material.removerClase(clase);
        }
        
        repositorioMaterial.delete(material);
        return materialEliminado;
    }

    // === SEARCH & FILTERING OPERATIONS ===

    @Transactional(readOnly = true)
    public List<DTOMaterial> buscar(String searchTerm) {
        validateReadAccess();

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return repositorioMaterial.findAll().stream()
                    .map(DTOMaterial::new)
                    .collect(Collectors.toList());
        }

        return repositorioMaterial.findByNameContainingIgnoreCase(searchTerm.trim()).stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOMaterial> buscarPaginado(
            String q,
            String name,
            String url,
            String type,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        validateReadAccess();
        validatePaginationParams(page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Prepare filter parameters
        String searchTerm = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String nombreFilter = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String urlFilter = (url != null && !url.trim().isEmpty()) ? url.trim() : null;
        String tipoFilter = (type != null && !type.trim().isEmpty()) ? type.toUpperCase() : null;

        Page<Material> materialPage = repositorioMaterial.findByFiltrosFlexibles(
                searchTerm, nombreFilter, urlFilter, tipoFilter, pageable);

        List<DTOMaterial> materiales = materialPage.getContent().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());

        return new DTORespuestaPaginada<>(
                materiales,
                materialPage.getNumber(),
                materialPage.getSize(),
                materialPage.getTotalElements(),
                materialPage.getTotalPages(),
                materialPage.isFirst(),
                materialPage.isLast(),
                materialPage.hasContent(),
                sortBy,
                sortDirection);
    }

    // === TYPE-SPECIFIC OPERATIONS ===

    @Transactional(readOnly = true)
    public List<DTOMaterial> obtenerPorTipo(String tipo) {
        validateReadAccess();

        if (tipo == null || tipo.trim().isEmpty()) {
            return repositorioMaterial.findAll().stream()
                    .map(DTOMaterial::new)
                    .collect(Collectors.toList());
        }

        String tipoUpper = tipo.trim().toUpperCase();
        switch (tipoUpper) {
            case "DOCUMENT":
            case "DOCUMENTO":
                return repositorioMaterial.findDocuments().stream()
                        .map(DTOMaterial::new)
                        .collect(Collectors.toList());
            case "IMAGE":
            case "IMAGEN":
                return repositorioMaterial.findImages().stream()
                        .map(DTOMaterial::new)
                        .collect(Collectors.toList());
            case "VIDEO":
                return repositorioMaterial.findVideos().stream()
                        .map(DTOMaterial::new)
                        .collect(Collectors.toList());
            default:
                return repositorioMaterial.findByFileExtension(tipo.trim()).stream()
                        .map(DTOMaterial::new)
                        .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public DTORespuestaPaginada<DTOMaterial> obtenerPorTipoPaginado(
            String tipo,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        validateReadAccess();
        validatePaginationParams(page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Material> materialPage;

        if (tipo == null || tipo.trim().isEmpty()) {
            materialPage = repositorioMaterial.findAll(pageable);
        } else {
            String tipoUpper = tipo.trim().toUpperCase();
            switch (tipoUpper) {
                case "DOCUMENT":
                case "DOCUMENTO":
                    materialPage = repositorioMaterial.findDocuments(pageable);
                    break;
                case "IMAGE":
                case "IMAGEN":
                    materialPage = repositorioMaterial.findImages(pageable);
                    break;
                case "VIDEO":
                    materialPage = repositorioMaterial.findVideos(pageable);
                    break;
                default:
                    materialPage = repositorioMaterial.findByFileExtension(tipo.trim(), pageable);
                    break;
            }
        }

        List<DTOMaterial> materiales = materialPage.getContent().stream()
                .map(DTOMaterial::new)
                .collect(Collectors.toList());

        return new DTORespuestaPaginada<>(
                materiales,
                materialPage.getNumber(),
                materialPage.getSize(),
                materialPage.getTotalElements(),
                materialPage.getTotalPages(),
                materialPage.isFirst(),
                materialPage.isLast(),
                materialPage.hasContent(),
                sortBy,
                sortDirection);
    }

    @Transactional(readOnly = true)
    public long contarPorTipo(String tipo) {
        validateReadAccess();

        if (tipo == null || tipo.trim().isEmpty()) {
            return repositorioMaterial.countAllMaterials();
        }

        String tipoUpper = tipo.trim().toUpperCase();
        switch (tipoUpper) {
            case "DOCUMENT":
            case "DOCUMENTO":
                return repositorioMaterial.countDocuments();
            case "IMAGE":
            case "IMAGEN":
                return repositorioMaterial.countImages();
            case "VIDEO":
                return repositorioMaterial.countVideos();
            default:
                return repositorioMaterial.countAllMaterials();
        }
    }

    // === PRIVATE HELPER METHODS ===

    private void validateReadAccess() {
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR") && !securityUtils.hasRole("ALUMNO")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para acceder a materiales");
        }
    }

    private void validateWriteAccess() {
        if (!securityUtils.hasRole("ADMIN") && !securityUtils.hasRole("PROFESOR")) {
            ExceptionUtils.throwAccessDenied("No tienes permisos para modificar materiales");
        }
    }

    private void validateMaterialData(String name, String url) {
        if (name == null || name.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("El nombre del material no puede estar vacío");
        }

        if (url == null || url.trim().isEmpty()) {
            ExceptionUtils.throwValidationError("La URL del material no puede estar vacía");
        }
    }

    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            ExceptionUtils.throwValidationError("El número de página no puede ser negativo");
        }
        if (size < 1 || size > 100) {
            ExceptionUtils.throwValidationError("El tamaño de página debe estar entre 1 y 100");
        }
    }
}
