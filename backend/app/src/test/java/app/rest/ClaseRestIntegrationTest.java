package app.rest;

import app.dtos.*;
import app.entidades.Material;
import app.entidades.enums.EPresencialidad;
import app.entidades.enums.EDificultad;
import app.servicios.ServicioClase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ClaseRest controller using @SpringBootTest
 * Tests only the endpoints that actually exist in the ClaseRest controller
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaseRestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicioClase servicioClase;

    @Autowired
    private ObjectMapper objectMapper;

    private DTOClase claseTest;
    private DTOPeticionCrearCurso peticionCrearCurso;
    private DTOPeticionCrearTaller peticionCrearTaller;
    private DTOMaterial material;

    @BeforeEach
    void setUp() {
        // Crear material de prueba
        material = new DTOMaterial(1L, "Apuntes de Java", "https://ejemplo.com/apuntes.pdf");

        // Crear clase de prueba
        claseTest = new DTOClase(
                1L,
                "Curso de Java Avanzado",
                "Aprende Java avanzado",
                new BigDecimal("129.99"),
                EPresencialidad.ONLINE,
                "imagen-java.jpg",
                EDificultad.AVANZADO,
                Arrays.asList(),
                Arrays.asList("profesor1"),
                Arrays.asList(),
                Arrays.asList(material),
                "CURSO"
        );

        // Crear petición de crear curso
        peticionCrearCurso = new DTOPeticionCrearCurso(
                "Curso de Java Avanzado",
                "Aprende Java avanzado",
                new BigDecimal("129.99"),
                EPresencialidad.ONLINE,
                "imagen-java.jpg",
                EDificultad.AVANZADO,
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(45),
                Arrays.asList(1L),
                Arrays.asList()
        );

        // Crear petición de crear taller
        peticionCrearTaller = new DTOPeticionCrearTaller(
                "Taller de Spring Boot",
                "Taller intensivo de Spring Boot",
                new BigDecimal("79.99"),
                EPresencialidad.PRESENCIAL,
                "imagen-spring.jpg",
                EDificultad.INTERMEDIO,
                8,
                LocalDate.now().plusDays(7),
                LocalTime.of(16, 0),
                Arrays.asList(2L),
                Arrays.asList()
        );
    }

    @Test
    @DisplayName("GET /api/clases debe retornar lista de clases")
    @WithMockUser(roles = "ADMIN")
    void testObtenerClases() throws Exception {
        Page<DTOClase> page = new PageImpl<>(Arrays.asList(claseTest), PageRequest.of(0, 10), 1);
        DTORespuestaPaginada<DTOClase> respuestaPaginada = DTORespuestaPaginada.fromPage(page, "id", "ASC");

        when(servicioClase.buscarClasesSegunRol(any(DTOParametrosBusquedaClase.class))).thenReturn(respuestaPaginada);

        mockMvc.perform(get("/api/clases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].titulo").value("Curso de Java Avanzado"));
    }

    @Test
    @DisplayName("GET /api/clases/{id} debe retornar clase por ID")
    @WithMockUser(roles = "ADMIN")
    void testObtenerClasePorId() throws Exception {
        when(servicioClase.obtenerClasePorId(1L)).thenReturn(claseTest);

        mockMvc.perform(get("/api/clases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Curso de Java Avanzado"));
    }

    @Test
    @DisplayName("POST /api/clases/cursos debe crear un curso")
    @WithMockUser(roles = "ADMIN")
    void testCrearCurso() throws Exception {
        DTOCurso cursoTest = new DTOCurso(
                1L,
                "Curso de Java Avanzado",
                "Aprende Java avanzado",
                new BigDecimal("129.99"),
                EPresencialidad.ONLINE,
                "imagen-java.jpg",
                EDificultad.AVANZADO,
                Arrays.asList(),
                Arrays.asList("profesor1"),
                Arrays.asList(),
                Arrays.asList(material),
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(45)
        );

        when(servicioClase.crearCurso(any(DTOPeticionCrearClase.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(cursoTest);

        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Curso de Java Avanzado"));
    }

    @Test
    @DisplayName("POST /api/clases/talleres debe crear un taller")
    @WithMockUser(roles = "ADMIN")
    void testCrearTaller() throws Exception {
        DTOTaller tallerTest = new DTOTaller(
                2L,
                "Taller de Spring Boot",
                "Taller intensivo de Spring Boot",
                new BigDecimal("79.99"),
                EPresencialidad.PRESENCIAL,
                "imagen-spring.jpg",
                EDificultad.INTERMEDIO,
                Arrays.asList(),
                Arrays.asList("profesor2"),
                Arrays.asList(),
                Arrays.asList(),
                8,
                LocalDate.now().plusDays(7),
                LocalTime.of(16, 0)
        );

        when(servicioClase.crearTaller(any(DTOPeticionCrearClase.class), any(Integer.class), any(LocalDate.class), any(LocalTime.class))).thenReturn(tallerTest);

        mockMvc.perform(post("/api/clases/talleres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearTaller)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value("Taller de Spring Boot"));
    }

    @Test
    @DisplayName("DELETE /api/clases/{id} debe borrar clase")
    @WithMockUser(roles = "ADMIN")
    void testBorrarClasePorId() throws Exception {
        when(servicioClase.borrarClasePorId(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/clases/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/clases/cursos debe validar datos inválidos")
    @WithMockUser(roles = "ADMIN")
    void testCrearCursoConDatosInvalidos() throws Exception {
        DTOPeticionCrearCurso peticionInvalida = new DTOPeticionCrearCurso(
                "Curso Inválido", "Curso con datos inválidos", new BigDecimal("-10"),
                EPresencialidad.ONLINE, "imagen.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(2),
                Arrays.asList(1L), Arrays.asList());

        when(servicioClase.crearCurso(any(DTOPeticionCrearClase.class), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new app.excepciones.ValidationException("Datos inválidos"));

        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/clases/cursos debe crear múltiples cursos")
    @WithMockUser(roles = "ADMIN")
    void testCrearMultiplesCursos() throws Exception {
        DTOCurso cursoTest = new DTOCurso(
                1L,
                "Curso de Java Avanzado",
                "Aprende Java avanzado",
                new BigDecimal("129.99"),
                EPresencialidad.ONLINE,
                "imagen-java.jpg",
                EDificultad.AVANZADO,
                Arrays.asList(),
                Arrays.asList("profesor1"),
                Arrays.asList(),
                Arrays.asList(material),
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(45)
        );

        when(servicioClase.crearCurso(any(DTOPeticionCrearClase.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(cursoTest);

        // Crear primer curso
        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peticionCrearCurso)))
                .andExpect(status().isCreated());

        // Crear segundo curso
        DTOPeticionCrearCurso segundoCurso = new DTOPeticionCrearCurso(
                "Curso de Python", "Aprende Python desde cero", new BigDecimal("99.99"),
                EPresencialidad.ONLINE, "imagen-python.jpg", EDificultad.PRINCIPIANTE,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(40),
                Arrays.asList(2L), Arrays.asList());

        DTOCurso cursoPython = new DTOCurso(
                2L,
                "Curso de Python",
                "Aprende Python desde cero",
                new BigDecimal("99.99"),
                EPresencialidad.ONLINE,
                "imagen-python.jpg",
                EDificultad.PRINCIPIANTE,
                Arrays.asList(),
                Arrays.asList("profesor2"),
                Arrays.asList(),
                Arrays.asList(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(40)
        );
        when(servicioClase.crearCurso(any(DTOPeticionCrearClase.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(cursoPython);

        mockMvc.perform(post("/api/clases/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(segundoCurso)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Curso de Python"));
    }
}
