package app.other;

import app.dtos.ErrorResponse;
import app.excepciones.ResourceNotFoundException;
import app.excepciones.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ErrorHandlingTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Test
    public void testResourceNotFoundException() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/api/clases/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/clases/999999"));
    }

    @Test
    public void testTypeMismatchException() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/api/clases/NaN"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errorCode").value("TYPE_MISMATCH_ERROR"))
                .andExpect(jsonPath("$.message").value("El parámetro 'id' con valor 'NaN' no es válido"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/clases/NaN"));
    }

    @Test
    public void testEndpointNotFound() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/api/non-existent-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENDPOINT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("El endpoint solicitado no existe"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/non-existent-endpoint"));
    }

    @Test
    public void testValidationException() {
        // Test the exception structure
        ValidationException exception = new ValidationException("Test validation error");
        
        assertEquals(400, exception.getStatus().value());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals("Test validation error", exception.getUserMessage());
    }

    @Test
    public void testResourceNotFoundExceptionStructure() {
        // Test the exception structure
        ResourceNotFoundException exception = new ResourceNotFoundException("Clase", "ID", 123);
        
        assertEquals(404, exception.getStatus().value());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertEquals("El clase solicitado no existe", exception.getUserMessage());
    }
}
