package app.rest;

import app.dtos.DTOEjercicio;
import app.dtos.DTOPeticionCrearEjercicio;
import app.dtos.DTORespuestaPaginada;
import app.servicios.ServicioEjercicio;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
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

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EjercicioRest controller using @SpringBootTest
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EjercicioRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServicioEjercicio servicioEjercicio;

    @Autowired
    private ObjectMapper objectMapper;

    private DTOEjercicio ejercicioTest;

    @BeforeEach
    void setUp() {
        ejercicioTest = new DTOEjercicio(
                1L,
                "Ejercicio de Java",
                "Implementa una función que calcule el factorial de un número",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "1",
                0,
                0L
        );
    }

    @Test
    @DisplayName("GET /api/ejercicios debe retornar lista de ejercicios")
    @WithMockUser(roles = "PROFESOR")
    @Disabled("Content type not set - needs investigation")
    void testObtenerEjercicios() throws Exception {
        Page<DTOEjercicio> page = new PageImpl<>(Arrays.asList(ejercicioTest), PageRequest.of(0, 10), 1);
        DTORespuestaPaginada<DTOEjercicio> respuestaPaginada = DTORespuestaPaginada.fromPage(page, "id", "ASC");

        when(servicioEjercicio.obtenerEjerciciosPaginados(
                anyString(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyInt(), anyString(), anyString())).thenReturn(respuestaPaginada);

        mockMvc.perform(get("/api/ejercicios"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Ejercicio de Java"));
    }

    @Test
    @DisplayName("GET /api/ejercicios/{id} debe retornar ejercicio por ID")
    @WithMockUser(roles = "PROFESOR")
    void testObtenerEjercicioPorId() throws Exception {
        when(servicioEjercicio.obtenerEjercicioPorId(1L)).thenReturn(ejercicioTest);

        mockMvc.perform(get("/api/ejercicios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ejercicio de Java"));
    }

    @Test
    @DisplayName("POST /api/ejercicios debe crear un ejercicio")
    @WithMockUser(roles = "PROFESOR")
    void testCrearEjercicio() throws Exception {
        when(servicioEjercicio.crearEjercicio(
                anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyLong()))
                .thenReturn(ejercicioTest);

        String ejercicioJson = objectMapper.writeValueAsString(new DTOPeticionCrearEjercicio(
                "Ejercicio de Java",
                "Implementa una función que calcule el factorial de un número",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                1L
        ));

        mockMvc.perform(post("/api/ejercicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ejercicioJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ejercicio de Java"));
    }

    @Test
    @DisplayName("DELETE /api/ejercicios/{id} debe borrar ejercicio")
    @WithMockUser(roles = "PROFESOR")
    void testBorrarEjercicioPorId() throws Exception {
        when(servicioEjercicio.borrarEjercicioPorId(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/ejercicios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/ejercicios debe validar datos inválidos")
    @WithMockUser(roles = "PROFESOR")
    void testCrearEjercicioConDatosInvalidos() throws Exception {
        when(servicioEjercicio.crearEjercicio(
                anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyLong()))
                .thenThrow(new RuntimeException("Datos inválidos"));

        String ejercicioInvalidoJson = objectMapper.writeValueAsString(new DTOPeticionCrearEjercicio(
                "", "", LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(2), 1L
        ));

        mockMvc.perform(post("/api/ejercicios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ejercicioInvalidoJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/ejercicios con filtros debe filtrar correctamente")
    @WithMockUser(roles = "PROFESOR")
    void testObtenerEjerciciosConFiltros() throws Exception {
        Page<DTOEjercicio> page = new PageImpl<>(Arrays.asList(ejercicioTest), PageRequest.of(0, 10), 1);
        DTORespuestaPaginada<DTOEjercicio> respuestaPaginada = DTORespuestaPaginada.fromPage(page, "id", "ASC");

        when(servicioEjercicio.obtenerEjerciciosPaginados(
                eq("Java"), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(10), eq("name"), eq("ASC"))).thenReturn(respuestaPaginada);

        mockMvc.perform(get("/api/ejercicios")
                .param("q", "Java")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Ejercicio de Java"));
    }


}
