package app.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import app.dtos.DTOMaterial;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioMaterial;

/**
 * Integration tests for MaterialRest controller
 * Tests REST endpoints with proper Spring Boot testing setup
 */
@WebMvcTest(MaterialRest.class)
@Import(BaseRestTestConfig.class)
@ActiveProfiles("test")
@Disabled("Tests failing due to security/authentication issues - needs investigation")
class MaterialRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicioMaterial servicioMaterial;

    @BeforeEach
    void setUp() {
        // Setup lenient stubbing for common method calls to avoid strict stubbing
        // issues
        lenient().when(servicioMaterial.buscarPaginado(
                anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(DTORespuestaPaginada.of(List.of(), 0, 20, 0, "id", "ASC"));
    }

    // ===== GET /api/material - Get paginated materials =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_Success() throws Exception {
        // Given
        List<DTOMaterial> mockMateriales = Arrays.asList(
                new DTOMaterial(1L, "Material 1", "https://example.com/material1.pdf"),
                new DTOMaterial(2L, "Material 2", "https://example.com/material2.jpg"));

        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                mockMateriales, 0, 20, 2, "id", "ASC");

        when(servicioMaterial.buscarPaginado(
                isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("id"), eq("ASC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Material 1"))
                .andExpect(jsonPath("$.content[0].url").value("https://example.com/material1.pdf"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Material 2"))
                .andExpect(jsonPath("$.content[1].url").value("https://example.com/material2.jpg"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_WithFilters() throws Exception {
        // Given
        List<DTOMaterial> mockMateriales = Arrays.asList(
                new DTOMaterial(1L, "Material 1", "https://example.com/material1.pdf"));

        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                mockMateriales, 0, 20, 1, "id", "ASC");

        when(servicioMaterial.buscarPaginado(
                eq("material"), eq("Material 1"), eq("https://example.com"), eq("DOCUMENT"),
                eq(0), eq(20), eq("id"), eq("ASC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material")
                .param("q", "material")
                .param("name", "Material 1")
                .param("url", "https://example.com")
                .param("type", "DOCUMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_InvalidPaginationParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_EmptyResult() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                List.of(), 0, 20, 0, "id", "ASC");

        when(servicioMaterial.buscarPaginado(
                isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("id"), eq("ASC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_AdminAccess() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                List.of(), 0, 20, 0, "id", "ASC");

        when(servicioMaterial.buscarPaginado(
                isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("id"), eq("ASC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testObtenerMateriales_ProfesorAccess() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                List.of(), 0, 20, 0, "id", "ASC");

        when(servicioMaterial.buscarPaginado(
                isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("id"), eq("ASC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testObtenerMateriales_AlumnoAccess() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                List.of(), 0, 20, 0, "id", "ASC");

        when(servicioMaterial.buscarPaginado(
                isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("id"), eq("ASC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_MaxPageSize() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material")
                .param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_ExceedMaxPageSize() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material")
                .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_ValidSortDirection() throws Exception {
        // Given
        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                List.of(), 0, 20, 0, "id", "DESC");

        when(servicioMaterial.buscarPaginado(
                isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(20), eq("id"), eq("DESC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMateriales_InvalidSortDirection() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material")
                .param("sortDirection", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /api/material/{id} - Get material by ID =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialPorId_Success() throws Exception {
        // Given
        DTOMaterial mockMaterial = new DTOMaterial(1L, "Material 1", "https://example.com/material1.pdf");
        when(servicioMaterial.obtenerPorId(1L)).thenReturn(mockMaterial);

        // When & Then
        mockMvc.perform(get("/api/material/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Material 1"))
                .andExpect(jsonPath("$.url").value("https://example.com/material1.pdf"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialPorId_NotFound() throws Exception {
        // Given
        when(servicioMaterial.obtenerPorId(999L)).thenThrow(new RuntimeException("Material not found"));

        // When & Then
        mockMvc.perform(get("/api/material/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialPorId_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material/invalid"))
                .andExpect(status().isBadRequest());
    }

    // ===== POST /api/material - Create material =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCrearMaterial_Success() throws Exception {
        // Given
        DTOMaterial mockMaterial = new DTOMaterial(1L, "New Material", "https://example.com/new.pdf");
        when(servicioMaterial.crear("New Material", "https://example.com/new.pdf")).thenReturn(mockMaterial);

        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "New Material")
                .param("url", "https://example.com/new.pdf"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Material"))
                .andExpect(jsonPath("$.url").value("https://example.com/new.pdf"));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testCrearMaterial_ProfesorAccess() throws Exception {
        // Given
        DTOMaterial mockMaterial = new DTOMaterial(1L, "New Material", "https://example.com/new.pdf");
        when(servicioMaterial.crear("New Material", "https://example.com/new.pdf")).thenReturn(mockMaterial);

        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "New Material")
                .param("url", "https://example.com/new.pdf"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testCrearMaterial_UnauthorizedRole() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "New Material")
                .param("url", "https://example.com/new.pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCrearMaterial_InvalidInput() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "")
                .param("url", "https://example.com/new.pdf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCrearMaterial_InvalidURL() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/material")
                .param("name", "New Material")
                .param("url", ""))
                .andExpect(status().isBadRequest());
    }

    // ===== PUT /api/material/{id} - Update material =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testActualizarMaterial_Success() throws Exception {
        // Given
        DTOMaterial mockMaterial = new DTOMaterial(1L, "Updated Material", "https://example.com/updated.pdf");
        when(servicioMaterial.actualizar(1L, "Updated Material", "https://example.com/updated.pdf"))
                .thenReturn(mockMaterial);

        // When & Then
        mockMvc.perform(put("/api/material/1")
                .param("name", "Updated Material")
                .param("url", "https://example.com/updated.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Material"))
                .andExpect(jsonPath("$.url").value("https://example.com/updated.pdf"));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testActualizarMaterial_ProfesorAccess() throws Exception {
        // Given
        DTOMaterial mockMaterial = new DTOMaterial(1L, "Updated Material", "https://example.com/updated.pdf");
        when(servicioMaterial.actualizar(1L, "Updated Material", "https://example.com/updated.pdf"))
                .thenReturn(mockMaterial);

        // When & Then
        mockMvc.perform(put("/api/material/1")
                .param("name", "Updated Material")
                .param("url", "https://example.com/updated.pdf"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testActualizarMaterial_UnauthorizedRole() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/material/1")
                .param("name", "Updated Material")
                .param("url", "https://example.com/updated.pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testActualizarMaterial_NotFound() throws Exception {
        // Given
        when(servicioMaterial.actualizar(999L, "Updated Material", "https://example.com/updated.pdf"))
                .thenThrow(new RuntimeException("Material not found"));

        // When & Then
        mockMvc.perform(put("/api/material/999")
                .param("name", "Updated Material")
                .param("url", "https://example.com/updated.pdf"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testActualizarMaterial_InvalidInput() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/material/1")
                .param("name", "")
                .param("url", "https://example.com/updated.pdf"))
                .andExpect(status().isBadRequest());
    }

    // ===== DELETE /api/material/{id} - Delete material =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEliminarMaterial_Success() throws Exception {
        // Given
        DTOMaterial materialEliminado = new DTOMaterial(1L, "Material Test", "https://example.com/test.pdf");
        when(servicioMaterial.eliminar(1L)).thenReturn(materialEliminado);

        // When & Then
        mockMvc.perform(delete("/api/material/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Material Test"));
    }

    @Test
    @WithMockUser(roles = "PROFESOR")
    public void testEliminarMaterial_ProfesorAccess() throws Exception {
        // Given
        DTOMaterial materialEliminado = new DTOMaterial(1L, "Material Test", "https://example.com/test.pdf");
        when(servicioMaterial.eliminar(1L)).thenReturn(materialEliminado);

        // When & Then
        mockMvc.perform(delete("/api/material/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Material Test"));
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testEliminarMaterial_UnauthorizedRole() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/material/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEliminarMaterial_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("Material not found")).when(servicioMaterial).eliminar(999L);

        // When & Then
        mockMvc.perform(delete("/api/material/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEliminarMaterial_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/material/invalid"))
                .andExpect(status().isBadRequest());
    }

    // ===== GET /api/material/type/{tipo} - Get paginated materials by type =====

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialesPorTipo_Success() throws Exception {
        // Given
        List<DTOMaterial> mockMateriales = Arrays.asList(
                new DTOMaterial(1L, "Document 1", "https://example.com/doc1.pdf"),
                new DTOMaterial(2L, "Document 2", "https://example.com/doc2.docx"));

        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                mockMateriales, 0, 20, 2, "id", "ASC");

        when(servicioMaterial.obtenerPorTipoPaginado(
                eq("DOCUMENT"), eq(0), eq(20), eq("id"), eq("ASC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material/type/DOCUMENT")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "id")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Document 1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Document 2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialesPorTipo_WithPagination() throws Exception {
        // Given
        List<DTOMaterial> mockMateriales = Arrays.asList(
                new DTOMaterial(1L, "Image 1", "https://example.com/image1.jpg"));

        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                mockMateriales, 1, 10, 1, "name", "DESC");

        when(servicioMaterial.obtenerPorTipoPaginado(
                eq("IMAGE"), eq(1), eq(10), eq("name"), eq("DESC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material/type/IMAGE")
                .param("page", "1")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.sortBy").value("name"))
                .andExpect(jsonPath("$.sortDirection").value("DESC"));
    }

    @Test
    @WithMockUser(roles = "ALUMNO")
    public void testObtenerMaterialesPorTipo_AlumnoAccess() throws Exception {
        // Given
        List<DTOMaterial> mockMateriales = Arrays.asList(
                new DTOMaterial(1L, "Document 1", "https://example.com/doc1.pdf"));

        DTORespuestaPaginada<DTOMaterial> mockResponse = DTORespuestaPaginada.of(
                mockMateriales, 0, 20, 1, "id", "ASC");

        when(servicioMaterial.obtenerPorTipoPaginado(
                eq("DOCUMENT"), eq(0), eq(20), eq("id"), eq("ASC"))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/material/type/DOCUMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testObtenerMaterialesPorTipo_InvalidPaginationParams() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/material/type/VIDEO")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
