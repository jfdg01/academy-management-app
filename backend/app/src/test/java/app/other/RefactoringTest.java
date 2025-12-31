package app.other;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import app.rest.ClaseManagementRest;
import app.rest.ClaseRest;
import app.rest.EntregaEjercicioRest;
import app.rest.UserClaseRest;
import app.servicios.ServicioClase;
import app.servicios.ServicioEntregaEjercicio;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test básico para verificar que los controladores refactorizados se pueden instanciar correctamente
 */
@SpringBootTest
@ActiveProfiles("test")
public class RefactoringTest {

    @Autowired(required = false)
    private ClaseRest claseRest;

    @Autowired(required = false)
    private EntregaEjercicioRest entregaEjercicioRest;

    @Autowired(required = false)
    private ClaseManagementRest claseManagementRest;

    @Autowired(required = false)
    private UserClaseRest userClaseRest;

    @MockBean
    private ServicioClase servicioClase;
    
    @MockBean
    private ServicioEntregaEjercicio servicioEntregaEjercicio;

    @Test
    public void testClaseRestInstantiation() {
        // Verificar que el controlador principal se puede instanciar
        assertNotNull(claseRest, "ClaseRest should be instantiated");
    }

    @Test
    public void testEntregaEjercicioRestInstantiation() {
        // Verificar que el controlador de entregas de ejercicios se puede instanciar
        assertNotNull(entregaEjercicioRest, "EntregaEjercicioRest should be instantiated");
    }

    @Test
    public void testClaseManagementRestInstantiation() {
        // Verificar que el controlador de gestión se puede instanciar
        assertNotNull(claseManagementRest, "ClaseManagementRest should be instantiated");
    }

    @Test
    public void testUserClaseRestInstantiation() {
        // Verificar que el controlador de usuario se puede instanciar
        assertNotNull(userClaseRest, "UserClaseRest should be instantiated");
    }
}
