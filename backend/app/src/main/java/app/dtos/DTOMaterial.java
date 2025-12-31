package app.dtos;

import app.entidades.Material;

/**
 * Simple DTO for Material entity to avoid lazy loading issues
 * Only includes essential fields needed for display
 */
public record DTOMaterial(
        Long id,
        String name,
        String url
) {
    
    /**
     * Constructor that creates a DTO from a Material entity
     */
    public DTOMaterial(Material material) {
        this(
            material.getId(),
            material.getName(),
            material.getUrl()
        );
    }
    
    /**
     * Constructor with explicit parameters
     */
    public DTOMaterial(Long id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }
}
