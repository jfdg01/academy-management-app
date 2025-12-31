package app.util.datainit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import app.servicios.ServicioCachePassword;

import java.util.Random;

@Component
public abstract class BaseDataInitializer {
    
    @Autowired
    protected ApplicationContext context;
    
    @Autowired
    protected ServicioCachePassword servicioCachePassword;
    
    protected final Random random = new Random();

    protected abstract void initialize();

    /**
     * Encode a password using the password cache service
     * @param rawPassword The raw password to encode
     * @return The encoded password
     */
    protected String encodePassword(String rawPassword) {
        return servicioCachePassword.encodePassword(rawPassword);
    }

    protected String[] generateRandomNames() {
        // Names and surnames that match the pattern [a-zA-ZáéíóúÁÉÍÓÚñÑ\s]
        String[] nombres = { 
            "Juan", "María", "Pedro", "Ana", "Luis", 
            "Carmen", "José", "Isabel", "Antonio", "Laura",
            "Ángel", "Sofía", "Nicolás", "Lucía", "Andrés"
        };
        String[] apellidos = { 
            "García", "Rodríguez", "González", "Fernández", "López", 
            "Martínez", "Sánchez", "Pérez", "Gómez", "Martín",
            "Jiménez", "Hernández", "Díaz", "Álvarez", "Muñoz"
        };

        String nombre = nombres[random.nextInt(nombres.length)];
        String apellido1 = apellidos[random.nextInt(apellidos.length)];
        String apellido2 = apellidos[random.nextInt(apellidos.length)];

        // Ensure we don't have the same surname twice
        while (apellido2.equals(apellido1)) {
            apellido2 = apellidos[random.nextInt(apellidos.length)];
        }

        return new String[] { nombre, apellido1, apellido2 };
    }

    protected String generateRandomEmail(String nombre, String apellido1, int index) {
        String[] domains = { "gmail.com", "hotmail.com", "yahoo.com", "outlook.com" };
        String domain = domains[random.nextInt(domains.length)];
        
        // Remove accents and special characters
        String localPart = removeAccents(nombre + "." + apellido1);
        
        // Add index to ensure uniqueness
        localPart = localPart + index;
        
        // Ensure local part is not too long (max 64 chars)
        if (localPart.length() > 60) {
            localPart = localPart.substring(0, 60);
        }
        
        // Remove any invalid characters
        localPart = localPart.replaceAll("[^a-zA-Z0-9._%+-]", "");
        
        return String.format("%s@%s", localPart, domain);
    }

    protected String generateRandomPhone() {
        // Spanish mobile phone format: 6XXXXXXXX or 7XXXXXXXX
        StringBuilder phone = new StringBuilder();
        phone.append(random.nextInt(2) == 0 ? "6" : "7"); // Start with 6 or 7
        
        // Add 8 random digits
        for (int i = 0; i < 8; i++) {
            phone.append(random.nextInt(10));
        }
        
        return phone.toString();
    }

    protected String generateRandomDNI() {
        StringBuilder dni = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            dni.append(random.nextInt(10));
        }
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        int numero = Integer.parseInt(dni.toString());
        dni.append(letras[numero % 23]);
        return dni.toString();
    }
    
    protected String generateUniqueDNI(int index) {
        // Use index to ensure uniqueness
        StringBuilder dni = new StringBuilder();
        String indexStr = String.valueOf(index);
        
        // Fill with zeros to make it 8 digits
        while (dni.length() + indexStr.length() < 8) {
            dni.append(random.nextInt(10));
        }
        dni.append(indexStr);
        
        // Ensure it's exactly 8 digits
        if (dni.length() > 8) {
            dni.setLength(8);
        }
        
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        int numero = Integer.parseInt(dni.toString());
        dni.append(letras[numero % 23]);
        return dni.toString();
    }
    
    /**
     * Removes accents and special characters from a string to make it username-safe
     * @param text The text to convert
     * @return The text without accents and special characters
     */
    protected String removeAccents(String text) {
        return text.toLowerCase()
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ñ', 'n');
    }
}
