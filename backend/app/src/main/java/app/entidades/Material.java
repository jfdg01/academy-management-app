package app.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Material
 * Representa material didáctico asociado a una clase
 * Basado en el UML de especificación
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "materiales")
@NamedEntityGraph(name = "Material.withClasses", attributeNodes = {
        @NamedAttributeNode("classes")
})
@NamedEntityGraph(name = "Material.withAllRelationships", attributeNodes = {
        @NamedAttributeNode("classes")
})
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(columnDefinition = "text")
    private String name;

    @NotNull
    @Size(max = 500)
    @Column(columnDefinition = "text")
    private String url;

    // Many-to-Many relationship with Clase
    @ManyToMany(mappedBy = "material", fetch = FetchType.LAZY)
    private List<Clase> classes = new ArrayList<>();

    public Material() {
    }

    public Material(String name, String url) {
        this.name = name;
        this.url = url;
    }

    /**
     * Adds a class to this material
     * 
     * @param clase Class to add
     */
    public void agregarClase(Clase clase) {
        if (!this.classes.contains(clase)) {
            this.classes.add(clase);
            clase.getMaterial().add(this);
        }
    }

    /**
     * Removes a class from this material
     * 
     * @param clase Class to remove
     */
    public void removerClase(Clase clase) {
        this.classes.remove(clase);
        clase.getMaterial().remove(this);
    }
}
