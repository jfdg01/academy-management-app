package app.servicios;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import app.dtos.DTOMaterial;
import app.dtos.DTORespuestaPaginada;
import app.entidades.Material;
import app.repositorios.RepositorioMaterial;
import app.util.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class ServicioMaterialTest {

    @Mock
    private RepositorioMaterial repositorioMaterial;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ServicioMaterial servicioMaterial;

    private Material material1;
    private Material material2;
    private DTOMaterial dtoMaterial1;
    private DTOMaterial dtoMaterial2;

    @BeforeEach
    void setUp() {
        material1 = new Material("Material 1", "http://example1.com");
        material1.setId(1L);
        material2 = new Material("Material 2", "http://example2.com");
        material2.setId(2L);
        dtoMaterial1 = new DTOMaterial(material1);
        dtoMaterial2 = new DTOMaterial(material2);
    }

    // === CORE CRUD OPERATIONS TESTS ===

    @Test
    void testObtenerPorId_DebeRetornarMaterial() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        when(repositorioMaterial.findById(1L)).thenReturn(Optional.of(material1));

        // Act
        DTOMaterial result = servicioMaterial.obtenerPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Material 1", result.name());
        verify(repositorioMaterial).findById(1L);
    }



    @Test
    void testCrear_DebeCrearMaterial() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        when(repositorioMaterial.save(any(Material.class))).thenReturn(material1);

        // Act
        DTOMaterial result = servicioMaterial.crear("Nuevo Material", "http://nuevo.com");

        // Assert
        assertNotNull(result);
        verify(repositorioMaterial).save(any(Material.class));
    }

    @Test
    void testActualizar_DebeActualizarMaterial() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        when(repositorioMaterial.findById(1L)).thenReturn(Optional.of(material1));
        when(repositorioMaterial.save(any(Material.class))).thenReturn(material1);

        // Act
        DTOMaterial result = servicioMaterial.actualizar(1L, "Material Actualizado", "http://actualizado.com");

        // Assert
        assertNotNull(result);
        verify(repositorioMaterial).save(any(Material.class));
    }

    @Test
    void testEliminar_DebeEliminarMaterial() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        when(repositorioMaterial.findById(1L)).thenReturn(Optional.of(material1));

        // Act
        DTOMaterial result = servicioMaterial.eliminar(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Material 1", result.name());
        verify(repositorioMaterial).delete(material1);
    }

    // === SEARCH & FILTERING OPERATIONS TESTS ===

    @Test
    void testBuscar_DebeRetornarListaDeMateriales() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        when(repositorioMaterial.findByNameContainingIgnoreCase("test"))
                .thenReturn(Arrays.asList(material1, material2));

        // Act
        List<DTOMaterial> result = servicioMaterial.buscar("test");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repositorioMaterial).findByNameContainingIgnoreCase("test");
    }



    @Test
    void testBuscarPaginado_DebeRetornarPaginacion() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        Page<Material> page = new PageImpl<>(Arrays.asList(material1, material2));
        when(repositorioMaterial.findByFiltrosFlexibles(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act
        DTORespuestaPaginada<DTOMaterial> result = servicioMaterial.buscarPaginado(
                "test", null, null, null, 0, 10, "name", "ASC");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.content().size());
        verify(repositorioMaterial).findByFiltrosFlexibles(any(), any(), any(), any(), any(Pageable.class));
    }

    // === TYPE-SPECIFIC OPERATIONS TESTS ===

    @Test
    void testObtenerPorTipo_DebeRetornarMaterialesPorTipo() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        when(repositorioMaterial.findDocuments()).thenReturn(Arrays.asList(material1, material2));

        // Act
        List<DTOMaterial> result = servicioMaterial.obtenerPorTipo("DOCUMENT");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repositorioMaterial).findDocuments();
    }

    @Test
    void testContarPorTipo_DebeRetornarConteo() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);
        when(repositorioMaterial.countDocuments()).thenReturn(5L);

        // Act
        long result = servicioMaterial.contarPorTipo("DOCUMENT");

        // Assert
        assertEquals(5L, result);
        verify(repositorioMaterial).countDocuments();
    }

    // === SECURITY TESTS ===

    @Test
    void testObtenerPorId_SinPermisos_DebeLanzarExcepcion() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(false);
        when(securityUtils.hasRole("ALUMNO")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.obtenerPorId(1L);
        });
    }

    @Test
    void testCrear_SinPermisos_DebeLanzarExcepcion() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(false);
        when(securityUtils.hasRole("PROFESOR")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.crear("test", "http://test.com");
        });
    }

    // === VALIDATION TESTS ===

    @Test
    void testCrear_ConNombreVacio_DebeLanzarExcepcion() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.crear("", "http://test.com");
        });
    }

    @Test
    void testCrear_ConUrlVacia_DebeLanzarExcepcion() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.crear("test", "");
        });
    }

    @Test
    void testBuscarPaginado_ConParametrosInvalidos_DebeLanzarExcepcion() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.buscarPaginado("test", null, null, null, -1, 10, "name", "ASC");
        });
    }

    @Test
    void testBuscarPaginado_ConTamañoInvalido_DebeLanzarExcepcion() {
        // Arrange
        when(securityUtils.hasRole("ADMIN")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            servicioMaterial.buscarPaginado("test", null, null, null, 0, 101, "name", "ASC");
        });
    }
}
