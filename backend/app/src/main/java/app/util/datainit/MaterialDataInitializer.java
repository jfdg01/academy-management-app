package app.util.datainit;

import app.dtos.DTOMaterial;
import app.servicios.ServicioMaterial;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
public class MaterialDataInitializer extends BaseDataInitializer {
    private static final int MATERIALS_TO_CREATE = 45;
    private final List<DTOMaterial> createdMaterials = new ArrayList<>();

    @Override
    public void initialize() {
        // Set up security context for material creation (as a teacher)
        setupSecurityContext();
        
        ServicioMaterial servicioMaterial = context.getBean(ServicioMaterial.class);
        
        // Check if materials already exist to avoid duplicates
        if (servicioMaterial.buscar(null).size() > 0) {
            System.out.println("Materials already exist, skipping material creation");
            return;
        }
        
        // Create various types of educational materials
        createDocumentMaterials(servicioMaterial);
        createImageMaterials(servicioMaterial);
        createVideoMaterials(servicioMaterial);
        
        System.out.println("MaterialDataInitializer: Created " + createdMaterials.size() + " materials");
    }
    
    private void setupSecurityContext() {
        // Create a teacher authentication context for material creation
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PROFESOR"));
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("teacher-init", "password", authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    private void createDocumentMaterials(ServicioMaterial servicioMaterial) {
        String[] documentNames = {
            "Guía de Matemáticas Básicas",
            "Manual de Programación Java",
            "Apuntes de Física Cuántica",
            "Tutorial de Bases de Datos",
            "Documentación de Spring Boot",
            "Libro de Química Orgánica",
            "Guía de Diseño Web",
            "Manual de Inglés Empresarial",
            "Guía de Álgebra Lineal",
            "Manual de Python Avanzado",
            "Apuntes de Cálculo Diferencial",
            "Tutorial de Machine Learning",
            "Documentación de React",
            "Libro de Biología Molecular",
            "Guía de UX/UI Design"
        };
        
        String[] documentUrls = {
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-file.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-download-10-mb.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-with-images.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-file.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-download-10-mb.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-with-images.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-file.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-download-10-mb.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-file.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-download-10-mb.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-with-images.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-file.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-download-10-mb.pdf",
            "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-with-images.pdf"
        };
        
        int minLength = Math.min(documentNames.length, documentUrls.length);
        for (int i = 0; i < minLength; i++) {
            try {
                DTOMaterial material = servicioMaterial.crear(documentNames[i], documentUrls[i]);
                createdMaterials.add(material);
            } catch (Exception e) {
                System.err.println("Error creating document material: " + e.getMessage());
            }
        }
    }
    
    private void createImageMaterials(ServicioMaterial servicioMaterial) {
        String[] imageNames = {
            "Diagrama de Flujo de Datos",
            "Estructura de Base de Datos",
            "Arquitectura de Aplicación",
            "Diagrama UML de Clases",
            "Flujo de Autenticación",
            "Modelo de Datos",
            "Interfaz de Usuario",
            "Diagrama de Red",
            "Diagrama de Secuencia",
            "Arquitectura de Microservicios",
            "Flujo de Datos",
            "Modelo Entidad-Relación",
            "Interfaz de Usuario Responsiva",
            "Diagrama de Componentes",
            "Flujo de Trabajo"
        };
        
        String[] imageUrls = {
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Large-Sample-png-Image-download-for-Testing.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-PNG-File-for-Testing.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-Image-file-Download.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-Small-Image-PNG-file-Download.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Small-Sample-png-Image-File-Download.jpg",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Large-Sample-png-Image-download-for-Testing.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-PNG-File-for-Testing.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-Image-file-Download.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Large-Sample-png-Image-download-for-Testing.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-PNG-File-for-Testing.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-Image-file-Download.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-Small-Image-PNG-file-Download.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Small-Sample-png-Image-File-Download.jpg",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Large-Sample-png-Image-download-for-Testing.png",
            "https://www.learningcontainer.com/wp-content/uploads/2020/08/Sample-PNG-File-for-Testing.png"
        };
        
        int minLength = Math.min(imageNames.length, imageUrls.length);
        for (int i = 0; i < minLength; i++) {
            try {
                DTOMaterial material = servicioMaterial.crear(imageNames[i], imageUrls[i]);
                createdMaterials.add(material);
            } catch (Exception e) {
                System.err.println("Error creating image material: " + e.getMessage());
            }
        }
    }
    
    private void createVideoMaterials(ServicioMaterial servicioMaterial) {
        String[] videoNames = {
            "Tutorial de Instalación",
            "Demostración de Funcionalidades",
            "Guía de Configuración",
            "Ejercicios Prácticos",
            "Explicación de Conceptos",
            "Resolución de Problemas",
            "Mejores Prácticas",
            "Casos de Uso Reales",
            "Tutorial de Despliegue",
            "Demostración de API",
            "Guía de Testing",
            "Ejercicios de Debugging",
            "Explicación de Patrones",
            "Resolución de Errores",
            "Mejores Prácticas de Seguridad"
        };
        
        String[] videoUrls = {
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
            "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
        };
        
        int minLength = Math.min(videoNames.length, videoUrls.length);
        for (int i = 0; i < minLength; i++) {
            try {
                DTOMaterial material = servicioMaterial.crear(videoNames[i], videoUrls[i]);
                createdMaterials.add(material);
            } catch (Exception e) {
                System.err.println("Error creating video material: " + e.getMessage());
            }
        }
    }
    
    public List<DTOMaterial> getCreatedMaterials() {
        return createdMaterials;
    }
}
